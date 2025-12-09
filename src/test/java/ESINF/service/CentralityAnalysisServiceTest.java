package ESINF.service;

import ESINF.model.HubCentralityResult;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import Graph.map.MapGraph;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CentralityAnalysisServiceTest {

    private final CentralityAnalysisService service =
            new CentralityAnalysisService(new RailwayNetworkRepository());

    @Test
    public void computeHubCentralityReturnsEmptyListWhenGraphIsEmpty() {
        // Grafo sem vértices deve devolver lista vazia.
        MapGraph<RailwayStation, Double> emptyGraph = new MapGraph<>(false);

        List<HubCentralityResult> results = service.computeHubCentrality(emptyGraph);

        assertTrue(results.isEmpty());
    }

    @Test
    public void computeHubCentralityCalculatesMeasuresOnLineGraph() {
        // Grafo em linha A-B-C: valida cálculo de graus, forças e que B lidera HubScore.
        RailwayStation a = new RailwayStation(1, "A", 0, 0, 0, 0);
        RailwayStation b = new RailwayStation(2, "B", 0, 0, 0, 0);
        RailwayStation c = new RailwayStation(3, "C", 0, 0, 0, 0);

        MapGraph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(a, b, 1.0);
        graph.addEdge(b, c, 1.0);

        List<HubCentralityResult> results = service.computeHubCentrality(graph);

        assertEquals(3, results.size());

        HubCentralityResult top = results.get(0);
        assertEquals(2, top.getStid());
        assertEquals(2.0, top.getDegree(), 1e-6);
        assertEquals(2.0, top.getStrength(), 1e-6);
        // Betweenness em grafo A-B-C (não dirigido) conta os pares A-C e C-A, dando 2.0.
        assertEquals(2.0, top.getBetweenness(), 1e-6);
        assertEquals(2.0, top.getHarmonicCloseness(), 1e-6);
        assertEquals(1.0, top.getHubScore(), 1e-6);

        HubCentralityResult leaf = results.get(1);
        assertEquals(1.0, leaf.getDegree(), 1e-6);
        assertEquals(1.0, leaf.getStrength(), 1e-6);
        assertEquals(0.0, leaf.getBetweenness(), 1e-6);
        assertEquals(1.5, leaf.getHarmonicCloseness(), 1e-6);
        assertEquals(0.4125, leaf.getHubScore(), 1e-4);
    }

    @Test
    public void computeHubCentralityHandlesDisconnectedVertices() {
        // Vértices isolados: todas as métricas devem ser zero e sem NaN.
        RailwayStation a = new RailwayStation(1, "A", 0, 0, 0, 0);
        RailwayStation b = new RailwayStation(2, "B", 0, 0, 0, 0);

        MapGraph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(a);
        graph.addVertex(b);

        List<HubCentralityResult> results = service.computeHubCentrality(graph);

        assertEquals(2, results.size());
        for (HubCentralityResult r : results) {
            assertEquals(0.0, r.getDegree(), 1e-6);
            assertEquals(0.0, r.getStrength(), 1e-6);
            assertEquals(0.0, r.getBetweenness(), 1e-6);
            assertEquals(0.0, r.getHarmonicCloseness(), 1e-6);
            assertEquals(0.0, r.getHubScore(), 1e-6);
        }
    }

    @Test
    public void computeHubCentralityStarGraphCentersHaveHighestScore() {
        // Grafo em estrela: nó central deve liderar HubScore e betweenness.
        RailwayStation center = new RailwayStation(0, "O", 0, 0, 0, 0);
        RailwayStation l1 = new RailwayStation(1, "L1", 0, 0, 0, 0);
        RailwayStation l2 = new RailwayStation(2, "L2", 0, 0, 0, 0);
        RailwayStation l3 = new RailwayStation(3, "L3", 0, 0, 0, 0);

        MapGraph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(center, l1, 1.0);
        graph.addEdge(center, l2, 1.0);
        graph.addEdge(center, l3, 1.0);

        List<HubCentralityResult> results = service.computeHubCentrality(graph);

        HubCentralityResult top = results.get(0);
        assertEquals(0, top.getStid());
        assertEquals(3.0, top.getDegree(), 1e-6);
        assertTrue("Centro deve ter betweenness maior que zero", top.getBetweenness() > 0);
        assertTrue("Centro deve ter HubScore superior às folhas", top.getHubScore() > results.get(1).getHubScore());
    }

    @Test
    public void computeHubCentralityAllEqualOnTriangle() {
        // Triângulo completo: todos os vértices devem ter métricas iguais e HubScore igual.
        RailwayStation a = new RailwayStation(1, "A", 0, 0, 0, 0);
        RailwayStation b = new RailwayStation(2, "B", 0, 0, 0, 0);
        RailwayStation c = new RailwayStation(3, "C", 0, 0, 0, 0);

        MapGraph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(a, b, 1.0);
        graph.addEdge(b, c, 1.0);
        graph.addEdge(c, a, 1.0);

        List<HubCentralityResult> results = service.computeHubCentrality(graph);

        assertEquals(3, results.size());
        double firstScore = results.get(0).getHubScore();
        for (HubCentralityResult r : results) {
            assertEquals(2.0, r.getDegree(), 1e-6);
            assertEquals(2.0, r.getStrength(), 1e-6);
            assertEquals(0.0, r.getBetweenness(), 1e-6);
            assertEquals(2.0, r.getHarmonicCloseness(), 1e-6);
            assertEquals(firstScore, r.getHubScore(), 1e-6);
        }
    }

    @Test
    public void computeHubCentralityRespectsEdgeWeightsForCloseness() {
        // Grafo em cadeia com pesos diferentes para validar distâncias usadas no closeness.
        RailwayStation a = new RailwayStation(1, "A", 0, 0, 0, 0);
        RailwayStation b = new RailwayStation(2, "B", 0, 0, 0, 0);
        RailwayStation c = new RailwayStation(3, "C", 0, 0, 0, 0);

        MapGraph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(a, b, 2.0); // distância maior
        graph.addEdge(b, c, 1.0);

        List<HubCentralityResult> results = service.computeHubCentrality(graph);

        HubCentralityResult bRes = results.stream().filter(r -> r.getStid() == 2).findFirst().orElseThrow();
        HubCentralityResult aRes = results.stream().filter(r -> r.getStid() == 1).findFirst().orElseThrow();
        HubCentralityResult cRes = results.stream().filter(r -> r.getStid() == 3).findFirst().orElseThrow();

        // B tem maior closeness (1/2 + 1/1 = 1.5) por estar no meio e ter a aresta mais leve para C.
        assertEquals(1.5, bRes.getHarmonicCloseness(), 1e-9);
        // A e C não são iguais: C está mais perto de B (1) do que A (2), logo closeness de C é maior.
        assertEquals(0.8333333333333333, aRes.getHarmonicCloseness(), 1e-9); // 1/2 + 1/3
        assertEquals(1.3333333333333333, cRes.getHarmonicCloseness(), 1e-9); // 1/1 + 1/3
        assertTrue(bRes.getHarmonicCloseness() > cRes.getHarmonicCloseness());
        assertTrue(cRes.getHarmonicCloseness() > aRes.getHarmonicCloseness());
    }

    @Test
    public void temporalComplexityTextIsReturned() {
        // Verifica que a string de complexidade contém a fórmula esperada.
        String complexity = service.getTemporalComplexityAnalysis();
        assertTrue(complexity.contains("O(V * (E + V log V))"));
    }
}

