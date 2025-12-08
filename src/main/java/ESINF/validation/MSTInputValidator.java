package ESINF.validation;

import Graph.Edge;
import Graph.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Validator for MST computation input
 * Ensures graph meets requirements for Kruskal's algorithm
 *
 * MST Requirements:
 * - Graph must be undirected
 * - Graph must have at least 1 vertex
 * - Edge weights must be non-negative (for standard MST)
 * - Graph must not be null
 */
public class MSTInputValidator {

    /**
     * Validates that a graph is suitable for MST computation
     *
     * @param graph the graph to validate
     * @param <V> vertex type
     * @param <E> edge weight type
     * @return ValidationResult with status and messages
     */
    public <V, E> ValidationResult validate(Graph<V, E> graph) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Critical validations
        if (graph == null) {
            errors.add("Graph cannot be null");
            return new ValidationResult(false, errors, warnings);
        }

        // Check if graph is directed
        if (graph.isDirected()) {
            errors.add("MST requires undirected graph. Use convertToUndirected() first.");
        }

        // Check minimum vertices
        if (graph.numVertices() == 0) {
            errors.add("Graph must have at least 1 vertex");
        }

        // Check for negative weights (if applicable)
        if (graph.numVertices() > 0) {
            validateEdgeWeights(graph, errors, warnings);
        }

        // Warnings
        if (graph.numVertices() == 1) {
            warnings.add("Graph has only 1 vertex. MST will be empty (0 edges).");
        }

        if (graph.numEdges() == 0 && graph.numVertices() > 1) {
            warnings.add("Graph has no edges. MST will be a forest of isolated vertices.");
        }

        // Check connectivity warning
        if (graph.numVertices() > 1 && graph.numEdges() < graph.numVertices() - 1) {
            warnings.add("Graph may be disconnected. Result will be MSF (Minimum Spanning Forest).");
        }

        boolean valid = errors.isEmpty();
        return new ValidationResult(valid, errors, warnings);
    }

    /**
     * Validates edge weights
     * Checks for negative weights and non-comparable types
     */
    private <V, E> void validateEdgeWeights(Graph<V, E> graph,
                                            List<String> errors,
                                            List<String> warnings) {
        Collection<Edge<V, E>> edges = graph.edges();

        if (edges == null || edges.isEmpty()) {
            return;
        }

        boolean hasNegativeWeight = false;
        boolean hasNullWeight = false;

        for (Edge<V, E> edge : edges) {
            E weight = edge.getWeight();

            // Check for null weights
            if (weight == null) {
                hasNullWeight = true;
                continue;
            }

            // Check for negative weights (if Number)
            if (weight instanceof Number) {
                double value = ((Number) weight).doubleValue();
                if (value < 0) {
                    hasNegativeWeight = true;
                }
            }

            // Check if Comparable
            if (!(weight instanceof Comparable)) {
                errors.add("Edge weights must implement Comparable interface");
                return;
            }
        }

        if (hasNullWeight) {
            errors.add("Graph contains edges with null weights");
        }

        if (hasNegativeWeight) {
            warnings.add("Graph contains negative edge weights. MST may not be meaningful.");
        }
    }

    /**
     * Quick validation that throws exception on error
     *
     * @param graph the graph
     * @param <V> vertex type
     * @param <E> edge weight type
     * @throws InvalidGraphException if validation fails
     */
    public <V, E> void validateOrThrow(Graph<V, E> graph) throws InvalidGraphException {
        ValidationResult result = validate(graph);

        if (!result.isValid()) {
            throw new InvalidGraphException(result.getErrorsAsString());
        }
    }

    /**
     * Validates graph with custom message
     */
    public <V, E> void validateOrThrow(Graph<V, E> graph, String context)
            throws InvalidGraphException {
        ValidationResult result = validate(graph);

        if (!result.isValid()) {
            throw new InvalidGraphException(
                    context + ": " + result.getErrorsAsString()
            );
        }
    }

    /**
     * Result class for validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public String getErrorsAsString() {
            return String.join("; ", errors);
        }

        public String getWarningsAsString() {
            return String.join("; ", warnings);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (valid) {
                sb.append("✓ Validation passed");
            } else {
                sb.append("✗ Validation failed");
            }

            if (!errors.isEmpty()) {
                sb.append("\n\nErrors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }

            if (!warnings.isEmpty()) {
                sb.append("\n").append(warnings.isEmpty() ? "" : "Warnings:\n");
                for (String warning : warnings) {
                    sb.append("  ⚠ ").append(warning).append("\n");
                }
            }

            return sb.toString();
        }
    }

    /**
     * Custom exception for invalid graph input
     */
    public static class InvalidGraphException extends Exception {
        public InvalidGraphException(String message) {
            super(message);
        }

        public InvalidGraphException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}