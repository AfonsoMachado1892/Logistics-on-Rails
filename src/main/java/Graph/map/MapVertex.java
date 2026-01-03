package Graph.map;

import Graph.Edge;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MapVertex - Vertex structure for Adjacency Map representation
 *
 * Each vertex stores:
 * - The vertex element (key)
 * - A map of outgoing edges (adjacent vertex -> edge)
 *
 * @param <V> Vertex element type
 * @param <E> Edge element type (weight)
 */
public class MapVertex<V, E> {

    private final V element;
    private final Map<V, Edge<V, E>> outgoingEdges;

    /**
     * Constructor
     * @param element the vertex element
     */
    public MapVertex(V element) {
        if (element == null) {
            throw new IllegalArgumentException("Vertex element cannot be null");
        }
        this.element = element;
        this.outgoingEdges = new HashMap<>();
    }

    /**
     * Returns the vertex element
     */
    public V getElement() {
        return element;
    }

    /**
     * Returns the map of outgoing edges
     * Key: adjacent vertex, Value: edge to that vertex
     */
    public Map<V, Edge<V, E>> getOutgoing() {
        return outgoingEdges;
    }

    /**
     * Adds an outgoing edge to an adjacent vertex
     * @param vAdj the adjacent vertex
     * @param edge the edge to that vertex
     * @return the previous edge if it existed, null otherwise
     */
    public Edge<V, E> addEdge(V vAdj, Edge<V, E> edge) {
        return outgoingEdges.put(vAdj, edge);
    }

    /**
     * Removes an outgoing edge to an adjacent vertex
     * @param vAdj the adjacent vertex
     * @return the removed edge, or null if it didn't exist
     */
    public Edge<V, E> removeEdge(V vAdj) {
        return outgoingEdges.remove(vAdj);
    }

    /**
     * Returns the edge to a given adjacent vertex
     * @param vAdj the adjacent vertex
     * @return the edge, or null if it doesn't exist
     */
    public Edge<V, E> getEdge(V vAdj) {
        return outgoingEdges.get(vAdj);
    }

    /**
     * Returns the number of outgoing edges (out-degree)
     */
    public int numAdjVerts() {
        return outgoingEdges.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        MapVertex<V, E> that = (MapVertex<V, E>) o;

        return Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    public String toString() {
        return element.toString();
    }
}