package ESINF.model;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * Unit tests for StationIndex
 * Tests all query operations and index functionality
 */
public class StationIndexTest {

    private StationIndex index;
    private Station lisboaSanta;
    private Station lisboaOriente;
    private Station porto;
    private Station madridAtocha;
    private Station madridChamartín;
    private Station berlinHbf;
    private Station parisNord;

    @Before
    public void setUp() {
        index = new StationIndex();

        // Create test stations with same coordinates for Lisboa (to test determinism)
        lisboaSanta = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        lisboaOriente = new Station(
                "Lisboa Oriente", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        porto = new Station(
                "Porto Campanhã", 41.14961, -8.58507, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        madridAtocha = new Station(
                "Madrid Atocha", 40.40677, -3.68848, "ES",
                "Europe/Madrid", "CET", true, true, false
        );
        madridChamartín = new Station(
                "Madrid Chamartín", 40.47259, -3.68159, "ES",
                "Europe/Madrid", "CET", true, true, false
        );
        berlinHbf = new Station(
                "Berlin Hauptbahnhof", 52.52509, 13.36946, "DE",
                "Europe/Berlin", "CET", true, true, false
        );
        parisNord = new Station(
                "Paris Nord", 48.88053, 2.35508, "FR",
                "Europe/Paris", "CET", true, true, false
        );
    }

    // ============ AddStation Tests ============

    @Test
    public void testAddSingleStation() {
        index.addStation(lisboaSanta);
        assertEquals(1, index.getTotalStations());
    }

    @Test
    public void testAddMultipleStations() {
        index.addStation(lisboaSanta);
        index.addStation(porto);
        index.addStation(madridAtocha);
        assertEquals(3, index.getTotalStations());
    }

    @Test
    public void testAddStationsWithSameCoordinates() {
        index.addStation(lisboaSanta);
        index.addStation(lisboaOriente);
        assertEquals(2, index.getTotalStations());
    }

    // ============ FinalizeIndex Tests ============

    @Test
    public void testFinalizeIndexSortsStationsByName() {
        index.addStation(lisboaOriente);
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLatitudeRange(38.71387, 38.71387);
        assertEquals("Lisboa Oriente", results.get(0).getName());
    }

    @Test
    public void testFinalizeIndexSecondStationSorted() {
        index.addStation(lisboaOriente);
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLatitudeRange(38.71387, 38.71387);
        assertEquals("Lisboa Santa Apolónia", results.get(1).getName());
    }

    // ============ GetStationsByTimeZoneGroup Tests ============

    @Test
    public void testGetStationsByTimeZoneGroupSingleCountry() {
        index.addStation(lisboaSanta);
        index.addStation(porto);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroup("WET/GMT");
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByTimeZoneGroupMultipleCountries() {
        index.addStation(madridAtocha);
        index.addStation(berlinHbf);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroup("CET");
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByTimeZoneGroupOrderedByCountry() {
        index.addStation(madridAtocha);
        index.addStation(berlinHbf);
        index.addStation(parisNord);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroup("CET");
        assertEquals("DE", results.get(0).getCountry());
    }

    @Test
    public void testGetStationsByTimeZoneGroupSecondCountry() {
        index.addStation(madridAtocha);
        index.addStation(berlinHbf);
        index.addStation(parisNord);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroup("CET");
        assertEquals("ES", results.get(1).getCountry());
    }

    @Test
    public void testGetStationsByTimeZoneGroupThirdCountry() {
        index.addStation(madridAtocha);
        index.addStation(berlinHbf);
        index.addStation(parisNord);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroup("CET");
        assertEquals("FR", results.get(2).getCountry());
    }

    @Test
    public void testGetStationsByTimeZoneGroupNonExistent() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroup("NONEXISTENT");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetStationsByTimeZoneGroupOrderedByName() {
        index.addStation(madridChamartín);
        index.addStation(madridAtocha);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroup("CET");
        assertEquals("Madrid Atocha", results.get(0).getName());
    }

    // ============ GetStationsByTimeZoneGroupAndCountry Tests ============

    @Test
    public void testGetStationsByTimeZoneGroupAndCountry() {
        index.addStation(lisboaSanta);
        index.addStation(porto);
        index.addStation(madridAtocha);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroupAndCountry("WET/GMT", "PT");
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByTimeZoneGroupAndCountrySingleResult() {
        index.addStation(lisboaSanta);
        index.addStation(madridAtocha);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroupAndCountry("CET", "ES");
        assertEquals(1, results.size());
    }

    @Test
    public void testGetStationsByTimeZoneGroupAndCountryNonExistentGroup() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroupAndCountry("NONEXISTENT", "PT");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetStationsByTimeZoneGroupAndCountryNonExistentCountry() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroupAndCountry("WET/GMT", "ES");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetStationsByTimeZoneGroupAndCountryReturnsFirstStation() {
        index.addStation(lisboaOriente);
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneGroupAndCountry("WET/GMT", "PT");
        assertEquals("Lisboa Oriente", results.get(0).getName());
    }

    // ============ GetStationsByTimeZoneWindow Tests ============

    @Test
    public void testGetStationsByTimeZoneWindowSingleGroup() {
        index.addStation(lisboaSanta);
        index.addStation(porto);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneWindow("WET/GMT");
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByTimeZoneWindowMultipleGroups() {
        index.addStation(lisboaSanta);
        index.addStation(madridAtocha);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneWindow("WET/GMT", "CET");
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByTimeZoneWindowDeterministicOrder() {
        index.addStation(madridAtocha);
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneWindow("WET/GMT", "CET");
        assertEquals("CET", results.get(0).getTimeZoneGroup());
    }

    @Test
    public void testGetStationsByTimeZoneWindowSecondGroup() {
        index.addStation(madridAtocha);
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneWindow("WET/GMT", "CET");
        assertEquals("WET/GMT", results.get(1).getTimeZoneGroup());
    }

    @Test
    public void testGetStationsByTimeZoneWindowEmptyResult() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneWindow("NONEXISTENT");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetStationsByTimeZoneWindowMixedExistent() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByTimeZoneWindow("WET/GMT", "NONEXISTENT");
        assertEquals(1, results.size());
    }

    // ============ GetStationsByLatitudeRange Tests ============

    @Test
    public void testGetStationsByLatitudeRangeSingleStation() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLatitudeRange(38.0, 39.0);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetStationsByLatitudeRangeMultipleStations() {
        index.addStation(lisboaSanta);
        index.addStation(porto);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLatitudeRange(38.0, 42.0);
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByLatitudeRangeExactMatch() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLatitudeRange(38.71387, 38.71387);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetStationsByLatitudeRangeOutOfRange() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLatitudeRange(50.0, 60.0);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetStationsByLatitudeRangeSameCoordinates() {
        index.addStation(lisboaSanta);
        index.addStation(lisboaOriente);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLatitudeRange(38.71387, 38.71387);
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByLatitudeRangeInclusiveBounds() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLatitudeRange(38.71387, 40.0);
        assertEquals(1, results.size());
    }

    // ============ GetStationsByLongitudeRange Tests ============

    @Test
    public void testGetStationsByLongitudeRangeSingleStation() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLongitudeRange(-10.0, -9.0);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetStationsByLongitudeRangeMultipleStations() {
        index.addStation(lisboaSanta);
        index.addStation(porto);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLongitudeRange(-10.0, -8.0);
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByLongitudeRangeExactMatch() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLongitudeRange(-9.122271, -9.122271);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetStationsByLongitudeRangeOutOfRange() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLongitudeRange(0.0, 10.0);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetStationsByLongitudeRangeSameCoordinates() {
        index.addStation(lisboaSanta);
        index.addStation(lisboaOriente);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLongitudeRange(-9.122271, -9.122271);
        assertEquals(2, results.size());
    }

    @Test
    public void testGetStationsByLongitudeRangeInclusiveBounds() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        List<Station> results = index.getStationsByLongitudeRange(-9.122271, -8.0);
        assertEquals(1, results.size());
    }

    // ============ GetAllTimeZoneGroups Tests ============

    @Test
    public void testGetAllTimeZoneGroupsSingle() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        Set<String> groups = index.getAllTimeZoneGroups();
        assertEquals(1, groups.size());
    }

    @Test
    public void testGetAllTimeZoneGroupsMultiple() {
        index.addStation(lisboaSanta);
        index.addStation(madridAtocha);
        index.finalizeIndex();

        Set<String> groups = index.getAllTimeZoneGroups();
        assertEquals(2, groups.size());
    }

    @Test
    public void testGetAllTimeZoneGroupsEmpty() {
        Set<String> groups = index.getAllTimeZoneGroups();
        assertTrue(groups.isEmpty());
    }

    @Test
    public void testGetAllTimeZoneGroupsContainsWET() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        Set<String> groups = index.getAllTimeZoneGroups();
        assertTrue(groups.contains("WET/GMT"));
    }

    @Test
    public void testGetAllTimeZoneGroupsContainsCET() {
        index.addStation(madridAtocha);
        index.finalizeIndex();

        Set<String> groups = index.getAllTimeZoneGroups();
        assertTrue(groups.contains("CET"));
    }

    // ============ GetCountriesInTimeZoneGroup Tests ============

    @Test
    public void testGetCountriesInTimeZoneGroupSingle() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        Set<String> countries = index.getCountriesInTimeZoneGroup("WET/GMT");
        assertEquals(1, countries.size());
    }

    @Test
    public void testGetCountriesInTimeZoneGroupMultiple() {
        index.addStation(madridAtocha);
        index.addStation(berlinHbf);
        index.finalizeIndex();

        Set<String> countries = index.getCountriesInTimeZoneGroup("CET");
        assertEquals(2, countries.size());
    }

    @Test
    public void testGetCountriesInTimeZoneGroupNonExistent() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        Set<String> countries = index.getCountriesInTimeZoneGroup("NONEXISTENT");
        assertTrue(countries.isEmpty());
    }

    @Test
    public void testGetCountriesInTimeZoneGroupContainsPT() {
        index.addStation(lisboaSanta);
        index.finalizeIndex();

        Set<String> countries = index.getCountriesInTimeZoneGroup("WET/GMT");
        assertTrue(countries.contains("PT"));
    }

    @Test
    public void testGetCountriesInTimeZoneGroupContainsES() {
        index.addStation(madridAtocha);
        index.finalizeIndex();

        Set<String> countries = index.getCountriesInTimeZoneGroup("CET");
        assertTrue(countries.contains("ES"));
    }

    // ============ GetStatistics Tests ============

    @Test
    public void testGetStatisticsTotalStations() {
        index.addStation(lisboaSanta);
        index.addStation(porto);
        index.finalizeIndex();

        StationIndex.IndexStatistics stats = index.getStatistics();
        assertEquals(2, stats.totalStations());
    }

    @Test
    public void testGetStatisticsUniqueLatitudes() {
        index.addStation(lisboaSanta);
        index.addStation(lisboaOriente);
        index.finalizeIndex();

        StationIndex.IndexStatistics stats = index.getStatistics();
        assertEquals(1, stats.uniqueLatitudes());
    }

    @Test
    public void testGetStatisticsUniqueLongitudes() {
        index.addStation(lisboaSanta);
        index.addStation(lisboaOriente);
        index.finalizeIndex();

        StationIndex.IndexStatistics stats = index.getStatistics();
        assertEquals(1, stats.uniqueLongitudes());
    }

    @Test
    public void testGetStatisticsUniqueTimeZoneGroups() {
        index.addStation(lisboaSanta);
        index.addStation(madridAtocha);
        index.finalizeIndex();

        StationIndex.IndexStatistics stats = index.getStatistics();
        assertEquals(2, stats.uniqueTimeZoneGroups());
    }

    @Test
    public void testGetStatisticsEmpty() {
        StationIndex.IndexStatistics stats = index.getStatistics();
        assertEquals(0, stats.totalStations());
    }
}