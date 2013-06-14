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
import org.apache.jmeter.threads.JMeterContextService;


/**
 * ExtractFlightsInfoFunction custom function called by the jmeter script using __extrainfo function call.  
 * Depends upon the flight information set by the FlightsPostProcessor. 
 * 
 * @see FlightsPostProcessor
 * 
 */
public class ExtractFlightsInfoFunction extends AbstractFunction {

	private static final List<String> DESC = Arrays.asList("extract_info");
	private static final String KEY = "__extrainfo";
	private List<CompoundVariable> parameters = Collections.emptyList();
	private static final String FLIGHT_TO_COUNT = "FLIGHTTOCOUNT";
	private static final String FLIGHT_RET_COUNT = "FLIGHTRETCOUNT";
	FlightsContext context;
	
	public static boolean pureIDs = false; // if false use the PK sub objects as created by WXS storage, if true, simpler _id at root level
	
	static {		
		if (System.getProperty("usePureIDs") != null) {
			pureIDs = true;
			System.out.println("usePureIDs property was set.");
		}
	}
	
	@Override
	synchronized public String execute(SampleResult arg0, Sampler arg1)
			throws InvalidVariableException {
		String value = parameters.get(0).execute();

	
		if (value.equalsIgnoreCase("UNSET")) {
			JMeterContextService.getContext().getVariables().remove(FLIGHT_TO_COUNT);
			JMeterContextService.getContext().getVariables().remove(FLIGHT_RET_COUNT);	
			FlightsThreadLocal.unset();
			return null;
		}

		context = FlightsThreadLocal.get();
		if (context == null) {
			System.out.println(this.getClass().getName() + " FlightsContext is null. This should not be null. FlightsContext should be created by FlightsPostProcessor.");
		}

		if (value.equalsIgnoreCase("isFlightAvailable")) {
			return context.getIsFlightAvailable();
		}
		if (value.equalsIgnoreCase("numOfToFlights")) {
			return context.getNumOfToFlights();
		}
		if (value.equalsIgnoreCase("numOfRetFlights")) {
			return context.getNumOfRetFlights();
		}
		if (value.equalsIgnoreCase("TOFLIGHT")) {
			return context.getTOFLIGHT();
		}
		if (value.equalsIgnoreCase("TOSEGMENTID")) {
			return context.getTOSEGMENTID();
		}
		if (value.equalsIgnoreCase("RETFLIGHT")) {
			return context.getRETFLIGHT();
		}
		if (value.equalsIgnoreCase("RESEGMENTID")) {
			return context.getRESEGMENTID();
		}
		if (value.equalsIgnoreCase("ONEWAY")) {
			return context.getONEWAY();
		}

		return null;
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
