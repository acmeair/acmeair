package com.acmeair.config;

import java.util.ArrayList;
import java.util.Map;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


import com.acmeair.service.ServiceLocator;


@Path("/config")
public class AcmeAirConfiguration {
    
	@Inject
	BeanManager beanManager;
	

    public AcmeAirConfiguration() {
        super();
    }

    
	@GET
	@Path("/info")
	@Produces("application/json")
	public ArrayList<ServiceData> getConfigInfo(
			@PathParam("number") String number,
			@FormParam("userid") String userid) {
		try {	
			ArrayList<ServiceData> list = new ArrayList<ServiceData>();
			Map<String, String> services =  ServiceLocator.instance().getServices();
			
			for (Map.Entry<String, String> entry : services.entrySet()){
				ServiceData data = new ServiceData();
				data.name = entry.getKey();
				data.description = entry.getValue();
				list.add(data);
			}
			
			return list;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	
	class ServiceData {
		public String name = "";
		public String description = "";
	}
}
