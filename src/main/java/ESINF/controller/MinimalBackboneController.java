package ESINF.controller;

import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import ESINF.service.ConnectedComponentsAnalyzer;
import ESINF.service.MinimalBackboneResult;
import ESINF.service.RailwayNetworkMSTService;

/**
 * Controller for USEI12 - Minimal Backbone Network
 * Handles communication between UI and service layer
 *
 * Based on Sprint 3 requirements
 */
public class MinimalBackboneController {

    private final RailwayNetworkRepository repository;
    private RailwayNetworkMSTService mstService;
    private ConnectedComponentsAnalyzer analyzer;

    /**
     * Constructor with repository
     * @param repository the railway network repository
     */
    public MinimalBackboneController(RailwayNetworkRepository repository) {
        this.repository = repository;
        this.mstService = new RailwayNetworkMSTService(repository);
        this.analyzer = new ConnectedComponentsAnalyzer();
    }

    /**
     * Default constructor - creates own repository
     * NOTE: For integration with USEI11, prefer passing shared repository
     */
    public MinimalBackboneController() {
        this(new RailwayNetworkRepository());
    }

    /**
     * Gets the repository (for sharing with other controllers)
     * This allows USEI11 and USEI12 to share the same loaded data
     *
     * @return the railway network repository
     */
    public RailwayNetworkRepository getRepository() {
        return repository;
    }

    /**
     * Checks if railway network data is loaded
     * @return true if data loaded, false otherwise
     */
    public boolean isDataLoaded() {
        return repository.isDataLoaded();
    }

    /**
     * Gets the number of stations in the network
     * @return station count
     */
    public int getStationCount() {
        return repository.getVertexCount();
    }

    /**
     * Gets the number of connections in the network
     * @return connection count
     */
    public int getConnectionCount() {
        return repository.getEdgeCount();
    }

    /**
     * Loads railway network data from CSV files
     *
     * @param stationsPath path to stations.csv
     * @param linesPath path to lines.csv
     * @return LoadResult with statistics
     * @throws Exception if loading fails
     */
    public RailwayNetworkRepository.LoadResult loadData(String stationsPath, String linesPath)
            throws Exception {
        RailwayNetworkRepository.LoadResult result = repository.loadFromCSV(stationsPath, linesPath);

        // Reinitialize service with new data
        this.mstService = new RailwayNetworkMSTService(repository);

        return result;
    }

    /**
     * Computes the Minimal Backbone Network (MST)
     * Returns formatted string with results
     *
     * @return formatted MST result
     * @throws IllegalStateException if data not loaded
     */
    public String computeMST() {
        if (!isDataLoaded()) {
            throw new IllegalStateException("Railway network data not loaded");
        }

        MinimalBackboneResult<RailwayStation, Double> result =
                mstService.computeMinimalBackbone();

        return result.toString();
    }

    /**
     * Computes MST and exports to DOT format
     * Returns MST info + DOT content
     *
     * @return formatted result with DOT
     * @throws IllegalStateException if data not loaded
     */
    public String computeAndExportDOT() {
        if (!isDataLoaded()) {
            throw new IllegalStateException("Railway network data not loaded");
        }

        RailwayNetworkMSTService.MSTWithExportResult result =
                mstService.computeAndExport();

        StringBuilder output = new StringBuilder();
        output.append(result.getMstResult().getSummary());
        output.append("\n\n");
        output.append("=== DOT FORMAT ===\n");
        output.append(result.getDotContent());

        return output.toString();
    }

    /**
     * Computes MST and saves DOT + SVG files
     *
     * @param baseName base name for output files
     * @return formatted result with file paths
     * @throws IllegalStateException if data not loaded
     */
    public String computeAndSaveFiles(String baseName) {
        if (!isDataLoaded()) {
            throw new IllegalStateException("Railway network data not loaded");
        }

        RailwayNetworkMSTService.MSTWithFilesResult result =
                mstService.computeAndSaveFiles(baseName);

        return result.toString();
    }

    /**
     * Gets comparison statistics between original network and MST
     *
     * @return formatted comparison statistics
     * @throws IllegalStateException if data not loaded
     */
    public String getComparisonStats() {
        if (!isDataLoaded()) {
            throw new IllegalStateException("Railway network data not loaded");
        }

        RailwayNetworkMSTService.NetworkComparisonStats stats =
                mstService.getComparisonStats();

        return stats.toString();
    }

    /**
     * Analyzes connected components in the MST
     *
     * @return formatted component analysis
     * @throws IllegalStateException if data not loaded
     */
    public String analyzeComponents() {
        if (!isDataLoaded()) {
            throw new IllegalStateException("Railway network data not loaded");
        }

        // Compute MST first
        MinimalBackboneResult<RailwayStation, Double> mstResult =
                mstService.computeMinimalBackbone();

        // Analyze components
        ConnectedComponentsAnalyzer.ComponentAnalysisResult<RailwayStation, Double> analysis =
                analyzer.analyzeComponents(mstResult.getMstGraph());

        StringBuilder output = new StringBuilder();
        output.append(analysis.toString());

        // Add validation
        ConnectedComponentsAnalyzer.ValidationResult validation =
                analyzer.validateMSTStructure(mstResult.getMstGraph());

        output.append("\n");
        output.append(validation.toString());

        return output.toString();
    }


}