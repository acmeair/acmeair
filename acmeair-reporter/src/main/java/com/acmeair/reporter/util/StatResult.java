package com.acmeair.reporter.util;

import java.util.ArrayList;

public class StatResult {
	public double min;
	public double max;
	public double average;
	public int count;
	public double sum;
	public double numberOfResults;
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public double getAverage() {
		return average;
	}
	public void setAverage(double average) {
		this.average = average;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public double getSum() {
		return sum;
	}
	public void setSum(double sum) {
		this.sum = sum;
	}
	public double getNumberOfResults() {
		return numberOfResults;
	}
	public void setNumberOfResults(double numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	
    public static StatResult getStatistics(ArrayList <Double> list){
    	StatResult result = new StatResult();
    	result.average = 0;
    	result.sum = 0;
        if (list.size()>1)
        result.min = list.get(1);
        result.max = 0;
        result.count  = 0;
		for (int i = 0; i< list.size();i++){
			double current = list.get(i).doubleValue();
			if(i > 0 && i < list.size()-1){
				result.sum += current;
				result.count ++;
				result.min = Math.min(result.min, current);
				result.max = Math.max(result.max, current);
			}
		}
		if(result.count > 0 && result.sum > 0)
			result.average = (result.sum/result.count);
		return result;
    }
	
}
