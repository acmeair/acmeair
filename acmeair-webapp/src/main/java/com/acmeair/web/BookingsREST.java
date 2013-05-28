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
package com.acmeair.web;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import com.acmeair.entities.Booking;
import com.acmeair.entities.BookingPK;
import com.acmeair.entities.FlightPK;
import com.acmeair.service.BookingService;

@Path("/bookings")
public class BookingsREST {
	
	private BookingService bs = ServiceLocator.getService(BookingService.class);
	
	@POST
	@Consumes({"application/x-www-form-urlencoded"})
	@Path("/bookflights")
	@Produces("application/json")
	public /*BookingInfo*/ Response bookFlights(
			@FormParam("userid") String userid,
			@FormParam("toFlightId") String toFlightId,
			@FormParam("toFlightSegId") String toFlightSegId,
			@FormParam("retFlightId") String retFlightId,
			@FormParam("retFlightSegId") String retFlightSegId,
			@FormParam("oneWayFlight") boolean oneWay) {
		try {
			BookingPK bookingIdTo = bs.bookFlight(userid,new FlightPK(toFlightSegId,toFlightId));
			BookingPK bookingIdReturn = null;
			if (!oneWay) {
				bookingIdReturn = bs.bookFlight(userid, new FlightPK(retFlightSegId,retFlightId));
			}
			// YL. BookingInfo will only contains the booking generated keys as customer info is always available from the session
			BookingInfo bi;
			if (!oneWay)
				bi = new BookingInfo(bookingIdTo.getId(), bookingIdReturn.getId(), oneWay);
			else
				bi = new BookingInfo(bookingIdTo.getId(), null, oneWay);
			
			return Response.ok(bi).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("/bybookingnumber/{userid}/{number}")
	@Produces("application/json")
	public Booking getBookingByNumber(
			@PathParam("number") String number,
			@FormParam("userid") String userid) {
		try {
			Booking b = bs.getBooking(userid, number);
			return b;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@GET
	@Path("/byuser/{user}")
	@Produces("application/json")
	public List<Booking> getBookingsByUser(@PathParam("user") String user) {
		try {
			return bs.getBookingsByUser(user);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@POST
	@Consumes({"application/x-www-form-urlencoded"})
	@Path("/cancelbooking")
	@Produces("application/json")
	public Response cancelBookingsByNumber(
			@FormParam("number") String number,
			@FormParam("userid") String userid) {
		try {
			bs.cancelBooking(userid, number);
			return Response.ok("booking " + number + " deleted.").build();
					
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}	
}