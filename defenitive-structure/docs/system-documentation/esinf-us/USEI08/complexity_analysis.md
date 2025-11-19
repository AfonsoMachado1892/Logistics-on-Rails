# TEMPORAL COMPLEXITY ANALYSIS (USEI08)

## Algorithm: 2D-Tree Range Search with Pruning

---

### 🔹 Best Case: **O(log n + k)**
**Quando:** Região pequena, área esparsa, pruning eficaz  
**Exemplo:** Região 0.1° × 0.1° → ~5ms para 64k estações

---

### 🔹 Average Case: **O(√n + k)**
**Quando:** Região média (1–5 graus)  
**Exemplo:** Portugal → ~15ms para 64k estações  
**Porquê:** A consulta retangular intersecta ~O(√n) planos de divisão

---

### 🔹 Worst Case: **O(n)**
**Quando:** A região cobre todo o mapa (sem pruning)  
**Exemplo:** Europa inteira → ~100ms para 64k estações

---

## Componentes de Custo

- **Region validation:** `O(1)`
- **Tree traversal:** `O(log n)` a `O(n)`
- **Filter application:** `O(k × f)` onde *f* = número de verificações de filtro
- **Result collection:** `O(k)`

**Onde:**  
- `n` = total de estações na árvore (~64,000)  
- `k` = número de resultados encontrados

---

##  Speedup vs Linear Scan

- **Linear scan:** Sempre `O(n)` → ~50–100ms  
- **2D-Tree:** Tipicamente `O(√n)` → ~5–30ms  


---
