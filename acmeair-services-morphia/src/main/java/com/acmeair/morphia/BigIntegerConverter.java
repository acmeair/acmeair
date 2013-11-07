package com.acmeair.morphia;

import java.math.BigInteger;

import com.github.jmkgreen.morphia.converters.SimpleValueConverter;
import com.github.jmkgreen.morphia.converters.TypeConverter;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;

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
