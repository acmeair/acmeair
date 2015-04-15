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
package com.acmeair.service;

import java.util.List;

import com.acmeair.entities.Booking;


public interface BookingService {

	//String bookFlight(String customerId, FlightPK flightId);
//	String bookFlight(String customerId, String flightId);
	
	String bookFlight(String customerId, String flightSegmentId, String FlightId);
	
	Booking getBooking(String user, String id);

	List<Booking> getBookingsByUser(String user);
	
	void cancelBooking(String user, String id);
	
	Long count();
}