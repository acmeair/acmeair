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
package com.acmeair.wxs.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.SerializedKey;

public class MapPutAllAgent implements MapGridAgent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(MapPutAllAgent.class);
	
	private HashMap<Object, HashMap<Object,Object>>objectsToSave = null  ;

	public HashMap<Object, HashMap<Object,Object>> getObjectsToSave() {
		return objectsToSave;
	}

	public void setObjectsToSave(HashMap<Object,HashMap<Object,Object>> objectsToSave) {
		this.objectsToSave = objectsToSave;
	}

	@Override
	public Object process(Session arg0, ObjectMap arg1, Object arg2) {
		// The key is the partition key, can be either the PK or when partition field is defined the partition field value
		try{
			Object key;
			// I need to find the real key as the hashmap is using the real key...
     		if( arg2 instanceof SerializedKey )
    		     key = ((SerializedKey)arg2).getObject();
    		else 
    		     key = arg2;     		
			
			HashMap<Object, Object> objectsForThePartition =  this.objectsToSave.get(key);
			
			if (objectsForThePartition==null)
				log.info("ERROR!!! Can not get the objects for partiton key:"+arg2);
			else
			{
				Entry<Object, Object> entry;
				Object value;
				for (Iterator<Map.Entry<Object, Object>> itr = objectsForThePartition.entrySet().iterator(); itr.hasNext();)
				{
					entry = itr.next();
					key = entry.getKey();
					value = entry.getValue();
					
					log.debug("Save using agent:"+key+",value:"+value);
					arg1.upsert(key, value);
				}
			}
		}catch (ObjectGridException e)
		{
			log.info("Getting exception:"+e);
		}
		return arg2;	
	}

	@Override
	public Map<Object, Object> processAllEntries(Session arg0, ObjectMap arg1) {
		return null; 
	}

}