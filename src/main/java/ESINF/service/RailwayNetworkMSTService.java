package ESINF.service;

import ESINF.export.GraphVizExporter;
import ESINF.export.GraphVizFileGenerator;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import Graph.Graph;

/**
 * Integration service for computing MST on Railway Network
 * Bridges RailwayNetworkRepository with MinimalBackboneService
 *
 * Based on USEI12 requirements
 */
public class RailwayNetworkMSTService {

    private final RailwayNetworkRepository repository;
    private final MinimalBackboneService mstService;
    private final GraphVizExporter exporter;
    private final GraphVizFileGenerator fileGenerator;

    /**
     * Constructor with repository
     * @param repository the railway network repository
     */
    public RailwayNetworkMSTService(RailwayNetworkRepository repository) {
        this.repository = repository;
        this.mstService = new MinimalBackboneService();
        this.exporter = new GraphVizExporter();
        this.fileGenerator = new GraphVizFileGenerator();
    }

    /**
     * Computes the Minimal Backbone Network (MST) for the loaded railway network
     *
     * Process:
     * 1. Validates that data is loaded
     * 2. Converts directed graph to undirected
     * 3. Computes MST using Kruskal's algorithm
     * 4. Returns result
     *
     * @return MinimalBackboneResult with MST and statistics
     * @throws IllegalStateException if data is not loaded
     */
    public MinimalBackboneResult<RailwayStation, Double> computeMinimalBackbone() {
        // Validate data is loaded
        if (!repository.isDataLoaded()) {
            throw new IllegalStateException(
                    "Railway network data not loaded. Call loadFromCSV() first."
            );
        }

        // Get original directed graph
        Graph<RailwayStation, Double> directedGraph = repository.getGraph();

        if (directedGraph.numVertices() == 0) {
            throw new IllegalStateException("Graph has no vertices");
        }

        // Convert to undirected graph (MST requires undirected)
        Graph<RailwayStation, Double> undirectedGraph =
                mstService.convertToUndirected(directedGraph);

        // Compute MST using Kruskal's algorithm
        MinimalBackboneResult<RailwayStation, Double> result =
                mstService.computeMST(undirectedGraph);

        return result;
    }

    /**
     * Computes MST and exports to DOT format
     *
     * @return complete result with MST and DOT content
     */
    public MSTWithExportResult computeAndExport() {
        // Compute MST
        MinimalBackboneResult<RailwayStation, Double> mstResult = computeMinimalBackbone();

        // Export to DOT format
        String dotContent = exporter.exportToDOT(
                mstResult.getMstGraph(),
                "Belgian_Railway_MST"
        );

        return new MSTWithExportResult(mstResult, dotContent);
    }

    /**
     * Computes MST, exports to DOT, and saves to files (DOT + SVG)
     *
     * @param baseName base name for output files
     * @return complete result with file paths
     */
    public MSTWithFilesResult computeAndSaveFiles(String baseName) {
        // Compute and export
        MSTWithExportResult exportResult = computeAndExport();

        // Save DOT and generate SVG
        GraphVizFileGenerator.CompleteGenerationResult fileResult =
                fileGenerator.generateBothFiles(exportResult.getDotContent(), baseName);

        return new MSTWithFilesResult(
                exportResult.getMstResult(),
                exportResult.getDotContent(),
                fileResult
        );
    }

    /**
     * Quick method to compute MST and save with default filename
     * @return result with file paths
     */
    public MSTWithFilesResult computeAndSaveFiles() {
        return computeAndSaveFiles("railway_mst");
    }

    /**
     * Gets statistics about the original network vs MST
     * @return comparison statistics
     */
    public NetworkComparisonStats getComparisonStats() {
        if (!repository.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded");
        }

        // Original network stats
        int originalVertices = repository.getVertexCount();
        int originalEdges = repository.getEdgeCount();

        // Compute MST
        MinimalBackboneResult<RailwayStation, Double> mstResult = computeMinimalBackbone();

        // MST stats
        int mstVertices = mstResult.getVertexCount();
        int mstEdges = mstResult.getEdgeCount();
        double mstWeight = mstResult.getTotalWeight();

        return new NetworkComparisonStats(
                originalVertices,
                originalEdges,
                mstVertices,
                mstEdges,
                mstWeight,
                mstResult.getNumComponents()
        );
    }

