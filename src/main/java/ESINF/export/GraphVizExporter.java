package ESINF.export;

import ESINF.model.RailwayStation;
import Graph.Edge;
import Graph.Graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * FIXED GraphViz exporter for Minimal Backbone Network
 *
 * FIXES:
 * 1. Prevents self-loops (edges from station to itself)
 * 2. Prevents duplicate edges in undirected graphs
 * 3. Uses proper coordinate scaling for Belgium
 * 4. Ensures neato respects fixed positions
 */
public class GraphVizExporter {

    // Visual style constants (matching professor's example)
    private static final String NODE_SHAPE = "point";
    private static final String NODE_STYLE = "filled";
    private static final String NODE_FILLCOLOR = "white";
    private static final String NODE_COLOR = "gray";
    private static final String NODE_WIDTH = "0.05";

    private static final String EDGE_COLOR = "red";
    private static final String EDGE_PENWIDTH = "1.0";

    // Coordinate scaling - ADJUSTED for Belgium coordinates
    // Belgium coords are roughly: X: 0-7200, Y: 126000-133000
    // We want to scale to reasonable SVG size: X: 0-720, Y: 1260-1330
    private static final double COORD_SCALE = 1;  // Changed from 0.01

    // Label settings
    private static final boolean SHOW_STATION_LABELS = true;
    private static final boolean SHOW_EDGE_WEIGHTS = true;
    private static final String LABEL_FONTSIZE = "3";
    private static final String EDGE_FONTSIZE = "5";

    /**
     * Exports graph to DOT format with geographic layout
     * FIXED: Filters self-loops and duplicate edges
     *
     * @param graph the railway network graph
     * @param graphName name for the graph
     * @return DOT format string
     */
    public String exportToDOT(Graph<RailwayStation, Double> graph, String graphName) {
        validateInputs(graph, graphName);

        StringBuilder dot = new StringBuilder();
        String graphType = graph.isDirected() ? "digraph" : "graph";
        String edgeOp = graph.isDirected() ? "->" : "--";

        // Graph header
        dot.append(graphType).append(" ").append(sanitizeName(graphName)).append(" {\n");

        // Graph layout settings
        appendGraphSettings(dot);

        // Node style
        appendNodeStyle(dot);

        // Edge style
        appendEdgeStyle(dot);

        // Add all nodes with positions
        appendNodes(dot, graph.vertices());

        // Add all edges with weights (FIXED: filter self-loops and duplicates)
        appendEdgesFiltered(dot, graph, edgeOp);

        dot.append("}\n");
        return dot.toString();
    }

