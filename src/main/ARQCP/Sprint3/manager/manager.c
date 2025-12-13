#include "sensors_comm.h"

// No loop principal do Manager (conforme o algoritmo fornecido):
void manager_main_loop() {
    SensorData sensors;

    while (1) {
        // 1. Request data from Sensors
        if (get_sensor_data("/dev/ttyACM0", &sensors)) {
            // 2. Update sensors data
            update_sensors_data(&sensors);
        }

        // 3. Wait for instructions from UI
        // ... resto do código
    }
}