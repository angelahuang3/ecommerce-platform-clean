package com.example.orderservice.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter
public class MapToJsonConverter implements AttributeConverter<Map<String,Integer>, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> attribute) {
        try { return attribute == null ? null : MAPPER.writeValueAsString(attribute); }
        catch (Exception e) { throw new IllegalStateException("Convert Map to JSON fail", e); }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        try { return dbData == null ? null : MAPPER.readValue(dbData, new TypeReference<>(){}); }
        catch (Exception e) { throw new IllegalStateException("Convert JSON to Map fail", e); }
    }
}
