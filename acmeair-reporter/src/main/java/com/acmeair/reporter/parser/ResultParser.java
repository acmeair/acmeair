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

import static com.googlecode.charts4j.Color.ALICEBLUE;
import static com.googlecode.charts4j.Color.BLACK;
import static com.googlecode.charts4j.Color.LAVENDER;
import static com.googlecode.charts4j.Color.WHITE;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import com.acmeair.reporter.parser.component.NmonParser;
import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.DataEncoding;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.LineStyle;
import com.googlecode.charts4j.LinearGradientFill;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Shape;

public abstract class ResultParser {

	protected MultipleChartResults multipleChartResults = new MultipleChartResults();
	protected OverallResults overallResults = new OverallResults();
	
	public MultipleChartResults getMultipleChartResults() {
		return multipleChartResults;
	}

	protected void addUp(ArrayList<Double> list) {
		//if empty, don't need to add up
		if (overallResults.getAllInputList().isEmpty()) {
			overallResults.setAllInputList(list);
			return;
		}
		int size = overallResults.getAllInputList().size();
		if (size > list.size()) {
			size = list.size();
		}
		for (int i = 0; i < size; i++) {
			overallResults.getAllInputList().set(i, overallResults.getAllInputList().get(i) + list.get(i));
		}

	}

	public void processDirectory(String dirName) {
		File root = new File(dirName);
		try {
			Collection<File> files = FileUtils.listFiles(root,
					new RegexFileFilter(getFileName()),
					DirectoryFileFilter.DIRECTORY);

			for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				processFile(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String generateChartStrings(String titileLabel, String ylabel,
			String xlable, double[] inputs, ArrayList<String> timeList, boolean addToList) {
		if (inputs == null || inputs.length == 0)
			return null;
		Line line1 = Plots.newLine(Data.newData(inputs),
				Color.newColor("CA3D05"), "");
		line1.setLineStyle(LineStyle.newLineStyle(2, 1, 0));
		line1.addShapeMarkers(Shape.DIAMOND, Color.newColor("CA3D05"), 6);		
		LineChart chart = GCharts.newLineChart(line1);
		// Defining axis info and styles
		chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(0,
				overallResults.getScale_max() / 0.9));		
		if (timeList != null && timeList.size() > 0) {
			chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(timeList));
		}

		String url = generateDefaultChartSettings(titileLabel, ylabel, xlable,
				chart, addToList);		
		return url;
	}

	public String generateDefaultChartSettings(String titileLabel,
			String ylabel, String xlable, LineChart chart, boolean addToList) {
		AxisStyle axisStyle = AxisStyle.newAxisStyle(BLACK, 13,
				AxisTextAlignment.CENTER);
		AxisLabels yAxisLabel = AxisLabelsFactory.newAxisLabels(ylabel, 50.0);
		yAxisLabel.setAxisStyle(axisStyle);
		AxisLabels time = AxisLabelsFactory.newAxisLabels(xlable, 50.0);
		time.setAxisStyle(axisStyle);

		chart.addYAxisLabels(yAxisLabel);

		chart.addXAxisLabels(time);

		chart.setDataEncoding(DataEncoding.SIMPLE);

		chart.setSize(1000, 300);

		chart.setTitle(titileLabel, BLACK, 16);
		chart.setGrid(100, 10, 3, 2);
		chart.setBackgroundFill(Fills.newSolidFill(ALICEBLUE));
		LinearGradientFill fill = Fills.newLinearGradientFill(0, LAVENDER, 100);
		fill.addColorAndOffset(WHITE, 0);
		chart.setAreaFill(fill);
		String url = chart.toURLString();
		if(addToList) {
			getCharStrings().add(url);
		}
		return url;
	}

	public String generateMultipleLinesCharString(String titileLabel,
			String ylabel, String xlabel, List<IndividualChartResults> list) {

		if (list ==null || list.size()==0) {
			return null;
		}
		Line[] lines = new Line[list.size()];
		for (int i = 0; i < list.size(); i++) {
			double[] multiLineData = processMultiLineData(list.get(i).getInputList());
			if (multiLineData!=null) {
				lines[i] = Plots.newLine(Data.newData(multiLineData), ResultParserHelper.getColor(i), list.get(i).getTitle());
				lines[i].setLineStyle(LineStyle.newLineStyle(2, 1, 0));		
			} else {
				System.out.println("found jmeter log file that doesn't have data:\" " + list.get(i).getTitle() +"\" skipping!");
				return null;
			}
		}

		LineChart chart = GCharts.newLineChart(lines);
		chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(0,
				overallResults.getOverallScale_max() / 0.9));
		
		chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(list.get(0)
				.getTimeList()));
		// Defining axis info and styles
		String url = generateDefaultChartSettings(titileLabel, ylabel, xlabel,
				chart, true);		
		return url;
	}

	public ArrayList<Double> getAllInputList() {
		return overallResults.getAllInputList();
	}
	public ArrayList<String> getAllTimeList() {
		return overallResults.getAllTimeList();
	}
	public ArrayList<String> getCharStrings() {
		return getMultipleChartResults().getCharStrings();
	}

	protected <E> IndividualChartResults getData(String fileName) {
		IndividualChartResults results = new IndividualChartResults();
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				processLine(results, strLine);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}		

		addUp(results.getInputList());
		overallResults.setAllTimeList(results.getTimeList());
		return results;
	}

	public abstract String getFileName();
	
	public abstract void setFileName(String fileName);

	public ArrayList<IndividualChartResults> getResults() {
		return getMultipleChartResults().getResults();
	}


	public double[] processData(ArrayList<Double> inputList, boolean isTotalThroughput) {
		if (inputList != null && inputList.size() > 0) {
			if (this instanceof NmonParser) {
					overallResults.setScale_max(90.0);
			} else {
				overallResults.setScale_max(Collections.max(inputList));
			}
			if (overallResults.getOverallScale_max() < overallResults.getScale_max() && !isTotalThroughput) {
				overallResults.setOverallScale_max( overallResults.getScale_max());
			}			
			double scale_factor = 90 / overallResults.getScale_max();
			return ResultParserHelper.scaleInputsData(inputList, scale_factor);
		}
		return null;
	}

	protected abstract void processFile(File file);

	protected abstract void processLine(IndividualChartResults result, String strLine);

	public double[] processMultiLineData(ArrayList<Double> inputList) {
		if (inputList != null && inputList.size() > 0) {			
			double scale_factor = 90 / overallResults.getOverallScale_max();
			return ResultParserHelper.scaleInputsData(inputList, scale_factor);
		}
		return null;
	}

	public String getMultipleChartTitle() {		
		return multipleChartResults.getMultipleChartTitle();
	}

	public void setMultipleYAxisLabel(String label){
		multipleChartResults.setMultipleChartYAxisLabel(label);
	}
	
	public void setMultipleChartTitle(String label){
		multipleChartResults.setMultipleChartTitle(label);
	}
	public String getMultipleYAxisLabel() {
		return multipleChartResults.getMultipleChartYAxisLabel();
	}
	
}
