/*******************************************************************************
* Copyright (c) 2013 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
var mongodb = require('mongodb');
var csv = require('csv');
var log4js = require('log4js');
var uuid = require('node-uuid');
var async = require('async');
var fs = require('fs');

var logger = log4js.getLogger();
logger.setLevel('INFO');

var dbSettings = JSON.parse(fs.readFileSync('../settings.json', 'utf8'));
var loaderSettings = JSON.parse(fs.readFileSync('loader-settings.json', 'utf8'));

var SKIP_DATABASE = false;
var DATABASE_PARALLELISM = 5;

var nowAtMidnight = getDateAtTwelveAM(new Date());

var dbClient = null;

function initializeDatabaseConnections(callback/*(error)*/) {
	if (!SKIP_DATABASE) {
		var dbServer = new mongodb.Server(dbSettings.mongoHost, dbSettings.mongoPort, {poolSize: DATABASE_PARALLELISM});
		var dbDatabase = new mongodb.Db('acmeair', dbServer, {}).open(function (error, client) {
			if (error) {
				callback(error);
			}
			dbClient = client;
			callback();
			return;
		});
	}
	else {
		callback();
	}
}

function closeDatabaseConnections() {
	if (!SKIP_DATABASE) {
		dbClient.close();
		dcClient = null;
	}
}

var customerTemplate = {
    _id : undefined,
    password : "password",
    status : "GOLD",
    total_miles : 1000000,
    miles_ytd : 1000,
    address : {
        streetAddress1 : "123 Main St.",
        city : "Anytown",
        stateProvince : "NC",
        country : "USA",
        postalCode : "27617"
    },
    phoneNumber : "919-123-4567",
    phoneNumberType : "BUSINESS"
};

var airportCodeMappingTemplate = {
	_id : undefined,
	airportName : undefined
};

var flightSegmentTemplate = {
    _id : undefined,
    originPort : undefined,
    destPort : undefined,
    miles : undefined
};

var flightTemplate = {
    _id : undefined,
    flightSegmentId : undefined,
    scheduledDepartureTime : undefined,
    scheduledArrivalTime : undefined,
    firstClassBaseCost : 500,
    economyClassBaseCost : 200,
    numFirstClassSeats : 10,
    numEconomyClassSeats : 200,
    airplaneTypeId : "B747"
}

function cloneObjectThroughSerialization(theObject) {
	return JSON.parse(JSON.stringify(theObject));
}

function getDepartureTimeDaysFromDate(baseTime, days) {
	milliseconds = days * 24 /* hours */ * 60 /* minutes */ * 60 /* seconds */ * 1000 /* milliseconds */;
	return new Date(baseTime.getTime() + milliseconds);
}

function getArrivalTime(departureTime, mileage) {
	averageSpeed = 600.0; // 600 miles/hours
	hours = mileage / averageSpeed; // miles / miles/hour = hours
	milliseconds = hours * 60 /* minutes */ * 60 /* seconds */ * 1000 /* milliseconds */;
	return new Date(departureTime.getTime() + milliseconds);
}

function getDateAtTwelveAM(theDate) {
	return new Date(theDate.getFullYear(), theDate.getMonth(), theDate.getDate(), 0, 0, 0, 0);
}

function getDateAtRandomTopOfTheHour(theDate) {
	randomHour = Math.floor((Math.random()*23));
	return new Date(theDate.getFullYear(), theDate.getMonth(), theDate.getDate(), randomHour, 0, 0, 0);
}

function mongoInsertOne(collectionname, doc, callback /* (error, insertedDocument) */) {
	if (SKIP_DATABASE) {
		callback(null, doc);
		return;
	}
	
	var collection = new mongodb.Collection(dbClient, collectionname);
	collection.insert(doc, {safe: true}, function(err, records) {
		callback(err, records);
	});
};

function insertCustomer(customer, callback) {
	logger.debug('customer to insert = ' + JSON.stringify(customer));
	mongoInsertOne('customer', customer, function(error, customerInserted) {
		logger.debug('customer inserted = ' + JSON.stringify(customerInserted));
		callback();
	});
}

function insertAirportCodeMapping(airportCodeMapping, callback) {
	mongoInsertOne('airportCodeMapping', airportCodeMapping, function(error, airportCodeMappingInserted) {
		logger.debug('airportCodeMapping inserted = ' + JSON.stringify(airportCodeMappingInserted));
		callback();
	});
}

function insertFlightSegment(flightSegment, callback) {
	mongoInsertOne('flightSegment', flightSegment, function(error, flightSegmentInserted) {
		logger.debug('flightSegment inserted = ' + JSON.stringify(flightSegmentInserted));
		callback();
	});
}

