package com.hydroplant.dao;

import com.hydroplant.model.StorageTank;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StorageTankDAO {

    public List<StorageTank> findAll() throws SQLException {
        List<StorageTank> tanks = new ArrayList<>();
        String sql = "SELECT * FROM storage_tanks ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tanks.add(mapRow(rs));
            }
        }
        return tanks;
    }

    public int insert(StorageTank tank) throws SQLException {
        String sql = "INSERT INTO storage_tanks (location, capacity_kg, current_level_kg, max_safe_pressure_bar) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tank.getLocation());
            ps.setDouble(2, tank.getCapacityKg());
            ps.setDouble(3, tank.getCurrentLevelKg());
            ps.setDouble(4, tank.getMaxSafePressureBar());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        }
    }

    /** Persists the tank's current level after an add/withdraw operation. */
    public void updateLevel(int tankId, double newLevelKg) throws SQLException {
        String sql = "UPDATE storage_tanks SET current_level_kg = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newLevelKg);
            ps.setInt(2, tankId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM storage_tanks WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private StorageTank mapRow(ResultSet rs) throws SQLException {
        return new StorageTank(
                rs.getInt("id"),
                rs.getString("location"),
                rs.getDouble("capacity_kg"),
                rs.getDouble("current_level_kg"),
                rs.getDouble("max_safe_pressure_bar")
        );
    }
}
