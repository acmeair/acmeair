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
import java.io.IOException;

import java.io.BufferedReader;

import java.io.FileReader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import com.acmeair.reporter.util.Messages;


public class JmeterJTLParser {
    
	private String jmeterJTLFileName = "AcmeAir[1-9].jtl";
	
    private String regEx =
        "<httpSample\\s*" + 
          "t=\"([^\"]*)\"\\s*"  +  
          "lt=\"([^\"]*)\"\\s*" +  
          "ts=\"([^\"]*)\"\\s*" +  
          "s=\"([^\"]*)\"\\s*"  +  
          "lb=\"([^\"]*)\"\\s*" +  
          "rc=\"([^\"]*)\"\\s*" +  
          "rm=\"([^\"]*)\"\\s*" +  
          "tn=\"([^\"]*)\"\\s*" +  
          "dt=\"([^\"]*)\"\\s*" +  
          "by=\"([^\"]*)\"\\s*" + 
          "FLIGHTTOCOUNT=\"([^\"]*)\"\\s*" +
          "FLIGHTRETCOUNT=\"([^\"]*)\"\\s*"+
          "ONEWAY\\s*=\"([^\"]*)\"\\s*";
    // NOTE: The regular expression depends on user.properties in jmeter having the sample_variables property added.
    //       sample_variables=FLIGHTTOCOUNT,FLIGHTRETCOUNT,ONEWAY
    

    private int GROUP_T  = 1;
    private int GROUP_TS = 3;
    private int GROUP_S  = 4;
    private int GROUP_LB = 5;
    private int GROUP_RC = 6;
    private int GROUP_TN = 8;
    private int GROUP_FLIGHTTOCOUNT = 11;
    private int GROUP_FLIGHTRETCOUNT = 12;
    private int GROUP_ONEWAY = 13;
        
    
    private  JtlTotals totalAll;
    private Map<String, JtlTotals> totalUrlMap;

    public JmeterJTLParser() {
    	totalAll = new JtlTotals();
    	totalUrlMap = new HashMap<String, JtlTotals>(); 
    	
       	String jtlRegularExpression = Messages.getString("parsers.JmeterJTLParser.jtlRegularExpression");
    	if (jtlRegularExpression != null){
    		System.out.println("set regex string to be '" + jtlRegularExpression+ "'");
    		regEx = jtlRegularExpression;
    	}
    	
      	String matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.t");
    	if (matcherGroup != null){
    		GROUP_T = new Integer(matcherGroup).intValue();
    	}
    	
      	matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.ts");
    	if (matcherGroup != null){
    		GROUP_TS = new Integer(matcherGroup).intValue();
    	}
    	
      	matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.s");
    	if (matcherGroup != null){
    		GROUP_S = new Integer(matcherGroup).intValue();
    	}   
    	
      	matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.lb");
    	if (matcherGroup != null){
    		GROUP_LB = new Integer(matcherGroup).intValue();
    	}    	
    	
      	matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.rc");
    	if (matcherGroup != null){
    		GROUP_RC = new Integer(matcherGroup).intValue();
    	}
    	
      	matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.tn");
    	if (matcherGroup != null){
    		GROUP_TN = new Integer(matcherGroup).intValue();
    	}    
    	
      	matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.FLIGHTTOCOUNT");
    	if (matcherGroup != null){
    		GROUP_FLIGHTTOCOUNT = new Integer(matcherGroup).intValue();
    	}
    	
     	matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.FLIGHTRETCOUNT");
    	if (matcherGroup != null){
    		GROUP_FLIGHTRETCOUNT = new Integer(matcherGroup).intValue();
    	}    
    	
     	matcherGroup = Messages.getString("parsers.JmeterJTLParser.regexGroups.ONEWAY");
    	if (matcherGroup != null){
    		GROUP_ONEWAY = new Integer(matcherGroup).intValue();
    	}   
    	
      	String responseTimeStepping = Messages.getString("parsers.JmeterJTLParser.responseTimeStepping");
    	if (responseTimeStepping != null){
    		JtlTotals.setResponseTimeStepping(new Integer(responseTimeStepping).intValue());
    	}    	
    }

    
    public void setLogFileName (String logFileName) {
    	this.jmeterJTLFileName = logFileName;
    }
    
    
    public void processResultsDirectory(String dirName) {
    	File root = new File(dirName);
    	try {
    		Collection<File> files = FileUtils.listFiles(root,
    				new RegexFileFilter(jmeterJTLFileName),
    				DirectoryFileFilter.DIRECTORY);

    		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
    			File file = (File) iterator.next();
    			parse(file);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    
    public void parse(File jmeterJTLfile) throws IOException {
    	if(totalAll == null){
    		totalAll = new JtlTotals();
    		totalUrlMap = new HashMap<String, JtlTotals>(); 
    	}
    	totalAll.incrementFiles();
        Pattern pattern = Pattern.compile(regEx);
        HashMap <String, Integer> threadCounter = new HashMap<String, Integer>();
        
        BufferedReader reader = new BufferedReader(new FileReader(jmeterJTLfile));
        try {
            String line = reader.readLine();
            while(line != null) {
            	
                Matcher matcher = pattern.matcher(line);
                if(matcher.find()) {
                    add(matcher, totalAll);
                    
                    String url = matcher.group(GROUP_LB);
                    JtlTotals urlTotals = totalUrlMap.get(url);
                    if(urlTotals == null) {
                        urlTotals = new JtlTotals();                        
                        totalUrlMap.put(url, urlTotals);
                    }
                    add(matcher, urlTotals);
                    String threadName = matcher.group(GROUP_TN);
                    Integer threadCnt = threadCounter.get(threadName);
                    if(threadCnt == null) {
                    	threadCnt = new Integer(1);
                    }else{
                    	threadCnt = Integer.valueOf(threadCnt.intValue()+1);
                    }
                    threadCounter.put(threadName, threadCnt);
                }                
                line = reader.readLine();
            }
            
        } finally {
        	reader.close();
        }
        totalAll.setThreadMap(threadCounter);
        if(totalAll.getCount() == 0) {
            System.out.println("JmeterJTLParser - No results found!");
            return;
        }
    } 
    
    public JtlTotals getResults() {
    	return totalAll;
    }

    public Map<String, JtlTotals> getResultsByUrl() {
    	return totalUrlMap;
    }
    
    private void add(Matcher matcher, JtlTotals total) {
        
        long timestamp = Long.parseLong(matcher.group(GROUP_TS));
        total.addTimestamp(timestamp);
        
        int time = Integer.parseInt(matcher.group(GROUP_T));
        total.addTime(time);
                
        String rc = matcher.group(GROUP_RC);
        total.addReturnCode(rc);
              
        if(!matcher.group(GROUP_S).equalsIgnoreCase("true")) {
        	total.incrementFailures();
        }

        String strFlightCount = matcher.group(GROUP_FLIGHTTOCOUNT);
        if (strFlightCount != null && !strFlightCount.isEmpty()){  
        	int count = Integer.parseInt(strFlightCount);
        	total.addToFlight(count);        	
        }        

        strFlightCount = matcher.group(GROUP_FLIGHTRETCOUNT);
        if (strFlightCount != null && !strFlightCount.isEmpty()){
        	total.addFlightRetCount(Integer.parseInt(strFlightCount));
        }
        
        String oneWay = matcher.group(GROUP_ONEWAY);
        if (oneWay != null && oneWay.equalsIgnoreCase("true")){        	
        	total.incrementOneWayCount();
        }        
    } 
} 
