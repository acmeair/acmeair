package com.acmeair.web;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;

import com.acmeair.config.AcmeAirConfiguration;
import com.acmeair.loader.Loader;

@ApplicationPath("/rest/api")
public class AcmeAirApp extends Application {
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(BookingsREST.class, CustomerREST.class, FlightsREST.class, LoginREST.class, Loader.class, AcmeAirConfiguration.class));
    }
}
