# TEMPORAL COMPLEXITY ANALYSIS (USEI10)

## Algorithm: Radius Search on 2D-Tree (KD-Tree) with Density Summary

---

### 🔹 Best / Average Case: **O(log n + m log m)**
**When:** The target radius `R` is small, and only a few stations `m` lie within the radius.  
**Example:** Searching 5 km around a station in Paris, most nodes pruned.  
**Why:**
- KD-Tree traversal prunes subtrees that are outside radius `R`, visiting only `O(log n)` nodes for a sparse search.
- The stations within the radius are inserted into a BST/AVL tree (`TreeMap`), giving `O(m log m)` for `m` insertions.

---

### 🔹 Worst Case: **O(n log m)**
**When:**
1. **Large Radius:** Radius `R` covers the entire dataset.
2. **All Nodes Valid:** No pruning occurs; all `n` stations are within `R`.  
   **Why:**
- The KD-Tree must visit all `n` nodes.
- Each station insertion into the TreeMap takes `O(log m)` time (BST/AVL insert), where `m = n` in the worst case.

---

### 🔹 Component Costs

- **KD-Tree Traversal:**
    - Best: `O(log n)` (pruned)
    - Worst: `O(n)` (full scan)
- **Distance Calculation (Haversine):** `O(1)` per node
- **BST/AVL Insertions (TreeMap):** `O(log m)` per insertion
- **Density Summary Updates:** `O(1)` per node (map increment)

**Where:**
- `n` = Total stations (~64,000)
- `m` = Stations within radius `R`

---

### 🔹 Notes

- If the radius is very small, the algorithm is extremely efficient due to pruning.
- If the radius is very large (e.g., covers all France), performance approaches `O(n log n)`.
- Density summary maps (`countryDistribution` and `cityDistribution`) are updated in `O(1)` per station.

---

### 🔹 Speedup vs Linear Scan

- **Linear Scan:** `O(n)` iterations + distance checks + list sort `O(m log m)`
- **KD-Tree + BST/AVL:** Best: `O(log n + m log m)`
- Typically 50x–500x faster for small radius queries due to effective subtree pruning.
