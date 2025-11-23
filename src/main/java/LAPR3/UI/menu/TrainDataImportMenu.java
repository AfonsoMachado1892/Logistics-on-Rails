package LAPR3.UI.menu;

import LAPR3.UI.utils.InputValidator;
import LAPR3.repository.*;

import java.io.File;
import java.util.Scanner;

public class TrainDataImportMenu {
    private final Scanner scanner;
    private final InputValidator validator;
    private final FacilityRepository facilityRepo;
    private final LineRepository lineRepo;
    private final SegmentRepository segmentRepo;
    private final LocomotiveRepository locomotiveRepo;
    private final WagonRepository wagonRepo;
    private final TrainRepository trainRepo;
    private final RouteRepository routeRepo;
    private final ScheduleRepository scheduleRepo;

    // ⚡ ATUALIZAÇÃO: Caminho da pasta alterado conforme pedido
    private static final String DATA_DIR = "Dataset/";

    public TrainDataImportMenu(Scanner scanner, InputValidator validator,
                               FacilityRepository facilityRepo,
                               LineRepository lineRepo,
                               SegmentRepository segmentRepo,
                               LocomotiveRepository locomotiveRepo,
                               WagonRepository wagonRepo,
                               TrainRepository trainRepo,
                               RouteRepository routeRepo,
                               ScheduleRepository scheduleRepo) {
        this.scanner = scanner;
        this.validator = validator;
        this.facilityRepo = facilityRepo;
        this.lineRepo = lineRepo;
        this.segmentRepo = segmentRepo;
        this.locomotiveRepo = locomotiveRepo;
        this.wagonRepo = wagonRepo;
        this.trainRepo = trainRepo;
        this.routeRepo = routeRepo;
        this.scheduleRepo = scheduleRepo;
    }

    public void show() {
        boolean running = true;

        while (running) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 10);

            switch (choice) {
                case 1 -> importAllData(); // Esta é a opção que será usada no workflow
                case 2 -> importFacilities();
                case 3 -> importLines();
                case 4 -> importSegments();
                case 5 -> importLocomotives();
                case 6 -> importWagons();
                case 7 -> importTrains();
                case 8 -> importRoutes();
                case 9 -> importSchedules();
                case 10 -> showDataStatus();
                case 0 -> running = false;
            }

            if (running && choice != 0) {
                pauseForUser();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│  TRAIN DATA IMPORT (" + DATA_DIR + ")                  │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.println();

        showDataStatus();

        System.out.println("\n  1. Import ALL Data              ");
        System.out.println("  ─────────────────────────────────────");
        System.out.println("  2. Import Facilities");
        System.out.println("  3. Import Lines");
        System.out.println("  4. Import Segments");
        System.out.println("  5. Import Locomotives");
        System.out.println("  6. Import Wagons");
        System.out.println("  7. Import Trains");
        System.out.println("  8. Import Routes");
        System.out.println("  9. Import Schedules");
        System.out.println("  ─────────────────────────────────────");
        System.out.println("  10. Show Data Status");
        System.out.println("  0. Back to Freight Manager Menu");
        System.out.println();
    }

    /**
     * Imports all data in correct order using a user-specified path
     */
    private void importAllData() {
        System.out.println("\n╔══ IMPORT ALL DATA ══╗");

        // TODO: Suggestion path to be filled here
        System.out.println("Suggested path: [C:\\Users\\Migas\\Documents\\GitHub\\sem3-pi-25-26-g092-repo\\sem3-pi-25-26-g092-repo\\Dataset]");

        System.out.print("Enter data folder path (default: " + DATA_DIR + "): ");
        String dataPath = scanner.nextLine().trim();

        // Use default if empty, otherwise ensure path ends with /
        if (dataPath.isEmpty()) {
            dataPath = DATA_DIR;
        } else if (!dataPath.endsWith("/") && !dataPath.endsWith("\\")) {
            dataPath += "/";
        }

        System.out.println("Using folder: " + dataPath);
        System.out.println("Warning: This will clear existing data and reload from CSVs.");

        System.out.print("\nContinue? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("Import cancelled.");
            return;
        }

        System.out.println("\n⏳ Starting data import...\n");

        boolean success = true;
        int totalLoaded = 0;

        // Ordem estrita de importação devido às dependências (foreign keys logicas)

        // 1. Infraestrutura
        if (loadFile("facilities.csv", this::importFacilitiesFromFile, dataPath)) totalLoaded += facilityRepo.count();
        else success = false;

        if (loadFile("lines.csv", this::importLinesFromFile, dataPath)) totalLoaded += lineRepo.count();
        else success = false;

        if (loadFile("segments.csv", this::importSegmentsFromFile, dataPath)) totalLoaded += segmentRepo.count();
        else success = false;

        // 2. Material Circulante
        if (loadFile("locomotives.csv", this::importLocomotivesFromFile, dataPath)) totalLoaded += locomotiveRepo.count();
        else success = false;

        if (loadFile("wagons.csv", this::importWagonsFromFile, dataPath)) totalLoaded += wagonRepo.count();
        else success = false;

        // 3. Comboios (Depende de Loco + Wagon)
        if (loadFile("trains.csv", this::importTrainsFromFile, dataPath)) totalLoaded += trainRepo.count();
        else success = false;

        // 4. Rotas (Depende de Facilities)
        if (loadFile("routes.csv", this::importRoutesFromFile, dataPath)) totalLoaded += routeRepo.count();
        else success = false;

        // 5. Horários (Depende de Train + Route)
        if (loadFile("schedules.csv", this::importSchedulesFromFile, dataPath)) totalLoaded += scheduleRepo.count();
        else success = false;

        System.out.println("\n═══════════════════════════════════════");
        if (success) {
            System.out.println("✓ ALL DATA IMPORTED SUCCESSFULLY!");
            System.out.println("Total records loaded: " + totalLoaded);
        } else {
            System.out.println("⚠️  IMPORT COMPLETED WITH WARNINGS");
            System.out.println("Ensure files exist in: " + dataPath);
        }
        System.out.println("═══════════════════════════════════════");
    }

