# Análise do Algoritmo Directed Line Upgrade Plan (USEI11)

**Componente:** UpgradePlanController, TopologicalSortService, CycleDetectionService  
**Algoritmo Base:** Topological Sort (DFS-based) + Cycle Detection (Colored DFS)

---

## 1. Análise da Complexidade Temporal

A implementação do algoritmo para determinar a ordem de upgrade de estações divide-se em duas etapas principais, ambas baseadas em DFS (Depth-First Search). A complexidade total é determinada pela execução sequencial destes algoritmos.

### Etapa A: Deteção de Ciclos (`findAllCycles`)
Antes de calcular a ordenação topológica, o sistema verifica se o grafo contém ciclos usando **Colored DFS**.
* **Operação:** Itera sobre todos os vértices ($V$) marcando-os com cores (WHITE/GRAY/BLACK) e examina todas as arestas ($E$) para detetar back edges (arestas que apontam para vértices GRAY).
* **Inicialização:** Marca todos os vértices como WHITE → $O(V)$
* **DFS:**
    * Cada vértice é visitado **exatamente uma vez** (WHITE → GRAY → BLACK)
    * Cada aresta é examinada **exatamente uma vez** ao percorrer adjacências
* **Complexidade:** $O(V + E)$
* **Nota:** Quando um ciclo é detetado, o algoritmo extrai o caminho usando apontadores `parent`. Esta operação é $O(V)$ no pior caso, mas não altera a complexidade dominante.

### Etapa B: Ordenação Topológica (`topologicalSort`)
Executado **apenas se o grafo for acíclico (DAG)**.
* **Operação:** Usa DFS para ordenar os vértices de forma que para cada aresta $u \rightarrow v$, o vértice $u$ aparece antes de $v$ na ordenação.
* **Algoritmo:**
    1. Marca todos os vértices como não visitados → $O(V)$
    2. Para cada vértice não visitado, executa DFS
    3. Quando um vértice termina o processamento (todos os adjacentes visitados), adiciona-o ao **início** da lista 
* **DFS:**
    * Cada vértice é visitado **exatamente uma vez**
    * Cada aresta é examinada **exatamente uma vez**
    * Operação `addFirst()` → $O(1)$ 
* **Complexidade:** $O(V + E)$

### Conclusão da Complexidade
Somando todas as etapas:
$$Total = O(V + E) + O(V + E) = O(V + E)$$

**Nota:** Embora existam duas DFS, ambas são lineares e a soma de operações lineares continua linear.

Como ambos os algoritmos executam no máximo $V$ visitas a vértices e $E$ exames de arestas, a complexidade assintótica final é:
$$O(V + E)$$

---