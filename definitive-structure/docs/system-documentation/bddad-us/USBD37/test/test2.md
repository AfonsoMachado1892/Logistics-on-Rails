**2. Teste de Validação de ID Duplicado**

+ O que é testado: A capacidade do sistema de rejeitar o registo de um modelo cujo ID já existe na base de dados.

**Importância:** Garante a integridade dos dados, impedindo a violação da Chave Primária através de uma mensagem de erro controlada e informativa.

**Como funciona:**

+ Tenta registar um modelo utilizando um ID que já existe no sistema (ID 1104).

+ Chama o procedimento *pr_register_wagon_model*.

+ O procedimento deve detetar a existência do ID e lançar a exceção personalizada ORA-20031.

+ Para o teste passar, o bloco de exceção deve capturar o código -20031 e imprimir a mensagem de sucesso do teste.