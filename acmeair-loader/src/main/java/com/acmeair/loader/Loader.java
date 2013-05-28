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
package com.acmeair.loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Loader {
	private static Logger logger = LoggerFactory.getLogger(Loader.class);

	public static void main(String args[]) throws Exception {
		Loader loader = new Loader();
		loader.execute(args);
	}
	
	private void execute(String args[]) {
		ApplicationContext ctx = null;
        /*
         * Get Properties from loader.properties file. 
         * If the file does not exist, use default values
         */
		Properties props = new Properties();
		String propFileName = "/loader.properties";
		try{			
			InputStream propFileStream = Loader.class.getResourceAsStream(propFileName);
			props.load(propFileStream);
		//	props.load(new FileInputStream(propFileName));
		}catch(FileNotFoundException e){
			logger.info("Property file " + propFileName + " not found.");
		}catch(IOException e){
			logger.info("IOException - Property file " + propFileName + " not found.");
		}
		
        String numCustomers = props.getProperty("loader.numCustomers","100");
    	System.setProperty("loader.numCustomers", numCustomers);

    	// TODO:  Later add back in other implementations
		ctx = new AnnotationConfigApplicationContext(WXSDirectAppConfig.class);
		
		FlightLoader flightLoader = ctx.getBean(FlightLoader.class);
		CustomerLoader customerLoader = ctx.getBean(CustomerLoader.class);
		
		try {
			long start = System.currentTimeMillis();
			logger.info("Start loading flights");
			flightLoader.loadFlights();
			logger.info("Start loading " +  numCustomers + " customers");
			customerLoader.loadCustomers(Long.parseLong(numCustomers));
			long stop = System.currentTimeMillis();
			logger.info("Finished loading in " + (stop - start)/1000.0 + " seconds");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}