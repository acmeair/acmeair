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

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.acmeair.entities.Customer;
import com.acmeair.entities.Customer.MemberShipStatus;
import com.acmeair.entities.Customer.PhoneType;
import com.acmeair.entities.CustomerAddress;
import com.acmeair.entities.CustomerSession;
import com.acmeair.service.BookingService;
import com.acmeair.service.CustomerService;
import com.acmeair.service.DataService;
import com.acmeair.wxs.WXSConstants;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

@Default
@DataService(name=WXSConstants.KEY,description=WXSConstants.KEY_DESCRIPTION)
public class CustomerServiceImpl implements CustomerService, WXSConstants{
	
	private static final String CUSTOMER_MAP_NAME="Customer";
	private static final String CUSTOMER_SESSION_MAP_NAME="CustomerSession";
	
	private static final int DAYS_TO_ALLOW_SESSION = 1;
	private final static Logger logger = Logger.getLogger(BookingService.class.getName()); 

	private ObjectGrid og;
	
	@Inject
	DefaultKeyGeneratorImpl keyGenerator;

	
	@PostConstruct
	private void initialization()  {		
		if(og == null) {
			try {
				InitialContext ic = new InitialContext();			
				og = (ObjectGrid) ic.lookup(JNDI_NAME);
			} catch (NamingException e) {
				logger.severe("Error looking up the ObjectGrid reference" + e.getMessage());
			}
		}
	}
	
	@Override
	public Customer createCustomer(String username, String password,
			MemberShipStatus status, int total_miles, int miles_ytd,
			String phoneNumber, PhoneType phoneNumberType,
			CustomerAddress address) {
		try{
			Customer customer = new Customer(username, password, status, total_miles, miles_ytd, address, phoneNumber, phoneNumberType);
			// Session session = sessionManager.getObjectGridSession();
			Session session = og.getSession();
			ObjectMap customerMap = session.getMap(CUSTOMER_MAP_NAME);
			customerMap.insert(customer.getUsername(), customer);
			return customer;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Customer updateCustomer(Customer customer) {
		try{
			//Session session = sessionManager.getObjectGridSession();
			Session session = og.getSession();
			ObjectMap customerMap = session.getMap(CUSTOMER_MAP_NAME);
			customerMap.update(customer.getUsername(), customer);
			return customer;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private Customer getCustomer(String username) {
		try{
			//Session session = sessionManager.getObjectGridSession();
			Session session = og.getSession();
			ObjectMap customerMap = session.getMap(CUSTOMER_MAP_NAME);
			
			Customer c = (Customer) customerMap.get(username);
			return c;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Customer getCustomerByUsername(String username) {
		Customer c = getCustomer(username);
		if (c != null) {
			c.setPassword(null);
		}
		return c;
	}

	@Override
	public boolean validateCustomer(String username, String password) {
		boolean validatedCustomer = false;
		Customer customerToValidate = getCustomer(username);
		if (customerToValidate != null) {
			validatedCustomer = password.equals(customerToValidate.getPassword());
		}
		return validatedCustomer;
	}

	@Override
	public Customer getCustomerByUsernameAndPassword(String username,
			String password) {
		Customer c = getCustomer(username);
		if (!c.getPassword().equals(password)) {
			return null;
		}
		// Should we also set the password to null?
		return c;
	}

	@Override
	public CustomerSession validateSession(String sessionid) {
		try{
			//Session session = sessionManager.getObjectGridSession();
			Session session = og.getSession();
			ObjectMap customerSessionMap = session.getMap(CUSTOMER_SESSION_MAP_NAME);
			
			CustomerSession cSession = (CustomerSession)customerSessionMap.get(sessionid);
			if (cSession == null) {
				return null;
			}
			
			Date now = new Date();
			
			if (cSession.getTimeoutTime().before(now)) {
				customerSessionMap.remove(sessionid);
				return null;
			}
			return cSession;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public CustomerSession createSession(String customerId) {
		try{
			String sessionId = keyGenerator.generate().toString();
			Date now = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(now);
			c.add(Calendar.DAY_OF_YEAR, DAYS_TO_ALLOW_SESSION);
			Date expiration = c.getTime();
			CustomerSession cSession = new CustomerSession(sessionId, customerId, now, expiration);
			// Session session = sessionManager.getObjectGridSession();
			Session session = og.getSession();
			ObjectMap customerSessionMap = session.getMap(CUSTOMER_SESSION_MAP_NAME);
			customerSessionMap.insert(cSession.getId(), cSession);
			return cSession;
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void invalidateSession(String sessionid) {
		try{
			//Session session = sessionManager.getObjectGridSession();
			Session session = og.getSession();
			ObjectMap customerSessionMap = session.getMap(CUSTOMER_SESSION_MAP_NAME);
			customerSessionMap.remove(sessionid);
		}catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
