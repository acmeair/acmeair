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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.acmeair.service.DataService;
import com.acmeair.service.TransactionService;
import com.acmeair.wxs.WXSConstants;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridManager;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.config.BackingMapConfiguration;
import com.ibm.websphere.objectgrid.config.ObjectGridConfigFactory;
import com.ibm.websphere.objectgrid.config.ObjectGridConfiguration;
import com.ibm.websphere.objectgrid.config.Plugin;
import com.ibm.websphere.objectgrid.config.PluginType;
import com.ibm.websphere.objectgrid.security.config.ClientSecurityConfiguration;
import com.ibm.websphere.objectgrid.security.config.ClientSecurityConfigurationFactory;
import com.ibm.websphere.objectgrid.security.plugins.CredentialGenerator;
import com.ibm.websphere.objectgrid.security.plugins.builtins.UserPasswordCredentialGenerator;
import com.ibm.websphere.objectgrid.spring.SpringLocalTxManager;

@DataService(name=WXSConstants.KEY,description=WXSConstants.KEY_DESCRIPTION)
public class WXSSessionManager implements TransactionService, WXSConstants{
	
		private static final String GRID_CONNECT_LOOKUP_KEY = "com.acmeair.service.wxs.gridConnect";
		private static final String GRID_NAME_LOOKUP_KEY = "com.acmeair.service.wxs.gridName";
		private static final String GRID_DISABLE_NEAR_CACHE_NAME_LOOKUP_KEY = "com.acmeair.service.wxs.disableNearCacheName";
		private static final String GRID_PARTITION_FIELD_NAME_LOOKUP_KEY = "com.acmeair.service.wxs.partitionFieldName";
		private static final Logger logger = Logger.getLogger(WXSSessionManager.class.getName());
		private static final String SPLIT_COMMA = "\\s*,\\s*";
		private static final String SPLIT_COLON = "\\s*:\\s*";		
	
		private String gridConnectString;
		private String gridUsername = null;
		private String gridPassword = null;
		private String gridName = "Grid";
		private boolean integrateWithWASTransactions = false;
		private String disableNearCacheNameString;
		private String[] disableNearCacheNames = null;
		private String partitionFieldNameString;
		private HashMap<String, String> partitionFieldNames = null; // For now to make it simple to only support one partition field
		private SpringLocalTxManager txManager;
        private String mapSuffix = "";
		private AtomicReference<ObjectGrid> sharedGrid = new AtomicReference<ObjectGrid>();
		private static AtomicReference<WXSSessionManager> connectionManager = new AtomicReference<WXSSessionManager>();
		
		
		public static WXSSessionManager getSessionManager() {
			if (connectionManager.get() == null) {
				synchronized (connectionManager) {
					if (connectionManager.get() == null) {
						connectionManager.set(new WXSSessionManager());
					}
				}
			}
			return connectionManager.get();
		}	
		
		
		private WXSSessionManager(){
			ObjectGrid og = null;
			
			try {
				InitialContext ic = new InitialContext();			
				og = (ObjectGrid) ic.lookup(JNDI_NAME);
				
			} catch (NamingException e) {
				logger.warning("Unable to look up the ObjectGrid reference " + e.getMessage());
			}
			if(og != null) {
				sharedGrid.set(og);
			} else {				
				initialization();				
			}
			
		}
		
		
		private void initialization()  {		
			
			
			String vcapJSONString = System.getenv("VCAP_SERVICES");
			if (vcapJSONString != null) {
				logger.info("Reading VCAP_SERVICES");
				Object jsonObject = JSONValue.parse(vcapJSONString);
				logger.info("jsonObject = " + ((JSONObject)jsonObject).toJSONString());
				JSONObject json = (JSONObject)jsonObject;
				String key;
				for (Object k: json.keySet())
				{
					key = (String ) k;
					if (key.startsWith("ElasticCaching")||key.startsWith("DataCache"))
					{
						JSONArray elasticCachingServiceArray = (JSONArray)json.get(key);
						JSONObject elasticCachingService = (JSONObject)elasticCachingServiceArray.get(0); 
						JSONObject credentials = (JSONObject)elasticCachingService.get("credentials");
						String username = (String)credentials.get("username");
						setGridUsername(username);
						String password = (String)credentials.get("password");
						setGridPassword(password);
						String gridName = (String)credentials.get("gridName");
						String catalogEndPoint = (String)credentials.get("catalogEndPoint");
						logger.info("username = " + username + "; password = " + password + "; gridName =  " + gridName + "; catalogEndpoint = " + catalogEndPoint);
						setGridConnectString(catalogEndPoint);
						setGridName(gridName);
						break;
					}
				}
				setMapSuffix(".NONE.O");
			} else {
				logger.info("Creating the WXS Client connection. Looking up host and port information" );
				gridName = lookup(GRID_NAME_LOOKUP_KEY);
				if(gridName == null){
					gridName = "AcmeGrid";
				}

				gridConnectString = lookup(GRID_CONNECT_LOOKUP_KEY);
				if(gridConnectString == null){							
					gridConnectString = "127.0.0.1:2809";
					logger.info("Using default grid connection setting of " + gridConnectString);
				}

				setDisableNearCacheNameString(lookup(GRID_DISABLE_NEAR_CACHE_NAME_LOOKUP_KEY));
				setPartitionFieldNameString(lookup(GRID_PARTITION_FIELD_NAME_LOOKUP_KEY));

			}
			
			
			if(getDisableNearCacheNameString() == null){
				setDisableNearCacheNameString("Flight,FlightSegment,AirportCodeMapping,CustomerSession,Booking,Customer");
				logger.info("Using default disableNearCacheNameString value of " + disableNearCacheNameString);
			}
			
			if(getPartitionFieldNameString() == null){
				setPartitionFieldNameString("Flight:pk.flightSegmentId,FlightSegment:originPort,Booking:pk.customerId");
				logger.info("Using default partitionFieldNameString value of " + partitionFieldNameString);
			}
			
			if (!integrateWithWASTransactions && txManager!=null) // Using Spring TX if WAS TX is not enabled
			{
				logger.info("Session will be created from SpringLocalTxManager w/ tx support.");
			}else
			{
				txManager=null;
				logger.info("Session will be created from ObjectGrid directly w/o tx support.");
			}
			
			
			try {
				prepareForTransaction();
			} catch (ObjectGridException e) {
				e.printStackTrace();
			} 
		}	
		
