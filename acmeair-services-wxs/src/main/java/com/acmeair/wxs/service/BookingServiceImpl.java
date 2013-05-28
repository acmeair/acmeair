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
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
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
import com.acmeair.wxs.utils.WXSSessionManager;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.query.ObjectQuery;

@Service("bookingService")
public class BookingServiceImpl implements BookingService  {
	
	private static final String BOOKING_MAP_NAME="Booking";
	
	//private static final Log log = LogFactory.getLog(BookingServiceImpl.class);
	
	@Autowired
	private WXSSessionManager sessionManager;

	@Resource
	FlightService flightService;

	@Resource
	CustomerService customerService;
	
	@Resource
	KeyGenerator keyGenerator;
	
	@Override
	public BookingPK bookFlight(String customerId, FlightPK flightId) {
		try{
			// We still delegate to the flight and customer service for the map access than getting the map instance directly
			Flight f = flightService.getFlightByFlightKey(flightId);
			Customer c = customerService.getCustomerByUsername(customerId);
			
			Booking newBooking = new Booking(keyGenerator.generate().toString(), new Date(), c, f);
			BookingPK key = newBooking.getPkey();
			
			Session session = sessionManager.getObjectGridSession();
			ObjectMap bookingMap = session.getMap(BOOKING_MAP_NAME);
			bookingMap.put(key, newBooking);
			return key;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Booking getBooking(String user, String id) {
		
		try{
			Session session = sessionManager.getObjectGridSession();
			ObjectMap bookingMap = session.getMap(BOOKING_MAP_NAME);
			
			return (Booking)bookingMap.get(new BookingPK(user, id));
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
			
	}

	@Override
	public void cancelBooking(String user, String id) {
		try{
			Session session = sessionManager.getObjectGridSession();
			ObjectMap bookingMap = session.getMap(BOOKING_MAP_NAME);
			
			bookingMap.remove(new BookingPK(user, id));
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}	
	
	@Override
	public List<Booking> getBookingsByUser(String user) {
		try{
			Session session = sessionManager.getObjectGridSession();
	
			boolean startedTran = false;
			if (!session.isTransactionActive()) 
			{
				startedTran = true;
				session.begin();
			}
			ObjectQuery query = session.createObjectQuery("select obj from Booking obj where obj.customerId=?1");
			query.setParameter(1, user);
		
			int partitionId = sessionManager.getBackingMap(BOOKING_MAP_NAME).getPartitionManager().getPartition(user);
			query.setPartition(partitionId);
			
			List<Booking> list = new ArrayList<Booking>();
			@SuppressWarnings("unchecked")
			Iterator<Object> itr = query.getResultIterator();
			while(itr.hasNext())
				list.add((Booking)itr.next());
			if (startedTran)
				session.commit();
			
			return list;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}
}
