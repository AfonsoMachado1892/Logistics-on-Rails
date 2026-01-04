// New test_setup.c for USAC11
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "../common/domain.h"
#include "setup.h"
#include "unity.h"

extern User* users;
extern int num_users;
extern Track* tracks;
extern int num_tracks;
extern SystemConfig sys_config;
extern void init_track_struct(Track* t, int id, int state, int train_id);
extern int encrypt_data(char* in, int key, char *out);

void setUp(void) {
    users = NULL;
    num_users = 0;
    tracks = NULL;
    num_tracks = 0;
}

void tearDown(void) {
    if (users) free(users);
    if (tracks) free(tracks);
}

void test_LoadInitialSetup_DefaultsOnNoFile(void) {
    load_initial_setup("nonexistent.txt");

    TEST_ASSERT_EQUAL_INT(10, sys_config.temp_buf_len);
    TEST_ASSERT_EQUAL_INT(5, sys_config.temp_win_len);
    TEST_ASSERT_EQUAL_INT(10, sys_config.hum_buf_len);
    TEST_ASSERT_EQUAL_INT(5, sys_config.hum_win_len);
    TEST_ASSERT_EQUAL_INT(0, num_users);
    TEST_ASSERT_EQUAL_INT(0, num_tracks);
}

void test_LoadInitialSetup_ParseConfig(void) {
    // Create temp file
    FILE* fp = fopen("test_config.txt", "w");
    fprintf(fp, "SENSOR_TEMP,20,10\n");
    fprintf(fp, "SENSOR_HUM,30,15\n");
    fprintf(fp, "TRACK,1,0,-1\n");
    fprintf(fp, "TRACK,2,1,100\n");
    fprintf(fp, "USER,Admin,admin,PASS,2\n");
    fclose(fp);

    load_initial_setup("test_config.txt");

    TEST_ASSERT_EQUAL_INT(20, sys_config.temp_buf_len);
    TEST_ASSERT_EQUAL_INT(10, sys_config.temp_win_len);
    TEST_ASSERT_EQUAL_INT(30, sys_config.hum_buf_len);
    TEST_ASSERT_EQUAL_INT(15, sys_config.hum_win_len);

    TEST_ASSERT_EQUAL_INT(2, num_tracks);
    TEST_ASSERT_EQUAL_INT(1, tracks[0].id);
    TEST_ASSERT_EQUAL_INT(0, tracks[0].state);
    TEST_ASSERT_EQUAL_INT(-1, tracks[0].train_id);
    TEST_ASSERT_EQUAL_INT(2, tracks[1].id);
    TEST_ASSERT_EQUAL_INT(1, tracks[1].state);
    TEST_ASSERT_EQUAL_INT(100, tracks[1].train_id);

    TEST_ASSERT_EQUAL_INT(1, num_users);
    TEST_ASSERT_EQUAL_STRING("Admin", users[0].name);
    TEST_ASSERT_EQUAL_STRING("admin", users[0].username);
    TEST_ASSERT_EQUAL_INT(2, users[0].key);
    // Check encrypted pass: PASS +2 = RCUU
    TEST_ASSERT_EQUAL_STRING("RCUU", users[0].password);

    remove("test_config.txt");
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_LoadInitialSetup_DefaultsOnNoFile);
    RUN_TEST(test_LoadInitialSetup_ParseConfig);
    return UNITY_END();
}