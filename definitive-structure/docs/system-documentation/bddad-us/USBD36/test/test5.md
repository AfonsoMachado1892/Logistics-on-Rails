**5. Teste de Múltiplas Inserções (Concorrência/Sequência)**

+   O que é testado: A capacidade de adicionar múltiplos edifícios à mesma estação consecutivamente.

**Importância:** Verifica se não existem restrições únicas incorretas (ex: assumir que uma estação só pode ter 1 edifício) e se a geração automática de IDs (IDENTITY/SEQUENCE) está a funcionar sem conflitos.

**Como funciona:**

+ Executa duas chamadas seguidas ao procedimento para a Estação ID 5 (ex: "Bilheteira" e "Cafetaria").

+ Faz uma contagem (COUNT) de edifícios na Estação 5.

+ Se o total for maior ou igual a 2, confirma que ambos foram persistidos corretamente.