package com.hydroplant.controller;

import com.hydroplant.model.EnergySource;
import com.hydroplant.model.MaintenanceTask;
import com.hydroplant.model.StorageTank;
import com.hydroplant.util.ReportGenerator;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pulls together data from the other controllers to build a plain-text
 * operational report and write it to disk (demonstrates File Handling).
 */
public class ReportController {

    private final ProductionController productionController = new ProductionController();
    private final ReportGenerator reportGenerator = new ReportGenerator();

    /**
     * Builds a full operational report (energy sources, storage, maintenance,
     * production) and saves it to the given file path.
     */
    public String generateOperationalReport(List<EnergySource> sources,
                                             List<StorageTank> tanks,
                                             List<MaintenanceTask> tasks,
                                             String outputFilePath) throws IOException, SQLException {

        double totalToday = productionController.getTotalProductionToday();
        String report = reportGenerator.buildReport(sources, tanks, tasks, totalToday, LocalDateTime.now());
        reportGenerator.saveToFile(report, outputFilePath);
        return report;
    }
}
