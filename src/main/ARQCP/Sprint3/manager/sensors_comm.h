#ifndef SENSORS_COMM_H
#define SENSORS_COMM_H

typedef struct {
    int temperature;
    char temp_unit[20];
    int humidity;
    char hum_unit[20];
    int valid;
} SensorData;

int get_sensor_data(const char* port, SensorData* data);
void display_sensor_data(const SensorData* data);

#endif