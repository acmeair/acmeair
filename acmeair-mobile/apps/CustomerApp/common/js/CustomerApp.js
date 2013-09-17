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

function wlCommonInit(){

}


function loadBookingData(){
    		
	var invocationData = {
        adapter : 'acmeairAdapter',
        procedure : 'listBookings',
		parameters : [ $("#username").val() ]
	};

    WL.Client.invokeProcedure(invocationData,{
		onSuccess: loadBookingSuccess,
		onFailure: loadBookingFailure,
		invocationContext: {}
		});
}


function loadBookingSuccess(result){
	WL.Logger.debug("loadBooking was successful. ");	
	
	var authRequired = result.invocationResult.authRequired;	
	if(authRequired == true){
		WL.SimpleDialog.show("Acme Air Error", 
				"Unable to retrieve bookings. Please Log in first.", 
				[{text: "Ok", handler: function() {
						window.location.hash = '#login'; 
					}
				}]);		
	}
	
	
	$("#bookingsList").empty();
	$.each( result.invocationResult.array, function( i, item ) {
		var li = $('<li> </li>');
		
		var a = $('<a href="#MyFlightDetails"> </a>')
			.append("<h3>"+ item.flight.flightSegmentId +"</h3>")
			.append("<p>Departing: " + (new Date(item.flight.scheduledDepartureTime).toLocaleTimeString()) +"</p>")
			.append("<p>Arriving: " + (new Date(item.flight.scheduledArrivalTime).toLocaleTimeString()) +"</p>")
			.append('<p class="ui-li-aside"><strong>' + (new Date(item.flight.scheduledDepartureTime).toLocaleDateString())+'</strong></p>')
			.click((function(booking){ 				
				var content = "";
				
				content +=  "<h1>"+ booking.flight.flightSegmentId +"</h1>";
				content +=  "<p>Booking Id: "+ booking.pkey.id +"</p>";
				content +=  "<p>Airplane Type: "+ booking.flight.airplaneTypeId +"</p>";
				content +=  '<p>Departure Date:<strong>' + (new Date(booking.flight.scheduledDepartureTime).toLocaleDateString())+'</strong></p>';
				content +=  "<p>Departing: " + (new Date(booking.flight.scheduledDepartureTime).toLocaleTimeString()) +"</p>";
				content +=  "<p>Arriving: " + (new Date(booking.flight.scheduledArrivalTime).toLocaleTimeString()) +"</p>";
				var uname = $("#username").val();
				content += ' <a href="#" data-role="button" onClick="cancelBooking(\''+uname+'\',\''+ booking.pkey.id+'\')">Cancel Booking</a>';
				$("#FlightDetailContent").html( content );
				
			})(item));
		  li.append(a);
		  $("#bookingsList").append(li).listview("refresh");		
	});	
}


function loadBookingFailure(result){
	WL.Logger.error("load booking data failed.");
	if(result.isSuccessful == false){
		WL.SimpleDialog.show("Acme Air Error", 
			"Unable to retrieve bookings. Unable to communicate with backend service.",
			[{text: "Ok", handler: function() {
					window.location.hash = '#main'; 
				}
			}]);					
	}else {
	 	if(result.invocationResult.isSuccessful == false){
			WL.SimpleDialog.show("Acme Air Error", 
				"Unable to retrieve bookings. Please Log in first.", 
				[{text: "Ok", handler: function() {
						window.location.hash = '#login'; 
					}
				}]);
		}			
	}
}


function cancelBooking(userid, number ){
	WL.Logger.debug("cancelBooking called." + userid + " , " + number);	   	
	var invocationData = {
        adapter : 'acmeairAdapter',
        procedure : 'cancelBooking',
		parameters : [ userid, number ]
	};

    WL.Client.invokeProcedure(invocationData,{
		onSuccess: cancelBookingSuccess,
		onFailure: cancelBookingFailure,
		invocationContext: {}
		});
}
	

function cancelBookingSuccess(result){
	WL.Logger.debug("cancel booking successful.");	
	WL.Logger.debug("isSuccessful = " + result.invocationResult.isSuccessful );
			
	if(result.invocationResult.statusCode == "200"){
		WL.SimpleDialog.show("Acme Air", 
			result.invocationResult.text,
			[{text: "Ok", handler: function() {
				  window.location.hash = '#main'; 
				}
			}]);
	}
}


