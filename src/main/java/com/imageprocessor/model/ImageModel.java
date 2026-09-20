package com.imageprocessor.model;

import java.time.LocalDateTime;

/**
 * Model class representing an Image record.
 */
public class ImageModel {
    private long id;
    private String filePath;
    private String fileName;
    private int width;
    private int height;
    private String format;
    private long fileSizeBytes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ImageModel() {
    }

    public ImageModel(long id, String filePath, String fileName, int width, int height, String format, long fileSizeBytes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.filePath = filePath;
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.format = format;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        }
        int exp = (int) (Math.log(fileSizeBytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", fileSizeBytes / Math.pow(1024, exp), pre);
    }

    public String getDimensionsString() {
        return width + "x" + height;
    }

    public java.io.File getFile() {
        return filePath != null ? new java.io.File(filePath) : null;
    }

    public void setFile(java.io.File file) {
        if (file != null) {
            this.filePath = file.getAbsolutePath();
            if (this.fileName == null || this.fileName.isEmpty()) {
                this.fileName = file.getName();
            }
        }
    }

    public LocalDateTime getAddedDate() {
        return createdAt;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.createdAt = addedDate;
        this.updatedAt = addedDate;
    }

    @Override
    public String toString() {
        return fileName;
    }
}
