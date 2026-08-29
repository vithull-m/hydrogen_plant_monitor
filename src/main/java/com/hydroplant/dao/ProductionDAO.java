package com.hydroplant.dao;

import com.hydroplant.model.HydrogenProductionUnit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductionDAO {

    public List<HydrogenProductionUnit> findAllUnits() throws SQLException {
        List<HydrogenProductionUnit> units = new ArrayList<>();
        String sql = "SELECT * FROM production_units ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HydrogenProductionUnit unit = new HydrogenProductionUnit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("energy_consumption_rate_kwh_per_kg"),
                        rs.getDouble("max_throughput_kg_per_hour")
                );
                unit.setUnderMaintenance(rs.getBoolean("under_maintenance"));
                units.add(unit);
            }
        }
        return units;
    }

    public int insertUnit(HydrogenProductionUnit unit) throws SQLException {
        String sql = "INSERT INTO production_units (name, energy_consumption_rate_kwh_per_kg, max_throughput_kg_per_hour, under_maintenance) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, unit.getName());
            ps.setDouble(2, unit.getEnergyConsumptionRateKwhPerKg());
            ps.setDouble(3, unit.getMaxThroughputKgPerHour());
            ps.setBoolean(4, unit.isUnderMaintenance());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        }
    }

    /** Logs one hour (or partial period) of production for reporting/history. */
    public void logProduction(int unitId, double energyConsumedKwh, double hydrogenProducedKg) throws SQLException {
        String sql = "INSERT INTO production_log (unit_id, log_time, energy_consumed_kwh, hydrogen_produced_kg) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, unitId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(3, energyConsumedKwh);
            ps.setDouble(4, hydrogenProducedKg);
            ps.executeUpdate();
        }
    }

    /** Total hydrogen produced across all units within a date range, for reports. */
    public double getTotalProduction(LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(hydrogen_produced_kg), 0) AS total FROM production_log " +
                "WHERE log_time BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("total") : 0.0;
            }
        }
    }
}
