**2. Teste de Tipologia Diferente (Validação de Dados)**

* O que é testado: A inserção de um edifício com um type (tipo) diferente (ex: "Silo" em vez de "Warehouse") numa estação diferente.

**Importância**: Garante que o sistema está adaptado para aceitar todos os tipos de edifício ou apenas uma estação específica. Valida a flexibilidade do procedimento.

**Como funciona:**

+ Chama o procedimento para a Estação ID 11 com o tipo "Silo".

+ Utiliza um SELECT COUNT(*) para verificar se existe exatamente 1 registo com essa combinação (Estação 11 + Nome + Tipo).

+ Se COUNT = 1, o teste é validado.