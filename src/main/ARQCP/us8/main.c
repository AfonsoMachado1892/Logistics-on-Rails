#include <string.h>  
#include "../unity.h"  // <--- Corrigido para ir buscar na pasta anterior (../)
#include "sort_array.h" // <--- Incluído para declarar a função sort_array (necessário para callfunc)
// O ficheiro "asm.h" original não era necessário se tivermos o "sort_array.h"

// Protótipo da função wrapper que chama a função em Assembly
// Assumimos que esta função está definida em um ficheiro externo (ex: ../callfunc.c ou .s)
int callfunc ( int (*f)(int* ptr, int num, char order), int* ptr, int num, char order);  

void setUp(void) {
    // set stuff up here
}

void tearDown(void) {
    // clean stuff up here
}

// Função auxiliar para configurar o teste e verificar o resultado
void run_test(int * vec, int in_num, char order, int exp_res, int * exp_vec)
{
    int vec1[100];
    int res; 

    // setup 
    memset(vec1, 0x55, sizeof vec1);
     
	// Copia o vetor de entrada para vec1+1 (deixando um sentinel antes)
	memcpy(vec1+1, vec, in_num * sizeof(int));  
    
    // Chama a função sort_array através do wrapper callfunc
	res = callfunc(sort_array, vec1+1, in_num, order);
    
    TEST_ASSERT_EQUAL_INT(exp_res, res);    // Verifica o código de retorno (1 ou 0)
    
    // Verificações de Sentinel (para garantir que não houve escrita fora dos limites)
    TEST_ASSERT_EQUAL_INT(0x55555555, vec1[in_num+1]);    
    TEST_ASSERT_EQUAL_INT(0x55555555, vec1[0]);    
    
    // Verifica se o vetor está ordenado corretamente
    if (in_num != 0) 
    TEST_ASSERT_EQUAL_INT_ARRAY(exp_vec, vec1+1, in_num);
    
}

// --- Testes para Ordem Ascendente (order = 1) ---

void test_NullVector()
{ 
    run_test((int[]){0}, 0, 1, 0, (int[]){0}); // Length = 0 deve retornar 0
}
void test_One()
{ 
    run_test((int[]){1000}, 1, 1, 1, (int[]){1000}); 
}
void test_Zero()
{ 
    run_test((int[]){10, 0, 1}, 3, 1, 1, (int[]){0, 1, 10}); // Vetor com zero
}
void test_Three()
{ 
    run_test((int[]){-1, -3, -2}, 3, 1, 1, (int[]){-3, -2, -1}); // Vetor com negativos
}
void test_Five()
{ 
    run_test((int[]){2, 1, 1, 1, 1}, 5, 1, 1, (int[]){1, 1, 1, 1, 2}); // Valores repetidos
}

// --- Testes para Ordem Descendente (order = 0) ---

void test_NullVectorD()
{ 
    run_test((int[]){0}, 0, 0, 0, (int[]){0}); // Length = 0 deve retornar 0
}
void test_OneD()
{ 
    run_test((int[]){1000}, 1, 0, 1, (int[]){1000}); 
}
void test_ZeroD()
{ 
    run_test((int[]){10, 0, 1}, 3, 0, 1, (int[]){10, 1, 0}); // Descendente
}
void test_ThreeD()
{ 
    run_test((int[]){-1, -3, -2}, 3, 0, 1, (int[]){-1, -2, -3}); // Descendente com negativos
}
void test_FiveD()
{ 
    run_test((int[]){1, 1, 1, 1, 2}, 5, 0, 1, (int[]){2, 1, 1, 1, 1}); // Descendente, repetidos
}


int main()
{ 
    UNITY_BEGIN();
    RUN_TEST(test_NullVector);
    RUN_TEST(test_One);
    RUN_TEST(test_Zero);
    RUN_TEST(test_Three);
    RUN_TEST(test_Five);
    RUN_TEST(test_NullVectorD);
    RUN_TEST(test_OneD);
    RUN_TEST(test_ZeroD);
    RUN_TEST(test_ThreeD);
    RUN_TEST(test_FiveD);
    return UNITY_END();  
}