    /**
     * Result class combining MST result with DOT export
     */
    public static class MSTWithExportResult {
        private final MinimalBackboneResult<RailwayStation, Double> mstResult;
        private final String dotContent;

        public MSTWithExportResult(MinimalBackboneResult<RailwayStation, Double> mstResult,
                                   String dotContent) {
            this.mstResult = mstResult;
            this.dotContent = dotContent;
        }

        public MinimalBackboneResult<RailwayStation, Double> getMstResult() {
            return mstResult;
        }

        public String getDotContent() {
            return dotContent;
        }
    }

    /**
     * Result class combining MST result with file generation
     */
    public static class MSTWithFilesResult {
        private final MinimalBackboneResult<RailwayStation, Double> mstResult;
        private final String dotContent;
        private final GraphVizFileGenerator.CompleteGenerationResult fileResult;

        public MSTWithFilesResult(MinimalBackboneResult<RailwayStation, Double> mstResult,
                                  String dotContent,
                                  GraphVizFileGenerator.CompleteGenerationResult fileResult) {
            this.mstResult = mstResult;
            this.dotContent = dotContent;
            this.fileResult = fileResult;
        }

        public MinimalBackboneResult<RailwayStation, Double> getMstResult() {
            return mstResult;
        }

        public String getDotContent() {
            return dotContent;
        }

        public GraphVizFileGenerator.CompleteGenerationResult getFileResult() {
            return fileResult;
        }

        public String getDotFilePath() {
            return fileResult.getDotFilePath();
        }

        public String getSvgFilePath() {
            return fileResult.getSvgFilePath();
        }

        public boolean hasSVG() {
            return fileResult.hasSVG();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== MST Computation Complete ===\n\n");
            sb.append(mstResult.getSummary()).append("\n\n");
            sb.append(fileResult.toString());
            return sb.toString();
        }
    }

    /**
     * Statistics comparing original network with MST
     */
    public static class NetworkComparisonStats {
        private final int originalVertices;
        private final int originalEdges;
        private final int mstVertices;
        private final int mstEdges;
        private final double mstWeight;
        private final int numComponents;

        public NetworkComparisonStats(int originalVertices, int originalEdges,
                                      int mstVertices, int mstEdges,
                                      double mstWeight, int numComponents) {
            this.originalVertices = originalVertices;
            this.originalEdges = originalEdges;
            this.mstVertices = mstVertices;
            this.mstEdges = mstEdges;
            this.mstWeight = mstWeight;
            this.numComponents = numComponents;
        }

        public int getOriginalVertices() {
            return originalVertices;
        }

        public int getOriginalEdges() {
            return originalEdges;
        }

        public int getMstVertices() {
            return mstVertices;
        }

        public int getMstEdges() {
            return mstEdges;
        }

        public double getMstWeight() {
            return mstWeight;
        }

        public int getNumComponents() {
            return numComponents;
        }

        public int getEdgesReduced() {
            return originalEdges - mstEdges;
        }

        public double getReductionPercentage() {
            return ((double) getEdgesReduced() / originalEdges) * 100.0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Network Comparison ===\n");
            sb.append(String.format("Original Network: %d vertices, %d edges\n",
                    originalVertices, originalEdges));
            sb.append(String.format("MST Network: %d vertices, %d edges\n",
                    mstVertices, mstEdges));
            sb.append(String.format("Edges reduced: %d (%.1f%% reduction)\n",
                    getEdgesReduced(), getReductionPercentage()));
            sb.append(String.format("Total MST weight: %.2f km\n", mstWeight));
            sb.append(String.format("Connected components: %d\n", numComponents));
            return sb.toString();
        }
    }
}