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

import java.io.Serializable;

public class FlightSegment implements Serializable{

	private static final long serialVersionUID = 1L;

	private String _id;
	private String originPort;
	private String destPort;
	private int miles;

	public FlightSegment() {
	}
	
	public FlightSegment(String flightName, String origPort, String destPort, int miles) {
		this._id = flightName;
		this.originPort = origPort;
		this.destPort = destPort;
		this.miles = miles;
	}
	
	public String getFlightName() {
		return _id;
	}

	public void setFlightName(String flightName) {
		this._id = flightName;
	}

	public String getOriginPort() {
		return originPort;
	}

	public void setOriginPort(String originPort) {
		this.originPort = originPort;
	}

	public String getDestPort() {
		return destPort;
	}

	public void setDestPort(String destPort) {
		this.destPort = destPort;
	}

	public int getMiles() {
		return miles;
	}

	public void setMiles(int miles) {
		this.miles = miles;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("FlightSegment ").append(_id).append(" originating from:\"").append(originPort).append("\" arriving at:\"").append(destPort).append("\"");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlightSegment other = (FlightSegment) obj;
		if (destPort == null) {
			if (other.destPort != null)
				return false;
		} else if (!destPort.equals(other.destPort))
			return false;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (miles != other.miles)
			return false;
		if (originPort == null) {
			if (other.originPort != null)
				return false;
		} else if (!originPort.equals(other.originPort))
			return false;
		return true;
	}
	
	
}