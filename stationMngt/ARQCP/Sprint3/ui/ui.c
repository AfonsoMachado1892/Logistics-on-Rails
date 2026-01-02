#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "ui.h"

/**
 * USAC16 - Robust Integer Input
 * Prevents system crashes if the user enters characters instead of numbers.
 */
int get_robust_int(const char* prompt) {
    int value;
    char buffer[100];

    while (1) {
        printf("%s", prompt);
        if (fgets(buffer, sizeof(buffer), stdin)) {
            // Attempt to parse an integer from the string
            if (sscanf(buffer, "%d", &value) == 1) {
                return value;
            }
        }
        printf("[!] Invalid input. Please enter a numeric value.\n");
    }
}

/**
 * USAC16 - Robust Menu Selection
 */
char get_menu_choice() {
    char buffer[10];
    printf("\nCOMMANDS: [1]Assign [2]Free [3]Inop [4]Stopped [5]ExportLogs [6]ControlLight [0]Quit\n");
    printf("Choice > ");

    if (fgets(buffer, sizeof(buffer), stdin)) {
        return buffer[0]; // Returns the first character (e.g., '1', '2', or '\n')
    }
    return '\n';
}