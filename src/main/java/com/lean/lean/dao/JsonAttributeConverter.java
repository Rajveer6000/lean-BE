package com.lean.lean.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

@Converter(autoApply = false)
public class JsonAttributeConverter implements AttributeConverter<Object, PGobject> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public PGobject convertToDatabaseColumn(Object attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(MAPPER.writeValueAsString(attribute));
            return jsonObject;
        } catch (Exception e) {
            try {
                PGobject fallback = new PGobject();
                fallback.setType("jsonb");
                fallback.setValue(String.valueOf(attribute));
                return fallback;
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    @Override
    public Object convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null) {
            return null;
        }
        try {
            return MAPPER.readValue(dbData.getValue(), Object.class);
        } catch (Exception e) {
            return dbData.getValue();
        }
    }
}
