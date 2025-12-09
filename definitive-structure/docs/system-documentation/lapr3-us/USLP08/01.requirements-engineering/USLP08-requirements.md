# USLP08 - Route Planner for Pending Freights

## 1. Requirements Engineering

### 1.1. User Story Description

As a **Freight Manager**, I want to use a route planner that allows me to define and assign routes (simple or complex) to fulfill a list of pending freights, so that each freight has a clear sequence of stops and operations.

### 1.2. Acceptance Criteria

- **AC1:** The system must allow selecting one or more pending freights to be planned.
- **AC2:** A route can be **simple** (direct sequence of stations) or **complex** (multiple segments/branches) and must be stored/associated to the freight(s).
- **AC3:** The planned route must list the **ordered sequence of stations** involved.
- **AC4:** For each station in the sequence, the system must specify the **cargo to load and/or unload** (quantities and SKUs/containers).
- **AC5:** The system must present the final route plan (stations + load/unload actions) before confirmation/assignment.
- **AC6:** Once confirmed, the route is marked as **assigned** to the selected freight(s).

### 1.3. Dependencies and Assumptions

- Requires pending freights data to be available (e.g., freight ID, cargo manifest, origin/destination).
- Requires stations/facilities master data to be loaded.
- Assumes that cargo compatibility/availability and path feasibility checks are handled in related USs or domain services (not detailed here).

### 1.4. Input and Output Data

**Input:**
- List of pending freights (with cargo manifest).
- Stations/facilities catalog.
- User selections: freight(s) to plan, route type (simple/complex), station sequence, load/unload actions per station.

**Output:**
- Draft route plan (preview): ordered stations with load/unload actions per station.
- Confirmation of assignment to the selected freight(s).
- Error/validation messages if required data is missing or route is invalid.

### 1.5. Open Questions / To Clarify

- Are there constraints on maximum hops/stations per route?
- Should the system auto-suggest routes or only capture manual plans?
- How are capacity and timing constraints validated (separate US)? 
- Required format for exporting/sharing the route plan?

