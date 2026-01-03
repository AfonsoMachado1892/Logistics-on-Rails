package Graph;

import java.util.Objects;

/**
 * Edge class - Represents an edge in a graph
 *
 * Stores:
 * - Weight of the edge
 * - Both endpoint vertices (vOrig and vDest)
 *
 * @param <V> Vertex element type
 * @param <E> Edge element type (weight)
 */
public class Edge<V, E> {

    private final V vOrig;      // Origin vertex
    private final V vDest;      // Destination vertex
    private E weight;           // Edge weight

    /**
     * Constructor
     * @param vOrig origin vertex
     * @param vDest destination vertex
     * @param weight edge weight
     */
    public Edge(V vOrig, V vDest, E weight) {
        if (vOrig == null || vDest == null) {
            throw new IllegalArgumentException("Vertices cannot be null");
        }
        this.vOrig = vOrig;
        this.vDest = vDest;
        this.weight = weight;
    }

    /**
     * Returns the origin vertex
     */
    public V getVOrig() {
        return vOrig;
    }

    /**
     * Returns the destination vertex
     */
    public V getVDest() {
        return vDest;
    }

    /**
     * Returns the edge weight
     */
    public E getWeight() {
        return weight;
    }

    /**
     * Sets the edge weight
     */
    public void setWeight(E weight) {
        this.weight = weight;
    }

    /**
     * Returns the endpoints as an array [vOrig, vDest]
     */
    @SuppressWarnings("unchecked")
    public V[] endpoints() {
        V[] endpoints = (V[]) new Object[2];
        endpoints[0] = vOrig;
        endpoints[1] = vDest;
        return endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        Edge<V, E> edge = (Edge<V, E>) o;

        return Objects.equals(vOrig, edge.vOrig) &&
                Objects.equals(vDest, edge.vDest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vOrig, vDest);
    }

    @Override
    public String toString() {
        return String.format("(%s -> %s, weight=%s)", vOrig, vDest, weight);
    }
}