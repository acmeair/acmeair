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
package com.acmeair.entities;



public interface Customer {

	public enum MemberShipStatus { NONE, SILVER, GOLD, PLATINUM, EXEC_PLATINUM, GRAPHITE };
	public enum PhoneType { UNKNOWN, HOME, BUSINESS, MOBILE };
	
	
	public String getCustomerId();
	
	public String getUsername();
	
	public void setUsername(String username);
	
	public String getPassword();
	
	public void setPassword(String password);
	
	public MemberShipStatus getStatus();
	
	public void setStatus(MemberShipStatus status);
	
	public int getTotal_miles();
	
	public int getMiles_ytd();
	
	public String getPhoneNumber();

	public void setPhoneNumber(String phoneNumber);

	public PhoneType getPhoneNumberType();

	public void setPhoneNumberType(PhoneType phoneNumberType);

	public CustomerAddress getAddress();

	public void setAddress(CustomerAddress address);

}
