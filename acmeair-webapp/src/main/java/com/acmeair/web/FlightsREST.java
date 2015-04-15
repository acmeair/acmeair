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

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import com.acmeair.entities.Flight;
import com.acmeair.service.FlightService;
import com.acmeair.service.ServiceLocator;
import com.acmeair.web.dto.TripFlightOptions;
import com.acmeair.web.dto.TripLegInfo;

@Path("/flights")
public class FlightsREST {
	
	private FlightService flightService = ServiceLocator.instance().getService(FlightService.class);
	
	// TODO:  Consider a pure GET implementation of this service, but maybe not much value due to infrequent similar searches
	@POST
	@Path("/queryflights")
	@Consumes({"application/x-www-form-urlencoded"})
	@Produces("application/json")
	public TripFlightOptions getTripFlights(
			@FormParam("fromAirport") String fromAirport,
			@FormParam("toAirport") String toAirport,
			@FormParam("fromDate") Date fromDate,
			@FormParam("returnDate") Date returnDate,
			@FormParam("oneWay") boolean oneWay
			) {
		TripFlightOptions options = new TripFlightOptions();
		ArrayList<TripLegInfo> legs = new ArrayList<TripLegInfo>();
		
		TripLegInfo toInfo = new TripLegInfo();
		List<Flight> toFlights = flightService.getFlightByAirportsAndDepartureDate(fromAirport, toAirport, fromDate);
		toInfo.setFlightsOptions(toFlights);
		legs.add(toInfo);
		toInfo.setCurrentPage(0);
		toInfo.setHasMoreOptions(false);
		toInfo.setNumPages(1);
		toInfo.setPageSize(TripLegInfo.DEFAULT_PAGE_SIZE);
		
		if (!oneWay) {
			TripLegInfo retInfo = new TripLegInfo();
			List<Flight> retFlights = flightService.getFlightByAirportsAndDepartureDate(toAirport, fromAirport, returnDate);
			retInfo.setFlightsOptions(retFlights);
			legs.add(retInfo);
			retInfo.setCurrentPage(0);
			retInfo.setHasMoreOptions(false);
			retInfo.setNumPages(1);
			retInfo.setPageSize(TripLegInfo.DEFAULT_PAGE_SIZE);
			options.setTripLegs(2);
		}
		else {
			options.setTripLegs(1);
		}
		
		options.setTripFlights(legs);
		
		return options;
	}
	
	
	@POST
	@Path("/browseflights")
	@Consumes({"application/x-www-form-urlencoded"})
	@Produces("application/json")
	public TripFlightOptions browseFlights(
			@FormParam("fromAirport") String fromAirport,
			@FormParam("toAirport") String toAirport,
			@FormParam("oneWay") boolean oneWay
			) {
		TripFlightOptions options = new TripFlightOptions();
		ArrayList<TripLegInfo> legs = new ArrayList<TripLegInfo>();
		
		TripLegInfo toInfo = new TripLegInfo();
		List<Flight> toFlights = flightService.getFlightByAirports(fromAirport, toAirport);
		toInfo.setFlightsOptions(toFlights);
		legs.add(toInfo);
		toInfo.setCurrentPage(0);
		toInfo.setHasMoreOptions(false);
		toInfo.setNumPages(1);
		toInfo.setPageSize(TripLegInfo.DEFAULT_PAGE_SIZE);
		
		if (!oneWay) {
			TripLegInfo retInfo = new TripLegInfo();
			List<Flight> retFlights = flightService.getFlightByAirports(toAirport, fromAirport);
			retInfo.setFlightsOptions(retFlights);
			legs.add(retInfo);
			retInfo.setCurrentPage(0);
			retInfo.setHasMoreOptions(false);
			retInfo.setNumPages(1);
			retInfo.setPageSize(TripLegInfo.DEFAULT_PAGE_SIZE);
			options.setTripLegs(2);
		}
		else {
			options.setTripLegs(1);
		}
		
		options.setTripFlights(legs);
		
		return options;
	}	

}