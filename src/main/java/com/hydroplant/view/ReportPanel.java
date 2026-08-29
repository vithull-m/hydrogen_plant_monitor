package com.hydroplant.view;

import com.hydroplant.controller.EnergyController;
import com.hydroplant.controller.MaintenanceController;
import com.hydroplant.controller.ReportController;
import com.hydroplant.controller.StorageController;
import com.hydroplant.model.EnergySource;
import com.hydroplant.model.MaintenanceTask;
import com.hydroplant.model.StorageTank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Lets the operator generate and preview a full operational report, then
 * save it to a text file on disk (File Handling demonstration).
 */
public class ReportPanel extends JPanel {

    private final ReportController reportController = new ReportController();
    private final EnergyController energyController = new EnergyController();
    private final StorageController storageController = new StorageController();
    private final MaintenanceController maintenanceController = new MaintenanceController();

    private JTextArea previewArea;
    private String lastGeneratedPath;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        buildUI();
    }

    private void buildUI() {
        JLabel header = new JLabel("Operational Reports");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        add(header, BorderLayout.NORTH);

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(previewArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton generateBtn = new JButton("Generate Report");
        JButton saveAsBtn = new JButton("Save As...");

        generateBtn.addActionListener(e -> generateReport());
        saveAsBtn.addActionListener(e -> saveAs());

        buttons.add(generateBtn);
        buttons.add(saveAsBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    private void generateReport() {
        try {
            List<EnergySource> sources = energyController.getAllSources();
            List<StorageTank> tanks = storageController.getAllTanks();
            List<MaintenanceTask> tasks = maintenanceController.getAllTasks();

            String fileName = "plant_report_" + System.currentTimeMillis() + ".txt";
            String outputPath = new File(System.getProperty("user.home"), fileName).getAbsolutePath();

            String report = reportController.generateOperationalReport(sources, tanks, tasks, outputPath);
            previewArea.setText(report);
            previewArea.setCaretPosition(0);
            lastGeneratedPath = outputPath;

            JOptionPane.showMessageDialog(this,
                    "Report generated and saved to:\n" + outputPath,
                    "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Database error while generating report:\n" + e.getMessage());
        } catch (IOException e) {
            showError("Could not write report file:\n" + e.getMessage());
        }
    }

    private void saveAs() {
        if (previewArea.getText().isBlank()) {
            showError("Generate a report first.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("plant_report.txt"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            java.nio.file.Files.writeString(chooser.getSelectedFile().toPath(), previewArea.getText());
            JOptionPane.showMessageDialog(this, "Saved to " + chooser.getSelectedFile().getAbsolutePath());
        } catch (IOException e) {
            showError("Could not save file:\n" + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
