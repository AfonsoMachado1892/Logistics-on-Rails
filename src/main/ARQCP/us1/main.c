#include <string.h>  
#include "../unity.h"
#include "encrypt_data.h"

int callfunc(int (*f)(char* in, int key, char* out), char* in, int key, char* out);  

void setUp(void) {
    // set stuff up here
}

void tearDown(void) {
    // clean stuff up here
}

void run_test(char* input, int key, int exp_res, char* exp_out)
{
    int res; 
    char out[100]; 
    memset(out, '@', sizeof(out)); 
    res = callfunc(encrypt_data, input, key, &out[1]); 
    TEST_ASSERT_EQUAL_INT(exp_res, res); 
    if (exp_res == 1) { 		
        TEST_ASSERT_EQUAL_STRING(exp_out, &out[1]); 
        TEST_ASSERT_EQUAL_INT8('@', out[strlen(exp_out) + 2]); // end of string sentinel  
    } else {
        TEST_ASSERT_EQUAL_INT8('\0', out[1]); // empty string on failure
    }
    TEST_ASSERT_EQUAL_INT8('@', out[0]); // sentinel
}

// Testes de sucesso
void test_BasicEncryption()
{ 
    run_test("ABC", 1, 1, "BCD");  
}

void test_FullAlphabet()
{ 
    run_test("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 1, 1, "BCDEFGHIJKLMNOPQRSTUVWXYZA"); 
}

void test_WrapAround()
{ 
    run_test("XYZ", 3, 1, "ABC"); 
}

void test_KeyEqualsOne()
{ 
    run_test("HELLO", 1, 1, "IFMMP"); 
}

void test_KeyEquals13()
{ 
    run_test("HELLO", 13, 1, "URYYB"); // ROT13
}

void test_KeyEquals26()
{ 
    run_test("ABCXYZ", 26, 1, "ABCXYZ"); // no change with key=26
}

void test_SingleChar()
{ 
    run_test("A", 5, 1, "F"); 
}

void test_EmptyString()
{ 
    run_test("", 5, 1, ""); 
}

void test_LongString()
{ 
    run_test("THEQUICKBROWNFOXJUMPSOVERTHELAZYDOG", 3, 1, "WKHTXLFNEURZQIRAMXPSVRYHUWKHODCBGRJ"); 
}

// Testes de falha - key inválido
void test_KeyZero()
{ 
    run_test("ABC", 0, 0, ""); 
}

void test_KeyNegative()
{ 
    run_test("ABC", -5, 0, ""); 
}

void test_KeyGreaterThan26()
{ 
    run_test("ABC", 27, 0, ""); 
}

void test_KeyGreaterThan26_Large()
{ 
    run_test("ABC", 100, 0, ""); 
}

// Testes de falha - caracteres inválidos
void test_LowercaseLetters()
{ 
    run_test("abc", 3, 0, ""); 
}

void test_MixedCase()
{ 
    run_test("AbC", 3, 0, ""); 
}

void test_WithSpaces()
{ 
    run_test("AB C", 3, 0, ""); 
}

void test_WithNumbers()
{ 
    run_test("ABC123", 3, 0, ""); 
}

void test_WithSpecialChars()
{ 
    run_test("ABC!", 3, 0, ""); 
}

void test_CharBeforeA()
{ 
    run_test("@BC", 3, 0, ""); 
}

void test_CharAfterZ()
{ 
    run_test("AB[", 3, 0, ""); 
}

int main()
{
    UNITY_BEGIN();
    
    // Success tests
    RUN_TEST(test_BasicEncryption);
    RUN_TEST(test_FullAlphabet);
    RUN_TEST(test_WrapAround);
    RUN_TEST(test_KeyEqualsOne);
    RUN_TEST(test_KeyEquals13);
    RUN_TEST(test_KeyEquals26);
    RUN_TEST(test_SingleChar);
    RUN_TEST(test_EmptyString);
    RUN_TEST(test_LongString);
    
    // Failure tests - invalid key
    RUN_TEST(test_KeyZero);
    RUN_TEST(test_KeyNegative);
    RUN_TEST(test_KeyGreaterThan26);
    RUN_TEST(test_KeyGreaterThan26_Large);
    
    // Failure tests - invalid characters
    RUN_TEST(test_LowercaseLetters);
    RUN_TEST(test_MixedCase);
    RUN_TEST(test_WithSpaces);
    RUN_TEST(test_WithNumbers);
    RUN_TEST(test_WithSpecialChars);
    RUN_TEST(test_CharBeforeA);
    RUN_TEST(test_CharAfterZ);
    
    return UNITY_END();  
}