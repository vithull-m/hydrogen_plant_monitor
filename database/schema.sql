-- =====================================================================
-- Green Hydrogen Plant Monitoring and Management System
-- MySQL Schema
-- =====================================================================
-- Import with:
--   mysql -u root -p < schema.sql
-- =====================================================================

DROP DATABASE IF EXISTS hydrogen_plant;
CREATE DATABASE hydrogen_plant CHARACTER SET utf8mb4;
USE hydrogen_plant;

-- ---------------------------------------------------------------------
-- Users (login)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'OPERATOR') NOT NULL DEFAULT 'OPERATOR'
);

INSERT INTO users (username, password_hash, role) VALUES
('admin', SHA2('admin123', 256), 'ADMIN');

-- ---------------------------------------------------------------------
-- Energy sources: solar panels and wind turbines share this table via
-- a discriminator column (source_type). Nullable columns hold whichever
-- subtype-specific fields apply.
-- ---------------------------------------------------------------------
CREATE TABLE energy_sources (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    source_type ENUM('Solar', 'Wind') NOT NULL,
    rated_capacity_kw DOUBLE NOT NULL,
    efficiency DOUBLE NULL,          -- solar only, 0.0 - 1.0
    cut_in_speed DOUBLE NULL,        -- wind only, m/s
    rated_speed DOUBLE NULL,         -- wind only, m/s
    cut_out_speed DOUBLE NULL,       -- wind only, m/s
    under_maintenance BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO energy_sources (name, source_type, rated_capacity_kw, efficiency, under_maintenance) VALUES
('Solar Array A', 'Solar', 500, 0.20, FALSE),
('Solar Array B', 'Solar', 750, 0.22, FALSE);

INSERT INTO energy_sources (name, source_type, rated_capacity_kw, cut_in_speed, rated_speed, cut_out_speed, under_maintenance) VALUES
('Wind Turbine 1', 'Wind', 1000, 3, 12, 25, FALSE),
('Wind Turbine 2', 'Wind', 1000, 3, 12, 25, FALSE);

-- Historical readings for energy sources (used for reporting/graphs)
CREATE TABLE energy_readings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id INT NOT NULL,
    reading_time DATETIME NOT NULL,
    environment_value DOUBLE NOT NULL, -- irradiance (W/m^2) or wind speed (m/s)
    output_kw DOUBLE NOT NULL,
    FOREIGN KEY (source_id) REFERENCES energy_sources(id) ON DELETE CASCADE,
    INDEX idx_energy_readings_time (reading_time)
);

-- ---------------------------------------------------------------------
-- Hydrogen production units (electrolyzers)
-- ---------------------------------------------------------------------
CREATE TABLE production_units (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    energy_consumption_rate_kwh_per_kg DOUBLE NOT NULL,
    max_throughput_kg_per_hour DOUBLE NOT NULL,
    under_maintenance BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO production_units (name, energy_consumption_rate_kwh_per_kg, max_throughput_kg_per_hour, under_maintenance) VALUES
('Electrolyzer Unit 1', 52, 25, FALSE),
('Electrolyzer Unit 2', 55, 20, FALSE);

-- Production history log
CREATE TABLE production_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id INT NOT NULL,
    log_time DATETIME NOT NULL,
    energy_consumed_kwh DOUBLE NOT NULL,
    hydrogen_produced_kg DOUBLE NOT NULL,
    FOREIGN KEY (unit_id) REFERENCES production_units(id) ON DELETE CASCADE,
    INDEX idx_production_log_time (log_time)
);

-- ---------------------------------------------------------------------
-- Storage tanks
-- ---------------------------------------------------------------------
CREATE TABLE storage_tanks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(100) NOT NULL,
    capacity_kg DOUBLE NOT NULL,
    current_level_kg DOUBLE NOT NULL DEFAULT 0,
    max_safe_pressure_bar DOUBLE NOT NULL
);

INSERT INTO storage_tanks (location, capacity_kg, current_level_kg, max_safe_pressure_bar) VALUES
('Tank Farm North', 2000, 350, 350),
('Tank Farm South', 1500, 900, 350);

-- ---------------------------------------------------------------------
-- Maintenance tasks
-- ---------------------------------------------------------------------
CREATE TABLE maintenance_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT NOT NULL,
    asset_name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(30) NOT NULL,
    scheduled_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    technician VARCHAR(100)
);

INSERT INTO maintenance_tasks (asset_id, asset_name, asset_type, scheduled_date, description, status, technician) VALUES
(1, 'Solar Array A', 'Solar', CURDATE() + INTERVAL 14 DAY, 'Panel cleaning and inspection', 'SCHEDULED', 'Unassigned');
