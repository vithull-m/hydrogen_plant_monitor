package com.hydroplant.model;

import java.time.LocalDate;

/**
 * Represents a scheduled or completed maintenance activity performed on
 * an asset (energy source or production unit) identified by assetId.
 */
public class MaintenanceTask {

    public enum Status { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }

    private int id;
    private int assetId;
    private String assetName;
    private String assetType;      // "Solar", "Wind", "Electrolyzer", etc.
    private LocalDate scheduledDate;
    private String description;
    private Status status;
    private String technician;

    public MaintenanceTask(int id, int assetId, String assetName, String assetType,
                            LocalDate scheduledDate, String description,
                            Status status, String technician) {
        this.id = id;
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetType = assetType;
        this.scheduledDate = scheduledDate;
        this.description = description;
        this.status = status;
        this.technician = technician;
    }

    public int getId() { return id; }
    public int getAssetId() { return assetId; }
    public String getAssetName() { return assetName; }
    public String getAssetType() { return assetType; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getTechnician() { return technician; }
    public void setTechnician(String technician) { this.technician = technician; }

    @Override
    public String toString() {
        return String.format("[#%d] %s (%s) - %s on %s - %s",
                id, assetName, assetType, description, scheduledDate, status);
    }
}
