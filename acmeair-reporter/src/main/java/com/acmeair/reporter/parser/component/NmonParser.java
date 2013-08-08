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


public class NmonParser extends ResultParser{

	private String nmonFileName = "output.nmon";
	
	public NmonParser(){
		super.setMultipleYAxisLabel("usr%+sys%"); //default label
	}
	
	@Override
	protected void processFile(File file) {
		IndividualChartResults result= getData(file.getPath());
		super.processData(ResultParserHelper.scaleDown(result.getInputList(),8),false);
		IndividualChartResults individualResults = new IndividualChartResults();

		individualResults.setTitle(result.getTitle());
		individualResults.setInputList(ResultParserHelper.scaleDown(result.getInputList(),6));
		individualResults.setTimeList(ResultParserHelper.scaleDown(result.getTimeList(),3));
		super.getMultipleChartResults().getResults().add(individualResults);
	}

	
	@Override
	public String getFileName() {
		return nmonFileName;
	}
	
	@Override
	public void setFileName(String fileName) {
		nmonFileName = fileName;
	}
	
	@Override
	protected void processLine(IndividualChartResults results, String strLine) {
		if(strLine.startsWith("AAA,host,")){
			String[] tokens = strLine.split(",");
			 results.setTitle(tokens[2].trim());			
		}
		
		if (strLine.indexOf("ZZZZ") >=0){
			String[] tokens = strLine.split(",");
			 results.getTimeList().add(tokens[2].trim());
		}
		
		if (strLine.indexOf("CPU_ALL") >=0 && strLine.indexOf("CPU Total")<0) {
			String[] tokens = strLine.split(",");
			String user = tokens[2].trim();
			String sys = tokens[3].trim();
			Double userDouble = Double.parseDouble(user);
			Double sysDouble = Double.parseDouble(sys);
			 results.getInputList().add(userDouble+sysDouble);		
		}
	}
}
