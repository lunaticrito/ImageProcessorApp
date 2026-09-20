package com.imageprocessor.database;

import com.imageprocessor.model.EditLog;
import com.imageprocessor.model.FilterPreset;
import com.imageprocessor.model.ImageModel;
import com.imageprocessor.util.JsonUtil;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton Database Manager for SQLite.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_DIR = "data";
    private static final String DB_FILE = "image_processor.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + File.separator + DB_FILE;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DatabaseManager() {
        initialize();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public synchronized void initialize() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            createTables();
        } catch (SQLException e) {
            System.err.println("Error initializing database connection: " + e.getMessage());
        }
    }

    private void createTables() {
        String createImagesTable = "CREATE TABLE IF NOT EXISTS images (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "file_path TEXT NOT NULL UNIQUE, " +
                "file_name TEXT NOT NULL, " +
                "width INTEGER, " +
                "height INTEGER, " +
                "format TEXT, " +
                "file_size_bytes INTEGER, " +
                "created_at TEXT NOT NULL, " +
                "updated_at TEXT NOT NULL" +
                ")";

        String createEditLogsTable = "CREATE TABLE IF NOT EXISTS edit_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "image_id INTEGER NOT NULL, " +
                "operation_type TEXT NOT NULL, " +
                "details TEXT, " +
                "timestamp TEXT NOT NULL, " +
                "FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE" +
                ")";

        String createFilterPresetsTable = "CREATE TABLE IF NOT EXISTS filter_presets (" +
                "name TEXT PRIMARY KEY, " +
                "preset_json TEXT NOT NULL" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createImagesTable);
            stmt.execute(createEditLogsTable);
            stmt.execute(createFilterPresetsTable);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    public long saveImage(ImageModel img) {
        String sql = "INSERT INTO images (id, file_path, file_name, width, height, format, file_size_bytes, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(file_path) DO UPDATE SET " +
                "file_name=excluded.file_name, width=excluded.width, height=excluded.height, " +
                "format=excluded.format, file_size_bytes=excluded.file_size_bytes, updated_at=excluded.updated_at";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (img.getId() > 0) {
                pstmt.setLong(1, img.getId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, img.getFilePath());
            pstmt.setString(3, img.getFileName());
            pstmt.setInt(4, img.getWidth());
            pstmt.setInt(5, img.getHeight());
            pstmt.setString(6, img.getFormat());
            pstmt.setLong(7, img.getFileSizeBytes());
            pstmt.setString(8, img.getCreatedAt() != null ? img.getCreatedAt().format(FORMATTER) : LocalDateTime.now().format(FORMATTER));
            pstmt.setString(9, LocalDateTime.now().format(FORMATTER));

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    img.setId(id);
                    return id;
                }
            }
            return img.getId();
        } catch (SQLException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return -1;
        }
    }

    public ImageModel getImageByPath(String filePath) {
        String sql = "SELECT * FROM images WHERE file_path = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, filePath);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToImageModel(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving image by path: " + e.getMessage());
        }
        return null;
    }

    public List<ImageModel> getAllImages() {
        List<ImageModel> images = new ArrayList<>();
        String sql = "SELECT * FROM images ORDER BY updated_at DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                images.add(mapResultSetToImageModel(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all images: " + e.getMessage());
        }
        return images;
    }

    public boolean deleteImage(long id) {
        String sql = "DELETE FROM images WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting image: " + e.getMessage());
            return false;
        }
    }

    public void logEdit(long imageId, String operation, String details) {
        String sql = "INSERT INTO edit_logs (image_id, operation_type, details, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, imageId);
            pstmt.setString(2, operation);
            pstmt.setString(3, details);
            pstmt.setString(4, LocalDateTime.now().format(FORMATTER));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging edit: " + e.getMessage());
        }
    }

    public List<EditLog> getLogsForImage(long imageId) {
        List<EditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM edit_logs WHERE image_id = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, imageId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EditLog log = new EditLog();
                    log.setId(rs.getLong("id"));
                    log.setImageId(rs.getLong("image_id"));
                    log.setOperationType(rs.getString("operation_type"));
                    log.setDetails(rs.getString("details"));
                    log.setTimestamp(LocalDateTime.parse(rs.getString("timestamp"), FORMATTER));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving logs for image: " + e.getMessage());
        }
        return logs;
    }

    public void savePreset(String name, String presetJson) {
        String sql = "INSERT INTO filter_presets (name, preset_json) VALUES (?, ?) " +
                "ON CONFLICT(name) DO UPDATE SET preset_json = excluded.preset_json";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, presetJson);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving preset: " + e.getMessage());
        }
    }

    public Map<String, String> getAllPresets() {
        Map<String, String> presets = new HashMap<>();
        String sql = "SELECT * FROM filter_presets";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                presets.put(rs.getString("name"), rs.getString("preset_json"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all presets: " + e.getMessage());
        }
        return presets;
    }

    public boolean deletePreset(String name) {
        String sql = "DELETE FROM filter_presets WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting preset: " + e.getMessage());
            return false;
        }
    }

    public long saveImageMetadata(ImageModel img) {
        return saveImage(img);
    }

    public void logEdit(long imageId, String operation) {
        logEdit(imageId, operation, null);
    }

    public List<String> getEditHistory(long imageId) {
        List<EditLog> logs = getLogsForImage(imageId);
        List<String> result = new ArrayList<>();
        for (EditLog log : logs) {
            String ts = log.getTimestamp() != null ? log.getTimestamp().format(FORMATTER) : "";
            result.add("[" + ts + "] " + log.getOperationType() + (log.getDetails() != null ? " - " + log.getDetails() : ""));
        }
        return result;
    }

    public void clearHistory(long imageId) {
        String sql = "DELETE FROM edit_logs WHERE image_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, imageId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing edit history: " + e.getMessage());
        }
    }

    public void savePreset(FilterPreset preset) {
        if (preset == null) return;
        try {
            String json = JsonUtil.serialize(preset);
            savePreset(preset.getName(), json);
        } catch (Exception e) {
            System.err.println("Error saving preset object: " + e.getMessage());
        }
    }

    public FilterPreset getPreset(String name) {
        String sql = "SELECT preset_json FROM filter_presets WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return JsonUtil.deserialize(rs.getString("preset_json"), FilterPreset.class);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting preset: " + e.getMessage());
        }
        return null;
    }

    public List<String> getAllPresetNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM filter_presets ORDER BY name ASC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all preset names: " + e.getMessage());
        }
        return names;
    }

    private ImageModel mapResultSetToImageModel(ResultSet rs) throws SQLException {
        ImageModel img = new ImageModel();
        img.setId(rs.getLong("id"));
        img.setFilePath(rs.getString("file_path"));
        img.setFileName(rs.getString("file_name"));
        img.setWidth(rs.getInt("width"));
        img.setHeight(rs.getInt("height"));
        img.setFormat(rs.getString("format"));
        img.setFileSizeBytes(rs.getLong("file_size_bytes"));
        img.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), FORMATTER));
        img.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at"), FORMATTER));
        return img;
    }
}
