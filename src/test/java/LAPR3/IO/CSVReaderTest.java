package LAPR3.IO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CSVReaderTest {

    // Cria uma pasta temporária para os testes de ficheiros
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testParseInt() {
        assertEquals(123, CSVReader.parseInt("123", -1));
        assertEquals(123, CSVReader.parseInt(" 123 ", -1));
        assertEquals(-1, CSVReader.parseInt("abc", -1));
        assertEquals(-1, CSVReader.parseInt(null, -1));
        assertEquals(-1, CSVReader.parseInt("", -1));
        assertEquals(0, CSVReader.parseInt("0", -1));
    }

    @Test
    public void testParseDouble() {
        assertEquals(123.45, CSVReader.parseDouble("123.45", -1.0), 0.001);
        assertEquals(123.45, CSVReader.parseDouble(" 123.45 ", -1.0), 0.001);
        assertEquals(-1.0, CSVReader.parseDouble("abc", -1.0), 0.001);
        assertEquals(-1.0, CSVReader.parseDouble(null, -1.0), 0.001);
        assertEquals(-1.0, CSVReader.parseDouble("", -1.0), 0.001);
    }

    @Test
    public void testParseBoolean() {
        assertTrue(CSVReader.parseBoolean("Yes"));
        assertTrue(CSVReader.parseBoolean("yes"));
        assertTrue(CSVReader.parseBoolean(" YES "));
        assertTrue(CSVReader.parseBoolean("true"));
        assertTrue(CSVReader.parseBoolean("1"));

        assertFalse(CSVReader.parseBoolean("No"));
        assertFalse(CSVReader.parseBoolean("no"));
        assertFalse(CSVReader.parseBoolean("false"));
        assertFalse(CSVReader.parseBoolean("0"));
        assertFalse(CSVReader.parseBoolean("abc"));
        assertFalse(CSVReader.parseBoolean(null));
        assertFalse(CSVReader.parseBoolean(""));
    }

    @Test
    public void testParseLine() throws Exception {
        // Usa reflexão para testar o método privado
        java.lang.reflect.Method method = CSVReader.class.getDeclaredMethod("parseLine", String.class);
        method.setAccessible(true);

        // Teste 1: Linha simples (separada por tabs)
        String line1 = "A\tB\tC";
        String[] result1 = (String[]) method.invoke(null, line1);
        assertArrayEquals(new String[]{"A", "B", "C"}, result1);

        // Teste 2: Linha com aspas (tabs dentro das aspas são ignorados)
        String line2 = "A\t\"B\tB2\"\tC";
        String[] result2 = (String[]) method.invoke(null, line2);
        assertArrayEquals(new String[]{"A", "B\tB2", "C"}, result2);

        // Teste 3: Linha com espaços (devem ser 'trimmed')
        String line3 = " A \t B \tC";
        String[] result3 = (String[]) method.invoke(null, line3);
        assertArrayEquals(new String[]{"A", "B", "C"}, result3);
    }

    @Test
    public void testReadCSV() throws IOException {
        // Arrange
        File testFile = folder.newFile("test.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Header1\tHeader2\tHeader3\n"); // Linha de cabeçalho
            writer.write("A\tB\tC\n");                   // Linha válida 1
            writer.write("\n");                          // Linha vazia (deve ser ignorada)
            writer.write("D\t\"E\tF\"\tG\n");            // Linha válida 2
        }

        // Act
        List<String[]> records = CSVReader.readCSV(testFile.getAbsolutePath());

        // Assert
        assertNotNull(records);
        assertEquals(2, records.size()); // Deve ignorar cabeçalho e linha vazia

        // Verificar Linha 1
        assertArrayEquals(new String[]{"A", "B", "C"}, records.get(0));
        // Verificar Linha 2
        assertArrayEquals(new String[]{"D", "E\tF", "G"}, records.get(1));
    }

    @Test(expected = IOException.class)
    public void testReadCSV_FileNotFound() throws IOException {
        CSVReader.readCSV("caminho/inexistente.csv");
    }
}
