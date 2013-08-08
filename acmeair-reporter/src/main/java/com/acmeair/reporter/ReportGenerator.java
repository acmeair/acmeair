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
package com.acmeair.reporter;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.generic.ComparisonDateTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;

import com.acmeair.reporter.util.Messages;
import com.acmeair.reporter.util.StatResult;
import com.acmeair.reporter.parser.IndividualChartResults;
import com.acmeair.reporter.parser.ResultParser;
import com.acmeair.reporter.parser.ResultParserHelper;
import com.acmeair.reporter.parser.component.JmeterJTLParser;
import com.acmeair.reporter.parser.component.JmeterSummariserParser;
import com.acmeair.reporter.parser.component.JtlTotals;
import com.acmeair.reporter.parser.component.NmonParser;

//import freemarker.cache.ClassTemplateLoader;
//import freemarker.template.Configuration;
//import freemarker.template.Template;

public class ReportGenerator {
	private static final int max_lines = 15;
			 
	private static final String RESULTS_FILE = Messages.getString("ReportGenerator.RESULT_FILE_NAME");    	
	private static String searchingLocation = Messages.getString("inputDirectory"); 
	private static String jmeterFileName = Messages.getString("ReportGenerator.DEFAULT_JMETER_FILENAME"); 
	private static String nmonFileName = Messages.getString("ReportGenerator.DEFAULT_NMON_FILE_NAME"); 
	
	private static final String BOOK_FLIGHT = "BookFlight";
	private static final String CANCEL_BOOKING = "Cancel Booking";
	private static final String LOGIN = "Login";
	private static final String LOGOUT = "logout";	
	private static final String LIST_BOOKINGS = "List Bookings";
	private static final String QUERY_FLIGHT = "QueryFlight";
	private static final String UPDATE_CUSTOMER = "Update Customer";
	private static final String VIEW_PROFILE = "View Profile Information";
	
	
	private LinkedHashMap<String,ArrayList<String>> charMap = new LinkedHashMap<String,ArrayList<String>>();
	
	public static void main(String[] args) {
		if (args.length == 1) {
			searchingLocation = (args[0]);
		}
		if (!new File(searchingLocation).isDirectory()) {
			System.out.println("\"" + searchingLocation + "\" is not a valid directory");
			return;
		}
		System.out.println("Parsing acme air test results in the location \"" + searchingLocation + "\""); 
		
		ReportGenerator generator = new ReportGenerator();
		long start, stop;
		start = System.currentTimeMillis();
		generator.process();
		stop = System.currentTimeMillis();
		System.out.println("Results generated in " + (stop - start)/1000.0 + " seconds");
	}

