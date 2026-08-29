package com.hydroplant.model;

import com.hydroplant.exception.InsufficientStorageException;

/**
 * Represents a physical hydrogen storage tank. Encapsulates the current
 * fill level and enforces capacity/withdrawal rules through checked
 * exceptions rather than silently failing.
 */
public class StorageTank {

    private int id;
    private String location;
    private double capacityKg;
    private double currentLevelKg;
    private double maxSafePressureBar;

    public StorageTank(int id, String location, double capacityKg,
                        double currentLevelKg, double maxSafePressureBar) {
        this.id = id;
        this.location = location;
        this.capacityKg = capacityKg;
        this.currentLevelKg = currentLevelKg;
        this.maxSafePressureBar = maxSafePressureBar;
    }

    /** Adds freshly produced hydrogen to the tank, respecting capacity. */
    public void addHydrogen(double kg) throws InsufficientStorageException {
        if (kg < 0) throw new IllegalArgumentException("Cannot add a negative amount");
        if (currentLevelKg + kg > capacityKg) {
            throw new InsufficientStorageException(
                    String.format("Tank '%s' cannot hold %.2f kg more - only %.2f kg of free space remains",
                            location, kg, (capacityKg - currentLevelKg)));
        }
        currentLevelKg += kg;
    }

    /** Withdraws hydrogen from the tank (e.g. for dispatch/sale). */
    public void withdrawHydrogen(double kg) throws InsufficientStorageException {
        if (kg < 0) throw new IllegalArgumentException("Cannot withdraw a negative amount");
        if (kg > currentLevelKg) {
            throw new InsufficientStorageException(
                    String.format("Tank '%s' only has %.2f kg stored - cannot withdraw %.2f kg",
                            location, currentLevelKg, kg));
        }
        currentLevelKg -= kg;
    }

    public double getFillPercentage() {
        return capacityKg == 0 ? 0 : (currentLevelKg / capacityKg) * 100.0;
    }

    public int getId() { return id; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getCapacityKg() { return capacityKg; }
    public void setCapacityKg(double capacityKg) { this.capacityKg = capacityKg; }

    public double getCurrentLevelKg() { return currentLevelKg; }

    public double getMaxSafePressureBar() { return maxSafePressureBar; }
    public void setMaxSafePressureBar(double bar) { this.maxSafePressureBar = bar; }

    @Override
    public String toString() {
        return String.format("Tank #%d @ %s: %.2f/%.2f kg (%.1f%% full)",
                id, location, currentLevelKg, capacityKg, getFillPercentage());
    }
}
