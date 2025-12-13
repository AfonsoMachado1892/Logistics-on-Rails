#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "../common/domain.h"
#include "sensors_comm.h"
#include "setup.h"
#include "../board/board.h"

// Definição das Variáveis Globais
User* users = NULL;
int num_users = 0;
Track* tracks = NULL;
int num_tracks = 0;
SystemConfig sys_config;

void manager_main_loop() {
    SensorData sensors;

    // USAC11: Setup Inicial
    load_initial_setup("config.txt");

    while (1) {
        // 1. Obter dados dos sensores (USAC13)
        if (get_sensor_data("/dev/ttyACM0", &sensors)) {
            // Sucesso
        } else {
            sensors.valid = 0;
        }

        // 2. Atualizar o Board (USAC15)
        update_board_view(tracks, num_tracks, &sensors);

        // 3. Simular espera por comandos (USAC16 - Input placeholder)
        // Aqui irias ler o teclado para gerir as tracks
        getchar();

        usleep(100000); // 100ms
    }

    if(users) free(users);
    if(tracks) free(tracks);
}

int main() {
    manager_main_loop();
    return 0;
}