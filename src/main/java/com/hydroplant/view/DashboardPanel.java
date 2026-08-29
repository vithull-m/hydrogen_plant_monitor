package com.hydroplant.view;

import com.hydroplant.controller.EnergyController;
import com.hydroplant.controller.ProductionController;
import com.hydroplant.controller.StorageController;
import com.hydroplant.model.EnergySource;
import com.hydroplant.model.StorageTank;
import com.hydroplant.util.DashboardUpdater;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Live overview dashboard: total generation, hydrogen produced today,
 * storage levels, and maintenance alerts. Refreshes automatically via a
 * background DashboardUpdater thread (multithreading demonstration).
 */
public class DashboardPanel extends JPanel {

    private final EnergyController energyController = new EnergyController();
    private final ProductionController productionController = new ProductionController();
    private final StorageController storageController = new StorageController();

    private JLabel generationValueLabel;
    private JLabel productionValueLabel;
    private JLabel storageValueLabel;
    private JLabel maintenanceValueLabel;
    private JLabel statusLabel;
    private JProgressBar storageBar;

    private DashboardUpdater updater;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
        refreshData();
        startAutoRefresh();
    }

    private void buildUI() {
        JLabel header = new JLabel("Plant Overview Dashboard");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 2, 15, 15));

        generationValueLabel = new JLabel("-- kW");
        productionValueLabel = new JLabel("-- kg");
        storageValueLabel = new JLabel("-- kg");
        maintenanceValueLabel = new JLabel("--");

        cards.add(buildCard("Current Generation", generationValueLabel, new Color(46, 125, 50)));
        cards.add(buildCard("Hydrogen Produced Today", productionValueLabel, new Color(21, 101, 192)));
        cards.add(buildCard("Total Stored Hydrogen", storageValueLabel, new Color(230, 81, 0)));
        cards.add(buildCard("Assets Under Maintenance", maintenanceValueLabel, new Color(183, 28, 28)));

        add(cards, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(5, 5));
        JLabel storageLabel = new JLabel("Overall Storage Fill Level:");
        storageBar = new JProgressBar(0, 100);
        storageBar.setStringPainted(true);
        south.add(storageLabel, BorderLayout.NORTH);
        south.add(storageBar, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 11f));
        statusLabel.setForeground(Color.GRAY);
        south.add(statusLabel, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);
    }

    private JPanel buildCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2),
                new EmptyBorder(10, 10, 10, 10)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 13f));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 24f));
        valueLabel.setForeground(accent);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** Pulls fresh data from the controllers and updates the labels. */
    public void refreshData() {
        try {
            List<EnergySource> sources = energyController.getAllSources();
            double totalKw = energyController.computeAndLogCurrentGeneration(sources);
            generationValueLabel.setText(String.format("%.1f kW", totalKw));

            double producedToday = productionController.getTotalProductionToday();
            productionValueLabel.setText(String.format("%.2f kg", producedToday));

            List<StorageTank> tanks = storageController.getAllTanks();
            double stored = storageController.getTotalStoredKg(tanks);
            double capacity = storageController.getTotalCapacityKg(tanks);
            storageValueLabel.setText(String.format("%.2f kg", stored));
            int pct = capacity == 0 ? 0 : (int) Math.round((stored / capacity) * 100);
            storageBar.setValue(pct);
            storageBar.setString(pct + "% of " + String.format("%.0f", capacity) + " kg capacity");

            long underMaintenance = sources.stream().filter(EnergySource::isUnderMaintenance).count();
            maintenanceValueLabel.setText(String.valueOf(underMaintenance));

            statusLabel.setText("Last updated: " + java.time.LocalTime.now().withNano(0));
        } catch (SQLException e) {
            statusLabel.setText("Unable to reach database: " + e.getMessage());
        }
    }

    private void startAutoRefresh() {
        updater = new DashboardUpdater(this::refreshData, 15_000); // every 15s
        updater.start();
    }

    public void stopAutoRefresh() {
        if (updater != null) updater.stopUpdating();
    }
}
