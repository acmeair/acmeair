package com.acmeair.morphia.services;

import com.acmeair.service.KeyGenerator;


public class DefaultKeyGeneratorImpl implements KeyGenerator {

	@Override
	public Object generate() {
		
		return java.util.UUID.randomUUID().toString();
	}

}
