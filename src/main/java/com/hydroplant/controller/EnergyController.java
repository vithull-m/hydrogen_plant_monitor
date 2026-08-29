package com.hydroplant.controller;

import com.hydroplant.dao.EnergySourceDAO;
import com.hydroplant.model.EnergySource;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Coordinates between the EnergySource model classes and their DAO.
 * Also simulates environmental readings (irradiance / wind speed) since
 * this is a desktop simulation rather than a live SCADA feed.
 */
public class EnergyController {

    private final EnergySourceDAO dao = new EnergySourceDAO();
    private final Random random = new Random();

    public List<EnergySource> getAllSources() throws SQLException {
        return dao.findAll();
    }

    public int addSource(EnergySource source) throws SQLException {
        return dao.insert(source);
    }

    public void deleteSource(int id) throws SQLException {
        dao.delete(id);
    }

    public void setMaintenanceMode(int id, boolean underMaintenance) throws SQLException {
        dao.updateMaintenanceStatus(id, underMaintenance);
    }

    /**
     * Simulates a plausible environmental reading for the given source type.
     * Solar: irradiance in W/m^2 (0-1000), higher around midday.
     * Wind: wind speed in m/s (0-25), randomly varying.
     */
    public double simulateEnvironmentValue(String sourceType) {
        if ("Solar".equalsIgnoreCase(sourceType)) {
            return 200 + random.nextDouble() * 750; // 200-950 W/m^2
        } else {
            return random.nextDouble() * 20; // 0-20 m/s
        }
    }

    /**
     * Computes current output for every source using a simulated reading,
     * logs the reading to the database, and returns the total kW generated
     * (this feeds into the production controller).
     */
    public double computeAndLogCurrentGeneration(List<EnergySource> sources) {
        double totalKw = 0;
        for (EnergySource source : sources) {
            double env = simulateEnvironmentValue(source.getSourceType());
            double output = source.calculateOutput(env);
            totalKw += output;
            try {
                dao.logReading(source.getId(), env, output);
            } catch (SQLException e) {
                // Logging failures shouldn't crash the dashboard; surface via stderr.
                System.err.println("Failed to log reading for source " + source.getId() + ": " + e.getMessage());
            }
        }
        return totalKw;
    }

    public List<EnergySource> sortByOutputDescending(List<EnergySource> sources, double environmentValueSolar, double environmentValueWind) {
        sources.sort((a, b) -> {
            double outA = a.calculateOutput("Solar".equals(a.getSourceType()) ? environmentValueSolar : environmentValueWind);
            double outB = b.calculateOutput("Solar".equals(b.getSourceType()) ? environmentValueSolar : environmentValueWind);
            return Double.compare(outB, outA);
        });
        return sources;
    }
}
