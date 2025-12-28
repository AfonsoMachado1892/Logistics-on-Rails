**Test 5: Validação rigorosa de normalização de nomes**

> - Testa a inserção de uma bitola com variações de maiúsculas/minúsculas e espaços adicionais (e.g., " bitola métrica ", "BITOLA MÉTRICA") quando esta já existe no sistema.
> - Verifica se a função retorna -3 (nome duplicado) e confirma que o registo redundante não foi criado, validando que a função normaliza os nomes para detetar duplicados disfarçados.
