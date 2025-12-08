package ESINF.export;

import ESINF.model.RailwayStation;
import Graph.Edge;
import Graph.Graph;

import java.util.Collection;
import java.util.Locale;

public class GraphVizExporter {

    private static final String DEFAULT_NODE_SHAPE = "circle";
    private static final String DEFAULT_NODE_COLOR = "lightblue";
    private static final String DEFAULT_NODE_STYLE = "filled";
    private static final String DEFAULT_EDGE_COLOR = "black";
    private static final double COORD_SCALE = 1.0; // Reduzido para evitar coords gigantes
    private static final double NORMALIZED_MAX = 1000.0; // Range máximo para normalização
    private static final boolean USE_SPLINES = false; // false para velocidade; true para curvas suaves

    public String exportToDOT(Graph<RailwayStation, Double> graph, String graphName) {
        if (graph == null) throw new IllegalArgumentException("Graph cannot be null");
        if (graphName == null || graphName.trim().isEmpty()) graphName = "RailwayNetwork";

        StringBuilder dot = new StringBuilder();
        String graphType = graph.isDirected() ? "digraph" : "graph";
        String edgeOp = graph.isDirected() ? "->" : "--";

        dot.append(graphType).append(" ").append(sanitizeName(graphName)).append(" {\n");

        // Definições do Grafo (otimizadas para velocidade)
        dot.append("  layout=neato;\n");
        dot.append("  overlap=false;\n");
        dot.append("  splines=").append(USE_SPLINES ? "true" : "false").append(";\n");
        dot.append("  sep=0.5;\n\n");

        // Nós (Pequenos e fixos)
        dot.append("  node [shape=").append(DEFAULT_NODE_SHAPE)
                .append(", style=").append(DEFAULT_NODE_STYLE)
                .append(", color=").append(DEFAULT_NODE_COLOR)
                .append(", fixedsize=true, width=0.15, height=0.15")
                .append(", fontsize=8];\n\n");

        // Arestas
        dot.append("  edge [color=").append(DEFAULT_EDGE_COLOR)
                .append(", penwidth=0.5")
                .append(", fontsize=8];\n\n");

        // Normalizar coordenadas
        double[] bounds = computeBounds(graph.vertices());
        double minX = bounds[0], maxX = bounds[1], minY = bounds[2], maxY = bounds[3];
        double scaleX = (maxX > minX) ? NORMALIZED_MAX / (maxX - minX) : 1.0;
        double scaleY = (maxY > minY) ? NORMALIZED_MAX / (maxY - minY) : 1.0;
        double scale = Math.min(scaleX, scaleY); // Preserva aspect ratio

        // Vértices com coords normalizadas
        for (RailwayStation station : graph.vertices()) {
            dot.append("  ").append(formatVertex(station, minX, minY, scale)).append(";\n");
        }
        dot.append("\n");

        // Arestas
        for (Edge<RailwayStation, Double> edge : graph.edges()) {
            RailwayStation origin = edge.getVOrig();
            RailwayStation dest = edge.getVDest();
            dot.append("  ").append(formatEdge(origin, dest, edge.getWeight(), edgeOp)).append(";\n");
        }

        dot.append("}\n");
        return dot.toString();
    }

    // Calcula min/max X/Y
    private double[] computeBounds(Collection<RailwayStation> stations) {
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (RailwayStation s : stations) {
            double x = s.getCoordX() * COORD_SCALE;
            double y = s.getCoordY() * COORD_SCALE;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        return new double[]{minX, maxX, minY, maxY};
    }

    private String formatVertex(RailwayStation station, double minX, double minY, double scale) {
        StringBuilder vertex = new StringBuilder();
        vertex.append("\"").append(sanitizeLabel(station.getName())).append("\"");
        vertex.append(" [");

        double rawX = station.getCoordX() * COORD_SCALE;
        double rawY = station.getCoordY() * COORD_SCALE;
        double x = (rawX - minX) * scale;
        double y = (rawY - minY) * scale;

        vertex.append("pos=\"").append(String.format(Locale.US, "%.2f,%.2f", x, y)).append("!\"");

        vertex.append(", label=\"").append(escapeLabel(station.getName())).append("\"");
        vertex.append(", tooltip=\"ID: ").append(station.getStationId()).append("\"");
        vertex.append("]");
        return vertex.toString();
    }

    private String formatEdge(RailwayStation origin, RailwayStation dest, Double weight, String edgeOp) {
        StringBuilder edge = new StringBuilder();
        edge.append("\"").append(sanitizeLabel(origin.getName())).append("\"");
        edge.append(" ").append(edgeOp).append(" ");
        edge.append("\"").append(sanitizeLabel(dest.getName())).append("\"");
        edge.append(" [");

        edge.append("label=\"").append(String.format(Locale.US, "%.1f", weight)).append(" km\"");

        edge.append(", len=").append(String.format(Locale.US, "%.2f", weight / 50.0));

        edge.append(", tooltip=\"Distance: ").append(String.format(Locale.US, "%.2f", weight)).append(" km\"");
        edge.append("]");
        return edge.toString();
    }

    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String sanitizeLabel(String label) {
        return label.replaceAll("\"", "'");
    }

    private String escapeLabel(String label) {
        return label.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}