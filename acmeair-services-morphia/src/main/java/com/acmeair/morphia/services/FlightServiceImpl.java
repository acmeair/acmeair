package com.acmeair.morphia.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.acmeair.entities.AirportCodeMapping;
import com.acmeair.entities.Flight;
import com.acmeair.entities.FlightPK;
import com.acmeair.entities.FlightSegment;
import com.acmeair.morphia.MorphiaConstants;
import com.acmeair.morphia.entities.AirportCodeMappingImpl;
import com.acmeair.morphia.entities.FlightImpl;
import com.acmeair.morphia.entities.FlightSegmentImpl;
import com.acmeair.morphia.services.util.MongoConnectionManager;
import com.acmeair.service.DataService;
import com.acmeair.service.FlightService;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

@DataService(name=MorphiaConstants.KEY,description=MorphiaConstants.KEY_DESCRIPTION)
public class FlightServiceImpl implements FlightService, MorphiaConstants {

	//private final static Logger logger = Logger.getLogger(FlightService.class.getName()); 
		
	Datastore datastore;
	
	@Inject
	DefaultKeyGeneratorImpl keyGenerator;
	
	//TODO:need to find a way to invalidate these maps
	private static ConcurrentHashMap<String, FlightSegment> originAndDestPortToSegmentCache = new ConcurrentHashMap<String,FlightSegment>();
	private static ConcurrentHashMap<String, List<Flight>> flightSegmentAndDataToFlightCache = new ConcurrentHashMap<String,List<Flight>>();
	private static ConcurrentHashMap<FlightPK, Flight> flightPKtoFlightCache = new ConcurrentHashMap<FlightPK, Flight>();
	
	
	@PostConstruct
	public void initialization() {	
		datastore = MongoConnectionManager.getConnectionManager().getDatastore();
	}
	
	
	@Override
	public Long countFlights() {
		return datastore.find(FlightImpl.class).countAll();
	}
	
	@Override
	public Long countFlightSegments() {
		return datastore.find(FlightSegmentImpl.class).countAll();
	}
	
	@Override
	public Long countAirports() {
		return datastore.find(AirportCodeMappingImpl.class).countAll();
	}
	
	@Override
	public Flight getFlightByFlightKey(FlightPK key) {
		try {
			Flight flight = flightPKtoFlightCache.get(key);
			if (flight == null) {
				Query<FlightImpl> q = datastore.find(FlightImpl.class).field("_id").equal(key);
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
				Query<FlightSegmentImpl> q = datastore.find(FlightSegmentImpl.class).field("originPort").equal(fromAirport).field("destPort").equal(toAirport);
				segment = q.get();
				if (segment == null) {
					segment = new FlightSegmentImpl(); // put a sentinel value of a non-populated flightsegment 
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
				Query<FlightImpl> q2 = datastore.find(FlightImpl.class).disableValidation().field("_id.flightSegmentId").equal(segment.getFlightName()).field("scheduledDepartureTime").equal(deptDate);
				List<FlightImpl> flightImpls = q2.asList();
				if (flightImpls != null) {
					flights =  new ArrayList<Flight>(); 
					for (Flight flight : flightImpls) {
						flight.setFlightSegment(segment);
						flights.add(flight);
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
			Query<FlightSegmentImpl> q = datastore.find(FlightSegmentImpl.class).field("originPort").equal(fromAirport).field("destPort").equal(toAirport);
			FlightSegment segment = q.get();
			if (segment == null) {
				return new ArrayList<Flight>(); 
			}			
			Query<FlightImpl> q2 = datastore.find(FlightImpl.class).disableValidation().field("_id.flightSegmentId").equal(segment.getFlightName());
			List<FlightImpl> flightImpls = q2.asList();
			if (flightImpls != null) {
				List<Flight> flights = new ArrayList<Flight>();
				for (Flight flight : flightImpls) {
					flight.setFlightSegment(segment);
					flights.add(flight);
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
	public AirportCodeMapping createAirportCodeMapping(String airportCode, String airportName){
		AirportCodeMapping acm = new AirportCodeMappingImpl(airportCode, airportName);
		return acm;
	}
	
	@Override
	public Flight createNewFlight(String flightSegmentId,
			Date scheduledDepartureTime, Date scheduledArrivalTime,
			BigDecimal firstClassBaseCost, BigDecimal economyClassBaseCost,
			int numFirstClassSeats, int numEconomyClassSeats,
			String airplaneTypeId) {
		String id = keyGenerator.generate().toString();
		Flight flight = new FlightImpl(id, flightSegmentId,
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
	
	@Override 
	public void storeFlightSegment(String flightName, String origPort, String destPort, int miles) {
		FlightSegment flightSeg = new FlightSegmentImpl(flightName, origPort, destPort, miles);
		storeFlightSegment(flightSeg);
	}
}
