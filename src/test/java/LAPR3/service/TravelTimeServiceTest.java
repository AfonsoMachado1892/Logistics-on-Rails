package LAPR3.service;

import LAPR3.DTO.TravelTimeResult;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.LineRepository;
import LAPR3.repository.LocomotiveRepository;
import LAPR3.repository.SegmentRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

public class TravelTimeServiceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private TravelTimeService service;

    // Repositórios reais
    private FacilityRepository facilityRepo;
    private LineRepository lineRepo;
    private SegmentRepository segmentRepo;
    private LocomotiveRepository locomotiveRepo;

    /**
     * Cria um ambiente de teste completo com ficheiros CSV
     */
    @Before
    public void setUp() throws IOException {
        // 1. Criar Repositórios
        facilityRepo = new FacilityRepository();
        lineRepo = new LineRepository(); // Assumindo que LineRepository existe
        segmentRepo = new SegmentRepository();
        locomotiveRepo = new LocomotiveRepository();

        // 2. Escrever ficheiros CSV

        // Facilities
        File facFile = folder.newFile("facilities.csv");
        try (FileWriter w = new FileWriter(facFile)) {
            w.write("ID\tName\n");
            w.write("1\tStation A\n");
            w.write("2\tStation B\n");
            w.write("3\tStation C\n");
        }

        // Lines (Assumindo que LineRepository também usa CSVReader)
        File lineFile = folder.newFile("lines.csv");
        try (FileWriter w = new FileWriter(lineFile)) {
            w.write("ID\tName\tOwner\tStartID\tStartName\tEndID\tEndName\tGauge\n");
            w.write("10\tLinha AB\tIP\t1\tStation A\t2\tStation B\t1435\n"); // Linha OK
            w.write("20\tLinha BC\tIP\t2\tStation B\t3\tStation C\t1000\n"); // Bitola errada
        }

        // Locomotives
        File locoFile = folder.newFile("locomotives.csv");
        try (FileWriter w = new FileWriter(locoFile)) {
            String H = "Number\tName\tMake\tModel\tService\tNBogies\tBogies\tPower\tLength\tWidth\tHeight\tWeight\tMaxSpeed\tOpSpeed\tTraction\tType\tVoltage\tFreq\tOperator\tBitola\tFuel\n";
            w.write(H);
            // 101: Diesel OK, Bitola 1435, Vel 100
            w.write("101\tDieselOK\tM\tM\t2000\t4\tT\t3k\t20\t3\t4\t100\t160\t100\t300\tDiesel\t\t\tCP\t1435\t5000\n");
            // 102: Electric OK, Bitola 1435, Vel 120
            w.write("102\tElectricOK\tM\tM\t2010\t4\tT\t5k\t18\t3\t4\t80\t200\t120\t400\tElectric\t25kV\t50Hz\tCP\t1435\t\n");
            // 103: Diesel Bitola Errada, Bitola 1000, Vel 100
            w.write("103\tDieselGauge\tM\tM\t2000\t4\tT\t3k\t20\t3\t4\t100\t160\t100\t300\tDiesel\t\t\tCP\t1000\t5000\n");
        }

        // Segments
        File segFile = folder.newFile("segments.csv");
        try (FileWriter w = new FileWriter(segFile)) {
            w.write("ID\tLine\tOrder\tElectrified\tMaxWeight\tLength\tTracks\n");
            w.write("1001\t10\t1\tYes\t5000\t10000\t2\n");  // Linha 10, Seg 1, 10km, Elet.
            w.write("1002\t10\t2\tNo\t5000\t15000\t1\n");  // Linha 10, Seg 2, 15km, Nao Elet.
            w.write("2001\t20\t1\tYes\t5000\t5000\t1\n");   // Linha 20, Seg 1
        }

        // 3. Carregar Repositórios
        facilityRepo.loadFromCSV(facFile.getAbsolutePath());
        lineRepo.loadFromCSV(lineFile.getAbsolutePath()); // Assumindo que isto existe
        locomotiveRepo.loadFromCSV(locoFile.getAbsolutePath());
        segmentRepo.loadFromCSV(segFile.getAbsolutePath());

        // 4. Criar Serviço
        service = new TravelTimeService(facilityRepo, lineRepo, segmentRepo, locomotiveRepo);
    }

    @Test
    public void testCalculateTravelTime_HappyPath_Diesel() {
        // Viagem: A(1) -> B(2) com Locomotiva Diesel (101)
        // Linha 10 (1435) -> 2 segmentos (10km + 15km = 25km)
        // Velocidade da Locomotiva: 100 km/h

        TravelTimeResult result = service.calculateTravelTime(1, 2, 101);

        assertNotNull(result);
        assertEquals("Station A", result.getOriginFacilityName());
        assertEquals("Station B", result.getDestinationFacilityName());
        assertEquals(101, result.getLocomotiveNumber());
        assertEquals(2, result.getSegments().size());

        // Distância Total: 10000m + 15000m = 25000m = 25km
        assertEquals(25.0, result.getTotalDistanceKilometers(), 0.001);

        // Tempo Total: Dist (km) / Vel (km/h) = 25 / 100 = 0.25 horas
        // 0.25 horas * 3600 s/h = 900 segundos
        assertEquals(900.0, result.getTotalTimeSeconds(), 0.001);
        assertEquals(100.0, result.getAverageSpeedKmh(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateTravelTime_Fail_ElectricLoco_NotElectrified() {
        // Viagem: A(1) -> B(2) com Locomotiva Elétrica (102)
        // Linha 10 tem o segmento 1002 que não é eletrificado.
        service.calculateTravelTime(1, 2, 102);
        // Espera-se "Electric locomotive requires fully electrified line"
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateTravelTime_Fail_GaugeMismatch() {
        // Viagem: A(1) -> B(2) com Locomotiva de Bitola Errada (103)
        // Linha 10 (1435mm) vs Locomotiva 103 (1000mm)
        service.calculateTravelTime(1, 2, 103);
        // Espera-se "incompatible with line gauge"
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateTravelTime_Fail_NoDirectLine() {
        // Viagem: A(1) -> C(3)
        // Não há linha direta
        service.calculateTravelTime(1, 3, 101);
        // Espera-se "No direct line found"
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateTravelTime_Fail_InvalidFacility() {
        service.calculateTravelTime(999, 1, 101);
        // Espera-se "Origin facility not found"
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateTravelTime_Fail_InvalidLocomotive() {
        service.calculateTravelTime(1, 2, 999);
        // Espera-se "Locomotive not found"
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateTravelTime_Fail_SameFacility() {
        service.calculateTravelTime(1, 1, 101);
        // Espera-se "Origin and destination must be different"
    }

    @Test
    public void testValidateJourney() {
        // Teste de sucesso
        Optional<String> valid = service.validateJourney(1, 2, 101);
        assertFalse(valid.isPresent());

        // Teste de falha (Bitola)
        Optional<String> invalidGauge = service.validateJourney(1, 2, 103);
        assertTrue(invalidGauge.isPresent());
        assertTrue(invalidGauge.get().contains("incompatible with line gauge"));

        // Teste de falha (Elétrica)
        Optional<String> invalidElectric = service.validateJourney(1, 2, 102);
        assertTrue(invalidElectric.isPresent());
        assertTrue(invalidElectric.get().contains("requires fully electrified line"));
    }
}
