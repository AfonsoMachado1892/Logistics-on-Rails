package LAPR3.repository;

import LAPR3.model.Segment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class SegmentRepositoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // Headers: ID, Line, Order, Electrified, Max Weight (kg/m), Length (m), NumberTracks
    private final String HEADER = "ID\tLine\tOrder\tElectrified\tMaxWeight\tLength\tTracks\n";
    private final String SEG_10_1 = "101\t10\t1\tYes\t5000\t10000\t2\n";
    private final String SEG_10_3 = "103\t10\t3\tNo\t5000\t5000\t1\n";
    private final String SEG_10_2 = "102\t10\t2\tYes\t5000\t20000\t2\n";
    private final String SEG_20_1 = "201\t20\t1\tYes\t6000\t50000\t2\n";
    private final String SEG_INV = "202\t20\t2\tYes\t6000\t0\t2\n"; // Comprimento 0

    private SegmentRepository createAndLoadRepo() throws IOException {
        File file = folder.newFile("segments.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(HEADER);
            writer.write(SEG_10_1);
            writer.write(SEG_10_3); // Adicionados fora de ordem
            writer.write(SEG_10_2);
            writer.write(SEG_20_1);
            writer.write(SEG_INV);
        }

        SegmentRepository repo = new SegmentRepository();
        repo.loadFromCSV(file.getAbsolutePath());
        return repo;
    }

    @Test
    public void testLoadFromCSV() throws IOException {
        SegmentRepository repo = createAndLoadRepo();
        // Deve carregar 4 válidos, ignorar 1 inválido
        assertEquals(4, repo.count());
        assertTrue(repo.exists(101));
        assertTrue(repo.exists(102));
        assertTrue(repo.exists(103));
        assertTrue(repo.exists(201));
        assertFalse(repo.exists(202));
    }

    @Test
    public void testFindByLineId_ReturnsSortedByOrder() throws IOException {
        SegmentRepository repo = createAndLoadRepo();

        List<Segment> line10Segments = repo.findByLineId(10);

        assertEquals(3, line10Segments.size());

        // Verificar se estão ordenados por 'order', não por ID ou ordem de inserção
        assertEquals(101, line10Segments.get(0).getId()); // Order 1
        assertEquals(102, line10Segments.get(1).getId()); // Order 2
        assertEquals(103, line10Segments.get(2).getId()); // Order 3
    }

    @Test
    public void testGetTotalLengthByLine() throws IOException {
        SegmentRepository repo = createAndLoadRepo();

        // Linha 10: 10000 + 5000 + 20000 = 35000
        assertEquals(35000.0, repo.getTotalLengthByLine(10), 0.001);

        // Linha 20: 50000
        assertEquals(50000.0, repo.getTotalLengthByLine(20), 0.001);
    }

    @Test
    public void testIsLineFullyElectrified() throws IOException {
        SegmentRepository repo = createAndLoadRepo();

        // Linha 10 não é (Segmento 103 é 'No')
        assertFalse(repo.isLineFullyElectrified(10));

        // Linha 20 é
        assertTrue(repo.isLineFullyElectrified(20));
    }
}
