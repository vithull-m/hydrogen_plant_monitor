package com.hydroplant.dao;

import com.hydroplant.model.MaintenanceTask;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    public List<MaintenanceTask> findAll() throws SQLException {
        List<MaintenanceTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_tasks ORDER BY scheduled_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    public List<MaintenanceTask> findByStatus(MaintenanceTask.Status status) throws SQLException {
        List<MaintenanceTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_tasks WHERE status = ? ORDER BY scheduled_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    public int insert(MaintenanceTask task) throws SQLException {
        String sql = "INSERT INTO maintenance_tasks " +
                "(asset_id, asset_name, asset_type, scheduled_date, description, status, technician) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getAssetId());
            ps.setString(2, task.getAssetName());
            ps.setString(3, task.getAssetType());
            ps.setDate(4, Date.valueOf(task.getScheduledDate()));
            ps.setString(5, task.getDescription());
            ps.setString(6, task.getStatus().name());
            ps.setString(7, task.getTechnician());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        }
    }

    public void updateStatus(int taskId, MaintenanceTask.Status status) throws SQLException {
        String sql = "UPDATE maintenance_tasks SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, taskId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM maintenance_tasks WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private MaintenanceTask mapRow(ResultSet rs) throws SQLException {
        return new MaintenanceTask(
                rs.getInt("id"),
                rs.getInt("asset_id"),
                rs.getString("asset_name"),
                rs.getString("asset_type"),
                rs.getDate("scheduled_date").toLocalDate(),
                rs.getString("description"),
                MaintenanceTask.Status.valueOf(rs.getString("status")),
                rs.getString("technician")
        );
    }
}
