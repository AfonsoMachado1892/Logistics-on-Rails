#ifndef UI_H
#define UI_H

/**
 * USAC16 / Sprint 3 - UI Component
 * Provides robust input handling to prevent system rejection of instructions.
 */

// Prompts user for an integer and validates it's not a character
int get_robust_int(const char* prompt);

// Displays the main menu and returns the selected character
char get_menu_choice();

#endif