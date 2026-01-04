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
#include "../ui/ui.h"

// Funções Assembly Externas
extern int format_command(char* op, int n, char *cmd);
extern void update_track_struct(Track* t, int state, int train_id);
extern int encrypt_data(char* in, int key, char *out); // added for login
extern int enqueue_value(int* buffer, int length, int* nelem, int* tail, int* head, int value); // for sensor history
extern int median(int* vec, int length, int *me); // USAC09

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

// Circular Buffer Variables for Sensor History (USAC05)
int *temp_buf = NULL, *hum_buf = NULL;
int t_nelem = 0, t_head = 0, t_tail = 0;
int h_nelem = 0, h_head = 0, h_tail = 0;

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
    strftime(system_logs[num_logs].timestamp, sizeof(system_logs[num_logs].timestamp), "%Y-%m-%d %H:%M:%S", t);

    // Persistent Master Audit
    FILE* master_f = fopen("logs/master_audit.log", "a");
    if (master_f) {
        fprintf(master_f, "[ID: %03d] %s | User: %s | Action: %s\n",
                system_logs[num_logs].id, system_logs[num_logs].timestamp,
                system_logs[num_logs].username, system_logs[num_logs].action);
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
    int train_id = get_robust_int("\n[ASSIGN TRACK] Enter Train ID: ");
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
    int track_id = get_robust_int("\n[DEPARTURE/FREE] Enter Track ID: ");
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
    int track_id = get_robust_int("\n[SET INOP] Enter Track ID: ");
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
    int track_id = get_robust_int("\n[SET STOPPED] Enter Track ID: ");
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
    int track_id = get_robust_int("\n========== TRACK LIGHT CONTROL ==========\nEnter Track ID: ");
    int idx = -1;
    for (int i = 0; i < num_tracks; i++) {
        if (tracks[i].id == track_id) {
            idx = i;
            break;
        }
    }

     // Validate track exists
    if (idx == -1) {
        printf("[ERROR] Track ID %d not found.\n", track_id);
        sleep(1);
        return;
    }

    printf("\nSelect Light Color: [G]REEN [Y]ELLOW [R]ED [B]LINK\nChoice: ");
    char light_choice;
    scanf(" %c", &light_choice); getchar();

     char* command_code = NULL;
     char* log_action = NULL;
     int new_state = tracks[idx].state; // Keep current state by default

    switch(light_choice) {
        case 'G': case 'g': command_code = "GE"; log_action = "SET LIGHT GREEN"; new_state = STATE_FREE; break;
        case 'Y': case 'y': command_code = "YE"; log_action = "SET LIGHT YELLOW"; new_state = STATE_ASSIGNED; break;
        case 'R': case 'r': command_code = "RE"; log_action = "SET LIGHT RED"; new_state = STATE_BUSY; break;
        case 'B': case 'b': command_code = "RB"; log_action = "SET LIGHT BLINKING"; new_state = STATE_INOP; break;
        default: printf("[ERROR] Invalid choice.\n"); sleep(1); return;
    }

     // Update track state and send command
    update_track_struct(&tracks[idx], new_state, tracks[idx].train_id);
    send_track_command(idx, command_code);
    record_action(current_user, log_action, track_id);
    printf("[SUCCESS] Track #%d light set to %s\n", track_id, command_code);
    sleep(1);
}

