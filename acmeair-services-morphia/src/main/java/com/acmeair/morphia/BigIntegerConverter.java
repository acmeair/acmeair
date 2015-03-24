package com.acmeair.morphia;

import java.math.BigInteger;

import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.MappingException;

public class BigIntegerConverter extends TypeConverter implements SimpleValueConverter{

    public BigIntegerConverter() {
        super(BigInteger.class);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        return value.toString();
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) throws MappingException {
        if (fromDBObject == null) return null;

        return new BigInteger(fromDBObject.toString());
    }
}
