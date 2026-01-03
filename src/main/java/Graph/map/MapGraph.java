package Graph.map;

import Graph.Edge;
import Graph.Graph;

import java.util.*;
import java.util.function.Predicate;

/**
 * MapGraph - Adjacency Map implementation of Graph interface
 *
 * Uses a Map structure where:
 * - Key: vertex element (V)
 * - Value: MapVertex containing outgoing edges map
 *
 * Space Complexity: O(V + E)
 * - Better for sparse graphs where |E| << |V|²
 *
 * @param <V> Vertex element type
 * @param <E> Edge element type (weight)
 */
public class MapGraph<V, E> implements Graph<V, E> {

    private final boolean isDirected;
    private final Map<V, MapVertex<V, E>> vertices;

    /**
     * Constructor
     * @param directed true if graph is directed, false if undirected
     */
    public MapGraph(boolean directed) {
        this.isDirected = directed;
        this.vertices = new LinkedHashMap<>(); // Preserve insertion order
    }

    @Override
    public boolean isDirected() {
        return isDirected;
    }

    @Override
    public int numVertices() {
        return vertices.size();
    }

    @Override
    public ArrayList<V> vertices() {
        return new ArrayList<>(vertices.keySet());
    }

    @Override
    public boolean validVertex(V vert) {
        return vertices.containsKey(vert);
    }

