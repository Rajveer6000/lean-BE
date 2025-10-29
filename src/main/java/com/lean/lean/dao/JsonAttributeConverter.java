package com.lean.lean.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class JsonAttributeConverter implements AttributeConverter<Object, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            // Fallback: store as plain string to avoid data loss
            return String.valueOf(attribute);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return MAPPER.readValue(dbData, Object.class);
        } catch (Exception e) {
            // Fallback: return raw string
            return dbData;
        }
    }
}
