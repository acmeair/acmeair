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
var fs = require('fs');
var settings = JSON.parse(fs.readFileSync('settings.json', 'utf8'));

var mongodb = require('mongodb');
var uuid = require('node-uuid');
var log4js = require('log4js');
var flightCache = require('ttl-lru-cache')({maxLength:settings.flightDataCacheMaxSize});
var flightSegmentCache = require('ttl-lru-cache')({maxLength:settings.flightDataCacheMaxSize});
var flightDataCacheTTL = settings.flightDataCacheTTL == -1 ? null : settings.flightDataCacheTTL; 

var logger = log4js.getLogger();
logger.setLevel(settings.loggerLevel);

var DATABASE_PARALLELISM = 5;
var dbClient = null;

exports.checkForValidSessionCookie = function(req, res, next) {
	logger.debug('checkForValidCookie');
	var sessionid = req.cookies.sessionid;
	if (sessionid) {
		sessiondid = sessionid.trim();
	}
	if (!sessionid || sessionid == '') {
		logger.debug('checkForValidCookie - no sessionid cookie so returning 403');
		res.send(403);
		return;
	}
	
	validateSession(sessionid, function(err, customerSession) {
		if (err) {
			logger.debug('checkForValidCookie - system error validating session so returning 500');
			res.send(500);
			return;
		}
		
		if (customerSession) {
			logger.debug('checkForValidCookie - good session so allowing next route handler to be called');
			req.acmeair_login_user = customerSession.customerid;
			next();
			return;
		}
		else {
			logger.debug('checkForValidCookie - bad session so returning 403');
			res.send(403);
			return;
		}
	});
}

exports.login = function(req, res) {
	logger.debug('logging in user');
	var login = req.body.login;
	var password = req.body.password;
	
	// replace eventually with call to business logic to validate customer
	validateCustomer(login, password, function(err, customerValid) {
		if (err) {
			res.send(500); // TODO: do I really need this or is there a cleaner way??
		}
		
		if (!customerValid) {
			res.send(403);
		}
		else {
			createSession(login, function(err, customerSession) {
				if (err) {
					res.send(500);
				}
				
				res.cookie('sessionid', customerSession._id);
				res.send('logged in');
			});
		}
	});
};

exports.logout = function(req, res) {
	logger.debug('logging out user');
	
	var sessionid = req.cookies.sessionid;
	var login = req.body.login;
	invalidateSession(sessionid, function(err) {
		res.cookie('sessionid', '');
		res.send('logged out');
	});
	
};

exports.queryflights = function(req, res) {
	logger.debug('querying flights');
	
	var fromAirport = req.body.fromAirport;
	var toAirport = req.body.toAirport;
	var fromDateWeb = new Date(req.body.fromDate);
	var fromDate = new Date(fromDateWeb.getFullYear(), fromDateWeb.getMonth(), fromDateWeb.getDate()); // convert date to local timezone
	var oneWay = (req.body.oneWay == 'true');
	var returnDateWeb = new Date(req.body.returnDate);
	var returnDate;
	if (!oneWay) {
		returnDate = new Date(returnDateWeb.getFullYear(), returnDateWeb.getMonth(), returnDateWeb.getDate()); // convert date to local timezone
	}
	
	getFlightByAirportsAndDepartureDate(fromAirport, toAirport, fromDate, function (error, flightSegmentOutbound, flightsOutbound) {
		logger.debug('flightsOutbound = ' + flightsOutbound);
		if (flightsOutbound) {
			for (ii = 0; ii < flightsOutbound.length; ii++) {
				flightsOutbound[ii].flightSegment = flightSegmentOutbound;
			}
		}
		else {
			flightsOutbound = [];
		}
		if (!oneWay) {
			getFlightByAirportsAndDepartureDate(toAirport, fromAirport, returnDate, function (error, flightSegmentReturn, flightsReturn) {
				logger.debug('flightsReturn = ' + JSON.stringify(flightsReturn));
				if (flightsReturn) {
					for (ii = 0; ii < flightsReturn.length; ii++) {
						flightsReturn[ii].flightSegment = flightSegmentReturn;
					}
				}
				else {
					flightsReturn = [];
				}
				var options = {"tripFlights":
					[
					 {"numPages":1,"flightsOptions": flightsOutbound,"currentPage":0,"hasMoreOptions":false,"pageSize":10},
					 {"numPages":1,"flightsOptions": flightsReturn,"currentPage":0,"hasMoreOptions":false,"pageSize":10}
					], "tripLegs":2};
				res.send(options);
			});
		}
		else {
			var options = {"tripFlights":
				[
				 {"numPages":1,"flightsOptions": flightsOutbound,"currentPage":0,"hasMoreOptions":false,"pageSize":10}
				], "tripLegs":1};
			res.send(options);
		}
	});
};

