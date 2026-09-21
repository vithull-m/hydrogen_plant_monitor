import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.*;

// ?????????????????????????????????????????????????????????????????????????????
//  GREEN HYDROGEN PLANT MONITOR  ?  Console Edition
//  Plain Java | No JDBC | No Swing | No external libraries
//
//  Compile :  javac Main.java
//  Run     :  java Main
//  Login   :  username = admin   password = admin123
// ?????????????????????????????????????????????????????????????????????????????
public class Main {

    // ???????????????????????????????????????????????????????????
    //  CUSTOM EXCEPTIONS
    // ???????????????????????????????????????????????????????????

    static class InsufficientStorageException extends Exception {
        public InsufficientStorageException(String message) { super(message); }
    }

    static class InvalidOperationException extends Exception {
        public InvalidOperationException(String message) { super(message); }
    }

    // ???????????????????????????????????????????????????????????
    //  INTERFACE
    // ???????????????????????????????????????????????????????????

    interface Maintainable {
        void scheduleMaintenance(String description);
        void completeMaintenance();
        boolean isUnderMaintenance();
        String getAssetName();
    }

    // ???????????????????????????????????????????????????????????
    //  MODEL : EnergySource  (abstract base class)
    // ???????????????????????????????????????????????????????????

    static abstract class EnergySource implements Maintainable {
        private static int counter = 1;
        protected int    id;
        protected String name;
        protected double ratedCapacity;
        protected boolean underMaintenance;

        public EnergySource(String name, double ratedCapacity) {
            this.id               = counter++;
            this.name             = name;
            this.ratedCapacity    = ratedCapacity;
            this.underMaintenance = false;
        }

        public abstract double calculateOutput();
        public abstract String getType();

        @Override public void   scheduleMaintenance(String d) { this.underMaintenance = true; }
        @Override public void   completeMaintenance()         { this.underMaintenance = false; }
        @Override public boolean isUnderMaintenance()         { return underMaintenance; }
        @Override public String  getAssetName()               { return name; }

        public int    getId()            { return id; }
        public String getName()          { return name; }
        public double getRatedCapacity() { return ratedCapacity; }

        @Override
        public String toString() {
            double output = underMaintenance ? 0 : calculateOutput();
            return String.format("[%2d] %-22s | %-6s | Cap: %6.1f kW | Out: %6.1f kW | Maint: %s",
                    id, name, getType(), ratedCapacity, output, underMaintenance ? "YES" : "No");
        }
    }

    // ???????????????????????????????????????????????????????????
    //  MODEL : SolarPanel  (extends EnergySource)
    // ???????????????????????????????????????????????????????????

    static class SolarPanel extends EnergySource {
        private double panelArea;
        private double efficiency;
        private static final Random rng = new Random();

        public SolarPanel(String name, double ratedCapacity, double panelArea, double efficiency) {
            super(name, ratedCapacity);
            this.panelArea  = panelArea;
            this.efficiency = efficiency;
        }

        @Override
        public double calculateOutput() {
            if (underMaintenance) return 0;
            double irradiance = 200 + rng.nextDouble() * 750;
            return Math.min((irradiance / 1000.0) * ratedCapacity * efficiency, ratedCapacity);
        }

        @Override public String getType()       { return "Solar"; }
        public double getPanelArea()            { return panelArea; }
        public double getEfficiency()           { return efficiency; }
    }

    // ???????????????????????????????????????????????????????????
    //  MODEL : WindTurbine  (extends EnergySource)
    // ???????????????????????????????????????????????????????????

    static class WindTurbine extends EnergySource {
        private double cutInSpeed;
        private double ratedSpeed;
        private double cutOutSpeed;
        private static final Random rng = new Random();

        public WindTurbine(String name, double ratedCapacity,
                           double cutInSpeed, double ratedSpeed, double cutOutSpeed) {
            super(name, ratedCapacity);
            this.cutInSpeed  = cutInSpeed;
            this.ratedSpeed  = ratedSpeed;
            this.cutOutSpeed = cutOutSpeed;
        }

