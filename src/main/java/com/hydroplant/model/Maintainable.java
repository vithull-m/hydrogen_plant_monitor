package com.hydroplant.model;

/**
 * Contract for any plant asset that can be scheduled for and undergo maintenance.
 * Implemented by equipment classes so the MaintenanceController can treat
 * different asset types uniformly (polymorphism through interfaces).
 */
public interface Maintainable {

    /** Unique identifier used to link this asset to maintenance records. */
    int getAssetId();

    /** Human-readable name shown in maintenance schedules and reports. */
    String getAssetName();

    /** Marks the asset as currently offline for maintenance. */
    void setUnderMaintenance(boolean underMaintenance);

    /** @return true if the asset is currently offline for maintenance. */
    boolean isUnderMaintenance();
}
