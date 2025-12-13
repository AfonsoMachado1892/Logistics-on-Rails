#ifndef SENSORS_COMM_H
#define SENSORS_COMM_H

#include "../common/domain.h" // Atualizado para usar a struct partilhada

// (Remove a definição struct SensorData daqui se ela existir)

int get_sensor_data(const char* port, SensorData* data);
void display_sensor_data(const SensorData* data);

#endif