        @Override
        public double calculateOutput() {
            if (underMaintenance) return 0;
            double wind = rng.nextDouble() * 20;
            if (wind < cutInSpeed || wind > cutOutSpeed) return 0;
            if (wind >= ratedSpeed) return ratedCapacity;
            return ratedCapacity * Math.pow((wind - cutInSpeed) / (ratedSpeed - cutInSpeed), 3);
        }

        @Override public String getType()  { return "Wind"; }
        public double getCutInSpeed()      { return cutInSpeed; }
        public double getRatedSpeed()      { return ratedSpeed; }
        public double getCutOutSpeed()     { return cutOutSpeed; }
    }

    // ???????????????????????????????????????????????????????????
    //  MODEL : StorageTank
    // ???????????????????????????????????????????????????????????

    static class StorageTank {
        private static int counter = 1;
        private int    id;
        private String name;
        private double capacity;
        private double stored;

        public StorageTank(String name, double capacity) {
            this.id       = counter++;
            this.name     = name;
            this.capacity = capacity;
            this.stored   = 0;
        }

        public void deposit(double amount) throws InsufficientStorageException {
            if (stored + amount > capacity)
                throw new InsufficientStorageException(String.format(
                    "Cannot deposit %.2f kg -- only %.2f kg of space left in '%s'.",
                    amount, capacity - stored, name));
            stored += amount;
        }

        public void withdraw(double amount) throws InsufficientStorageException {
            if (amount > stored)
                throw new InsufficientStorageException(String.format(
                    "Cannot withdraw %.2f kg -- only %.2f kg available in '%s'.",
                    amount, stored, name));
            stored -= amount;
        }

        public int    getId()          { return id; }
        public String getName()        { return name; }
        public double getCapacity()    { return capacity; }
        public double getStored()      { return stored; }
        public double getFillPercent() { return (stored / capacity) * 100.0; }

        @Override
        public String toString() {
            return String.format("[%2d] %-22s | Stored: %7.2f / %7.2f kg | Fill: %5.1f%%",
                    id, name, stored, capacity, getFillPercent());
        }
    }

    // ???????????????????????????????????????????????????????????
    //  MODEL : HydrogenProductionUnit  (Electrolyzer)
    // ???????????????????????????????????????????????????????????

    static class HydrogenProductionUnit implements Maintainable {
        private static int counter = 1;
        private int    id;
        private String name;
        private double consumptionRate;
        private boolean underMaintenance;

        public HydrogenProductionUnit(String name, double consumptionRate) {
            this.id               = counter++;
            this.name             = name;
            this.consumptionRate  = consumptionRate;
            this.underMaintenance = false;
        }

        public double produceHydrogen(double energyKwh) throws InvalidOperationException {
            if (underMaintenance)
                throw new InvalidOperationException(
                    "Electrolyzer '" + name + "' is under maintenance and cannot produce.");
            return energyKwh / consumptionRate;
        }

        @Override public void   scheduleMaintenance(String d) { this.underMaintenance = true; }
        @Override public void   completeMaintenance()         { this.underMaintenance = false; }
        @Override public boolean isUnderMaintenance()         { return underMaintenance; }
        @Override public String  getAssetName()               { return name; }

        public int    getId()              { return id; }
        public String getName()            { return name; }
        public double getConsumptionRate() { return consumptionRate; }

        @Override
        public String toString() {
            return String.format("[%2d] %-22s | Rate: %.1f kWh/kg | Maint: %s",
                    id, name, consumptionRate, underMaintenance ? "YES" : "No");
        }
    }

    // ???????????????????????????????????????????????????????????
    //  MODEL : MaintenanceTask
    // ???????????????????????????????????????????????????????????

    static class MaintenanceTask {
        private static int counter = 1;
        private int    id;
        private String assetName;
        private String description;
        private String status;
        private String scheduledDate;

