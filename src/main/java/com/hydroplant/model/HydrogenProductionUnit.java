package com.hydroplant.model;

/**
 * Represents an electrolyzer unit that converts electrical energy into
 * hydrogen gas. A commonly used approximation for PEM/alkaline electrolyzers
 * is roughly 50-55 kWh of electricity per kg of hydrogen produced; this
 * class lets that conversion rate be configured per unit.
 */
public class HydrogenProductionUnit implements Maintainable {

    private int id;
    private String name;
    private double energyConsumptionRateKwhPerKg; // kWh required per kg of H2
    private double maxThroughputKgPerHour;         // physical ceiling of the unit
    private boolean underMaintenance;

    public HydrogenProductionUnit(int id, String name,
                                   double energyConsumptionRateKwhPerKg,
                                   double maxThroughputKgPerHour) {
        this.id = id;
        this.name = name;
        this.energyConsumptionRateKwhPerKg = energyConsumptionRateKwhPerKg;
        this.maxThroughputKgPerHour = maxThroughputKgPerHour;
        this.underMaintenance = false;
    }

    /**
     * Given available energy for a one-hour window, calculates how many kg
     * of hydrogen can be produced, capped by the unit's physical throughput.
     */
    public double produceHydrogen(double availableEnergyKwh) {
        if (underMaintenance || availableEnergyKwh <= 0) return 0.0;
        double possibleKg = availableEnergyKwh / energyConsumptionRateKwhPerKg;
        return Math.min(possibleKg, maxThroughputKgPerHour);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getEnergyConsumptionRateKwhPerKg() { return energyConsumptionRateKwhPerKg; }
    public void setEnergyConsumptionRateKwhPerKg(double rate) {
        if (rate <= 0) throw new IllegalArgumentException("Rate must be positive");
        this.energyConsumptionRateKwhPerKg = rate;
    }

    public double getMaxThroughputKgPerHour() { return maxThroughputKgPerHour; }
    public void setMaxThroughputKgPerHour(double v) { this.maxThroughputKgPerHour = v; }

    @Override
    public int getAssetId() { return id; }

    @Override
    public String getAssetName() { return name; }

    @Override
    public boolean isUnderMaintenance() { return underMaintenance; }

    @Override
    public void setUnderMaintenance(boolean underMaintenance) {
        this.underMaintenance = underMaintenance;
    }

    @Override
    public String toString() {
        return String.format("%s (max %.1f kg/h, %.1f kWh/kg)", name,
                maxThroughputKgPerHour, energyConsumptionRateKwhPerKg);
    }
}
