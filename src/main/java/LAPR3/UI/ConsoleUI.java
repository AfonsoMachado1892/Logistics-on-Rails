package LAPR3.UI;

import LAPR3.controller.DataController;
import LAPR3.UI.menu.MainMenu;
import LAPR3.UI.utils.InputValidator;

import java.util.Scanner;

/**
 * Main console user interface for the Cargo Handling System
 * Implements USLP02 - User-friendly text-based interface
 */
public class ConsoleUI {

    private final Scanner scanner;
    private final DataController dataController;
    private final MainMenu mainMenu;
    private final InputValidator validator;
    private boolean running;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.dataController = new DataController();
        this.validator = new InputValidator(scanner);
        this.mainMenu = new MainMenu(scanner, dataController, validator);
        this.running = true;
    }

    /**
     * Main execution loop
     */
    public void run() {
        displayWelcomeMessage();

        while (running) {
            try {
                mainMenu.display();
                int choice = validator.readInt("Enter your choice: ", 0, 5);

                if (choice == 0) {
                    if (confirmExit()) {
                        running = false;
                    }
                } else {
                    mainMenu.handleChoice(choice);
                }

                if (running) {
                    pauseForUser();
                }

            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                System.out.println("Please try again.");
                pauseForUser();
            }
        }

        scanner.close();
    }

    private void displayWelcomeMessage() {
        System.out.println("Welcome, Station Storage Manager!");
        System.out.println("This system allows you to:");
        System.out.println("  • Import and manage warehouse data");
        System.out.println("  • Process wagon unloading operations");
        System.out.println("  • Manage order eligibility and allocation");
        System.out.println();
    }

    private boolean confirmExit() {
        System.out.print("\n⚠️  Are you sure you want to exit? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    private void pauseForUser() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
        clearScreen();
    }

    private void clearScreen() {
        // Print blank lines for cleaner display
        for (int i = 0; i < 2; i++) {
            System.out.println();
        }
    }
}