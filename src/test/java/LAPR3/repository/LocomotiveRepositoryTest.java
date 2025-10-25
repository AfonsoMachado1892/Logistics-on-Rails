package LAPR3.repository;

import LAPR3.model.Locomotive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class LocomotiveRepositoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // Headers: Number, Name, Make, Model, Service, NumBogies, Bogies,
    //          Power, Length, Width, Height, Weight, MaxSpeed,
    //          OpSpeed, Traction, Type, Voltage, Frequency,
    //          Operator, Bitola, Combustível
    private final String HEADER = "Number\tName\tMake\tModel\tService\tNBogies\tBogies\tPower\tLength\tWidth\tHeight\tWeight\tMaxSpeed\tOpSpeed\tTraction\tType\tVoltage\tFreq\tOperator\tBitola\tFuel\n";
    private final String DIESEL_LOCO = "101\tDiesel1\tM\tM\t2000\t4\tT\t3000\t20\t3\t4\t100\t160\t140\t300\tDiesel\t\t\tCP\t1435\t5000\n";
    private final String ELECTRIC_LOCO = "201\tElectric1\tM\tM\t2010\t4\tT\t5000\t18\t3\t4\t80\t200\t180\t400\tElectric\t25kV\t50Hz\tCP\t1435\t\n";
    private final String DIESEL_LOCO_MEDWAY = "102\tDiesel2\tM\tM\t2005\t4\tT\t3000\t20\t3\t4\t100\t160\t120\t300\tDiesel\t\t\tMedway\t1000\t4000\n";
    private final String INVALID_LOCO = "103\tInvalid\tM\tM\t2000\t4\tT\t3000\t20\t3\t4\t100\t160\t0\t300\tDiesel\t\t\tCP\t1435\t5000\n"; // OpSpeed 0


    private LocomotiveRepository createAndLoadRepo() throws IOException {
        File file = folder.newFile("locomotives.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(HEADER);
            writer.write(DIESEL_LOCO);
            writer.write(ELECTRIC_LOCO);
            writer.write(DIESEL_LOCO_MEDWAY);
            writer.write(INVALID_LOCO);
        }

        LocomotiveRepository repo = new LocomotiveRepository();
        repo.loadFromCSV(file.getAbsolutePath());
        return repo;
    }

    @Test
    public void testLoadFromCSV() throws IOException {
        LocomotiveRepository repo = createAndLoadRepo();
        // Deve carregar 3 válidos, ignorar 1 inválido
        assertEquals(3, repo.count());
        assertTrue(repo.exists(101));
        assertTrue(repo.exists(201));
        assertTrue(repo.exists(102));
        assertFalse(repo.exists(103));
    }

    @Test
    public void testFindByNumber() throws IOException {
        LocomotiveRepository repo = createAndLoadRepo();

        Optional<Locomotive> loco101 = repo.findByNumber(101);
        assertTrue(loco101.isPresent());
        assertEquals("Diesel1", loco101.get().getName());
        assertEquals(Locomotive.LocomotiveType.DIESEL, loco101.get().getType());

        Optional<Locomotive> loco999 = repo.findByNumber(999);
        assertFalse(loco999.isPresent());
    }

    @Test
    public void testFindByType() throws IOException {
        LocomotiveRepository repo = createAndLoadRepo();

        List<Locomotive> diesels = repo.findByType(Locomotive.LocomotiveType.DIESEL);
        assertEquals(2, diesels.size()); // 101 e 102

        List<Locomotive> electrics = repo.findByType(Locomotive.LocomotiveType.ELECTRIC);
        assertEquals(1, electrics.size()); // 201
    }

    @Test
    public void testFindByGauge() throws IOException {
        LocomotiveRepository repo = createAndLoadRepo();

        List<Locomotive> gauge1435 = repo.findByGauge(1435);
        assertEquals(2, gauge1435.size()); // 101 e 201

        List<Locomotive> gauge1000 = repo.findByGauge(1000);
        assertEquals(1, gauge1000.size()); // 102
    }

    @Test
    public void testFindByOperator() throws IOException {
        LocomotiveRepository repo = createAndLoadRepo();

        List<Locomotive> cp = repo.findByOperator("CP");
        assertEquals(2, cp.size()); // 101 e 201

        // Teste case-insensitive
        List<Locomotive> medway = repo.findByOperator("medway");
        assertEquals(1, medway.size()); // 102
    }
}
