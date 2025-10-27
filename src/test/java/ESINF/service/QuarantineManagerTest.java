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

/**
 * Testes para QuarantineManager - verifica processamento de devoluções em quarentena.
 */
public class QuarantineManagerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Warehouse warehouse;
    private AuditLogger auditLogger;
    private Map<String, Item> itemCatalog;
    private QuarantineManager manager;
    private File auditLogFile;

    /**
     * Inicializa uma warehouse com 2 bays, um catálogo e um audit logger antes de cada teste.
     */
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

    /**
     * Testa o metodo de processar as devoluções
     * A razao customer-remorse deve ser aceite.
     */
    @Test
    public void processReturns() throws Exception {
        ReturnItem ret = new ReturnItem("R001", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null);

        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        assertFalse(bay.isEmpty());
    }

    /**
     * Testa estratégia de restocking: prefere bay com mesmo SKU.
     * Bay2 tem SKU001, então nova devolução SKU001 vai para bay2, não bay1 (vazio).
     */
    @Test
    public void hasSameSku() {
        Bay bay1 = warehouse.getAisle(1).getBay(1);
        Bay bay2 = warehouse.getAisle(1).getBay(2);

        bay2.addBox(new Box("B1", "SKU001", 1, null, LocalDateTime.now()));

        ReturnItem ret = new ReturnItem("R001", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        assertTrue(bay1.isEmpty());
        assertEquals(2, bay2.getBoxes().size());
    }

    /**
     * Testa estratégia alternativa: sem SKU matching, usa primeira bay disponível.
     * Bay1 tem SKU002, devolução é SKU001 → vai para bay1 (primeiro disponível).
     */
    @Test
    public void tryRestock() {
        Bay bay1 = warehouse.getAisles().get(1).getBays().get(1);
        Bay bay2 = warehouse.getAisles().get(1).getBays().get(2);

        bay1.addBox(new Box("B1", "SKU002", 1, null, LocalDateTime.now()));

        ReturnItem ret = new ReturnItem("R001", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        assertEquals( 2, bay1.getBoxes().size());
        assertTrue( bay2.isEmpty());
        assertTrue(bay1.getBoxes().stream().anyMatch(box -> box.getBoxId().equals("RET-R001")));
    }

    /**
     * Testa o metodo restockItem com 3 situaçoes:
     * 1. Devolução bem-sucedida, então audit log "Restocked"
     * 2. Expirado durante a quarentena, então audit log "Discarded-ExpiredDuringQuarantine"
     * 3. Warehouse cheio, então audit log "FailedRestock-NoSpace"
     */
    @Test
    public void restockItem() throws Exception {
        // Cenário 1: Sucesso
        ReturnItem validRet = new ReturnItem("R-VALID", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), LocalDate.now().plusDays(10));
        manager.processReturns(Collections.singletonList(validRet));
        assertEquals(1, warehouse.getAisle(1).getBay(1).getBoxes().size());
        assertTrue(Files.readAllLines(auditLogFile.toPath()).get(0).contains("Restocked"));

        // Cenário 2: Expirado
        ReturnItem expiredRet = new ReturnItem("R-EXPIRED", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), LocalDate.now().minusDays(1));
        manager.processReturns(Collections.singletonList(expiredRet));
        assertEquals(1, warehouse.getAisle(1).getBay(1).getBoxes().size()); // Não adicionado
        assertTrue(Files.readAllLines(auditLogFile.toPath()).get(1)
                .contains("Discarded-ExpiredDuringQuarantine"));

        // Cenário 3: Warehouse cheio
        warehouse.getAisle(1).getBays().values().forEach(bay -> {
            while(!bay.isFull()) bay.addBox(new Box("FILLER", "SKU-FILLER", 1, null,
                    LocalDateTime.now()));
        });
        ReturnItem noSpaceRet = new ReturnItem("R-NOSPACE", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), LocalDate.now().plusDays(10));
        manager.processReturns(Collections.singletonList(noSpaceRet));
        assertTrue(Files.readAllLines(auditLogFile.toPath()).get(2).contains("FailedRestock-NoSpace"));
    }

    /**
     * Testa customer-remorse: cria caixa "RET-{returnId}", restock com sucesso.
     */
    @Test
    public void processReturns_CustomerRemorseRestocked() throws Exception {
        ReturnItem ret = new ReturnItem("R001", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        Box box = bay.getBoxes().peek();

        assertEquals("RET-R001", box.getBoxId());
        assertEquals("SKU001", box.getSku());
        assertEquals(5, box.getQty());
    }

    /**
     * Testa cycle-count: restock com data de validade válida.
     */
    @Test
    public void processReturns_CycleCountRestocked() throws Exception {
        ReturnItem ret = new ReturnItem("R002", "SKU001", 10, "cycle-count",
                LocalDateTime.now(), LocalDate.now().plusDays(30));
        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        Box box = bay.getBoxes().peek();

        assertEquals("RET-R002", box.getBoxId());
        assertNotNull(box.getExpiryDate());
    }

    /**
     * Testa a razão damaged
     * Deve ser descartado
     */
    @Test
    public void processReturns_DamagedDiscarded() throws Exception {
        ReturnItem ret = new ReturnItem("R003", "SKU001", 8, "damaged",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        assertTrue(warehouse.getAisle(1).getBay(1).isEmpty());
        assertTrue(warehouse.getAisle(1).getBay(2).isEmpty());
    }

    /**
     * Testa a razao expired
     * Deve ser descartado.
     */
    @Test
    public void processReturns_ExpiredDiscarded() throws Exception {
        ReturnItem ret = new ReturnItem("R004", "SKU001", 12, "expired",
                LocalDateTime.now(), LocalDate.now().minusDays(1));
        manager.processReturns(Collections.singletonList(ret));

        assertTrue(warehouse.getAisle(1).getBay(1).isEmpty());
    }

    /**
     * Testa SKU inválido
     * Deve ser descartado com log "Discarded-InvalidSKU".
     */
    @Test
    public void processReturns_InvalidSKUDiscarded() throws Exception {
        ReturnItem ret = new ReturnItem("R005", "INVALID_SKU", 5, "customer-remorse",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        assertTrue(warehouse.getAisle(1).getBay(1).isEmpty());
        assertTrue(Files.readAllLines(auditLogFile.toPath()).stream()
                .anyMatch(line -> line.contains("Discarded-InvalidSKU")));
    }

    /**
     * Testa um produto que expirou durante a quarentena
     * Deve ser descartado com log específico.
     */
    @Test
    public void processReturns_ExpiredDuringQuarantine() throws Exception {
        ReturnItem ret = new ReturnItem("R006", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), LocalDate.now().minusDays(5));
        manager.processReturns(Collections.singletonList(ret));

        assertTrue(warehouse.getAisle(1).getBay(1).isEmpty());
        assertTrue(Files.readAllLines(auditLogFile.toPath()).stream()
                .anyMatch(line -> line.contains("Discarded-ExpiredDuringQuarantine")));
    }

    /**
     * Testa múltiplas devoluções: todas restocked no mesmo bay.
     */
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

        assertEquals(3, warehouse.getAisle(1).getBay(1).getBoxes().size());
    }

    /**
     * Testa varios casos
     * 2 devoluções válidas (customer-remorse, cycle-count), 2 inválidas (damaged, expired).
     */
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

    /**
     * Testa criação de audit log: ficheiro existe com entradas corretas.
     */
    @Test
    public void processReturns_CreatesAuditLog() throws Exception {
        ReturnItem ret = new ReturnItem("R017", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null);
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

    /**
     * Testa uma lista vazia sem alterações no warehouse.
     */
    @Test
    public void processReturns_EmptyList() {
        manager.processReturns(new ArrayList<>());
        assertTrue(warehouse.getAisle(1).getBay(1).isEmpty());
    }

    /**
     * Testa preferência por bay com mesmo SKU: nova caixa SKU001 vai para bay1 (que já tem SKU001).
     */
    @Test
    public void processReturns_PrefersSameSKUBay() throws Exception {
        Bay bay1 = warehouse.getAisle(1).getBay(1);
        Box existingBox = new Box("BOX001", "SKU001", 10, null, LocalDateTime.now());
        bay1.addBox(existingBox);
        existingBox.setBay(bay1);

        ReturnItem ret = new ReturnItem("R014", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        assertEquals(2, bay1.getBoxes().size());
    }

    /**
     * Testa overflow: bay1 cheio, usa bay2.
     */
    @Test
    public void processReturns_UsesNextBayWhenFull() throws Exception {
        Bay bay1 = warehouse.getAisle(1).getBay(1);
        for (int i = 0; i < 10; i++) {
            Box box = new Box("BOX" + i, "SKU001", 1, null, LocalDateTime.now());
            bay1.addBox(box);
            box.setBay(bay1);
        }

        ReturnItem ret = new ReturnItem("R015", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        Bay bay2 = warehouse.getAisle(1).getBay(2);
        assertEquals(1, bay2.getBoxes().size());
        assertEquals("RET-R015", bay2.getBoxes().peek().getBoxId());
    }

    /**
     * Testa warehouse totalmente cheio: restock falha, log "FailedRestock-NoSpace".
     */
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

        ReturnItem ret = new ReturnItem("R016", "SKU001", 5, "customer-remorse",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        for (Bay bay : warehouse.getAisle(1).getBays().values()) {
            assertEquals(10, bay.getBoxes().size());
            assertTrue(bay.getBoxes().stream()
                    .noneMatch(b -> b.getBoxId().equals("RET-R016")));
        }

        assertTrue(Files.readAllLines(auditLogFile.toPath()).stream()
                .anyMatch(line -> line.contains("FailedRestock-NoSpace")));
    }

    /**
     * Testa a modificação da data de validade no restock.
     */
    @Test
    public void processReturns_WithExpiryDate() throws Exception {
        LocalDate futureExpiry = LocalDate.now().plusDays(60);
        ReturnItem ret = new ReturnItem("R018", "SKU001", 8, "customer-remorse",
                LocalDateTime.now(), futureExpiry);
        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        assertEquals(futureExpiry, bay.getBoxes().peek().getExpiryDate());
    }

    /**
     * Testa SKUs diferentes: distribui em bays diferentes ou iguais conforme disponibilidade.
     */
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

    /**
     * Testa todas descartadas: warehouse permanece vazio.
     */
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

    /**
     * Testa quantidade grande: restock bem-sucedido mesmo com valores elevados.
     */
    @Test
    public void processReturns_LargeQuantity() throws Exception {
        ReturnItem ret = new ReturnItem("R023", "SKU001", 999999, "customer-remorse",
                LocalDateTime.now(), null);
        manager.processReturns(Collections.singletonList(ret));

        Bay bay = warehouse.getAisle(1).getBay(1);
        assertFalse(bay.isEmpty());
        assertEquals(999999, bay.getBoxes().peek().getQty());
    }

    /**
     * Testa várias razões e verifica no audit log: cada devolução gera exatamente uma entrada.
     */
    @Test
    public void processReturns_MultipleAuditEntries() throws Exception {
        List<ReturnItem> returns = Arrays.asList(
                new ReturnItem("R024", "SKU001", 5, "customer-remorse",
                        LocalDateTime.now(), null),
                new ReturnItem("R025", "SKU001", 3, "damaged",
                        LocalDateTime.now(), null),
                new ReturnItem("R026", "INVALID_SKU", 2, "customer-remorse",
                        LocalDateTime.now(), null)
        );
        manager.processReturns(returns);

        List<String> logLines = Files.readAllLines(auditLogFile.toPath());
        assertEquals(3, logLines.size());

        assertTrue(logLines.stream().anyMatch(line ->
                line.contains("R024") && line.contains("Restocked")));
        assertTrue(logLines.stream().anyMatch(line ->
                line.contains("R025") && line.contains("Discarded")));
        assertTrue(logLines.stream().anyMatch(line ->
                line.contains("R026") && line.contains("Discarded-InvalidSKU")));
    }

    /**
     * Testa ordem FEFO: caixa com data de validade mais próxima fica à frente na PriorityQueue.
     */
    @Test
    public void processReturns_PreservesFEFOOrder() throws Exception {
        List<ReturnItem> returns = Arrays.asList(
                new ReturnItem("R027", "SKU001", 5, "customer-remorse",
                        LocalDateTime.now().plusDays(1), LocalDate.now().plusDays(10)),
                new ReturnItem("R028", "SKU001", 3, "customer-remorse",
                        LocalDateTime.now(), LocalDate.now().plusDays(5)),
                new ReturnItem("R029", "SKU001", 7, "customer-remorse",
                        LocalDateTime.now().minusDays(1), LocalDate.now().plusDays(15))
        );
        manager.processReturns(returns);

        Bay bay = warehouse.getAisle(1).getBay(1);
        assertEquals(3, bay.getBoxes().size());

        // Primeira caixa deve ser a que expira primeiro (R028 - 5 dias)
        Box firstBox = bay.getBoxes().poll();
        assertEquals("RET-R028", firstBox.getBoxId());
    }
}