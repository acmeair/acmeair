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
package com.acmeair.entities;



import java.math.BigDecimal;
import java.util.Date;


public interface Flight{

	
	public FlightPK getPkey();

	public void setPkey(FlightPK pkey);

	// The method is needed for index calculation
	public String getFlightSegmentId();
	
	public Date getScheduledDepartureTime();


	public void setScheduledDepartureTime(Date scheduledDepartureTime);


	public Date getScheduledArrivalTime();


	public void setScheduledArrivalTime(Date scheduledArrivalTime);


	public BigDecimal getFirstClassBaseCost();


	public void setFirstClassBaseCost(BigDecimal firstClassBaseCost);


	public BigDecimal getEconomyClassBaseCost();


	public void setEconomyClassBaseCost(BigDecimal economyClassBaseCost);


	public int getNumFirstClassSeats();


	public void setNumFirstClassSeats(int numFirstClassSeats);


	public int getNumEconomyClassSeats();


	public void setNumEconomyClassSeats(int numEconomyClassSeats);


	public String getAirplaneTypeId();


	public void setAirplaneTypeId(String airplaneTypeId);


	public FlightSegment getFlightSegment();

	public void setFlightSegment(FlightSegment flightSegment);

}