        public MaintenanceTask(String assetName, String description) {
            this.id            = counter++;
            this.assetName     = assetName;
            this.description   = description;
            this.status        = "PENDING";
            this.scheduledDate = LocalDate.now().toString();
        }

        public void complete() { status = "COMPLETE";  }
        public void cancel()   { status = "CANCELLED"; }

        public int    getId()        { return id; }
        public String getStatus()    { return status; }
        public String getAssetName() { return assetName; }

        @Override
        public String toString() {
            return String.format("[%2d] %-22s | %-10s | %s | %s",
                    id, assetName, status, scheduledDate, description);
        }
    }

    // ???????????????????????????????????????????????????????????
    //  IN-MEMORY DATA STORE
    // ???????????????????????????????????????????????????????????

    static List<EnergySource>           energySources    = new ArrayList<>();
    static List<StorageTank>            tanks            = new ArrayList<>();
    static List<HydrogenProductionUnit> electrolyzers    = new ArrayList<>();
    static List<MaintenanceTask>        maintenanceTasks = new ArrayList<>();
    static double totalH2ProducedToday = 0;

    static Scanner sc = new Scanner(System.in);

    static void seedData() {
        energySources.add(new SolarPanel  ("Solar Array Alpha",  500, 1000, 0.18));
        energySources.add(new SolarPanel  ("Solar Array Beta",   300,  600, 0.20));
        energySources.add(new WindTurbine ("Wind Turbine W1",    250, 3.0, 12.0, 25.0));
        energySources.add(new WindTurbine ("Wind Turbine W2",    200, 3.5, 13.0, 25.0));

        tanks.add(new StorageTank("Tank Alpha", 500));
        tanks.add(new StorageTank("Tank Beta",  300));

        electrolyzers.add(new HydrogenProductionUnit("Electrolyzer Unit 1", 50.0));
        electrolyzers.add(new HydrogenProductionUnit("Electrolyzer Unit 2", 55.0));

        try { tanks.get(0).deposit(120); tanks.get(1).deposit(80); }
        catch (InsufficientStorageException ignored) {}
    }

    // ???????????????????????????????????????????????????????????
    //  UTILITY HELPERS
    // ???????????????????????????????????????????????????????????

