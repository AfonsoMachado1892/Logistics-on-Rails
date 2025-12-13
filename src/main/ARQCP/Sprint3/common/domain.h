#ifndef DOMAIN_H
#define DOMAIN_H

// Estrutura para Utilizador
typedef struct {
    char name[50];
    char username[20];
    char password[20];
    int key;
} User;

// Estrutura para Track
// A ordem dos campos deve corresponder aos offsets no Assembly (0, 4, 8)
typedef struct {
    int id;
    int state;     // 0=Livre, 1=Ocupado, 2=Reservado, 3=Inoperativo
    int train_id;
} Track;

// Estrutura para Dados dos Sensores
typedef struct {
    int temperature;
    char temp_unit[20];
    int humidity;
    char hum_unit[20];
    int valid;
} SensorData;

// Configuração do Sistema
typedef struct {
    int temp_buf_len;
    int temp_win_len;
    int hum_buf_len;
    int hum_win_len;
} SystemConfig;

#endif