package ESINF.service;

import ESINF.model.HubCentralityResult;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import Graph.Edge;
import Graph.Graph;

import java.util.*;

/**
 * Serviço para calcular as medidas de centralidade e o HubScore composto (USEI13).
 * Utiliza o algoritmo de Dijkstra (via MapGraph utility) para as métricas de caminho mais curto.
 */
public class CentralityAnalysisService {

    @SuppressWarnings("unused")
    private final RailwayNetworkRepository repository;

    public CentralityAnalysisService(RailwayNetworkRepository repository) {
        this.repository = repository;
    }

    /**
     * Executa a análise completa de Centralidade na rede ferroviária não direcionada.
     *
     * @param graph O grafo de RailwayStations com pesos em Double (distância).
     * @return Uma lista de HubCentralityResult, ordenada pelo HubScore descendente.
     */
    public List<HubCentralityResult> computeHubCentrality(Graph<RailwayStation, Double> graph) {
        List<HubCentralityResult> results = new ArrayList<>();

        if (graph.numVertices() == 0) return results;

        // 1. Inicializar Resultados e Calcular Grau/Força (Strength)
        Map<RailwayStation, HubCentralityResult> resultMap = initializeAndComputeSimpleMeasures(graph, results);

        // 2. Calcular Betweenness e Harmonic Closeness (Baseado em SSSP - Single Source Shortest Path)
        computeShortestPathMeasures(graph, resultMap);

        // 3. Normalizar e Calcular HubScore Final

        // Encontrar os valores máximos para normalização
        double maxStrength = results.stream().mapToDouble(HubCentralityResult::getStrength).max().orElse(1.0);
        double maxBetweenness = results.stream().mapToDouble(HubCentralityResult::getBetweenness).max().orElse(1.0);
        double maxCloseness = results.stream().mapToDouble(HubCentralityResult::getHarmonicCloseness).max().orElse(1.0);

        results.forEach(r -> {
            r.normalizeScores(maxStrength, maxBetweenness, maxCloseness);
            r.calculateHubScore();
        });

        // 4. Ordenar resultados pelo HubScore descendente
        Collections.sort(results);

        return results;
    }

    /**
     * Calcula o Degree (Grau) e a Strength (Força/Peso total) para cada estação.
     */
    private Map<RailwayStation, HubCentralityResult> initializeAndComputeSimpleMeasures(
            Graph<RailwayStation, Double> graph, List<HubCentralityResult> results) {

        Map<RailwayStation, HubCentralityResult> resultMap = new HashMap<>();

        for (RailwayStation station : graph.vertices()) {
            HubCentralityResult result = new HubCentralityResult(station);

            // Degree (Grau - número de ligações)
            result.setDegree(graph.outDegree(station));

            // Strength (Força - soma dos pesos/distância das arestas)
            double strength = 0.0;
            for (Edge<RailwayStation, Double> edge : graph.outgoingEdges(station)) {
                strength += edge.getWeight();
            }
            result.setStrength(strength);

            results.add(result);
            resultMap.put(station, result);
        }
        return resultMap;
    }