	public void process() {
		long start, stop;
		String overallChartTitle = Messages.getString("ReportGenerator.THROUGHPUT_TOTAL_LABEL"); 
		String throughputChartTitle = Messages.getString("ReportGenerator.THROUGHPUT_TITLE"); 
		String yAxisLabel = Messages.getString("ReportGenerator.THROUGHPUT_YAXIS_LABEL");
		Map<String, Object> input = new HashMap<String, Object>();	
		start = System.currentTimeMillis();
		JmeterSummariserParser jmeterParser = new JmeterSummariserParser();
		jmeterParser.setFileName(jmeterFileName);
		jmeterParser.setMultipleChartTitle(throughputChartTitle);
		jmeterParser.setMultipleYAxisLabel(yAxisLabel);
		jmeterParser.processDirectory(searchingLocation);
		//always call it before call generating multiple chart string		
		String url = jmeterParser.generateChartStrings(overallChartTitle, yAxisLabel,
				"", jmeterParser.processData(jmeterParser.getAllInputList(), true),
				ResultParserHelper.scaleDown(jmeterParser.getAllTimeList(), 3), false);
		ArrayList<String> list = new ArrayList<String>();		
		list.add(url);
		charMap.put(overallChartTitle, list);
		generateMulitpleLinesChart(jmeterParser);
		
		charMap.put(throughputChartTitle, jmeterParser.getCharStrings());
		
		StatResult jmeterStats = StatResult.getStatistics(jmeterParser.getAllInputList());
    	input.put("jmeterStats", jmeterStats);
    	if(!jmeterParser.getAllTimeList().isEmpty()){
    		input.put("testStart", jmeterParser.getTestDate() + " " + jmeterParser.getAllTimeList().get(0));
    		input.put("testEnd", jmeterParser.getTestDate() + " " + jmeterParser.getAllTimeList().get(jmeterParser.getAllTimeList().size()-1));
    	}
		
    	input.put("charUrlMap", charMap);
		
		stop = System.currentTimeMillis();
		System.out.println("Parsed jmeter in " + (stop - start)/1000.0 + " seconds");
		
		start = System.currentTimeMillis();
		JmeterJTLParser jtlParser = new JmeterJTLParser();
		jtlParser.processResultsDirectory(searchingLocation);
		
    	input.put("totals", jtlParser.getResults());
    	String urls[] = {BOOK_FLIGHT,CANCEL_BOOKING,LOGIN,LOGOUT,LIST_BOOKINGS,QUERY_FLIGHT,UPDATE_CUSTOMER,VIEW_PROFILE,"Authorization"};

    	input.put("totalUrlMap" ,reorderTestcases(jtlParser.getResultsByUrl(), urls));	      
    	input.put("queryTotals", getTotals(QUERY_FLIGHT, jtlParser.getResultsByUrl()));
    	input.put("bookingTotals", getTotals(BOOK_FLIGHT, jtlParser.getResultsByUrl()));
    	input.put("loginTotals", getTotals(LOGIN, jtlParser.getResultsByUrl()));

		stop = System.currentTimeMillis();
		System.out.println("Parsed jmeter jtl files in " + (stop - start)/1000.0 + " seconds");



    	List<Object> nmonParsers = Messages.getConfiguration().getList("parsers.nmonParser.directory");
    	if (nmonParsers != null){
        	LinkedHashMap<String,StatResult> cpuList = new LinkedHashMap<String,StatResult>();
    		start = System.currentTimeMillis();
    		for(int i = 0;i < nmonParsers.size(); i++) {
    			
    			String enabled = Messages.getString("parsers.nmonParser("+i+")[@enabled]");			
    			if (enabled == null ||  !enabled.equalsIgnoreCase("false")) {

    				String directory = Messages.getString("parsers.nmonParser("+i+").directory");    				
    				String chartTitle = Messages.getString("parsers.nmonParser("+i+").chartTitle");
    				String label = Messages.getString("parsers.nmonParser("+i+").label");
    				String fileName = Messages.getString("parsers.nmonParser("+i+").fileName");    				
    				String relativePath = Messages.getString("parsers.nmonParser("+i+").directory[@relative]");
    				
    				if (relativePath == null ||  !relativePath.equalsIgnoreCase("false")) {
    					directory = searchingLocation +"/" + directory;
    				} 
    				if (fileName == null){
    					fileName = nmonFileName;
    				}

    				NmonParser nmon = parseNmonDirectory(directory, fileName, chartTitle);
    				cpuList  = addCpuStats(nmon, label, cpuList);
    			}
    		}
 
    		input.put("cpuList", cpuList);

    		stop = System.currentTimeMillis();
    		System.out.println("Parsed nmon files in " + (stop - start)/1000.0 + " seconds");
       	}				
		
		
		if (charMap.size() > 0) {
			start = System.currentTimeMillis();
			generateHtmlfile(input);
			stop = System.currentTimeMillis();
			System.out.println("Generated html file in " + (stop - start)/1000.0 + " seconds");
			System.out.println("Done, charts were saved to \""
							+ searchingLocation + System.getProperty("file.separator") + RESULTS_FILE + "\""); 
		} else {
			System.out.println("Failed, cannot find valid \"" 
							+ jmeterFileName + "\" or \"" + nmonFileName + "\" files in location " + searchingLocation); 
		}
	}
	
