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
package com.acmeair.service;

import com.acmeair.entities.Customer;
import com.acmeair.entities.CustomerAddress;
import com.acmeair.entities.Customer.MemberShipStatus;
import com.acmeair.entities.Customer.PhoneType;
import com.acmeair.entities.CustomerSession;

public interface CustomerService {
	static final int DAYS_TO_ALLOW_SESSION = 1;
	
	Customer createCustomer(
			String username, String password, MemberShipStatus status, int total_miles,
			int miles_ytd, String phoneNumber, PhoneType phoneNumberType, CustomerAddress address);
	
	Customer updateCustomer(Customer customer);
	
	Customer getCustomerByUsername(String username);
	
	boolean validateCustomer(String username, String password);
	
	Customer getCustomerByUsernameAndPassword(String username, String password);
	
	CustomerSession validateSession(String sessionid);
	
	CustomerSession createSession(String customerId);

	void invalidateSession(String sessionid);
	
	Long count();
	
	Long countSessions();
	
}