function cancelBookingFailure(result){
	WL.Logger.debug("cancel booking failed! ");
	WL.SimpleDialog.show("Acme Air Error", 
			"There was an error canceling your booking. lease check your internet connectivity.",
			[{text: "Ok", handler: function() {
				  window.location.hash = '#main'; 
				}
			}]);					
} 


function queryFlights(){
	var invocationData = {
	        adapter : 'acmeairAdapter',
	        procedure : 'queryFlights',
			parameters : [ $("#fromAirport").val(), $("#toAirport").val(), $("#fromDate").val(), $("#toDate").val(), $("#oneWayCheck").val() ]
		};

	    WL.Client.invokeProcedure(invocationData,{
			onSuccess: queryFlightsSuccess,
			onFailure: queryFlightsFailure,
			invocationContext: {}
			});
}


function queryFlightsSuccess(result){
	WL.Logger.debug("login successful.");		
			
	if(result.invocationResult.statusCode == "200"){					
		var tripFlights = result.invocationResult.tripFlights;
		var content = "";
		if(tripFlights[0].flightsOptions.length < 1){
			content += "<h3>No Outbound flights found matching your destination on the specfied date. </h3> ";
		}else{
			content += "<h3>Number of Outbound flights "+ tripFlights[0].flightsOptions.length + "</h3>";
			content += '<ul data-role="listview" id="OutBound" data-inset="true">';
 				
			$.each( tripFlights[0].flightsOptions, function( i, item ) {
				content += '<li><a href="#" >';
				content +=  "<h2>"+ item.flightSegmentId +"</h2>";
				content +=  "<p>"+ item.flightSegmentId +"</p>";
				content +=  "<p>Departing: " + (new Date(item.scheduledDepartureTime).toLocaleTimeString()) +"</p>";
				content +=  "<p>Arriving: " + (new Date(item.scheduledArrivalTime).toLocaleTimeString()) +"</p>";
				content +=  "<p>Price: $" + item.economyClassBaseCost +"</p>";
				content +=  '<p class="ui-li-aside"><strong>' + (new Date(item.scheduledDepartureTime).toLocaleDateString())+'</strong></p>';

				content += '</a></li>';
			});
			content += '</ul>';		 
			
			if(tripFlights.length > 1){
				content += "<h3>Number of Inbound flights "+ tripFlights[1].flightsOptions.length + "</h3>";
				
				if(tripFlights[1].flightsOptions.length >= 1){
					
					content += '<ul data-role="listview" id="OutBound" data-inset="true">';
		 				
					$.each( tripFlights[1].flightsOptions, function( i, item ) {
						content += '<li><a href="#" >';
						content +=  "<h2>"+ item.flightSegmentId +"</h2>";
						content +=  "<p>"+ item.flightSegmentId +"</p>";
						content +=  "<p>Departing: " + (new Date(item.scheduledDepartureTime).toLocaleTimeString()) +"</p>";
						content +=  "<p>Arriving: " + (new Date(item.scheduledArrivalTime).toLocaleTimeString()) +"</p>";
						content +=  "<p>Price: $" + item.economyClassBaseCost +"</p>";
						content +=  '<p class="ui-li-aside"><strong>' + (new Date(item.scheduledDepartureTime).toLocaleDateString())+'</strong></p>';

						content += '</a></li>';
					});
					content += '</ul>';		 
				}				
			}		
		}
		
		$("#SearchResultsContent").html( content );
		window.location.hash = '#SearchResults'; 
		
	}else if(result.invocationResult.statusCode == "403"){
		WL.SimpleDialog.show("Acme Air Error", 
			"There was an error logging in. Please check your username and password and try again. ",
			[{text: "Ok", handler: function() {
					window.location.hash = '#login'; 
				}
			}]);	
	}			
}


function queryFlightsFailure(result){
	WL.Logger.error("query Flights failed! ");				
	WL.SimpleDialog.show("Acme Air Error", 
			"There was an error searching for your flight. lease check your internet connectivity.",
			[{text: "Ok", handler: function() {
					window.location.hash = '#flights'; 
				}
			}]);
}


