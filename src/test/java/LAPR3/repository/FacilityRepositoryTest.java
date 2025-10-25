package LAPR3.repository;

import LAPR3.model.Facility;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class FacilityRepositoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private FacilityRepository createAndLoadRepo() throws IOException {
        File file = folder.newFile("facilities.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("ID\tName\n"); // Header
            writer.write("1\tStation A\n");
            writer.write("2\tStation B\n");
            writer.write("3\tStation C\n");
            writer.write("-1\tInvalid Station\n"); // ID Inválido
            writer.write("4\t\n"); // Nome inválido (Facility não permite)
        }

        FacilityRepository repo = new FacilityRepository();
        repo.loadFromCSV(file.getAbsolutePath());
        return repo;
    }

    @Test
    public void testLoadFromCSV() throws IOException {
        FacilityRepository repo = createAndLoadRepo();

        // Deve carregar 3 válidos, ignorar 2 inválidos
        assertEquals(3, repo.count());
        assertTrue(repo.exists(1));
        assertTrue(repo.exists(2));
        assertTrue(repo.exists(3));
        assertFalse(repo.exists(-1));
        assertFalse(repo.exists(4));
    }

    @Test
    public void testFindById() throws IOException {
        FacilityRepository repo = createAndLoadRepo();

        Optional<Facility> fac1 = repo.findById(1);
        assertTrue(fac1.isPresent());
        assertEquals("Station A", fac1.get().getName());

        Optional<Facility> fac99 = repo.findById(99);
        assertFalse(fac99.isPresent());
    }

    @Test
    public void testFindByName() throws IOException {
        FacilityRepository repo = createAndLoadRepo();

        Optional<Facility> facB = repo.findByName("Station B");
        assertTrue(facB.isPresent());
        assertEquals(2, facB.get().getId());

        // Teste case-insensitive
        Optional<Facility> facC = repo.findByName("station c");
        assertTrue(facC.isPresent());
        assertEquals(3, facC.get().getId());

        Optional<Facility> facX = repo.findByName("Station X");
        assertFalse(facX.isPresent());
    }

    @Test
    public void testFindAll() throws IOException {
        FacilityRepository repo = createAndLoadRepo();
        List<Facility> all = repo.findAll();
        assertEquals(3, all.size());
    }
}
