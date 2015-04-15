package com.acmeair.web.dto;

import com.acmeair.entities.FlightSegment;
public class FlightSegmentInfo {

	private String _id;
	private String originPort;
	private String destPort;
	private int miles;
	
	public FlightSegmentInfo() {
		
	}
	public FlightSegmentInfo(FlightSegment flightSegment) {
		this._id = flightSegment.getFlightName();
		this.originPort = flightSegment.getOriginPort();
		this.destPort = flightSegment.getDestPort();
		this.miles = flightSegment.getMiles();
	}
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getOriginPort() {
		return originPort;
	}
	public void setOriginPort(String originPort) {
		this.originPort = originPort;
	}
	public String getDestPort() {
		return destPort;
	}
	public void setDestPort(String destPort) {
		this.destPort = destPort;
	}
	public int getMiles() {
		return miles;
	}
	public void setMiles(int miles) {
		this.miles = miles;
	}	
}
