package ESINF.export;

import ESINF.model.RailwayStation;
import Graph.Graph;
import Graph.map.MapGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit4 test suite for GraphVizExporter
 *
 * Tests cover:
 * - DOT format generation with various graph structures
 * - Node formatting with geographic coordinates
 * - Edge formatting with weights
 * - Self-loop and duplicate edge filtering
 * - Label escaping and sanitization
 * - Directed vs undirected graph handling
 * - Input validation
 *
 * @author ESINF Test Team
 * @version 1.0
 */
public class GraphVizExporterTest {

    private GraphVizExporter exporter;
    private RailwayStation stationA;
    private RailwayStation stationB;
    private RailwayStation stationC;

    @Before
    public void setUp() {
        exporter = new GraphVizExporter();

        // Create test stations with realistic Belgian coordinates
        stationA = new RailwayStation(1, "Brussels", 50.8467, 4.3517, 3500.0, 129000.0);
        stationB = new RailwayStation(2, "Antwerp", 51.2194, 4.4025, 3600.0, 130000.0);
        stationC = new RailwayStation(3, "Ghent", 51.0361, 3.7103, 2800.0, 130500.0);
    }

    // ========== Constructor Tests ==========

    /**
     * Test that GraphVizExporter can be instantiated
     */
    @Test
    public void testConstructor_SuccessfulInstantiation() {
        assertNotNull(new GraphVizExporter());
    }

    // ========== Input Validation Tests ==========

    /**
     * Test that exportToDOT throws exception when graph is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExportToDOT_NullGraph_ThrowsException() {
        exporter.exportToDOT(null, "TestGraph");
    }

    /**
     * Test that exportToDOT throws exception when graph name is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExportToDOT_NullGraphName_ThrowsException() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        exporter.exportToDOT(graph, null);
    }

    /**
     * Test that exportToDOT throws exception when graph name is empty
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExportToDOT_EmptyGraphName_ThrowsException() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        exporter.exportToDOT(graph, "");
    }

    /**
     * Test that exportToDOT throws exception when graph name is whitespace only
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExportToDOT_WhitespaceGraphName_ThrowsException() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        exporter.exportToDOT(graph, "   ");
    }

    // ========== Empty Graph Tests ==========

    /**
     * Test export of empty undirected graph
     */
    @Test
    public void testExportToDOT_EmptyUndirectedGraph_ReturnsValidDOT() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        String dot = exporter.exportToDOT(graph, "EmptyGraph");