exports.bookflights = function(req, res) {
	logger.debug('booking flights');
	
	var userid = req.body.userid;
	var toFlight = req.body.toFlight;
	var retFlight = req.body.retFlight;
	var oneWay = (req.body.oneWayFlight == 'true');
	
	bookFlight(toFlight, userid, function (error, toBookingId) {
		if (!oneWay) {
			bookFlight(retFlight, userid, function (error, retBookingId) {
				var bookingInfo = {"oneWay":false,"returnBookingId":retBookingId,"departBookingId":toBookingId};
				res.header('Cache-Control', 'no-cache');
				res.send(bookingInfo);
			});
		}
		else {
			var bookingInfo = {"oneWay":true,"departBookingId":toBookingId};
			res.header('Cache-Control', 'no-cache');
			res.send(bookingInfo);
		}
	});
};

exports.cancelBooking = function(req, res) {
	logger.debug('canceling booking');
	
	var number = req.body.number;
	var userid = req.body.userid;
	
	cancelBooking(number, userid, function (error) {
		if (error) {
			res.send({'status':'error'});
		}
		else {
			res.send({'status':'success'});
		}
	});
};

exports.bookingsByUser = function(req, res) {
	logger.debug('listing booked flights by user ' + req.params.user);

	getBookingsByUser(req.params.user, function(err, bookings) {
		if (err) {
			res.send(500);
		}
		res.send(bookings);
	});
};

exports.getCustomerById = function(req, res) {
	logger.debug('getting customer by user ' + req.params.user);

	getCustomer(req.params.user, function(err, customer) {
		if (err) {
			res.send(500);
		}
		res.send(customer);
	});
};

exports.putCustomerById = function(req, res) {
	logger.debug('putting customer by user ' + req.params.user);
	
	updateCustomer(req.params.user, req.body, function(err, customer) {
		if (err) {
			res.send(500);
		}
		res.send(customer);
	});
};

function validateCustomer(username, password, callback /* (error, boolean validCustomer) */) {
	var collection = new mongodb.Collection(dbClient, 'customer');
	  
	collection.find({_id: username}).toArray(function(err, docs) {
		if (err) callback (err, null);
		var customer = docs[0];
		callback(null, customer.password == password);
	});
};

function createSession(customerId, callback /* (error, CustomerSession) */) {
	var collection = new mongodb.Collection(dbClient, 'customerSession');
		
	var now = new Date();
	var later = new Date(now.getTime() + 1000*60*60*24);
		
	var document = { "_id" : uuid.v4(), "customerid" : customerId, "lastAccessedTime" : now, "timeoutTime" : later };
	  
	collection.insert(document, {safe: true}, function(err, records){
		callback(err, document);
	});
}

function validateSession(sessionId, callback /* (error, CustomerSession) */) {
	var collection = new mongodb.Collection(dbClient, 'customerSession');
		
	var now = new Date();
		
	collection.find({_id: sessionId}).toArray(function(err, docs) {
		if (err) callback (err, null);
		if (docs.length == 0) {
			callback(null, null);
			return;
		}
		var session = docs[0];
		if (now > session.timeoutTime) {
			collection.remove({_id: sessionId}, {safe: true}, function(err, numDocs) {
				if (err) callback (err, null);
				callback(null, null);
				return;
			});
		}
		callback(null, session);
	});
}

function getCustomer(username, callback /* (error, Customer) */) {
	var collection = new mongodb.Collection(dbClient, 'customer');
	  
	collection.find({_id: username}).toArray(function(err, docs) {
		if (err) callback (err, null);
		var customer = docs[0];
		callback(null, customer);
	});
}

function updateCustomer(login, customer, callback /* (error, Customer) */) {
	var collection = new mongodb.Collection(dbClient, 'customer');
	  
	collection.update({_id: login}, customer, {safe: true}, function(err, numUpdates) {
		logger.debug(numUpdates);
		callback(err, customer);
	});
}

function getBookingsByUser(username, callback /* (error, Bookings) */) {
	var collection = new mongodb.Collection(dbClient, 'booking');
	  
	collection.find({customerId: username}).toArray(function(err, docs) {
		if (err) callback (err, null);
		callback(null, docs);
	});
}

function invalidateSession(sessionid, callback /* error */) {
	var collection = new mongodb.Collection(dbClient, 'customerSession');
	  
	collection.remove({_id: sessionid}, {safe: true}, function(err, numDocs) {
		if (err) callback (err);
		callback(null);
	});
}

function invalidateAllUserSessions(login, callback /* error */) {
	var collection = new mongodb.Collection(dbClient, 'customerSession');
	  
	collection.remove({customerid: login}, {safe: true}, function(err, numDocs) {
		if (err) callback (err);
		callback(null);
	});
}

