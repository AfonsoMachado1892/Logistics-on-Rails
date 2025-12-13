/**
 * USAC13 - Get Data from Sensors Component
 * Manager Component - Railway Station Management System - Sprint 3
 *
 * Purpose: Request and receive temperature/humidity data from Sensors component
 * Communication: Serial communication with Raspberry Pi Pico
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <errno.h>

// Assembly function from Sprint 2
extern int extract_data(char* str, char* token, char* unit, int* value);

// Sensor data structure
typedef struct {
    int temperature;
    char temp_unit[20];
    int humidity;
    char hum_unit[20];
    int valid;
} SensorData;

/**
 * Configure serial port for communication with Raspberry Pi Pico
 * Returns file descriptor on success, -1 on error
 */
int configure_serial_port(const char* port) {
    int fd = open(port, O_RDWR | O_NOCTTY | O_SYNC);
    if (fd < 0) {
        perror("Error opening serial port");
        return -1;
    }

    struct termios tty;
    if (tcgetattr(fd, &tty) != 0) {
        perror("Error getting terminal attributes");
        close(fd);
        return -1;
    }

    // Set baud rate to 115200
    cfsetospeed(&tty, B115200);
    cfsetispeed(&tty, B115200);

    // 8N1 mode
    tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8;
    tty.c_iflag &= ~IGNBRK;
    tty.c_lflag = 0;
    tty.c_oflag = 0;
    tty.c_cc[VMIN] = 0;
    tty.c_cc[VTIME] = 50;  // 5 seconds timeout

    tty.c_iflag &= ~(IXON | IXOFF | IXANY);
    tty.c_cflag |= (CLOCAL | CREAD);
    tty.c_cflag &= ~(PARENB | PARODD);
    tty.c_cflag &= ~CSTOPB;
    tty.c_cflag &= ~CRTSCTS;

    if (tcsetattr(fd, TCSANOW, &tty) != 0) {
        perror("Error setting terminal attributes");
        close(fd);
        return -1;
    }

    return fd;
}

/**
 * Send GTH command to Sensors component
 * Returns 1 on success, 0 on error
 */
int send_gth_command(int fd) {
    const char* cmd = "GTH\n";
    int n = write(fd, cmd, strlen(cmd));

    if (n < 0) {
        perror("Error writing to serial port");
        return 0;
    }

    // Give time for sensor to respond
    usleep(100000);  // 100ms

    return 1;
}

/**
 * Read response from Sensors component
 * Returns 1 on success, 0 on error
 */
int read_sensor_response(int fd, char* buffer, int buffer_size) {
    memset(buffer, 0, buffer_size);

    int total_bytes = 0;
    int max_attempts = 10;
    int attempts = 0;

    while (attempts < max_attempts) {
        int n = read(fd, buffer + total_bytes, buffer_size - total_bytes - 1);

        if (n < 0) {
            if (errno == EAGAIN || errno == EWOULDBLOCK) {
                attempts++;
                usleep(100000);  // Wait 100ms
                continue;
            }
            perror("Error reading from serial port");
            return 0;
        }

        if (n == 0) {
            attempts++;
            usleep(100000);
            continue;
        }

        total_bytes += n;

        // Check if we have a complete response (ends with newline)
        if (strchr(buffer, '\n') != NULL) {
            break;
        }

        attempts++;
    }

    if (total_bytes == 0) {
        fprintf(stderr, "No data received from sensor\n");
        return 0;
    }

    // Remove newline characters
    buffer[strcspn(buffer, "\r\n")] = 0;

    return 1;
}

/**
 * Parse sensor response and extract temperature and humidity
 * Format: TEMP&unit:celsius&value:XX#HUM&unit:percentage&value:XX
 * Returns 1 on success, 0 on error
 */
int parse_sensor_data(char* response, SensorData* data) {
    // Check for error responses
    if (strstr(response, "ERROR:") != NULL) {
        fprintf(stderr, "Sensor error: %s\n", response);
        data->valid = 0;
        return 0;
    }

    // Extract temperature
    if (!extract_data(response, "TEMP", data->temp_unit, &data->temperature)) {
        fprintf(stderr, "Failed to extract temperature data\n");
        data->valid = 0;
        return 0;
    }

    // Extract humidity
    if (!extract_data(response, "HUM", data->hum_unit, &data->humidity)) {
        fprintf(stderr, "Failed to extract humidity data\n");
        data->valid = 0;
        return 0;
    }

    data->valid = 1;
    return 1;
}

/**
 * USAC13 - Get data from Sensors component
 * Main function that orchestrates the sensor data retrieval
 *
 * @param port Serial port path (e.g., "/dev/ttyACM0")
 * @param data Pointer to SensorData structure to store results
 * @return 1 on success, 0 on error
 */
int get_sensor_data(const char* port, SensorData* data) {
    int fd;
    char response[256];

    // Initialize data structure
    data->valid = 0;
    data->temperature = 0;
    data->humidity = 0;
    memset(data->temp_unit, 0, sizeof(data->temp_unit));
    memset(data->hum_unit, 0, sizeof(data->hum_unit));

    // Open and configure serial port
    fd = configure_serial_port(port);
    if (fd < 0) {
        return 0;
    }

    // Send GTH command
    if (!send_gth_command(fd)) {
        close(fd);
        return 0;
    }

    // Read response
    if (!read_sensor_response(fd, response, sizeof(response))) {
        close(fd);
        return 0;
    }

    // Parse sensor data
    if (!parse_sensor_data(response, data)) {
        close(fd);
        return 0;
    }

    close(fd);
    return 1;
}

/**
 * Display sensor data
 */
void display_sensor_data(const SensorData* data) {
    if (!data->valid) {
        printf("No valid sensor data available\n");
        return;
    }

    printf("\n=== Sensor Data ===\n");
    printf("Temperature: %d %s\n", data->temperature, data->temp_unit);
    printf("Humidity: %d %s\n", data->humidity, data->hum_unit);
    printf("==================\n\n");
}

// Example usage and test function
#ifdef USAC13_TEST
int main(int argc, char* argv[]) {
    const char* port = "/dev/ttyACM0";  // Default port

    if (argc > 1) {
        port = argv[1];
    }

    printf("USAC13 - Get Data from Sensors Component\n");
    printf("Connecting to: %s\n\n", port);

    SensorData data;

    // Get sensor data
    if (get_sensor_data(port, &data)) {
        display_sensor_data(&data);
        return 0;
    } else {
        fprintf(stderr, "Failed to get sensor data\n");
        return 1;
    }
}
#endif