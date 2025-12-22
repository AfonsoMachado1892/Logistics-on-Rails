**2. Teste de Duplicação de ID (Integridade)**

+ O que é testado: A tentativa de registar um Modelo de Vagão com um ID que já existe na base de dados.

**Importância:** Garante a integridade da Chave Primária e impede a sobreposição acidental de dados de modelos já existentes.

**Como funciona:**

+ Tenta registar um modelo usando o ID 1104 (que já existe nos dados de *setup* do sistema).

+ Espera-se que o procedimento detete a duplicidade antes de tentar o INSERT.

+ No bloco EXCEPTION, verifica se o código de erro é -20031 ("Modelo já existe"). Se for, o teste é SUCESSO.