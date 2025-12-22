**3. Teste de Parâmetros Obrigatórios**

+ O que é testado: A validação de dados de entrada, impedindo o registo de modelos com campos obrigatórios a NULL (ID, Nome ou Bitola).

**Importância:** Assegura a qualidade dos dados (Data Quality), evitando registos incompletos ou "lixo" na base de dados.

**Como funciona:**

+ Tenta chamar o procedimento passando NULL nos parâmetros *p_model_id* e *p_model_name*.

+ Espera-se que o procedimento lance um erro de validação.

+ No bloco EXCEPTION, verifica se o código de erro é -20030 ("Parâmetros obrigatórios"). Se for, o teste é SUCESSO.