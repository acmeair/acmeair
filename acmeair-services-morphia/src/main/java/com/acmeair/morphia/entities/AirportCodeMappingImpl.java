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
package com.acmeair.morphia.entities;

import java.io.Serializable;

import org.mongodb.morphia.annotations.Entity;

import com.acmeair.entities.AirportCodeMapping;

@Entity(value="airportCodeMapping")
public class AirportCodeMappingImpl implements AirportCodeMapping, Serializable{
	
	private static final long serialVersionUID = 1L;

	private String _id;
	private String airportName;
	
	public AirportCodeMappingImpl() {
	}
	
	public AirportCodeMappingImpl(String airportCode, String airportName) {
		this._id = airportCode;
		this.airportName = airportName;
	}
	
	public String getAirportCode() {
		return _id;
	}
	
	public void setAirportCode(String airportCode) {
		this._id = airportCode;
	}
	
	public String getAirportName() {
		return airportName;
	}
	
	public void setAirportName(String airportName) {
		this.airportName = airportName;
	}

}