**4. Teste de Parâmetros Obrigatórios (Constraints)**

+   O que é testado: A tentativa de criar um edifício sem nome (NULL).

**Importância:** Regras de negócio e consistência de dados. Um edifício sem nome não é identificável. O procedimento deve validar os inputs antes de tentar inserir.

**Como funciona:**

+ Chama o procedimento passando NULL no parâmetro p_name.

+ Tal como no teste anterior, espera-se que o procedimento falhe.

+ O bloco de exceção verifica se o erro capturado é o -20003 (código para "Parâmetros Obrigatórios"). Se for, o sistema reagiu corretamente.