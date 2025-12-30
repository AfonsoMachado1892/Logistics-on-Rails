**3. Teste de Validação de Input Nulo**

* **O que é testado:** A robustez da função quando é passado um valor NULL como parâmetro de nome.

**Como funciona:**
* **Teste 3a:** Tenta invocar a função passando `NULL`. Espera-se que o sistema bloqueie a operação.
* O sistema deve intercetar o valor nulo logo no início da função.
* O teste é um SUCESSO se o sistema retornar o erro -20001 com a mensagem "Facility name cannot be null or empty".