function showCustomerProfile() {
	var invocationData = {
	        adapter : 'acmeairAdapter',
	        procedure : 'viewProfile',
			parameters : [ $("#username").val()]
		};

	    WL.Client.invokeProcedure(invocationData,{
			onSuccess: showCustomerProfileSuccess,
			onFailure: showCustomerProfileFailure,
			invocationContext: {}
		});		
}


function showCustomerProfileSuccess(result){
	WL.Logger.debug("Retreiving customer profile data was successful.");		
	
	var authRequired = result.invocationResult.authRequired;	
	if(authRequired == true){
		WL.SimpleDialog.show("Acme Air Error", 
				"Unable to retrieve customer profile data. Please Log in first.", 
				[{text: "Ok", handler: function() {
						window.location.hash = '#login'; 
					}
				}]);		
	}
	
	if(result.invocationResult.statusCode == "200"){
		
		$("#accountId").val(result.invocationResult.username);
		$("#phoneNumber").val(result.invocationResult.phoneNumber);
		$("#phoneNumberType").val(result.invocationResult.phoneNumberType);
		$("#streetAddress").val(result.invocationResult.address.streetAddress1);
		$("#streetAddress2").val(result.invocationResult.address.streetAddress2);
		$("#city").val(result.invocationResult.address.city);
		$("#state").val(result.invocationResult.address.stateProvince);
		$("#postalCode").val(result.invocationResult.address.postalCode);
		$("#Country").val(result.invocationResult.address.country);		
		$("#status").val(result.invocationResult.status);
		$("#milesYTD").val(result.invocationResult.miles_ytd);
		$("#milesTotal").val(result.invocationResult.total_miles);
	}else if(result.invocationResult.statusCode == "403"){
		WL.SimpleDialog.show("Acme Air Error", 
			"There was an error retreiving your profile. Please log in first and try again. ",
			[{text: "Ok", handler: function() {
					window.location.hash = '#login'; 
				}
			}]);
	}			
}


function showCustomerProfileFailure(result){
	WL.Logger.error("Retreiving customer profile data failed! ");	
	if(result.isSuccessful == false){
		WL.SimpleDialog.show("Acme Air Error", 
			"There was an error retreiving your profile. Please check your internet connectivity.",
			[{text: "Ok", handler: function() {
					window.location.hash = '#main'; 
				}
			}]);	
	} else {
		WL.SimpleDialog.show("Acme Air Error", 
				"There was an error retreiving your profile. Please log in first, and try again.",
				[{text: "Ok", handler: function() {
						window.location.hash = '#login'; 
					}
				}]);		
	}
}



function updateCustomerProfileData() {
	var invocationData = {
	        adapter : 'acmeairAdapter',
	        procedure : 'updateProfile',
			parameters : [ $("#username").val(), 
			               $("#phoneNumberType").val(), 
			               $("#phoneNumber").val(), 
			               $("#password").val(), 
			               $("#milesYTD").val(), 
			               $("#status").val(), 
			               $("#milesTotal").val(), 
			               $("#streetAddress").val(), 
			               $("#streetAddress2").val(), 
			               $("#city").val(), 
			               $("#state").val(), 
			               $("#Country").val(), 
			               $("#postalCode").val() 
			              ]
		};

	    WL.Client.invokeProcedure(invocationData,{
			onSuccess: updateCustomerProfileSuccess,
			onFailure: updateCustomerProfileFailure,
			invocationContext: {}
		});		
}



function updateCustomerProfileSuccess(result){
	WL.Logger.debug("Updating customer profile data was successful.");		
			
	if(result.invocationResult.statusCode == "200"){
		WL.SimpleDialog.show("Acme Air", 
				"Updating customer profile data was successful.",
				[{text: "Ok", handler: function() {
						window.location.hash = '#main'; 
					}
				}]);	
		
	}else if(result.invocationResult.statusCode == "403"){
		WL.SimpleDialog.show("Acme Air Error", 
			"There was an error updating your profile. Please log in first and try again. ",
			[{text: "Ok", handler: function() {
					window.location.hash = '#login'; 
				}
			}]);	
	}			
}

function updateCustomerProfileFailure(result){
	WL.Logger.error("Updating customer profile data failed! ");				
	WL.SimpleDialog.show("Acme Air Error", 
			"There was an error retreiving your profile. lease check your internet connectivity.",
			[{text: "Ok", handler: function() {
					window.location.hash = '#main'; 
				}
			}]);	
}



