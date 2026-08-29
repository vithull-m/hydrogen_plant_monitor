package com.hydroplant.view;

import com.hydroplant.controller.StorageController;
import com.hydroplant.exception.InsufficientStorageException;
import com.hydroplant.model.StorageTank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Management screen for storage tanks: view fill levels, add tanks,
 * manually deposit/withdraw hydrogen (e.g. dispatch to a customer truck).
 */
public class StoragePanel extends JPanel {

    private final StorageController controller = new StorageController();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<StorageTank> currentTanks;

    public StoragePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel header = new JLabel("Storage Tanks");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Location", "Current (kg)", "Capacity (kg)", "Fill %", "Max Pressure (bar)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Tank");
        JButton depositBtn = new JButton("Deposit Hydrogen");
        JButton withdrawBtn = new JButton("Withdraw Hydrogen");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> addTank());
        depositBtn.addActionListener(e -> depositHydrogen());
        withdrawBtn.addActionListener(e -> withdrawHydrogen());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());

        buttons.add(addBtn);
        buttons.add(depositBtn);
        buttons.add(withdrawBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadData() {
        try {
            currentTanks = controller.getAllTanks();
            tableModel.setRowCount(0);
            for (StorageTank t : currentTanks) {
                tableModel.addRow(new Object[]{
                        t.getId(), t.getLocation(),
                        String.format("%.2f", t.getCurrentLevelKg()),
                        String.format("%.2f", t.getCapacityKg()),
                        String.format("%.1f%%", t.getFillPercentage()),
                        String.format("%.1f", t.getMaxSafePressureBar())
                });
            }
        } catch (SQLException e) {
            showError("Could not load storage tanks:\n" + e.getMessage());
        }
    }

    private void addTank() {
        JTextField locationField = new JTextField();
        JTextField capacityField = new JTextField("1000");
        JTextField currentField = new JTextField("0");
        JTextField pressureField = new JTextField("350");

        Object[] fields = {
                "Location:", locationField,
                "Capacity (kg):", capacityField,
                "Current Level (kg):", currentField,
                "Max Safe Pressure (bar):", pressureField
        };
        int result = JOptionPane.showConfirmDialog(this, fields, "Add Storage Tank", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String location = locationField.getText().trim();
            double capacity = Double.parseDouble(capacityField.getText().trim());
            double current = Double.parseDouble(currentField.getText().trim());
            double pressure = Double.parseDouble(pressureField.getText().trim());
            if (location.isEmpty()) throw new IllegalArgumentException("Location cannot be empty");
            if (current > capacity) throw new IllegalArgumentException("Current level cannot exceed capacity");

            StorageTank tank = new StorageTank(0, location, capacity, current, pressure);
            controller.addTank(tank);
            loadData();
        } catch (NumberFormatException ex) {
            showError("Capacity, level, and pressure must be valid numbers.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void depositHydrogen() {
        StorageTank tank = getSelectedTank();
        if (tank == null) return;
        String input = JOptionPane.showInputDialog(this, "Amount to deposit (kg) into tank '" + tank.getLocation() + "':");
        if (input == null) return;
        try {
            double kg = Double.parseDouble(input.trim());
            controller.depositHydrogen(tank, kg);
            loadData();
        } catch (NumberFormatException ex) {
            showError("Enter a valid number.");
        } catch (InsufficientStorageException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void withdrawHydrogen() {
        StorageTank tank = getSelectedTank();
        if (tank == null) return;
        String input = JOptionPane.showInputDialog(this, "Amount to withdraw (kg) from tank '" + tank.getLocation() + "':");
        if (input == null) return;
        try {
            double kg = Double.parseDouble(input.trim());
            controller.withdrawHydrogen(tank, kg);
            loadData();
        } catch (NumberFormatException ex) {
            showError("Enter a valid number.");
        } catch (InsufficientStorageException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        StorageTank tank = getSelectedTank();
        if (tank == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this tank permanently?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            controller.deleteTank(tank.getId());
            loadData();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private StorageTank getSelectedTank() {
        int row = table.getSelectedRow();
        if (row < 0 || currentTanks == null || row >= currentTanks.size()) {
            showError("Select a tank first.");
            return null;
        }
        return currentTanks.get(row);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