    @Override
    public int key(V vert) {
        int i = 0;
        for (V v : vertices.keySet()) {
            if (v.equals(vert)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public V vertex(int key) {
        if (key < 0 || key >= numVertices()) {
            return null;
        }

        int i = 0;
        for (V v : vertices.keySet()) {
            if (i == key) {
                return v;
            }
            i++;
        }
        return null;
    }

    @Override
    public V vertex(Predicate<V> p) {
        for (V v : vertices.keySet()) {
            if (p.test(v)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public Collection<V> adjVertices(V vert) {
        if (!validVertex(vert)) {
            return null;
        }

        MapVertex<V, E> mv = vertices.get(vert);
        return mv.getOutgoing().keySet();
    }

    @Override
    public int numEdges() {
        int count = 0;
        for (MapVertex<V, E> mv : vertices.values()) {
            count += mv.numAdjVerts();
        }

        // For undirected graphs, each edge is counted twice
        return isDirected ? count : count / 2;
    }

    @Override
    public Collection<Edge<V, E>> edges() {
        Set<Edge<V, E>> edges = new HashSet<>();

        for (MapVertex<V, E> mv : vertices.values()) {
            edges.addAll(mv.getOutgoing().values());
        }

        return edges;
    }

    @Override
    public Edge<V, E> edge(V vOrig, V vDest) {
        if (!validVertex(vOrig) || !validVertex(vDest)) {
            return null;
        }

        MapVertex<V, E> mv = vertices.get(vOrig);
        return mv.getEdge(vDest);
    }

    @Override
    public Edge<V, E> edge(int vOrigKey, int vDestKey) {
        V vOrig = vertex(vOrigKey);
        V vDest = vertex(vDestKey);

        if (vOrig == null || vDest == null) {
            return null;
        }

        return edge(vOrig, vDest);
    }

    @Override
    public int outDegree(V vert) {
        if (!validVertex(vert)) {
            return -1;
        }

        MapVertex<V, E> mv = vertices.get(vert);
        return mv.numAdjVerts();
    }

    @Override
    public int inDegree(V vert) {
        if (!validVertex(vert)) {
            return -1;
        }

        int count = 0;

        // Count edges from other vertices to this vertex
        for (MapVertex<V, E> mv : vertices.values()) {
            if (mv.getEdge(vert) != null) {
                count++;
            }
        }

        return count;
    }

    @Override
    public Collection<Edge<V, E>> outgoingEdges(V vert) {
        if (!validVertex(vert)) {
            return null;
        }

        MapVertex<V, E> mv = vertices.get(vert);
        return mv.getOutgoing().values();
    }

    @Override
    public Collection<Edge<V, E>> incomingEdges(V vert) {
        if (!validVertex(vert)) {
            return null;
        }

        List<Edge<V, E>> incoming = new ArrayList<>();

        // Find all edges that have vert as destination
        for (V v : vertices.keySet()) {
            Edge<V, E> edge = edge(v, vert);
            if (edge != null) {
                incoming.add(edge);
            }
        }

        return incoming;
    }

    @Override
    public boolean addVertex(V vert) {
        if (vert == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }

        if (validVertex(vert)) {
            return false; // Vertex already exists
        }

        vertices.put(vert, new MapVertex<>(vert));
        return true;
    }

    @Override
    public boolean addEdge(V vOrig, V vDest, E weight) {
        if (vOrig == null || vDest == null) {
            throw new IllegalArgumentException("Vertices cannot be null");
        }

        // Add vertices if they don't exist
        if (!validVertex(vOrig)) {
            addVertex(vOrig);
        }
        if (!validVertex(vDest)) {
            addVertex(vDest);
        }

        // Check if edge already exists
        if (edge(vOrig, vDest) != null) {
            return false;
        }

        // Create edge
        Edge<V, E> edge = new Edge<>(vOrig, vDest, weight);

        // Add edge from vOrig to vDest
        MapVertex<V, E> mvOrig = vertices.get(vOrig);
        mvOrig.addEdge(vDest, edge);

        // If undirected, add reverse edge
        if (!isDirected) {
            Edge<V, E> reverseEdge = new Edge<>(vDest, vOrig, weight);
            MapVertex<V, E> mvDest = vertices.get(vDest);
            mvDest.addEdge(vOrig, reverseEdge);
        }

        return true;
    }

    @Override
    public boolean removeVertex(V vert) {
        if (!validVertex(vert)) {
            return false;
        }

        // Remove all edges to this vertex
        for (V v : vertices.keySet()) {
            removeEdge(v, vert);
        }

        // Remove vertex
        vertices.remove(vert);
        return true;
    }

    @Override
    public boolean removeEdge(V vOrig, V vDest) {
        if (!validVertex(vOrig) || !validVertex(vDest)) {
            return false;
        }

        // Check if edge exists
        if (edge(vOrig, vDest) == null) {
            return false;
        }

        // Remove edge from vOrig to vDest
        MapVertex<V, E> mvOrig = vertices.get(vOrig);
        mvOrig.removeEdge(vDest);

        // If undirected, remove reverse edge
        if (!isDirected) {
            MapVertex<V, E> mvDest = vertices.get(vDest);
            mvDest.removeEdge(vOrig);
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Graph<V, E> clone() {
        MapGraph<V, E> clone = new MapGraph<>(isDirected);

        // Clone vertices
        for (V v : vertices.keySet()) {
            clone.addVertex(v);
        }

        // Clone edges
        for (MapVertex<V, E> mv : vertices.values()) {
            V vOrig = mv.getElement();
            for (Map.Entry<V, Edge<V, E>> entry : mv.getOutgoing().entrySet()) {
                V vDest = entry.getKey();
                E weight = entry.getValue().getWeight();

                // Only add edge once for undirected graphs
                if (isDirected || key(vOrig) < key(vDest)) {
                    clone.addEdge(vOrig, vDest, weight);
                }
            }
        }

        return clone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Graph (").append(isDirected ? "directed" : "undirected").append(")\n");
        sb.append("Vertices: ").append(numVertices()).append("\n");
        sb.append("Edges: ").append(numEdges()).append("\n");
        sb.append("\nAdjacency List:\n");

        for (V v : vertices.keySet()) {
            sb.append(v).append(" -> ");

            Collection<V> adjacent = adjVertices(v);
            if (adjacent != null && !adjacent.isEmpty()) {
                sb.append(adjacent);
            } else {
                sb.append("[]");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        MapGraph<V, E> that = (MapGraph<V, E>) o;

        return isDirected == that.isDirected &&
                vertices.equals(that.vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDirected, vertices);
    }
}