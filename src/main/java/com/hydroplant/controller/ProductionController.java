package com.hydroplant.controller;

import com.hydroplant.dao.ProductionDAO;
import com.hydroplant.exception.InvalidOperationException;
import com.hydroplant.model.HydrogenProductionUnit;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Coordinates hydrogen production: distributes available electrical
 * energy across production units and logs the resulting output.
 */
public class ProductionController {

    private final ProductionDAO dao = new ProductionDAO();

    public List<HydrogenProductionUnit> getAllUnits() throws SQLException {
        return dao.findAllUnits();
    }

    public int addUnit(HydrogenProductionUnit unit) throws SQLException {
        return dao.insertUnit(unit);
    }

    /**
     * Distributes availableEnergyKwh evenly across all units that are not
     * under maintenance, produces hydrogen for each, logs the results, and
     * returns the total hydrogen produced (kg) for this cycle.
     *
     * @throws InvalidOperationException if there are no operational units to run
     */
    public double runProductionCycle(List<HydrogenProductionUnit> units, double availableEnergyKwh)
            throws InvalidOperationException {

        List<HydrogenProductionUnit> operational = units.stream()
                .filter(u -> !u.isUnderMaintenance())
                .toList();

        if (operational.isEmpty()) {
            throw new InvalidOperationException(
                    "No operational electrolyzer units are available - all units are under maintenance.");
        }

        double energyPerUnit = availableEnergyKwh / operational.size();
        double totalHydrogenKg = 0;

        for (HydrogenProductionUnit unit : operational) {
            double hydrogenKg = unit.produceHydrogen(energyPerUnit);
            double energyActuallyUsed = hydrogenKg * unit.getEnergyConsumptionRateKwhPerKg();
            totalHydrogenKg += hydrogenKg;
            try {
                dao.logProduction(unit.getId(), energyActuallyUsed, hydrogenKg);
            } catch (SQLException e) {
                System.err.println("Failed to log production for unit " + unit.getId() + ": " + e.getMessage());
            }
        }
        return totalHydrogenKg;
    }

    public double getTotalProductionToday() throws SQLException {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return dao.getTotalProduction(startOfDay, now);
    }

    public double getTotalProductionInRange(LocalDateTime from, LocalDateTime to) throws SQLException {
        return dao.getTotalProduction(from, to);
    }
}
