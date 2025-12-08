package ESINF.service;

import Graph.Edge;
import Graph.Graph;

import java.util.*;

/**
 * Analyzer for detecting and analyzing connected components in graphs
 * Particularly useful for MST/MSF scenarios with disconnected networks
 *
 * Based on USEI12 requirements for handling disconnected railway networks
 */
public class ConnectedComponentsAnalyzer {

    /**
     * Analyzes all connected components in a graph
     *
     * @param graph the graph to analyze
     * @param <V> vertex type
     * @param <E> edge weight type
     * @return ComponentAnalysisResult with detailed information
     */
    public <V, E> ComponentAnalysisResult<V, E> analyzeComponents(Graph<V, E> graph) {
        if (graph == null || graph.numVertices() == 0) {
            return new ComponentAnalysisResult<>(
                    new ArrayList<>(),
                    0,
                    true
            );
        }

        List<Component<V, E>> components = findAllComponents(graph);
        int totalComponents = components.size();
        boolean fullyConnected = (totalComponents == 1);

        return new ComponentAnalysisResult<>(
                components,
                totalComponents,
                fullyConnected
        );
    }

    /**
     * Finds all connected components using DFS
     *
     * @param graph the graph
     * @param <V> vertex type
     * @param <E> edge weight type
     * @return list of components
     */
    private <V, E> List<Component<V, E>> findAllComponents(Graph<V, E> graph) {
        List<Component<V, E>> components = new ArrayList<>();
        Set<V> globalVisited = new HashSet<>();

        for (V vertex : graph.vertices()) {
            if (!globalVisited.contains(vertex)) {
                // Found a new component, explore it
                Component<V, E> component = exploreComponent(graph, vertex, globalVisited);
                components.add(component);
            }
        }

        return components;
    }

    /**
     * Explores a single connected component starting from a vertex
     *
     * @param graph the graph
     * @param startVertex starting vertex
     * @param globalVisited set of all visited vertices (updated)
     * @param <V> vertex type
     * @param <E> edge weight type
     * @return Component containing all vertices and edges
     */
    private <V, E> Component<V, E> exploreComponent(Graph<V, E> graph,
                                                    V startVertex,
                                                    Set<V> globalVisited) {
        Set<V> componentVertices = new HashSet<>();
        Set<Edge<V, E>> componentEdges = new HashSet<>();

        dfsComponent(graph, startVertex, componentVertices, componentEdges);

        // Update global visited set
        globalVisited.addAll(componentVertices);

        // Create component with all required parameters
        return new Component<V,E>(
                componentVertices,
                componentEdges,
                startVertex  // representative vertex
        );
    }

    /**
     * DFS to explore all vertices and edges in a component
     */
    private <V, E> void dfsComponent(Graph<V, E> graph,
                                     V vertex,
                                     Set<V> visitedVertices,
                                     Set<Edge<V, E>> componentEdges) {
        visitedVertices.add(vertex);

        // Get all outgoing edges
        Collection<Edge<V, E>> outgoing = graph.outgoingEdges(vertex);
        if (outgoing != null) {
            for (Edge<V, E> edge : outgoing) {
                componentEdges.add(edge);

                V adjacent = edge.getVDest();
                if (!visitedVertices.contains(adjacent)) {
                    dfsComponent(graph, adjacent, visitedVertices, componentEdges);
                }
            }
        }

        // For undirected graphs, also check incoming edges
        if (!graph.isDirected()) {
            Collection<Edge<V, E>> incoming = graph.incomingEdges(vertex);
            if (incoming != null) {
                for (Edge<V, E> edge : incoming) {
                    componentEdges.add(edge);

                    V adjacent = edge.getVOrig();
                    if (!visitedVertices.contains(adjacent)) {
                        dfsComponent(graph, adjacent, visitedVertices, componentEdges);
                    }
                }
            }
        }
    }

    /**
     * Checks if a graph is fully connected
     *
     * @param graph the graph
     * @param <V> vertex type
     * @param <E> edge weight type
     * @return true if single component, false otherwise
     */
    public <V, E> boolean isFullyConnected(Graph<V, E> graph) {
        if (graph == null || graph.numVertices() == 0) {
            return true; // Empty graph is trivially connected
        }

        if (graph.numVertices() == 1) {
            return true; // Single vertex is connected
        }

        // DFS from first vertex
        V startVertex = graph.vertices().iterator().next();
        Set<V> visited = new HashSet<>();
        dfsVisit(graph, startVertex, visited);

        // If all vertices visited, graph is connected
        return visited.size() == graph.numVertices();
    }

