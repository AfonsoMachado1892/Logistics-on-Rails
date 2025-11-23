package ESINF.model;

import org.junit.Before;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class RadiusSearchResultTest {

    Station s1, s2, s3;
    RadiusSearchResult.StationKey k1, k2, k3;
    TreeMap<RadiusSearchResult.StationKey, Station> tree;
    RadiusSearchResult.DensitySummary summary;
    RadiusSearchResult result;

    @Before
    public void setup() {

        s1 = new Station("Alpha", 10, 10, "PT", "UTC", "EU", true, false, false);
        s2 = new Station("Beta", 20, 20, "ES", "UTC+1", "EU", false, false, false);
        s3 = new Station("Gamma", 30, 30, "PT", "UTC", "EU", false, false, false);

        // KEYS (note: same constructor as your class)
        k1 = new RadiusSearchResult.StationKey(10.0, s1);
        k2 = new RadiusSearchResult.StationKey(5.0, s2);
        k3 = new RadiusSearchResult.StationKey(5.0, s3);

        tree = new TreeMap<>();
        tree.put(k1, s1);
        tree.put(k2, s2);
        tree.put(k3, s3);

        Map<String, Integer> cdist = new HashMap<>();
        cdist.put("PT", 2);
        cdist.put("ES", 1);

        Map<Boolean, Integer> cd = new HashMap<>();
        cd.put(true, 1);
        cd.put(false, 2);

        summary = new RadiusSearchResult.DensitySummary(cdist, cd, 3);

        result = new RadiusSearchResult(tree, summary, 40.0, -8.0, 20.0, 500000); // 0.5s
    }

    // -------------------------- StationKey tests ------------------------------

    @Test
    public void testKeyCompareByDistance() {
        assertTrue(k2.compareTo(k1) < 0); // 5km < 10km
    }

    @Test
    public void testKeyCompareByNameDescending() {
        // k2 e k3 com mesma distância → compara nomes DESC
        assertTrue(k3.compareTo(k2) < 0); // "Gamma" vem antes de "Beta"
    }


    @Test
    public void testKeyEquals() {
        RadiusSearchResult.StationKey clone = new RadiusSearchResult.StationKey(10.0, s1);
        assertEquals(k1, clone);
    }

    @Test
    public void testKeyHash() {
        RadiusSearchResult.StationKey clone = new RadiusSearchResult.StationKey(10.0, s1);
        assertEquals(k1.hashCode(), clone.hashCode());
    }

    // -------------------------- DensitySummary tests --------------------------

    @Test
    public void testSummaryTotalCounts() {
        assertEquals(3, summary.getTotalStations());
        assertEquals(1, summary.getCityCount());
        assertEquals(2, summary.getNonCityCount());
    }

    @Test
    public void testSummaryCountrySorting() {
        List<String> keys = new ArrayList<>(summary.getCountryDistribution().keySet());
        assertEquals(List.of("ES", "PT"), keys); // TreeMap → sorted
    }

    @Test
    public void testSummaryContainsPercentages() {
        String text = summary.toString();
        assertTrue(text.contains("PT: 2"));
        assertTrue(text.contains("ES: 1"));
    }

    // -------------------------- RadiusSearchResult tests --------------------------

    @Test
    public void testGetStationListSorted() {
        List<Station> list = result.getStationList();

        // Distances: Beta(5), Gamma(5), Alpha(10)
        // Tiebreak real: name DESC => Gamma before Beta
        assertEquals("Gamma", list.get(0).getName());
        assertEquals("Beta", list.get(1).getName());
        assertEquals("Alpha", list.get(2).getName());
    }


    @Test
    public void testClosestStation() {
        assertEquals("Gamma", result.getClosestStation().getName());
    }

    @Test
    public void testFarthestStation() {
        assertEquals("Alpha", result.getFarthestStation().getName());
    }

    @Test
    public void testDistanceLimits() {
        assertEquals(5.0, result.getMinDistance(), 0.0001);
        assertEquals(10.0, result.getMaxDistance(), 0.0001);
    }


    @Test
    public void testSummaryString() {
        String s = result.toSummaryString();
        assertTrue(s.contains("3 stations"));
        assertTrue(s.contains("1 cities"));
        assertTrue(s.contains("2 countries"));
    }

    @Test
    public void testToStringContainsSections() {
        String txt = result.toString();
        assertTrue(txt.contains("Radius Search Result"));
        assertTrue(txt.contains("Density Summary"));
        assertTrue(txt.contains("Station List"));
    }

    @Test
    public void testEmptyResult() {
        TreeMap<RadiusSearchResult.StationKey, Station> empty = new TreeMap<>();
        RadiusSearchResult emptyRes = new RadiusSearchResult(empty, summary, 0, 0, 5, 1000);

        assertNull(emptyRes.getClosestStation());
        assertNull(emptyRes.getFarthestStation());
        assertEquals(Double.MAX_VALUE, emptyRes.getMinDistance(), 0.0001);
        assertEquals(0, emptyRes.getMaxDistance(), 0.0001);
    }

}
