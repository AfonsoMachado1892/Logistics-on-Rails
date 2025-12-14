**4. Teste de Parâmetros Nulos (Defesa)**
+ O que é testado: O envio de valores NULL para o ID do comboio ou da carga.

**Importância:** Evitar erros de SQL ou comportamentos indefinidos. A operação exige inequivocamente dois identificadores.

**Como funciona:**

+ Chama o procedimento com NULL num dos argumentos.

+ O teste valida se a exceção -20010 (definida para "Parâmetros Obrigatórios") é disparada.

+ Se o sistema não der erro, o teste falha, pois permitiu uma operação ilegal.