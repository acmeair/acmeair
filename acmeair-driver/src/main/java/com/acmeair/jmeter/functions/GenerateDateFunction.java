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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

public class GenerateDateFunction extends AbstractFunction {

	private static final List<String> DESC = Arrays.asList("generate_date");
	private static final String KEY = "__generateDate";

	private List<CompoundVariable> parameters = Collections.emptyList();

	@Override
	public String execute(SampleResult arg0, Sampler arg1)
			throws InvalidVariableException {
		SimpleDateFormat date_format = new SimpleDateFormat(
				"EEE MMM dd 00:00:00 z yyyy");
		if (parameters.get(0).execute().equalsIgnoreCase("from")) {
			Calendar aDay = Calendar.getInstance();
			aDay.add(Calendar.DATE, new Random().nextInt(6));
			return date_format.format(aDay.getTime()).toString();
		} else if (parameters.get(0).execute().equalsIgnoreCase("return")) {
			Calendar aDay = Calendar.getInstance();
			aDay.add(Calendar.DATE, new Random().nextInt(7) + 6);
			return date_format.format(aDay.getTime()).toString();
		}
		return "";
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
