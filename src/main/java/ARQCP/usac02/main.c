#include <stdio.h>
#include "decrypt_data.h"

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

    return 0;
}
