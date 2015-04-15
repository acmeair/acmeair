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

public class BookingReceiptInfo {
	
	private String departBookingId;
	private String returnBookingId;
	private boolean oneWay;
	
	public BookingReceiptInfo(String departBookingId, String returnBookingId, boolean oneWay) {
		this.departBookingId = departBookingId;
		this.returnBookingId = returnBookingId;
		this.oneWay = oneWay;
	}
	
	public BookingReceiptInfo() {
	}
	
	public String getDepartBookingId() {
		return departBookingId;
	}
	public void setDepartBookingId(String departBookingId) {
		this.departBookingId = departBookingId;
	}
	public String getReturnBookingId() {
		return returnBookingId;
	}
	public void setReturnBookingId(String returnBookingId) {
		this.returnBookingId = returnBookingId;
	}
	public boolean isOneWay() {
		return oneWay;
	}
	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}
	
	@Override
	public String toString() {
		return "BookingInfo [departBookingId=" + departBookingId
				+ ", returnBookingId=" + returnBookingId + ", oneWay=" + oneWay
				+ "]";
	}
}
