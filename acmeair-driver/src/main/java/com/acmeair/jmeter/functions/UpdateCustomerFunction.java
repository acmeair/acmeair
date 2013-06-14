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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UpdateCustomerFunction extends AbstractFunction {

	private static final List<String> DESC = Arrays.asList("update_customer");
	private static final String KEY = "__updateCustomer";

	@SuppressWarnings("unused")
	private List<CompoundVariable> parameters = Collections.emptyList();

	/**
	 * Get response from "View Profile Information" REST API, parse and modify
	 * the user profile, and return JSON object with new profile (non-Javadoc)
	 *
	 * @see org.apache.jmeter.functions.AbstractFunction#execute(org.apache.jmeter.samplers.SampleResult,
	 *      org.apache.jmeter.samplers.Sampler)
	 * @return JSON String
	 */
	@Override
	public String execute(SampleResult arg0, Sampler arg1)
			throws InvalidVariableException {
		return processJSonString(arg0.getResponseDataAsString());
	}

	// String sampleJsonInput=
	// "{\"username\":\"uid53800@email.com\",\"status\":\"GOLD\",\"total_miles\":1000000,\"miles_ytd\":1000,\"phoneNumber\":\"919-123-4567\",\"phoneNumberType\":\"BUSINESS\",\"address\":{\"streetAddress1\":\"123 Main St.\",\"city\":\"Anytown\",\"stateProvince\":\"NC\",\"country\":\"USA\",\"postalCode\":\"27617\"}}";
	/**
	 * sample input:
	 * {"username":"uid53800@email.com","status":"GOLD","total_miles"
	 * :1000000,"miles_ytd"
	 * :1000,"phoneNumber":"919-123-4567","phoneNumberType":"BUSINESS"
	 * ,"address":
	 * {"streetAddress1":"123 Main St.","city":"Anytown","stateProvince"
	 * :"NC","country":"USA","postalCode":"27617"}} sample
	 * output:{"username":"uid53800@email.com"
	 * ,"password":"password","status":"GOLD"
	 * ,"total_miles":1000000,"miles_ytd":1000
	 * ,"phoneNumber":"919-123-4567","phoneNumberType"
	 * :"BUSINESS","address":{"streetAddress1"
	 * :"124 Main St.","city":"Anytown","stateProvince"
	 * :"NC","country":"USA","postalCode":"27618"}}
	 * 
	 * @param responseDataAsString
	 * @return
	 */
	@SuppressWarnings("unchecked")
	synchronized public String processJSonString(String responseDataAsString) {
		try {
			if (responseDataAsString == null)
				return "{}";
			JSONObject json = (JSONObject) new JSONParser()
					.parse(responseDataAsString);
			JSONObject jsonAddress = (JSONObject) json.get("address");
			jsonAddress
					.put("streetAddress1",
							updateAddress(jsonAddress.get("streetAddress1")
									.toString()));
			jsonAddress.put("postalCode",
					updatePostalCode(jsonAddress.get("postalCode").toString()));
			json.put("password", "password");
			return json.toJSONString();

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("NullPointerException in UpdateCustomerFunction - ResponseData =" + responseDataAsString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String updatePostalCode(String str) {
		int postalCode = Integer.parseInt(str);
		if (postalCode > 99999) {
			return "10000";
		} else {
			return (postalCode + 1) + "";
		}
	}

	private String updateAddress(String str) {
		String num = str.substring(0, str.indexOf(" "));
		return (Integer.parseInt(num) + 1) + str.substring(str.indexOf(" "));
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
