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
package com.acmeair.wxs.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.acmeair.entities.Booking;
import com.acmeair.entities.BookingPK;
import com.acmeair.entities.Customer;
import com.acmeair.entities.Flight;
import com.acmeair.entities.FlightPK;
import com.acmeair.service.BookingService;
import com.acmeair.service.CustomerService;
import com.acmeair.service.DataService;
import com.acmeair.service.FlightService;
import com.acmeair.service.ServiceLocator;
import com.acmeair.wxs.WXSConstants;
import com.acmeair.wxs.utils.WXSSessionManager;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

@DataService(name=WXSConstants.KEY,description=WXSConstants.KEY_DESCRIPTION)
public class BookingServiceImpl implements BookingService, WXSConstants  {
	
	private final static Logger logger = Logger.getLogger(BookingService.class.getName()); 
	
	private static final String BOOKING_MAP_NAME="Booking";

	private ObjectGrid og;
	
	@Inject
	private DefaultKeyGeneratorImpl keyGenerator;
	
	private FlightService flightService = ServiceLocator.instance().getService(FlightService.class);
	private CustomerService customerService = ServiceLocator.instance().getService(CustomerService.class);
	
	
	@PostConstruct
	private void initialization()  {
		try {
			og = WXSSessionManager.getSessionManager().getObjectGrid();
		} catch (ObjectGridException e) {
			logger.severe("Unable to retreive the ObjectGrid reference " + e.getMessage());
		}
	}
	
	@Override
	public BookingPK bookFlight(String customerId, FlightPK flightId) {
		try{
			// We still delegate to the flight and customer service for the map access than getting the map instance directly
			Flight f = flightService.getFlightByFlightKey(flightId);
			Customer c = customerService.getCustomerByUsername(customerId);
			
			Booking newBooking = new Booking(keyGenerator.generate().toString(), new Date(), c, f);
			BookingPK key = newBooking.getPkey();
			
			//Session session = sessionManager.getObjectGridSession();
			Session session = og.getSession();
			ObjectMap bookingMap = session.getMap(BOOKING_MAP_NAME);
			
			HashSet<Booking> bookingsByUser = (HashSet<Booking>)bookingMap.get(customerId);
			if (bookingsByUser == null) {
				bookingsByUser = new HashSet<Booking>();
			}
			if (bookingsByUser.contains(newBooking)) {
				throw new Exception("trying to book a duplicate booking");
			}
			bookingsByUser.add(newBooking);
			bookingMap.upsert(customerId, bookingsByUser);
			return key;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Booking getBooking(String user, String id) {
		
		try{
			//Session session = sessionManager.getObjectGridSession();
			Session session = og.getSession();
			ObjectMap bookingMap = session.getMap(BOOKING_MAP_NAME);
			
//			return (Booking)bookingMap.get(new BookingPK(user, id));
			HashSet<Booking> bookingsByUser = (HashSet<Booking>)bookingMap.get(user);
			if (bookingsByUser == null) {
				return null;
			}
			for (Booking b : bookingsByUser) {
				if (b.getPkey().getId().equals(id)) {
					return b;
				}
			}
			return null;

		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
			
	}

	@Override
	public void cancelBooking(String user, String id) {
		try{
			Session session = og.getSession();
			//Session session = sessionManager.getObjectGridSession();
			ObjectMap bookingMap = session.getMap(BOOKING_MAP_NAME);
			
			HashSet<Booking> bookingsByUser = (HashSet<Booking>)bookingMap.get(user);
			if (bookingsByUser == null) {
				return;
			}
			boolean found = false;
			HashSet<Booking> newBookings = new HashSet<Booking>();
			for (Booking b : bookingsByUser) {
				if (b.getPkey().getId().equals(id)) {
					found = true;
				}
				else {
					newBookings.add(b);
				}
			}
			
			if (found) {
				bookingMap.upsert(user, newBookings);
			}
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}		
	
	@Override
	public List<Booking> getBookingsByUser(String user) {
		try{
			Session session = og.getSession();
			//Session session = sessionManager.getObjectGridSession();
	
			boolean startedTran = false;
			if (!session.isTransactionActive()) {
				startedTran = true;
				session.begin();
			}
			
			ObjectMap bookingMap = session.getMap(BOOKING_MAP_NAME);
			HashSet<Booking> bookingsByUser = (HashSet<Booking>)bookingMap.get(user);
			if (bookingsByUser == null) {
				bookingsByUser = new HashSet<Booking>();
			}
			
			ArrayList<Booking> bookingsList = new ArrayList<Booking>();
			for (Booking b : bookingsByUser) {
				bookingsList.add(b);
			}
		
			if (startedTran)
				session.commit();
			
			return bookingsList;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public Long count () {
		return -1L;
	}
}
