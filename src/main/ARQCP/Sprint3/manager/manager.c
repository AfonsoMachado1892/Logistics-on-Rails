#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <time.h>
#include <sys/stat.h>
#include <sys/types.h>
#include "../common/domain.h"
#include "sensors_comm.h"
#include "setup.h"
#include "../board/board.h"

// Funções Assembly Externas
extern int format_command(char* op, int n, char *cmd);
extern void update_track_struct(Track* t, int state, int train_id);
extern int encrypt_data(char* in, int key, char *out); // Added for login
extern int enqueue_value(int* buffer, int length, int* nelem, int* tail, int* head, int value); // For sensor history

// Variáveis Globais
User* users = NULL;
int num_users = 0;
Track* tracks = NULL;
int num_tracks = 0;
SystemConfig sys_config;
const char* SERIAL_PORT = "/dev/ttyACM0"; // Porta padrão
Log* system_logs = NULL;
int num_logs = 0;
User* current_user = NULL;


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

// AC12 - Record action with Track ID Persistence
void record_action(User* user, const char* action, int track_id) {
    if (user == NULL) return;

    mkdir("logs", 0777);

    Log* temp = (Log*) realloc(system_logs, (num_logs + 1) * sizeof(Log));
    if (temp == NULL) return;
    system_logs = temp;

    system_logs[num_logs].id = num_logs + 1;
    strncpy(system_logs[num_logs].username, user->username, 19);

    // Format action with Track ID (e.g., "FREED TRACK #2")
    if (track_id != -1) {
        snprintf(system_logs[num_logs].action, 49, "%s #%d", action, track_id);
    } else {
        strncpy(system_logs[num_logs].action, action, 49);
    }

    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    strftime(system_logs[num_logs].timestamp, sizeof(system_logs[num_logs].timestamp),
             "%Y-%m-%d %H:%M:%S", t);

    // Persistent Master Audit
    FILE* master_f = fopen("logs/master_audit.log", "a");
    if (master_f) {
        fprintf(master_f, "[ID: %03d] %s | User: %s | Action: %s\n",
                system_logs[num_logs].id,
                system_logs[num_logs].timestamp,
                system_logs[num_logs].username,
                system_logs[num_logs].action);
        fclose(master_f);
    }

    num_logs++;
}

// USAC12 - Export logs by filtering the persistent master file
void export_user_logs() {
    char target_user[20];
    char out_filename[60];
    char line[256];

    printf("\n[USAC12] Export logs for which username? ");
    scanf("%19s", target_user);
    getchar();

    // Create a specific export file for this user
    sprintf(out_filename, "logs/actions_%s.txt", target_user);

    FILE* master_f = fopen("logs/master_audit.log", "r");
    if (!master_f) {
        printf("[ERROR] No audit history found.\n");
        return;
    }

    FILE* export_f = fopen(out_filename, "w");
    if (!export_f) {
        fclose(master_f);
        return;
    }

    fprintf(export_f, "=== HISTORICAL LOG SEQUENCE FOR USER: %s ===\n", target_user);

    int found = 0;
    // Scan every line in the master file for the target username
    while (fgets(line, sizeof(line), master_f)) {
        // Look for the string "User: username" in the log line
        char search_pattern[30];
        sprintf(search_pattern, "| User: %s |", target_user);

        if (strstr(line, search_pattern) != NULL) {
            fprintf(export_f, "%s", line);
            found = 1;
        }
    }

    if (!found) fprintf(export_f, "No historical actions found for this user.\n");

    fclose(master_f);
    fclose(export_f);
    printf("[SUCCESS] Full history exported to %s\n", out_filename);
    sleep(1);
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

        //guardar no log
        record_action(current_user, "ASSIGNED TRACK TO TRAIN", tracks[found_track].id);
        // Atualiza LightSigns (Amarelo = YE)
        send_track_command(found_track, "YE");
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

        // gaurdar no log
        record_action(current_user, "FREED TRACK", track_id);

        // Atualiza LightSigns (Verde = GE)
        send_track_command(idx, "GE");

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

        // guardar no log
        record_action(current_user, "SET INOPERATIVE", track_id);

        // Atualiza LightSigns (Vermelho a piscar = RB)
        send_track_command(idx, "RB");

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

        // guardar no log
        record_action(current_user, "SET BUSY (STOP)", track_id);

        // Atualiza LightSigns (Vermelho = RE)
        send_track_command(idx, "RE");

        printf("[SUCCESS] Track %d set to BUSY (Stopped).\n", track_id);
    } else {
        printf("[ERROR] Track ID not found.\n");
    }
    sleep(1);
}
//USAC14 As a User, I want to control the track sign light.
 void control_track_light() {
     int track_id;
     char light_choice;

     printf("\n========== TRACK LIGHT CONTROL ==========\n");
     printf("Enter Track ID: ");
     if (scanf("%d", &track_id) != 1) {
         getchar();
         return;
     }
     getchar(); // Clear newline

     // Validate track exists
     int idx = -1;
     for (int i = 0; i < num_tracks; i++) {
         if (tracks[i].id == track_id) {
             idx = i;
             break;
         }
     }

     if (idx == -1) {
         printf("[ERROR] Track ID %d not found.\n", track_id);
         sleep(1);
         return;
     }

     printf("\nSelect Light Color:\n");
     printf("  [G] GREEN   - Free/Departure\n");
     printf("  [Y] YELLOW  - Assigned/Caution\n");
     printf("  [R] RED     - Stopped/Occupied\n");
     printf("  [B] BLINK   - Red Blinking (Inoperative)\n");
     printf("\nChoice: ");

     scanf("%c", &light_choice);
     getchar(); // Clear newline

     char* command_code = NULL;
     char* log_action = NULL;
     int new_state = tracks[idx].state; // Keep current state by default

     switch(light_choice) {
         case 'G':
         case 'g':
             command_code = "GE";
             log_action = "SET LIGHT GREEN";
             new_state = STATE_FREE;
             break;
         case 'Y':
         case 'y':
             command_code = "YE";
             log_action = "SET LIGHT YELLOW";
             new_state = STATE_ASSIGNED;
             break;
         case 'R':
         case 'r':
             command_code = "RE";
             log_action = "SET LIGHT RED";
             new_state = STATE_BUSY;
             break;
         case 'B':
         case 'b':
             command_code = "RB";
             log_action = "SET LIGHT BLINKING";
             new_state = STATE_INOP;
             break;
         default:
             printf("[ERROR] Invalid choice.\n");
             sleep(1);
             return;
     }

     // Update track state and send command
     update_track_struct(&tracks[idx], new_state, tracks[idx].train_id);
     send_track_command(idx, command_code);
     record_action(current_user, log_action, track_id);

     printf("[SUCCESS] Track #%d light set to %s\n", track_id,
            command_code[0] == 'G' ? "GREEN" :
            command_code[0] == 'Y' ? "YELLOW" :
            command_code[1] == 'B' ? "RED BLINKING" : "RED");
     sleep(1);
 }

