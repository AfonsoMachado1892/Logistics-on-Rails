# OO Analysis

The construction process of the domain model is based on the client specifications, especially the nouns (for _concepts_) and verbs (for _relations_) used.

## Rationale to identify domain conceptual classes

To identify domain conceptual classes, we started by listing candidate conceptual classes inspired by the categories suggested in the book *"Applying UML and Patterns: An Introduction to Object-Oriented Analysis and Design and Iterative Development"*.

---

### _Conceptual Class Category List_

**Business Transactions**

- Upgrade
- Train Purchase (implicit)
- Station building

---

**Transaction Line Items**

- Locomotive
- Building

---

**Product/Service related to a Transaction or Transaction Line Item**

- Train
- RailwayLine
- Station

---

**Transaction Records**

- Upgrade

---

**Roles of People or Organizations**

- Player
- Editor
- Product owner

---

**Places**

- Station
- City
- Map
- HouseBlock
- Industry

---

**Noteworthy Events**

- Construction of stations
- Construction of Railway Lines
- Building upgrades
- Add a city to a map
- Add an industry to a map
- Map creation
- Scenario creation
- Assign trains to routes
- Create simulations


---

**Physical Objects**

- Train
- Station
- RailwayLine
- Building
- Locomotive
- Industry
- Cargo

---

**Descriptions of Things**

- Building (e.g., cost, effect)
- Locomotive
- Restrictions

---

**Catalogs**

- Types of Train
- Types of Buildings
- Types of Cargo
- Types of Industries
- Types of Railway lines
- Types of House Block Distribution

---
**Containers**

- City
- Map
- Station
- Scenario

---

**Elements of Containers**

- HouseBlock
- Station
- Industry
- City
- Building
- Cargo
- Restrictions

---

**Organizations**

- RailwaySystem (implicit concept)
- Industries

---

**Other External/Collaborating Systems**

- Simulator

---

**Records of finance, work, contracts, legal matters**

- Upgrade (records player actions)

---

**Financial Instruments**

- N/A

---

**Documents mentioned/used to perform some work**

- N/A

---

## Rationale to identify associations between conceptual classes

An association is a relationship between instances of objects that indicates a relevant connection and that is worth remembering, or it is derivable from the List of Common Associations:

- *A* is physically or logically part of *B*
- *A* is physically or logically contained in *B*
- *A* is a description for *B*
- *A* is known/logged/recorded in *B*
- *A* uses, manages, or owns *B*
- *A* is related to a transaction of *B*

---

| Concept (A)  | Association           | Concept (B)     |
|-------------|------------------------|-----------------|
| Player      | buys                   | Train           |
| Player      | builds                 | Station         |
| Player      | builds                 | RailwayLine     |
| Player      | owns                   | Station         |
| Player      | performs               | Upgrade         |
| Station     | contains               | Building        |
| Station     | generates/receives     | Cargo           |
| Station     | is part of             | City            |
| Station     | connects to            | RailwayLine     |
| RailwayLine | connects               | Station         |
| Train       | has                    | Locomotive      |
| Train       | picks up               | Cargo           |
| Train       | assigned to            | Route           |
| Route       | passes through         | Station         |
| Simulator   | generates cargo for    | City            |
| City        | contains               | Industry        |
| City        | contains               | Station         |
| City        | has                    | HouseBlock      |
| City        | uses                   | Distribution    |
| Industry    | produces/consumes      | Cargo           |
| Editor      | adds                   | Industry        |
| Editor      | selects/creates        | Map             |
| Editor      | selects                | Restrictions    |
| Map         | contains               | City            |
| Map         | contains               | Industry        |
| Upgrade     | applies to             | Building        |
| Upgrade     | upgrades               | Station         |

---

## Domain Model

*Do NOT forget to identify concept attributes too.*

*Insert below the Domain Model Diagram in an SVG format*

![Domain Model](svg/DM.svg)

