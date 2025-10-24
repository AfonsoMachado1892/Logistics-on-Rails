package ESINF.model;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class BoxTest {
    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDateTime now;
    private LocalDateTime yesterday;

    @Before
    public void setUp() throws Exception {
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        now = LocalDateTime.now();
        yesterday = now.minusDays(1);
    }

    @Test
    public void isNotPerishable() {
        Box nonPerishable = new Box("B2", "SKU1", 10, null, now);

        assertFalse( nonPerishable.isPerishable());
    }

    @Test
    public void isPerishable() {
        Box perishable = new Box("B1", "SKU2", 5, tomorrow, now);

        assertTrue( perishable.isPerishable());
    }
    public void testCompareTo_FEFO_EarlierExpiryComesFirst() {
        Box boxExpiresToday = new Box("B1", "SKU1", 10, today, now);
        Box boxExpiresTomorrow = new Box("B2", "SKU1", 10, tomorrow, now);

        // boxExpiresToday < boxExpiresTomorrow
        assertTrue("Caixa que expira hoje deve vir antes da que expira amanhã",
                boxExpiresToday.compareTo(boxExpiresTomorrow) < 0);
        // boxExpiresTomorrow > boxExpiresToday
        assertTrue("Caixa que expira amanhã deve vir depois da que expira hoje",
                boxExpiresTomorrow.compareTo(boxExpiresToday) > 0);
    }

    // Testa a regra FEFO (Perecível vs Não-Perecível) [cite: 274]
    @Test
    public void testCompareTo_FEFO_PerishableComesBeforeNonPerishable() {
        Box perishable = new Box("B1", "SKU1", 10, today, now);
        Box nonPerishable = new Box("B2", "SKU1", 10, null, now);

        // perishable < nonPerishable (nulls last)
        assertTrue("Caixa perecível deve vir antes de não-perecível",
                perishable.compareTo(nonPerishable) < 0);
        // nonPerishable > perishable
        assertTrue("Caixa não-perecível deve vir depois de perecível",
                nonPerishable.compareTo(perishable) > 0);
    }

    // Testa a regra FIFO (First-In-First-Out) [cite: 276]
    @Test
    public void testCompareTo_FIFO_BothNonPerishable_EarlierReceivedComesFirst() {
        Box receivedYesterday = new Box("B1", "SKU1", 10, null, yesterday);
        Box receivedNow = new Box("B2", "SKU1", 10, null, now);

        // receivedYesterday < receivedNow
        assertTrue("Caixa recebida ontem deve vir antes da recebida hoje",
                receivedYesterday.compareTo(receivedNow) < 0);
        // receivedNow > receivedYesterday
        assertTrue("Caixa recebida hoje deve vir depois da recebida ontem",
                receivedNow.compareTo(receivedYesterday) > 0);
    }

    // Testa a regra FIFO (Datas de validade iguais) [cite: 276]
    @Test
    public void testCompareTo_FIFO_SameExpiryDate_EarlierReceivedComesFirst() {
        Box receivedYesterday = new Box("B1", "SKU1", 10, today, yesterday);
        Box receivedNow = new Box("B2", "SKU1", 10, today, now);

        // receivedYesterday < receivedNow
        assertTrue("Com mesma validade, a recebida ontem vem primeiro",
                receivedYesterday.compareTo(receivedNow) < 0);
        // receivedNow > receivedYesterday
        assertTrue("Com mesma validade, a recebida hoje vem depois",
                receivedNow.compareTo(receivedYesterday) > 0);
    }

    // Testa a regra de Tie-Break (Box ID) [cite: 277]
    @Test
    public void testCompareTo_TieBreak_SameExpiryAndReceived_BoxIdComesFirst() {
        Box boxA = new Box("BOX-A-100", "SKU1", 10, today, now);
        Box boxB = new Box("BOX-B-200", "SKU1", 10, today, now);

        // boxA < boxB
        assertTrue("Com tudo igual, ID 'A' deve vir antes de 'B'",
                boxA.compareTo(boxB) < 0);
        // boxB > boxA
        assertTrue("Com tudo igual, ID 'B' deve vir depois de 'A'",
                boxB.compareTo(boxA) > 0);
    }

    // Testa a igualdade
    @Test
    public void testCompareTo_Equality() {
        Box box1 = new Box("BOX-A", "SKU1", 10, today, now);
        Box box2 = new Box("BOX-A", "SKU1", 10, today, now);

        assertEquals("Caixas com os mesmos dados (incluindo ID) devem ser iguais",
                0, box1.compareTo(box2));
    }
    @Test
    public void testConstructorAndGetters() {
        Box box = new Box("B1", "SKU1", 10, today, now);

        assertEquals("B1", box.getBoxId());
        assertEquals("SKU1", box.getSku());
        assertEquals(10, box.getQty());
        assertEquals(today, box.getExpiryDate());
        assertEquals(now, box.getReceivedAt());

        // Bay começa como null, pois não é definida no construtor
        assertNull(box.getBay());
    }

    /**
     * Testa o setQty.
     */
    @Test
    public void testSetQty() {
        Box box = new Box("B1", "SKU1", 10, today, now);
        assertEquals(10, box.getQty()); // Verifica estado inicial

        box.setQty(99); // Modifica

        assertEquals(99, box.getQty()); // Verifica estado final
    }

    /**
     * Testa getAisleNumber quando a baia é null (cobre o 'else' do ternário).
     */
    @Test
    public void testGetAisleNumber_NullBay() {
        Box box = new Box("B1", "SKU1", 10, today, now);
        assertNull("getAisleNumber deve retornar null se a baia for null", box.getBay());
        assertNull("getAisleNumber deve retornar null se a baia for null", box.getAisleNumber());
    }

    /**
     * Testa setBay, getBay e getAisleNumber quando a baia não é null.
     */
    @Test
    public void testGetAisleNumber_WithBay() {
        Box box = new Box("B1", "SKU1", 10, today, now);

        // Cria uma Bay (simulada ou real, real é fácil aqui)
        Bay testBay = new Bay(1, "W01", 3, 10); // Bay 1, Aisle 3

        // Testa setBay e getBay
        box.setBay(testBay);
        assertSame("getBay deve retornar o objeto Bay exato que foi definido",
                testBay, box.getBay());

        // Testa getAisleNumber (cobre o 'if' do ternário)
        Integer aisleNum = box.getAisleNumber();
        assertNotNull(aisleNum);
        assertEquals("Aisle number deve ser 3",
                Integer.valueOf(3), aisleNum);
    }

    /**
     * Testa o método toString.
     */
    @Test
    public void testToString() {
        Box box = new Box("B1", "SKU1", 10, today, now);
        String boxString = box.toString();

        assertTrue("toString deve conter o BoxId", boxString.contains("Box[B1"));
        assertTrue("toString deve conter o SKU", boxString.contains("SKU=SKU1"));
        assertTrue("toString deve conter a Quantidade", boxString.contains("Qty=10"));
        assertTrue("toString deve conter a Data de Expiração", boxString.contains("Exp=" + today.toString()));
        assertTrue("toString deve conter a Data de Receção", boxString.contains("Rec=" + now.toString()));
    }
}