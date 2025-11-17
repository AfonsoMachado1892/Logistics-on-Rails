package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for {@link KDTree2D} focusing on USEI08 range searches with filtering.
 * The scenarios use real Iberian coordinates to keep assertions meaningful for the project domain.
 */
public class KDTree2DTest {

    private KDTree2D tree;
    private Station portoCampanha;
    private Station portoSaoBento;
    private Station aveiro;
    private Station madridChamartin;
    private Station lisbonOriente;

    @Before
    public void setUp() {
        portoCampanha = buildStation("Porto Campanhã", 41.14961, -8.61099, "PT", true, true);
        portoSaoBento = buildStation("Porto São Bento", 41.14961, -8.61099, "PT", false, false);
        aveiro = buildStation("Aveiro", 40.64050, -8.65370, "PT", true, false);
        madridChamartin = buildStation("Madrid Chamartín", 40.47295, -3.68920, "ES", true, true);
        lisbonOriente = buildStation("Lisboa Oriente", 38.76792, -9.09944, "PT", true, true);

        List<Station> stations = Arrays.asList(
                portoCampanha,
                portoSaoBento,
                aveiro,
                madridChamartin,
                lisbonOriente
        );

        tree = new KDTree2D();
        tree.buildBalanced(stations, stations); // both lists feed the bulk-build pipeline
    }

    /**
     * Ensures {@link KDTree2D#rangeSearchWithFilter(GeographicRegion, StationFilter)} returns every station
     * inside the Iberian bounding box when no filter is applied.
     */
    @Test
    public void rangeSearchWithFilterReturnsAllStationsWithoutFilter() {
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

    /**
     * Verifies that the recursive traversal prunes correctly when the region crosses a latitude split,
     * still surfacing stations from both subtrees that satisfy the PT-only filter.
     */
    @Test
    public void rangeSearchWithFilterTraversesBothSubtreesWhenRegionStraddlesSplit() {
        GeographicRegion northernPortugal = new GeographicRegion(40.5, 41.5, -8.8, -8.3);
        StationFilter portugueseFilter = StationFilter.forCountry("PT");

        List<Station> result = tree.rangeSearchWithFilter(northernPortugal, portugueseFilter);

        assertEquals("Three Portuguese stations should lie in the filtered region", 3, result.size());
        assertTrue(result.contains(portoCampanha));
        assertTrue(result.contains(portoSaoBento));
        assertTrue(result.contains(aveiro));
    }

    /**
     * Confirms that combined filters (main station + country) are honored by the recursive search,
     * returning only the flag stations within the rectangular query.
     */
    @Test
    public void rangeSearchWithFilterSupportsCompoundFilters() {
        GeographicRegion iberia = new GeographicRegion(37.5, 42.5, -10.5, -3.0);
        StationFilter mainStationsInPortugal = StationFilter.mainStationsInCountry("PT");

        List<Station> result = tree.rangeSearchWithFilter(iberia, mainStationsInPortugal);

        assertEquals(2, result.size());
        assertTrue(result.contains(portoCampanha));
        assertTrue(result.contains(lisbonOriente));
    }

    /**
     * Ensures the search returns an empty list for disjoint regions and records the execution time
     * so observability metrics remain meaningful to the controllers.
     */
    @Test
    public void rangeSearchWithFilterReturnsEmptyListOutsideRegionAndTracksTime() {
        GeographicRegion centralEurope = new GeographicRegion(46.0, 50.0, 6.0, 15.0);

        List<Station> result = tree.rangeSearchWithFilter(centralEurope, StationFilter.noFilter());

        assertTrue(result.isEmpty());
        assertTrue("Query time must be captured even when no station matches",
                tree.getLastQueryTimeNanos() > 0);
    }

    /**
     * Validates the public contract that a null region triggers an {@link IllegalArgumentException},
     * guarding controller usage errors.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rangeSearchWithFilterRejectsNullRegion() {
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
}

