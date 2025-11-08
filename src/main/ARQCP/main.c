#include <stdio.h>
#include "DECRYPT_DATA.H" 
#include "SORT_ARRAY.H"
#include "REMOVE_OLDEST.H"

int main(void) {

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

    int result = move_n_to_array(buffer, length, &nelem, &tail, head, n, array);

    if (result == 1) {
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
	int length = sizeof(vec) / sizeof(vec[0]);  
	char order = 1; 

	int result2	 = sort_array(vec, length, order);

	if (result2 == 1) {
    printf("Sucesso\n");
		for (int i = 0; i < length; i++) {
        printf("%d ", vec[i]);  
		}
    printf("\n");
	} else {
		printf("Erro \n");
	}

    return 0;
}

