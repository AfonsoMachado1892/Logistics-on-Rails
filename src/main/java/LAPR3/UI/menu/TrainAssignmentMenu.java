package LAPR3.UI.menu;

import LAPR3.DTO.*;
import LAPR3.repository.TrainAssignmentRepository;
import LAPR3.UI.utils.InputValidator;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TrainAssignmentMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final TrainAssignmentRepository repository;

    public TrainAssignmentMenu(Scanner scanner, InputValidator validator) {
        this.scanner = scanner;
        this.validator = validator;
        this.repository = new TrainAssignmentRepository();
    }

    public void show() {
        boolean back = false;
        while (!back) {
            displayHeader();
            System.out.println("┌───────────────────────────────────────────────────────┐");
            System.out.println("│  1. List Available Rolling Stock (Locos & Wagons)     │");
            System.out.println("│  2. List Available Routes                             │");
            System.out.println("│  3. Assemble and Assign Train (Step-by-Step)          │");
            System.out.println("├───────────────────────────────────────────────────────┤");
            System.out.println("│  0. Back to Main Menu                                 │");
            System.out.println("└───────────────────────────────────────────────────────┘");
            System.out.println();

            int choice = validator.readInt("Enter your choice: ", 0, 3);

            switch (choice) {
                case 1 -> listAvailableStock();
                case 2 -> listAvailableRoutes();
                case 3 -> assembleAndAssign();
                case 0 -> back = true;
                default -> System.out.println("Invalid option!");
            }

            if (!back) {
                System.out.println("\nPress ENTER to continue...");
                scanner.nextLine();
                scanner.nextLine();
            }
        }
    }

    private void displayHeader() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║   USLP09 - ASSEMBLE AND ASSIGN TRAIN TO ROUTE         ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }

    private void assembleAndAssign() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║   USLP09 - ASSEMBLE AND ASSIGN TRAIN TO ROUTE         ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");

        try {
            System.out.println("\n--- STEP 1: SELECT START FACILITY ---");
            listAvailableFacilities();
            int startId = validator.readInt("Enter Start Facility ID: ", 1, 9999);

            System.out.println("\n--- STEP 2: SELECT END FACILITY ---");
            listAvailableFacilities();
            int endId = validator.readInt("Enter End Facility ID: ", 1, 9999);

            System.out.println("\n--- STEP 3: SELECT LOCOMOTIVE ---");
            listAvailableStock();
            String locoId = validator.readString("Enter Locomotive ID: ");

// --- NOVO PASSO: SELECT ROUTE ---
            System.out.println("\n--- STEP 3.5: SELECT ROUTE ---");
            listAvailableRoutes();
            String routeId = validator.readString("Enter Route ID (e.g., r001): ");

            // --- STEP 4: SELECT WAGONS ---
            System.out.println("\n--- STEP 4: SELECT WAGONS ---");
            System.out.print("Enter Wagon IDs (Ex: 356 3 077, 356 3 088): ");

            String wagonsInput = "";
// Este loop limpa qualquer "Enter" pendente e espera pelo teu texto real
            while (scanner.hasNextLine()) {
                wagonsInput = scanner.nextLine().trim();
                if (!wagonsInput.isEmpty()) {
                    break; // Sai do loop assim que receber texto
                }
            }

// Criar a lista garantindo que os espaços internos (356 3 077) são mantidos
            List<String> wagonIds = Arrays.stream(wagonsInput.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            // --- STEP 5: SELECT OPERATOR ---
            System.out.println("\n--- STEP 5: SELECT OPERATOR ---");
            listAvailableOperators();
            String operatorVat = validator.readString("Enter Operator VAT: ");

            // --- STEP 6: DEPARTURE DATE ---
            System.out.println("\n--- STEP 6: DEPARTURE DATE ---");
            System.out.print("Enter Date and Time (YYYY-MM-DD HH:MM): ");

            // Nova correção de buffer para a data após o readString do operador
            String departureDateTime = scanner.nextLine();
            if (departureDateTime.isEmpty()) {
                departureDateTime = scanner.nextLine();
            }

            // Chamada ao repositório (com as 5 tabelas)
            boolean success = repository.createFreight(
                    wagonIds,
                    locoId,
                    String.valueOf(startId),
                    String.valueOf(endId),
                    departureDateTime,
                    operatorVat,
                    routeId // Novo parâmetro
            );

            if (success) {
                System.out.println("\n" + "═".repeat(60));
                System.out.println("  SUCESSO: Comboio Registado em todas as tabelas!");
                System.out.println("═".repeat(60));
            }

        } catch (SQLException e) {
            System.err.println("\n[ERRO DE BD] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n[ERRO] " + e.getMessage());
        }
    }
    private void listAvailableOperators() {
        System.out.println("\n--- REGISTERED OPERATORS ---");
        try {
            List<OperatorDTO> operators = repository.getAvailableOperators();
            if (operators.isEmpty()) {
                System.out.println("No operators found in the database.");
            } else {
                // Formatação simples para o utilizador escolher o VAT
                for (OperatorDTO op : operators) {
                    System.out.println(op);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    // Métodos auxiliares de listagem interna (privados para o fluxo de seleção)
    private void listAvailableFacilities() {
        try {
            List<FacilityDTO> facilities = repository.getAvailableFacilities();
            if (facilities.isEmpty()) System.out.println("No facilities found.");
            else facilities.forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    private void listAvailableStock() {
        try {
            List<RollingStockItem> stock = repository.getAvailableRollingStock();
            if (stock.isEmpty()) System.out.println("No rolling stock found.");
            else stock.forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    private void listAvailableRoutes() {
        try {
            List<RouteDto> routes = repository.getAvailableRoutes();
            if (routes.isEmpty()) System.out.println("No routes found.");
            else routes.forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }
}