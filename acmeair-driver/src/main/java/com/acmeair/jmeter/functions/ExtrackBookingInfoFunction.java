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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ExtrackBookingInfoFunction extends AbstractFunction {

	private static final List<String> DESC = Arrays
			.asList("extract_booking_info");
	private static final String KEY = "__extractBookingInfo";
	private List<CompoundVariable> parameters = Collections.emptyList();
	BookingContext context;

	@Override
	synchronized public String execute(SampleResult arg0, Sampler arg1)
			throws InvalidVariableException {
		String value = parameters.get(0).execute();
		if (value.equalsIgnoreCase("UNSET")) {
			BookingThreadLocal.unset();
			return "";
		}
		if(arg0.getErrorCount()>0){
			System.out.println("ExtrackBookingInfoFunction - Last sample received an error. Response Code = " + arg0.getResponseCode() +".");			
			return "";			
		}
		
		context = BookingThreadLocal.get();
		if (context == null) {
			context = new BookingContext();
			processJSonString(arg0);
		}

		if (value.equalsIgnoreCase("BOOKING_ID")) {
			context.setCounter(context.getCounter() + 1);
			return context.getBOOKING_IDs()[context.getCounter()];
		}
		if (value.equalsIgnoreCase("NUMBER_OF_BOOKINGS")) {
			return context.getNUMBER_OF_BOOKINGS();
		}
		if (value.equalsIgnoreCase("NUMBER_TO_CANCEL")) {
			if (context == null) {
				context = new BookingContext();
				processJSonString(arg0);
			}
			return context.getNUMBER_TO_CANCEL();
		}

		return "";

	}

	private void processJSonString(SampleResult sample) {

		try {			
			Object returnObject =  new JSONParser().parse(sample.getResponseDataAsString());
			JSONArray jsonArray;
			if (returnObject instanceof JSONArray) {
				jsonArray = (JSONArray) returnObject;
			}else {
				throw new RuntimeException("failed to parse booking information: " + returnObject.toString());
			}
			int bookingNum = jsonArray.size();

			context.setNUMBER_OF_BOOKINGS(bookingNum + "");

			if (bookingNum > 2) {
				context.setNUMBER_TO_CANCEL(bookingNum - 2 + "");
			} else {
				context.setNUMBER_TO_CANCEL("0");
			}
			String[] bookingIds = new String[bookingNum];
			for (int counter = 0; counter < bookingNum; counter++) {
				JSONObject booking = (JSONObject) jsonArray.get(counter);// .pkey.id;
				String bookingId;
				if (ExtractFlightsInfoFunction.pureIDs) {
					bookingId = (String)booking.get("_id");
				}
				else {
					JSONObject bookingPkey = (JSONObject) booking.get("pkey");
					bookingId = (String) bookingPkey.get("id");
				}
				bookingIds[counter] = bookingId;
			}
			context.setBOOKING_IDs(bookingIds);
			BookingThreadLocal.set(context);

		} catch (ParseException e) {
			System.out.println("responseDataAsString = " + sample.getResponseDataAsString());
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			System.out.println("NullPointerException in ExtrackBookingInfoFunction - ResponseData =" + sample.getResponseDataAsString());
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getReferenceKey() {
		return KEY;
	}

	@Override
	public void setParameters(Collection<CompoundVariable> arg0) throws InvalidVariableException {
		parameters = new ArrayList<CompoundVariable>(arg0);
	}

	public List<String> getArgumentDesc() {
		return DESC;
	}

}
