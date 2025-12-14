**3. Teste de Estação Inexistente (Integridade Referencial)**

+ O que é testado: O comportamento do sistema quando se tenta adicionar um edifício a uma estação que não existe na base de dados (ex: ID 9999).

**Importância**: É crucial para a integridade da base de dados. Não podem existir edifícios "órfãos" (sem estação válida). O sistema deve bloquear esta operação com uma mensagem clara, em vez de um erro genérico de SQL.

**Como funciona:**

+ O bloco tenta chamar o procedimento com o ID 9999.

+ O teste é desenhado para esperar um erro. Se o procedimento correr até ao fim sem erro, o teste falha (mensagem "FALHA").

+ O bloco EXCEPTION captura o erro. Se o código do erro for -20004 (o código personalizado definido para "Estação não encontrada"), o teste é considerado um SUCESSO.