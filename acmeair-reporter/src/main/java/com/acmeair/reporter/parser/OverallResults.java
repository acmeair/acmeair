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

public class OverallResults {
	private ArrayList<Double> allInputList = new ArrayList<Double>();
	private ArrayList<String> allTimeList = new ArrayList<String>();
	private double scale_max;
	private double overallScale_max;

	public ArrayList<Double> getAllInputList() {
		return allInputList;
	}

	public void setAllInputList(ArrayList<Double> allInputList) {
		this.allInputList = new ArrayList<Double> (allInputList);
	}

	public ArrayList<String> getAllTimeList() {
		return allTimeList;
	}

	public void setAllTimeList(ArrayList<String> allTimeList) {
		this.allTimeList = new ArrayList<String> (allTimeList);
	}

	public double getOverallScale_max() {
		return overallScale_max;
	}

	public void setOverallScale_max(double overallScale_max) {
		this.overallScale_max = overallScale_max;
	}

	public double getScale_max() {
		return scale_max;
	}

	public void setScale_max(double scale_max) {
		this.scale_max = scale_max;
	}
}
