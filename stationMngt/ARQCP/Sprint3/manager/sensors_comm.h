#ifndef SENSORS_COMM_H
#define SENSORS_COMM_H

#include "../common/domain.h"

// Função para obter dados dos sensores (Temperatura/Humidade)
int get_sensor_data(const char* port, SensorData* data);

// Função para enviar comandos para os LightSigns (USAC16)
// Retorna 1 se sucesso, 0 se erro
int send_light_command(const char* port, const char* cmd);

void display_sensor_data(const SensorData* data);

#endif
