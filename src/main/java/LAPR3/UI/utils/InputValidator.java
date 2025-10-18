package LAPR3.UI.utils;

import java.util.Scanner;

/**
 * Utility class for validating and reading user input
 */
public class InputValidator {

    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Read an integer within a range
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("❌ Input cannot be empty. Please try again.");
                    continue;
                }

                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.printf("❌ Value must be between %d and %d. Please try again.%n", min, max);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Read a non-empty string
     */
    public String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("❌ Input cannot be empty. Please try again.");
                continue;
            }

            return input;
        }
    }

    /**
     * Read a file path
     */
    public String readFilePath(String prompt, String defaultPath) {
        System.out.print(prompt);
        System.out.printf(" [default: %s]: ", defaultPath);
        String input = scanner.nextLine().trim();

        return input.isEmpty() ? defaultPath : input;
    }

    /**
     * Read a yes/no confirmation
     */
    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                System.out.println("❌ Please enter 'y' or 'n'.");
            }
        }
    }

    /**
     * Read a selection from a list
     */
    public int readSelection(String prompt, int optionsCount) {
        return readInt(prompt, 1, optionsCount);
    }
}