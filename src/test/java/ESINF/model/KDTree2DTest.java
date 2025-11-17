package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class KDTree2DTest {

    private KDTree2D tree;
    private Station portoCampanha;
    private Station portoSaoBento;
    private Station aveiro;
    private Station madridChamartin;
    private Station lisbonOriente;
    private List<Station> stations;

    @Before
    public void setUp() {
        portoCampanha = buildStation("Porto Campanhã", 41.14961, -8.61099, "PT", true, true);
        portoSaoBento = buildStation("Porto São Bento", 41.14961, -8.61099, "PT", false, false);
        aveiro = buildStation("Aveiro", 40.64050, -8.65370, "PT", true, false);
        madridChamartin = buildStation("Madrid Chamartín", 40.47295, -3.68920, "ES", true, true);
        lisbonOriente = buildStation("Lisboa Oriente", 38.76792, -9.09944, "PT", true, true);

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
    public void testRangeQueryReturnsStationsInsideBounds() {
        // rectangle covers Porto + Aveiro + Madrid only
        List<Station> result = tree.rangeQuery(39.5, 42.5, -10.5, -3.0);

        assertEquals(4, result.size());
        assertTrue(result.contains(portoCampanha));
        assertTrue(result.contains(portoSaoBento));
        assertTrue(result.contains(aveiro));
        assertTrue(result.contains(madridChamartin));
        assertFalse(result.contains(lisbonOriente));
    }

    @Test
    public void testRangeQueryOutsideBoundsReturnsEmptyList() {
        List<Station> result = tree.rangeQuery(-20.0, -10.0, 10.0, 20.0);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testNearestNeighborReturnsClosestStation() {
        Station nearest = tree.nearestNeighbor(38.7, -9.1);

        assertNotNull(nearest);
        assertEquals(lisbonOriente.getName(), nearest.getName());
    }

    @Test
    public void testNearestNeighborOnEmptyTreeReturnsNull() {
        KDTree2D emptyTree = new KDTree2D();

        assertNull(emptyTree.nearestNeighbor(0.0, 0.0));
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

