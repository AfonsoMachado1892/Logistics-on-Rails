package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class KDTree2DTest {

    private KDTree2D tree;
    private Station portoCampanha;
    private Station portoSaoBento;
    private Station aveiro;
    private Station madridChamartin;
    private Station lisbonOriente;
    private List<Station> stations;

    // New stations for USEI09 tests
    private Station parisGareLyon;
    private Station berlinHbf;
    private Station kyivPas;

    @Before
    public void setUp() {
        // Original Stations
        portoCampanha = buildStation("Porto Campanhã", 41.14961, -8.61099, "PT", true, true);
        portoSaoBento = buildStation("Porto São Bento", 41.14961, -8.61099, "PT", false, false);
        aveiro = buildStation("Aveiro", 40.64050, -8.65370, "PT", true, false);
        madridChamartin = buildStation("Madrid Chamartín", 40.47295, -3.68920, "ES", true, true);
        lisbonOriente = buildStation("Lisboa Oriente", 38.76792, -9.09944, "PT", true, true);

        // Initialize new stations (BUT DO NOT ADD TO DEFAULT LIST)
        // Paris (CET)
        parisGareLyon = new Station("Paris-Gare-Lyon", 48.84, 2.37, "FR", "Europe/Paris", "CET", true, true, false);
        // Berlin (CET)
        berlinHbf = new Station("Berlin Hbf", 52.52, 13.36, "DE", "Europe/Berlin", "CET", true, true, false);
        // Kyiv (EET)
        kyivPas = new Station("Kyiv-Pasazhyrskyi", 50.43, 30.48, "UA", "Europe/Kyiv", "EET", true, true, false);

        stations = Arrays.asList(
                portoCampanha,
                portoSaoBento,
                aveiro,
                madridChamartin,
                lisbonOriente
        );

        tree = new KDTree2D();
        tree.buildBalanced(stations, stations); // both lists feed the bulk-build pipeline
    }

    @Test
    public void testBuildBalancedAggregatesStationsWithSameCoordinates() {
        // both Porto stations must be stored on the same node bucket
        KDTree2D.Node portoNode = tree.inOrderTraversal().stream()
                .filter(node -> close(node.latitude, portoCampanha.getLatitude())
                        && close(node.longitude, portoCampanha.getLongitude()))
                .findFirst()
                .orElse(null);

        assertNotNull(portoNode);
        assertEquals(2, portoNode.stations.size());
        assertEquals("Porto Campanhã", portoNode.stations.get(0).getName());
        assertEquals("Porto São Bento", portoNode.stations.get(1).getName());
    }

    @Test
    public void testBuildBalancedWithEmptyListsCreatesEmptyTree() {
        // building with empty lists should produce zero stats and null root
        KDTree2D emptyTree = new KDTree2D();
        KDTree2D.TreeStatistics stats = emptyTree.buildBalanced(Arrays.asList(), Arrays.asList());

        assertEquals(0, stats.size);
        assertEquals(0, stats.height);
        assertTrue(stats.bucketSizes.isEmpty());
        assertNull(emptyTree.getRoot());
    }

    @Test
    public void testRangeSearchWithFilterReturnsAllStationsWithoutFilter() {
        GeographicRegion iberia = new GeographicRegion(39.5, 42.5, -10.5, -3.0);

        List<Station> result = tree.rangeSearchWithFilter(iberia, null);

        assertEquals("All nodes within range must be returned", 4, result.size());
        assertTrue(result.contains(portoCampanha));
        assertTrue(result.contains(portoSaoBento));
        assertTrue(result.contains(aveiro));
        assertTrue(result.contains(madridChamartin));
        long bucketEntries = result.stream()
                .filter(s -> Math.abs(s.getLatitude() - portoCampanha.getLatitude()) < 0.000001
                        && Math.abs(s.getLongitude() - portoCampanha.getLongitude()) < 0.000001)
                .count();
        assertEquals("Bucket with duplicated coordinates must surface both stations",
                2, bucketEntries);
    }

    @Test
    public void testRangeSearchWithFilterTraversesBothSubtreesWhenRegionStraddlesSplit() {
        GeographicRegion northernPortugal = new GeographicRegion(40.5, 41.5, -8.8, -8.3);
        StationFilter portugueseFilter = StationFilter.forCountry("PT");

        List<Station> result = tree.rangeSearchWithFilter(northernPortugal, portugueseFilter);

        assertEquals("Three Portuguese stations should lie in the filtered region", 3, result.size());
        assertTrue(result.contains(portoCampanha));
        assertTrue(result.contains(portoSaoBento));
        assertTrue(result.contains(aveiro));
    }

    @Test
    public void testRangeSearchWithFilterSupportsCompoundFilters() {
        GeographicRegion iberia = new GeographicRegion(37.5, 42.5, -10.5, -3.0);
        StationFilter mainStationsInPortugal = StationFilter.mainStationsInCountry("PT");

        List<Station> result = tree.rangeSearchWithFilter(iberia, mainStationsInPortugal);

        assertEquals(2, result.size());
        assertTrue(result.contains(portoCampanha));
        assertTrue(result.contains(lisbonOriente));
    }

    @Test
    public void testRangeSearchWithFilterReturnsEmptyListOutsideRegionAndTracksTime() {
        GeographicRegion centralEurope = new GeographicRegion(46.0, 50.0, 6.0, 15.0);

        List<Station> result = tree.rangeSearchWithFilter(centralEurope, StationFilter.noFilter());

        assertTrue(result.isEmpty());
        assertTrue("Query time must be captured even when no station matches",
                tree.getLastQueryTimeNanos() > 0);
    }

    @Test
    public void testRangeSearchWithFilterFiltersStationsIndividuallyInsideBuckets() {
        GeographicRegion portoArea = new GeographicRegion(41.1490, 41.1505, -8.6115, -8.6100);

        List<Station> result = tree.rangeSearchWithFilter(portoArea, StationFilter.mainStationsOnly());

        assertEquals(1, result.size());
        assertTrue(result.contains(portoCampanha));
        assertFalse(result.contains(portoSaoBento));
    }

    @Test
    public void testRangeSearchWithFilterIncludesStationsOnBoundary() {
        GeographicRegion lisbonBoundary = new GeographicRegion(
                lisbonOriente.getLatitude(),
                lisbonOriente.getLatitude(),
                lisbonOriente.getLongitude(),
                lisbonOriente.getLongitude()
        );

        List<Station> result = tree.rangeSearchWithFilter(lisbonBoundary, StationFilter.noFilter());

        assertEquals(1, result.size());
        assertTrue(result.contains(lisbonOriente));
    }

    @Test
    public void testRangeSearchWithFilterCountryFilterIsCaseInsensitive() {
        GeographicRegion iberia = new GeographicRegion(37.5, 42.5, -10.5, -3.0);
        StationFilter lowerCaseCountry = StationFilter.forCountry("pt");

        List<Station> result = tree.rangeSearchWithFilter(iberia, lowerCaseCountry);

        assertEquals(4, result.size());
        assertTrue(result.contains(portoCampanha));
        assertTrue(result.contains(portoSaoBento));
        assertTrue(result.contains(aveiro));
        assertTrue(result.contains(lisbonOriente));
    }

    @Test
    public void testRangeSearchWithFilterReturnsEmptyWhenFilterExcludesAllStations() {
        GeographicRegion iberia = new GeographicRegion(37.5, 42.5, -10.5, -3.0);
        StationFilter brazilOnly = StationFilter.forCountry("BR");

        List<Station> result = tree.rangeSearchWithFilter(iberia, brazilOnly);

        assertTrue(result.isEmpty());
        assertTrue(tree.getLastQueryTimeNanos() > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeSearchWithFilterRejectsNullRegion() {
        tree.rangeSearchWithFilter(null, StationFilter.noFilter());
    }

    // -----------------------------------------------------------------
    // --- NEW TESTS FOR USEI09 (Proximity Search) ---
    // -----------------------------------------------------------------

    /**
     * Helper to build a larger tree specifically for USEI09 tests
     * that require different Time Zones.
     */
    private KDTree2D buildFullTree() {
        List<Station> allStations = new ArrayList<>(stations); // Copy original 5
        allStations.add(parisGareLyon);
        allStations.add(berlinHbf);
        allStations.add(kyivPas);

        KDTree2D fullTree = new KDTree2D();
        fullTree.buildBalanced(allStations, allStations);
        return fullTree;
    }

    @Test
    public void testFindNearestN_Simple() {
        // Search near Lisbon (38, -9) for N=1
        // Nearest is Lisbon Oriente
        List<Station> results = tree.findNearestNStations(38.0, -9.0, 1, null);

        assertEquals(1, results.size());
        assertEquals(lisbonOriente.getName(), results.get(0).getName());
    }

    @Test
    public void testFindNearestN_N_is_2() {
        // Search near Lisbon (38, -9) for N=2
        // 1. Lisbon Oriente
        // 2. Aveiro (closer than Madrid)
        List<Station> results = tree.findNearestNStations(38.0, -9.0, 2, null);

        assertEquals(2, results.size());
        assertEquals(lisbonOriente.getName(), results.get(0).getName());
        assertEquals(aveiro.getName(), results.get(1).getName());
    }

    @Test
    public void testFindNearestN_SpecialCase_N_less_X() {
        // N=1 search near Porto.
        // Porto coord has 2 stations. N=1 must return both.
        List<Station> results = tree.findNearestNStations(41.15, -8.61, 1, null);

        assertEquals(2, results.size());
        assertTrue(results.contains(portoCampanha));
        assertTrue(results.contains(portoSaoBento));
    }

    @Test
    public void testFindNearestN_WithFilter() {
        // Use the FULL tree with Paris/Berlin/Kyiv for this test
        KDTree2D fullTree = buildFullTree();

        // Search near Lisbon (38, -9) for N=2
        // Filter: "CET" (Central European Time)
        // Lisbon (WET) and Aveiro (WET) should be ignored.
        // Expected: 1. Madrid (CET), 2. Paris (CET)

        TimeZoneFilter filter = new TimeZoneFilter("CET");
        List<Station> results = fullTree.findNearestNStations(38.0, -9.0, 2, filter);

        assertEquals(2, results.size());
        assertEquals(madridChamartin.getName(), results.get(0).getName());
        assertEquals(parisGareLyon.getName(), results.get(1).getName());
    }

    @Test
    public void testFindNearestN_FilterExcludesAll() {
        TimeZoneFilter filter = new TimeZoneFilter("NonExistentGroup");
        List<Station> results = tree.findNearestNStations(38.0, -9.0, 3, filter);
        assertTrue(results.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindNearestN_N_zero_throwsException() {
        tree.findNearestNStations(38.0, -9.0, 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindNearestN_N_negative_throwsException() {
        tree.findNearestNStations(38.0, -9.0, -1, null);
    }

    @Test
    public void testFindNearestN_N_greater_than_nodes() {
        // N=100 on default tree (5 stations)
        List<Station> results = tree.findNearestNStations(38.0, -9.0, 100, null);

        // Should return all 5 original stations (Porto counts as 2) = Total 6 objects?
        // Actually: Porto Campanhã, Porto SB, Aveiro, Madrid, Lisbon = 5 objects.
        // Wait, Porto stations share coordinates.
        // Unique coordinates: Porto, Aveiro, Madrid, Lisbon = 4 nodes.
        // N=100 asks for 100 closest NODES.
        // It returns ALL 4 nodes -> extracting ALL 5 stations.

        assertEquals(5, results.size());
        assertEquals(lisbonOriente.getName(), results.get(0).getName());
    }

    @Test
    public void testFindNearestN_EmptyTree() {
        KDTree2D emptyTree = new KDTree2D();
        List<Station> results = emptyTree.findNearestNStations(38.0, -9.0, 3, null);
        assertTrue(results.isEmpty());
    }

    // Helper methods
    private Station buildStation(String name, double lat, double lon, String country,
                                 boolean isCity, boolean isMain) {
        return new Station(
                name,
                lat,
                lon,
                country,
                "Europe/Lisbon",
                "UTC+0",
                isCity,
                isMain,
                false
        );
    }

    private boolean close(double a, double b) {
        final double EPS = 0.000001;
        return Math.abs(a - b) < EPS;
    }
}