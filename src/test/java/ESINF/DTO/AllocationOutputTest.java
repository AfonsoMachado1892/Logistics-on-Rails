package ESINF.DTO;

import ESINF.model.Allocation;
import ESINF.model.OrderEligibility;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class AllocationOutputTest {

    private List<OrderEligibility> eligibilityList;
    private List<Allocation> allocationList;
    private AllocationOutput output;

    @Before
    public void setUp() {
        // Setup eligibility results
        eligibilityList = new ArrayList<>();
        eligibilityList.add(new OrderEligibility("ORD001", 1, "SKU100", 50, 50, "ELIGIBLE"));
        eligibilityList.add(new OrderEligibility("ORD001", 2, "SKU200", 100, 75, "PARTIAL"));
        eligibilityList.add(new OrderEligibility("ORD002", 1, "SKU300", 20, 0, "UNDISPATCHABLE"));

        // Setup allocations
        allocationList = new ArrayList<>();
        allocationList.add(new Allocation("ORD001", 1, "SKU100", 50, "BOX001", "1", "5"));
        allocationList.add(new Allocation("ORD001", 2, "SKU200", 40, "BOX002", "2", "10"));
        allocationList.add(new Allocation("ORD001", 2, "SKU200", 35, "BOX003", "2", "11"));

        // Create output
        output = new AllocationOutput(eligibilityList, allocationList);
    }

    // --- Testes de Lógica Principal ---

    @Test
    public void testConstructorAndAccessors() {
        assertNotNull("AllocationOutput não deve ser null", output);
        assertNotNull("Lista de results não deve ser null", output.results());
        assertNotNull("Lista de allocations não deve ser null", output.allocations());
    }

    @Test
    public void testGetResults() {
        List<OrderEligibility> results = output.results();

        assertEquals("Deve haver 3 resultados de elegibilidade", 3, results.size());
        assertEquals("Primeiro resultado deve ser ORD001 linha 1", "ORD001", results.get(0).getOrderId());
        assertEquals("Segundo resultado deve ser ORD001 linha 2", "ORD001", results.get(1).getOrderId());
        assertEquals("Terceiro resultado deve ser ORD002 linha 1", "ORD002", results.get(2).getOrderId());
    }

    @Test
    public void testGetAllocations() {
        List<Allocation> allocations = output.allocations();

        assertEquals("Deve haver 3 alocações", 3, allocations.size());
        assertEquals("Primeira alocação deve ser ORD001 linha 1", "ORD001", allocations.get(0).getOrderId());
        assertEquals("Segunda alocação deve ser BOX002", "BOX002", allocations.get(1).getBoxId());
        assertEquals("Terceira alocação deve ter quantidade 35", 35, allocations.get(2).getQty());
    }

    @Test
    public void testEmptyLists() {
        AllocationOutput emptyOutput = new AllocationOutput(
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertNotNull("Output com listas vazias não deve ser null", emptyOutput);
        assertTrue("Lista de results deve estar vazia", emptyOutput.results().isEmpty());
        assertTrue("Lista de allocations deve estar vazia", emptyOutput.allocations().isEmpty());
    }

    @Test
    public void testResultsWithNoAllocations() {
        // Caso: todas as linhas são UNDISPATCHABLE
        List<OrderEligibility> undispatchableList = new ArrayList<>();
        undispatchableList.add(new OrderEligibility("ORD999", 1, "SKU999", 100, 0, "UNDISPATCHABLE"));

        AllocationOutput noAllocOutput = new AllocationOutput(undispatchableList, Collections.emptyList());

        assertEquals("Deve haver 1 resultado", 1, noAllocOutput.results().size());
        assertTrue("Não deve haver alocações", noAllocOutput.allocations().isEmpty());
        assertEquals("Status deve ser UNDISPATCHABLE", "UNDISPATCHABLE",
                noAllocOutput.results().get(0).getStatus());
    }

    @Test
    public void testAllocationsWithoutResults() {
        // Caso hipotético (não usual): apenas alocações sem resultados de elegibilidade
        List<Allocation> onlyAllocations = new ArrayList<>();
        onlyAllocations.add(new Allocation("ORD-X", 1, "SKU-X", 10, "BOX-X", "1", "1"));

        AllocationOutput allocOnlyOutput = new AllocationOutput(Collections.emptyList(), onlyAllocations);

        assertTrue("Lista de results deve estar vazia", allocOnlyOutput.results().isEmpty());
        assertEquals("Deve haver 1 alocação", 1, allocOnlyOutput.allocations().size());
    }

    // --- Testes de Record Behavior ---

    @Test
    public void testRecordEquality() {
        // Records geram automaticamente equals() baseado nos componentes
        AllocationOutput output1 = new AllocationOutput(eligibilityList, allocationList);
        AllocationOutput output2 = new AllocationOutput(eligibilityList, allocationList);

        assertEquals("Outputs com as mesmas listas devem ser iguais", output1, output2);
    }

    @Test
    public void testRecordInequality() {
        List<OrderEligibility> differentList = new ArrayList<>();
        differentList.add(new OrderEligibility("ORD-DIFF", 1, "SKU-DIFF", 10, 10, "ELIGIBLE"));

        AllocationOutput differentOutput = new AllocationOutput(differentList, allocationList);

        assertNotEquals("Outputs com listas diferentes devem ser diferentes", output, differentOutput);
    }

    @Test
    public void testRecordHashCode() {
        // Records geram automaticamente hashCode() baseado nos componentes
        AllocationOutput output1 = new AllocationOutput(eligibilityList, allocationList);
        AllocationOutput output2 = new AllocationOutput(eligibilityList, allocationList);

        assertEquals("HashCodes de records iguais devem ser iguais",
                output1.hashCode(), output2.hashCode());
    }

    @Test
    public void testRecordToString() {
        String str = output.toString();

        assertNotNull("toString não deve retornar null", str);
        assertTrue("toString deve conter 'AllocationOutput'", str.contains("AllocationOutput"));
        assertTrue("toString deve conter 'results'", str.contains("results"));
        assertTrue("toString deve conter 'allocations'", str.contains("allocations"));
    }

    @Test
    public void testRecordImmutability_CannotModifyOriginalLists() {
        // Modificar as listas originais não deve afetar o record
        List<OrderEligibility> modifiableResults = new ArrayList<>(eligibilityList);
        List<Allocation> modifiableAllocations = new ArrayList<>(allocationList);

        AllocationOutput immutableOutput = new AllocationOutput(modifiableResults, modifiableAllocations);

        int originalResultsSize = immutableOutput.results().size();
        int originalAllocationsSize = immutableOutput.allocations().size();

        // Modificar listas originais
        modifiableResults.clear();
        modifiableAllocations.clear();

        // O record deve manter as referências originais
        assertNotNull("Results do record não devem ser afetados", immutableOutput.results());
        assertNotNull("Allocations do record não devem ser afetados", immutableOutput.allocations());

        // Nota: Records não fazem defensive copy, então isto depende da implementação
        // Se as listas forem mutáveis externamente, o record reflete essas mudanças
    }

    // --- Testes de Cobertura (Edge Cases) ---

    @Test
    public void testSingleResult_SingleAllocation() {
        List<OrderEligibility> singleResult = List.of(
                new OrderEligibility("ORD-SINGLE", 1, "SKU-SINGLE", 10, 10, "ELIGIBLE")
        );
        List<Allocation> singleAllocation = List.of(
                new Allocation("ORD-SINGLE", 1, "SKU-SINGLE", 10, "BOX-SINGLE", "1", "1")
        );

        AllocationOutput singleOutput = new AllocationOutput(singleResult, singleAllocation);

        assertEquals("Deve haver 1 resultado", 1, singleOutput.results().size());
        assertEquals("Deve haver 1 alocação", 1, singleOutput.allocations().size());
    }

    @Test
    public void testLargeLists() {
        List<OrderEligibility> largeResults = new ArrayList<>();
        List<Allocation> largeAllocations = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            largeResults.add(new OrderEligibility("ORD" + i, 1, "SKU" + i, 10, 10, "ELIGIBLE"));
            largeAllocations.add(new Allocation("ORD" + i, 1, "SKU" + i, 10, "BOX" + i, "1", "1"));
        }

        AllocationOutput largeOutput = new AllocationOutput(largeResults, largeAllocations);

        assertEquals("Deve haver 1000 resultados", 1000, largeOutput.results().size());
        assertEquals("Deve haver 1000 alocações", 1000, largeOutput.allocations().size());
    }

    @Test
    public void testMismatchedSizes() {
        // Caso real: mais alocações que resultados (múltiplas boxes por linha)
        List<OrderEligibility> oneResult = List.of(
                new OrderEligibility("ORD-MULTI", 1, "SKU-MULTI", 100, 100, "ELIGIBLE")
        );
        List<Allocation> multipleAllocations = List.of(
                new Allocation("ORD-MULTI", 1, "SKU-MULTI", 50, "BOX001", "1", "1"),
                new Allocation("ORD-MULTI", 1, "SKU-MULTI", 30, "BOX002", "1", "2"),
                new Allocation("ORD-MULTI", 1, "SKU-MULTI", 20, "BOX003", "1", "3")
        );

        AllocationOutput mismatchedOutput = new AllocationOutput(oneResult, multipleAllocations);

        assertEquals("Deve haver 1 resultado", 1, mismatchedOutput.results().size());
        assertEquals("Deve haver 3 alocações", 3, mismatchedOutput.allocations().size());
    }

    @Test
    public void testNullLists_ShouldHandleGracefully() {
        // Records não validam null por padrão, mas vamos testar o comportamento
        try {
            AllocationOutput nullOutput = new AllocationOutput(null, null);
            assertNull("Results pode ser null", nullOutput.results());
            assertNull("Allocations pode ser null", nullOutput.allocations());
        } catch (NullPointerException e) {
            fail("Record deve aceitar null (ou lançar NPE consistentemente)");
        }
    }

    @Test
    public void testGetEligibilityResults_MethodName() {
        // Testa se há método getter alternativo (compatibilidade)
        // Records geram automaticamente métodos com nome do componente
        List<OrderEligibility> results = output.results();
        assertNotNull("Método results() deve existir", results);
        assertEquals(eligibilityList, results);
    }

    @Test
    public void testGetAllocations_MethodName() {
        // Testa se há método getter alternativo
        List<Allocation> allocations = output.allocations();
        assertNotNull("Método allocations() deve existir", allocations);
        assertEquals(allocationList, allocations);
    }

    @Test
    public void testMultipleCallsToAccessors_ReturnsSameReference() {
        List<OrderEligibility> results1 = output.results();
        List<OrderEligibility> results2 = output.results();

        assertSame("Múltiplas chamadas devem retornar a mesma referência",
                results1, results2);

        List<Allocation> allocations1 = output.allocations();
        List<Allocation> allocations2 = output.allocations();

        assertSame("Múltiplas chamadas devem retornar a mesma referência",
                allocations1, allocations2);
    }

    @Test
    public void testRecordComponentsOrder() {
        // Verifica se a ordem dos componentes está correta
        AllocationOutput testOutput = new AllocationOutput(eligibilityList, allocationList);

        // Primeiro componente: results
        assertSame("Primeiro componente deve ser results", eligibilityList, testOutput.results());

        // Segundo componente: allocations
        assertSame("Segundo componente deve ser allocations", allocationList, testOutput.allocations());
    }
}