function login(){    	
	var invocationData = {
        adapter : 'acmeairAdapter',
        procedure : 'login',
		parameters : [ $("#username").val(), $("#password").val() ]
	};

    WL.Client.invokeProcedure(invocationData,{
		onSuccess: loginSuccess,
		onFailure: loginFailure,
		invocationContext: {}
		});
	}
	
function loginSuccess(result){
	WL.Logger.debug("login successful.");		
	var authRequired = result.invocationResult.authRequired;	
	if(authRequired == true){
		WL.Logger.debug("authRequired is true");	
		var invocationData = {
		        adapter : 'acmeairAdapter',
		        procedure : 'submitAuthentication',
				parameters : [ $("#username").val() ]
			};

		    WL.Client.invokeProcedure(invocationData,{
				onSuccess: authSuccess,
				onFailure: loginFailure,
				invocationContext: {}
				});
			
	}else{
		WL.Logger.debug("authRequired is false");
	
	if(result.invocationResult.statusCode == "200"){
		WL.SimpleDialog.show("Acme Air", 
			"Welcome Back "+$("#username").val() +".",
			[{text: "Ok", handler: function() {
					window.location.hash = '#main'; 
				}
			}]);	
			
	}else if(result.invocationResult.statusCode == "403"){
		WL.SimpleDialog.show("Acme Air Error", 
			"There was an error logging in. Please check your username and password and try again. ",
			[{text: "Ok", handler: function() {
					window.location.hash = '#login'; 
				}
			}]);	
	}
	}
}


function authSuccess(result){
	WL.Logger.debug("login successful.");		
	var authRequired = result.invocationResult.authRequired;	
	if(authRequired == true){
		WL.Logger.debug("authRequired is true");	
		var invocationData = {
		        adapter : 'acmeairAdapter',
		        procedure : 'submitAuthentication',
				parameters : [ $("#username").val() ]
			};

		    WL.Client.invokeProcedure(invocationData,{
				onSuccess: authSuccess,
				onFailure: loginFailure,
				invocationContext: {}
				});
			
	}else{
		WL.Logger.debug("authRequired is false");
		login();
	}
}


function loginFailure(result){
	WL.Logger.error("login failed! ");				
	WL.SimpleDialog.show("Acme Air Error", 
			"There was an error logging in. lease check your internet connectivity.",
			[{text: "Ok", handler: function() {
					window.location.hash = '#login'; 
				}
			}]);	
}
	
function logout(){    	
	var invocationData = {
        adapter : 'acmeairAdapter',
        procedure : 'logout',
		parameters : [ $("#username").val() ]
	};

    WL.Client.invokeProcedure(invocationData,{
		onSuccess: logoutSuccess,
		onFailure: logoutFailure,
		invocationContext: {}
		});
	}
	
function logoutSuccess(result){
	WL.Logger.debug("logout successful.");	
	WL.Logger.debug("isSuccessful = " + result.invocationResult.isSuccessful );
	
	
	var invocationData = {
	        adapter : 'acmeairAdapter',
	        procedure : 'onLogout',
			parameters : [ ]
		};

	    WL.Client.invokeProcedure(invocationData,{
			onSuccess: adapterLogoutSuccess,
			onFailure: adapterLogoutFailure,
			invocationContext: {}
			});
	    
	if(result.invocationResult.statusCode == "200"){
		WL.SimpleDialog.show("Acme Air", 
			"Good Bye " + $("#username").val() + ".",
			[{text: "Ok", handler: function() {
					window.location.hash = '#login'; 
				}
			}]);							
	}				
	
}

function adapterLogoutSuccess(result){
	WL.Logger.error("adapter logout successful ");				

}

function adapterLogoutFailure(result){
	WL.Logger.error("adapter logout failed! ");	
	WL.SimpleDialog.show("Acme Air Error", 
			"There was an error logging out. lease check your internet connectivity.",
			[{text: "Ok", handler: function() {
					window.location.hash = '#login'; 
				}
			}]);
}


function logoutFailure(result){
	WL.Logger.debug("logout failed! ");
	WL.SimpleDialog.show("Acme Air Error", 
			"There was an error logging out. lease check your internet connectivity.",
			[{text: "Ok", handler: function() {
					window.location.hash = '#login'; 
				}
			}]);					
}  
     		


