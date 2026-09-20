package com.imageprocessor.model;

import java.time.LocalDateTime;

/**
 * Model class representing an edit log entry.
 */
public class EditLog {
    private long id;
    private long imageId;
    private String operationType;
    private String details;
    private LocalDateTime timestamp;

    public EditLog() {
    }

    public EditLog(long imageId, String operationType, String details) {
        this.imageId = imageId;
        this.operationType = operationType;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "EditLog{" +
                "id=" + id +
                ", imageId=" + imageId +
                ", operationType='" + operationType + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
