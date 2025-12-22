**3. Teste de Quantidade Inválida**

+ O que é testado: A validação dos parâmetros numéricos, impedindo pedidos de vagões com quantidade zero ou negativa.

**Importância:** Previne erros de input que poderiam causar comportamentos inesperados no sistema ou *freights* vazios.

**Como funciona:**

+ Tenta chamar o procedimento solicitando 0 vagões.

+ Espera-se que a validação inicial do procedimento bloqueie a operação.

+ No bloco EXCEPTION, verifica se o código é -20040 ("Parâmetros inválidos"). Se for, o teste é SUCESSO.