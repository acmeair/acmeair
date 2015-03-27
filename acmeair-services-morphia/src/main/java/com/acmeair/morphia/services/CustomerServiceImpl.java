package com.acmeair.morphia.services;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.acmeair.entities.Customer;
import com.acmeair.entities.Customer.MemberShipStatus;
import com.acmeair.entities.Customer.PhoneType;
import com.acmeair.entities.CustomerAddress;
import com.acmeair.entities.CustomerSession;
import com.acmeair.morphia.entities.CustomerAddressImpl;
import com.acmeair.morphia.entities.CustomerSessionImpl;
import com.acmeair.morphia.MorphiaConstants;
import com.acmeair.morphia.entities.CustomerImpl;
import com.acmeair.morphia.services.util.MongoConnectionManager;
import com.acmeair.service.DataService;
import com.acmeair.service.CustomerService;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;



@DataService(name=MorphiaConstants.KEY,description=MorphiaConstants.KEY_DESCRIPTION)
public class CustomerServiceImpl implements CustomerService, MorphiaConstants {	
		
	
	protected Datastore datastore;
		
	@Inject
	DefaultKeyGeneratorImpl keyGenerator;
	
	
	@PostConstruct
	public void initialization() {	
		datastore = MongoConnectionManager.getConnectionManager().getDatastore();
	}
	
	@Override
	public Long count() {
		return datastore.find(CustomerImpl.class).countAll();
	}
	
	@Override
	public Long countSessions() {
		return datastore.find(CustomerSessionImpl.class).countAll();
	}
	
	@Override
	public Customer createCustomer(String username, String password,
			MemberShipStatus status, int total_miles, int miles_ytd,
			String phoneNumber, PhoneType phoneNumberType,
			CustomerAddress address) {
	

		Customer customer = new CustomerImpl(username, password, status, total_miles, miles_ytd, address, phoneNumber, phoneNumberType);
		try{
			datastore.save(customer);
			return customer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override 
	public CustomerAddress createAddress (String streetAddress1, String streetAddress2,
			String city, String stateProvince, String country, String postalCode){
		CustomerAddress address = new CustomerAddressImpl(streetAddress1, streetAddress2,
				 city, stateProvince,  country,  postalCode);
		return address;
	}

	@Override
	public Customer updateCustomer(Customer customer) {
		try{
			datastore.save(customer);
			return customer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Customer getCustomer(String username) {
		try{
			
			Query<CustomerImpl> q = datastore.find(CustomerImpl.class).field("_id").equal(username);
			Customer customer = q.get();					
			return customer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Customer getCustomerByUsername(String username) {
		try{
			Query<CustomerImpl> q = datastore.find(CustomerImpl.class).field("_id").equal(username);
			Customer customer = q.get();
			if (customer != null) {
				customer.setPassword(null);
			}			
			return customer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean validateCustomer(String username, String password) {
		boolean validatedCustomer = false;
		Customer customerToValidate = getCustomer(username);		
		if (customerToValidate != null) {
			validatedCustomer = password.equals(customerToValidate.getPassword());
		}		
		return validatedCustomer;
	}

	@Override
	public Customer getCustomerByUsernameAndPassword(String username,
			String password) {
		Customer c = getCustomer(username);
		if (!c.getPassword().equals(password)) {
			return null;
		}
		return c;
	}

	@Override
	public CustomerSession validateSession(String sessionid) {
		try {
			Query<CustomerSessionImpl> q = datastore.find(CustomerSessionImpl.class).field("_id").equal(sessionid);
			
			CustomerSession cSession = q.get();
			if (cSession == null) {
				return null;
			}
			
			Date now = new Date();
			
			if (cSession.getTimeoutTime().before(now)) {
				datastore.delete(cSession);
				return null;
			}
			return cSession;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CustomerSession createSession(String customerId) {
		try {
			String sessionId = keyGenerator.generate().toString();
			Date now = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(now);
			c.add(Calendar.DAY_OF_YEAR, DAYS_TO_ALLOW_SESSION);
			Date expiration = c.getTime();
			CustomerSession cSession = new CustomerSessionImpl(sessionId, customerId, now, expiration);
			datastore.save(cSession);
			return cSession;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void invalidateSession(String sessionid) {
		try {
			Query<CustomerSessionImpl> q = datastore.find(CustomerSessionImpl.class).field("_id").equal(sessionid);
			datastore.delete(q);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
