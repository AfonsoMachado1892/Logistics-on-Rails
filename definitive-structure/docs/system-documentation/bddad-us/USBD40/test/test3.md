**3. Teste de Falha por Quantidade Excessiva**

+ O que é testado: O impedimento da criação de um *Freight* quando a quantidade solicitada é superior à quantidade existente na estação.

**Importância:** Assegura que o stock não fica negativo e que o sistema valida a capacidade real da estação.

**Como funciona:**

+ Tenta solicitar 1000 vagões na estação de Leixões (ID 50), onde existe stock, mas insuficiente para o pedido.

+ Chama o procedimento *pr_register_freight*.

+ O procedimento deve comparar a quantidade pedida com o `COUNT` disponível e lançar a exceção ORA-20012.

+ Para o teste passar, o erro de quantidade insuficiente deve ser capturado.