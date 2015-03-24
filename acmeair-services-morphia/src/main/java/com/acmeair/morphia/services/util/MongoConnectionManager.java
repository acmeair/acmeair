package com.acmeair.morphia.services.util;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.acmeair.morphia.BigDecimalConverter;
import com.acmeair.morphia.MorphiaConstants;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongoConnectionManager implements MorphiaConstants{

	private static AtomicReference<MongoConnectionManager> connectionManager = new AtomicReference<MongoConnectionManager>();
	
	private final static Logger logger = Logger.getLogger(MongoConnectionManager.class.getName());
	protected DB db;
	protected Datastore datastore;
	
	public static MongoConnectionManager getConnectionManager() {
		if (connectionManager.get() == null) {
			synchronized (connectionManager) {
				if (connectionManager.get() == null) {
					connectionManager.set(new MongoConnectionManager());
				}
			}
		}
		return connectionManager.get();
	}
	
	
	private MongoConnectionManager (){
		if(db == null){
	        try {
	        	db = (DB) new InitialContext().lookup(JNDI_NAME);
			} catch (NamingException e) {
				logger.severe("Caught NamingException : " + e.getMessage() );
			}	        
		}
		
		if(db == null){
			String host; 
        	String port;
        	String database;
			logger.fine("Creating the MongoDB Client connection. Looking up host and port information " );
	        try {	        	
	        	host = (String) new InitialContext().lookup("java:comp/env/" + HOSTNAME);
	        	port = (String) new InitialContext().lookup("java:comp/env/" + PORT);
	        	database = (String) new InitialContext().lookup("java:comp/env/" + DATABASE);
				ServerAddress server = new ServerAddress(host, Integer.parseInt(port));
				MongoClient mongo = new MongoClient(server);
				db = mongo.getDB(database);
			} catch (NamingException e) {
				logger.severe("Caught NamingException : " + e.getMessage() );			
			} catch (Exception e) {
				logger.severe("Caught Exception : " + e.getMessage() );
			}
		}
		
		if(db == null){
			logger.severe("Unable to retreive reference to database, please check the server logs.");
		} else {
			Morphia morphia = new Morphia();
			try {
				morphia.getMapper().getConverters().addConverter(new BigDecimalConverter());
				datastore = morphia.createDatastore(new MongoClient(db.getMongo().getConnectPoint()), db.getName());
			} catch (UnknownHostException e) {
				logger.severe("Caught Exception : " + e.getMessage() );				
			}
			
		}
	}
	
	public DB getDB(){
		return db;
	}
	
	public Datastore getDatastore(){
		return datastore;
	}
	
	public String getDriverVersion(){
		return datastore.getMongo().getVersion();
	}
	
	public String getMongoVersion(){
		return datastore.getDB().command("buildInfo").getString("version");
	}
}