void manager_main_loop() {
    SensorData sensors;
    char command_buffer[10];
    int running = 1;

    // USAC11: Initial Setup
    load_initial_setup("config.txt");


    // --- LOGIN PROMPT ---
    char input_username[20];
    char input_password[20];
    char encrypted_input[20];

    printf("\n============== LOGIN ==============\n");
    printf("Username: ");
    scanf("%19s", input_username);
    printf("Password: ");
    scanf("%19s", input_password);
    getchar(); // Clear newline from buffer

    for (int i = 0; i < num_users; i++) {
        if (strcmp(input_username, users[i].username) == 0) {
            // Use assembly to encrypt input for comparison
            encrypt_data(input_password, users[i].key, encrypted_input);

            if (strcmp(encrypted_input, users[i].password) == 0) {
                current_user = &users[i];
                break;
            }
        }
    }

    if (!current_user) {
        printf("[ERROR] Authentication failed. Exiting...\n");
        running = 0;
    } else {
        printf("[SUCCESS] Welcome, %s!\n", current_user->name);
        sleep(1);
    }

    // --- MAIN SYSTEM LOOP ---
    while (running) {
        // 1. Get sensor data (USAC13)
        if (!get_sensor_data(SERIAL_PORT, &sensors)) {
            sensors.valid = 0;
        }

        // 2. Update Dashboard (USAC15)
        update_board_view(tracks, num_tracks, &sensors);

        // 3. UI Menu with Quit Option
        printf("\nUSER: %s\n", current_user->username);
        printf("COMMANDS: [1]Assign [2]Free [3]Inop [4]Stopped [5]ExportLogs [6]ControlLight [0]Quit [ENTER]Refresh\n");
        printf("Choice > ");

        if (!fgets(command_buffer, sizeof(command_buffer), stdin)) break;

        // Parsing do comando
        if (command_buffer[0] == '0') {
            running = 0; // Quit
        } else if (command_buffer[0] == '1') {
            assign_track_to_train();
        } else if (command_buffer[0] == '2') {
            free_track_operation();
        } else if (command_buffer[0] == '3') {
            set_track_inoperative();
        } else if (command_buffer[0] == '4') {
            set_track_busy();
        } else if (command_buffer[0] == '5') {
            // USAC12 Check: Only "admin" (System Administrator) is authorized
            if (strcmp(current_user->username, "admin") == 0) {
                export_user_logs();
            } else {
                printf("[ERROR] Access Denied: Administrator profile required.\n");
                sleep(1);
            }
        }
        else if (command_buffer[0] == '6') {
                    control_track_light(); 
                }
        }
        // Se for ENTER (newline), apenas continua o loop e atualiza sensores/board

    // --- QUIT & DYNAMIC MEMORY CLEANUP ---
    printf("\n[INFO] Closing system and freeing memory...\n");
    if(users) {
        free(users);
        users = NULL;
    }
    if(tracks) {
        free(tracks);
        tracks = NULL;
    }

    if(system_logs) {
        free(system_logs);
        system_logs = NULL;
    }

    printf("[Info]: Memory cleared.\n");
}

int main() {
    manager_main_loop();
    return 0;
}
