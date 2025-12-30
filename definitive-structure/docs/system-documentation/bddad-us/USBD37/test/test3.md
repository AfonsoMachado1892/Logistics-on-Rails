**3. Teste de Validação de Nome Duplicado (Case Insensitive)**

+ O que é testado: A validação de unicidade do nome do modelo, ignorando diferenças de maiúsculas/minúsculas e espaços.

**Importância:** Evita a criação de modelos duplicados por "preguiça" ou erro de formatação (ex: "Modelo X" vs "modelo x").

**Como funciona:**

+ Tenta registar um novo ID (8888), mas usa um nome que já existe (ex: "regmms...") com formatação diferente (espaços e minúsculas).

+ Chama o procedimento *pr_register_wagon_model*.

+ O procedimento deve normalizar o nome (UPPER/TRIM), detetar a duplicação e lançar a exceção ORA-20033.

+ Para o teste passar, o erro -20033 deve ser capturado corretamente.