# OO Analysis
The construction process of the domain model is based on the client specifications, especially the nouns (for _concepts_) and verbs (for _relations_) used.
## Rationale to identify domain conceptual classes

To identify domain conceptual classes, we started by listing candidate conceptual classes inspired by the categories suggested in the book *"Applying UML and Patterns: An Introduction to Object-Oriented Analysis and Design and Iterative Development"*.
---
### _Conceptual Class Category List_
**Business Transactions**
- Freight Dispatching
- Train Assignment
- Station Construction
- Line Segment Maintenance

**Transaction Line Items**
- Locomotive
- Wagon
- Cargo

**Product/Service related to a Transaction or Transaction Line Item**
- Train
- RailwayLine
- Station
- Freight Service

**Transaction Records**
- Maintenance Log
- Dispatch Record

**Roles of People or Organizations**
- LogisticsOperator
- DatabaseAdministrator
- InventoryManager
- SystemUser

**Places**
- Station
- Terminal
- FreightYard
- RailwayLineSegment
- Endpoint

**Noteworthy Events**
- Cargo Loading
- Cargo Unloading
- Train Scheduling
- Line Segment Construction
- Freight Dispatch
- Route Planning

**Physical Objects**
- Train
- Locomotive
- Wagon
- Cargo
- RailwayLineSegment
- Track

**Descriptions of Things**
- LineSegment (e.g., gauge, weight limit, speed limit)
- Locomotive (e.g., type, gauge)
- Wagon (e.g., type, capacity)

**Catalogs**
- Types of Locomotives
- Types of Wagons
- Types of Cargo
- Types of RailwayLineSegments

**Containers**
- Terminal
- FreightYard
- RailwayNetwork

**Elements of Containers**
- Station
- Track
- Cargo
- LineSegment

**Organizations**
- TrainOperator
- RailwayInfrastructureOwner

**Other External/Collaborating Systems**
- Simulator
- WarehouseManagementSystem

**Records of finance, work, contracts, legal matters**
- MaintenanceSchedule
- OperationLog

**Financial Instruments**
- N/A

**Documents mentioned/used to perform some work**
- DispatchOrder
- MaintenanceReport

## Rationale to identify associations between conceptual classes
An association is a relationship between instances of objects that indicates a relevant connection and that is worth remembering, or it is derivable from the List of Common Associations:
- *A* is physically or logically part of *B*
- *A* is physically or logically contained in *B*
- *A* is a description for *B*
- *A* is known/logged/recorded in *B*
- *A* uses, manages, or owns *B*
- *A* is related to a transaction of *B*

---
| Concept (A)          | Association           | Concept (B)           |
|-----------------------|-----------------------|-----------------------|
| LogisticsOperator    | manages               | Train                 |
| LogisticsOperator    | schedules             | Route                 |
| TrainOperator        | owns                  | Train                 |
| Train                | contains              | Locomotive            |
| Train                | carries               | Cargo                 |
| Station              | contains              | Cargo                 |
| Station              | connects to           | RailwayLineSegment    |
| RailwayLineSegment   | connects              | Station               |
| RailwayLineSegment   | part of               | RailwayNetwork        |
| Terminal             | contains              | Station               |
| FreightYard          | handles               | Cargo                 |
| Cargo                | transported by        | Train                 |
| Endpoint             | connected by          | RailwayLineSegment    |
| RailwayInfrastructureOwner | maintains       | RailwayLineSegment    |
| Simulator            | simulates             | Route                 |
| WarehouseManagementSystem | manages         | Cargo                |
| DatabaseAdministrator | records               | MaintenanceLog        |
| InventoryManager     | oversees              | Cargo                 |
| SystemUser           | views                 | DispatchRecord        |

## Domain Model
*Do NOT forget to identify concept attributes too.*
*Insert below the Domain Model Diagram in an SVG format*
![Domain Model](../01.requirements-engineering/svg/DM.svg)