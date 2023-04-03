package com.springboot3.boilerplate.app.converter;

import com.springboot3.boilerplate.app.enums.ContactType;
import org.springframework.core.convert.converter.Converter;

public class StringToDeviceTypeConverter implements Converter<String, ContactType> {
    @Override
    public ContactType convert(String source) {
        try {
            return ContactType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
