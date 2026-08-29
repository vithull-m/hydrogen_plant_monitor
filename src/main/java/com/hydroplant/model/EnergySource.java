package com.hydroplant.model;

/**
 * Abstract base class representing any renewable energy generator feeding
 * the plant. Concrete subclasses (SolarPanel, WindTurbine) each provide
 * their own calculateOutput() implementation, which is the key polymorphic
 * behaviour used throughout the dashboard and controllers.
 */
public abstract class EnergySource implements Maintainable {

    private int id;
    private String name;
    private double ratedCapacityKw;   // maximum rated output in kW
    private boolean underMaintenance;

    protected EnergySource(int id, String name, double ratedCapacityKw) {
        this.id = id;
        this.name = name;
        this.ratedCapacityKw = ratedCapacityKw;
        this.underMaintenance = false;
    }

    /**
     * Calculates the current real-time power output (in kW) of this source
     * given the environmental reading passed in. Each subclass implements
     * this differently -- this is the polymorphism demonstration.
     *
     * @param environmentValue for solar this is irradiance (W/m^2, 0-1000),
     *                         for wind this is wind speed (m/s)
     * @return simulated output in kW, never exceeding ratedCapacityKw
     */
    public abstract double calculateOutput(double environmentValue);

    /** @return a short label describing the type of source, e.g. "Solar" */
    public abstract String getSourceType();

    // ----- Encapsulated accessors -----
    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getRatedCapacityKw() { return ratedCapacityKw; }
    public void setRatedCapacityKw(double ratedCapacityKw) {
        if (ratedCapacityKw <= 0) {
            throw new IllegalArgumentException("Rated capacity must be positive");
        }
        this.ratedCapacityKw = ratedCapacityKw;
    }

    // ----- Maintainable interface implementation -----
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
        return String.format("%s [%s] - %.1f kW rated", name, getSourceType(), ratedCapacityKw);
    }
}
