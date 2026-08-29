package com.hydroplant.controller;

import com.hydroplant.dao.StorageTankDAO;
import com.hydroplant.exception.InsufficientStorageException;
import com.hydroplant.model.StorageTank;

import java.sql.SQLException;
import java.util.List;

/**
 * Coordinates storage tank operations, translating model-level checked
 * exceptions into DB updates so the UI layer only needs to catch one
 * exception type per action.
 */
public class StorageController {

    private final StorageTankDAO dao = new StorageTankDAO();

    public List<StorageTank> getAllTanks() throws SQLException {
        return dao.findAll();
    }

    public int addTank(StorageTank tank) throws SQLException {
        return dao.insert(tank);
    }

    public void deleteTank(int id) throws SQLException {
        dao.delete(id);
    }

    /**
     * Adds newly produced hydrogen to the given tank and persists the new
     * level. Throws InsufficientStorageException if the tank would overflow.
     */
    public void depositHydrogen(StorageTank tank, double kg) throws InsufficientStorageException, SQLException {
        tank.addHydrogen(kg);
        dao.updateLevel(tank.getId(), tank.getCurrentLevelKg());
    }

    /**
     * Withdraws hydrogen from the given tank (e.g. dispatch to a customer)
     * and persists the new level. Throws InsufficientStorageException if
     * there isn't enough hydrogen stored.
     */
    public void withdrawHydrogen(StorageTank tank, double kg) throws InsufficientStorageException, SQLException {
        tank.withdrawHydrogen(kg);
        dao.updateLevel(tank.getId(), tank.getCurrentLevelKg());
    }

    /**
     * Distributes a batch of newly produced hydrogen across all tanks,
     * filling each in order until capacity is reached. Any hydrogen that
     * cannot be stored anywhere is returned as "overflow" (kg) so the
     * caller/UI can warn the operator.
     */
    public double distributeProduction(List<StorageTank> tanks, double totalKg) throws SQLException {
        double remaining = totalKg;
        for (StorageTank tank : tanks) {
            if (remaining <= 0) break;
            double freeSpace = tank.getCapacityKg() - tank.getCurrentLevelKg();
            double toAdd = Math.min(freeSpace, remaining);
            if (toAdd > 0) {
                try {
                    tank.addHydrogen(toAdd);
                    dao.updateLevel(tank.getId(), tank.getCurrentLevelKg());
                    remaining -= toAdd;
                } catch (InsufficientStorageException e) {
                    // Should not happen since toAdd <= freeSpace, but guard anyway.
                    System.err.println("Unexpected overflow while distributing production: " + e.getMessage());
                }
            }
        }
        return remaining; // overflow, if any
    }

    public double getTotalStoredKg(List<StorageTank> tanks) {
        return tanks.stream().mapToDouble(StorageTank::getCurrentLevelKg).sum();
    }

    public double getTotalCapacityKg(List<StorageTank> tanks) {
        return tanks.stream().mapToDouble(StorageTank::getCapacityKg).sum();
    }
}
