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
package com.acmeair.jmeter.functions;

public class FlightsContext {
	private String numOfToFlights;

	public String getNumOfToFlights() {
		return numOfToFlights;
	}

	public void setNumOfToFlights(String numOfToFlights) {
		this.numOfToFlights = numOfToFlights;
	}

	public String getNumOfRetFlights() {
		return numOfRetFlights;
	}

	public void setNumOfRetFlights(String numOfRetFlights) {
		this.numOfRetFlights = numOfRetFlights;
	}

	public String getTOFLIGHT() {
		return TOFLIGHT;
	}

	public void setTOFLIGHT(String tOFLIGHT) {
		TOFLIGHT = tOFLIGHT;
	}

	public String getTOSEGMENTID() {
		return TOSEGMENTID;
	}

	public void setTOSEGMENTID(String tOSEGMENTID) {
		TOSEGMENTID = tOSEGMENTID;
	}

	public String getRETFLIGHT() {
		return RETFLIGHT;
	}

	public void setRETFLIGHT(String rETFLIGHT) {
		RETFLIGHT = rETFLIGHT;
	}

	public String getRESEGMENTID() {
		return RESEGMENTID;
	}

	public void setRESEGMENTID(String rESEGMENTID) {
		RESEGMENTID = rESEGMENTID;
	}

	public String getONEWAY() {
		return ONEWAY;
	}

	public void setONEWAY(String oNEWAY) {
		ONEWAY = oNEWAY;
	}

	public String getIsFlightAvailable() {
		return isFlightAvailable;
	}

	public void setIsFlightAvailable(String isFlightAvailable) {
		this.isFlightAvailable = isFlightAvailable;
	}

	private String numOfRetFlights;
	private String TOFLIGHT;
	private String TOSEGMENTID;
	private String RETFLIGHT;
	private String RESEGMENTID;
	private String ONEWAY;
	private String isFlightAvailable;

	@Override
	public String toString() {
		return "FlightsContext [numOfToFlights=" + numOfToFlights
				+ ", numOfRetFlights=" + numOfRetFlights + ", TOFLIGHT="
				+ TOFLIGHT + ", TOSEGMENTID=" + TOSEGMENTID + ", RETFLIGHT="
				+ RETFLIGHT + ", RESEGMENTID=" + RESEGMENTID + ", ONEWAY="
				+ ONEWAY + ", isFlightAvailable=" + isFlightAvailable + "]";
	}
}
