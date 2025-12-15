#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "../common/domain.h"
#include "sensors_comm.h"
#include "setup.h"
#include "../board/board.h"

// Funções Assembly Externas
extern int format_command(char* op, int n, char *cmd);
extern void update_track_struct(Track* t, int state, int train_id);

// Variáveis Globais
User* users = NULL;
int num_users = 0;
Track* tracks = NULL;
int num_tracks = 0;
SystemConfig sys_config;
const char* SERIAL_PORT = "/dev/ttyACM0"; // Porta padrão

// Estados das Tracks (conforme board.c e lógica)
#define STATE_FREE 0
#define STATE_BUSY 1     // Ocupado/Parado
#define STATE_ASSIGNED 2 // Atribuído/Amarelo
#define STATE_INOP 3     // Inoperativo

void send_track_command(int track_id, char* op_code) {
    char cmd_str[20];

    // CHAMADA ASSEMBLY: format_command (USAC04)
    // Prepara a string ex: "YE,01"
    if (format_command(op_code, track_id, cmd_str)) {
        // Envia para LightSigns (USAC14/USAC16 requirement)
        send_light_command(SERIAL_PORT, cmd_str);
    } else {
        printf("[ERROR] Failed to format command for LightSigns\n");
    }
}

// USAC16 - Assign Track to Arriving Train
void assign_track_to_train() {
    int train_id;
    printf("\n[ASSIGN TRACK] Enter Train ID: ");
    if (scanf("%d", &train_id) != 1) return;

    // Procurar track livre
    int found_track = -1;
    for (int i = 0; i < num_tracks; i++) {
        if (tracks[i].state == STATE_FREE) {
            found_track = i;
            break;
        }
    }

    if (found_track != -1) {
        // CHAMADA ASSEMBLY: update_track_struct
        // Parâmetro struct obrigatório para US Sprint 3
        update_track_struct(&tracks[found_track], STATE_ASSIGNED, train_id);

        // Atualiza LightSigns (Amarelo = YE)
        send_track_command(tracks[found_track].id, "YE");

        printf("[SUCCESS] Train %d assigned to Track %d\n", train_id, tracks[found_track].id);
    } else {
        printf("\n[!!!] EMERGENCY STOP: NO TRACKS AVAILABLE [!!!]\n");
    }
    sleep(1);
}

// USAC16 - Give Departure Order / Free Track
void free_track_operation() {
    int track_id;
    printf("\n[DEPARTURE/FREE] Enter Track ID: ");
    if (scanf("%d", &track_id) != 1) return;

    int idx = -1;
    for (int i = 0; i < num_tracks; i++) {
        if (tracks[i].id == track_id) {
            idx = i;
            break;
        }
    }

    if (idx != -1) {
        // Atualiza struct via Assembly
        update_track_struct(&tracks[idx], STATE_FREE, -1);

        // Atualiza LightSigns (Verde = GE)
        send_track_command(track_id, "GE");

        printf("[SUCCESS] Track %d is now FREE.\n", track_id);
    } else {
        printf("[ERROR] Track ID not found.\n");
    }
    sleep(1);
}

// USAC16 - Set Inoperative
void set_track_inoperative() {
    int track_id;
    printf("\n[SET INOP] Enter Track ID: ");
    if (scanf("%d", &track_id) != 1) return;

    int idx = -1;
    for (int i = 0; i < num_tracks; i++) {
        if (tracks[i].id == track_id) {
            idx = i;
            break;
        }
    }

    if (idx != -1) {
        update_track_struct(&tracks[idx], STATE_INOP, -1);

        // Atualiza LightSigns (Vermelho a piscar = RB)
        send_track_command(track_id, "RB");

        printf("[SUCCESS] Track %d set to INOPERATIVE.\n", track_id);
    } else {
        printf("[ERROR] Track ID not found.\n");
    }
    sleep(1);
}

// USAC16 - Set Busy (Train Stopped) - Auxiliar
void set_track_busy() {
    int track_id;
    printf("\n[SET STOPPED] Enter Track ID: ");
    if (scanf("%d", &track_id) != 1) return;

    int idx = -1;
    for (int i = 0; i < num_tracks; i++) {
        if (tracks[i].id == track_id) {
            idx = i;
            break;
        }
    }

    if (idx != -1) {
        // Mantém o comboio atual, muda estado para BUSY (1)
        update_track_struct(&tracks[idx], STATE_BUSY, tracks[idx].train_id);

        // Atualiza LightSigns (Vermelho = RE)
        send_track_command(track_id, "RE");

        printf("[SUCCESS] Track %d set to BUSY (Stopped).\n", track_id);
    } else {
        printf("[ERROR] Track ID not found.\n");
    }
    sleep(1);
}

void manager_main_loop() {
    SensorData sensors;
    char command_buffer[10];

    // USAC11: Setup Inicial
    load_initial_setup("config.txt");

    while (1) {
        // 1. Obter dados dos sensores (USAC13)
        // Tentamos obter dados; se falhar (ex: cabo desligado), marca como inválido mas não crasha
        if (!get_sensor_data(SERIAL_PORT, &sensors)) {
            sensors.valid = 0;
        }

        // 2. Atualizar o Board (USAC15)
        update_board_view(tracks, num_tracks, &sensors);

        // 3. Menu UI
        printf("\nCOMMANDS: [1]Assign Train  [2]Depart/Free  [3]Set Inop  [4]Set Stopped  [ENTER]Refresh\n");
        printf("Choice > ");

        // Leitura de comando simples (bloqueante para permitir interação do user)
        fgets(command_buffer, sizeof(command_buffer), stdin);

        // Parsing do comando
        if (command_buffer[0] == '1') {
            assign_track_to_train();
        } else if (command_buffer[0] == '2') {
            free_track_operation();
        } else if (command_buffer[0] == '3') {
            set_track_inoperative();
        } else if (command_buffer[0] == '4') {
            set_track_busy();
        }
        // Se for ENTER (newline), apenas continua o loop e atualiza sensores/board
    }

    if(users) free(users);
    if(tracks) free(tracks);
}

int main() {
    manager_main_loop();
    return 0;
}