    static void line()           { System.out.println("-".repeat(62)); }
    static void header(String t) { System.out.println(); line(); System.out.println("  " + t); line(); }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Please enter a valid integer."); }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Please enter a valid number."); }
        }
    }

    static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // ???????????????????????????????????????????????????????????
    //  LOGIN
    // ???????????????????????????????????????????????????????????

    static boolean login() {
        header("GREEN HYDROGEN PLANT MONITOR  -  Console Edition");
        for (int attempt = 1; attempt <= 3; attempt++) {
            String user = readString("  Username : ");
            String pass = readString("  Password : ");
            if (user.equals("admin") && pass.equals("admin123")) {
                System.out.println("\n  Login successful. Welcome, " + user + "!\n");
                return true;
            }
            System.out.println("  Invalid credentials. Attempts remaining: " + (3 - attempt) + "\n");
        }
        System.out.println("  Too many failed attempts. Exiting.");
        return false;
    }

    // ???????????????????????????????????????????????????????????
    //  1. DASHBOARD
    // ???????????????????????????????????????????????????????????

    static void showDashboard() {
        header("DASHBOARD");
        double currentGen  = energySources.stream().filter(e -> !e.isUnderMaintenance()).mapToDouble(EnergySource::calculateOutput).sum();
        double totalStored = tanks.stream().mapToDouble(StorageTank::getStored).sum();
        long   assetsInMaint = energySources.stream().filter(EnergySource::isUnderMaintenance).count()
                             + electrolyzers.stream().filter(HydrogenProductionUnit::isUnderMaintenance).count();

        System.out.printf("  Current Generation     :  %8.2f kW%n", currentGen);
        System.out.printf("  H2 Produced Today      :  %8.2f kg%n", totalH2ProducedToday);
        System.out.printf("  Total H2 Stored        :  %8.2f kg%n", totalStored);
        System.out.printf("  Assets in Maintenance  :  %8d%n",      assetsInMaint);
        System.out.printf("  Energy Sources         :  %8d%n",      energySources.size());
        System.out.printf("  Storage Tanks          :  %8d%n",      tanks.size());
        System.out.printf("  Electrolyzers          :  %8d%n",      electrolyzers.size());
        System.out.printf("  Time                   :  %s%n",
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        line();
    }

    // ???????????????????????????????????????????????????????????
    //  2. ENERGY SOURCES
    // ???????????????????????????????????????????????????????????

    static void energyMenu() {
        while (true) {
            header("ENERGY SOURCES");
            if (energySources.isEmpty()) System.out.println("  (no energy sources added yet)");
            else energySources.forEach(e -> System.out.println("  " + e));
            line();
            System.out.println("  1. Add Solar Panel");
            System.out.println("  2. Add Wind Turbine");
            System.out.println("  3. Toggle Maintenance on a Source");
            System.out.println("  4. Remove Energy Source");
            System.out.println("  0. Back");
            line();

            switch (readInt("  Choice: ")) {
                case 1  -> addSolarPanel();
                case 2  -> addWindTurbine();
                case 3  -> toggleEnergyMaintenance();
                case 4  -> removeEnergySource();
                case 0  -> { return; }
                default -> System.out.println("  Invalid option.");
            }
        }
    }

    static void addSolarPanel() {
        System.out.println("\n  -- Add Solar Panel --");
        String name     = readString ("  Name                : ");
        double capacity = readDouble ("  Rated Capacity (kW) : ");
        double area     = readDouble ("  Panel Area (m2)     : ");
        double eff      = readDouble ("  Efficiency (0-1)    : ");
        energySources.add(new SolarPanel(name, capacity, area, eff));
        System.out.println("  Solar Panel '" + name + "' added.\n");
    }

    static void addWindTurbine() {
        System.out.println("\n  -- Add Wind Turbine --");
        String name     = readString ("  Name                   : ");
        double capacity = readDouble ("  Rated Capacity (kW)    : ");
        double cutIn    = readDouble ("  Cut-in Speed  (m/s)    : ");
        double rated    = readDouble ("  Rated Speed   (m/s)    : ");
        double cutOut   = readDouble ("  Cut-out Speed (m/s)    : ");
        energySources.add(new WindTurbine(name, capacity, cutIn, rated, cutOut));
        System.out.println("  Wind Turbine '" + name + "' added.\n");
    }

    static void toggleEnergyMaintenance() {
        int id = readInt("  Enter Source ID to toggle maintenance: ");
        energySources.stream().filter(e -> e.getId() == id).findFirst()
                .ifPresentOrElse(e -> {
                    if (e.isUnderMaintenance()) { e.completeMaintenance();          System.out.println("  Maintenance completed for: " + e.getName()); }
                    else                        { e.scheduleMaintenance("Manual");  System.out.println("  Maintenance scheduled for: " + e.getName()); }
                }, () -> System.out.println("  No source found with ID " + id));
    }

    static void removeEnergySource() {
        int id = readInt("  Enter Source ID to remove: ");
        System.out.println(energySources.removeIf(e -> e.getId() == id) ? "  Removed." : "  ID not found.");
    }

    // ???????????????????????????????????????????????????????????
    //  3. PRODUCTION
    // ???????????????????????????????????????????????????????????

    static void productionMenu() {
        while (true) {
            header("PRODUCTION  (Electrolyzers)");
            if (electrolyzers.isEmpty()) System.out.println("  (no electrolyzers added yet)");
            else electrolyzers.forEach(e -> System.out.println("  " + e));
            line();
            System.out.println("  1. Run Production Cycle");
            System.out.println("  2. Add Electrolyzer");
            System.out.println("  3. Toggle Electrolyzer Maintenance");
            System.out.println("  0. Back");
            line();

            switch (readInt("  Choice: ")) {
                case 1  -> runProductionCycle();
                case 2  -> addElectrolyzer();
                case 3  -> toggleElectrolyzerMaintenance();
                case 0  -> { return; }
                default -> System.out.println("  Invalid option.");
            }
        }
    }

    static void runProductionCycle() {
        System.out.println("\n  -- Running Production Cycle --\n");

        double totalKw = 0;
        for (EnergySource src : energySources) {
            double out = src.isUnderMaintenance() ? 0 : src.calculateOutput();
            System.out.printf("  %-24s : %6.2f kW%n", src.getName(), out);
            totalKw += out;
        }
        System.out.printf("%n  Total Energy Available  : %.2f kW%n", totalKw);

        if (totalKw == 0) { System.out.println("  No energy available. Cannot produce hydrogen.\n"); return; }

        long activeElec = electrolyzers.stream().filter(e -> !e.isUnderMaintenance()).count();
        if (activeElec == 0) { System.out.println("  All electrolyzers are under maintenance.\n"); return; }

        double sharePerUnit = totalKw / activeElec;
        double totalH2 = 0;
        System.out.println();

        for (HydrogenProductionUnit unit : electrolyzers) {
            try {
                double h2 = unit.produceHydrogen(sharePerUnit);
                System.out.printf("  %-24s -> %6.2f kg H2%n", unit.getName(), h2);
                totalH2 += h2;
            } catch (InvalidOperationException e) {
                System.out.println("  [WARN] " + e.getMessage());
            }
        }

        System.out.printf("%n  H2 Produced This Cycle  : %.2f kg%n", totalH2);
        totalH2ProducedToday += totalH2;

        double remaining = totalH2;
        System.out.println();
        for (StorageTank tank : tanks) {
            if (remaining <= 0) break;
            double space   = tank.getCapacity() - tank.getStored();
            double deposit = Math.min(remaining, space);
            try {
                tank.deposit(deposit);
                System.out.printf("  Deposited %.2f kg -> '%s'  (%.1f%% full)%n",
                        deposit, tank.getName(), tank.getFillPercent());
                remaining -= deposit;
            } catch (InsufficientStorageException e) {
                System.out.println("  [WARN] " + e.getMessage());
            }
        }
        if (remaining > 0.001)
            System.out.printf("%n  [WARN] %.2f kg H2 could not be stored -- all tanks full!%n", remaining);
        System.out.println();
    }

    static void addElectrolyzer() {
        String name = readString ("  Name                       : ");
        double rate = readDouble ("  Consumption Rate (kWh/kg)  : ");
        electrolyzers.add(new HydrogenProductionUnit(name, rate));
        System.out.println("  Electrolyzer '" + name + "' added.\n");
    }

    static void toggleElectrolyzerMaintenance() {
        int id = readInt("  Enter Electrolyzer ID: ");
        electrolyzers.stream().filter(e -> e.getId() == id).findFirst()
                .ifPresentOrElse(e -> {
                    if (e.isUnderMaintenance()) { e.completeMaintenance();         System.out.println("  Maintenance completed for: " + e.getName()); }
                    else                        { e.scheduleMaintenance("Manual"); System.out.println("  Maintenance scheduled for: " + e.getName()); }
                }, () -> System.out.println("  ID not found."));
    }

    // ???????????????????????????????????????????????????????????
    //  4. STORAGE TANKS
    // ???????????????????????????????????????????????????????????

    static void storageMenu() {
        while (true) {
            header("STORAGE TANKS");
            if (tanks.isEmpty()) System.out.println("  (no tanks added yet)");
            else tanks.forEach(t -> System.out.println("  " + t));
            line();
            System.out.println("  1. Add Tank");
            System.out.println("  2. Deposit Hydrogen");
            System.out.println("  3. Withdraw Hydrogen");
            System.out.println("  4. Remove Tank");
            System.out.println("  0. Back");
            line();

            switch (readInt("  Choice: ")) {
                case 1  -> addTank();
                case 2  -> depositHydrogen();
                case 3  -> withdrawHydrogen();
                case 4  -> removeTank();
                case 0  -> { return; }
                default -> System.out.println("  Invalid option.");
            }
        }
    }

    static void addTank() {
        String name = readString ("  Tank Name        : ");
        double cap  = readDouble ("  Capacity (kg H2) : ");
        tanks.add(new StorageTank(name, cap));
        System.out.println("  Tank '" + name + "' added.\n");
    }

    static void depositHydrogen() {
        int id = readInt("  Tank ID  : ");
        tanks.stream().filter(t -> t.getId() == id).findFirst()
                .ifPresentOrElse(t -> {
                    double amt = readDouble("  Amount (kg) : ");
                    try {
                        t.deposit(amt);
                        System.out.printf("  Deposited %.2f kg. Tank now %.2f kg (%.1f%% full).%n",
                                amt, t.getStored(), t.getFillPercent());
                    } catch (InsufficientStorageException e) { System.out.println("  " + e.getMessage()); }
                }, () -> System.out.println("  Tank ID not found."));
    }

    static void withdrawHydrogen() {
        int id = readInt("  Tank ID  : ");
        tanks.stream().filter(t -> t.getId() == id).findFirst()
                .ifPresentOrElse(t -> {
                    double amt = readDouble("  Amount (kg) : ");
                    try {
                        t.withdraw(amt);
                        System.out.printf("  Withdrawn %.2f kg. Tank now %.2f kg (%.1f%% full).%n",
                                amt, t.getStored(), t.getFillPercent());
                    } catch (InsufficientStorageException e) { System.out.println("  " + e.getMessage()); }
                }, () -> System.out.println("  Tank ID not found."));
    }

    static void removeTank() {
        int id = readInt("  Tank ID to remove: ");
        System.out.println(tanks.removeIf(t -> t.getId() == id) ? "  Removed." : "  ID not found.");
    }

    // ???????????????????????????????????????????????????????????
    //  5. MAINTENANCE
    // ???????????????????????????????????????????????????????????

    static void maintenanceMenu() {
        while (true) {
            header("MAINTENANCE TASKS");
            if (maintenanceTasks.isEmpty()) System.out.println("  (no tasks scheduled)");
            else maintenanceTasks.forEach(t -> System.out.println("  " + t));
            line();
            System.out.println("  1. Schedule New Task");
            System.out.println("  2. Mark Task Complete");
            System.out.println("  3. Cancel Task");
            System.out.println("  0. Back");
            line();

            switch (readInt("  Choice: ")) {
                case 1  -> scheduleTask();
                case 2  -> completeTask();
                case 3  -> cancelTask();
                case 0  -> { return; }
                default -> System.out.println("  Invalid option.");
            }
        }
    }

    static void scheduleTask() {
        System.out.println("\n  Available Assets:");
        energySources.forEach(e -> System.out.println("    [Energy]  " + e.getName()));
        electrolyzers.forEach(e -> System.out.println("    [Electro] " + e.getName()));

        String asset = readString("  Asset Name  : ");
        String desc  = readString("  Description : ");

        maintenanceTasks.add(new MaintenanceTask(asset, desc));

        energySources.stream().filter(e -> e.getName().equalsIgnoreCase(asset)).forEach(e -> e.scheduleMaintenance(desc));
        electrolyzers.stream().filter(e -> e.getName().equalsIgnoreCase(asset)).forEach(e -> e.scheduleMaintenance(desc));

        System.out.println("  Maintenance task scheduled for '" + asset + "'.\n");
    }

    static void completeTask() {
        int id = readInt("  Task ID to mark complete: ");
        maintenanceTasks.stream().filter(t -> t.getId() == id).findFirst()
                .ifPresentOrElse(t -> {
                    t.complete();
                    energySources.stream().filter(e -> e.getName().equalsIgnoreCase(t.getAssetName())).forEach(EnergySource::completeMaintenance);
                    electrolyzers.stream().filter(e -> e.getName().equalsIgnoreCase(t.getAssetName())).forEach(HydrogenProductionUnit::completeMaintenance);
                    System.out.println("  Task complete. Asset back online.");
                }, () -> System.out.println("  Task ID not found."));
    }

    static void cancelTask() {
        int id = readInt("  Task ID to cancel: ");
        maintenanceTasks.stream().filter(t -> t.getId() == id).findFirst()
                .ifPresentOrElse(t -> { t.cancel(); System.out.println("  Task cancelled."); },
                                 () -> System.out.println("  Task ID not found."));
    }

    // ???????????????????????????????????????????????????????????
    //  6. REPORT GENERATOR  (File I/O)
    // ???????????????????????????????????????????????????????????

    static void generateReport() {
        header("GENERATE REPORT");
        String filename = "hydrogen_report_" + LocalDate.now() + ".txt";
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(62)).append("\n");
        sb.append("  GREEN HYDROGEN PLANT -- OPERATIONAL REPORT\n");
        sb.append("  Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("=".repeat(62)).append("\n\n");

        sb.append("ENERGY SOURCES\n").append("-".repeat(62)).append("\n");
        if (energySources.isEmpty()) sb.append("  (none)\n");
        else energySources.forEach(e -> sb.append("  ").append(e).append("\n"));

        sb.append("\nSTORAGE TANKS\n").append("-".repeat(62)).append("\n");
        if (tanks.isEmpty()) sb.append("  (none)\n");
        else tanks.forEach(t -> sb.append("  ").append(t).append("\n"));

        sb.append("\nELECTROLYZERS\n").append("-".repeat(62)).append("\n");
        if (electrolyzers.isEmpty()) sb.append("  (none)\n");
        else electrolyzers.forEach(e -> sb.append("  ").append(e).append("\n"));

        sb.append("\nMAINTENANCE TASKS\n").append("-".repeat(62)).append("\n");
        if (maintenanceTasks.isEmpty()) sb.append("  (none)\n");
        else maintenanceTasks.forEach(t -> sb.append("  ").append(t).append("\n"));

        double totalStored = tanks.stream().mapToDouble(StorageTank::getStored).sum();
        sb.append("\nSUMMARY\n").append("-".repeat(62)).append("\n");
        sb.append(String.format("  H2 Produced Today  : %.2f kg%n", totalH2ProducedToday));
        sb.append(String.format("  Total H2 Stored    : %.2f kg%n", totalStored));
        sb.append(String.format("  Energy Sources     : %d%n",      energySources.size()));
        sb.append(String.format("  Electrolyzers      : %d%n",      electrolyzers.size()));
        sb.append(String.format("  Storage Tanks      : %d%n",      tanks.size()));
        sb.append(String.format("  Maintenance Tasks  : %d%n",      maintenanceTasks.size()));
        sb.append("=".repeat(62)).append("\n");

        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(sb.toString());
            System.out.println("  Report saved to: " + filename);
        } catch (IOException e) {
            System.out.println("  Failed to write report: " + e.getMessage());
        }
    }

    // ???????????????????????????????????????????????????????????
    //  MAIN ENTRY POINT
    // ???????????????????????????????????????????????????????????

    public static void main(String[] args) {
        seedData();
        if (!login()) return;

        while (true) {
            header("MAIN MENU");
            System.out.println("  1. Dashboard");
            System.out.println("  2. Energy Sources");
            System.out.println("  3. Production");
            System.out.println("  4. Storage Tanks");
            System.out.println("  5. Maintenance");
            System.out.println("  6. Generate Report");
            System.out.println("  0. Exit");
            line();

            switch (readInt("  Choice: ")) {
                case 1  -> showDashboard();
                case 2  -> energyMenu();
                case 3  -> productionMenu();
                case 4  -> storageMenu();
                case 5  -> maintenanceMenu();
                case 6  -> generateReport();
                case 0  -> { System.out.println("\n  Goodbye!\n"); return; }
                default -> System.out.println("  Invalid option.\n");
            }
        }
    }
}

