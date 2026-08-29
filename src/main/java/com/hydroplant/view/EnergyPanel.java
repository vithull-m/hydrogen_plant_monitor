package com.hydroplant.view;

import com.hydroplant.controller.EnergyController;
import com.hydroplant.model.EnergySource;
import com.hydroplant.model.SolarPanel;
import com.hydroplant.model.WindTurbine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Management screen for solar panels and wind turbines: list, add, remove,
 * and toggle maintenance mode. Demonstrates polymorphism -- both subtypes
 * of EnergySource are displayed and manipulated through the same table.
 */
public class EnergyPanel extends JPanel {

    private final EnergyController controller = new EnergyController();
    private DefaultTableModel tableModel;
    private JTable table;

    public EnergyPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel header = new JLabel("Energy Sources (Solar & Wind)");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Type", "Rated (kW)", "Extra Info", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addSolarBtn = new JButton("Add Solar Panel");
        JButton addWindBtn = new JButton("Add Wind Turbine");
        JButton toggleMaintenanceBtn = new JButton("Toggle Maintenance");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addSolarBtn.addActionListener(e -> addSolarPanel());
        addWindBtn.addActionListener(e -> addWindTurbine());
        toggleMaintenanceBtn.addActionListener(e -> toggleMaintenance());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());

        buttons.add(addSolarBtn);
        buttons.add(addWindBtn);
        buttons.add(toggleMaintenanceBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    public void loadData() {
        try {
            List<EnergySource> sources = controller.getAllSources();
            tableModel.setRowCount(0);
            for (EnergySource s : sources) {
                String extra;
                if (s instanceof SolarPanel sp) {
                    extra = String.format("Efficiency: %.0f%%", sp.getEfficiency() * 100);
                } else if (s instanceof WindTurbine wt) {
                    extra = String.format("Cut-in %.1f / Rated %.1f / Cut-out %.1f m/s",
                            wt.getCutInSpeed(), wt.getRatedSpeed(), wt.getCutOutSpeed());
                } else {
                    extra = "";
                }
                tableModel.addRow(new Object[]{
                        s.getId(), s.getName(), s.getSourceType(),
                        String.format("%.1f", s.getRatedCapacityKw()),
                        extra,
                        s.isUnderMaintenance() ? "Under Maintenance" : "Online"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load energy sources:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSolarPanel() {
        JTextField nameField = new JTextField();
        JTextField capacityField = new JTextField("500");
        JTextField efficiencyField = new JTextField("0.20");

        Object[] fields = {
                "Name:", nameField,
                "Rated Capacity (kW):", capacityField,
                "Efficiency (0-1):", efficiencyField
        };
        int result = JOptionPane.showConfirmDialog(this, fields, "Add Solar Panel", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String name = nameField.getText().trim();
            double capacity = Double.parseDouble(capacityField.getText().trim());
            double efficiency = Double.parseDouble(efficiencyField.getText().trim());
            if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty");

            SolarPanel panel = new SolarPanel(0, name, capacity, efficiency);
            controller.addSource(panel);
            loadData();
        } catch (NumberFormatException ex) {
            showError("Capacity and efficiency must be valid numbers.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void addWindTurbine() {
        JTextField nameField = new JTextField();
        JTextField capacityField = new JTextField("1000");
        JTextField cutInField = new JTextField("3");
        JTextField ratedField = new JTextField("12");
        JTextField cutOutField = new JTextField("25");

        Object[] fields = {
                "Name:", nameField,
                "Rated Capacity (kW):", capacityField,
                "Cut-in Speed (m/s):", cutInField,
                "Rated Speed (m/s):", ratedField,
                "Cut-out Speed (m/s):", cutOutField
        };
        int result = JOptionPane.showConfirmDialog(this, fields, "Add Wind Turbine", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String name = nameField.getText().trim();
            double capacity = Double.parseDouble(capacityField.getText().trim());
            double cutIn = Double.parseDouble(cutInField.getText().trim());
            double rated = Double.parseDouble(ratedField.getText().trim());
            double cutOut = Double.parseDouble(cutOutField.getText().trim());
            if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
            if (!(cutIn < rated && rated < cutOut)) {
                throw new IllegalArgumentException("Speeds must satisfy cut-in < rated < cut-out");
            }

            WindTurbine turbine = new WindTurbine(0, name, capacity, cutIn, rated, cutOut);
            controller.addSource(turbine);
            loadData();
        } catch (NumberFormatException ex) {
            showError("Speed and capacity fields must be valid numbers.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void toggleMaintenance() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a source first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 5);
        boolean newState = !"Under Maintenance".equals(status);
        try {
            controller.setMaintenanceMode(id, newState);
            loadData();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a source first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this energy source permanently?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            controller.deleteSource(id);
            loadData();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