    /**
     * Simple DFS visit (only vertices, no edges)
     */
    private <V, E> void dfsVisit(Graph<V, E> graph, V vertex, Set<V> visited) {
        visited.add(vertex);

        Collection<V> adjacents = graph.adjVertices(vertex);
        if (adjacents != null) {
            for (V adjacent : adjacents) {
                if (!visited.contains(adjacent)) {
                    dfsVisit(graph, adjacent, visited);
                }
            }
        }
    }

    /**
     * Validates MST/MSF structure
     * For k components: edges = vertices - k
     *
     * @param graph the MST/MSF graph
     * @param <V> vertex type
     * @param <E> edge weight type
     * @return ValidationResult
     */
    public <V, E> ValidationResult validateMSTStructure(Graph<V, E> graph) {
        ComponentAnalysisResult<V, E> analysis = analyzeComponents(graph);

        int vertices = graph.numVertices();
        int edges = graph.numEdges();
        int components = analysis.getNumComponents();

        int expectedEdges = vertices - components;
        boolean valid = (edges == expectedEdges);

        String message;
        if (valid) {
            if (components == 1) {
                message = "Valid MST structure: " + edges + " edges for " + vertices + " vertices";
            } else {
                message = "Valid MSF structure: " + edges + " edges for " +
                        vertices + " vertices in " + components + " components";
            }
        } else {
            message = "Invalid structure: Expected " + expectedEdges + " edges but found " + edges;
        }

        return new ValidationResult(valid, message, components);
    }

    /**
     * Component class representing a connected component
     */
    public static class Component<V, E> {
        private final Set<V> vertices;
        private final Set<Edge<V, E>> edges;
        private final V representativeVertex;

        public Component(Set<V> vertices, Set<Edge<V, E>> edges, V representativeVertex) {
            this.vertices = vertices;
            this.edges = edges;
            this.representativeVertex = representativeVertex;
        }

        public Set<V> getVertices() {
            return new HashSet<>(vertices);
        }

        public Set<Edge<V, E>> getEdges() {
            return new HashSet<>(edges);
        }

        public V getRepresentativeVertex() {
            return representativeVertex;
        }

        public int getVertexCount() {
            return vertices.size();
        }

        public int getEdgeCount() {
            return edges.size();
        }

        /**
         * Calculates total weight of edges in this component
         * Assumes E extends Number
         */
        public double getTotalWeight() {
            double sum = 0.0;
            for (Edge<V, E> edge : edges) {
                E weight = edge.getWeight();
                if (weight instanceof Number) {
                    sum += ((Number) weight).doubleValue();
                }
            }
            return sum;
        }

        @Override
        public String toString() {
            return String.format("Component[vertices=%d, edges=%d, weight=%.2f]",
                    getVertexCount(), getEdgeCount(), getTotalWeight());
        }
    }

    /**
     * Result class for component analysis
     */
    public static class ComponentAnalysisResult<V, E> {
        private final List<Component<V, E>> components;
        private final int numComponents;
        private final boolean fullyConnected;

        public ComponentAnalysisResult(List<Component<V, E>> components,
                                       int numComponents,
                                       boolean fullyConnected) {
            this.components = components;
            this.numComponents = numComponents;
            this.fullyConnected = fullyConnected;
        }

        public List<Component<V, E>> getComponents() {
            return new ArrayList<>(components);
        }

        public int getNumComponents() {
            return numComponents;
        }

        public boolean isFullyConnected() {
            return fullyConnected;
        }

        public Component<V, E> getLargestComponent() {
            return components.stream()
                    .max(Comparator.comparingInt(Component::getVertexCount))
                    .orElse(null);
        }

        public Component<V, E> getSmallestComponent() {
            return components.stream()
                    .min(Comparator.comparingInt(Component::getVertexCount))
                    .orElse(null);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Component Analysis ===\n");
            sb.append(String.format("Total components: %d\n", numComponents));
            sb.append(String.format("Fully connected: %s\n", fullyConnected ? "Yes" : "No"));
            sb.append("\nComponents:\n");

            for (int i = 0; i < components.size(); i++) {
                Component<V, E> comp = components.get(i);
                sb.append(String.format("  Component %d: %s\n", i + 1, comp));
            }

            return sb.toString();
        }
    }

    /**
     * Validation result for MST/MSF structure
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final int numComponents;

        public ValidationResult(boolean valid, String message, int numComponents) {
            this.valid = valid;
            this.message = message;
            this.numComponents = numComponents;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public int getNumComponents() {
            return numComponents;
        }

        @Override
        public String toString() {
            return (valid ? "✓ " : "✗ ") + message;
        }
    }
}