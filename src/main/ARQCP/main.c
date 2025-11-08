#include <stdio.h>
#include "DECRYPT_DATA.H" 
#include "SORT_ARRAY.H"

int main(void) {
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

