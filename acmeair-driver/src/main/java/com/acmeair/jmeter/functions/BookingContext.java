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

public class BookingContext {
	private String[] BOOKING_IDs;
	private int counter = -1;

	public String getNUMBER_OF_BOOKINGS() {
		return NUMBER_OF_BOOKINGS;
	}

	public void setNUMBER_OF_BOOKINGS(String nUMBER_OF_BOOKINGS) {
		NUMBER_OF_BOOKINGS = nUMBER_OF_BOOKINGS;
	}

	public String getNUMBER_TO_CANCEL() {
		return NUMBER_TO_CANCEL;
	}

	public void setNUMBER_TO_CANCEL(String nUMBER_TO_CANCEL) {
		NUMBER_TO_CANCEL = nUMBER_TO_CANCEL;
	}

	public String[] getBOOKING_IDs() {
		return BOOKING_IDs;
	}

	public void setBOOKING_IDs(String[] bOOKING_IDs) {
		BOOKING_IDs = bOOKING_IDs;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	private String NUMBER_OF_BOOKINGS;
	private String NUMBER_TO_CANCEL;

}
