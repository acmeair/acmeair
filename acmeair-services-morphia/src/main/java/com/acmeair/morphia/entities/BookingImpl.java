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
import java.util.Date;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.acmeair.entities.Booking;
import com.acmeair.entities.Customer;
import com.acmeair.entities.Flight;

@Entity(value="booking")
public class BookingImpl implements Booking, Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private String _id;	
	private String flightId;
	private String customerId;
	private Date dateOfBooking;
		
	public BookingImpl() {
	}
	
	public BookingImpl(String bookingId, Date dateOfFlight, String customerId, String flightId) {
		this._id = bookingId;		
		this.flightId = flightId;
		this.customerId = customerId;
		this.dateOfBooking = dateOfFlight;
	}
	
	public BookingImpl(String bookingId, Date dateOfFlight, Customer customer, Flight flight) {		
		this._id = bookingId;
		this.flightId = flight.getFlightId();
		this.dateOfBooking = dateOfFlight;
		this.customerId = customer.getCustomerId();		
	}
	
	
	public String getBookingId() {
		return _id;
	}
	
	public void setBookingId(String bookingId) {
		this._id = bookingId;
	}

	public String getFlightId() {
		return flightId;
	}

	public void setFlightId(String flightId) {
		this.flightId = flightId;
	}

	

	public Date getDateOfBooking() {
		return dateOfBooking;
	}
	
	public void setDateOfBooking(Date dateOfBooking) {
		this.dateOfBooking = dateOfBooking;
	}

	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomer(String customerId) {
		this.customerId = customerId;
	}



	@Override
	public String toString() {
		return "Booking [key=" + _id + ", flightId=" + flightId
				+ ", dateOfBooking=" + dateOfBooking + ", customerId=" + customerId + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookingImpl other = (BookingImpl) obj;
		if (customerId == null) {
			if (other.customerId != null)
				return false;
		} else if (!customerId.equals(other.customerId))
			return false;
		if (dateOfBooking == null) {
			if (other.dateOfBooking != null)
				return false;
		} else if (!dateOfBooking.equals(other.dateOfBooking))
			return false;
		if (flightId == null) {
			if (other.flightId != null)
				return false;
		} else if (!flightId.equals(other.flightId))
			return false;		
		return true;
	}

}
