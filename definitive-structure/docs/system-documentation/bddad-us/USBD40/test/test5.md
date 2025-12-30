**5. Teste de Falha por Parâmetros Inválidos**

+ O que é testado: A validação dos dados de entrada, especificamente quantidades negativas ou nulas.

**Importância:** Previne a corrupção de dados ou comportamentos inesperados nos loops de atribuição de vagões.

**Como funciona:**

+ Tenta registar um Freight solicitando uma quantidade de "-5" vagões.

+ Chama o procedimento *pr_register_freight*.

+ O procedimento deve validar se `p_qty > 0` e, caso contrário, lançar a exceção ORA-20010.

+ Para o teste passar, o erro de parâmetros inválidos deve ser capturado.