function insertFlight(flight, callback) {
	mongoInsertOne('flight', flight, function(error, flightInserted) {
		logger.debug('flight inserted = ' + JSON.stringify(flightInserted));
		callback();
	});
}

function startLoadDatabase() {
	logger.debug('about to load database');
	customerQueue.push(customers);
}

var customerQueue = async.queue(insertCustomer, DATABASE_PARALLELISM);
customerQueue.drain = function() {
	logger.info('all customers loaded');
	airportCodeMappingQueue.push(airportCodeMappings);
}

var airportCodeMappingQueue = async.queue(insertAirportCodeMapping, DATABASE_PARALLELISM);
airportCodeMappingQueue.drain = function() {
	logger.info('all airportMappings loaded');
	flightSegmentsQueue.push(flightSegments);
}

var flightSegmentsQueue = async.queue(insertFlightSegment, DATABASE_PARALLELISM);
flightSegmentsQueue.drain = function() {
	logger.info('all flightSegments loaded');
	flightQueue.push(flights);
}

var flightQueue = async.queue(insertFlight, DATABASE_PARALLELISM);
flightQueue.drain = function() {
	logger.info('all flights loaded');
	logger.info('ending loading database');
	closeDatabaseConnections();
}


var customers = new Array();
var airportCodeMappings = new Array();
var flightSegments = new Array();
var flights = new Array();

function createCustomers() {
	for (var ii = 0; ii < loaderSettings.MAX_CUSTOMERS; ii++) {
		var customer = cloneObjectThroughSerialization(customerTemplate);
		customer._id = "uid" + ii + "@email.com";
		customers.push(customer);
	};
}

function createFlightRelatedData(callback/*()*/) {
	var rows = new Array();
	csv()
	.fromPath('mileage.csv')
	.on('data', function(data, index) {
		rows[index] = data;
	    logger.debug('#'+index+' '+JSON.stringify(data));
	})
	.on('end', function(count) {
	    logger.debug('Number of lines: ' + count);
	    logger.debug('rows.length = ' + rows.length);
		for (var ii = 0; ii < rows[0].length; ii++) {
			var airportCodeMapping = cloneObjectThroughSerialization(airportCodeMappingTemplate);
			airportCodeMapping._id = rows[1][ii];
			airportCodeMapping.airportName = rows[0][ii];
			airportCodeMappings.push(airportCodeMapping);
		}
		
		var flightSegmentId = 0;
		// actual mileages start on the third (2) row
		for (var ii = 2; ii < rows.length; ii++) {
			var fromAirportCode = rows[ii][1];
			// format of the row is "long airport name name" (0), "airport code" (1), mileage to first airport in rows 0/1 (2), mileage to second airport in rows 0/1 (3), ... mileage to last airport in rows 0/1 (length)
			for (var jj = 2; jj < rows[ii].length; jj++) {
				toAirportCode = rows[1][jj-2];
				mileage = rows[ii][jj];
				if (mileage != 'NA') {
					var flightSegment = cloneObjectThroughSerialization(flightSegmentTemplate);
					flightSegment._id = 'AA' + flightSegmentId++;
					flightSegment.originPort = fromAirportCode;
					flightSegment.destPort = toAirportCode;
					flightSegment.miles = mileage;
					flightSegments.push(flightSegment);
					
					for (var kk = 0; kk < loaderSettings.MAX_DAYS_TO_SCHEDULE_FLIGHTS; kk++) {
						for (var ll = 0; ll < loaderSettings.MAX_FLIGHTS_PER_DAY; ll++) {
							var flight = cloneObjectThroughSerialization(flightTemplate);
						    flight._id = uuid.v4();
							flight.flightSegmentId = flightSegment._id;
							
							var randomHourDate = getDateAtRandomTopOfTheHour(nowAtMidnight);
							flight.scheduledDepartureTime = getDepartureTimeDaysFromDate(randomHourDate, kk);
							flight.scheduledArrivalTime = getArrivalTime(flight.scheduledDepartureTime, mileage);
							flights.push(flight);
						}
					}
				}
			}
		}
		callback();
	});
}

logger.info('starting loading database');
initializeDatabaseConnections(function(error) {
	if (error) {
		throw error;
	}
	createCustomers();
	createFlightRelatedData(function() {
		logger.info('number of customers = ' + customers.length);
		logger.info('number of airportCodeMappings = ' + airportCodeMappings.length);
		logger.info('number of flightSegments = ' + flightSegments.length);
		logger.info('number of flights = ' + flights.length);
		if (!SKIP_DATABASE) {
			startLoadDatabase();
		}
	});
});