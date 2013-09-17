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

function login(login, password) {
	var input = {
		method : 'POST',
		returnedContentType : 'text/plain',
		path : 'acmeair-webapp/rest/api/login',
		parameters : {
			"login":login,
			"password":password
		}
	};
	return WL.Server.invokeHttp(input);
}


function logout(login) {
	var input = {
		method : 'GET',
		returnedContentType : 'text/plain',
		path : 'acmeair-webapp/rest/api/login/logout',

		parameters : {
			"login":login
		},
	};	
	return WL.Server.invokeHttp(input);
}


function queryFlights(fromAirport, toAirport, fromDate, returnDate, oneWay) {
	var input = {
		method : 'POST',
		returnedContentType : 'json',
		path : 'acmeair-webapp/rest/api/flights/queryflights',

		parameters : {
			"fromAirport":fromAirport,
			"toAirport":toAirport,
			"fromDate":fromDate,
			"returnDate":returnDate,
			"oneWayChecked":oneWay
		},
	};
	return WL.Server.invokeHttp(input);
}


function bookFlights(
			userid, 
			toFlightId, 
			toFlightSegId, 
			retFlightId, 
			retFlightSegId, 
			oneWayFlight ) {
	var input = {
		method : 'POST',
		returnedContentType : 'json',
		path : 'acmeair-webapp/rest/api/bookings/bookflights',

		parameters : {
			"userid":userid,
			"toFlightId":toFlightId,
			"toFlightSegId":toFlightSegId,
			"retFlightId":retFlightId,
			"retFlightSegId":retFlightSegId,
			"oneWayFlight":oneWayFlight
		},
	};	
	return WL.Server.invokeHttp(input);
}


function listBookings(userid) {
	var input = {
		method : 'GET',
		returnedContentType : 'json',
		path : 'acmeair-webapp/rest/api/bookings/byuser/'+userid,
		parameters : {},
	};	
	return WL.Server.invokeHttp(input);
}


function viewProfile(userid) {	
	var input = {
		method : 'GET',
		returnedContentType : 'json',
		path : 'acmeair-webapp/rest/api/customer/byid/' + userid,
		parameters : {},
	};
	return WL.Server.invokeHttp(input);
}


function updateProfile(
			username, 
			phoneNumberType, 
			phoneNumber, 
			password, 
			miles_ytd,status,
			total_miles,
			streetAddress1,
			streetAddress2,
			city,
			stateProvince,
			country,postalCode) {
	var customerJson= '{"username":"'+username
					+'","password":"'+password
					+'","phoneNumber":"'+phoneNumber
					+'","phoneNumberType":"'+phoneNumberType
					+'","miles_ytd":"'+miles_ytd
					+'","status":"'+status
					+'","total_miles":"'+total_miles
					+'","address":{"streetAddress1":"'+streetAddress1
								+'","streetAddress2":"'+streetAddress2
								+'","city":"'+city
								+'","stateProvince":"'+stateProvince
								+'","country":"'+country
								+'","postalCode":"'+postalCode
					+'"}}';
	var input = {
		method : 'POST',
		returnedContentType : 'application/json; charset=UTF-8',
		path : 'acmeair-webapp/rest/api/customer/byid/' + username,

		body:{
			content:customerJson,
			contentType:'application/json; charset=UTF-8'
		},
	};
	return WL.Server.invokeHttp(input);
}


function cancelBooking(userid, number) {
	var input = {
		method : 'POST',
		returnedContentType : 'text/plain',
		path : 'acmeair-webapp/rest/api/bookings/cancelbooking',

		parameters : {
			"userid":userid,
			"number":number
		},
	};
	return WL.Server.invokeHttp(input);
}


function onAuthRequired(headers, errorMessage) {
	errorMessage = errorMessage ? errorMessage : null;
	
	return {
		authRequired: true,
		errorMessage: errorMessage
	};
}


function submitAuthentication(username) {
	var userIdentity = {
		userId: username,
		displayName: username, 
		attributes: {
			foo: "bar"
		}
	};

	WL.Server.setActiveUser("AcmeairMobileRealm", userIdentity);
		
	return { 
		authRequired: false 
	};
}


function onLogout() {
	WL.Server.setActiveUser("AcmeairMobileRealm", null);
	WL.Logger.debug("Logged out");
}
