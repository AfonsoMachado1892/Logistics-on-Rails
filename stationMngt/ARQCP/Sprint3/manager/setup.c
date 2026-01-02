#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../common/domain.h"
#include "setup.h"

// Funções Assembly externas
extern void init_track_struct(Track* t, int id, int state, int train_id);
extern int encrypt_data(char* in, int key, char *out);

// Variáveis Globais Externas
extern User* users;
extern int num_users;
extern Track* tracks;
extern int num_tracks;
extern SystemConfig sys_config;

void load_initial_setup(const char* filename) {
    FILE* file = fopen(filename, "r");
    if (!file) {
        perror("[ERROR] Config file not found");
        // Valores default seguros caso o ficheiro não exista
        sys_config.temp_buf_len = 10;
        sys_config.temp_win_len = 5;
        sys_config.hum_buf_len = 10;
        sys_config.hum_win_len = 5;
        return;
    }

    char line[256];

    // Limpeza de memória prévia
    num_users = 0; num_tracks = 0;
    if(users) { free(users); users = NULL; }
    if(tracks) { free(tracks); tracks = NULL; }

    while (fgets(line, sizeof(line), file)) {
        line[strcspn(line, "\r\n")] = 0; // Remover quebra de linha

        char* type = strtok(line, ",");
        if (!type) continue;

        // --- Requisito: Configuração de Sensores (Temperatura) ---
        if (strcmp(type, "SENSOR_TEMP") == 0) {
            char* buf_len = strtok(NULL, ",");
            char* win_len = strtok(NULL, ",");
            if (buf_len) sys_config.temp_buf_len = atoi(buf_len);
            if (win_len) sys_config.temp_win_len = atoi(win_len);
        }
        // --- Requisito: Configuração de Sensores (Humidade) ---
        else if (strcmp(type, "SENSOR_HUM") == 0) {
            char* buf_len = strtok(NULL, ",");
            char* win_len = strtok(NULL, ",");
            if (buf_len) sys_config.hum_buf_len = atoi(buf_len);
            if (win_len) sys_config.hum_win_len = atoi(win_len);
        }
        // --- Requisito: Track (ID, State, Train) ---
        else if (strcmp(type, "TRACK") == 0) {
            char* id_str = strtok(NULL, ",");
            char* state_str = strtok(NULL, ",");
            char* train_str = strtok(NULL, ",");

            if (id_str) {
                int id = atoi(id_str);
                int state = 0;     // Default: Livre
                int train_id = -1; // Default: Sem comboio

                if (state_str) state = atoi(state_str);
                if (train_str) train_id = atoi(train_str);

                // Alocação Dinâmica
                tracks = (Track*) realloc(tracks, (num_tracks + 1) * sizeof(Track));

                // CHAMADA ASSEMBLY (USAC16 req): init_track_struct
                init_track_struct(&tracks[num_tracks], id, state, train_id);
                num_tracks++;
            }
        }
        // --- Requisito: User (Name, Username, Password, Key) ---
        else if (strcmp(type, "USER") == 0) {
            char* name = strtok(NULL, ",");
            char* user = strtok(NULL, ",");
            char* pass = strtok(NULL, ",");
            char* key_str = strtok(NULL, ",");

            if (name && user && pass && key_str) {
                int key = atoi(key_str);

                // Alocação Dinâmica
                users = (User*) realloc(users, (num_users + 1) * sizeof(User));

                strncpy(users[num_users].name, name, 49);
                strncpy(users[num_users].username, user, 19);
                users[num_users].key = key;

                // CHAMADA ASSEMBLY (USAC01 req): encrypt_data
                // Requisito da imagem: "stored encrypted, according to Caesar Cipher"
                char encrypted_pass[20];
                encrypt_data(pass, key, encrypted_pass);
                strncpy(users[num_users].password, encrypted_pass, 19);

                num_users++;
            }
        }
    }
    fclose(file);
    printf("[INFO] Setup Loaded based on USAC11 Requirements.\n");
    printf("       -> Temp Config: Buffer=%d, Window=%d\n", sys_config.temp_buf_len, sys_config.temp_win_len);
    printf("       -> Hum Config:  Buffer=%d, Window=%d\n", sys_config.hum_buf_len, sys_config.hum_win_len);
    printf("       -> Loaded %d Tracks and %d Users.\n", num_tracks, num_users);
}