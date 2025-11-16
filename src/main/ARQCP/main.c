#include <stdio.h>
#include "DECRYPT_DATA.H" 
#include <string.h>
#include "SORT_ARRAY.H"
#include "REMOVE_OLDEST.H"
#include "ENCRYPT_DATA.H"
#include "EXTRACT_DATA.H"
#include "DEQUEUE_VALUE.H"
#INCLUDE "ENQUEUE_VALUE.H"
#include "format_command.h"
#include "median.h"

int main(void) {
	
	 // USAC01
    char plaintext[] = "HELLO";
    char encrypted[100];
    int key1 = 3;
    int result1;
    result1 = encrypt_data(plaintext, key1, encrypted);
    if (result1 == 1) {
        printf("Original: %s\n", plaintext);
        printf("Encrypted: %s\n", encrypted);
    } else {
        printf("Encryption failed.\n");
    }
    printf("\n");

    // USAC02
    char input[] = "KHOOR";   // "HELLO" encrypted with key = 3
    char output[100];
    int key = 3;
    int result;

    result = decrypt_data(input, key, output);

    if (result == 1) {
        printf("Decrypted string: %s\n", output);
    } else {
        printf("Decryption failed.\n");
    }
    printf("\n");
    // USAC05
    extern int enqueue_value(int* buffer, int length, int nelem, int tail, int* head, int value);

    void print_buffer(int* buffer, int length, int head, int tail, int nelem) {
        printf("Buffer: [");
        for (int i = 0; i < length; i++) {
            if (i > 0) printf(", ");
            printf("%d", buffer[i]);
        }
        printf("]\n");
        printf("Head: %d, Tail: %d, Nelem: %d\n\n", head, tail, nelem);
    }


        int length = 5;
        int* buffer = (int*)calloc(length, sizeof(int));
        int head = 0;
        int tail = 0;
        int nelem = 0;

        printf("=== Teste da função enqueue_value ===\n\n");

        printf("Estado inicial:\n");
        print_buffer(buffer, length, head, tail, nelem);

        printf("Inserindo valores 10, 20, 30, 40, 50:\n");
        for (int i = 1; i <= 5; i++) {
            int value = i * 10;
            int is_full = enqueue_value(buffer, length, nelem, tail, &head, value);

            tail = (tail + 1) % length;
            if (nelem < length) nelem++;

            printf("Inserido %d - Buffer %s\n", value, is_full ? "CHEIO" : "não cheio");
            print_buffer(buffer, length, head, tail, nelem);
        }

        printf("Inserindo mais valores (60, 70) - deve sobrescrever os mais antigos:\n");
        for (int i = 6; i <= 7; i++) {
            int value = i * 10;
            int is_full = enqueue_value(buffer, length, nelem, tail, &head, value);

            tail = (tail + 1) % length;

            printf("Inserido %d - Buffer %s\n", value, is_full ? "CHEIO" : "não cheio");
            print_buffer(buffer, length, head, tail, nelem);
        }

        free(buffer);
       


    // USAC07

    int buffer[10] = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    int length = 10;
    int nelem = 5;   // buffer tem 5 elementos válidos
    int tail = 0;    // tail aponta para o elemento mais antigo
    int head = 5;    // head aponta para onde entraria o próximo
    int n = 3;       // queremos remover 3 elementos

    int array[10];

    int result2 = move_n_to_array(buffer, length, &nelem, &tail, head, n, array);

    if (result2 == 1) {
        printf("Remoção bem sucedida.\n");
        printf("Elementos removidos: ");
        for (int i = 0; i < n; i++)
            printf("%d ", array[i]);
        printf("\n");

        printf("Novo tail: %d\n", tail);
        printf("Novo nelem: %d\n", nelem);

    } else {
        printf("Erro: não existem %d elementos no buffer.\n", n);
    }
    printf("\n");

    // USAC08
    int vec[] = {9, 7, 5, 3, 1, 2, 4, 6, 8};  
	int length2 = sizeof(vec) / sizeof(vec[0]);  
	char order = 1; 

	int result3 = sort_array(vec, length2, order);

	if (result3 == 1) {
        printf("Sucesso\n");
		for (int i = 0; i < length2; i++) {
            printf("%d ", vec[i]);
		}
        printf("\n");
	} else {
		printf("Erro \n");
	}
    printf("\n");

	//USAC03
	char str[] = "TEMP&unit:celsius&value:20#HUM&unit:percentage&value:80";
	char token[] = "TEMP";
	char unit[20];
	int value;

	int res3 = extract_data(str, token, unit, &value);
	if (res3 == 1) {
        printf("Unit: %s, Value: %d\n", unit, value);
	} else {
        printf("Extract failed.\n");
	}
	
	// USAC06 - Teste dequeue_value
    int buffer2[5] = {100, 200, 300, 400, 500};
    int length3 = 5;
    int nelem2 = 3;     // 3 elementos válidos
    int tail2 = 0;      // primeiro elemento está em buffer2[0]
    int head2 = 3;      // próximo elemento seria inserido em buffer2[3]
    int dequeued_value;
    
    printf("Buffer inicial (nelem=%d, tail=%d, head=%d): ", nelem2, tail2, head2);
    for (int i = 0; i < nelem2; i++) {
        printf("%d ", buffer2[(tail2 + i) % length3]);
    }
    printf("\n"); // <-- ADDED for better spacing


    // USAC04
    printf("--- USAC04 Tests ---\n");
    char cmd_out[20];
    char op_str[20];
    int op_val;
    int op_res;

    // Test 1: " rB ", 5 -> "RB, 05" (Success)
    strcpy(op_str, " rB ");
    op_val = 5;
    op_res = format_command(op_str, op_val, cmd_out);
    printf("Test 1: Result=%d, Output='%s'\n", op_res, cmd_out); // Expected: 1, 'RB, 05'

    // Test 2: " Ye ", 25 -> "YE, 25" (Success)
    strcpy(op_str, " Ye ");
    op_val = 25;
    op_res = format_command(op_str, op_val, cmd_out);
    printf("Test 2: Result=%d, Output='%s'\n", op_res, cmd_out); // Expected: 1, 'YE, 25'

    // Test 3: " Ye ", 125 -> "" (Failure)
    strcpy(op_str, " Ye ");
    op_val = 125;
    op_res = format_command(op_str, op_val, cmd_out);
    printf("Test 3: Result=%d, Output='%s'\n", op_res, cmd_out); // Expected: 0, ''

    // Test 4: " aaa ", 25 -> "" (Failure)
    strcpy(op_str, " aaa ");
    op_val = 25;
    op_res = format_command(op_str, op_val, cmd_out);
    printf("Test 4: Result=%d, Output='%s'\n", op_res, cmd_out); // Expected: 0, ''

    // Test 5: " gTh ", 25 -> "GTH" (Success)
    strcpy(op_str, " gTh ");
    op_val = 25; // Value should be ignored
    op_res = format_command(op_str, op_val, cmd_out);
    printf("Test 5: Result=%d, Output='%s'\n", op_res, cmd_out); // Expected: 1, 'GTH'
    printf("\n");
    
    // Remover primeiro elemento
    int result4 = dequeue_value(buffer2, length3, &nelem2, &tail2, &head2, &dequeued_value);
    if (result4 == 1) {
        printf("Elemento removido: %d\n", dequeued_value);
        printf("Novo tail: %d, Novo nelem: %d\n", tail2, nelem2);
    } else {
        printf("Erro: buffer vazio.\n");
    }
    
    // Remover segundo elemento
    result4 = dequeue_value(buffer2, length3, &nelem2, &tail2, &head2, &dequeued_value);
    if (result4 == 1) {
        printf("Elemento removido: %d\n", dequeued_value);
        printf("Novo tail: %d, Novo nelem: %d\n", tail2, nelem2);
    }
    
    // Tentar remover de buffer vazio (após remover todos)
    dequeue_value(buffer2, length3, &nelem2, &tail2, &head2, &dequeued_value);
    dequeue_value(buffer2, length3, &nelem2, &tail2, &head2, &dequeued_value);
    result4 = dequeue_value(buffer2, length3, &nelem2, &tail2, &head2, &dequeued_value);
    if (result4 == 0) {
        printf("Buffer vazio - não foi possível remover elemento.\n");
    }


    // --- USAC09 ---
        printf("--- USAC09 Tests ---\n");
        int median_val;
        int median_res;

        // Test 1: Success case (unsorted, 5 elements)
        // {50, 10, 30, 20, 40} -> {10, 20, 30, 40, 50} -> median é 30
        int vec_median[] = {50, 10, 30, 20, 40};
        int len_median = 5;
        median_res = median(vec_median, len_median, &median_val);
        printf("Test 1 (Success): Result=%d, Median is %d\n", median_res, median_val); // 1, 30

        // Test 2: Length é 0
        int vec_fail[] = {};
        int len_fail = 0;
        median_res = median(vec_fail, len_fail, &median_val);
        printf("Test 2 (Fail): Result=%d\n", median_res); // 0
        printf("\n");

    return 0;
}

