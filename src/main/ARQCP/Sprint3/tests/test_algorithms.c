#include "unity.h"
#include <stdlib.h>

// Declarações das funções Assembly
extern int sort_array(int* vec, int length, char order);
extern int median(int* vec, int length, int *me);

void setUp(void) {}
void tearDown(void) {}

// --- Testes SORT_ARRAY ---
void test_SortArray_Ascending(void) {
    int vec[] = {5, 1, 4, 2, 8};
    int expected[] = {1, 2, 4, 5, 8};
    int res = sort_array(vec, 5, 1); // 1 = Ascendente

    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_INT_ARRAY(expected, vec, 5);
}

void test_SortArray_Descending(void) {
    int vec[] = {1, 3, 2};
    int expected[] = {3, 2, 1};
    int res = sort_array(vec, 3, 0); // 0 = Descendente

    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_INT_ARRAY(expected, vec, 3);
}

// --- Testes MEDIAN ---
void test_Median_OddLength(void) {
    int vec[] = {10, 2, 30, 5, 1}; // Não ordenado
    // Ordenado seria: 1, 2, 5, 10, 30. Mediana = 5.
    int m = 0;

    int res = median(vec, 5, &m);

    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_INT(5, m);
    // Verifica se o array ficou ordenado (side-effect da tua função median)
    TEST_ASSERT_EQUAL_INT(1, vec[0]);
}

void test_Median_EvenLength(void) {
    int vec[] = {4, 1, 3, 2};
    // Ordenado: 1, 2, 3, 4. Mediana (int) = (2+3)/2 = 2.
    int m = 0;

    median(vec, 4, &m);
    TEST_ASSERT_EQUAL_INT(2, m);
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_SortArray_Ascending);
    RUN_TEST(test_SortArray_Descending);
    RUN_TEST(test_Median_OddLength);
    RUN_TEST(test_Median_EvenLength);
    return UNITY_END();
}