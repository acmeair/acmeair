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


public interface CustomerAddress {

	
	public String getStreetAddress1();
	public void setStreetAddress1(String streetAddress1);
	public String getStreetAddress2();
	public void setStreetAddress2(String streetAddress2);
	public String getCity();
	public void setCity(String city);
	public String getStateProvince();
	public void setStateProvince(String stateProvince);
	public String getCountry();
	public void setCountry(String country);
	public String getPostalCode();
	public void setPostalCode(String postalCode);
		
}
