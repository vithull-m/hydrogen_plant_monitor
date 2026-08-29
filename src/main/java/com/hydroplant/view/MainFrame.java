package com.hydroplant.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Top-level application window. Hosts all feature panels in a JTabbedPane
 * and owns the DashboardPanel's background refresh thread lifecycle.
 */
public class MainFrame extends JFrame {

    private DashboardPanel dashboardPanel;

    public MainFrame() {
        super("Green Hydrogen Plant Monitoring and Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        buildUI();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dashboardPanel.stopAutoRefresh();
                dispose();
                System.exit(0);
            }
        });
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();

        dashboardPanel = new DashboardPanel();
        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Energy Sources", new EnergyPanel());
        tabs.addTab("Production", new ProductionPanel());
        tabs.addTab("Storage Tanks", new StoragePanel());
        tabs.addTab("Maintenance", new MaintenancePanel());
        tabs.addTab("Reports", new ReportPanel());

        // Refresh dashboard whenever the user switches back to it
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == dashboardPanel) {
                dashboardPanel.refreshData();
            }
        });

        setContentPane(tabs);
    }
}
