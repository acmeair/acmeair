/*******************************************************************************
* Copyright (c) 2013-2015 IBM Corp.
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.acmeair.entities.Flight;
import com.acmeair.entities.FlightSegment;
import com.acmeair.entities.AirportCodeMapping;

public abstract class FlightService {
	protected Logger logger =  Logger.getLogger(FlightService.class.getName());
	
	//TODO:need to find a way to invalidate these maps
	protected static ConcurrentHashMap<String, FlightSegment> originAndDestPortToSegmentCache = new ConcurrentHashMap<String,FlightSegment>();
	protected static ConcurrentHashMap<String, List<Flight>> flightSegmentAndDataToFlightCache = new ConcurrentHashMap<String,List<Flight>>();
	protected static ConcurrentHashMap<String, Flight> flightPKtoFlightCache = new ConcurrentHashMap<String, Flight>();
	
	

	public Flight getFlightByFlightId(String flightId, String flightSegment) {
		try {
			Flight flight = flightPKtoFlightCache.get(flightId);
			if (flight == null) {				
				flight = getFlight(flightId, flightSegment);
				if (flightId != null && flight != null) {
					flightPKtoFlightCache.putIfAbsent(flightId, flight);
				}
			}
			return flight;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	protected abstract Flight getFlight(String flightId, String flightSegment);
	
	public List<Flight> getFlightByAirportsAndDepartureDate(String fromAirport,	String toAirport, Date deptDate) {
		if(logger.isLoggable(Level.FINE))
			logger.fine("Search for flights from "+ fromAirport + " to " + toAirport + " on " + deptDate.toString());

		String originPortAndDestPortQueryString= fromAirport+toAirport;
		FlightSegment segment = originAndDestPortToSegmentCache.get(originPortAndDestPortQueryString);

		if (segment == null) {
			segment = getFlightSegment(fromAirport, toAirport);
			originAndDestPortToSegmentCache.putIfAbsent(originPortAndDestPortQueryString, segment);
		}		
		// cache flights that not available (checks against sentinel value above indirectly)
		if (segment.getFlightName() == null) {
			return new ArrayList<Flight>(); 
		}

		String segId = segment.getFlightName();
		String flightSegmentIdAndScheduledDepartureTimeQueryString = segId + deptDate.toString();
		List<Flight> flights = flightSegmentAndDataToFlightCache.get(flightSegmentIdAndScheduledDepartureTimeQueryString);

		if (flights == null) {				
			flights = getFlightBySegment(segment, deptDate);
			flightSegmentAndDataToFlightCache.putIfAbsent(flightSegmentIdAndScheduledDepartureTimeQueryString, flights);
		}
		if(logger.isLoggable(Level.FINEST))
			logger.finest("Returning "+ flights);
		return flights;

	}

	// NOTE:  This is not cached
	public List<Flight> getFlightByAirports(String fromAirport, String toAirport) {
			FlightSegment segment = getFlightSegment(fromAirport, toAirport);
			if (segment == null) {
				return new ArrayList<Flight>(); 
			}	
			return getFlightBySegment(segment, null);
	}
	
	protected abstract FlightSegment getFlightSegment(String fromAirport, String toAirport);
	
	protected abstract List<Flight> getFlightBySegment(FlightSegment segment, Date deptDate);  
			
	public abstract void storeAirportMapping(AirportCodeMapping mapping);

	public abstract AirportCodeMapping createAirportCodeMapping(String airportCode, String airportName);
	
	public abstract Flight createNewFlight(String flightSegmentId,
			Date scheduledDepartureTime, Date scheduledArrivalTime,
			BigDecimal firstClassBaseCost, BigDecimal economyClassBaseCost,
			int numFirstClassSeats, int numEconomyClassSeats,
			String airplaneTypeId);

	public abstract void storeFlightSegment(FlightSegment flightSeg);
	
	public abstract void storeFlightSegment(String flightName, String origPort, String destPort, int miles);
	
	public abstract Long countFlightSegments();
	
	public abstract Long countFlights();
	
	public abstract Long countAirports();
	
}