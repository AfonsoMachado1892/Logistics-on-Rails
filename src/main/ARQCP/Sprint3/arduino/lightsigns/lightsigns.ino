/**
 * USAC10 - LightSigns Component
 * Railway Station Management System - Sprint 3
 *
 * Hardware: Raspberry Pi Pico + LEDs
 * Purpose: Control track state LEDs (Red, Yellow, Green, Red Blinking)
 */

// Pin configuration (3 LEDs per track, up to 99 tracks)
// This example supports 10 tracks
#define MAX_TRACKS 10
#define RED_LED_BASE 3    // Pins 3, 6, 9, 12, ...
#define YELLOW_LED_BASE 4 // Pins 4, 7, 10, 13, ...
#define GREEN_LED_BASE 5  // Pins 5, 8, 11, 14, ...

// Track state enumeration
enum TrackState {
    STATE_FREE,           // Green
    STATE_BUSY,           // Red
    STATE_ASSIGNED,       // Yellow
    STATE_INOPERATIVE,    // Red Blinking
    STATE_OFF             // All off
};

// Track structure
typedef struct {
    int id;
    TrackState state;
    int redPin;
    int yellowPin;
    int greenPin;
    bool blinking;
    unsigned long lastBlinkTime;
} Track;

// Global variables
Track tracks[MAX_TRACKS];
int numTracks = 10;
const unsigned long BLINK_INTERVAL = 500; // 500ms for blinking

/**
 * Initialize track structure
 */
void init_track(int trackId) {
    if (trackId < 0 || trackId >= MAX_TRACKS) return;

    tracks[trackId].id = trackId;
    tracks[trackId].state = STATE_OFF;
    tracks[trackId].redPin = RED_LED_BASE + (trackId * 3);
    tracks[trackId].yellowPin = YELLOW_LED_BASE + (trackId * 3);
    tracks[trackId].greenPin = GREEN_LED_BASE + (trackId * 3);
    tracks[trackId].blinking = false;
    tracks[trackId].lastBlinkTime = 0;

    // Configure pins as output
    pinMode(tracks[trackId].redPin, OUTPUT);
    pinMode(tracks[trackId].yellowPin, OUTPUT);
    pinMode(tracks[trackId].greenPin, OUTPUT);
}

/**
 * Turn off all LEDs of a track
 */
void turn_off_all_leds(int trackId) {
    if (trackId < 0 || trackId >= numTracks) return;

    digitalWrite(tracks[trackId].redPin, LOW);
    digitalWrite(tracks[trackId].yellowPin, LOW);
    digitalWrite(tracks[trackId].greenPin, LOW);
    tracks[trackId].blinking = false;
}

/**
 * Set track to Red state (Busy)
 */
void set_track_red(int trackId) {
    if (trackId < 0 || trackId >= numTracks) return;

    turn_off_all_leds(trackId);
    digitalWrite(tracks[trackId].redPin, HIGH);
    tracks[trackId].state = STATE_BUSY;
}

/**
 * Set track to Yellow state (Assigned)
 */
void set_track_yellow(int trackId) {
    if (trackId < 0 || trackId >= numTracks) return;

    turn_off_all_leds(trackId);
    digitalWrite(tracks[trackId].yellowPin, HIGH);
    tracks[trackId].state = STATE_ASSIGNED;
}

/**
 * Set track to Green state (Free)
 */
void set_track_green(int trackId) {
    if (trackId < 0 || trackId >= numTracks) return;

    turn_off_all_leds(trackId);
    digitalWrite(tracks[trackId].greenPin, HIGH);
    tracks[trackId].state = STATE_FREE;
}

/**
 * Set track to Red Blinking state (Inoperative)
 */
void set_track_red_blinking(int trackId) {
    if (trackId < 0 || trackId >= numTracks) return;

    turn_off_all_leds(trackId);
    tracks[trackId].blinking = true;
    tracks[trackId].state = STATE_INOPERATIVE;
    tracks[trackId].lastBlinkTime = millis();
    digitalWrite(tracks[trackId].redPin, HIGH);
}

