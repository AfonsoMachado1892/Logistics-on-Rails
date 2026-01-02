#include "unity.h"
#include <string.h>

// Assinaturas baseadas na tua análise dos ficheiros .s
// enqueue: a3=rd_ptr (head/read), a4=wr_ptr (tail/write)
extern int enqueue_value(int* buffer, int length, int* nelem, int* rd_ptr, int* wr_ptr, int value);
// dequeue: a3=tail (read pointer na tua logica), a4=head (ignored/write?)
extern int dequeue_value(int* buffer, int length, int* nelem, int* rd_ptr, int* ignored, int* value);
// move_n: a3=tail (read ptr)
extern int move_n_to_array(int* buffer, int length, int* nelem, int* rd_ptr, int ignored, int n, int* array_out);

#define BUF_LEN 5
int buffer[BUF_LEN];
int nelem = 0;
int read_ptr = 0;  // Onde lemos (oldest)
int write_ptr = 0; // Onde escrevemos (newest)

void setUp(void) {
    memset(buffer, 0, sizeof(buffer));
    nelem = 0;
    read_ptr = 0;
    write_ptr = 0;
}
void tearDown(void) {}

void test_Enqueue_Should_AddValue(void) {
    enqueue_value(buffer, BUF_LEN, &nelem, &read_ptr, &write_ptr, 10);

    TEST_ASSERT_EQUAL_INT(1, nelem);
    TEST_ASSERT_EQUAL_INT(10, buffer[0]);
    TEST_ASSERT_EQUAL_INT(1, write_ptr); // Avançou
}

void test_Enqueue_Full_Should_OverwriteAndMoveReadPtr(void) {
    // Encher buffer
    for(int i=0; i<BUF_LEN; i++)
        enqueue_value(buffer, BUF_LEN, &nelem, &read_ptr, &write_ptr, i+1);

    // Buffer cheio: [1, 2, 3, 4, 5]. nelem=5, wr=0 (wrap), rd=0

    // Adicionar 6 (deve sobrescrever o 1)
    int res = enqueue_value(buffer, BUF_LEN, &nelem, &read_ptr, &write_ptr, 6);

    TEST_ASSERT_EQUAL_INT(1, res); // Retorna 1 se estava cheio/ficou cheio
    TEST_ASSERT_EQUAL_INT(5, nelem); // Mantém-se 5
    TEST_ASSERT_EQUAL_INT(6, buffer[0]); // Sobrescreveu índice 0
    TEST_ASSERT_EQUAL_INT(1, read_ptr); // Read ptr foi "empurrado" para frente
}

void test_Dequeue_Should_RemoveOldest(void) {
    enqueue_value(buffer, BUF_LEN, &nelem, &read_ptr, &write_ptr, 10);
    enqueue_value(buffer, BUF_LEN, &nelem, &read_ptr, &write_ptr, 20);

    int val = 0;
    dequeue_value(buffer, BUF_LEN, &nelem, &read_ptr, NULL, &val);

    TEST_ASSERT_EQUAL_INT(10, val); // O mais antigo
    TEST_ASSERT_EQUAL_INT(1, nelem);
    TEST_ASSERT_EQUAL_INT(1, read_ptr); // Avançou
}

void test_MoveNToArray_Should_ExtractMultiple(void) {
    enqueue_value(buffer, BUF_LEN, &nelem, &read_ptr, &write_ptr, 100);
    enqueue_value(buffer, BUF_LEN, &nelem, &read_ptr, &write_ptr, 200);
    enqueue_value(buffer, BUF_LEN, &nelem, &read_ptr, &write_ptr, 300);

    int out_array[3] = {0};
    // Move os 2 mais antigos
    move_n_to_array(buffer, BUF_LEN, &nelem, &read_ptr, 0, 2, out_array);

    TEST_ASSERT_EQUAL_INT(100, out_array[0]);
    TEST_ASSERT_EQUAL_INT(200, out_array[1]);
    TEST_ASSERT_EQUAL_INT(1, nelem); // Sobrou 1 (o 300)
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_Enqueue_Should_AddValue);
    RUN_TEST(test_Enqueue_Full_Should_OverwriteAndMoveReadPtr);
    RUN_TEST(test_Dequeue_Should_RemoveOldest);
    RUN_TEST(test_MoveNToArray_Should_ExtractMultiple);
    return UNITY_END();
}