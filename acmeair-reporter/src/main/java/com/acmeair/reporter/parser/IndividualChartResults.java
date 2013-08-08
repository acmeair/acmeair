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

public class IndividualChartResults {
	private ArrayList<Double> inputList = new ArrayList<Double>();
	private String title;
	private ArrayList<String> timeList = new ArrayList<String>();
	private int files = 0;
	
	public void setTitle(String title) {
		this.title = title;
	}
	public ArrayList<Double> getInputList() {
		return inputList;
	}
	public void setInputList(ArrayList<Double> inputList) {
		this.inputList = inputList;
	}
	public ArrayList<String> getTimeList() {
		return timeList;
	}
	public void setTimeList(ArrayList<String> timeList) {
		this.timeList = timeList;
	}
	
	public String getTitle() {
		return title;
	}
	
    public void incrementFiles(){
    	files++;
    }
    
    public int getFilesCount(){
    	return files;
    }
	
}
