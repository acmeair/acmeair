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

import java.util.Date;

import com.acmeair.entities.Customer;
import com.acmeair.entities.CustomerAddress;
import com.acmeair.entities.Customer.MemberShipStatus;
import com.acmeair.entities.Customer.PhoneType;
import com.acmeair.entities.CustomerSession;

public abstract class CustomerService {
	protected static final int DAYS_TO_ALLOW_SESSION = 1;
	
	public abstract Customer createCustomer(
			String username, String password, MemberShipStatus status, int total_miles,
			int miles_ytd, String phoneNumber, PhoneType phoneNumberType, CustomerAddress address);
	
	public abstract CustomerAddress createAddress (String streetAddress1, String streetAddress2,
			String city, String stateProvince, String country, String postalCode);
	
	public abstract Customer updateCustomer(Customer customer);
		
	
	protected abstract Customer getCustomer(String username);
	
	public Customer getCustomerByUsername(String username) {
		Customer c = getCustomer(username);
		if (c != null) {
			c.setPassword(null);
		}
		return c;
	}
	
	public boolean validateCustomer(String username, String password) {
		boolean validatedCustomer = false;
		Customer customerToValidate = getCustomer(username);
		if (customerToValidate != null) {
			validatedCustomer = password.equals(customerToValidate.getPassword());
		}
		return validatedCustomer;
	}
	
	public Customer getCustomerByUsernameAndPassword(String username,
			String password) {
		Customer c = getCustomer(username);
		if (!c.getPassword().equals(password)) {
			return null;
		}
		// Should we also set the password to null?
		return c;
	}
		
	public CustomerSession validateSession(String sessionid) {
		CustomerSession cSession = getSession(sessionid);
		if (cSession == null) {
			return null;
		}

		Date now = new Date();

		if (cSession.getTimeoutTime().before(now)) {
			removeSession(cSession);
			return null;
		}
		return cSession;		
	}
	
	protected abstract CustomerSession getSession(String sessionid);
	
	protected abstract void removeSession(CustomerSession session);
	
	public abstract CustomerSession createSession(String customerId);

	public abstract void invalidateSession(String sessionid);
	
	public abstract Long count();
	
	public abstract Long countSessions();
	
}
