**2. Teste de Stock Insuficiente (Regra de Negócio)**

+ O que é testado: A tentativa de criar um *Freight* a partir de uma estação que não possui vagões do modelo solicitado.

**Importância:** Implementa a regra de negócio crítica que impede o planeamento de transporte sem recursos (evita "falsos positivos" logísticos).

**Como funciona:**

+ Tenta criar um transporte a partir da estação de São Romão (ID 1), sabendo que esta não possui vagões (stock = 0).

+ Espera-se que o procedimento conte os vagões, encontre zero, e lance um erro.

+ No bloco EXCEPTION, verifica se o código é -20041 ("Quantidade insuficiente"). Se for, o teste é SUCESSO.