	private void generateMulitpleLinesChart(ResultParser parser) {
		if (parser.getResults().size()<=max_lines){
			parser.generateMultipleLinesCharString(parser.getMultipleChartTitle(),
				parser.getMultipleYAxisLabel(), "", parser.getResults());
		}else {
			System.out.println("More than "+max_lines+" throughput files found, will break them to "+max_lines+" each");
			ArrayList<IndividualChartResults> results= parser.getResults();
			int size = results.size();
			for (int i=0;i<size;i=i+max_lines){
				int endLocation = i+max_lines;
				if (endLocation >size) {
					endLocation=size;
				}
				parser.generateMultipleLinesCharString(parser.getMultipleChartTitle(),
						parser.getMultipleYAxisLabel(), "", results.subList(i,endLocation)); 
			}
		}
	}
	    

    
    private ArrayList<Double> getCombinedResultsList (NmonParser parser){
		Iterator<IndividualChartResults> itr = parser.getMultipleChartResults().getResults().iterator();
        ArrayList<Double> resultList = new ArrayList<Double>();
		while(itr.hasNext()){
			//trim trailing idle times from each of the individual results,
			//then combine the results together to get the final tallies. 				
			ArrayList<Double>  curList = itr.next().getInputList();
			
			for(int j = curList.size() - 1; j >= 0; j--){
				  if (curList.get(j).doubleValue() < 1){
					  curList.remove(j);
				  }
			}
			resultList.addAll(curList);
		}
		return resultList;
    }
   /* 
    private void generateHtmlfile(Map<String, Object> input) {	   
	    try{
	    	Configuration cfg = new Configuration();
	    	ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/templates");
	    	cfg.setTemplateLoader(ctl);	    	
	    	Template template = cfg.getTemplate("acmeair-report.ftl");
	    	
	    	Writer file = new FileWriter(new File(searchingLocation
					+ System.getProperty("file.separator") + RESULTS_FILE));
	    	template.process(input, file);
	    	file.flush();
	    	file.close();
	      
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
    }
    
    */
    
    
    private void generateHtmlfile(Map<String, Object> input) {	   
	    try{
	    	VelocityEngine ve = new VelocityEngine();
	    	ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
	    	ve.setProperty("classpath.resource.loader.class",ClasspathResourceLoader.class.getName());
	    	ve.init();
	    	Template template = ve.getTemplate("templates/acmeair-report.vtl");
	    	VelocityContext context = new VelocityContext();
	    	 	    
	    	 
	    	for(Map.Entry<String, Object> entry: input.entrySet()){
	    		context.put(entry.getKey(), entry.getValue());
	    	}
	    	context.put("math", new MathTool());
	    	context.put("number", new NumberTool());
	    	context.put("date", new ComparisonDateTool());
	    	
	    	Writer file = new FileWriter(new File(searchingLocation
					+ System.getProperty("file.separator") + RESULTS_FILE));	    
	    	template.merge( context, file );
	    	file.flush();
	    	file.close();
	      
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
    }

    private LinkedHashMap<String,StatResult> addCpuStats(NmonParser parser, String label, LinkedHashMap<String,StatResult> toAdd){
    	if (parser != null) {				
    		StatResult cpuStats = StatResult.getStatistics(getCombinedResultsList(parser));
    		cpuStats.setNumberOfResults(parser.getMultipleChartResults().getResults().size());
    		toAdd.put(label, cpuStats);			
    	}else {
    		System.out.println("no "+label+" cpu data found");
    	}
    	return toAdd;
    }
    
    
    /**
     * Re-orders a given map to using an array of Strings. 
     * Any remaining items in the map that was passed in will be appended to the end of
     * the map to be returned. 
     * @param totalUrlMap the map to be re-ordered. 
     * @param urls An array of Strings with the desired order for the map keys.
     * @return     A LinkedHashMap with the keys in the order requested. 
     * @see LinkedHashMap
     */
    private Map<String,JtlTotals> reorderTestcases(Map<String,JtlTotals> totalUrlMap, String urls[]){
    	LinkedHashMap<String,JtlTotals> newMap = new LinkedHashMap<String,JtlTotals>();
    	
    	Iterator<String> keys;
		for(int i=0; i< urls.length;i++){
			keys  = totalUrlMap.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();		        	      
	        	if(key.toLowerCase().contains(urls[i].toLowerCase())){
	        		newMap.put(key, totalUrlMap.get(key));	        		
	        	}			        	
	        }
		}
		//loop 2nd time to get the remaining items
		keys  = totalUrlMap.keySet().iterator();
		while (keys.hasNext()) {
	        String key = keys.next();
	        boolean found = false;		        
	        for(int i=0; i< urls.length;i++){
	        	if(key.toLowerCase().contains(urls[i].toLowerCase())){
	        		found = true;	
	        	}
	        }
	        if(!found){
	        	newMap.put(key, totalUrlMap.get(key));	        	
        	}
		}		
    	return newMap;
    }
  
    /**
     * Searches the map for the given jmeter testcase url key. 
     * The passed in string is expected to contain all or part of the desired key. 
     * for example "QueryFlight"  could match both "Mobile QueryFlight" and "Desktop QueryFlight" or just "QueryFlight".
     * If multiple results are found, their totals are added togehter in the JtlTotals Object returned. 
     * 
     * @param url         String, jMeter Testcase URL string to search for. 
     * @param totalUrlMap Map containing Strings and JtlTotals results. 
     * @return   JtlTotals object. 
     * @see JtlTotals
     */
    private JtlTotals getTotals(String url, Map<String,JtlTotals> totalUrlMap){
    	JtlTotals urlTotals = null;
    	Iterator<String> keys  = totalUrlMap.keySet().iterator();

    	while (keys.hasNext()) {
    		String key = keys.next();
    		if(key.toLowerCase().contains(url.toLowerCase())){

    			if(urlTotals == null){
    				urlTotals = totalUrlMap.get(key);
    			}else {
    				urlTotals.add(totalUrlMap.get(key));
    			}
    		}
    	}
    	return urlTotals;
    }

	
	/**
	 * Sets up a new NmonParser Object for parsing a given directory.
	 * @param directory   directory to search for nmon files.
	 * @param chartTitle  Name of the title for the chart to be generated. 
	 * @return            NmonParser object
	 */
	private NmonParser parseNmonDirectory (String directory, String fileName, String chartTitle ){	
		if (!new File(directory).isDirectory()) {
			return null;
		}		
		NmonParser parser = new NmonParser();
		parser.setFileName(fileName);
		parser.setMultipleChartTitle(chartTitle);
		parser.processDirectory(directory);
		generateMulitpleLinesChart(parser);
		charMap.put(chartTitle, parser.getCharStrings());
		return parser;
	}
}
