# Railway Logistics Management System

**Academic group project | 3rd Semester (2025/2026) | Computer Engineering — ISEP**

An integrated system for **railway operations and warehouse logistics**, developed as part of the Computer Engineering degree at the Instituto Superior de Engenharia do Porto (ISEP). The project combines Java application development, graph algorithms, data management, Oracle SQL/PLSQL, and low-level programming in C and RISC-V Assembly.

## Overview

The project brings together several areas of railway and logistics management:

- **Warehouse operations:** import inventory data, process incoming wagons, manage orders, generate picking plans, and handle returns and quarantine.
- **Railway network analysis:** query stations geographically, analyse network connectivity and centrality, calculate risk-aware paths, and identify network improvement opportunities.
- **Railway operations:** calculate travel times, assign freight, assemble trains, plan routes, and manage train traffic and schedules.
- **Low-level systems:** C and RISC-V Assembly components for railway station management, including sensor and buffer-related operations.

The repository contains both a **console application** and a **JavaFX graphical interface**, alongside sample datasets, automated tests, database scripts, and project documentation.

## Technologies

| Area | Technologies |
| --- | --- |
| Application development | Java 17, JavaFX, Maven |
| Algorithms and data structures | Graph algorithms, spatial indexing (2D/KD-tree) |
| Databases | Oracle Database, SQL, PL/SQL, JDBC |
| Low-level programming | C, RISC-V Assembly |
| Testing | JUnit, C/Assembly test suite (Unity) |
| Supporting tools | CSV datasets, GraphViz output, QEMU (RISC-V) |

## Project Structure

```text
.
├── src/
│   ├── main/java/
│   │   ├── LAPR3/                 # Railway operations, services and user interfaces
│   │   ├── ESINF/                 # Warehouse logic, algorithms and network analysis
│   │   ├── Graph/                 # Graph data structures
│   │   └── pt/ipp/isep/dei/       # Additional dataset-processing entry point
│   ├── main/resources/           # JavaFX views, CSS and application configuration
│   └── test/java/                # Java unit tests
├── stationMngt/ARQCP/           # C and RISC-V Assembly exercises/components
├── warehouseMngt/train_station_dataset/  # Warehouse CSV sample data
├── Dataset/                     # Railway network and operational CSV files
├── definitive-structure/docs/    # Academic documentation and SQL/PLSQL scripts
├── Scrum/                        # Sprint reports and planning material
└── pom.xml                       # Maven configuration
```

## Getting Started

### Prerequisites

For the Java applications:

- **JDK 17**
- **Apache Maven**
- Access to an **Oracle database** for database-backed functionality

The repository also includes components with additional requirements. See [Running the C / RISC-V Module](#running-the-c--risc-v-module).

### 1. Clone the repository

```bash
git clone https://github.com/1241182AfonsoMachado/railway-logistics-management-system-sem3.git
cd railway-logistics-management-system-sem3
```

Run the following commands from the **repository root** unless stated otherwise.

### 2. Build the Java project

```bash
mvn clean package
```

The Maven configuration builds a JAR with dependencies and declares `LAPR3.Application` as its command-line entry point.

### 3. Run the console application

After packaging successfully:

```bash
java -jar target/untitled-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The console menu includes warehouse operations, station queries, railway network analysis, freight assignment, and traffic management. Some options require data to be imported or a configured database connection.

### 4. Run the JavaFX application

```bash
mvn javafx:run
```

The JavaFX interface is configured to start `LAPR3.UI.javafx.RailwayManagementApp` and includes views for freight assignment, train assembly, and traffic management.

### Database configuration

The Java application reads its Oracle JDBC settings from:

```text
src/main/resources/application.properties
```

Provide **your own authorized database connection details** in the following format (placeholders only):

```properties
db.url=jdbc:oracle:thin:@<host>:<port>:<sid>
db.username=<your_username>
db.password=<your_password>
```

SQL/PLSQL scripts and database design material can be found under `definitive-structure/docs/system-documentation/bddad-us/`. A working database and the appropriate schema/data are required for the database-backed operations; the supplied CSV files are not a substitute for the full Oracle setup.

### Sample data

Sample railway and warehouse CSV files are included in `Dataset/` and `warehouseMngt/train_station_dataset/`. The warehouse dataset has its own [format documentation](warehouseMngt/train_station_dataset/README.md).

Some features import data from relative paths, so launch the program from the repository root to avoid missing-file errors.

## Running Tests

The repository contains JUnit tests under `src/test/java/`:

```bash
mvn test
```

These tests cover several algorithms, data models, importers, repositories, and service components. Some tests or integration paths may require additional environment configuration or input data.

## Running the C / RISC-V Module

The `stationMngt/ARQCP/Sprint3/` module has a `Makefile` for compiling and running C and RISC-V Assembly code.

**Additional prerequisites:** a compatible `riscv32-linux-gcc` toolchain, `qemu-riscv32`, and the RISC-V sysroot expected by the Makefile. A Linux or appropriately configured WSL environment is recommended.

From the repository root:

```bash
make -C stationMngt/ARQCP/Sprint3
make -C stationMngt/ARQCP/Sprint3 run
```

The Makefile uses a local sysroot path (`$HOME/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot`), so it may need adjustment for your environment. A separate test Makefile is available under `stationMngt/ARQCP/Sprint3/tests/`.

## Example Network Output

The repository contains an exported railway network visualization from the minimum-backbone analysis:

![Railway network graph output](output/usei12/belgian_railway_mst.svg)

## Academic Context and Authorship

This project was developed collaboratively by **Group 092** during the **3rd semester of the Computer Engineering degree at ISEP** (2025/2026). The repository retains the team's original source files, sprint documentation, and commit history. Individual authorship can be explored through the Git history.

> **Environment note:** This is an academic multi-module project, not a packaged standalone product. The Maven configuration and Makefiles are provided in the repository, but their execution depends on the installed toolchains, database access, and project-specific datasets.