    /**
     * Calcula Betweenness e Harmonic Closeness.
     * Baseado na execução de SSSP (Algoritmo de Dijkstra) para cada nó de origem.
     * Implementa o algoritmo de Brandes para calcular Betweenness Centrality.
     */
    private void computeShortestPathMeasures(Graph<RailwayStation, Double> graph, Map<RailwayStation, HubCentralityResult> resultMap) {

        List<RailwayStation> vertices = graph.vertices();

        // Inicializar betweenness para todos os vértices
        for (RailwayStation v : vertices) {
            resultMap.get(v).setBetweenness(0.0);
        }

        // Para cada vértice de origem s, executar Brandes' algorithm
        for (RailwayStation s : vertices) {
            // --- 1. Forward phase: Dijkstra modificado para contar caminhos mais curtos ---
            Map<RailwayStation, Double> distances = new HashMap<>();
            Map<RailwayStation, Integer> pathCounts = new HashMap<>();
            Map<RailwayStation, List<RailwayStation>> predecessors = new HashMap<>();
            PriorityQueue<Map.Entry<RailwayStation, Double>> pq = new PriorityQueue<>(
                    Comparator.comparingDouble(Map.Entry::getValue)
            );

            // Inicialização
            for (RailwayStation v : vertices) {
                distances.put(v, Double.MAX_VALUE);
                pathCounts.put(v, 0);
                predecessors.put(v, new ArrayList<>());
            }
            distances.put(s, 0.0);
            pathCounts.put(s, 1);
            pq.add(new AbstractMap.SimpleEntry<>(s, 0.0));

            // Dijkstra modificado
            while (!pq.isEmpty()) {
                RailwayStation u = pq.poll().getKey();
                double distU = distances.get(u);

                if (distU == Double.MAX_VALUE) continue;

                for (Edge<RailwayStation, Double> edge : graph.outgoingEdges(u)) {
                    RailwayStation v = edge.getVDest();
                    double weight = edge.getWeight();
                    double newDist = distU + weight;
                    double distV = distances.get(v);

                    if (newDist < distV) {
                        distances.put(v, newDist);
                        pathCounts.put(v, pathCounts.get(u));
                        predecessors.get(v).clear();
                        predecessors.get(v).add(u);
                        pq.add(new AbstractMap.SimpleEntry<>(v, newDist));
                    } else if (Math.abs(newDist - distV) < 1e-9) {
                        // Caminho alternativo com mesma distância
                        pathCounts.put(v, pathCounts.get(v) + pathCounts.get(u));
                        if (!predecessors.get(v).contains(u)) {
                            predecessors.get(v).add(u);
                        }
                    }
                }
            }

            // --- 2. Calcular Harmonic Closeness: Soma dos inversos das distâncias de s para todos os v ---
            double harmonicCloseness = 0.0;
            for (Map.Entry<RailwayStation, Double> entry : distances.entrySet()) {
                double distance = entry.getValue();
                // Apenas se a distância for finita e > 0
                if (distance > 0 && distance != Double.MAX_VALUE) {
                    harmonicCloseness += 1.0 / distance;
                }
            }
            resultMap.get(s).setHarmonicCloseness(harmonicCloseness);

            // --- 3. Backward phase: Calcular dependências e acumular betweenness ---
            Map<RailwayStation, Double> dependencies = new HashMap<>();
            for (RailwayStation v : vertices) {
                dependencies.put(v, 0.0);
            }

            // Ordenar vértices por distância descendente
            List<RailwayStation> sortedVertices = new ArrayList<>(vertices);
            sortedVertices.sort((v1, v2) -> Double.compare(distances.get(v2), distances.get(v1)));

            // Back-propagation
            for (RailwayStation w : sortedVertices) {
                if (w.equals(s)) continue;

                double dependency = dependencies.get(w);
                int pathCountW = pathCounts.get(w);
                
                if (pathCountW > 0) {
                    for (RailwayStation v : predecessors.get(w)) {
                        double ratio = (double) pathCounts.get(v) / pathCountW;
                        dependencies.put(v, dependencies.get(v) + ratio * (1.0 + dependency));
                    }
                }

                if (!w.equals(s)) {
                    resultMap.get(w).setBetweenness(
                            resultMap.get(w).getBetweenness() + dependency
                    );
                }
            }
        }
    }


    /**
     * Retorna a análise de complexidade temporal (USEI13).
     */
    public String getTemporalComplexityAnalysis() {
        return "A complexidade temporal da análise de centralidade é dominada pelo cálculo da Centralidade de Intermediação (Betweenness) e Proximidade Harmónica (Harmonic Closeness). Ambas requerem o cálculo de caminhos mais curtos entre todos os pares de nós.\n\n" +
                "Utilizando o Algoritmo de Brandes, que se baseia em V execuções do Algoritmo de Dijkstra para um grafo esparso (rede ferroviária), a complexidade é:\n" +
                "\n" +
                "- O(V * (E + V log V))\n" +
                "\n" +
                "Onde V é o número de estações (vértices) e E é o número de conexões (arestas) no grafo.";
    }
}