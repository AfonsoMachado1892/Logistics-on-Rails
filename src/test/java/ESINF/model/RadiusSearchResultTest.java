package ESINF.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RadiusSearchResultTest {

    Station s1, s2, s3;
    RadiusSearchResult.StationKey k1, k2, k3;
    TreeMap<RadiusSearchResult.StationKey, Station> tree;
    RadiusSearchResult.DensitySummary summary;
    RadiusSearchResult result;

    @BeforeEach
    void setup() {

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

    
}
