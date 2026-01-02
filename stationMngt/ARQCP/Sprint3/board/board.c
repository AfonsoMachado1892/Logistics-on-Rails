#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "board.h"

static void clear_screen() {
    printf("\033[H\033[J");
}

static void draw_line(int width) {
    printf("+");
    for (int i = 0; i < width; i++) printf("-");
    printf("+\n");
}

void update_board_view(Track* tracks, int num_tracks, SensorData* sensorData) {
    clear_screen();

    printf("  __________________________________________________  \n");
    printf(" /                                                  \\ \n");
    printf("|        RAILWAY STATION DASHBOARD - SPRINT 3        |\n");
    printf(" \\__________________________________________________/ \n");
    printf("\n");

    // --- SENSORES ---
    draw_line(50);
    printf("| SENSORS (Component: Sensors)                     |\n");
    draw_line(50);

    if (sensorData && sensorData->valid) {
        printf("| TEMP: %-3d %-8s | HUM: %-3d %-8s          |\n",
               sensorData->temperature, sensorData->temp_unit,
               sensorData->humidity, sensorData->hum_unit);
    } else {
        printf("| [!] WAITING FOR SENSOR DATA...                   |\n");
    }
    draw_line(50);
    printf("\n");

    // --- TRACKS ---
    printf("   __||__      __||__      __||__   \n");
    printf("  (______)    (______)    (______)  \n");
    printf("\n");

    draw_line(50);
    printf("| TRACK STATUS (Component: LightSigns)             |\n");
    draw_line(50);
    printf("| ID  | STATE       | TRAIN | VISUAL           |\n");
    draw_line(50);

    for (int i = 0; i < num_tracks; i++) {
        char state_str[15];
        char visual[10];

        switch(tracks[i].state) {
            case 0: strcpy(state_str, "FREE"); strcpy(visual, "[  ]"); break;
            case 1: strcpy(state_str, "BUSY"); strcpy(visual, "[##]"); break;
            case 2: strcpy(state_str, "ASSIGNED"); strcpy(visual, "[--]"); break;
            case 3: strcpy(state_str, "INOP"); strcpy(visual, "[XX]"); break;
            default: strcpy(state_str, "ERR"); strcpy(visual, "[??]");
        }

        char train_info[10];
        if (tracks[i].train_id != -1) sprintf(train_info, "T-%d", tracks[i].train_id);
        else strcpy(train_info, " -- ");

        printf("| #%-2d | %-11s | %-5s | %-16s |\n",
               tracks[i].id, state_str, train_info, visual);
    }
    draw_line(50);
    printf("\n[Press ENTER to refresh...]");
}