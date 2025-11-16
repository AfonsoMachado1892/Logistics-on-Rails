# USEI07 - Build a balanced 2D-Tree on Latitude/Longitude

## 1. Requirements Engineering

### 1.1. User Story Description

As a data engineer, I want to build a balanced 2D-tree using (lat, lon) so that range and proximity searches run efficiently and deterministically.

### 1.2. Customer Specifications and Clarifications

**From the specifications document:**

> **Context**: We need a spatial index over 64k (approximately) European train stations to enable fast geographic queries. A KD-tree (k=2) on (latitude, longitude) supports range and nearest-neighbour operations much faster than scanning all stations.
>
> **Spatial Indexing Requirements:**
> - The 2D-tree must be balanced to ensure efficient queries with O(log n) average-case performance
> - Stations are indexed by their geographic coordinates (latitude, longitude)
> - Multiple stations may share the same coordinates (e.g., different station names at the same location)
> - When stations share coordinates, they must be stored in a bucket sorted alphabetically by station name
>
> **Build Strategy:**
> - Use the BST/AVL structures of latitude and longitude created in USEI06 for efficient construction
> - Perform a bulk build using recursive median partitioning to ensure balance
> - Alternate splitting axis at each depth level: even depth = latitude, odd depth = longitude
> - Node splitting follows the KD-tree convention: left subtree contains points with coordinate < median, right subtree contains points with coordinate ≥ median
>
> **Coordinate Bucket Example:**
> - Coordinates 38.71387, -9.122271 correspond to multiple stations:
    >   - Lisbon Oriente
>   - Lisbon Santa Apolónia
> - These must be stored together in a single node, sorted alphabetically: ["Lisbon Oriente", "Lisbon Santa Apolónia"]

**From the client clarifications:**

> **Question:** In USEI07, if, for example, there is one station with latitude and longitude (1.5) and another with (1.2), when we sort by latitude, it should be (1.2); (1.5) or (1.5); (1.2). Should it be sorted only by looking at the latitude, and if there is a tie, is it sorted by the order it appears, or is the tie broken by longitude?
>
> **Answer:** You should sort in ascending order. Ties do not happen.

> **Question:** Relativamente às USEI06 e USEI07, um dos requisitos refere: "If multiple stations share the same coordinates, such as Lisbon Santa Apolónia and Lisbon Oriente, the tree must preserve all these stations sorted by name in ascending order.", no caso da USEI06 - numa AVL/BST, e no caso da USEI07 numa 2D Tree. A inserção de multipos nodes com as mesmas coordenas numa 2DTree compromete o balanceamento deste tipo de estrutura de informação e/ou futuras pesquisas? (assumindo que um node apenas pode conter um "elemento" (a.k.a estação).  Ou alternativamente, fará sentido que este tipo de estrutura permita a inserção de multiplas estações (numa estrutura de informação secundária, como um "bucket") no mesmo node da 2D Tree - de forma a não violar o principio de "Any internal node in a Kd-tree divides the space into two halves irrespective of the number of dimensions"?
>
> **Answer:** Uma BST ou 2D-Tree se bem implementada não permite a inserção de múltiplos nodes com as mesmas coords. pelo que se isso acontecer o nó da árvore terá de ter uma estrutura auxiliar para os guardar.

### 1.3. Acceptance Criteria

- **AC1:** The simplest way to ensure a balanced 2D-tree is to
  perform a bulk build; for this, develop a balanced build strategy that will
  use the BST/AVL of latitude and longitude created in USEI06 to efficiently
  construct the 2D-tree. If multiple stations share the same coordinates, such
  as 38.71387, -9.122271, which are linked to Lisbon Santa Apol´onia and Lis-
  bon Oriente, the 2D-tree must preserve all these stations sorted by name in
  ascending order.

### 1.4. Found out Dependencies

- **USEI06 - Station Queries - Time-Zone Index**: There is a strong dependency on USEI06, as the 2D-tree build strategy reuses the BST/AVL structures of latitude and longitude created in that user story. The station data must be loaded via USEI06 before building the 2D-tree.

### 1.5 Input and Output Data

**Input Data:**

- **From USEI06:**
    - Latitude BST/AVL (sorted list of stations by latitude, then by name)
    - Longitude BST/AVL (sorted list of stations by longitude, then by name)
    - Set of all unique stations (deduplicated from both indices)

**Output Data:**

- **Tree Statistics:**
    - Size: Total number of nodes in the 2D-tree (number of unique coordinate pairs)
    - Height: Maximum depth of the tree
    - Distinct bucket sizes: Set of all unique bucket sizes (e.g., {1, 2, 3, 5})
    - Bucket size frequency distribution: How many nodes have 1 station, 2 stations, etc.

- **Temporal Complexity Analysis:**
    - Expected complexity: O(n log n)
    - Actual build time in milliseconds
    - Comparison with ideal balanced height: log₂(n)
    - Balance quality assessment (excellent/good/fair)

- **Success Indicators:**
    - Confirmation that tree was built successfully
    - Validation that all stations are indexed
    - Verification that buckets are sorted by name

### 1.6. System Sequence Diagram (SSD)

![System Sequence Diagram](svg/USEI07-SSD.svg)

**Alternative Flow:**

### 1.7 Other Relevant Remarks

- **Spatial Query Operations**: Once built, the 2D-tree supports efficient spatial queries:
    - **Range Query**: Find all stations within a rectangular region defined by [minLat, maxLat] × [minLon, maxLon] in O(√n + k) time, where k is the number of results
    - **Nearest Neighbor**: Find the closest station to a target coordinate in O(log n) average time using the Haversine distance formula

- **Haversine Distance**: Geographic distance between two coordinates is calculated using the Haversine formula, which accounts for Earth's spherical shape:
  R = 6371 km (Earth radius)
  dLat = lat₂ - lat₁
  dLon = lon₂ - lon₁
  a = sin²(dLat/2) + cos(lat₁) × cos(lat₂) × sin²(dLon/2)
  c = 2 × atan2(√a, √(1-a))
  distance = R × c

- **Deterministic Ordering**: To ensure reproducible results, the build algorithm always:
  1. Sorts stations by the current axis (latitude or longitude)
  2. For ties, uses station name as secondary sort key (ascending)
  3. Groups stations with identical coordinates in the same node
  4. Sorts bucket contents alphabetically by name

- **Performance Characteristics**:
  - **Build Time**: O(n log n) where n = number of unique coordinate pairs
  - **Memory**: O(n) for tree storage
  - **Query Time**: O(log n) for nearest neighbor, O(√n + k) for range queries
  - **Height**: Typically close to log₂(n) for a balanced tree built with median partitioning

- **Integration with USEI06**: The 2D-tree complements the time-zone index from USEI06:
  - USEI06 provides efficient queries by time zone, country, and 1D coordinate ranges
  - USEI07 adds 2D spatial queries (rectangular regions, proximity searches)
  - Both share the same station data loaded once from CSV

- **Bucket Size Analysis**: Analyzing bucket sizes reveals geographic clustering patterns:
  - Most nodes have 1 station (unique coordinates)
  - Some nodes have 2-3 stations (nearby platforms, multiple stations at same location)
  - Large buckets (5+ stations) may indicate data quality issues or major transportation hubs