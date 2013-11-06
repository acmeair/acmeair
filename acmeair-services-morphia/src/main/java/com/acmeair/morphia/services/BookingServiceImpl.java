package com.acmeair.morphia.services;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acmeair.entities.Booking;
import com.acmeair.entities.BookingPK;
import com.acmeair.entities.Customer;
import com.acmeair.entities.Flight;
import com.acmeair.entities.FlightPK;
import com.acmeair.service.BookingService;
import com.acmeair.service.CustomerService;
import com.acmeair.service.FlightService;
import com.acmeair.service.KeyGenerator;

import com.github.jmkgreen.morphia.*;
import com.github.jmkgreen.morphia.query.Query;

@Service("bookingService")
public class BookingServiceImpl implements BookingService {
	
	@Autowired
	Datastore datastore;
	
	@Resource
	FlightService flightService;

	@Resource
	CustomerService customerService;
	
	@Resource
	KeyGenerator keyGenerator;

	@Override
	public BookingPK bookFlight(String customerId, FlightPK flightId) {
		try{
			Flight f = flightService.getFlightByFlightKey(flightId);
			Customer c = customerService.getCustomerByUsername(customerId);
			
			Booking newBooking = new Booking(keyGenerator.generate().toString(), new Date(), c, f);

			datastore.save(newBooking);
			return newBooking.getPkey();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Booking getBooking(String user, String id) {
		try{
			Query<Booking> q = datastore.find(Booking.class).field("_id").equal(new BookingPK(user, id));
			Booking booking = q.get();
			
			return booking;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Booking> getBookingsByUser(String user) {
		try{
			Query<Booking> q = datastore.find(Booking.class).field("pkey.customerId").equal(user);
			List<Booking> bookings = q.asList();
			
			return bookings;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void cancelBooking(String user, String id) {
		try{
			datastore.delete(Booking.class, new BookingPK(user, id) );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
