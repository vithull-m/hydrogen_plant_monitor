package com.hydroplant.controller;

import com.hydroplant.dao.MaintenanceDAO;
import com.hydroplant.model.Maintainable;
import com.hydroplant.model.MaintenanceTask;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Coordinates maintenance scheduling. Works against the Maintainable
 * interface so it can schedule maintenance for any asset type
 * (EnergySource subclasses, HydrogenProductionUnit, etc.) uniformly.
 */
public class MaintenanceController {

    private final MaintenanceDAO dao = new MaintenanceDAO();

    public List<MaintenanceTask> getAllTasks() throws SQLException {
        return dao.findAll();
    }

    public List<MaintenanceTask> getTasksByStatus(MaintenanceTask.Status status) throws SQLException {
        return dao.findByStatus(status);
    }

    /**
     * Schedules a new maintenance task for the given asset and marks the
     * asset as under maintenance immediately (taking it offline).
     */
    public int scheduleMaintenance(Maintainable asset, String assetType, LocalDate date,
                                    String description, String technician) throws SQLException {
        MaintenanceTask task = new MaintenanceTask(
                0, asset.getAssetId(), asset.getAssetName(), assetType,
                date, description, MaintenanceTask.Status.SCHEDULED, technician);
        asset.setUnderMaintenance(true);
        return dao.insert(task);
    }

    /**
     * Marks a task complete and brings the asset back online.
     */
    public void completeMaintenance(MaintenanceTask task, Maintainable asset) throws SQLException {
        dao.updateStatus(task.getId(), MaintenanceTask.Status.COMPLETED);
        task.setStatus(MaintenanceTask.Status.COMPLETED);
        if (asset != null) {
            asset.setUnderMaintenance(false);
        }
    }

    public void updateStatus(int taskId, MaintenanceTask.Status status) throws SQLException {
        dao.updateStatus(taskId, status);
    }

    public void cancelTask(int taskId) throws SQLException {
        dao.updateStatus(taskId, MaintenanceTask.Status.CANCELLED);
    }

    public void deleteTask(int taskId) throws SQLException {
        dao.delete(taskId);
    }
}