void manager_main_loop() {
    SensorData sensors;
    int running = 1;

    load_initial_setup("res/config.txt");

    // Dynamic Buffer Initialization (USAC11 Requirement)
    temp_buf = (int*) malloc(sys_config.temp_buf_len * sizeof(int));
    hum_buf = (int*) malloc(sys_config.hum_buf_len * sizeof(int));

    char input_username[20], input_password[20], encrypted_input[20];
    printf("\n============== LOGIN ==============\nUsername: ");
    scanf("%19s", input_username);
    printf("Password: ");
    scanf("%19s", input_password); getchar();

    for (int i = 0; i < num_users; i++) {
        if (strcmp(input_username, users[i].username) == 0) {
            encrypt_data(input_password, users[i].key, encrypted_input);
            if (strcmp(encrypted_input, users[i].password) == 0) {
                current_user = &users[i];
                break;
            }
        }
    }

    if (!current_user) {
        printf("[ERROR] Authentication failed.\n");
        running = 0;
    } else {
        printf("[SUCCESS] Welcome, %s!\n", current_user->name);
        sleep(1);
    }

    while (running) {
        if (get_sensor_data(SERIAL_PORT, &sensors)) {
            // USAC05: Update circular history buffers via Assembly
            enqueue_value(temp_buf, sys_config.temp_buf_len, &t_nelem, &t_tail, &t_head, sensors.temperature);
            enqueue_value(hum_buf, sys_config.hum_buf_len, &h_nelem, &h_tail, &h_head, sensors.humidity);

            // USAC09: Apply Moving Median filter to handle error-prone sensor data (Requirement USAC10/USAC13)
            // This logic ensures that the dashboard shows a smoothed "median" value rather than raw, noisy data.

            // 1. Process Temperature Median
            if (t_nelem >= sys_config.temp_win_len) {
                // Dynamically allocate a temporary vector for sorting (required for the median assembly function)
                int *temp_sort_vec = (int*)malloc(sys_config.temp_win_len * sizeof(int));
                if (temp_sort_vec == NULL) {
                    printf("[ERROR] Memory allocation failed for temperature sorting.\n");
                    continue; // Skip this iteration if memory fails
                }

                // Copy the most recent 'n' elements from the circular buffer history to the linear temp vector
                for (int i = 0; i < sys_config.temp_win_len; i++) {
                    // Calculate index moving backwards from the current 'head' position
                    // The modulo operator (%) ensures we wrap around correctly if we reach the start of the buffer
                    int idx = (t_head - 1 - i + sys_config.temp_buf_len) % sys_config.temp_buf_len;
                    temp_sort_vec[i] = temp_buf[idx];
                }

                // Call the Assembly function USAC09: sorts the vector and calculates the median value
                // The result is written directly back into the 'sensors.temperature' variable for display
                median(temp_sort_vec, sys_config.temp_win_len, &sensors.temperature);

                // Free temporary memory to avoid memory leaks (Mandatory Dynamic Memory Management)
                free(temp_sort_vec);
            }

            // 2. Process Humidity Median
            if (h_nelem >= sys_config.hum_win_len) {
                int *hum_sort_vec = (int*)malloc(sys_config.hum_win_len * sizeof(int));
                if (hum_sort_vec == NULL) {
                    printf("[ERROR] Memory allocation failed for humidity sorting.\n");
                    continue;
                }

                // Extract the window of values specified in setup.txt (e.g., the last 5 readings)
                for (int i = 0; i < sys_config.hum_win_len; i++) {
                    int idx = (h_head - 1 - i + sys_config.hum_buf_len) % sys_config.hum_buf_len;
                    hum_sort_vec[i] = hum_buf[idx];
                }

                // Perform assembly-based median calculation for humidity
                median(hum_sort_vec, sys_config.hum_win_len, &sensors.humidity);

                free(hum_sort_vec);
            }
        } else {
            sensors.valid = 0;
        }

        update_board_view(tracks, num_tracks, &sensors);

        char choice = get_menu_choice();
        switch(choice) {
            case '0': running = 0; break;
            case '1': assign_track_to_train(); break;
            case '2': free_track_operation(); break;
            case '3': set_track_inoperative(); break;
            case '4': set_track_busy(); break;
            case '5':
                if (strcmp(current_user->username, "admin") == 0) export_user_logs();
                else { printf("[ERROR] Admin required.\n"); sleep(1); }
                break;
            case '6': control_track_light(); break;
            default: break;
        }
    }

    printf("\n[INFO] Closing system and freeing memory...\n");
    if(users) free(users);
    if(tracks) free(tracks);
    if(system_logs) free(system_logs);
    if(temp_buf) free(temp_buf);
    if(hum_buf) free(hum_buf);
}

int main() {
    manager_main_loop();
    return 0;
}