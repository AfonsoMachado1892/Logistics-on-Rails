# OO Analysis

The construction of the domain model is based on the client specifications, extracting nouns (for _concepts_) and verbs (for _relations_).

---

## Rationale to Identify Domain Conceptual Classes

To identify domain conceptual classes, candidate conceptual classes were listed inspired by the categories suggested by Larman in *"Applying UML and Patterns: An Introduction to Object-Oriented Analysis and Design and Iterative Development"*.

---

### _Conceptual Class Category List_

**Business Transactions**
- Freight
- Route (Planned high-level connection)
- Order (Warehouse dispatch request)
- Allocation (Assignment of stock to orders)

**Transaction Line Items**
- OrderLine (Specific SKU and quantity in an order)
- FreightWagon (Wagon assignment in a freight context)

**Product/Service related to a Transaction or Transaction Line Item**
- Train
- Freight Service
- Item (SKU definition)

**Transaction Records**
- Freight (records a transport operation)
- MovementLog (records internal warehouse movements)
- ReturnsEntry (audit log for returned goods inspection)
- CrossingOperation (log/plan of train crossings)

**Roles of People or Organizations**
- Operator

**Places**
- Station (Transport node)
- Facility (Specific building e.g., Warehouse, Silo)
- RailwayLine
- LineSegment
- Siding (Parking line on a segment)
- StationTrack (Lines inside a station)
- Aisle (Warehouse location)
- Bay (Warehouse location)

**Noteworthy Events**
- Freight dispatching
- Freight arrival
- Sensor Reading (Temperature/Humidity updates)
- Return Inspection (Quality control)

**Physical Objects**
- Train
- Locomotive (Electric/Diesel)
- Wagon
- Sensor (IoT device at station)
- Trolley (Used for picking in warehouse)
- Box (Physical container of items)

**Descriptions of Things**
- WagonModel (technical characteristics)
- LocomotiveModel
- Gauge (track width specifications)
- Item (Product description/Category)
- HubScore (Centrality metrics for network analysis)

**Catalogs**
- FacilityType
- WagonModelType

**Containers**
- Inventory (Logical container of warehouse contents)
- Aisle (Contains Bays)
- Bay (Contains Boxes)
- Siding (Contains Wagons)

**Elements of Containers**
- Box
- Wagon

**Organizations**
- Operator

**Records of Finance, Work, Contracts, Legal Matters**
- N/A

---

## Rationale to Identify Associations Between Conceptual Classes

Associations were identified based on semantic relations observed between classes, according to common association patterns. The updated list includes new requirements for Warehouse Management, Safety Systems (Sensors), and Network Optimization.

---

| Concept (A) | Association | Concept (B) |
|--------------|-------------|-------------|
| **Infrastructure** | | |
| RailwayLine | composed of | LineSegment |
| LineSegment | contains | Siding |
| Station | possesses | Facility |
| Station | contains | StationTrack |
| Station | equipped with | Sensor |
| Facility | categorized as | FacilityType |
| **Rolling Stock** | | |
| Operator | manages | Train |
| Train | uses | Locomotive |
| Train | pulls | Wagon |
| Train | follows | Route |
| Locomotive | supports | Gauge |
| **Freight** | | |
| Freight | includes | FreightWagon |
| Freight | origin/destination | Station |
| Route | defined by | Path |
| Path | uses | LineSegment |
| CrossingOperation | occurs at | Station |
| **Warehouse** | | |
| Facility | manages | Inventory |
| Inventory | composed of | Aisle |
| Aisle | composed of | Bay |
| Bay | contains | Box |
| Box | references | Item |
| Order | contains | OrderLine |
| Allocation | references | Box |
| ReturnsEntry | restocks as | Box |
| MovementLog | logs | Box |

---

## Domain Model Diagram

![Domain Model](../02.analysis/svg/domain-model.svg)