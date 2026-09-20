package com.imageprocessor;

import com.imageprocessor.model.FilterPreset;
import com.imageprocessor.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilTest {

    @Test
    public void testSerializePreset() throws IOException {
        FilterPreset preset = new FilterPreset();
        preset.setName("Test Preset");
        preset.setBrightness(25.0);
        preset.setContrast(-10.0);
        preset.setSepia(true);
        preset.setGrayscale(false);

        String json = JsonUtil.serialize(preset);
        assertNotNull(json);
        assertTrue(json.contains("Test Preset"));
        assertTrue(json.contains("25.0"));
        assertTrue(json.contains("-10.0"));
        assertTrue(json.contains("true"));
    }

    @Test
    public void testDeserializePreset() throws IOException {
        String json = "{\"name\":\"Test Preset\",\"brightness\":25.0,\"contrast\":-10.0,\"sepia\":true,\"grayscale\":false}";
        FilterPreset preset = JsonUtil.deserialize(json, FilterPreset.class);
        
        assertNotNull(preset);
        assertEquals("Test Preset", preset.getName());
        assertEquals(25.0, preset.getBrightness());
        assertEquals(-10.0, preset.getContrast());
        assertTrue(preset.isSepia());
        assertFalse(preset.isGrayscale());
    }

    @Test
    public void testExportAndImportPreset(@TempDir Path tempDir) throws IOException {
        FilterPreset preset = new FilterPreset();
        preset.setName("Test Preset");
        preset.setBrightness(25.0);
        preset.setContrast(-10.0);
        preset.setSepia(true);
        preset.setGrayscale(false);

        File tempFile = tempDir.resolve("preset.json").toFile();
        JsonUtil.exportPreset(preset, tempFile);
        
        assertTrue(tempFile.exists());

        FilterPreset imported = JsonUtil.importPreset(tempFile);
        assertNotNull(imported);
        assertEquals(preset.getName(), imported.getName());
        assertEquals(preset.getBrightness(), imported.getBrightness());
        assertEquals(preset.getContrast(), imported.getContrast());
        assertEquals(preset.isSepia(), imported.isSepia());
        assertEquals(preset.isGrayscale(), imported.isGrayscale());
    }

    @Test
    public void testDefaultPresetSerialization() throws IOException {
        FilterPreset preset = new FilterPreset();
        
        String json = JsonUtil.serialize(preset);
        FilterPreset deserialized = JsonUtil.deserialize(json, FilterPreset.class);
        
        assertTrue(deserialized.isDefault());
    }
}
