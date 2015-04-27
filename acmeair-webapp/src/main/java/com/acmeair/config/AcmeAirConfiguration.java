package com.acmeair.config;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.acmeair.service.BookingService;
import com.acmeair.service.CustomerService;
import com.acmeair.service.FlightService;
import com.acmeair.service.ServiceLocator;


@Path("/config")
public class AcmeAirConfiguration {
    
	@Inject
	BeanManager beanManager;
	Logger logger = Logger.getLogger(AcmeAirConfiguration.class.getName());

	private BookingService bs = ServiceLocator.instance().getService(BookingService.class);
	private CustomerService customerService = ServiceLocator.instance().getService(CustomerService.class);
	private FlightService flightService = ServiceLocator.instance().getService(FlightService.class);

	
    public AcmeAirConfiguration() {
        super();
    }

	@PostConstruct
	private void initialization()  {		
		if(beanManager == null){
			logger.info("Attempting to look up BeanManager through JNDI at java:comp/BeanManager");
			try {
				beanManager = (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
			} catch (NamingException e) {
				logger.severe("BeanManager not found at java:comp/BeanManager");
			}
		}
		
		if(beanManager == null){
			logger.info("Attempting to look up BeanManager through JNDI at java:comp/env/BeanManager");
			try {
				beanManager = (BeanManager) new InitialContext().lookup("java:comp/env/BeanManager");
			} catch (NamingException e) {
				logger.severe("BeanManager not found at java:comp/env/BeanManager ");
			}
		}
	}
    
    
	@GET
	@Path("/dataServices")
	@Produces("application/json")
	public ArrayList<ServiceData> getDataServiceInfo() {
		try {	
			ArrayList<ServiceData> list = new ArrayList<ServiceData>();
			Map<String, String> services =  ServiceLocator.instance().getServices();
			logger.fine("Get data service configuration info");
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

	
	@GET
	@Path("/activeDataService")
	@Produces("application/json")
	public Response getActiveDataServiceInfo() {
		try {		
			logger.fine("Get active Data Service info");
			return  Response.ok(ServiceLocator.instance().getServiceType()).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok("Unknown").build();
		}
	}
	
	@GET
	@Path("/runtime")
	@Produces("application/json")
	public ArrayList<ServiceData> getRuntimeInfo() {
		try {
			logger.fine("Getting Runtime info");
			ArrayList<ServiceData> list = new ArrayList<ServiceData>();
			ServiceData data = new ServiceData();
			data.name = "Runtime";
			data.description = "Java";			
			list.add(data);
			
			data = new ServiceData();
			data.name = "Version";
			data.description = System.getProperty("java.version");			
			list.add(data);
			
			data = new ServiceData();
			data.name = "Vendor";
			data.description = System.getProperty("java.vendor");			
			list.add(data);
			
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
	
	@GET
	@Path("/countBookings")
	@Produces("application/json")
	public Response countBookings() {
		try {
			Long count = bs.count();			
			return Response.ok(count).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(-1).build();
		}
	}
	
	@GET
	@Path("/countCustomers")
	@Produces("application/json")
	public Response countCustomer() {
		try {
			Long customerCount = customerService.count();
			
			return Response.ok(customerCount).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(-1).build();
		}
	}
	
	
	@GET
	@Path("/countSessions")
	@Produces("application/json")
	public Response countCustomerSessions() {
		try {
			Long customerCount = customerService.countSessions();
			
			return Response.ok(customerCount).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(-1).build();
		}
	}
	
	
	@GET
	@Path("/countFlights")
	@Produces("application/json")
	public Response countFlights() {
		try {
			Long count = flightService.countFlights();			
			return Response.ok(count).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(-1).build();
		}
	}
	
	@GET
	@Path("/countFlightSegments")
	@Produces("application/json")
	public Response countFlightSegments() {
		try {
			Long count = flightService.countFlightSegments();			
			return Response.ok(count).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(-1).build();
		}
	}
	
	@GET
	@Path("/countAirports")
	@Produces("application/json")
	public Response countAirports() {
		try {			
			Long count = flightService.countAirports();	
			return Response.ok(count).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(-1).build();
		}
	}
	
}
