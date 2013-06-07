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

public class FlightsThreadLocal {
	public static final ThreadLocal<FlightsContext> userThreadLocal = new ThreadLocal<FlightsContext>();

	public static void set(FlightsContext context) {
		userThreadLocal.set(context);
	}

	public static void unset() {
		userThreadLocal.remove();
	}

	public static FlightsContext get() {
		return userThreadLocal.get();
	}
}
