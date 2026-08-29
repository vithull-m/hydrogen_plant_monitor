package com.hydroplant.dao;

import com.hydroplant.model.EnergySource;
import com.hydroplant.model.SolarPanel;
import com.hydroplant.model.WindTurbine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence of EnergySource objects (both SolarPanel and
 * WindTurbine subtypes) in the energy_sources table. Demonstrates
 * mapping a class hierarchy onto a single table using a discriminator
 * column (source_type).
 */
public class EnergySourceDAO {

    public List<EnergySource> findAll() throws SQLException {
        List<EnergySource> sources = new ArrayList<>();
        String sql = "SELECT * FROM energy_sources ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sources.add(mapRow(rs));
            }
        }
        return sources;
    }

    public EnergySource findById(int id) throws SQLException {
        String sql = "SELECT * FROM energy_sources WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public int insert(EnergySource source) throws SQLException {
        String sql = "INSERT INTO energy_sources " +
                "(name, source_type, rated_capacity_kw, efficiency, cut_in_speed, rated_speed, cut_out_speed, under_maintenance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, source.getName());
            ps.setString(2, source.getSourceType());
            ps.setDouble(3, source.getRatedCapacityKw());

            if (source instanceof SolarPanel sp) {
                ps.setDouble(4, sp.getEfficiency());
                ps.setNull(5, Types.DOUBLE);
                ps.setNull(6, Types.DOUBLE);
                ps.setNull(7, Types.DOUBLE);
            } else if (source instanceof WindTurbine wt) {
                ps.setNull(4, Types.DOUBLE);
                ps.setDouble(5, wt.getCutInSpeed());
                ps.setDouble(6, wt.getRatedSpeed());
                ps.setDouble(7, wt.getCutOutSpeed());
            }
            ps.setBoolean(8, source.isUnderMaintenance());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        }
    }

    public void updateMaintenanceStatus(int id, boolean underMaintenance) throws SQLException {
        String sql = "UPDATE energy_sources SET under_maintenance = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, underMaintenance);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM energy_sources WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Records a reading (environment value + calculated output) for history/reporting. */
    public void logReading(int sourceId, double environmentValue, double outputKw) throws SQLException {
        String sql = "INSERT INTO energy_readings (source_id, reading_time, environment_value, output_kw) " +
                "VALUES (?, NOW(), ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sourceId);
            ps.setDouble(2, environmentValue);
            ps.setDouble(3, outputKw);
            ps.executeUpdate();
        }
    }

    private EnergySource mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String type = rs.getString("source_type");
        double rated = rs.getDouble("rated_capacity_kw");
        boolean underMaintenance = rs.getBoolean("under_maintenance");

        EnergySource source;
        if ("Solar".equalsIgnoreCase(type)) {
            double eff = rs.getDouble("efficiency");
            source = new SolarPanel(id, name, rated, eff);
        } else {
            double cutIn = rs.getDouble("cut_in_speed");
            double ratedSpeed = rs.getDouble("rated_speed");
            double cutOut = rs.getDouble("cut_out_speed");
            source = new WindTurbine(id, name, rated, cutIn, ratedSpeed, cutOut);
        }
        source.setUnderMaintenance(underMaintenance);
        return source;
    }
}
