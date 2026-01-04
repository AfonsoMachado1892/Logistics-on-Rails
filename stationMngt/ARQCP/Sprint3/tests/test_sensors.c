// Updated test_sensors.c (added one more test for extract invalid value)
#include "unity.h"
#include <string.h>

extern int extract_data(char* str, char* token, char* unit, int* value);
extern int format_command(char* op, int n, char *cmd);

void setUp(void) {}
void tearDown(void) {}

// --- Testes EXTRACT DATA ---
void test_ExtractData_ValidString(void) {
    char input[] = "AAA#TEMP&unit:C&value:2500&ZZZ";
    char token[] = "TEMP";
    char unit[10] = {0};
    int value = 0;

    int res = extract_data(input, token, unit, &value);

    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_STRING("C", unit);
    TEST_ASSERT_EQUAL_INT(2500, value);
}

void test_ExtractData_TokenNotFound(void) {
    char input[] = "#HUM&unit:%&value:50&";
    char token[] = "TEMP"; // Não existe
    char unit[10];
    int value;

    int res = extract_data(input, token, unit, &value);
    TEST_ASSERT_EQUAL_INT(0, res);
}

void test_ExtractData_InvalidValue(void) {
    char input[] = "TEMP&unit:C&value:abc&";
    char token[] = "TEMP";
    char unit[10];
    int value;

    int res = extract_data(input, token, unit, &value);
    TEST_ASSERT_EQUAL_INT(0, res);
}

// --- Testes FORMAT COMMAND ---
void test_FormatCommand_TwoParams(void) {
    char buffer[20];
    // "RE" com N=10 deve dar "RE,10"
    int res = format_command("RE", 10, buffer);

    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_STRING("RE,10", buffer);
}

void test_FormatCommand_OneParam(void) {
    char buffer[20];
    // "GTH" deve ignorar o N e dar apenas "GTH"
    int res = format_command("GTH", 99, buffer);

    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_STRING("GTH", buffer);
}

void test_FormatCommand_InvalidRange(void) {
    char buffer[20];
    // N > 99 deve falhar
    int res = format_command("RE", 100, buffer);

    TEST_ASSERT_EQUAL_INT(0, res);
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_ExtractData_ValidString);
    RUN_TEST(test_ExtractData_TokenNotFound);
    RUN_TEST(test_ExtractData_InvalidValue);
    RUN_TEST(test_FormatCommand_TwoParams);
    RUN_TEST(test_FormatCommand_OneParam);
    RUN_TEST(test_FormatCommand_InvalidRange);
    return UNITY_END();
}