package io.will.webfluxdemo.model;

import java.time.LocalDateTime;

public class ExportResult {
    private Long taskId;
    private String fileName;
    private String downloadUrl;
    private Long totalRecords;
    private String status;
    private LocalDateTime completedAt;
    private Long fileSizeBytes;

    public ExportResult() {}

    public ExportResult(Long taskId, String fileName, String downloadUrl, 
                       Long totalRecords, String status, LocalDateTime completedAt, 
                       Long fileSizeBytes) {
        this.taskId = taskId;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.totalRecords = totalRecords;
        this.status = status;
        this.completedAt = completedAt;
        this.fileSizeBytes = fileSizeBytes;
    }

    // Getters and setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    @Override
    public String toString() {
        return "ExportResult{" +
                "taskId=" + taskId +
                ", fileName='" + fileName + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", totalRecords=" + totalRecords +
                ", status='" + status + '\'' +
                ", completedAt=" + completedAt +
                ", fileSizeBytes=" + fileSizeBytes +
                '}';
    }
}