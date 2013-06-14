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

import java.util.Arrays;
import java.util.List;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * FlightsPostProcessor will parse the JSON return string and set the variables that are 
 * used by the ExtractFlightsInfoFunction. It will also set some custom variable used to
 * collect statistics about the run. 
 * 
 * Reference to collecting custom variables: 
 *   http://stackoverflow.com/questions/12809877/graph-a-custom-variable-in-jmeter
 *
 * To enable this PostProcessor you'll need to add the following in the jmx script 
 *  right after the flights query:
 *       <com.acmeair.jmeter.functions.FlightsPostProcessor guiclass="com.acmeair.jmeter.functions.FlightsPostProcessorGui" testclass="com.acmeair.jmeter.functions.FlightsPostProcessor" testname="Flights PostProcessor" enabled="true">
 *       </com.acmeair.jmeter.functions.FlightsPostProcessor>
 *
 * To see the custom metrics in the jtl log need to add the following in the user.properties file:
 *    sample_variables=FLIGHTTOCOUNT,FLIGHTRETCOUNT,ONEWAY
 *    
 * @see ExtractFlightsInfoFunction
 * 
 */
public class FlightsPostProcessor extends AbstractTestElement implements PostProcessor, TestElement {


	private static final long serialVersionUID = 1L;
	
	private static final List<String> DESC = Arrays.asList("extract_info");
	private static final String FLIGHT_TO_COUNT = "FLIGHTTOCOUNT";
	private static final String FLIGHT_RET_COUNT = "FLIGHTRETCOUNT";
	private static final String ONE_WAY = "ONEWAY";
	FlightsContext context;
	
	@Override
	public void process() {
		 SampleResult prev = JMeterContextService.getContext().getPreviousResult();
		 
		 context = FlightsThreadLocal.get();
		  if (context == null) {
			context = new FlightsContext();
		  }		
		 processJSonString(prev.getResponseDataAsString());	
	}
	

	public String processJSonString(String responseDataAsString) {

		try {
			JSONObject json = (JSONObject) new JSONParser().parse(responseDataAsString);
			
			JSONArray tripFlights = (JSONArray) json.get("tripFlights");
			
			if (tripFlights == null || tripFlights.size() == 0) {

				context.setIsFlightAvailable("false");
				FlightsThreadLocal.set(context);
				return null;
			}
			for (int counter = 1; counter <= tripFlights.size(); counter++) {
				if (counter == 1) {
					JSONObject jsonTripFlight = (JSONObject) tripFlights.get(0);
					JSONArray jsonFlightOptions = (JSONArray) jsonTripFlight.get("flightsOptions");
					
					String numFlightsAsString = jsonFlightOptions.size()  + "";
					context.setNumOfToFlights(numFlightsAsString);
					JMeterContextService.getContext().getVariables().put(FLIGHT_TO_COUNT, numFlightsAsString); 
					if (jsonFlightOptions.size() > 0) {
						context.setIsFlightAvailable("true");
					} else {
						context.setIsFlightAvailable("false");
					}
				} else if (counter == 2) {
					JSONObject jsonTripFlight = (JSONObject) tripFlights.get(1);
					JSONArray jsonFlightOptions = (JSONArray) jsonTripFlight.get("flightsOptions");
					String numFlightAsString = jsonFlightOptions.size() + "";
					context.setNumOfRetFlights(numFlightAsString);
					JMeterContextService.getContext().getVariables().put(FLIGHT_RET_COUNT, numFlightAsString); 
					if (jsonFlightOptions.size() > 0) {
						context.setONEWAY("false");
						JMeterContextService.getContext().getVariables().put(ONE_WAY, "false");
					} else {
						context.setONEWAY("true");
						JMeterContextService.getContext().getVariables().put(ONE_WAY, "true");
					}
				}
				
				JSONObject jsonTripFlight = (JSONObject) tripFlights.get(counter - 1);
				JSONArray jsonFlightOptions = (JSONArray) jsonTripFlight.get("flightsOptions");

				for (int tripCounter = 1; tripCounter <= jsonFlightOptions.size(); tripCounter++) {
					if (counter == 1) {
						JSONObject flightOption0 = (JSONObject) jsonFlightOptions.get(tripCounter - 1);
						if (ExtractFlightsInfoFunction.pureIDs) {
							String id = (String)flightOption0.get("_id");
							String fsId = (String)flightOption0.get("flightSegmentId");
							context.setTOFLIGHT(id);
							context.setTOSEGMENTID(fsId);
						}
						else {
							JSONObject flightOption0Pkey = (JSONObject) flightOption0.get("pkey");
							context.setTOFLIGHT((String) flightOption0Pkey.get("id"));
							context.setTOSEGMENTID((String) flightOption0Pkey.get("flightSegmentId"));
						}
					} else if (counter == 2) {
						JSONObject flightOption0 = (JSONObject) jsonFlightOptions.get(tripCounter - 1);
						if (ExtractFlightsInfoFunction.pureIDs) {
							String id = (String)flightOption0.get("_id");
							String fsId = (String)flightOption0.get("flightSegmentId");
							context.setRETFLIGHT(id);
							context.setRESEGMENTID(fsId);
						}
						else {
							JSONObject flightOption0Pkey = (JSONObject) flightOption0.get("pkey");
							context.setRETFLIGHT((String) flightOption0Pkey.get("id"));
							context.setRESEGMENTID((String) flightOption0Pkey.get("flightSegmentId"));
						}
					}
				}
			}
			FlightsThreadLocal.set(context);

			return json.toJSONString();

		} catch (ParseException e) {
			System.out.println("responseDataAsString = " + responseDataAsString);
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("NullPointerException in FlightsPostProcessor - ResponseData =" + responseDataAsString);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public List<String> getArgumentDesc() {
		return DESC;
	}

}
