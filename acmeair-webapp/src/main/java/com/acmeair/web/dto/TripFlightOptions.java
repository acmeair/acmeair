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
package com.acmeair.web.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * TripFlightOptions is the main return type when searching for flights.
 * 
 * The object will return as many tripLeg's worth of Flight options as requested.  So if the user
 * requests a one way flight they will get a List that has only one TripLegInfo and it will have
 * a list of flights that are options for that flight.  If a user selects round trip, they will
 * have a List of two TripLegInfo objects.  If a user does a multi-leg flight then the list will
 * be whatever size they requested.  For now, only supporting one way and return flights so the
 * list should always be of size one or two.
 * 
 * 
 * @author aspyker
 *
 */
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement
public class TripFlightOptions {
	private int tripLegs;
	
	private List<TripLegInfo> tripFlights;

	public int getTripLegs() {
		return tripLegs;
	}

	public void setTripLegs(int tripLegs) {
		this.tripLegs = tripLegs;
	}

	public List<TripLegInfo> getTripFlights() {
		return tripFlights;
	}

	public void setTripFlights(List<TripLegInfo> tripFlights) {
		this.tripFlights = tripFlights;
	}
}