    // Helper para carregar ficheiros usando o caminho especificado
    private boolean loadFile(String filename, FileImporter importer, String basePath) {
        String filepath = basePath + filename;
        File file = new File(filepath);

        if (!file.exists()) {
            System.out.println("✗ File not found: " + filepath);
            return false;
        }
        return importer.importFile(filepath);
    }

    @FunctionalInterface
    private interface FileImporter {
        boolean importFile(String filepath);
    }

    // --- Métodos individuais de importação (wrappers) ---

    private void importFacilities() {
        String filepath = getFilePath("facilities.csv");
        importFacilitiesFromFile(filepath);
    }

    private boolean importFacilitiesFromFile(String filepath) {
        try {
            facilityRepo.loadFromCSV(filepath);
            System.out.println("✓ Facilities: " + facilityRepo.count());
            return true;
        } catch (Exception e) {
            System.err.println("✗ Error (Facilities): " + e.getMessage());
            return false;
        }
    }

    // ... Repete o padrão para os outros métodos, mas certifique-se de que
    // o loadFile() usa o importXFromFile correto.
    // Vou incluir apenas os métodos auxiliares essenciais para compilar o importAllData:

    private boolean importLinesFromFile(String filepath) {
        try { lineRepo.loadFromCSV(filepath); System.out.println("✓ Lines: " + lineRepo.count()); return true; }
        catch (Exception e) { System.err.println("✗ Error (Lines): " + e.getMessage()); return false; }
    }
    private boolean importSegmentsFromFile(String filepath) {
        try { segmentRepo.loadFromCSV(filepath); System.out.println("✓ Segments: " + segmentRepo.count()); return true; }
        catch (Exception e) { System.err.println("✗ Error (Segments): " + e.getMessage()); return false; }
    }
    private boolean importLocomotivesFromFile(String filepath) {
        try { locomotiveRepo.loadFromCSV(filepath); System.out.println("✓ Locomotives: " + locomotiveRepo.count()); return true; }
        catch (Exception e) { System.err.println("✗ Error (Locomotives): " + e.getMessage()); return false; }
    }
    private boolean importWagonsFromFile(String filepath) {
        try { wagonRepo.loadFromCSV(filepath); System.out.println("✓ Wagons: " + wagonRepo.count()); return true; }
        catch (Exception e) { System.err.println("✗ Error (Wagons): " + e.getMessage()); return false; }
    }
    private boolean importTrainsFromFile(String filepath) {
        try { trainRepo.loadFromCSV(filepath); System.out.println("✓ Trains: " + trainRepo.count()); return true; }
        catch (Exception e) { System.err.println("✗ Error (Trains): " + e.getMessage()); return false; }
    }
    private boolean importRoutesFromFile(String filepath) {
        try { routeRepo.loadFromCSV(filepath); System.out.println("✓ Routes: " + routeRepo.count()); return true; }
        catch (Exception e) { System.err.println("✗ Error (Routes): " + e.getMessage()); return false; }
    }
    private boolean importSchedulesFromFile(String filepath) {
        try { scheduleRepo.loadFromCSV(filepath); System.out.println("✓ Schedules: " + scheduleRepo.count()); return true; }
        catch (Exception e) { System.err.println("✗ Error (Schedules): " + e.getMessage()); return false; }
    }

    // Métodos "dummy" para o menu individual (opções 2-9) funcionarem se o utilizador quiser usar outro path
    private void importLines() { importLinesFromFile(getFilePath("lines.csv")); }
    private void importSegments() { importSegmentsFromFile(getFilePath("segments.csv")); }
    private void importLocomotives() { importLocomotivesFromFile(getFilePath("locomotives.csv")); }
    private void importWagons() { importWagonsFromFile(getFilePath("wagons.csv")); }
    private void importTrains() { importTrainsFromFile(getFilePath("trains.csv")); }
    private void importRoutes() { importRoutesFromFile(getFilePath("routes.csv")); }
    private void importSchedules() { importSchedulesFromFile(getFilePath("schedules.csv")); }

    private void showDataStatus() {
        System.out.println("Status: F:" + facilityRepo.count() + " | L:" + lineRepo.count() + " | S:" + segmentRepo.count() + " | T:" + trainRepo.count());
    }

    private String getFilePath(String defaultFilename) {
        System.out.print("Enter file path (default: " + DATA_DIR + defaultFilename + "): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? DATA_DIR + defaultFilename : input;
    }

    private void pauseForUser() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}