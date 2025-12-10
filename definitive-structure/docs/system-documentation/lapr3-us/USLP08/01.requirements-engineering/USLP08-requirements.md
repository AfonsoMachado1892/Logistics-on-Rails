# USLP08 – Route Planner for Pending Freights

## 1. Requirements Engineering

### 1.1. User Story Description

As a **Freight Manager**, I want to use a **route planner** that allows me to **define and assign routes** in order to accomplish a list of **pending freights**, where a route can be **simple or complex**, so that freights can be properly transported across the railway network.

---

### 1.2. Customer Specifications and Clarifications

> No additional clarifications were made up to now for this User Story.

---

### 1.3. Acceptance Criteria

- **AC1:** A list with the sequence of stations and the cargos to be loaded/unloaded should be presented
---

### 1.4. Found out Dependencies

- This User Story depends on **freight data being available**, namely pending freights and their cargo manifests.
- There is a dependency on **Facilities (Stations)** being previously loaded into the system.
- There is an implicit dependency on **USLP01 (Domain Model)** for the definition of:
    - Freight
    - Cargo
    - Route
    - Station
- This User Story is conceptually related to **USLP07 (Scheduler)** and precedes **USLP09 (Assemble and assign a train to a route)**.
- Data availability depends on the **BDDAD component** (database and/or file-based integration).

---

### 1.5. Input and Output Data

#### Input Data

- **Pre-loaded data:**
    - List of pending freights
    - Cargo information per freight
    - List of stations/facilities
- **User-selected data:**
    - Freight(s) to be planned
    - Route type (simple or complex)
    - Ordered list of stations
    - Load/unload operations per station

#### Output Data

- List of pending freights (for selection)
- Planned route containing:
    - Ordered sequence of stations
    - For each station:
        - Cargo(s) to be loaded
        - Cargo(s) to be unloaded
- Confirmation of successful route assignment
- Error messages in case of:
    - Invalid route definition
    - Missing or inconsistent cargo operations
- (In)Success of the operation

---

### 1.6. System Sequence Diagram (SSD)

To be defined.

---

### 1.7. Other Relevant Remarks

- This User Story focuses only on **route planning from a logistics perspective**, not on train composition or scheduling.
- No automatic pathfinding is required at this stage; routes are explicitly defined by the Freight Manager.
- Timing, crossings, and speed calculations are handled in subsequent User Stories.
