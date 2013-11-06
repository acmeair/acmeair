package com.acmeair.morphia.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acmeair.entities.AirportCodeMapping;
import com.acmeair.entities.Flight;
import com.acmeair.entities.FlightPK;
import com.acmeair.entities.FlightSegment;
import com.acmeair.service.FlightService;
import com.acmeair.service.KeyGenerator;
import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.query.Query;

@Service("flightService")
public class FlightServiceImpl implements FlightService {

	@Autowired
	Datastore datastore;
	
	@Resource
	KeyGenerator keyGenerator;
	
	//TODO:need to find a way to invalidate these maps
	private static ConcurrentHashMap<String, FlightSegment> originAndDestPortToSegmentCache = new ConcurrentHashMap<String,FlightSegment>();
	private static ConcurrentHashMap<String, List<Flight>> flightSegmentAndDataToFlightCache = new ConcurrentHashMap<String,List<Flight>>();
	private static ConcurrentHashMap<FlightPK, Flight> flightPKtoFlightCache = new ConcurrentHashMap<FlightPK, Flight>();
	
	@Override
	public Flight getFlightByFlightKey(FlightPK key) {
		try {
			Flight flight = flightPKtoFlightCache.get(key);
			if (flight == null) {
				Query<Flight> q = datastore.find(Flight.class).field("_id").equal(key);
				flight = q.get();
				if (key != null && flight != null) {
					flightPKtoFlightCache.putIfAbsent(key, flight);
				}
			}
			return flight;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Flight> getFlightByAirportsAndDepartureDate(String fromAirport,	String toAirport, Date deptDate) {
		try {
			String originPortAndDestPortQueryString= fromAirport+toAirport;
			FlightSegment segment = originAndDestPortToSegmentCache.get(originPortAndDestPortQueryString);
			
			if (segment == null) {
				Query<FlightSegment> q = datastore.find(FlightSegment.class).field("originPort").equal(fromAirport).field("destPort").equal(toAirport);
				segment = q.get();
				if (segment == null) {
					segment = new FlightSegment(); // put a sentinel value of a non-populated flightsegment 
				}
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
				Query<Flight> q2 = datastore.find(Flight.class).field("pkey.flightSegmentId").equal(segment.getFlightName()).field("scheduledDepartureTime").equal(deptDate);
				flights = q2.asList();
				if (flights != null) {
					for (Flight flight : flights) {
						flight.setFlightSegment(segment);
					}
				}
				else {
					flights = new ArrayList<Flight>(); // put an empty list into the cache in the cache in the case where no matching flights
				}
				flightSegmentAndDataToFlightCache.putIfAbsent(flightSegmentIdAndScheduledDepartureTimeQueryString, flights);
			}
			
			return flights;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	// NOTE:  This is not cached
	public List<Flight> getFlightByAirports(String fromAirport, String toAirport) {
		try {
			Query<FlightSegment> q = datastore.find(FlightSegment.class).field("originPort").equal(fromAirport).field("destPort").equal(toAirport);
			FlightSegment segment = q.get();
			if (segment == null) {
				return new ArrayList<Flight>(); 
			}
			
			Query<Flight> q2 = datastore.find(Flight.class).field("pkey.flightSegmentId").equal(segment.getFlightName());
			List<Flight> flights = q2.asList();
			if (flights != null) {
				for (Flight flight : flights) {
					flight.setFlightSegment(segment);
				}
				return flights;
			}
			else {
				return new ArrayList<Flight>();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void storeAirportMapping(AirportCodeMapping mapping) {
		try{
			datastore.save(mapping);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Flight createNewFlight(String flightSegmentId,
			Date scheduledDepartureTime, Date scheduledArrivalTime,
			BigDecimal firstClassBaseCost, BigDecimal economyClassBaseCost,
			int numFirstClassSeats, int numEconomyClassSeats,
			String airplaneTypeId) {
		String id = keyGenerator.generate().toString();
		Flight flight = new Flight(id, flightSegmentId,
			scheduledDepartureTime, scheduledArrivalTime,
			firstClassBaseCost, economyClassBaseCost,
			numFirstClassSeats, numEconomyClassSeats,
			airplaneTypeId);
		try{
			datastore.save(flight);
			return flight;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void storeFlightSegment(FlightSegment flightSeg) {
		try{
			datastore.save(flightSeg);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
