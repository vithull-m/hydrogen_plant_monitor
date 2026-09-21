# Executable commands
mvn clean package
java -jar target\hydrogen-plant-monitor.jar
Start-Service MySQL80
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -u root -p < database\schema.sql

Username: admin
Password: admin123

<<<<<<< HEAD
# Green Hydrogen Plant Monitoring and Management System

A Java Swing desktop application that simulates monitoring and management
of a renewable-energy-powered green hydrogen production plant. Built as a
semester project demonstrating core Java concepts on top of an MVC
architecture with a MySQL/JDBC backend.

## Features

- **Dashboard** — live overview of current generation (kW), hydrogen
  produced today, total stored hydrogen, and assets under maintenance.
  Auto-refreshes every 15 seconds using a background thread.
- **Energy Sources** — add/remove solar panels and wind turbines, toggle
  maintenance mode, and view rated capacity and type-specific details.
- **Production** — run a production cycle that pulls current generation
  from all energy sources and feeds it through the electrolyzer units to
  produce hydrogen.
- **Storage Tanks** — manage tanks, deposit/withdraw hydrogen, and watch
  fill percentages update (throws a clear error rather than silently
  overflowing or under-drawing a tank).
- **Maintenance** — schedule maintenance for any asset (energy source or
  electrolyzer unit), mark tasks complete, cancel tasks.
- **Reports** — generate a plain-text operational report and save it to
  disk (or "Save As" to a location of your choice).

## Tech Stack

| Layer          | Technology                         |
|----------------|-------------------------------------|
| Language       | Java 17+                            |
| GUI            | Java Swing                          |
| Database       | MySQL 8                             |
| Connectivity   | JDBC (`mysql-connector-j`)          |
| Build          | Maven (also works directly in NetBeans/Eclipse) |
| Architecture   | MVC (Model / View / Controller, plus a DAO layer for persistence) |

## Core Java Concepts Demonstrated

| Concept              | Where                                                              |
|-----------------------|--------------------------------------------------------------------|
| Classes & Objects    | Throughout `model/`                                                |
| Encapsulation        | Private fields with validated getters/setters (e.g. `StorageTank`, `EnergySource`) |
| Inheritance          | `EnergySource` → `SolarPanel`, `WindTurbine`                       |
| Polymorphism         | `calculateOutput()` overridden differently per subtype; used polymorphically in `EnergyController` |
| Interfaces           | `Maintainable` implemented by `EnergySource` and `HydrogenProductionUnit` |
| Exception Handling   | Custom checked exceptions `InsufficientStorageException`, `InvalidOperationException`, plus JDBC `SQLException` handling throughout |
| Collections          | `List<>` used across DAOs/controllers; sorting with comparators in `EnergyController` |
| File Handling        | `ReportGenerator` writes operational reports to disk with `java.io` |
| JDBC                 | Full CRUD in `dao/` package via `PreparedStatement`                |
| Multithreading       | `DashboardUpdater` background thread refreshes the dashboard without blocking the UI |

## Project Structure

```
hydrogen-plant-monitor/
├── pom.xml
├── database/
│   └── schema.sql              -- MySQL schema + seed data
├── src/main/resources/
│   └── db.properties           -- DB connection settings (EDIT THIS)
└── src/main/java/com/hydroplant/
    ├── Main.java                -- application entry point
    ├── model/                   -- domain classes (EnergySource, StorageTank, ...)
    ├── dao/                     -- JDBC data-access classes
    ├── controller/               -- business logic tying model + DAO together
    ├── view/                     -- Swing panels/frames
    ├── util/                     -- ReportGenerator, DashboardUpdater
    └── exception/                -- custom checked exceptions
```

## Setup Instructions

### 1. Install prerequisites
- JDK 17 or newer
- MySQL Server 8.x (running locally or reachable over the network)
- Maven (or open the project directly in NetBeans, which bundles Maven support)

### 2. Create the database
```bash
mysql -u root -p < database/schema.sql
```
This creates the `hydrogen_plant` database, all tables, and some sample
seed data (a couple of solar/wind sources, two electrolyzer units, two
storage tanks) so the dashboard has something to show immediately.

### 3. Configure the connection
Edit `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/hydrogen_plant?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_actual_mysql_password
```

### 4. Build and run

**Using Maven from the command line:**
```bash
mvn clean package
java -jar target/hydrogen-plant-monitor.jar
```

**Using Maven's exec plugin (no jar needed):**
```bash
mvn compile exec:java
```

**Using NetBeans:**
1. `File → Open Project`, select the `hydrogen-plant-monitor` folder (NetBeans recognizes the `pom.xml` automatically).
2. Right-click the project → `Run`.
3. NetBeans will download `mysql-connector-j` automatically via Maven.

**Using Eclipse:**
1. `File → Import → Maven → Existing Maven Projects`, select the folder.
2. Right-click `Main.java` → `Run As → Java Application`.

### 5. Log in
The login screen uses a simple demo account (separate from the `users`
table, so the app is runnable even before you've set up the database):
```
Username: admin
Password: admin123
```

## Notes on the Simulation

Since this is a desktop simulation rather than a live SCADA/IoT feed,
`EnergyController.simulateEnvironmentValue()` generates plausible solar
irradiance (200–950 W/m²) and wind speed (0–20 m/s) readings each time
the dashboard refreshes or a production cycle is run. The underlying
physics formulas are real approximations:
- **Solar:** `output = (irradiance / 1000) × ratedCapacity × efficiency`
- **Wind:** cubic ramp-up between cut-in and rated speed, full output at/above rated speed, zero above cut-out (standard turbine power-curve model)
- **Electrolysis:** `hydrogen (kg) = energy (kWh) / consumption rate (kWh per kg)`, roughly 50–55 kWh/kg for real PEM/alkaline electrolyzers

## Extending the Project

Ideas if you want to go further for extra credit:
- Wire `energy_readings` and `production_log` history into a line chart (e.g. using `JFreeChart`).
- Add role-based access (`ADMIN` vs `OPERATOR`) using the existing `users` table and `User.Role` enum.
- Export reports as PDF instead of plain text.
- Replace the simulated environment values with a real weather API call.

## Known Simplifications (student-project scope)

- The login screen checks a hard-coded demo account rather than the
  `users` table, so the app runs even before the database is configured.
  Wiring it to `users` (with `SHA2(password, 256)` comparison, matching
  the schema) is a good exercise if your instructor wants full login
  integration.
- No connection pooling — each DAO call opens and closes its own
  connection, which is fine for a single-user desktop app at this scale
  but wouldn't scale to many concurrent users.
=======
# hydrogen_plant_monitor
A Java Swing desktop application (MVC + JDBC/MySQL) that simulates monitoring and managing a renewable-energy-powered green hydrogen production plant — tracking solar/wind generation, hydrogen production, storage tanks, and equipment maintenance.
>>>>>>> d4a00984cd08b70ee3a71c341ce14737e7e8d403