function getFlightByAirportsAndDepartureDate(fromAirport, toAirport, flightDate, callback /* error, flightSegment, flights[] */) {
	logger.debug("getFlightByAirportsAndDepartureDate " + fromAirport + " " + toAirport + " " + flightDate);
	
	getFlightSegmentByOriginPortAndDestPort(fromAirport, toAirport, function(error, flightsegment) {
		if (error) throw error;
		
		logger.debug("flightsegment = " + JSON.stringify(flightsegment));
		if (!flightsegment) {
			callback(null, null, null);
			return;
		}
		
		var collection = new mongodb.Collection(dbClient, 'flight');
		
		dayAtMightnight = new Date(flightDate.getFullYear(), flightDate.getMonth(), flightDate.getDate());
		milliseconds = 24 /* hours */ * 60 /* minutes */ * 60 /* seconds */ * 1000 /* milliseconds */;
		nextDayAtMightnight = new Date(dayAtMightnight.getTime() + milliseconds);

		var cacheKey = flightsegment._id + "-" + dayAtMightnight.getTime();
		if (settings.useFlightDataRelatedCaching) {
			//var cacheKey = flightsegment._id + "-" + dayAtMightnight.getTime();
			var flights = flightCache.get(cacheKey);
			if (flights) {
				logger.debug("cache hit - flight search, key = " + cacheKey);
				callback(null, flightsegment, (flights == "NULL" ? null : flights));
				return;
			}
			// else - fall into loading from data store
			logger.debug("cache miss - flight search, key = " + cacheKey + " flightCache size = " + flightCache.size());
		}
		
		var searchCriteria = {flightSegmentId: flightsegment._id, scheduledDepartureTime: {$gte: dayAtMightnight, $lt: nextDayAtMightnight}};
		logger.debug('searchCriteria = ' + JSON.stringify(searchCriteria));
		collection.find(searchCriteria).toArray(function(err, docs) {
			if (err) {
				callback (err, null, null);
			}
			logger.debug("after cache miss - key = " + cacheKey + ", docs = " + JSON.stringify(docs));

			var docsEmpty = !docs || docs.length == 0;
			
			if (settings.useFlightDataRelatedCaching) {
				//var cacheKey = flightsegment._id + "-" + dayAtMightnight.getTime();
				var cacheValue = (docsEmpty ? "NULL" : docs);
				logger.debug("about to populate the cache with flights key = " + cacheKey + " with value of " + JSON.stringify(cacheValue));
				flightCache.set(cacheKey, cacheValue, flightDataCacheTTL);
				logger.debug("after cache populate with key = " + cacheKey + ", flightCacheSize = " + flightCache.size())
			}
			
			callback(null, flightsegment, docs);
		});
	});
}

function getFlightSegmentByOriginPortAndDestPort(fromAirport, toAirport, callback /* error, flightsegment */) {
	var segment;
	
	if (settings.useFlightDataRelatedCaching) {
		segment = flightSegmentCache.get(fromAirport+toAirport);
		if (segment) {
			logger.debug("cache hit - flightsegment search, key = " + fromAirport+toAirport);
			callback(null, (segment == "NULL" ? null : segment));
			return;
		}
		// else - fall into loading from data store
		logger.debug("cache miss - flightsegment search, key = " + fromAirport+toAirport + ", flightSegmentCache size = " + flightSegmentCache.size());
	}
	
	var collection = new mongodb.Collection(dbClient, 'flightSegment');
	  
	collection.find({originPort: fromAirport, destPort: toAirport}).toArray(function(err, docs) {
		if (err) callback (err, null);
			
		segment = docs[0];
		if (segment == undefined) {
			segment = null;
		}
		
		if (settings.useFlightDataRelatedCaching) {
			logger.debug("about to populate the cache with flightsegment key = " + fromAirport+toAirport + " with value of " + JSON.stringify(docs));
			flightSegmentCache.set(fromAirport+toAirport, (segment == null ? "NULL" : segment), flightDataCacheTTL);
			logger.debug("after cache populate with key = " + fromAirport+toAirport + ", flightSegmentCacheSize = " + flightSegmentCache.size())
		}
		callback(null, segment);
	});
}

function bookFlight(flightId, userid, callback /* (error, bookingId) */) {
	var collection = new mongodb.Collection(dbClient, 'booking');
		
	var now = new Date();
	var docId = uuid.v4();

	var document = { "_id" : docId, "customerId" : userid, "flightId" : flightId, "dateOfBooking" : now };
	  
	collection.insert(document, {safe: true}, function(err, records){
		callback(err, docId);
	});
}

function cancelBooking(bookingid, userid, callback /*(error)*/) {
	var collection = new mongodb.Collection(dbClient, 'booking');
	
	collection.remove({"_id" : bookingid, "customerId" : userid}, null, function(err, removed) {
		callback(err);
	});
}

exports.initializeDatabaseConnections = function(callback/*(error)*/) {
	var dbServer = new mongodb.Server(settings.mongoHost, settings.mongoPort, {poolSize: DATABASE_PARALLELISM});
	var dbDatabase = new mongodb.Db('acmeair', dbServer, {}).open(function (error, client) {
		if (error) {
			callback(error);
		}
		dbClient = client;
		callback();
		return;
	});
}