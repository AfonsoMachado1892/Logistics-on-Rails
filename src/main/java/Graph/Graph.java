package Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Graph interface - Based on theoretical material (Slide 25-26)
 * Generic interface for graph data structures
 *
 * @param <V> Vertex element type
 * @param <E> Edge element type (weight)
 */
public interface Graph<V, E> extends Cloneable {

    /**
     * Returns true if graph is directed, false if undirected
     */
    boolean isDirected();

    /**
     * Returns the number of vertices of the graph
     */
    int numVertices();

    /**
     * Returns all vertices of the graph as an ArrayList
     */
    ArrayList<V> vertices();

    /**
     * Returns true if vert is a valid vertex of the graph
     */
    boolean validVertex(V vert);

    /**
     * Returns the key (index) of a vertex
     * Returns -1 if vertex does not exist
     */
    int key(V vert);

    /**
     * Returns the vertex at a given key (index)
     * Returns null if key is invalid
     */
    V vertex(int key);

    /**
     * Returns a vertex that satisfies a given predicate
     * Returns null if no vertex satisfies the predicate
     */
    V vertex(Predicate<V> p);

    /**
     * Returns a collection of adjacent vertices to vert
     * Returns null if vert is not valid
     */
    Collection<V> adjVertices(V vert);

    /**
     * Returns the number of edges in the graph
     */
    int numEdges();

    /**
     * Returns a collection of all edges in the graph
     */
    Collection<Edge<V, E>> edges();

    /**
     * Returns the edge from vOrig to vDest
     * Returns null if edge does not exist
     */
    Edge<V, E> edge(V vOrig, V vDest);

    /**
     * Returns the edge from vertex with key vOrigKey to vertex with key vDestKey
     * Returns null if edge does not exist
     */
    Edge<V, E> edge(int vOrigKey, int vDestKey);

    /**
     * Returns the out-degree of vertex vert
     * (number of edges leaving the vertex)
     */
    int outDegree(V vert);

    /**
     * Returns the in-degree of vertex vert
     * (number of edges entering the vertex)
     */
    int inDegree(V vert);

    /**
     * Returns a collection of outgoing edges from vertex vert
     */
    Collection<Edge<V, E>> outgoingEdges(V vert);

    /**
     * Returns a collection of incoming edges to vertex vert
     */
    Collection<Edge<V, E>> incomingEdges(V vert);

    /**
     * Adds a vertex to the graph
     * Returns false if vertex already exists
     */
    boolean addVertex(V vert);

    /**
     * Adds an edge from vOrig to vDest with given weight
     * If vertices do not exist, they are added to the graph
     * Returns false if edge already exists
     */
    boolean addEdge(V vOrig, V vDest, E weight);

    /**
     * Removes a vertex and all its incident edges from the graph
     * Returns false if vertex does not exist
     */
    boolean removeVertex(V vert);

    /**
     * Removes the edge from vOrig to vDest
     * Returns false if edge does not exist
     */
    boolean removeEdge(V vOrig, V vDest);

    /**
     * Returns a clone of the graph
     */
    Graph<V, E> clone();
}