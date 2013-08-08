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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class JtlTotals {
    private static final String DECIMAL_PATTERN = "#,##0.0##";
    private static final double MILLIS_PER_SECOND = 1000.0;
    private static int millisPerBucket = 500;
    private int files = 0;
    private int request_count = 0;
    private int time_sum = 0;
    private int time_max = 0; 
    private int time_min = Integer.MAX_VALUE; 

    private int failures = 0;
    private long timestamp_start = Long.MAX_VALUE; 
    private long timestamp_end = 0;  
    private Map<String, Integer> rcMap = new HashMap<String, Integer>(); // key rc, value count
    private Map<Integer, Integer> millisMap = new TreeMap<Integer, Integer>(); // key bucket Integer, value count
    private Map <String, Integer> threadMap = new HashMap<String,Integer>(); 
    private ArrayList<Integer> timeList = new ArrayList<Integer>();
    private long flight_to_sum = 0;
    private long flight_to_count = 0;
	private int flight_to_empty_count = 0;    
    private long flight_ret_count = 0;
    private long one_way_count = 0;
    

    
    public JtlTotals() {
    }
    
    
    public void add(JtlTotals totals){
      rcMap.putAll(totals.getReturnCodeCounts());
      millisMap.putAll(totals.getMillisMap());
      threadMap.putAll(totals.getThreadMap());
      one_way_count += totals.getOneWayCount();
      flight_ret_count += totals.getFlightRetCount();
      flight_to_empty_count += totals.getEmptyToFlightCount();
      flight_to_sum += totals.getFlightToSum();
      flight_to_count += totals.getFlightToCount();
      failures += totals.getFailures();
      request_count += totals.getCount();  
    }
    
    public long getFlightToCount() {
		return flight_to_count;
	}
    
    public void addTime(int time){
    	request_count++;
    	time_sum+=time;
        time_max = Math.max(time_max, time);
        time_min = Math.min(time_min, time);
        timeList.add(time);
        Integer bucket = new Integer(time / millisPerBucket);
        Integer count = millisMap.get(bucket);
        if(count == null) {
            count = new Integer(0);
        }
        millisMap.put(bucket, new Integer(count.intValue() + 1));
    }
    
    public Map<Integer, Integer> getMillisMap() {
		return millisMap;
	}


	public void addReturnCode(String rc){
        Integer rc_count = rcMap.get(rc);
        if(rc_count == null) {
            rc_count = new Integer(0);
        }
        rcMap.put(rc, new Integer(rc_count.intValue() + 1));    
    }
    
    public void setThreadMap(Map<String,Integer> threadMap){
    	this.threadMap = threadMap;
    }
    
    public void addTimestamp(long timestamp){
    	timestamp_end = Math.max(timestamp_end, timestamp);
        timestamp_start = Math.min(timestamp_start, timestamp);
    }
    
    public void incrementFailures(){
    	failures++;
    }
    
    public void addToFlight(int count){
    	this.flight_to_count++;
    	this.flight_to_sum += count;
    	if(count == 0)
    		this.flight_to_empty_count++;	
    }
      
    public void addFlightRetCount(int count){
    	this.flight_ret_count += count;
    }
    
    public void incrementOneWayCount(){
    	one_way_count++;
    }
    
    public void incrementFiles(){
    	files++;
    }
    
    public int getFilesCount(){
    	return files;
    }
    
    public int getCount(){
    	return request_count;
    }
    
    public Map<String,Integer> getThreadMap(){
    	return this.threadMap;
    }
    
    public int getAverageResponseTime(){
    	//in case .jtl file doesn't exist, request_count could be 0
    	//adding this condition to avoid "divide by zero" runtime exception
    	if (request_count==0) {
    		return time_sum;
    	}
    	return  (time_sum/request_count);
    }

    public int getMaxResponseTime(){
    	return time_max;
    }

    public int getMinResponseTime(){
    	return time_min;
    }
    public int getFailures(){
    	return failures;
    }
    public int get90thPrecentile(){
    	if(timeList.isEmpty()){
    		return  Integer.MAX_VALUE; 
    	}
    	int target = (int)Math.round(timeList.size() * .90 );
    	Collections.sort(timeList); 
    	if(target == timeList.size()){target--;}    	
    	return timeList.get(target);
    }    

    public Map<String, Integer> getReturnCodeCounts(){
    	return rcMap;
    }

    public long getElapsedTimeInSeconds(){
        double secondsElaspsed = (timestamp_end - timestamp_start) / MILLIS_PER_SECOND;
        return Math.round(secondsElaspsed);
    }
    
    public long getRequestsPerSecond (){      
        return  Math.round(request_count / getElapsedTimeInSeconds());
    }
    
    public long getFlightToSum(){
    	return flight_to_sum;
    }

    public long getEmptyToFlightCount(){
    	return flight_to_empty_count;
    }    

    public float getAverageToFlights(){
    	return (float)flight_to_sum/flight_to_count;
    }
    
    public long getFlightRetCount(){
    	return flight_ret_count;
    }
    
    public long getOneWayCount(){
    	return one_way_count;
    }
    
    public static void setResponseTimeStepping(int milliseconds){
    	millisPerBucket = milliseconds;
    }
    
    public static int getResponseTimeStepping(){
    	return millisPerBucket;
    }
    
    public String cntByTimeString() {
        DecimalFormat df = new DecimalFormat(DECIMAL_PATTERN);
        List<String> millisStr = new LinkedList<String>();
        
        Iterator <Entry<Integer,Integer>>iter = millisMap.entrySet().iterator();
        while(iter.hasNext()) {
            Entry<Integer,Integer> millisEntry = iter.next();
            Integer bucket = (Integer)millisEntry.getKey();
            Integer bucketCount = (Integer)millisEntry.getValue();
            
            int minMillis = bucket.intValue() * millisPerBucket;
            int maxMillis = (bucket.intValue() + 1) * millisPerBucket;
            
            millisStr.add(
              df.format(minMillis/MILLIS_PER_SECOND)+" s "+
              "- "+
              df.format(maxMillis/MILLIS_PER_SECOND)+" s "+
              "= " + bucketCount);
        }
        return millisStr.toString();
    }
    
    public HashMap<String, Integer> cntByTime() {
        DecimalFormat df = new DecimalFormat(DECIMAL_PATTERN);     
        LinkedHashMap<String, Integer> millisStr = new LinkedHashMap<String, Integer>(); 
        Iterator <Entry<Integer,Integer>>iter = millisMap.entrySet().iterator();
        while(iter.hasNext()) {
            Entry<Integer,Integer> millisEntry = iter.next();
            Integer bucket = (Integer)millisEntry.getKey();
            Integer bucketCount = (Integer)millisEntry.getValue();
            
            int minMillis = bucket.intValue() * millisPerBucket;
            int maxMillis = (bucket.intValue() + 1) * millisPerBucket;
            
            millisStr.put(
              df.format(minMillis/MILLIS_PER_SECOND)+" s "+
              "- "+
              df.format(maxMillis/MILLIS_PER_SECOND)+" s "
              , bucketCount);
        }
        return millisStr;
    }
}
