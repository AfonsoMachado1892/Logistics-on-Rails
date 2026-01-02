#include <string.h>
#include <stdlib.h>
#include "../common/domain.h"
#include "unity.h"
#include "unity_internals.h"

// --- MOCKS E DECLARAÇÕES EXTERNAS ---
// Precisamos declarar as variáveis globais e funções que vamos testar
// (Normalmente estariam no header, mas como o manager.c não expõe tudo, definimos aqui para o teste)

Track* tracks = NULL;
int num_tracks = 0;
User* users = NULL;
int num_users = 0;

// Funções Assembly (Mocks ou Reais se linkares os .s)
// Aqui assumimos que estás a linkar com os teus .s reais do Sprint 2/3
extern void update_track_struct(Track* t, int state, int train_id);
extern int encrypt_data(char* in, int key, char *out);
extern int decrypt_data(char* in, int key, char *out);

// --- SETUP & TEARDOWN ---

void setUp(void) {
    // Alocar espaço para testes
    num_tracks = 5;
    tracks = (Track*) malloc(num_tracks * sizeof(Track));
    // Reset tracks
    for(int i=0; i<num_tracks; i++) {
        tracks[i].id = i+1;
        tracks[i].state = 0; // FREE
        tracks[i].train_id = -1;
    }
}

void tearDown(void) {
    if(tracks) {
        free(tracks);
        tracks = NULL;
    }
}

// --- TESTES ---

// Teste para USAC16 e US Sprint 3 (update_track_struct em Assembly)
void test_AssignTrackToTrain_Should_UpdateStateAndTrainId(void) {
    int track_index = 0; // Track ID 1
    int train_id = 99;
    int expected_state = 2; // ASSIGNED/RESERVED

    // Action: Chama a função assembly
    update_track_struct(&tracks[track_index], expected_state, train_id);

    // Assert
    TEST_ASSERT_EQUAL_INT_MESSAGE(expected_state, tracks[track_index].state, "O estado da track devia ter mudado para 2 (Assigned)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(train_id, tracks[track_index].train_id, "O ID do comboio devia ser 99");
}

void test_FreeTrack_Should_ResetStateAndRemoveTrain(void) {
    int track_index = 1; // Track ID 2
    // Setup inicial: track ocupada
    tracks[track_index].state = 1;
    tracks[track_index].train_id = 50;

    // Action
    update_track_struct(&tracks[track_index], 0, -1); // 0 = FREE

    // Assert
    TEST_ASSERT_EQUAL_INT_MESSAGE(0, tracks[track_index].state, "A track devia estar livre (0)");
    TEST_ASSERT_EQUAL_INT_MESSAGE(-1, tracks[track_index].train_id, "O ID do comboio devia ser -1");
}

void test_SetTrackInoperative_Should_ChangeStateOnly(void) {
    int track_index = 2;
    tracks[track_index].train_id = -1;

    // Action
    update_track_struct(&tracks[track_index], 3, -1); // 3 = INOP

    // Assert
    TEST_ASSERT_EQUAL_INT_MESSAGE(3, tracks[track_index].state, "A track devia estar inoperativa (3)");
}

// Teste para USAC11 (Login/Encriptação) e USAC01
void test_PasswordEncryption_Should_MatchLogic(void) {
    char plain_pass[] = "ADMIN";
    int key = 1;
    char encrypted[20];
    char expected[] = "BENJO"; // A+1=B, D+1=E...

    // Action
    int res = encrypt_data(plain_pass, key, encrypted);

    // Assert
    TEST_ASSERT_EQUAL_INT(1, res);
    TEST_ASSERT_EQUAL_STRING_MESSAGE(expected, encrypted, "A encriptação Caesar Cipher falhou");
}

// Teste de integridade de memória (Offset da Struct)
// Isto garante que o Assembly e o C estão a aceder aos mesmos bytes
void test_StructMemoryLayout_Should_BeCompatibleWithAssembly(void) {
    Track t;
    t.id = 0xAAAAAAAA;
    t.state = 0xBBBBBBBB;
    t.train_id = 0xCCCCCCCC;

    // Se o Assembly espera offsets 0, 4, 8, vamos verificar se o compilador C gerou isso
    // Nota: Isto depende do compilador, mas em RV32IM deve ser 4 bytes int.

    int* p_int = (int*)&t;
    TEST_ASSERT_EQUAL_HEX32(0xAAAAAAAA, p_int[0]); // Offset 0
    TEST_ASSERT_EQUAL_HEX32(0xBBBBBBBB, p_int[1]); // Offset 4
    TEST_ASSERT_EQUAL_HEX32(0xCCCCCCCC, p_int[2]); // Offset 8
}

// --- MAIN DO TESTE ---

int main(void) {
    UNITY_BEGIN();

    RUN_TEST(test_AssignTrackToTrain_Should_UpdateStateAndTrainId);
    RUN_TEST(test_FreeTrack_Should_ResetStateAndRemoveTrain);
    RUN_TEST(test_SetTrackInoperative_Should_ChangeStateOnly);
    RUN_TEST(test_PasswordEncryption_Should_MatchLogic);
    RUN_TEST(test_StructMemoryLayout_Should_BeCompatibleWithAssembly);

    return UNITY_END();
}