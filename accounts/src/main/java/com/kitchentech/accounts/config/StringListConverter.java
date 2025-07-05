package com.kitchentech.accounts.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        log.debug("🔄 [StringListConverter] convertToDatabaseColumn: {}", attribute);
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        String result = String.join(",", attribute);
        log.debug("🔄 [StringListConverter] convertToDatabaseColumn result: {}", result);
        return result;
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        log.debug("🔄 [StringListConverter] convertToEntityAttribute: {}", dbData);
        if (dbData == null || dbData.isEmpty()) {
            return List.of();
        }
        List<String> result = Arrays.stream(dbData.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        log.debug("🔄 [StringListConverter] convertToEntityAttribute result: {}", result);
        return result;
    }
} 