
/**
 * Manager Component - Sensors & LightSigns Communication
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <errno.h>
#include "sensors_comm.h"

// Assembly function from Sprint 2
extern int extract_data(char* str, char* token, char* unit, int* value);

/**
 * Configure serial port for communication
 */
int configure_serial_port(const char* port) {
    int fd = open(port, O_RDWR | O_NOCTTY | O_SYNC);
    if (fd < 0) {
        // perror("Error opening serial port"); // Comentado para não poluir UI se desligado
        return -1;
    }

    struct termios tty;
    if (tcgetattr(fd, &tty) != 0) {
        close(fd);
        return -1;
    }

    cfsetospeed(&tty, B115200);
    cfsetispeed(&tty, B115200);

    tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8;
    tty.c_iflag &= ~IGNBRK;
    tty.c_lflag = 0;
    tty.c_oflag = 0;
    tty.c_cc[VMIN] = 0;
    tty.c_cc[VTIME] = 50; // 5 seconds timeout

    tty.c_iflag &= ~(IXON | IXOFF | IXANY);
    tty.c_cflag |= (CLOCAL | CREAD);
    tty.c_cflag &= ~(PARENB | PARODD);
    tty.c_cflag &= ~CSTOPB;
    tty.c_cflag &= ~CRTSCTS;

    if (tcsetattr(fd, TCSANOW, &tty) != 0) {
        close(fd);
        return -1;
    }

    return fd;
}

/**
 * Send GTH command to Sensors
 */
int send_gth_command(int fd) {
    const char* cmd = "GTH\n";
    int n = write(fd, cmd, strlen(cmd));
    if (n < 0) return 0;
    usleep(100000); 
    return 1;
}

/**
 * Read response from Sensors
 */
int read_sensor_response(int fd, char* buffer, int buffer_size) {
    memset(buffer, 0, buffer_size);
    int total_bytes = 0;
    int max_attempts = 5; // Reduced attempts for faster loops
    int attempts = 0;

    while (attempts < max_attempts) {
        int n = read(fd, buffer + total_bytes, buffer_size - total_bytes - 1);

        if (n < 0) {
            if (errno == EAGAIN || errno == EWOULDBLOCK) {
                attempts++;
                usleep(50000); 
                continue;
            }
            return 0;
        }

        if (n == 0) {
            attempts++;
            usleep(50000);
            continue;
        }

        total_bytes += n;
        if (strchr(buffer, '\n') != NULL) break;
    }

    if (total_bytes == 0) return 0;

    buffer[strcspn(buffer, "\r\n")] = 0;
    return 1;
}

int parse_sensor_data(char* response, SensorData* data) {
    if (strstr(response, "ERROR:") != NULL) {
        data->valid = 0;
        return 0;
    }
    if (!extract_data(response, "TEMP", data->temp_unit, &data->temperature)) {
        data->valid = 0;
        return 0;
    }
    if (!extract_data(response, "HUM", data->hum_unit, &data->humidity)) {
        data->valid = 0;
        return 0;
    }
    data->valid = 1;
    return 1;
}

// USAC13 - Get Data
int get_sensor_data(const char* port, SensorData* data) {
    int fd;
    char response[256];

    data->valid = 0;
    data->temperature = 0;
    data->humidity = 0;
    memset(data->temp_unit, 0, sizeof(data->temp_unit));
    memset(data->hum_unit, 0, sizeof(data->hum_unit));

    fd = configure_serial_port(port);
    if (fd < 0) return 0;

    if (!send_gth_command(fd)) { close(fd); return 0; }
    if (!read_sensor_response(fd, response, sizeof(response))) { close(fd); return 0; }
    if (!parse_sensor_data(response, data)) { close(fd); return 0; }

    close(fd);
    return 1;
}

// USAC16 / USAC14 - Send Light Command
int send_light_command(const char* port, const char* cmd) {
    int fd = configure_serial_port(port);
    if (fd < 0) return 0;

    char cmd_with_newline[32];
    snprintf(cmd_with_newline, sizeof(cmd_with_newline), "%s\n", cmd);

    int n = write(fd, cmd_with_newline, strlen(cmd_with_newline));
    
    // Give slight delay for processing
    usleep(50000); 

    close(fd);
    return (n > 0) ? 1 : 0;
}

void display_sensor_data(const SensorData* data) {
    (void)data; // This tells the compiler the parameter is intentionally unused
    // Helper function used in tests
}