		private String lookup (String key){
			String value = null;
			String lookup = key.replace('.', '/');
			javax.naming.Context context = null;
			javax.naming.Context envContext = null;
			try {
				context = new javax.naming.InitialContext();
				envContext = (javax.naming.Context) context.lookup("java:comp/env");
				if (envContext != null)
					value = (String) envContext.lookup(lookup);
			} catch (NamingException e) {  }
			
			if (value != null) {
				logger.info("JNDI Found " + lookup + " : " + value);
			}
			else if (context != null) {
				try {
					value = (String) context.lookup(lookup);
					if (value != null)
						logger.info("JNDI Found " +lookup + " : " + value);
				} catch (NamingException e) {	}
			}

			if (value == null) {
				value = System.getProperty(key);
				if (value != null)
					logger.info("Found " + key + " in jvm property : " + value);
				else {
					value = System.getenv(key);
					if (value != null)
						logger.info("Found "+key+" in environment property : " + value);
				}
			}
			return value;
		}
		
	    /**
	     * Connect to a remote ObjectGrid
	     * @param cep the catalog server end points in the form: <host>:<port>
	     * @param gridName the name of the ObjectGrid to connect to that is managed by the Catalog Service
	     * @return a client ObjectGrid connection.
	     */
		private ObjectGrid connectClient(String cep, String gridName, boolean integrateWithWASTransactions,String[] disableNearCacheNames) {
			try {
				ObjectGrid gridToReturn = sharedGrid.get();
				if (gridToReturn == null) {
					synchronized(sharedGrid) {
						if (sharedGrid.get() == null) {
							ObjectGridManager ogm = ObjectGridManagerFactory.getObjectGridManager();
							ObjectGridConfiguration ogConfig = ObjectGridConfigFactory.createObjectGridConfiguration(gridName);
							if (integrateWithWASTransactions) // Using WAS Transactions as Highest Priority
							{

								Plugin trans = ObjectGridConfigFactory.createPlugin(PluginType.TRANSACTION_CALLBACK,
										"com.ibm.websphere.objectgrid.plugins.builtins.WebSphereTransactionCallback");
								ogConfig.addPlugin(trans);
							}
							if (disableNearCacheNames!=null) {
								String mapNames[] = disableNearCacheNames;
								for (String mName : mapNames) {									
									BackingMapConfiguration bmc = ObjectGridConfigFactory.createBackingMapConfiguration(mName);
									bmc.setNearCacheEnabled(false);
									ogConfig.addBackingMapConfiguration(bmc);
								}
							}					
													
							ClientClusterContext ccc = null;
							if (gridUsername != null) {
								ClientSecurityConfiguration clientSC = ClientSecurityConfigurationFactory.getClientSecurityConfiguration();
								clientSC.setSecurityEnabled(true);
								CredentialGenerator credGen = new UserPasswordCredentialGenerator(gridUsername, gridPassword);
								clientSC.setCredentialGenerator(credGen);
								ccc = ogm.connect(cep, clientSC, null);
							}
							else {
								ccc = ogm.connect(cep, null, null);
							}

							ObjectGrid grid = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, gridName, ogConfig);
							sharedGrid.compareAndSet(null, grid);
							gridToReturn = grid;
							logger.info("Create instance of Grid: " + gridToReturn);
						}else{
							gridToReturn = sharedGrid.get(); 
						}
					}
				}
				return gridToReturn;
			} catch (Exception e) {
				throw new ObjectGridRuntimeException("Unable to connect to catalog server at endpoints:" + cep,	e);
			}
		}
		public String getMapSuffix(){
			return mapSuffix;
		}
		
		public void setMapSuffix(String suffix){
			this.mapSuffix = suffix;
		}
		
		public String getGridConnectString() {
			return gridConnectString;
		}
		public void setGridConnectString(String gridConnectString) {
			this.gridConnectString = gridConnectString;
		}
		public String getGridName() {
			return gridName;
		}
		public void setGridName(String gridName) {
			this.gridName = gridName;
		}
		public String getGridUsername() {
			return gridUsername;
		}

		public void setGridUsername(String gridUsername) {
			this.gridUsername = gridUsername;
		}

		public String getGridPassword() {
			return gridPassword;
		}

		public void setGridPassword(String gridPassword) {
			this.gridPassword = gridPassword;
		}

		public boolean isIntegrateWithWASTransactions() {
			return integrateWithWASTransactions;
		}
		public void setIntegrateWithWASTransactions(boolean integrateWithWASTransactions) {
			this.integrateWithWASTransactions = integrateWithWASTransactions;
		}

		public String getDisableNearCacheNameString() {
			return disableNearCacheNameString;
		}
		public void setDisableNearCacheNameString(String disableNearCacheNameString) {
			this.disableNearCacheNameString = disableNearCacheNameString;
			if (disableNearCacheNameString ==null || disableNearCacheNameString.length()==0)
				disableNearCacheNames =null;
			else
				disableNearCacheNames = disableNearCacheNameString.split(SPLIT_COMMA);
		}
		
		public String getPartitionFieldNameString() {
			return partitionFieldNameString;
		}
		public void setPartitionFieldNameString(String partitionFieldNameString) {
			this.partitionFieldNameString = partitionFieldNameString;
			// In the form of <MapName>:<PartitionFieldName>,<MapName>:<PartitionFieldName>
			if (partitionFieldNameString ==null || partitionFieldNameString.length()==0)
				partitionFieldNames =null;
			else
			{
				String[] maps = partitionFieldNameString.split(SPLIT_COMMA);
				partitionFieldNames = new HashMap<String, String>();
				String[] mapDef;
				for (int i=0; i<maps.length; i++)
				{
					mapDef = maps[i].split(SPLIT_COLON);
					partitionFieldNames.put(mapDef[0], mapDef[1]);
				}
			}
			
		}
		public String getPartitionFieldName(String mapName) {
			if (partitionFieldNames == null)
				return null;
			return partitionFieldNames.get(mapName);
		}
		
		public SpringLocalTxManager getTxManager() {
			return txManager;
		}
		
		public void setTxManager(SpringLocalTxManager txManager) {
			logger.finer("txManager:"+txManager);
			this.txManager = txManager;
		}
		
		
		// This method needs to be called by the client from its thread before triggering a service with @Transactional annotation
		public void prepareForTransaction() throws ObjectGridException
		{
			ObjectGrid grid = this.getObjectGrid();
			if (txManager!=null)
				txManager.setObjectGridForThread(grid);
		}
		
		// Helper function
		public ObjectGrid getObjectGrid() throws ObjectGridException {
			ObjectGrid grid = connectClient(this.gridConnectString, this.gridName, this.integrateWithWASTransactions, this.disableNearCacheNames);
			return grid;
		}

		public Session getObjectGridSession() throws ObjectGridException {
			Session result;
			ObjectGrid grid = getObjectGrid();
			if (txManager!=null)
				result= txManager.getSession();
			else
				result = grid.getSession();
			
//			this.log.debug("Got session:"+ result);
			return result;
		}
		
		public BackingMap getBackingMap(String mapName)throws ObjectGridException
		{
			return this.getObjectGrid().getMap(mapName);			
		}
		
		
}
