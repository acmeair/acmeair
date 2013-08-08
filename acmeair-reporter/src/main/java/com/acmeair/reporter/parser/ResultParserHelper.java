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

import com.googlecode.charts4j.Color;

public class ResultParserHelper {

	public static Color getColor(int i) {
		Color[] colors = { Color.RED, Color.BLACK, Color.BLUE, Color.YELLOW,
				Color.GREEN, Color.ORANGE, Color.PINK, Color.SILVER,
				Color.GOLD, Color.WHITE, Color.BROWN, Color.CYAN,Color.GRAY,Color.HONEYDEW,Color.IVORY };
		return colors[i % 15];
	}

	public static <E> ArrayList<E> scaleDown(ArrayList<E> testList, int scaleDownFactor) {
		
		if (testList==null) {
			return null;
		}
		if (testList.size() <= 7)
			return testList;
		if (scaleDownFactor > 10 || scaleDownFactor < 0) {
			throw new RuntimeException(
					"currently only support factor from 0-10");
		}
		int listLastItemIndex = testList.size() - 1;
		int a = (int) java.lang.Math.pow(2, scaleDownFactor);
		if (a > listLastItemIndex) {
			return testList;
		}
		ArrayList<E> newList = new ArrayList<E>();
		newList.add(testList.get(0));
	
		if (scaleDownFactor == 0) {
			newList.add(testList.get(listLastItemIndex));
	
		} else {
	
			for (int m = 1; m <= a; m++) {
				newList.add(testList.get(listLastItemIndex * m / a));
			}
		}
		return newList;
	}

	public static double[] scaleInputsData(ArrayList<Double> inputList,
			double scale_factor) {
		double[] inputs = new double[inputList.size()];
		for (int i = 0; i <= inputList.size() - 1; i++) {
			inputs[i] = inputList.get(i) * scale_factor;
		}
		return inputs;
	}
}