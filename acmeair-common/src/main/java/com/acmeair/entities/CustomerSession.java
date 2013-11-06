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
package com.acmeair.entities;

import java.io.Serializable;
import java.util.*;

public class CustomerSession implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String _id;
	private String customerid;
	private Date lastAccessedTime;
	private Date timeoutTime;
	
	public CustomerSession() {
	}

	public CustomerSession(String id, String customerid, Date lastAccessedTime,	Date timeoutTime) {
		this._id= id;
		this.customerid = customerid;
		this.lastAccessedTime = lastAccessedTime;
		this.timeoutTime = timeoutTime;
	}
	

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public String getCustomerid() {
		return customerid;
	}

	public void setCustomerid(String customerid) {
		this.customerid = customerid;
	}

	public Date getLastAccessedTime() {
		return lastAccessedTime;
	}

	public void setLastAccessedTime(Date lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	public Date getTimeoutTime() {
		return timeoutTime;
	}

	public void setTimeoutTime(Date timeoutTime) {
		this.timeoutTime = timeoutTime;
	}

	@Override
	public String toString() {
		return "CustomerSession [id=" + _id + ", customerid=" + customerid
				+ ", lastAccessedTime=" + lastAccessedTime + ", timeoutTime="
				+ timeoutTime + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomerSession other = (CustomerSession) obj;
		if (customerid == null) {
			if (other.customerid != null)
				return false;
		} else if (!customerid.equals(other.customerid))
			return false;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (lastAccessedTime == null) {
			if (other.lastAccessedTime != null)
				return false;
		} else if (!lastAccessedTime.equals(other.lastAccessedTime))
			return false;
		if (timeoutTime == null) {
			if (other.timeoutTime != null)
				return false;
		} else if (!timeoutTime.equals(other.timeoutTime))
			return false;
		return true;
	}


	
}