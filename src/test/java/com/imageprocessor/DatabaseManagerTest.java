package com.imageprocessor;

import com.imageprocessor.database.DatabaseManager;
import com.imageprocessor.model.EditLog;
import com.imageprocessor.model.FilterPreset;
import com.imageprocessor.model.ImageModel;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseManagerTest {

    @BeforeAll
    public static void setUp() {
        DatabaseManager.getInstance().initialize();
    }

    @AfterAll
    public static void tearDown() {
        DatabaseManager.getInstance().close();
        File dataDir = new File("data");
        if (dataDir.exists()) {
            File[] files = dataDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            dataDir.delete();
        }
    }

    @Test
    @Order(1)
    public void testInitializeCreatesDatabase() {
        assertDoesNotThrow(() -> DatabaseManager.getInstance().initialize());
    }

    @Test
    @Order(2)
    public void testSaveAndRetrieveImage() {
        ImageModel img = new ImageModel();
        img.setFilePath("C:/test/image1.png");
        img.setFileName("image1.png");
        img.setWidth(800);
        img.setHeight(600);
        img.setFormat("PNG");
        img.setFileSizeBytes(12345);
        img.setCreatedAt(LocalDateTime.now());
        img.setUpdatedAt(LocalDateTime.now());

        long id = DatabaseManager.getInstance().saveImage(img);
        assertTrue(id > 0);

        ImageModel retrieved = DatabaseManager.getInstance().getImageByPath("C:/test/image1.png");
        assertNotNull(retrieved);
        assertEquals("image1.png", retrieved.getFileName());
        assertEquals(800, retrieved.getWidth());
        assertEquals(600, retrieved.getHeight());
        assertEquals("PNG", retrieved.getFormat());
        assertEquals(12345, retrieved.getFileSizeBytes());
    }

    @Test
    @Order(3)
    public void testGetAllImages() {
        ImageModel img2 = new ImageModel();
        img2.setFilePath("C:/test/image2.png");
        img2.setFileName("image2.png");
        DatabaseManager.getInstance().saveImage(img2);

        ImageModel img3 = new ImageModel();
        img3.setFilePath("C:/test/image3.png");
        img3.setFileName("image3.png");
        DatabaseManager.getInstance().saveImage(img3);

        List<ImageModel> images = DatabaseManager.getInstance().getAllImages();
        assertTrue(images.size() >= 3);
    }

    @Test
    @Order(4)
    public void testLogEdit() {
        ImageModel img = DatabaseManager.getInstance().getImageByPath("C:/test/image1.png");
        assertNotNull(img);

        DatabaseManager.getInstance().logEdit(img.getId(), "Brightness", "value=50");
        List<EditLog> logs = DatabaseManager.getInstance().getLogsForImage(img.getId());

        assertFalse(logs.isEmpty());
        boolean found = false;
        for (EditLog log : logs) {
            if ("Brightness".equals(log.getOperationType())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    @Order(5)
    public void testDeleteImage() {
        ImageModel img = DatabaseManager.getInstance().getImageByPath("C:/test/image1.png");
        assertNotNull(img);

        boolean deleted = DatabaseManager.getInstance().deleteImage(img.getId());
        assertTrue(deleted);

        ImageModel retrieved = DatabaseManager.getInstance().getImageByPath("C:/test/image1.png");
        assertNull(retrieved);
    }

    @Test
    @Order(6)
    public void testSaveAndRetrievePreset() {
        FilterPreset preset = new FilterPreset();
        preset.setName("Test Preset");
        preset.setBrightness(25.0);

        DatabaseManager.getInstance().savePreset(preset);

        Map<String, String> presets = DatabaseManager.getInstance().getAllPresets();
        assertTrue(presets.containsKey("Test Preset"));

        FilterPreset retrieved = DatabaseManager.getInstance().getPreset("Test Preset");
        assertNotNull(retrieved);
        assertEquals("Test Preset", retrieved.getName());
        assertEquals(25.0, retrieved.getBrightness());
    }

    @Test
    @Order(7)
    public void testDeletePreset() {
        boolean deleted = DatabaseManager.getInstance().deletePreset("Test Preset");
        assertTrue(deleted);

        FilterPreset afterDelete = DatabaseManager.getInstance().getPreset("Test Preset");
        assertNull(afterDelete);
    }
}
