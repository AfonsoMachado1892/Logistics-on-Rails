# USEI15 – Risk-Aware Shortest Path Analysis



## 1. Context
The goal is to find the shortest path between two stations in a directed graph where edges may have negative costs (representing risk reduction or subsidies) but must not contain negative cycles reachable from the source.



## 2. Temporal Complexity Analysis

The implementation of the Bellman-Ford algorithm can be divided into three distinct phases. The total complexity is determined by the dominant relaxation phase.

### Phase A: Initialization
Before processing, the algorithm initializes the distance map and predecessor map.
* **Operation:** Set `dist[s] = 0` and `dist[v] = ∞` for all other vertices $V$.
* **Complexity:** $O(V)$
* **Note:** Requires iterating through all vertices to set initial values.

### Phase B: Edge Relaxation (The "Core" Loop)
This is the main body of the algorithm, responsible for finding the shortest paths.
* **Outer Loop:** The algorithm iterates $(V - 1)$ times. This is the maximum path length (in edges) possible without forming a cycle.
* **Inner Loop:** In each iteration, the algorithm traverses all edges ($E$) in the graph to check if a shorter path exists (`relax` operation).
* **Operation Cost:** The `relax` method involves basic Map retrieval and addition, which are $O(1)$ on average.
* **Calculation:** $(V - 1) \times E$
* **Complexity:** $O(V \cdot E)$

### Phase C: Negative Cycle Detection
After the relaxation phase, one final pass is required to verify if the solution is valid.
* **Operation:** Iterate over all edges ($E$) one last time. If any edge can still be relaxed (`dist[u] + weight < dist[v]`), a negative cycle exists.
* **Complexity:** $O(E)$



## 3. Total Complexity

Combining the three phases:
$$O(V) + O(V \cdot E) + O(E)$$

The term $O(V \cdot E)$ dominates the equation. Therefore, the Worst-Case Time Complexity is:

### **$$O(V \cdot E)$$**

*Where $V$ is the number of stations and $E$ is the number of connections.*



## 4. Justification


Although Bellman-Ford is slower than Dijkstra, it is the **most efficient correct algorithm** for this specific user story due to the business requirement of handling negative edge costs.