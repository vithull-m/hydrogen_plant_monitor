package com.hydroplant.view;

import com.hydroplant.controller.EnergyController;
import com.hydroplant.controller.MaintenanceController;
import com.hydroplant.controller.ProductionController;
import com.hydroplant.model.EnergySource;
import com.hydroplant.model.HydrogenProductionUnit;
import com.hydroplant.model.Maintainable;
import com.hydroplant.model.MaintenanceTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Maintenance scheduling screen. Lets the operator schedule maintenance
 * for any asset implementing Maintainable (energy sources or production
 * units), and mark tasks as completed.
 */
public class MaintenancePanel extends JPanel {

    private final MaintenanceController maintenanceController = new MaintenanceController();
    private final EnergyController energyController = new EnergyController();
    private final ProductionController productionController = new ProductionController();

    private DefaultTableModel tableModel;
    private JTable table;
    private List<MaintenanceTask> currentTasks;

    public MaintenancePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel header = new JLabel("Maintenance Scheduling");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Asset", "Type", "Scheduled", "Description", "Status", "Technician"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton scheduleBtn = new JButton("Schedule Maintenance");
        JButton completeBtn = new JButton("Mark Completed");
        JButton cancelBtn = new JButton("Cancel Task");
        JButton refreshBtn = new JButton("Refresh");

        scheduleBtn.addActionListener(e -> scheduleMaintenance());
        completeBtn.addActionListener(e -> completeSelected());
        cancelBtn.addActionListener(e -> cancelSelected());
        refreshBtn.addActionListener(e -> loadData());

        buttons.add(scheduleBtn);
        buttons.add(completeBtn);
        buttons.add(cancelBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            currentTasks = maintenanceController.getAllTasks();
            tableModel.setRowCount(0);
            for (MaintenanceTask t : currentTasks) {
                tableModel.addRow(new Object[]{
                        t.getId(), t.getAssetName(), t.getAssetType(),
                        t.getScheduledDate(), t.getDescription(), t.getStatus(), t.getTechnician()
                });
            }
        } catch (SQLException e) {
            showError("Could not load maintenance tasks:\n" + e.getMessage());
        }
    }

    private void scheduleMaintenance() {
        try {
            List<EnergySource> sources = energyController.getAllSources();
            List<HydrogenProductionUnit> units = productionController.getAllUnits();

            if (sources.isEmpty() && units.isEmpty()) {
                showError("No assets available. Add an energy source or electrolyzer unit first.");
                return;
            }

            // Build a combined list of asset display names mapped to Maintainable + type label
            java.util.List<Maintainable> assets = new java.util.ArrayList<>();
            java.util.List<String> assetLabels = new java.util.ArrayList<>();
            java.util.List<String> assetTypes = new java.util.ArrayList<>();

            for (EnergySource s : sources) {
                assets.add(s);
                assetLabels.add(s.getSourceType() + ": " + s.getName());
                assetTypes.add(s.getSourceType());
            }
            for (HydrogenProductionUnit u : units) {
                assets.add(u);
                assetLabels.add("Electrolyzer: " + u.getName());
                assetTypes.add("Electrolyzer");
            }

            JComboBox<String> assetCombo = new JComboBox<>(assetLabels.toArray(new String[0]));
            JTextField dateField = new JTextField(LocalDate.now().plusDays(7).toString());
            JTextField descField = new JTextField("Routine inspection");
            JTextField techField = new JTextField("Unassigned");

            Object[] fields = {
                    "Asset:", assetCombo,
                    "Scheduled Date (YYYY-MM-DD):", dateField,
                    "Description:", descField,
                    "Technician:", techField
            };
            int result = JOptionPane.showConfirmDialog(this, fields, "Schedule Maintenance", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            int idx = assetCombo.getSelectedIndex();
            Maintainable asset = assets.get(idx);
            String assetType = assetTypes.get(idx);
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            String description = descField.getText().trim();
            String technician = techField.getText().trim();

            maintenanceController.scheduleMaintenance(asset, assetType, date, description, technician);

            // Persist the maintenance flag on the underlying asset too
            if (asset instanceof EnergySource es) {
                energyController.setMaintenanceMode(es.getId(), true);
            }

            loadData();
        } catch (java.time.format.DateTimeParseException ex) {
            showError("Date must be in YYYY-MM-DD format.");
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void completeSelected() {
        MaintenanceTask task = getSelectedTask();
        if (task == null) return;
        try {
            maintenanceController.completeMaintenance(task, null);
            if (!"Electrolyzer".equals(task.getAssetType())) {
                energyController.setMaintenanceMode(task.getAssetId(), false);
            }
            loadData();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void cancelSelected() {
        MaintenanceTask task = getSelectedTask();
        if (task == null) return;
        try {
            maintenanceController.cancelTask(task.getId());
            loadData();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private MaintenanceTask getSelectedTask() {
        int row = table.getSelectedRow();
        if (row < 0 || currentTasks == null || row >= currentTasks.size()) {
            showError("Select a maintenance task first.");
            return null;
        }
        return currentTasks.get(row);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
