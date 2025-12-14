**1. Teste de Remoção com Sucesso**

   + O que é testado: A remoção efetiva de uma associação válida entre um Freight e um Comboio.

**Importância:** Verificar se a funcionalidade principal cumpre o objetivo: eliminar a linha da tabela de ligação Train_Freight.

**Como funciona:**

+ Identifica uma ligação existente nos dados de teste (Comboio 5421 e Freight 2001).

+ Chama o procedimento *pr_remove_freight_from_train*.

+ Faz um SELECT COUNT(*) à tabela Train_Freight para esses IDs.

+ Para o teste passar, a contagem tem de ser 0 (zero), provando que o registo desapareceu.