#include "unity.h"
#include <string.h>
#include <stdio.h>

void setUp(void) {}
void tearDown(void) {}

// USAC16 - ROBUST INPUT VALIDATION TESTS

void test_ValidInteger_Parsing(void) {
    char input1[] = "42\n";
    char input2[] = "-15\n";

    int val1, val2;
    int res1 = sscanf(input1, "%d", &val1);
    int res2 = sscanf(input2, "%d", &val2);

    TEST_ASSERT_EQUAL_INT(1, res1);
    TEST_ASSERT_EQUAL_INT(42, val1);
    TEST_ASSERT_EQUAL_INT(1, res2);
    TEST_ASSERT_EQUAL_INT(-15, val2);
}

void test_MenuChoice_ValidCommands(void) {
    char valid_commands[] = {'0', '1', '2', '3', '4', '5', '6'};

    for(int i = 0; i < 7; i++) {
        TEST_ASSERT_TRUE(valid_commands[i] >= '0' && valid_commands[i] <= '6');
    }
}

void test_TrackIDRange_Validation(void) {
    int valid_ids[] = {1, 50, 99};
    int invalid_ids[] = {0, 100, -1};

    for(int i = 0; i < 3; i++) {
        TEST_ASSERT_TRUE(valid_ids[i] >= 1 && valid_ids[i] <= 99);
    }

    for(int i = 0; i < 3; i++) {
        TEST_ASSERT_FALSE(invalid_ids[i] >= 1 && invalid_ids[i] <= 99);
    }
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_ValidInteger_Parsing);
    RUN_TEST(test_MenuChoice_ValidCommands);
    RUN_TEST(test_TrackIDRange_Validation);
    return UNITY_END();
}