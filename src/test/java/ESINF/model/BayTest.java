package ESINF.model;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class BayTest {

    private Bay bayCap2;
    private LocalDateTime now;
    private LocalDate tomorrow;
    private Box boxFIFO; // Não perecível
    private Box boxFEFO; // Perecível

    @Before
    public void setUp() {
        bayCap2 = new Bay(1, "W01", 1, 2); // Capacidade 2
        now = LocalDateTime.now();
        tomorrow = LocalDate.now().plusDays(1);

        // FIFO: Não perecível (null), recebida agora
        boxFIFO = new Box("B-FIFO", "SKU1", 10, null, now);

        // FEFO: Perecível, expira amanhã, recebida agora
        boxFEFO = new Box("B-FEFO", "SKU1", 10, tomorrow, now);
    }

    // --- Testes de Lógica Principal ---

    @Test
    public void testAddAndDispatchBox_RespectsFEFO_Order() {
        assertTrue(bayCap2.addBox(boxFIFO)); // Adiciona FIFO primeiro
        assertTrue(bayCap2.addBox(boxFEFO)); // Adiciona FEFO depois

        // A boxFEFO deve estar à frente (peek) porque perecível vem antes
        Box firstBox = bayCap2.getBoxes().peek();
        assertNotNull(firstBox);
        assertEquals("A caixa FEFO (perecível) deve estar à frente da fila",
                "B-FEFO", firstBox.getBoxId());

        // O primeiro dispatch deve ser a caixa FEFO
        Box dispatchedBox1 = bayCap2.dispatchBox();
        assertEquals("Primeira caixa despachada deve ser a FEFO", "B-FEFO", dispatchedBox1.getBoxId());

        // O segundo dispatch deve ser a caixa FIFO
        Box dispatchedBox2 = bayCap2.dispatchBox();
        assertEquals("Segunda caixa despachada deve ser a FIFO", "B-FIFO", dispatchedBox2.getBoxId());
    }

    @Test
    public void testAddBox_CapacityLimit() {
        assertTrue(bayCap2.addBox(boxFIFO));
        assertTrue(bayCap2.addBox(boxFEFO));

        // Tenta adicionar a terceira caixa, deve falhar
        Box boxExtra = new Box("B-EXTRA", "SKU1", 10, null, now);
        assertFalse("Não deve ser possível adicionar caixa a baia cheia",
                bayCap2.addBox(boxExtra));
        assertEquals("Baia deve ter apenas 2 caixas", 2, bayCap2.getBoxes().size());
    }

    @Test
    public void testDispatchBox_FromEmptyBay() {
        assertTrue("Baia deve estar vazia inicialmente", bayCap2.isEmpty());
        Box dispatchedBox = bayCap2.dispatchBox();
        assertNull("Dispatch de baia vazia deve retornar null", dispatchedBox);
    }

    // --- Testes de Cobertura (Getters/Setters/etc.) ---

    @Test
    public void testConstructorAndGetters() {
        Bay bay = new Bay(10, "W-TEST", 3, 50);
        assertEquals(10, bay.getNumber());
        assertEquals(3, bay.getAisleNumber());
        assertEquals(50, bay.getCapacityBoxes());
        assertNotNull("A PriorityQueue 'boxes' não deve ser null", bay.getBoxes());
        assertTrue("A PriorityQueue 'boxes' deve estar vazia", bay.getBoxes().isEmpty());
    }

    @Test
    public void testIsEmpty() {
        assertTrue("Baia nova deve estar vazia", bayCap2.isEmpty());
        bayCap2.addBox(boxFIFO);
        assertFalse("Baia com 1 caixa não deve estar vazia", bayCap2.isEmpty());
    }

    @Test
    public void testIsFull() {
        assertFalse("Baia (cap 2) com 0 caixas não deve estar cheia", bayCap2.isFull());
        bayCap2.addBox(boxFIFO);
        assertFalse("Baia (cap 2) com 1 caixa não deve estar cheia", bayCap2.isFull());
        bayCap2.addBox(boxFEFO);
        assertTrue("Baia (cap 2) com 2 caixas deve estar cheia", bayCap2.isFull());
    }

    @Test
    public void testToString() {
        bayCap2.addBox(boxFIFO);
        String str = bayCap2.toString();

        assertTrue(str.contains("Bay[1")); // number
        assertTrue(str.contains("aisle=1")); // aisleNumber
        assertTrue(str.contains("1 boxes")); // boxes.size()
    }
}