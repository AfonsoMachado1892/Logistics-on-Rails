**3. Teste de Validação de Inputs Nulos ou Vazios**

* **O que é testado:** A robustez da função perante entradas de dados inválidas, como valores `NULL` ou strings compostas apenas por espaços.

**Importância:** Impede a corrupção da base de dados com registos "fantasmas" ou sem nome, o que impossibilitaria a identificação das estações em consultas futuras ou no planeamento de rotas.

**Como funciona:**
* **Teste 3a:** Tenta invocar a função passando `NULL`. Espera-se que o sistema bloqueie a operação.
* **Teste 3b:** Tenta enviar uma string com espaços em branco ('   ').
* O teste é validado se o sistema retornar o código de erro `-20001`, confirmando que as validações de input estão ativas.