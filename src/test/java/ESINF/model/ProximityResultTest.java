package ESINF.model;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import static org.junit.Assert.*;

public class ProximityResultTest {

    private Station sA = new Station("A", 1, 1, "PT", "Z", "Z", false, false, false);
    private List<Station> listA = List.of(sA);

    @Test
    public void testCompareTo_LesserThan() {
        ProximityResult res50 = new ProximityResult(50.0, listA);
        ProximityResult res100 = new ProximityResult(100.0, listA);

        assertTrue("50 should be less than 100", res50.compareTo(res100) < 0);
    }

    @Test
    public void testCompareTo_GreaterThan() {
        ProximityResult res50 = new ProximityResult(50.0, listA);
        ProximityResult res100 = new ProximityResult(100.0, listA);

        assertTrue("100 should be greater than 50", res100.compareTo(res50) > 0);
    }

    @Test
    public void testCompareTo_EqualTo() {
        ProximityResult res50 = new ProximityResult(50.0, listA);
        ProximityResult res50_dupe = new ProximityResult(50.0, listA);

        assertEquals("50 should be equal to 50", 0, res50.compareTo(res50_dupe));
    }

    @Test
    public void testPriorityQueueAsMaxHeap_FirstPoll() {

        PriorityQueue<ProximityResult> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        ProximityResult res50 = new ProximityResult(50.0, listA);
        ProximityResult res100 = new ProximityResult(100.0, listA);
        ProximityResult res25 = new ProximityResult(25.0, listA);

        maxHeap.add(res50);
        maxHeap.add(res100);
        maxHeap.add(res25);

        // The top of the heap (poll) should be the one with the MAXIMUM distance
        assertEquals(res100, maxHeap.poll()); // Remove 100
    }

    @Test
    public void testPriorityQueueAsMaxHeap_SecondPoll() {

        PriorityQueue<ProximityResult> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        ProximityResult res50 = new ProximityResult(50.0, listA);
        ProximityResult res100 = new ProximityResult(100.0, listA);
        ProximityResult res25 = new ProximityResult(25.0, listA);

        maxHeap.add(res50);
        maxHeap.add(res100);
        maxHeap.add(res25);

        maxHeap.poll(); // Remove 100
        assertEquals(res50, maxHeap.poll()); // Remove 50
    }

    @Test
    public void testPriorityQueueAsMaxHeap_ThirdPoll() {

        PriorityQueue<ProximityResult> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        ProximityResult res50 = new ProximityResult(50.0, listA);
        ProximityResult res100 = new ProximityResult(100.0, listA);
        ProximityResult res25 = new ProximityResult(25.0, listA);

        maxHeap.add(res50);
        maxHeap.add(res100);
        maxHeap.add(res25);

        maxHeap.poll(); // Remove 100
        maxHeap.poll(); // Remove 50
        assertEquals(res25, maxHeap.poll()); // Remove 25
    }
}