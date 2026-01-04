#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "board.h"

#define STATE_FREE 0
#define STATE_BUSY 1
#define STATE_ASSIGNED 2
#define STATE_INOP 3

/**
 * USAC15 - Board Component (Complex ASCII Output)
 * Purpose: Display station data using high-detail ASCII art to capture user attention.
 */

static void clear_screen() {
    printf("\033[H\033[J");
}

static void draw_main_station_INOP() {
    printf("      _______________________                   (    )    )    \n");
    printf("     /                       \\                 (_   _  _)     \n");
    printf("    /_________________________\\      '  ________      '     '            \n");
    printf("    |     RAILWAY STATION '   |   '  [']        |     '        '  \n");
    printf("    |_________________________|      | | 'INOP  |         '      '\n");
    printf("    |  _  |     __      |  _  | '    | |________|     '        '  \n");
    printf("    | |_| | '  |  |     | |_| |   '  |'|      '           '       \n");
    printf(" ___|_____|____|__|_____|_____|______|_|_________________________\n");
    printf("|  _^_    '       _^_     '       '       _^_      _^_      '    |\n");
    printf("|      '  _^_  '       '   _^_ '      '           _^_       '    |\n");
    printf("|________________________________________________________________|\n");
    printf("  '   '              '            '       '         '        '    \n");
    printf("|================================================================|\n");
    printf("    ||  _^_ ||      ||      ||   _^_||      ||  _^_ ||      ||    \n");
}

static void draw_main_station_BUSY() {
    printf("      _______________________                   (    )    )    \n");
    printf("     /                       \\                 (_   _  _)     \n");
    printf("    /_________________________\\      '  ________       '    '     '\n");
    printf("    |     RAILWAY STATION '   |   '  [']        |     '        '   \n");
    printf("    |_________________________|   '  | | 'BUSY  |         '      '\n");
    printf("    |  _  |     __      |  _  | '    | |________|     '        '  \n");
    printf("    | |_| | '  |  |     | |_| |   '  | | '    '        '       \n");
    printf(" ___|_____|____|__|_____|_____|______|_|_________________________\n");
    printf("|  _^_    '       _^_     '       '       _^_      _^_      '    |\n");
    printf("|      '  _^_  '       '   _^_ '         _________________       |\n");
    printf("|_____________________________________ _/                 \\______|\n");
    printf("   '      '       '       '       '   |____________________|      \n");
    printf("|========================================(O)(O)======(O)(O)======|\n");
    printf("    ||   _^_||      ||  _^_ ||      ||_^_   ||      ||      ||    \n");
}

static void draw_main_station_ASSIGNED() {
    printf("      _______________________                   \\ | /            \n");
    printf("     /                       \\                  - O -            \n");
    printf("    /_________________________\\         ________/ | \\           \n");
    printf("    |     RAILWAY STATION     |      [ ]        |                  \n");
    printf("    |_________________________|      | |ASSIGNED|                 \n");
    printf("    |  _  |     __      |  _  |      | |________|                 \n");
    printf("    | |_| |    |  |     | |_| |      | |                          \n");
    printf(" ___|_____|____|__|_____|_____|______|_|_________________________\n");
    printf("|                                                                |\n");
    printf("|                                        _________________       |\n");
    printf("|_____________________________________ _/                 \\______|\n");
    printf("                                      |____________________|      \n");
    printf("|========================================(O)(O)======(O)(O)======|\n");
    printf("    ||      ||      ||      ||      ||      ||      ||      ||    \n");
}

static void draw_main_station_FREE() {
    printf("      _______________________                   \\ | /          \n");
    printf("     /                       \\                  - O -           \n");
    printf("    /_________________________\\         ________/ | \\          \n");
    printf("    |     RAILWAY STATION     |      [ ]        |                 \n");
    printf("    |_________________________|      | |  FREE  |                 \n");
    printf("    |  _  |     __      |  _  |      | |________|                 \n");
    printf("    | |_| |    |  |     | |_| |      | |                          \n");
    printf(" ___|_____|____|__|_____|_____|______|_|_________________________\n");
    printf("|                                                                |\n");
    printf("|                                                                |\n");
    printf("|________________________________________________________________|\n");
    printf("                                                                  \n");
    printf("|================================================================|\n");
    printf("    ||      ||      ||      ||      ||      ||      ||      ||    \n");
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
    printf("|       RAILWAY STATION DASHBOARD - SPRINT 3        |\n");
    printf(" \\__________________________________________________/ \n");

    // USAC15 - Dynamic Drawing based on Track 1 State
    if (num_tracks > 0) {
        switch(tracks[0].state) {
            case STATE_FREE:     draw_main_station_FREE(); break;
            case STATE_BUSY:     draw_main_station_BUSY(); break;
            case STATE_ASSIGNED: draw_main_station_ASSIGNED(); break;
            case STATE_INOP:     draw_main_station_INOP(); break;
            default:             draw_main_station_FREE(); break;
        }
    } else {
        draw_main_station_FREE();
    }

    printf("\n");

    // --- UNIFIED CONTROL PANEL (Remains same) ---
    draw_line(62);
    if (sensorData && sensorData->valid) {
        printf("| SENSORS | TEMP: %-3d %-5s | HUM: %-3d %-10s           |\n",
               sensorData->temperature, sensorData->temp_unit,
               sensorData->humidity, sensorData->hum_unit);
    } else {
        printf("| SENSORS | [!] WAITING FOR DATA...                        |\n");
    }
    draw_line(62);
    printf("| ID  | STATUS      | TRAIN | VISUAL SIGNAL                |\n");
    draw_line(62);

    for (int i = 0; i < num_tracks; i++) {
        char state_str[15];
        char visual[30];

        switch(tracks[i].state) {
            case STATE_FREE:     strcpy(state_str, "FREE");     strcpy(visual, "[  ]"); break;
            case STATE_BUSY:     strcpy(state_str, "BUSY");     strcpy(visual, "[##]"); break;
            case STATE_ASSIGNED: strcpy(state_str, "ASSIGNED"); strcpy(visual, "[--]"); break;
            case STATE_INOP:     strcpy(state_str, "INOP");     strcpy(visual, "[XX]"); break;
            default:             strcpy(state_str, "ERR");      strcpy(visual, "[??]");
        }

        char train_info[10];
        if (tracks[i].train_id != -1) sprintf(train_info, "T-%d", tracks[i].train_id);
        else strcpy(train_info, " -- ");

        printf("| #%-2d | %-11s | %-5s | %-28s |\n",
               tracks[i].id, state_str, train_info, visual);
    }
    draw_line(62);
    printf("\n[Press ENTER to refresh...]");
}