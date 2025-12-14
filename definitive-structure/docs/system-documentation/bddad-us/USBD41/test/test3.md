**3. Teste de IDs Inexistentes (Validação de Input)**

+   O que é testado: A tentativa de executar a operação com IDs puramente inventados (ex: Comboio 9999).

**Importância:** Robustez. O sistema não deve tentar executar DELETEs cegos na base de dados com IDs inválidos. Deve validar a existência antes da ação.

**Como funciona:**

+ Chama o procedimento com p_train_id = 9999 e p_freight_id = 8888.

+ O procedimento faz uma validação prévia (SELECT COUNT) e, ao ver que é 0, lança erro.

+ O teste captura o erro -20011 e confirma que o sistema bloqueou a operação inválida.