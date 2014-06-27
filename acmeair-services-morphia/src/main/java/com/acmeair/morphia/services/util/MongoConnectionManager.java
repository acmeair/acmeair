package com.acmeair.morphia.services.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.acmeair.morphia.MorphiaConstants;
import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongoConnectionManager implements MorphiaConstants{

	private static AtomicReference<MongoConnectionManager> connectionManager = new AtomicReference<MongoConnectionManager>();
	
	private final static Logger logger = Logger.getLogger(MongoConnectionManager.class.getName());
	protected DB db;
	protected MongoClient mongo;
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
	        	host = (String) new InitialContext().lookup(HOSTNAME);
	        	port = (String) new InitialContext().lookup(PORT);
	        	database = (String) new InitialContext().lookup(DATABASE);
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
			datastore = morphia.createDatastore(db.getMongo(), db.getName());
		}
	}
	
	public DB getDB(){
		return db;
	}
	
	public Datastore getDatastore(){
		return datastore;
	}
	
	public void close(){	
		mongo.close();
	}
}
