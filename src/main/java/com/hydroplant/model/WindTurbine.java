package com.hydroplant.model;

/**
 * Wind energy source. Uses a simplified cubic power curve typical of real
 * wind turbines: power scales with the cube of wind speed between the
 * cut-in and rated speeds, and is zero outside the cut-in/cut-out range.
 */
public class WindTurbine extends EnergySource {

    private double cutInSpeed;   // m/s, below this: no output
    private double ratedSpeed;   // m/s, at/above this: full rated output
    private double cutOutSpeed;  // m/s, above this: shut down for safety

    public WindTurbine(int id, String name, double ratedCapacityKw,
                        double cutInSpeed, double ratedSpeed, double cutOutSpeed) {
        super(id, name, ratedCapacityKw);
        this.cutInSpeed = cutInSpeed;
        this.ratedSpeed = ratedSpeed;
        this.cutOutSpeed = cutOutSpeed;
    }

    public double getCutInSpeed() { return cutInSpeed; }
    public double getRatedSpeed() { return ratedSpeed; }
    public double getCutOutSpeed() { return cutOutSpeed; }

    @Override
    public double calculateOutput(double windSpeedMs) {
        if (isUnderMaintenance()) return 0.0;
        if (windSpeedMs < cutInSpeed || windSpeedMs >= cutOutSpeed) {
            return 0.0;
        }
        if (windSpeedMs >= ratedSpeed) {
            return getRatedCapacityKw();
        }
        // Cubic ramp-up between cut-in and rated speed
        double fraction = Math.pow(
                (windSpeedMs - cutInSpeed) / (ratedSpeed - cutInSpeed), 3);
        return getRatedCapacityKw() * fraction;
    }

    @Override
    public String getSourceType() {
        return "Wind";
    }
}
