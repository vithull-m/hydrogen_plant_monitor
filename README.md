# Green Hydrogen Plant Monitor

A single-file Java console application that simulates monitoring and managing a renewable-energy-powered hydrogen production plant.

## Overview

This project is a plain Java console app, not a Swing GUI. It models:

- renewable energy sources such as solar panels and wind turbines
- hydrogen production using electrolyzers
- storage tank inventory and fill levels
- maintenance scheduling and tracking
- a simple operator dashboard and menu-driven workflow

## Features

- Dashboard summary with generation, stored hydrogen, and maintenance counts
- Add and remove solar and wind energy assets
- Toggle maintenance mode for assets
- Run hydrogen production calculations from available energy
- Deposit and withdraw hydrogen from storage tanks
- Schedule, complete, and cancel maintenance tasks
- Use a simple login flow with a demo admin account

## Tech Stack

- Language: Java
- Style: plain console application
- Build: `javac Main.java`
- Run: `java Main`

## Run It

```bash
javac Main.java
java Main
```

## Login

```text
Username: admin
Password: admin123
```

## Notes

The app is intentionally self-contained in a single `Main.java` file and avoids external libraries or GUI frameworks. It is designed as a lightweight Java simulation for learning and demonstration.
