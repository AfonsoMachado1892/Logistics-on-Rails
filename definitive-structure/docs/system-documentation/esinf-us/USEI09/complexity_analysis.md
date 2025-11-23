# TEMPORAL COMPLEXITY ANALYSIS (USEI09)

## Algorithm: k-Nearest Neighbors (k-NN) on 2D-Tree with Filtering

---

### Best / Average Case: **O(log n)**
**When:** N is small, and the Filter matches local stations.
**Example:** Finding the 5 nearest stations in Paris (no filter).
**Why:** The algorithm finds valid neighbors quickly. The distance to the N-th neighbor becomes the "pruning threshold." As this threshold shrinks, the algorithm discards huge sections of the tree (subtrees) because their bounding boxes are farther away than the N-th neighbor found so far.

---

###  Worst Case: **O(n * log N)**
**When:** 1. **Strict Filtering:** Searching for "Asia/Tokyo" stations while targeting coordinates in Portugal. The algorithm visits all local nodes, rejects them (filter mismatch), and cannot shrink the pruning threshold. It is forced to expand the search to the entire world (entire tree).
2. **Large N:** Requesting N ≈ total stations.
   **Why:** The "pruning check" (`distToSplittingPlane < maxDist`) always returns true, forcing the recursion to visit both Left and Right children at every node.

---

## Component Costs

- **Tree Traversal:** `O(log n)` (depth) to `O(n)` (full scan)
- **Priority Queue Ops:** `O(log N)` (insert/poll)
- **Distance Calc:** `O(1)`
- **Filter Check:** `O(1)`

**Where:** - `n` = Total stations (~64,000)
- `N` = Desired neighbors (k)

---

## Speedup vs Linear Scan

- **Linear Scan:** Always `O(n)` iterations.
- **2D-Tree:** Varies from `O(log n)` to `O(n)`.
    - *Note:* With `N=10`, the 2D-Tree is typically **100x-500x faster** than linear scan unless the filter forces a full traversal.