    /**
     * Validates inputs
     */
    private void validateInputs(Graph<RailwayStation, Double> graph, String graphName) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (graphName == null || graphName.trim().isEmpty()) {
            throw new IllegalArgumentException("Graph name cannot be null or empty");
        }
    }

    /**
     * Appends graph-level settings
     * CRITICAL: These settings ensure neato respects positions
     */
    private void appendGraphSettings(StringBuilder dot) {
        dot.append("  // Graph layout settings\n");
        dot.append("  layout=neato;\n");
        dot.append("  mode=\"KK\";\n");                  // Use Kamada-Kawai for better layout
        dot.append("  overlap=false;\n");
        dot.append("  splines=false;\n");                // Straight lines
        dot.append("  sep=0.5;\n");
        dot.append("  outputorder=edgesfirst;\n");
        dot.append("  dim=2;\n");                        // 2D layout
        dot.append("\n");
    }

    /**
     * Appends node style
     */
    private void appendNodeStyle(StringBuilder dot) {
        dot.append("  // Node style (small points)\n");
        dot.append("  node [");
        dot.append("shape=").append(NODE_SHAPE);
        dot.append(", style=").append(NODE_STYLE);
        dot.append(", fillcolor=").append(NODE_FILLCOLOR);
        dot.append(", color=").append(NODE_COLOR);
        dot.append(", width=").append(NODE_WIDTH);
        dot.append("];\n\n");
    }

    /**
     * Appends edge style
     */
    private void appendEdgeStyle(StringBuilder dot) {
        dot.append("  // Edge style (red lines for MST)\n");
        dot.append("  edge [");
        dot.append("color=").append(EDGE_COLOR);
        dot.append(", penwidth=").append(EDGE_PENWIDTH);
        dot.append("];\n\n");
    }

    /**
     * Appends all nodes with geographic positions
     */
    private void appendNodes(StringBuilder dot, Collection<RailwayStation> stations) {
        dot.append("  // Stations with geographic positions\n");

        for (RailwayStation station : stations) {
            dot.append("  ");
            dot.append(formatNode(station));
            dot.append(";\n");
        }

        dot.append("\n");
    }

    /**
     * Formats a single node with position and label
     */
    private String formatNode(RailwayStation station) {
        StringBuilder node = new StringBuilder();

        // Node identifier
        String nodeId = getNodeId(station);
        node.append("\"").append(nodeId).append("\"");

        // Node attributes
        node.append(" [");

        // Fixed position with "!" to force it
        double x = station.getCoordX() * COORD_SCALE;
        double y = station.getCoordY() * COORD_SCALE;
        node.append("pos=\"");
        node.append(String.format(Locale.US, "%.2f,%.2f", x, y));
        node.append("!\"");  // "!" forces neato to use this exact position

        // Pin the node (prevents movement)
        node.append(", pin=true");

        // Label (station name + ID)
        if (SHOW_STATION_LABELS) {
            node.append(", xlabel=\"");
            node.append(escapeLabel(station.getName()));
            node.append("\\n");
            node.append("(").append(station.getStationId()).append(")");
            node.append("\"");
            node.append(", fontsize=").append(LABEL_FONTSIZE);
        }

        // Tooltip
        node.append(", tooltip=\"");
        node.append("ID: ").append(station.getStationId());
        node.append(" | ").append(station.getName());
        node.append("\"");

        node.append("]");

        return node.toString();
    }

    /**
     * Appends all edges with filtering
     * FIXED: Removes self-loops and prevents duplicates in undirected graphs
     */
    private void appendEdgesFiltered(StringBuilder dot, Graph<RailwayStation, Double> graph, String edgeOp) {
        dot.append("  // Edges with distances (filtered)\n");

        Collection<Edge<RailwayStation, Double>> edges = graph.edges();
        Set<String> processedEdges = new HashSet<>();
        int selfLoops = 0;
        int duplicates = 0;
        int validEdges = 0;

        for (Edge<RailwayStation, Double> edge : edges) {
            RailwayStation origin = edge.getVOrig();
            RailwayStation dest = edge.getVDest();

            // FILTER 1: Skip self-loops
            if (origin.getStationId() == dest.getStationId()) {
                selfLoops++;
                continue;
            }

            // FILTER 2: For undirected graphs, skip duplicate edges
            if (!graph.isDirected()) {
                String edgeKey = createEdgeKey(origin, dest);
                if (processedEdges.contains(edgeKey)) {
                    duplicates++;
                    continue;
                }
                processedEdges.add(edgeKey);
            }

            // Valid edge - add it
            dot.append("  ");
            dot.append(formatEdge(edge, edgeOp));
            dot.append(";\n");
            validEdges++;
        }

        // Add comment with statistics
        dot.append("\n");
        dot.append("  // Edge statistics:\n");
        dot.append("  // Valid edges: ").append(validEdges).append("\n");
        dot.append("  // Self-loops filtered: ").append(selfLoops).append("\n");
        dot.append("  // Duplicates filtered: ").append(duplicates).append("\n");
    }

    /**
     * Creates unique edge key for undirected graph deduplication
     */
    private String createEdgeKey(RailwayStation v1, RailwayStation v2) {
        int id1 = v1.getStationId();
        int id2 = v2.getStationId();

        // Ensure consistent ordering (smaller ID first)
        if (id1 < id2) {
            return id1 + "-" + id2;
        } else {
            return id2 + "-" + id1;
        }
    }

    /**
     * Formats a single edge with weight label
     */
    private String formatEdge(Edge<RailwayStation, Double> edge, String edgeOp) {
        StringBuilder edgeStr = new StringBuilder();

        RailwayStation origin = edge.getVOrig();
        RailwayStation dest = edge.getVDest();
        Double weight = edge.getWeight();

        // Edge definition
        edgeStr.append("\"").append(getNodeId(origin)).append("\"");
        edgeStr.append(" ").append(edgeOp).append(" ");
        edgeStr.append("\"").append(getNodeId(dest)).append("\"");

        // Edge attributes
        edgeStr.append(" [");

        // Weight label
        if (SHOW_EDGE_WEIGHTS) {
            edgeStr.append("label=\"");
            edgeStr.append(String.format(Locale.US, "%.1f", weight));
            edgeStr.append("\"");
            edgeStr.append(", fontsize=").append(EDGE_FONTSIZE);
        }

        // Tooltip
        edgeStr.append(", tooltip=\"");
        edgeStr.append(origin.getName());
        edgeStr.append(" <-> ");
        edgeStr.append(dest.getName());
        edgeStr.append(" | ");
        edgeStr.append(String.format(Locale.US, "%.2f", weight));
        edgeStr.append(" km\"");

        edgeStr.append("]");

        return edgeStr.toString();
    }

    /**
     * Gets unique node identifier using station ID
     */
    private String getNodeId(RailwayStation station) {
        return "S" + station.getStationId();
    }

    /**
     * Sanitizes graph name for DOT format
     */
    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    /**
     * Escapes special characters in labels
     */
    private String escapeLabel(String label) {
        return label
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}