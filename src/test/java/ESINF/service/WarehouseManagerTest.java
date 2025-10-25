package ESINF.service; // Note o package diferente

import ESINF.model.*;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class WarehouseManagerTest {

    private WarehouseManager manager;
    private Warehouse warehouse;
    private Bay bay1; // cap 2
    private Bay bay2; // cap 2
    private Box boxA_SKU1; // 50 unidades, FIFO
    private Box boxB_SKU1; // 30 unidades, FIFO (mais recente)
    private Box boxC_SKU2; // 10 unidades, FEFO
    private Box boxD_SKU1; // 20 unidades, FEFO (urgente)

    @Before
    public void setUp() {
        warehouse = new Warehouse("W01");
        Aisle aisle1 = new Aisle(1);

        bay1 = new Bay(1, "W01", 1, 2); // Bay 1
        bay2 = new Bay(2, "W01", 1, 2); // Bay 2

        aisle1.addBay(bay1); // Adiciona bay1 primeiro
        aisle1.addBay(bay2); // Adiciona bay2 depois
        warehouse.addAisle(aisle1);

        manager = new WarehouseManager(warehouse);

        LocalDateTime now = LocalDateTime.now();
        boxA_SKU1 = new Box("B-A", "SKU1", 50, null, now.minusHours(1)); // 50
        boxB_SKU1 = new Box("B-B", "SKU1", 30, null, now);                 // 30
        boxC_SKU2 = new Box("B-C", "SKU2", 10, LocalDate.now().plusDays(5), now); // 10
        boxD_SKU1 = new Box("B-D", "SKU1", 20, LocalDate.now().plusDays(1), now); // 20 (urgente)
    }

    // --- Testes para unloadWagons ---

    @Test
    public void testUnloadWagons_SimpleUnload() {
        Wagon wagon = new Wagon("W-100");
        wagon.addBox(boxA_SKU1);

        manager.unloadWagons(Collections.singletonList(wagon));

        assertEquals("bay1 deve ter 1 caixa", 1, bay1.getBoxes().size());
        assertEquals("Caixa deve ser a B-A", "B-A", bay1.getBoxes().peek().getBoxId());
        assertTrue("bay2 deve estar vazia", bay2.isEmpty());
        assertSame("A 'bay' da caixa deve ser bay1", bay1, boxA_SKU1.getBay());
    }

    @Test
    public void testUnloadWagons_FillAndSpillover() {
        Wagon wagon = new Wagon("W-100");
        wagon.addBox(boxA_SKU1);
        wagon.addBox(boxB_SKU1);
        wagon.addBox(boxC_SKU2); // 3ª caixa

        // bay1 tem cap 2, bay2 tem cap 2
        manager.unloadWagons(Collections.singletonList(wagon));

        assertTrue("bay1 deve estar cheia", bay1.isFull());
        assertEquals("bay2 deve ter a 3ª caixa", 1, bay2.getBoxes().size());
        assertEquals("Caixa em bay2 deve ser B-C", "B-C", bay2.getBoxes().peek().getBoxId());
    }

    @Test
    public void testUnloadWagons_NoAvailableSpace() {
        // Enche a warehouse (4 caixas no total)
        manager.unloadWagons(Collections.singletonList(new Wagon("T1") {{
            addBox(new Box("T1", "SKU-T", 1, null, LocalDateTime.now()));
            addBox(new Box("T2", "SKU-T", 1, null, LocalDateTime.now()));
            addBox(new Box("T3", "SKU-T", 1, null, LocalDateTime.now()));
            addBox(new Box("T4", "SKU-T", 1, null, LocalDateTime.now()));
        }}));

        assertTrue(bay1.isFull());
        assertTrue(bay2.isFull());

        Wagon wagonExtra = new Wagon("W-100");
        wagonExtra.addBox(boxA_SKU1); // Tenta adicionar 5ª caixa

        manager.unloadWagons(Collections.singletonList(wagonExtra));

        // Nada deve ter mudado
        assertEquals(2, bay1.getBoxes().size());
        assertEquals(2, bay2.getBoxes().size());
        assertNull("A 'bay' da caixa extra deve ser null", boxA_SKU1.getBay());
        // (A implementação deve logar um erro, que é testado manualmente)
    }

    // --- Testes para dispatch ---

    @Test
    public void testDispatch_SimplePartial() {
        bay1.addBox(boxA_SKU1); // 50 unidades de SKU1

        List<Box> dispatched = manager.dispatch("SKU1", 20);

        assertEquals("Deve retornar 1 fragmento de caixa", 1, dispatched.size());
        assertEquals("Fragmento deve ter 20 unidades", 20, dispatched.get(0).getQty());
        assertEquals("Caixa original na baia deve ter 30 unidades restantes", 30, boxA_SKU1.getQty());
        assertEquals("Baia deve continuar com 1 caixa", 1, bay1.getBoxes().size());
    }

    @Test
    public void testDispatch_FullBox() {
        bay1.addBox(boxA_SKU1); // 50 unidades de SKU1

        List<Box> dispatched = manager.dispatch("SKU1", 50);

        assertEquals(1, dispatched.size());
        assertEquals(50, dispatched.get(0).getQty());

        // A implementação [WarehouseManager.java] remove a caixa se qty == 0 [cite: 279]
        assertEquals("Caixa original deve ter 0 unidades", 0, boxA_SKU1.getQty());
        assertTrue("Baia deve ficar vazia", bay1.isEmpty());
    }

    @Test
    public void testDispatch_PartialAcrossMultipleBays() {
        // A implementação do dispatch itera as baias por ordem crescente de número [cite: 279]
        bay1.addBox(boxA_SKU1); // Bay 1: SKU1, 50 unidades
        bay2.addBox(boxB_SKU1); // Bay 2: SKU1, 30 unidades

        List<Box> dispatched = manager.dispatch("SKU1", 60);

        assertEquals("Deve retornar 2 fragmentos (1 de cada caixa)", 2, dispatched.size());

        assertTrue("Baia 1 deve estar vazia (boxA consumida)", bay1.isEmpty());
        assertEquals("Caixa B (Bay 2) deve ter 20 unidades restantes", 20, boxB_SKU1.getQty());
        assertEquals("Bay 2 deve ter 1 caixa", 1, bay2.getBoxes().size());
    }

    @Test
    public void testDispatch_RespectsFEFO_Order() {
        // boxA (FIFO, 50), boxD (FEFO, 20)
        bay1.addBox(boxA_SKU1); // Bay 1
        bay1.addBox(boxD_SKU1); // Bay 1

        // A baia 1, sendo PriorityQueue, tem boxD (FEFO) à frente
        assertEquals("B-D", bay1.getBoxes().peek().getBoxId());

        // Pedido de 30 unidades de SKU1
        List<Box> dispatched = manager.dispatch("SKU1", 30);

        // Deve consumir 20 da boxD e 10 da boxA
        assertEquals(2, dispatched.size());

        assertEquals("Primeiro fragmento deve ser da Box D", "B-D", dispatched.get(0).getBoxId());
        assertEquals("Fragmento D deve ter 20 unidades", 20, dispatched.get(0).getQty());

        assertEquals("Segundo fragmento deve ser da Box A", "B-A", dispatched.get(1).getBoxId());
        assertEquals("Fragmento A deve ter 10 unidades", 10, dispatched.get(1).getQty());

        // A baia 1 deve ficar apenas com 40 unidades da boxA
        assertEquals("Baia 1 deve ter 1 caixa", 1, bay1.getBoxes().size());
        assertEquals("Caixa A deve ter 40 unidades restantes", 40, boxA_SKU1.getQty());
    }

    @Test
    public void testDispatch_StockShortage() {
        bay1.addBox(boxA_SKU1); // 50 unidades de SKU1

        List<Box> dispatched = manager.dispatch("SKU1", 100);

        assertEquals(1, dispatched.size());
        assertEquals("Deve ter despachado apenas as 50 disponíveis", 50, dispatched.get(0).getQty());
        assertTrue("Baia 1 deve ficar vazia", bay1.isEmpty());
        // (A implementação deve logar um erro)
    }

    @Test
    public void testDispatch_SkuNotFound() {
        bay1.addBox(boxA_SKU1); // SKU1
        List<Box> dispatched = manager.dispatch("SKU-NAO-EXISTE", 20);
        assertTrue("Lista de dispatch deve ser vazia", dispatched.isEmpty());
        assertEquals("Stock não deve ser alterado", 50, boxA_SKU1.getQty());
    }

    @Test
    public void testDispatch_ZeroOrNegativeQuantity() {
        bay1.addBox(boxA_SKU1);
        List<Box> dispatchedZero = manager.dispatch("SKU1", 0);
        assertTrue("Dispatch de 0 deve retornar lista vazia", dispatchedZero.isEmpty());

        List<Box> dispatchedNegative = manager.dispatch("SKU1", -10);
        assertTrue("Dispatch de -10 deve retornar lista vazia", dispatchedNegative.isEmpty());

        assertEquals("Stock não deve ser alterado", 50, boxA_SKU1.getQty());
    }

    // --- Testes de Cobertura (Getters/etc.) ---

    @Test
    public void testGetAllBoxes() {
        bay1.addBox(boxA_SKU1);
        bay1.addBox(boxC_SKU2);
        bay2.addBox(boxB_SKU1);

        List<Box> allBoxes = manager.getAllBoxes();
        assertEquals("Deve encontrar 3 caixas no total", 3, allBoxes.size());
        assertTrue(allBoxes.contains(boxA_SKU1));
        assertTrue(allBoxes.contains(boxB_SKU1));
        assertTrue(allBoxes.contains(boxC_SKU2));
    }

    @Test
    public void testGetBoxesForSku_Sorted() {
        // Adiciona 3 caixas de SKU1 em ordem desordenada
        bay1.addBox(boxA_SKU1); // FIFO (velha)
        bay2.addBox(boxB_SKU1); // FIFO (nova)
        bay1.addBox(boxD_SKU1); // FEFO (urgente)
        bay2.addBox(boxC_SKU2); // Outro SKU

        List<Box> sku1Boxes = manager.getBoxesForSku("SKU1");

        assertEquals("Deve encontrar 3 caixas de SKU1", 3, sku1Boxes.size());

        // Verifica a ordem FEFO/FIFO [cite: 289]
        assertEquals("Primeira caixa deve ser a FEFO urgente", "B-D", sku1Boxes.get(0).getBoxId());
        assertEquals("Segunda caixa deve ser a FIFO mais antiga", "B-A", sku1Boxes.get(1).getBoxId());
        assertEquals("Terceira caixa deve ser a FIFO mais recente", "B-B", sku1Boxes.get(2).getBoxId());
    }

    @Test
    public void testGetBoxesForSku_NotFound() {
        bay1.addBox(boxC_SKU2);
        List<Box> boxes = manager.getBoxesForSku("SKU-INEXISTENTE");
        assertTrue("Lista deve estar vazia para SKU inexistente", boxes.isEmpty());
    }
}