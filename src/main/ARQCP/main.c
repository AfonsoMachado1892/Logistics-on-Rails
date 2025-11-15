#include <stdio.h>
#include "DECRYPT_DATA.H" 
#include "SORT_ARRAY.H"
#include "REMOVE_OLDEST.H"
#include "ENCRYPT_DATA.H"
#include "EXTRACT_DATA.H"
#include "DEQUEUE_VALUE.H"

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

    // USAC08
    int vec[] = {9, 7, 5, 3, 1, 2, 4, 6, 8};  
	int length2 = sizeof(vec) / sizeof(vec[0]);  
	char order = 1; 

	int result3	 = sort_array(vec, length2, order);

	if (result3 == 1) {
    printf("Sucesso\n");
		for (int i = 0; i < length2; i++) {
        printf("%d ", vec[i]);  
		}
    printf("\n");
	} else {
		printf("Erro \n");
	}
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

    return 0;
}

