#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../common/domain.h"
#include "setup.h"

// Declaração das funções Assembly
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
        sys_config.temp_buf_len = 10;
        return;
    }

    char line[256];

    // Limpeza de memória prévia
    num_users = 0; num_tracks = 0;
    if(users) { free(users); users = NULL; }
    if(tracks) { free(tracks); tracks = NULL; }

    while (fgets(line, sizeof(line), file)) {
        line[strcspn(line, "\r\n")] = 0;
        char* type = strtok(line, ",");
        if (!type) continue;

        if (strcmp(type, "CONFIG") == 0) {
            char* t_buf = strtok(NULL, ",");
            char* t_win = strtok(NULL, ",");
            char* h_buf = strtok(NULL, ",");
            char* h_win = strtok(NULL, ",");
            if (t_buf) sys_config.temp_buf_len = atoi(t_buf);
            if (t_win) sys_config.temp_win_len = atoi(t_win);
            if (h_buf) sys_config.hum_buf_len = atoi(h_buf);
            if (h_win) sys_config.hum_win_len = atoi(h_win);
        }
        else if (strcmp(type, "TRACK") == 0) {
            char* id_str = strtok(NULL, ",");
            if (id_str) {
                int id = atoi(id_str);
                // Alocação Dinâmica
                tracks = (Track*) realloc(tracks, (num_tracks + 1) * sizeof(Track));
                // CHAMADA ASSEMBLY (Requisito Struct)
                init_track_struct(&tracks[num_tracks], id, 0, -1);
                num_tracks++;
            }
        }
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
                // CHAMADA ASSEMBLY (Sprint 2)
                char encrypted_pass[20];
                encrypt_data(pass, key, encrypted_pass);
                strncpy(users[num_users].password, encrypted_pass, 19);
                num_users++;
            }
        }
    }
    fclose(file);
    printf("[INFO] Setup Loaded: %d Tracks, %d Users.\n", num_tracks, num_users);
}