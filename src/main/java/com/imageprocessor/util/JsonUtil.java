package com.imageprocessor.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.imageprocessor.model.FilterPreset;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for JSON operations using Jackson.
 */
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Exports a FilterPreset to a JSON file.
     */
    public static void exportPreset(FilterPreset preset, File file) throws IOException {
        objectMapper.writeValue(file, preset);
    }

    /**
     * Imports a FilterPreset from a JSON file.
     */
    public static FilterPreset importPreset(File file) throws IOException {
        return objectMapper.readValue(file, FilterPreset.class);
    }

    /**
     * Serializes an object to a JSON string.
     */
    public static String serialize(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Deserializes a JSON string to an object of the specified class.
     */
    public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
}
