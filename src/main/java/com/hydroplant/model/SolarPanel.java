package com.hydroplant.model;

/**
 * Solar energy source. Output is modelled from solar irradiance (W/m^2)
 * and a fixed panel efficiency factor.
 */
public class SolarPanel extends EnergySource {

    private double efficiency; // 0.0 - 1.0

    public SolarPanel(int id, String name, double ratedCapacityKw, double efficiency) {
        super(id, name, ratedCapacityKw);
        this.efficiency = efficiency;
    }

    public double getEfficiency() { return efficiency; }
    public void setEfficiency(double efficiency) {
        if (efficiency < 0 || efficiency > 1) {
            throw new IllegalArgumentException("Efficiency must be between 0 and 1");
        }
        this.efficiency = efficiency;
    }

    /**
     * output(kW) = (irradiance / 1000) * ratedCapacity * efficiency
     * irradiance is clamped to [0, 1000] W/m^2 (standard test condition ceiling).
     */
    @Override
    public double calculateOutput(double irradianceWm2) {
        if (isUnderMaintenance()) return 0.0;
        double clamped = Math.max(0, Math.min(irradianceWm2, 1000));
        double output = (clamped / 1000.0) * getRatedCapacityKw() * efficiency;
        return Math.min(output, getRatedCapacityKw());
    }

    @Override
    public String getSourceType() {
        return "Solar";
    }
}
