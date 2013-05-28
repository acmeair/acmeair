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
var express = require('express')
  , http = require('http')
  , fs = require('fs');

var settings = JSON.parse(fs.readFileSync('settings.json', 'utf8'));

var routes = require('./routes');

var app = express();

app.configure(function(){
  app.set('port', process.env.PORT || 3000);
  if (settings.useDevLogger) {
	  app.use(express.logger('dev'));
  }
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(express.cookieParser());
  //app.use(express.session());
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
});

app.configure('development', function(){
  app.use(express.errorHandler());
});

app.post('/rest/api/login', routes.login);
app.get('/rest/api/login/logout', routes.logout);
app.post('/rest/api/flights/queryflights', routes.checkForValidSessionCookie, routes.queryflights);
app.post('/rest/api/bookings/bookflights', routes.checkForValidSessionCookie, routes.bookflights);
app.post('/rest/api/bookings/cancelbooking', routes.checkForValidSessionCookie, routes.cancelBooking);
app.get('/rest/api/bookings/byuser/:user', routes.checkForValidSessionCookie, routes.bookingsByUser);
app.get('/rest/api/customer/byid/:user', routes.checkForValidSessionCookie, routes.getCustomerById);
app.post('/rest/api/customer/byid/:user', routes.checkForValidSessionCookie, routes.putCustomerById);

routes.initializeDatabaseConnections(function(error) {
	if (error) {
		console.log('Error connecting to mongo - exiting process');
		process.exit(1);
	}
	console.log("initializing database connections");
	if (error) {
		throw error;
	}
	startServer();
});

function startServer() {
	http.createServer(app).listen(app.get('port'), function(){
		console.log("Express server listening on port " + app.get('port'));
	});
}