/**
 * Parse command string
 * Format: CMD,XX where CMD is RE/YE/GE/RB and XX is track number (00-99)
 * Returns: 1 if valid, 0 if invalid
 */
int parse_command(String cmd, char* command, int* trackNum) {
    cmd.trim();

    if (cmd.length() < 4 || cmd.length() > 5) {
        return 0;
    }

    int commaIndex = cmd.indexOf(',');
    if (commaIndex != 2) {
        return 0;
    }

    String cmdPart = cmd.substring(0, commaIndex);
    String numPart = cmd.substring(commaIndex + 1);

    cmdPart.toUpperCase();

    if (cmdPart != "RE" && cmdPart != "YE" &&
        cmdPart != "GE" && cmdPart != "RB") {
        return 0;
    }

    int num = numPart.toInt();
    if (num < 0 || num >= 100) {
        return 0;
    }

    // Copy command (2 chars + null terminator)
    command[0] = cmdPart.charAt(0);
    command[1] = cmdPart.charAt(1);
    command[2] = '\0';

    *trackNum = num;

    return 1;
}

/**
 * Process command from Manager
 * Format: CMD,XX
 */
void process_command(String cmd) {
    char command[3];
    int trackNum;

    if (!parse_command(cmd, command, &trackNum)) {
        Serial.println("ERROR:INVALID_COMMAND");
        return;
    }

    if (trackNum >= numTracks) {
        Serial.println("ERROR:INVALID_TRACK_NUMBER");
        return;
    }

    // Execute command
    if (strcmp(command, "RE") == 0) {
        set_track_red(trackNum);
        Serial.print("OK:TRACK_");
        Serial.print(trackNum);
        Serial.println("_RED");
    }
    else if (strcmp(command, "YE") == 0) {
        set_track_yellow(trackNum);
        Serial.print("OK:TRACK_");
        Serial.print(trackNum);
        Serial.println("_YELLOW");
    }
    else if (strcmp(command, "GE") == 0) {
        set_track_green(trackNum);
        Serial.print("OK:TRACK_");
        Serial.print(trackNum);
        Serial.println("_GREEN");
    }
    else if (strcmp(command, "RB") == 0) {
        set_track_red_blinking(trackNum);
        Serial.print("OK:TRACK_");
        Serial.print(trackNum);
        Serial.println("_RED_BLINKING");
    }
}

/**
 * Handle blinking LEDs
 * Must be called frequently in loop
 */
void handle_blinking() {
    unsigned long currentTime = millis();

    for (int i = 0; i < numTracks; i++) {
        if (tracks[i].blinking) {
            if (currentTime - tracks[i].lastBlinkTime >= BLINK_INTERVAL) {
                tracks[i].lastBlinkTime = currentTime;

                // Toggle red LED
                int currentState = digitalRead(tracks[i].redPin);
                digitalWrite(tracks[i].redPin, !currentState);
            }
        }
    }
}

/**
 * Setup function - runs once
 */
void setup() {
    Serial.begin(115200);

    // Initialize all tracks
    for (int i = 0; i < numTracks; i++) {
        init_track(i);
        turn_off_all_leds(i);
    }

    // Wait for serial connection
    while (!Serial) {
        delay(10);
    }

    Serial.println("LIGHTSIGNS:READY");
}

/**
 * Main loop - waits for commands and handles blinking
 */
void loop() {
    // Handle blinking LEDs
    handle_blinking();

    // Check for commands
    if (Serial.available() > 0) {
        String command = Serial.readStringUntil('\n');
        process_command(command);
    }

    delay(10);
}

/**
 * Alternative: Combined Sensors + LightSigns in one Pico
 * Uncomment the following section if you want both on same device
 */
/*
// You would need to merge both sketches:
// - Keep all LightSigns functions
// - Add DHT sensor code
// - Handle both "GTH" and "CMD,XX" commands in loop
// - Make sure Serial communication is synchronized

void loop() {
    handle_blinking();

    if (Serial.available() > 0) {
        String command = Serial.readStringUntil('\n');
        command.trim();

        if (command.equals("GTH")) {
            process_gth_command();  // From Sensors
        } else {
            process_command(command);  // From LightSigns
        }
    }

    delay(10);
}
*/