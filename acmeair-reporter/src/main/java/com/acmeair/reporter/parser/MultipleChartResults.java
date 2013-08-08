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
package com.acmeair.reporter.parser;

import java.util.ArrayList;

public class MultipleChartResults {
	
	private String multipleChartTitle;
	private String multipleChartYAxisLabel;
	private ArrayList<IndividualChartResults> results = new  ArrayList<IndividualChartResults> ();
	private ArrayList<String> charStrings= new ArrayList<String>();
	
	public String getMultipleChartTitle() {
		return multipleChartTitle;
	}
	public void setMultipleChartTitle(String multipleChartTitle) {
		this.multipleChartTitle = multipleChartTitle;
	}
	public String getMultipleChartYAxisLabel() {
		return multipleChartYAxisLabel;
	}
	public void setMultipleChartYAxisLabel(String multipleChartYAxisLabel) {
		this.multipleChartYAxisLabel = multipleChartYAxisLabel;
	}

	public ArrayList<IndividualChartResults> getResults() {
		return results;
	}
	public void setResults(ArrayList<IndividualChartResults> results) {
		this.results = results;
	}
		
	public ArrayList<String> getCharStrings() {
		return charStrings;
	}
	public void setCharStrings(ArrayList<String> charStrings) {
		this.charStrings = charStrings;
	}
}
