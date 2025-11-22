#include <string.h>  
#include "../unity.h"
#include "dequeue_value.h"

// Declaração do callfunc com 7 parâmetros (função + 6 args)
int callfunc(int (*f)(int*, int, int*, int*, int*, int*), 
             int* buffer, int length, int* nelem, int* tail, int* head, int* value);

void setUp(void) {
    // set stuff up here
}

void tearDown(void) {
    // clean stuff up here
}

void run_test(int* initial_buffer, int length, int initial_nelem, 
              int initial_tail, int initial_head, 
              int exp_res, int exp_value, int exp_nelem, int exp_tail)
{
    int buffer[100];
    int nelem = initial_nelem;
    int tail = initial_tail;
    int head = initial_head;
    int value = -999; // valor sentinela
    
    // Copiar buffer inicial
    memcpy(buffer, initial_buffer, length * sizeof(int));
    
    int res = callfunc(dequeue_value, buffer, length, &nelem, &tail, &head, &value);
    
    TEST_ASSERT_EQUAL_INT(exp_res, res);
    
    if (exp_res == 1) {
        TEST_ASSERT_EQUAL_INT(exp_value, value);
        TEST_ASSERT_EQUAL_INT(exp_nelem, nelem);
        TEST_ASSERT_EQUAL_INT(exp_tail, tail);
    } else {
        // Em caso de falha, value não deve ser alterado
        TEST_ASSERT_EQUAL_INT(-999, value);
    }
}

// Testes de sucesso
void test_DequeueFromSimpleBuffer()
{
    int buffer[] = {10, 20, 30, 0, 0};
    run_test(buffer, 5, 3, 0, 3, 1, 10, 2, 1);
}

void test_DequeueWithWrapAround()
{
    int buffer[] = {50, 60, 70, 40};
    run_test(buffer, 4, 4, 3, 3, 1, 40, 3, 0);
}

void test_DequeueSingleElement()
{
    int buffer[] = {100, 0, 0};
    run_test(buffer, 3, 1, 0, 1, 1, 100, 0, 1);
}

void test_DequeueMultipleTimes()
{
    int buffer[] = {5, 15, 25, 35};
    int length = 4;
    int nelem = 4;
    int tail = 0;
    int head = 0;
    int value;
    int res;
    
    // Primeira remoção
    res = callfunc(dequeue_value, buffer, length, &nelem, &tail, &head, &value);
    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_INT(5, value);
    TEST_ASSERT_EQUAL_INT(3, nelem);
    TEST_ASSERT_EQUAL_INT(1, tail);
    
    // Segunda remoção
    res = callfunc(dequeue_value, buffer, length, &nelem, &tail, &head, &value);
    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_INT(15, value);
    TEST_ASSERT_EQUAL_INT(2, nelem);
    TEST_ASSERT_EQUAL_INT(2, tail);
}

void test_DequeueFromLargeBuffer()
{
    int buffer[20];
    for (int i = 0; i < 20; i++) {
        buffer[i] = i * 10;
    }
    run_test(buffer, 20, 20, 0, 0, 1, 0, 19, 1);
}

void test_DequeueTailAtMiddle()
{
    int buffer[] = {100, 200, 300, 400, 500};
    run_test(buffer, 5, 2, 2, 4, 1, 300, 1, 3);
}

void test_DequeueTailAtEnd()
{
    int buffer[] = {10, 20, 30};
    run_test(buffer, 3, 1, 2, 0, 1, 30, 0, 0);
}

void test_DequeueNegativeValues()
{
    int buffer[] = {-10, -20, -30, -40};
    run_test(buffer, 4, 3, 0, 3, 1, -10, 2, 1);
}

void test_DequeueZeroValue()
{
    int buffer[] = {0, 5, 10};
    run_test(buffer, 3, 2, 0, 2, 1, 0, 1, 1);
}

// Testes de falha
void test_DequeueFromEmptyBuffer()
{
    int buffer[] = {0, 0, 0};
    run_test(buffer, 3, 0, 0, 0, 0, -999, 0, 0);
}

void test_DequeueFromEmptyBuffer_DifferentTailHead()
{
    int buffer[] = {0, 0, 0, 0, 0};
    run_test(buffer, 5, 0, 2, 2, 0, -999, 0, 2);
}

// Teste: Tail no final com wrap-around para posição 0
void test_DequeueWrapToZero()
{
    int buffer[] = {100, 200, 300, 400};
    // length=4, nelem=1, tail=3, head=0
    // Remove 400, tail wrap para 0
    run_test(buffer, 4, 1, 3, 0, 1, 400, 0, 0);
}

// Teste: Buffer com length=1
void test_DequeueSizeOne()
{
    int buffer[] = {999};
    // length=1, nelem=1, tail=0, head=0
    run_test(buffer, 1, 1, 0, 0, 1, 999, 0, 0);
}

// Teste: Valores muito grandes
void test_DequeueLargeValues()
{
    int buffer[] = {2147483647, -2147483648, 0, 1};
    // INT_MAX e INT_MIN
    run_test(buffer, 4, 2, 0, 2, 1, 2147483647, 1, 1);
}

int main()
{
    UNITY_BEGIN();
    
    // Success tests
    RUN_TEST(test_DequeueFromSimpleBuffer);
    RUN_TEST(test_DequeueWithWrapAround);
    RUN_TEST(test_DequeueSingleElement);
    RUN_TEST(test_DequeueMultipleTimes);
    RUN_TEST(test_DequeueFromLargeBuffer);
    RUN_TEST(test_DequeueTailAtMiddle);
    RUN_TEST(test_DequeueTailAtEnd);
    RUN_TEST(test_DequeueNegativeValues);
    RUN_TEST(test_DequeueZeroValue);
    RUN_TEST(test_DequeueWrapToZero);        
    RUN_TEST(test_DequeueSizeOne);           
    RUN_TEST(test_DequeueLargeValues);       
    
    // Failure tests
    RUN_TEST(test_DequeueFromEmptyBuffer);
    RUN_TEST(test_DequeueFromEmptyBuffer_DifferentTailHead);
    
    return UNITY_END();  
}