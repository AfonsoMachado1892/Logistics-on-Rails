package ESINF.service;

import ESINF.IO.AuditLogger;
import ESINF.model.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;

public class QuarantineManagerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Warehouse warehouse;
    private AuditLogger auditLogger;
    private Map<String, Item> itemCatalog;
    private QuarantineManager manager;
    private File auditLogFile;

    @Before
    public void setUp() throws Exception {
        warehouse = new Warehouse("WH001");

        Aisle aisle1 = new Aisle(1);
        Bay bay1 = new Bay(1, "WH001", 1, 10);
        Bay bay2 = new Bay(2, "WH001", 1, 10);
        aisle1.addBay(bay1);
        aisle1.addBay(bay2);
        warehouse.addAisle(aisle1);

        itemCatalog = new HashMap<>();
        itemCatalog.put("SKU001", new Item("SKU001", "Product A", "Cat1", "unit", 1.0, 1.0));
        itemCatalog.put("SKU002", new Item("SKU002", "Product B", "Cat2", "unit", 2.0, 2.0));

        auditLogFile = tempFolder.newFile("test_audit.log");
        auditLogger = new AuditLogger(auditLogFile.getAbsolutePath());

        manager = new QuarantineManager(warehouse, auditLogger, itemCatalog);
    }

    @Test
    public void processReturns() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R001", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null
        );

        List<ReturnItem> returns = Collections.singletonList(ret);
        manager.processReturns(returns);

        Bay bay = warehouse.getAisle(1).getBay(1);
        assertFalse(bay.isEmpty());
    }

    /**
     * Verifies the logic of the private `hasSameSku` method by testing the restocking strategy.
     * It ensures the manager prefers a bay that already contains the same SKU over other
     * empty bays, demonstrating the SKU check is working correctly.
     */
    @Test
    public void hasSameSku() {
        Bay bay1 = warehouse.getAisle(1).getBay(1);
        Bay bay2 = warehouse.getAisle(1).getBay(2);

        // Pre-populate bay2 with SKU001
        bay2.addBox(new Box("B1", "SKU001", 1, null, LocalDateTime.now()));

        // This return item should also go into bay2, not the empty bay1
        ReturnItem returnToRestock = new ReturnItem("R001", "SKU001", 5, "customer-remorse", LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(returnToRestock));

        assertTrue("Bay 1 should remain empty because bay 2 was preferred", bay1.isEmpty());
        assertEquals("Bay 2 should now have two boxes", 2, bay2.getBoxes().size());
    }

    /**
     * Verifies the logic of the private `tryRestock` method.
     * It ensures that if no bay contains a matching SKU, the manager will place
     * the new box in the first available bay it finds.
     */
    @Test
    public void tryRestock() {
        Bay bay1 = warehouse.getAisle(1).getBay(1);
        Bay bay2 = warehouse.getAisle(1).getBay(2);

        // Pre-populate bay1 with a *different* SKU
        bay1.addBox(new Box("B1", "SKU002", 1, null, LocalDateTime.now()));

        // This return item has no matching SKU bay, so it should go into the first available bay (bay1)
        ReturnItem returnToRestock = new ReturnItem("R001", "SKU001", 5, "customer-remorse", LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(returnToRestock));

        // CORRECTED ASSERTIONS:
        assertEquals("Bay 1 should now contain its original box plus the new one", 2, bay1.getBoxes().size());
        assertTrue("Bay 2 should remain empty", bay2.isEmpty());
        assertTrue("The new box 'RET-R001' should be in bay 1",
                bay1.getBoxes().stream().anyMatch(box -> box.getBoxId().equals("RET-R001")));
    }

    /**
     * Verifies the logic of the private `restockItem` method.
     * This test checks all three outcomes: a successful restock, a discard due to
     * expiring in quarantine, and a failed restock due to no space, by asserting
     * both the state of the warehouse and the content of the audit log.
     */
    @Test
    public void restockItem() throws Exception {
        // Case 1: Successful restock
        ReturnItem validReturn = new ReturnItem("R-VALID", "SKU001", 5, "customer-remorse", LocalDateTime.now(), LocalDate.now().plusDays(10));
        manager.processReturns(Collections.singletonList(validReturn));
        assertEquals("A box should be added to the warehouse", 1, warehouse.getAisle(1).getBay(1).getBoxes().size());
        assertTrue("Log should confirm successful restock", Files.readAllLines(auditLogFile.toPath()).get(0).contains("action=Restocked"));

        // Case 2: Discarded because it expired during quarantine
        ReturnItem expiredReturn = new ReturnItem("R-EXPIRED", "SKU001", 5, "customer-remorse", LocalDateTime.now(), LocalDate.now().minusDays(1));
        manager.processReturns(Collections.singletonList(expiredReturn));
        assertEquals("Box that expired in quarantine should not be added", 1, warehouse.getAisle(1).getBay(1).getBoxes().size()); // No change from previous state
        assertTrue("Log should confirm discard due to expiry", Files.readAllLines(auditLogFile.toPath()).get(1).contains("action=Discarded-ExpiredDuringQuarantine"));

        // Case 3: Failed restock because warehouse is full
        warehouse.getAisle(1).getBays().values().forEach(bay -> {
            while(!bay.isFull()) bay.addBox(new Box("FILLER", "SKU-FILLER", 1, null, LocalDateTime.now()));
        });
        ReturnItem noSpaceReturn = new ReturnItem("R-NOSPACE", "SKU001", 5, "customer-remorse", LocalDateTime.now(), LocalDate.now().plusDays(10));
        manager.processReturns(Collections.singletonList(noSpaceReturn));
        assertTrue("Log should confirm failure due to no space", Files.readAllLines(auditLogFile.toPath()).get(2).contains("action=FailedRestock-NoSpace"));
    }

    @Test
    public void processReturns_CustomerRemorseRestocked() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R001", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        assertFalse(bay.isEmpty());
        assertEquals(1, bay.getBoxes().size());

        Box box = bay.getBoxes().peek();
        assertEquals("RET-R001", box.getBoxId());
        assertEquals("SKU001", box.getSku());
        assertEquals(5, box.getQty());
    }

    @Test
    public void processReturns_CycleCountRestocked() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R002", "SKU001", 10, "cycle-count",
                LocalDateTime.now(), LocalDate.now().plusDays(30)
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        Box box = bay.getBoxes().peek();

        assertEquals("RET-R002", box.getBoxId());
        assertEquals(10, box.getQty());
        assertNotNull(box.getExpiryDate());
    }

    @Test
    public void processReturns_DamagedDiscarded() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R003", "SKU001", 8, "damaged",
                LocalDateTime.now(), null
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay1 = warehouse.getAisle(1).getBay(1);
        Bay bay2 = warehouse.getAisle(1).getBay(2);

        assertTrue(bay1.isEmpty());
        assertTrue(bay2.isEmpty());
    }

    @Test
    public void processReturns_ExpiredDiscarded() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R004", "SKU001", 12, "expired",
                LocalDateTime.now(), LocalDate.now().minusDays(1)
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay1 = warehouse.getAisle(1).getBay(1);
        assertTrue(bay1.isEmpty());
    }

    @Test
    public void processReturns_InvalidSKUDiscarded() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R005", "INVALID_SKU", 5, "customer-remorse",
                LocalDateTime.now(), null
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay1 = warehouse.getAisle(1).getBay(1);
        assertTrue(bay1.isEmpty());

        List<String> logLines = Files.readAllLines(auditLogFile.toPath());
        assertTrue(logLines.stream().anyMatch(line -> line.contains("Discarded-InvalidSKU")));
    }

    @Test
    public void processReturns_ExpiredDuringQuarantine() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R006", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), LocalDate.now().minusDays(5)
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay1 = warehouse.getAisle(1).getBay(1);
        assertTrue(bay1.isEmpty());

        List<String> logLines = Files.readAllLines(auditLogFile.toPath());
        assertTrue(logLines.stream().anyMatch(line ->
                line.contains("Discarded-ExpiredDuringQuarantine")));
    }

    @Test
    public void processReturns_MultipleItemsRestocked() throws Exception {
        List<ReturnItem> returns = Arrays.asList(
                new ReturnItem("R007", "SKU001", 5, "customer-remorse",
                        LocalDateTime.now().minusHours(2), null),
                new ReturnItem("R008", "SKU001", 3, "customer-remorse",
                        LocalDateTime.now().minusHours(1), null),
                new ReturnItem("R009", "SKU001", 7, "customer-remorse",
                        LocalDateTime.now(), null)
        );

        manager.processReturns(returns);

        Bay bay = warehouse.getAisle(1).getBay(1);
        assertEquals(3, bay.getBoxes().size());
    }

    @Test
    public void processReturns_MixedRestockAndDiscard() throws Exception {
        List<ReturnItem> returns = Arrays.asList(
                new ReturnItem("R010", "SKU001", 5, "customer-remorse",
                        LocalDateTime.now(), null),
                new ReturnItem("R011", "SKU001", 3, "damaged",
                        LocalDateTime.now(), null),
                new ReturnItem("R012", "SKU002", 8, "expired",
                        LocalDateTime.now(), null),
                new ReturnItem("R013", "SKU002", 4, "cycle-count",
                        LocalDateTime.now(), null)
        );

        manager.processReturns(returns);

        int totalBoxes = 0;
        for (Bay bay : warehouse.getAisle(1).getBays().values()) {
            totalBoxes += bay.getBoxes().size();
        }

        assertEquals(2, totalBoxes);
    }

    @Test
    public void processReturns_CreatesAuditLog() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R017", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null
        );

        manager.processReturns(Collections.singletonList(ret));

        assertTrue(auditLogFile.exists());

        List<String> lines = Files.readAllLines(auditLogFile.toPath());
        assertFalse(lines.isEmpty());

        String lastLine = lines.get(lines.size() - 1);
        assertTrue(lastLine.contains("returnId=R017"));
        assertTrue(lastLine.contains("sku=SKU001"));
        assertTrue(lastLine.contains("qty=5"));
        assertTrue(lastLine.contains("Restocked"));
    }

    @Test
    public void processReturns_EmptyList() {
        List<ReturnItem> emptyList = new ArrayList<>();

        manager.processReturns(emptyList);

        Bay bay1 = warehouse.getAisle(1).getBay(1);
        assertTrue(bay1.isEmpty());
    }

    @Test
    public void processReturns_PrefersSameSKUBay() throws Exception {
        Bay bay1 = warehouse.getAisle(1).getBay(1);
        Box existingBox = new Box("BOX001", "SKU001", 10, null, LocalDateTime.now());
        bay1.addBox(existingBox);
        existingBox.setBay(bay1);

        ReturnItem ret = new ReturnItem(
                "R014", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null
        );

        manager.processReturns(Collections.singletonList(ret));

        assertEquals(2, bay1.getBoxes().size());
    }

    @Test
    public void processReturns_UsesNextBayWhenFull() throws Exception {
        Bay bay1 = warehouse.getAisle(1).getBay(1);

        for (int i = 0; i < 10; i++) {
            Box box = new Box("BOX" + i, "SKU001", 1, null, LocalDateTime.now());
            bay1.addBox(box);
            box.setBay(bay1);
        }

        ReturnItem ret = new ReturnItem(
                "R015", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay2 = warehouse.getAisle(1).getBay(2);
        assertEquals(1, bay2.getBoxes().size());
        assertEquals("RET-R015", bay2.getBoxes().peek().getBoxId());
    }

    @Test
    public void processReturns_FailsWhenWarehouseFull() throws Exception {
        for (Bay bay : warehouse.getAisle(1).getBays().values()) {
            for (int i = 0; i < 10; i++) {
                Box box = new Box("FILL" + i + bay.getNumber(), "SKU001", 1,
                        null, LocalDateTime.now());
                bay.addBox(box);
                box.setBay(bay);
            }
        }

        ReturnItem ret = new ReturnItem(
                "R016", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null
        );

        manager.processReturns(Collections.singletonList(ret));

        for (Bay bay : warehouse.getAisle(1).getBays().values()) {
            assertEquals(10, bay.getBoxes().size());
            assertTrue(bay.getBoxes().stream()
                    .noneMatch(b -> b.getBoxId().equals("RET-R016")));
        }

        List<String> logLines = Files.readAllLines(auditLogFile.toPath());
        assertTrue(logLines.stream().anyMatch(line -> line.contains("FailedRestock-NoSpace")));
    }

    @Test
    public void processReturns_WithExpiryDate() throws Exception {
        LocalDate futureExpiry = LocalDate.now().plusDays(60);
        ReturnItem ret = new ReturnItem(
                "R018", "SKU001", 8, "customer-remorse",
                LocalDateTime.now(), futureExpiry
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        Box box = bay.getBoxes().peek();

        assertEquals(futureExpiry, box.getExpiryDate());
    }

    @Test
    public void processReturns_DifferentSKUs() throws Exception {
        List<ReturnItem> returns = Arrays.asList(
                new ReturnItem("R019", "SKU001", 5, "customer-remorse",
                        LocalDateTime.now(), null),
                new ReturnItem("R020", "SKU002", 3, "customer-remorse",
                        LocalDateTime.now(), null)
        );

        manager.processReturns(returns);

        int totalBoxes = 0;
        for (Bay bay : warehouse.getAisle(1).getBays().values()) {
            totalBoxes += bay.getBoxes().size();
        }

        assertEquals(2, totalBoxes);
    }

    @Test
    public void processReturns_AllDiscarded() throws Exception {
        List<ReturnItem> returns = Arrays.asList(
                new ReturnItem("R021", "SKU001", 5, "damaged",
                        LocalDateTime.now(), null),
                new ReturnItem("R022", "SKU002", 3, "expired",
                        LocalDateTime.now(), null)
        );

        manager.processReturns(returns);

        for (Bay bay : warehouse.getAisle(1).getBays().values()) {
            assertTrue(bay.isEmpty());
        }
    }

    @Test
    public void processReturns_LargeQuantity() throws Exception {
        ReturnItem ret = new ReturnItem(
                "R023", "SKU001", 999999, "customer-remorse",
                LocalDateTime.now(), null
        );

        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        assertFalse(bay.isEmpty());
        assertEquals(999999, bay.getBoxes().peek().getQty());
    }
}