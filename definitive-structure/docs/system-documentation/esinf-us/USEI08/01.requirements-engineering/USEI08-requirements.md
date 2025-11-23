# USEI08 - Search by Geographical Area

## 1. Requirements Engineering

### 1.1. User Story Description

As a planner, I want to query the 2D-tree for all stations in a
latitude/longitude range with optional filters, so I can extract relevant stations
quickly.

### 1.2. Customer Specifications and Clarifications

**From the specifications document:**
- No clarifications from the client were necessary for this user story.


### 1.3. Acceptance Criteria

- **AC1:** The region: lat ∈ [latMin, latMax], lon ∈ [lonMin, lonMax] (inclusive).
The search should allow the optional application of filters with the following criteria:
- – isCity (true | false)
- – isMainStation (true | false)
- – country (PT | ES | all)

### 1.4. Found out Dependencies

- **USEI06 - Station Queries - Time-Zone Index**: There is a strong dependency on USEI06, as the 2D-tree build strategy reuses the BST/AVL structures of latitude and longitude created in that user story. The station data must be loaded via USEI06 before building the 2D-tree.
- **USEI07 - 2D Tree Build**: This user story builds upon the 2D-tree structure created in USEI07. The spatial query operations defined here rely on the correct implementation of the 2D-tree from USEI07.

### 1.5 Input and Output Data

**Input Data:**

- **From USEI06 and USEI07:**
    - Latitude BST/AVL (sorted list of stations by latitude, then by name)
    - Longitude BST/AVL (sorted list of stations by longitude, then by name)
    - Set of all unique stations (deduplicated from both indices)
    - The 2D-tree structure built in USEI07
    - Station data loaded from CSV (name, latitude, longitude, isCity, isMainStation, country)

**Output Data:**

- **The stations passed by the filter and range search:**
    - List of stations within the specified latitude/longitude range
    - Stations filtered by optional criteria (isCity, isMainStation, country)
    - Stations sorted by name in ascending order within each node's bucket
    

- **Temporal Complexity Analysis:**
    - Build Time Complexity: O(n log n) where n = number of unique coordinate pairs
    - Memory Complexity: O(n) for tree storage
    - Query Time Complexity:
        - Nearest Neighbor: O(log n) average time
        - Range Query: O(√n + k) time, where k = number of results

- **Success Indicators:**
    - Correctness of returned stations (all within range and matching filters)
    - Performance benchmarks for query times
    - Scalability with increasing dataset sizes
    
  

### 1.6 Other Relevant Remarks

- The implementation must ensure that the 2D-tree remains balanced for optimal query performance.
- The filtering mechanism should be efficient to avoid significant overhead during queries.
- Thorough testing with various geographic ranges and filter combinations is essential to validate correctness and performance.
- Consider edge cases such as stations located exactly on the boundary of the specified range.
- Documentation of the API for geographic range search should be clear and include examples for users.
- Ensure that the system can handle large datasets, as real-world applications may involve thousands of stations