package com.hydroplant.view;

import com.hydroplant.controller.EnergyController;
import com.hydroplant.controller.ProductionController;
import com.hydroplant.controller.StorageController;
import com.hydroplant.exception.InvalidOperationException;
import com.hydroplant.model.EnergySource;
import com.hydroplant.model.HydrogenProductionUnit;
import com.hydroplant.model.StorageTank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Lets the operator run a production cycle: pulls current generation from
 * all energy sources, feeds it into the electrolyzer units, and stores the
 * resulting hydrogen across the available tanks. Demonstrates exception
 * handling for the InvalidOperationException business rule.
 */
public class ProductionPanel extends JPanel {

    private final EnergyController energyController = new EnergyController();
    private final ProductionController productionController = new ProductionController();
    private final StorageController storageController = new StorageController();

    private DefaultTableModel unitTableModel;
    private JTable unitTable;
    private JTextArea logArea;

    public ProductionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
        loadUnits();
    }

    private void buildUI() {
        JLabel header = new JLabel("Hydrogen Production");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));

        String[] columns = {"ID", "Name", "kWh/kg", "Max kg/h", "Status"};
        unitTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        unitTable = new JTable(unitTableModel);
        unitTable.setRowHeight(24);
        center.add(new JScrollPane(unitTable), BorderLayout.NORTH);

        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        center.add(new JScrollPane(logArea), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addUnitBtn = new JButton("Add Electrolyzer Unit");
        JButton runCycleBtn = new JButton("Run Production Cycle");
        JButton refreshBtn = new JButton("Refresh");

        addUnitBtn.addActionListener(e -> addUnit());
        runCycleBtn.addActionListener(e -> runCycle());
        refreshBtn.addActionListener(e -> loadUnits());

        buttons.add(addUnitBtn);
        buttons.add(runCycleBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadUnits() {
        try {
            List<HydrogenProductionUnit> units = productionController.getAllUnits();
            unitTableModel.setRowCount(0);
            for (HydrogenProductionUnit u : units) {
                unitTableModel.addRow(new Object[]{
                        u.getId(), u.getName(),
                        String.format("%.1f", u.getEnergyConsumptionRateKwhPerKg()),
                        String.format("%.1f", u.getMaxThroughputKgPerHour()),
                        u.isUnderMaintenance() ? "Under Maintenance" : "Online"
                });
            }
        } catch (SQLException e) {
            appendLog("ERROR loading units: " + e.getMessage());
        }
    }

    private void addUnit() {
        JTextField nameField = new JTextField();
        JTextField rateField = new JTextField("52");
        JTextField throughputField = new JTextField("20");

        Object[] fields = {
                "Name:", nameField,
                "Energy Rate (kWh per kg H2):", rateField,
                "Max Throughput (kg/hour):", throughputField
        };
        int result = JOptionPane.showConfirmDialog(this, fields, "Add Electrolyzer Unit", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String name = nameField.getText().trim();
            double rate = Double.parseDouble(rateField.getText().trim());
            double throughput = Double.parseDouble(throughputField.getText().trim());
            if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty");

            HydrogenProductionUnit unit = new HydrogenProductionUnit(0, name, rate, throughput);
            productionController.addUnit(unit);
            loadUnits();
        } catch (NumberFormatException ex) {
            appendLog("ERROR: rate and throughput must be valid numbers.");
        } catch (IllegalArgumentException ex) {
            appendLog("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            appendLog("Database error: " + ex.getMessage());
        }
    }

    private void runCycle() {
        try {
            List<EnergySource> sources = energyController.getAllSources();
            double totalKw = energyController.computeAndLogCurrentGeneration(sources);
            appendLog(String.format("Generated %.1f kW from %d energy source(s).", totalKw, sources.size()));

            List<HydrogenProductionUnit> units = productionController.getAllUnits();
            double producedKg = productionController.runProductionCycle(units, totalKw);
            appendLog(String.format("Produced %.2f kg of hydrogen this cycle.", producedKg));

            List<StorageTank> tanks = storageController.getAllTanks();
            double overflow = storageController.distributeProduction(tanks, producedKg);
            if (overflow > 0.001) {
                appendLog(String.format("WARNING: %.2f kg could not be stored - all tanks are full!", overflow));
            } else {
                appendLog("All produced hydrogen was successfully stored.");
            }
            loadUnits();
        } catch (InvalidOperationException ex) {
            appendLog("Cannot run production cycle: " + ex.getMessage());
        } catch (SQLException ex) {
            appendLog("Database error: " + ex.getMessage());
        }
    }

    private void appendLog(String message) {
        logArea.append("[" + java.time.LocalTime.now().withNano(0) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
