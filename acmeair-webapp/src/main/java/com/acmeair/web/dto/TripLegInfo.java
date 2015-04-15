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

import java.util.ArrayList;
import java.util.List;

import com.acmeair.entities.Flight;

/**
 * The TripLegInfo object contains a list of flights that satisfy the query request for any one
 * leg of a trip.  Also, it supports paging so a query can't return too many requests.
 * @author aspyker
 *
 */
public class TripLegInfo {
	public static int DEFAULT_PAGE_SIZE = 10;
	
	private boolean hasMoreOptions;
	
	private int numPages;
	private int pageSize;
	private int currentPage;
	
	private List<FlightInfo> flightsOptions;

	public boolean isHasMoreOptions() {
		return hasMoreOptions;
	}

	public void setHasMoreOptions(boolean hasMoreOptions) {
		this.hasMoreOptions = hasMoreOptions;
	}

	public int getNumPages() {
		return numPages;
	}

	public void setNumPages(int numPages) {
		this.numPages = numPages;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public List<FlightInfo> getFlightsOptions() {
		return flightsOptions;
	}

	public void setFlightsOptions(List<Flight> flightsOptions) {
		List<FlightInfo> flightInfoOptions = new ArrayList<FlightInfo>();
		for(Flight info : flightsOptions){
			flightInfoOptions.add(new FlightInfo(info));
		}
		this.flightsOptions = flightInfoOptions;
	}
	

}