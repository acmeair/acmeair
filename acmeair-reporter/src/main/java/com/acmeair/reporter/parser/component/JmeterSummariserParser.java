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
package com.acmeair.reporter.parser.component;

import java.io.File;

import com.acmeair.reporter.parser.IndividualChartResults;
import com.acmeair.reporter.parser.ResultParser;
import com.acmeair.reporter.parser.ResultParserHelper;

public class JmeterSummariserParser extends ResultParser {

	private static boolean SKIP_JMETER_DROPOUTS = false; 
	static {
		SKIP_JMETER_DROPOUTS = System.getProperty("SKIP_JMETER_DROPOUTS") != null;
	}
	
	private String jmeterFileName = "AcmeAir[1-9].log";	
	private String testDate = "";
	
	@Override
	protected void processFile(File file) {
		IndividualChartResults result= getData(file.getPath());		
		super.processData(ResultParserHelper.scaleDown(result.getInputList(),8),false);
		IndividualChartResults individualResults = new IndividualChartResults();
		if(result.getTitle() != null){
			individualResults.setTitle(result.getTitle());
		} else {
			individualResults.setTitle(file.getName());
		}
		individualResults.setInputList(ResultParserHelper.scaleDown(result.getInputList(),6));
		individualResults.setTimeList(ResultParserHelper.scaleDown(result.getTimeList(),3));
		super.getMultipleChartResults().getResults().add(individualResults);
	}

	@Override
	public String getFileName() {
		return jmeterFileName;
	}

	@Override
	public void setFileName(String fileName) {
		jmeterFileName = fileName;
	}

	public String getTestDate(){
		return testDate;
	}

	@Override
	protected void processLine(IndividualChartResults results, String strLine) {		
		if (strLine.indexOf("summary +") > 0) {			
			String[] tokens = strLine.split(" ");
			results.getTimeList().add(tokens[1].trim());
			testDate = tokens[0].trim();		
			int endposition = strLine.indexOf("/s");
			int startposition = strLine.indexOf("=");
			String thoughputS = strLine.substring(startposition + 1, endposition).trim();
			Double throughput = Double.parseDouble(thoughputS);
			if (throughput == 0.0 && SKIP_JMETER_DROPOUTS) {
				return;
			}
			results.getInputList().add(throughput);
		} else if (strLine.indexOf("Name:") > 0) {
			int startIndex = strLine.indexOf(" Name:")+7;
			int endIndex = strLine.indexOf(" ", startIndex);
			String name = strLine.substring(startIndex, endIndex);
			results.setTitle(name);
		}
	}
}