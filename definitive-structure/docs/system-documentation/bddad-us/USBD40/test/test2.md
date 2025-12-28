**2. Teste de Falha por Falta de Stock (Estação Vazia)**

+ O que é testado: O impedimento da criação de um *Freight* numa estação que não possui vagões do modelo solicitado.

**Importância:** Garante que não é possível iniciar transportes a partir de locais sem recursos físicos (Validação de Regra de Negócio).

**Como funciona:**

+ Tenta criar um Freight a partir da estação de Valença (ID 11), que não possui vagões deste modelo.

+ Chama o procedimento *pr_register_freight*.

+ O procedimento deve verificar o stock, encontrar zero e lançar a exceção ORA-20012.

+ Para o teste passar, o erro -20012 deve ser capturado.