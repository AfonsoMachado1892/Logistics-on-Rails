/**
 * USAC10 - Sensors Component
 * Railway Station Management System - Sprint 3
 *
 * Hardware: Raspberry Pi Pico + DHT22 Sensor
 * Purpose: Read temperature and humidity with Moving Median Filter
 */

#include <DHT.h>

// Pin configuration
#define DHTPIN 2
#define DHTTYPE DHT22

// Moving Median configuration
#define TEMP_BUFFER_SIZE 10
#define TEMP_MEDIAN_WINDOW 5
#define HUM_BUFFER_SIZE 10
#define HUM_MEDIAN_WINDOW 5

// Circular buffer structure
typedef struct {
    int buffer[20];  // Max size
    int length;
    int nelem;
    int tail;
    int head;
} CircularBuffer;

// Global variables
DHT dht(DHTPIN, DHTTYPE);
CircularBuffer tempBuffer = {{0}, TEMP_BUFFER_SIZE, 0, 0, 0};
CircularBuffer humBuffer = {{0}, HUM_BUFFER_SIZE, 0, 0, 0};

// Function prototypes from Sprint 2 (Assembly - adapted to C++ for Arduino)
int enqueue_value(CircularBuffer* cb, int value);
int move_n_to_array(CircularBuffer* cb, int n, int* array);
int sort_array(int* vec, int length, char order);
int median(int* vec, int length, int* me);

/**
 * Enqueue value into circular buffer
 * If full, removes oldest element
 * Returns 1 if buffer is full after insertion, 0 otherwise
 */
int enqueue_value(CircularBuffer* cb, int value) {
    if (cb->nelem == cb->length) {
        // Buffer full - remove oldest
        cb->tail = (cb->tail + 1) % cb->length;
        cb->nelem--;
    }

    cb->buffer[cb->head] = value;
    cb->head = (cb->head + 1) % cb->length;
    cb->nelem++;

    return (cb->nelem == cb->length) ? 1 : 0;
}

/**
 * Move n elements from buffer to array
 * Returns 1 if success, 0 if not enough elements
 */
int move_n_to_array(CircularBuffer* cb, int n, int* array) {
    if (cb->nelem < n) {
        return 0;
    }

    for (int i = 0; i < n; i++) {
        int idx = (cb->tail + i) % cb->length;
        array[i] = cb->buffer[idx];
    }

    return 1;
}

/**
 * Sort array
 * order: 1 for ascending, 0 for descending
 * Returns 1 if success, 0 if length <= 0
 */
int sort_array(int* vec, int length, char order) {
    if (length <= 0) return 0;

    // Bubble sort
    for (int i = 0; i < length - 1; i++) {
        for (int j = 0; j < length - i - 1; j++) {
            bool swap = (order == 1) ?
                (vec[j] > vec[j + 1]) :
                (vec[j] < vec[j + 1]);

            if (swap) {
                int temp = vec[j];
                vec[j] = vec[j + 1];
                vec[j + 1] = temp;
            }
        }
    }

    return 1;
}

/**
 * Calculate median of array
 * Sorts array and returns median value
 * Returns 1 if success, 0 if length <= 0
 */
int median(int* vec, int length, int* me) {
    if (length <= 0) return 0;

    if (!sort_array(vec, length, 1)) {
        return 0;
    }

    if (length % 2 == 0) {
        *me = (vec[length/2 - 1] + vec[length/2]) / 2;
    } else {
        *me = vec[length/2];
    }

    return 1;
}

/**
 * Apply Moving Median Filter
 * Returns filtered value
 */
int apply_moving_median(CircularBuffer* cb, int windowSize, int rawValue) {
    enqueue_value(cb, rawValue);

    if (cb->nelem < windowSize) {
        return rawValue;  // Not enough samples yet
    }

    int window[20];
    if (!move_n_to_array(cb, windowSize, window)) {
        return rawValue;
    }

    int medianValue;
    if (!median(window, windowSize, &medianValue)) {
        return rawValue;
    }

    return medianValue;
}

/**
 * Read temperature with Moving Median Filter
 * Returns temperature in Celsius
 */
int read_filtered_temperature() {
    float rawTemp = dht.readTemperature();

    if (isnan(rawTemp)) {
        return -999;  // Error value
    }

    int tempInt = (int)rawTemp;
    return apply_moving_median(&tempBuffer, TEMP_MEDIAN_WINDOW, tempInt);
}

/**
 * Read humidity with Moving Median Filter
 * Returns humidity in percentage
 */
int read_filtered_humidity() {
    float rawHum = dht.readHumidity();

    if (isnan(rawHum)) {
        return -999;  // Error value
    }

    int humInt = (int)rawHum;
    return apply_moving_median(&humBuffer, HUM_MEDIAN_WINDOW, humInt);
}

/**
 * Process GTH command and send response
 * Format: TEMP&unit:celsius&value:XX#HUM&unit:percentage&value:XX
 */
void process_gth_command() {
    int temp = read_filtered_temperature();
    int hum = read_filtered_humidity();

    if (temp == -999 || hum == -999) {
        Serial.println("ERROR:SENSOR_READ_FAILED");
        return;
    }

    Serial.print("TEMP&unit:celsius&value:");
    Serial.print(temp);
    Serial.print("#HUM&unit:percentage&value:");
    Serial.println(hum);
}

/**
 * Setup function - runs once
 */
void setup() {
    Serial.begin(115200);
    dht.begin();

    // Wait for serial connection
    while (!Serial) {
        delay(10);
    }

    Serial.println("SENSORS:READY");
}

/**
 * Main loop - waits for commands
 */
void loop() {
    if (Serial.available() > 0) {
        String command = Serial.readStringUntil('\n');
        command.trim();

        if (command.equals("GTH")) {
            process_gth_command();
        } else {
            Serial.println("ERROR:UNKNOWN_COMMAND");
        }
    }

    delay(100);
}