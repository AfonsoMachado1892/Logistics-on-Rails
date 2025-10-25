package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class OrderTest {

    private Order order1;
    private Order order2;
    private Order order3;
    private LocalDateTime now;
    private LocalDateTime tomorrow;
    private LocalDateTime nextWeek;

    @Before
    public void setUp() {
        now = LocalDateTime.now();
        tomorrow = now.plusDays(1);
        nextWeek = now.plusDays(7);

        order1 = new Order("ORD001", tomorrow, 1);
        order2 = new Order("ORD002", now, 2);
        order3 = new Order("ORD003", nextWeek, 1);
    }

    // --- Testes de Lógica Principal ---

    @Test
    public void testCompareTo_SortByPriority() {
        // Priority: 1 < 2 < 3
        Order highPriority = new Order("ORD-HIGH", tomorrow, 1);
        Order lowPriority = new Order("ORD-LOW", tomorrow, 3);

        assertTrue("Ordem com prioridade 1 deve vir antes de prioridade 3",
                highPriority.compareTo(lowPriority) < 0);
        assertTrue("Ordem com prioridade 3 deve vir depois de prioridade 1",
                lowPriority.compareTo(highPriority) > 0);
    }

    @Test
    public void testCompareTo_SortByDueDate_WhenSamePriority() {
        Order earlyDue = new Order("ORD-EARLY", now, 1);
        Order lateDue = new Order("ORD-LATE", nextWeek, 1);

        assertTrue("Ordem com due date mais cedo deve vir primeiro",
                earlyDue.compareTo(lateDue) < 0);
        assertTrue("Ordem com due date mais tarde deve vir depois",
                lateDue.compareTo(earlyDue) > 0);
    }

    @Test
    public void testCompareTo_SortByOrderId_WhenSamePriorityAndDueDate() {
        Order orderA = new Order("ORD-AAA", tomorrow, 1);
        Order orderB = new Order("ORD-ZZZ", tomorrow, 1);

        assertTrue("Ordem com ID alfabeticamente menor deve vir primeiro",
                orderA.compareTo(orderB) < 0);
        assertTrue("Ordem com ID alfabeticamente maior deve vir depois",
                orderB.compareTo(orderA) > 0);
    }

    @Test
    public void testCompareTo_CompleteOrderingSorting() {
        List<Order> orders = new ArrayList<>();

        // Different priorities
        orders.add(new Order("ORD-P3", tomorrow, 3));
        orders.add(new Order("ORD-P1", tomorrow, 1));
        orders.add(new Order("ORD-P2", tomorrow, 2));

        // Same priority, different dates
        orders.add(new Order("ORD-LATE", nextWeek, 1));
        orders.add(new Order("ORD-EARLY", now, 1));

        // Same priority, same date, different IDs
        orders.add(new Order("ORD-ZZZ", tomorrow, 1));
        orders.add(new Order("ORD-AAA", tomorrow, 1));

        Collections.sort(orders);

        // Expected order:
        // 1. ORD-EARLY (priority=1, date=now)
        // 2. ORD-AAA (priority=1, date=tomorrow, ID=AAA)
        // 3. ORD-P1 (priority=1, date=tomorrow, ID=ORD-P1)
        // 4. ORD-ZZZ (priority=1, date=tomorrow, ID=ZZZ)
        // 5. ORD-LATE (priority=1, date=nextWeek)
        // 6. ORD-P2 (priority=2)
        // 7. ORD-P3 (priority=3)

        assertEquals("Primeira ordem deve ser ORD-EARLY", "ORD-EARLY", orders.get(0).getOrderId());
        assertEquals("Segunda ordem deve ser ORD-AAA", "ORD-AAA", orders.get(1).getOrderId());
        assertEquals("Última ordem deve ser ORD-P3", "ORD-P3", orders.get(6).getOrderId());
    }

    @Test
    public void testAddLine() {
        OrderLine line1 = new OrderLine("ORD001", 1, "SKU100", 10);
        OrderLine line2 = new OrderLine("ORD001", 2, "SKU200", 20);

        order1.addLine(line1);
        order1.addLine(line2);

        assertEquals("Ordem deve ter 2 linhas", 2, order1.getLines().size());
        assertTrue("Lista deve conter line1", order1.getLines().contains(line1));
        assertTrue("Lista deve conter line2", order1.getLines().contains(line2));
    }

    @Test
    public void testSortLines_ByLineNo() {
        OrderLine line3 = new OrderLine("ORD001", 3, "SKU300", 30);
        OrderLine line1 = new OrderLine("ORD001", 1, "SKU100", 10);
        OrderLine line2 = new OrderLine("ORD001", 2, "SKU200", 20);

        // Add in random order
        order1.addLine(line3);
        order1.addLine(line1);
        order1.addLine(line2);

        order1.sortLines();

        List<OrderLine> sortedLines = order1.getLines();
        assertEquals("Primeira linha deve ter lineNo=1", 1, sortedLines.get(0).getLineNo());
        assertEquals("Segunda linha deve ter lineNo=2", 2, sortedLines.get(1).getLineNo());
        assertEquals("Terceira linha deve ter lineNo=3", 3, sortedLines.get(2).getLineNo());
    }

    // --- Testes de Cobertura (Getters/Setters/etc.) ---

    @Test
    public void testConstructorAndGetters() {
        Order order = new Order("ORD-TEST", tomorrow, 2);

        assertEquals("ORD-TEST", order.getOrderId());
        assertEquals(tomorrow, order.getDueDate());
        assertEquals(2, order.getPriority());
        assertNotNull("Lista de linhas não deve ser null", order.getLines());
        assertTrue("Lista de linhas deve estar vazia inicialmente", order.getLines().isEmpty());
    }

    @Test
    public void testGetLines_ReturnsActualList() {
        OrderLine line1 = new OrderLine("ORD001", 1, "SKU100", 10);
        order1.addLine(line1);

        List<OrderLine> lines = order1.getLines();
        assertSame("Deve retornar a lista interna", order1.getLines(), lines);
    }

    @Test
    public void testToString() {
        order1.addLine(new OrderLine("ORD001", 1, "SKU100", 10));
        order1.addLine(new OrderLine("ORD001", 2, "SKU200", 20));

        String str = order1.toString();

        assertTrue(str.contains("Order[ORD001"));
        assertTrue(str.contains("priority=1"));
        assertTrue(str.contains("lines=2"));
        assertTrue(str.contains("due="));
    }

    @Test
    public void testCompareTo_SameOrder() {
        Order sameOrder = new Order("ORD001", tomorrow, 1);
        assertEquals("Comparação da mesma ordem deve retornar 0",
                0, order1.compareTo(sameOrder));
    }

    @Test
    public void testSortLines_EmptyList() {
        Order emptyOrder = new Order("ORD-EMPTY", tomorrow, 1);
        emptyOrder.sortLines();

        assertTrue("Lista de linhas deve continuar vazia", emptyOrder.getLines().isEmpty());
    }

    @Test
    public void testSortLines_SingleLine() {
        OrderLine line1 = new OrderLine("ORD001", 1, "SKU100", 10);
        order1.addLine(line1);

        order1.sortLines();

        assertEquals("Deve haver apenas 1 linha", 1, order1.getLines().size());
        assertEquals(1, order1.getLines().get(0).getLineNo());
    }
}
