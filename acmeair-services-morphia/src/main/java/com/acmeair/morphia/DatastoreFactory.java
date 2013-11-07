package com.acmeair.morphia;

import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.acmeair.entities.Booking;
import com.acmeair.entities.Flight;
import com.acmeair.entities.FlightSegment;
import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;

public class DatastoreFactory {

	private static String mongourl = null;
	
	static {
		String vcapJSONString = System.getenv("VCAP_SERVICES");
		if (vcapJSONString != null) {
			System.out.println("Reading VCAP_SERVICES");
			Object jsonObject = JSONValue.parse(vcapJSONString);
			JSONObject json = (JSONObject)jsonObject;
			System.out.println("jsonObject = " + json.toJSONString());
			for (Object key: json.keySet())
			{
				if (((String)key).contains("mongo"))
				{
					System.out.println("Found mongo service:" +key);
					JSONArray mongoServiceArray = (JSONArray)json.get(key);
					JSONObject mongoService = (JSONObject) mongoServiceArray.get(0);
					JSONObject credentials = (JSONObject)mongoService.get("credentials");
					mongourl = (String)credentials.get("url");
					if (mongourl==null)
						mongourl= (String)credentials.get("uri");
					System.out.println("service url = " + mongourl);
					break;
				}
			}
		}


	}

	public static Datastore getDatastore(Datastore ds)
	{
		Datastore result =ds;
		
		if (mongourl!=null)
		{
			try{
				Properties prop = new Properties();
				prop.load(DatastoreFactory.class.getResource("/acmeair-mongo.properties").openStream());
				boolean fsync = new Boolean(prop.getProperty("mongo.fsync"));
				int w = new Integer(prop.getProperty("mongo.w"));
				int connectionsPerHost = new Integer(prop.getProperty("mongo.connectionsPerHost"));
				int threadsAllowedToBlockForConnectionMultiplier = new Integer(prop.getProperty("mongo.threadsAllowedToBlockForConnectionMultiplier"));
				
				// To match the local options
				MongoClientOptions.Builder builder = new MongoClientOptions.Builder()
					.writeConcern(new WriteConcern(w, 0, fsync))
					.connectionsPerHost(connectionsPerHost)
					.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
			
				MongoClientURI mongoURI = new MongoClientURI(mongourl, builder);
				MongoClient mongo = new MongoClient(mongoURI);
				Morphia morphia = new Morphia();
				result = morphia.createDatastore( mongo ,mongoURI.getDatabase());
				System.out.println("create mongo datastore with options:"+result.getMongo().getMongoOptions());
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
    	// The converter is added for handing JDK 7 issue
		result.getMapper().getConverters().addConverter(new BigDecimalConverter());
		result.getMapper().getConverters().addConverter(new BigIntegerConverter());
    	
		// Enable index
		result.ensureIndex(Booking.class, "pkey.customerId");
		result.ensureIndex(Flight.class, "pkey.flightSegmentId,scheduledDepartureTime");
		result.ensureIndex(FlightSegment.class, "originPort,destPort");

    	return result;
	}
}
