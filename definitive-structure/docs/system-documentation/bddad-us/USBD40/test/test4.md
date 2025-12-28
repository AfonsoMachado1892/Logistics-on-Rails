**4. Teste de Falha por ID Duplicado**

+ O que é testado: A validação da unicidade do ID do Freight fornecido manualmente pelo utilizador.

**Importância:** O sistema deve garantir robustamente que não são criados Freights com IDs repetidos.

**Como funciona:**

+ Tenta registar um Freight usando o ID 2001, que já existe na base de dados.

+ Chama o procedimento *pr_register_freight*.

+ O procedimento deve validar a existência prévia do ID e lançar a exceção ORA-20011.

+ Para o teste passar, o erro de duplicidade deve ser capturado.