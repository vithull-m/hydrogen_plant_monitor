package com.hydroplant.util;

import com.hydroplant.model.EnergySource;
import com.hydroplant.model.MaintenanceTask;
import com.hydroplant.model.StorageTank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builds plain-text operational reports and writes them to disk.
 * Demonstrates File Handling (java.io) as required by the project scope.
 */
public class ReportGenerator {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String buildReport(List<EnergySource> sources,
                               List<StorageTank> tanks,
                               List<MaintenanceTask> tasks,
                               double totalHydrogenToday,
                               LocalDateTime generatedAt) {

        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("  GREEN HYDROGEN PLANT - OPERATIONAL REPORT\n");
        sb.append("  Generated: ").append(generatedAt.format(TIMESTAMP_FMT)).append("\n");
        sb.append("=====================================================\n\n");

        sb.append("--- ENERGY SOURCES (").append(sources.size()).append(") ---\n");
        double totalRated = 0;
        int underMaintenanceCount = 0;
        for (EnergySource s : sources) {
            totalRated += s.getRatedCapacityKw();
            if (s.isUnderMaintenance()) underMaintenanceCount++;
            sb.append(String.format("  #%d %-20s %-6s rated %7.1f kW %s%n",
                    s.getId(), s.getName(), s.getSourceType(), s.getRatedCapacityKw(),
                    s.isUnderMaintenance() ? "[UNDER MAINTENANCE]" : "[ONLINE]"));
        }
        sb.append(String.format("  Total rated capacity: %.1f kW | Units under maintenance: %d%n%n",
                totalRated, underMaintenanceCount));

        sb.append("--- STORAGE TANKS (").append(tanks.size()).append(") ---\n");
        double totalStored = 0, totalCapacity = 0;
        for (StorageTank t : tanks) {
            totalStored += t.getCurrentLevelKg();
            totalCapacity += t.getCapacityKg();
            sb.append(String.format("  #%d %-15s %8.2f / %8.2f kg (%.1f%% full)%n",
                    t.getId(), t.getLocation(), t.getCurrentLevelKg(), t.getCapacityKg(), t.getFillPercentage()));
        }
        sb.append(String.format("  Total stored: %.2f kg / %.2f kg capacity (%.1f%%)%n%n",
                totalStored, totalCapacity, totalCapacity == 0 ? 0 : (totalStored / totalCapacity) * 100));

        sb.append("--- HYDROGEN PRODUCTION ---\n");
        sb.append(String.format("  Produced today: %.2f kg%n%n", totalHydrogenToday));

        sb.append("--- MAINTENANCE TASKS (").append(tasks.size()).append(") ---\n");
        for (MaintenanceTask t : tasks) {
            sb.append(String.format("  #%d %-20s [%s] scheduled %s - %s (tech: %s)%n",
                    t.getId(), t.getAssetName(), t.getStatus(), t.getScheduledDate(),
                    t.getDescription(), t.getTechnician()));
        }

        sb.append("\n=====================================================\n");
        return sb.toString();
    }

    /** Writes the report text to the given file path, creating/overwriting it. */
    public void saveToFile(String content, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }
}
