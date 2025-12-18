#include <DHT.h>

/**
 * USAC10 - Integrated Sensors & LightSigns (Sprint 3)
 * Logic: One Pico handles GTH (Sensors) and CMD,XX (Lights)
 */

#define DHTPIN 28
#define DHTTYPE DHT11
#define MAX_TRACKS 10
#define BLINK_INTERVAL 500

// Base pins for Track 0
#define RED_LED_BASE 10
#define YELLOW_LED_BASE 11
#define GREEN_LED_BASE 12

DHT dht(DHTPIN, DHTTYPE);

// USAC10: Moving Median Buffer (Window size of 5)
int tempBuffer[5] = {0,0,0,0,0};
int humBuffer[5] = {0,0,0,0,0};
int bufIdx = 0;

typedef struct {
    int id;
    int redPin, yellowPin, greenPin;
    bool blinking;
    unsigned long lastBlink;
} Track;

Track tracks[MAX_TRACKS];

void setup() {
    Serial.begin(115200); // Must match Manager serial config
    dht.begin();
    for (int i = 0; i < MAX_TRACKS; i++) {
        tracks[i].id = i;
        tracks[i].redPin = RED_LED_BASE + (i * 3);
        tracks[i].yellowPin = YELLOW_LED_BASE + (i * 3);
        tracks[i].greenPin = GREEN_LED_BASE + (i * 3);
        tracks[i].blinking = false;

        pinMode(tracks[i].redPin, OUTPUT);
        pinMode(tracks[i].yellowPin, OUTPUT);
        pinMode(tracks[i].greenPin, OUTPUT);

        // Initial state: Free (Green On) [cite: 234]
        digitalWrite(tracks[i].redPin, LOW);
        digitalWrite(tracks[i].yellowPin, LOW);
        digitalWrite(tracks[i].greenPin, HIGH);
    }
}

// USAC10: Moving Median Implementation [cite: 358-359, 366]
int applyMovingMedian(int val, int* buf) {
    buf[bufIdx % 5] = val;
    int sorted[5];
    for(int i=0; i<5; i++) sorted[i] = buf[i];

    // Simple sort to find median
    for(int i=0; i<4; i++) {
        for(int j=i+1; j<5; j++) {
            if(sorted[i] > sorted[j]) {
                int t = sorted[i]; sorted[i] = sorted[j]; sorted[j] = t;
            }
        }
    }
    return sorted[2]; // Pick middle value
}

void process_gth() {
    int t = applyMovingMedian((int)dht.readTemperature(), tempBuffer);
    int h = applyMovingMedian((int)dht.readHumidity(), humBuffer);
    bufIdx++;

    // Required Format: TOKEN&unit:xxxx&value:xx#TOKEN&unit:xxxx&value:xx
    Serial.print("TEMP&unit:celsius&value:");
    Serial.print(t);
    Serial.print("#HUM&unit:percentage&value:");
    Serial.println(h);
}

void update_track(int id, int r, int y, int g, bool blink) {
    // Switch off all LEDs before setting new state
    digitalWrite(tracks[id].redPin, LOW);
    digitalWrite(tracks[id].yellowPin, LOW);
    digitalWrite(tracks[id].greenPin, LOW);

    tracks[id].blinking = false;
    digitalWrite(tracks[id].redPin, r);
    digitalWrite(tracks[id].yellowPin, y);
    digitalWrite(tracks[id].greenPin, g);
    tracks[id].blinking = blink;
}

void loop() {
    // USAC10: Handle Red Blinking (RB) without blocking [cite: 31-33]
    unsigned long now = millis();
    for (int i=0; i<MAX_TRACKS; i++) {
        if (tracks[i].blinking && (now - tracks[i].lastBlink >= BLINK_INTERVAL)) {
            tracks[i].lastBlink = now;
            digitalWrite(tracks[i].redPin, !digitalRead(tracks[i].redPin));
        }
    }

    if (Serial.available() > 0) {
        String cmd = Serial.readStringUntil('\n');
        cmd.trim();

        if (cmd == "GTH") {
            process_gth();
        } else {
            // Process light commands (Format: CMD,x) [cite: 363-364]
            int comma = cmd.indexOf(',');
            if (comma != -1) {
                String type = cmd.substring(0, comma);
                int id = cmd.substring(comma + 1).toInt();
                if (id >= 0 && id < MAX_TRACKS) {
                    if (type == "RE") update_track(id, HIGH, LOW, LOW, false);
                    else if (type == "YE") update_track(id, LOW, HIGH, LOW, false);
                    else if (type == "GE") update_track(id, LOW, LOW, HIGH, false);
                    else if (type == "RB") update_track(id, HIGH, LOW, LOW, true);
                }
            }
        }
    }
}