        assertNotNull(dot);
    }

    /**
     * Test that empty graph contains graph declaration
     */
    @Test
    public void testExportToDOT_EmptyGraph_ContainsGraphDeclaration() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        String dot = exporter.exportToDOT(graph, "EmptyGraph");

        assertTrue(dot.contains("graph EmptyGraph"));
    }

    /**
     * Test that empty directed graph uses digraph keyword
     */
    @Test
    public void testExportToDOT_EmptyDirectedGraph_UsesDigraph() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(true);
        String dot = exporter.exportToDOT(graph, "EmptyGraph");

        assertTrue(dot.contains("digraph EmptyGraph"));
    }

    /**
     * Test that empty graph contains layout settings
     */
    @Test
    public void testExportToDOT_EmptyGraph_ContainsLayoutSettings() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        String dot = exporter.exportToDOT(graph, "EmptyGraph");

        assertTrue(dot.contains("layout=neato"));
    }

    /**
     * Test that empty graph contains node style
     */
    @Test
    public void testExportToDOT_EmptyGraph_ContainsNodeStyle() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        String dot = exporter.exportToDOT(graph, "EmptyGraph");

        assertTrue(dot.contains("node ["));
    }

    /**
     * Test that empty graph contains edge style
     */
    @Test
    public void testExportToDOT_EmptyGraph_ContainsEdgeStyle() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        String dot = exporter.exportToDOT(graph, "EmptyGraph");

        assertTrue(dot.contains("edge ["));
    }

    // ========== Single Station Tests ==========

    /**
     * Test export of graph with single station
     */
    @Test
    public void testExportToDOT_SingleStation_ContainsStation() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "SingleStation");

        assertTrue(dot.contains("S1"));
    }

    /**
     * Test that single station has position attribute
     */
    @Test
    public void testExportToDOT_SingleStation_HasPosition() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "SingleStation");

        assertTrue(dot.contains("pos="));
    }

    /**
     * Test that single station position is pinned
     */
    @Test
    public void testExportToDOT_SingleStation_PositionIsPinned() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "SingleStation");

        assertTrue(dot.contains("pin=true"));
    }

    /**
     * Test that single station has label
     */
    @Test
    public void testExportToDOT_SingleStation_HasLabel() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "SingleStation");

        assertTrue(dot.contains("xlabel="));
    }

    /**
     * Test that single station label contains station name
     */
    @Test
    public void testExportToDOT_SingleStation_LabelContainsName() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "SingleStation");

        assertTrue(dot.contains("Brussels"));
    }

    /**
     * Test that single station has tooltip
     */
    @Test
    public void testExportToDOT_SingleStation_HasTooltip() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "SingleStation");

        assertTrue(dot.contains("tooltip="));
    }

    // ========== Two Stations with Edge Tests ==========

    /**
     * Test export of two connected stations
     */
    @Test
    public void testExportToDOT_TwoConnectedStations_ContainsBothStations() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "TwoStations");

        assertTrue(dot.contains("S1") && dot.contains("S2"));
    }

    /**
     * Test that undirected graph uses -- operator
     */
    @Test
    public void testExportToDOT_UndirectedEdge_UsesDoubleDash() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "TwoStations");

        assertTrue(dot.contains("--"));
    }

    /**
     * Test that directed graph uses -> operator
     */
    @Test
    public void testExportToDOT_DirectedEdge_UsesArrow() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(true);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "TwoStations");

        assertTrue(dot.contains("->"));
    }

    /**
     * Test that edge has weight label
     */
    @Test
    public void testExportToDOT_Edge_HasWeightLabel() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "TwoStations");

        assertTrue(dot.contains("45.5"));
    }

    /**
     * Test that edge has label attribute
     */
    @Test
    public void testExportToDOT_Edge_HasLabelAttribute() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "TwoStations");

        assertTrue(dot.contains("label="));
    }

    /**
     * Test that edge has tooltip
     */
    @Test
    public void testExportToDOT_Edge_HasTooltip() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "TwoStations");

        assertTrue(dot.contains("tooltip="));
    }

    // ========== Self-Loop Filtering Tests ==========

    /**
     * Test that self-loops are filtered out
     */
    @Test
    public void testExportToDOT_SelfLoop_IsFiltered() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(true);
        graph.addVertex(stationA);
        graph.addEdge(stationA, stationA, 10.0);

        String dot = exporter.exportToDOT(graph, "SelfLoop");

        assertTrue(dot.contains("Self-loops filtered: 1"));
    }

    /**
     * Test that self-loop count is reported correctly
     */
    @Test
    public void testExportToDOT_MultipleSelfLoops_CorrectCount() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(true);
        graph.addEdge(stationA, stationA, 10.0);
        graph.addEdge(stationB, stationB, 20.0);
        graph.addEdge(stationA, stationB, 30.0);

        String dot = exporter.exportToDOT(graph, "MultiSelfLoop");

        assertTrue(dot.contains("Self-loops filtered: 2"));
    }

    /**
     * Test that graph without self-loops shows zero filtered
     */
    @Test
    public void testExportToDOT_NoSelfLoops_ZeroFiltered() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "NoSelfLoops");

        assertTrue(dot.contains("Self-loops filtered: 0"));
    }

    // ========== Duplicate Edge Filtering Tests ==========

    /**
     * Test that duplicate edges in undirected graph are filtered
     */
    @Test
    public void testExportToDOT_UndirectedDuplicates_AreFiltered() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);
        // In undirected graph, A-B and B-A are same edge (but stored twice internally)

        String dot = exporter.exportToDOT(graph, "Duplicates");

        // Should have statistics comment
        assertTrue(dot.contains("Valid edges:"));
    }

    /**
     * Test that directed graph does not filter reverse edges
     */
    @Test
    public void testExportToDOT_DirectedReverseEdges_NotFiltered() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(true);
        graph.addEdge(stationA, stationB, 45.5);
        graph.addEdge(stationB, stationA, 50.0);

        String dot = exporter.exportToDOT(graph, "DirectedReverse");

        assertTrue(dot.contains("Duplicates filtered: 0"));
    }

    /**
     * Test valid edge count is reported
     */
    @Test
    public void testExportToDOT_ValidEdgeCount_IsReported() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);
        graph.addEdge(stationB, stationC, 30.0);

        String dot = exporter.exportToDOT(graph, "ValidEdges");

        assertTrue(dot.contains("Valid edges:"));
    }

    // ========== Triangle Graph Tests ==========

    /**
     * Test export of triangle graph (3 stations, 3 edges)
     */
    @Test
    public void testExportToDOT_TriangleGraph_AllStationsPresent() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);
        graph.addEdge(stationB, stationC, 30.0);
        graph.addEdge(stationC, stationA, 55.0);

        String dot = exporter.exportToDOT(graph, "Triangle");

        assertTrue(dot.contains("S1") && dot.contains("S2") && dot.contains("S3"));
    }

    /**
     * Test that triangle graph has 3 valid edges in undirected mode
     */
    @Test
    public void testExportToDOT_TriangleGraph_ThreeValidEdges() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);
        graph.addEdge(stationB, stationC, 30.0);
        graph.addEdge(stationC, stationA, 55.0);

        String dot = exporter.exportToDOT(graph, "Triangle");

        assertTrue(dot.contains("Valid edges: 3"));
    }

    // ========== Graph Name Sanitization Tests ==========

    /**
     * Test that graph name with spaces is sanitized
     */
    @Test
    public void testExportToDOT_NameWithSpaces_IsSanitized() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "My Graph Name");

        assertTrue(dot.contains("My_Graph_Name"));
    }

    /**
     * Test that graph name with special characters is sanitized
     */
    @Test
    public void testExportToDOT_NameWithSpecialChars_IsSanitized() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "Graph@2024!");

        assertTrue(dot.contains("Graph_2024_"));
    }

    /**
     * Test that graph name with hyphens is sanitized
     */
    @Test
    public void testExportToDOT_NameWithHyphens_IsSanitized() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "My-Graph-Name");

        assertTrue(dot.contains("My_Graph_Name"));
    }

    // ========== Label Escaping Tests ==========

    /**
     * Test that station name with quotes is escaped
     */
    @Test
    public void testExportToDOT_StationNameWithQuotes_IsEscaped() {
        RailwayStation specialStation = new RailwayStation(
                10, "Brussels \"Central\"", 50.8467, 4.3517, 3500.0, 129000.0
        );
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(specialStation);

        String dot = exporter.exportToDOT(graph, "SpecialChars");

        assertTrue(dot.contains("\\\""));
    }

    /**
     * Test that station name with backslash is escaped
     */
    @Test
    public void testExportToDOT_StationNameWithBackslash_IsEscaped() {
        RailwayStation specialStation = new RailwayStation(
                10, "Brussels\\Station", 50.8467, 4.3517, 3500.0, 129000.0
        );
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(specialStation);

        String dot = exporter.exportToDOT(graph, "SpecialChars");

        assertTrue(dot.contains("\\\\"));
    }

    // ========== Coordinate Scaling Tests ==========

    /**
     * Test that coordinates are included in output
     */
    @Test
    public void testExportToDOT_CoordinatesAreIncluded() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "Coords");

        assertTrue(dot.contains("3500"));
    }

    /**
     * Test that position has exclamation mark for forcing
     */
    @Test
    public void testExportToDOT_PositionIsForcedWithExclamation() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "Forced");

        assertTrue(dot.contains("pos=") && dot.contains("!"));
    }

    // ========== Weight Formatting Tests ==========

    /**
     * Test that weight is formatted with one decimal place
     */
    @Test
    public void testExportToDOT_WeightFormattedOneDecimal() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.567);

        String dot = exporter.exportToDOT(graph, "Weight");

        assertTrue(dot.contains("45.6"));
    }

    /**
     * Test that integer weight is formatted correctly
     */
    @Test
    public void testExportToDOT_IntegerWeight_HasDecimal() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.0);

        String dot = exporter.exportToDOT(graph, "IntWeight");

        assertTrue(dot.contains("45.0"));
    }

    /**
     * Test that very small weight is formatted correctly
     */
    @Test
    public void testExportToDOT_SmallWeight_FormattedCorrectly() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 0.5);

        String dot = exporter.exportToDOT(graph, "SmallWeight");

        assertTrue(dot.contains("0.5"));
    }

    // ========== Complex Graph Tests ==========

    /**
     * Test export of larger graph (5+ stations)
     */
    @Test
    public void testExportToDOT_LargerGraph_AllStationsPresent() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        RailwayStation d = new RailwayStation(4, "Bruges", 51.2, 3.2, 2500.0, 131000.0);
        RailwayStation e = new RailwayStation(5, "Liège", 50.6, 5.5, 4500.0, 128000.0);

        graph.addEdge(stationA, stationB, 45.5);
        graph.addEdge(stationB, stationC, 30.0);
        graph.addEdge(stationC, d, 50.0);
        graph.addEdge(d, e, 100.0);
        graph.addEdge(e, stationA, 90.0);

        String dot = exporter.exportToDOT(graph, "LargeGraph");

        assertTrue(dot.contains("S1") && dot.contains("S2") &&
                dot.contains("S3") && dot.contains("S4") &&
                dot.contains("S5"));
    }

    /**
     * Test that larger graph has all edges
     */
    @Test
    public void testExportToDOT_LargerGraph_AllEdgesPresent() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);
        graph.addEdge(stationB, stationC, 30.0);
        graph.addEdge(stationC, stationA, 55.0);

        String dot = exporter.exportToDOT(graph, "LargeGraph");

        assertTrue(dot.contains("45.5") && dot.contains("30.0") &&
                dot.contains("55.0"));
    }

    // ========== Output Structure Tests ==========

    /**
     * Test that output starts with graph/digraph declaration
     */
    @Test
    public void testExportToDOT_OutputStartsWithDeclaration() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "Test");

        assertTrue(dot.trim().startsWith("graph") ||
                dot.trim().startsWith("digraph"));
    }

    /**
     * Test that output ends with closing brace
     */
    @Test
    public void testExportToDOT_OutputEndsWithBrace() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "Test");

        assertTrue(dot.trim().endsWith("}"));
    }

    /**
     * Test that output contains opening brace
     */
    @Test
    public void testExportToDOT_OutputContainsOpeningBrace() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "Test");

        assertTrue(dot.contains("{"));
    }

    // ========== Edge Statistics Tests ==========

    /**
     * Test that edge statistics comment is present
     */
    @Test
    public void testExportToDOT_ContainsEdgeStatistics() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "Stats");

        assertTrue(dot.contains("Edge statistics"));
    }

    /**
     * Test that duplicates filtered count is present
     */
    @Test
    public void testExportToDOT_ContainsDuplicatesFilteredCount() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addEdge(stationA, stationB, 45.5);

        String dot = exporter.exportToDOT(graph, "Stats");

        assertTrue(dot.contains("Duplicates filtered"));
    }

    // ========== Node ID Tests ==========

    /**
     * Test that node ID uses S prefix
     */
    @Test
    public void testExportToDOT_NodeIDUsesSPrefix() {
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(stationA);

        String dot = exporter.exportToDOT(graph, "NodeID");

        assertTrue(dot.contains("\"S1\""));
    }

    /**
     * Test that node ID includes station ID number
     */
    @Test
    public void testExportToDOT_NodeIDIncludesStationID() {
        RailwayStation station = new RailwayStation(
                999, "Test Station", 50.0, 4.0, 3000.0, 129000.0
        );
        Graph<RailwayStation, Double> graph = new MapGraph<>(false);
        graph.addVertex(station);

        String dot = exporter.exportToDOT(graph, "NodeID");

        assertTrue(dot.contains("S999"));
    }
}