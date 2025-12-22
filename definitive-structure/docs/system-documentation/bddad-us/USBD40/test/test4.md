**4. Teste de Stock Insuficiente Parcial (Validação Matemática)**

+ O que é testado: A validação da quantidade disponível quando a estação possui *alguns* vagões do modelo, mas não em número suficiente para satisfazer o pedido total.

**Importância:** Garante que a verificação de stock não é apenas booleana ("tem/não tem"), mas sim uma validação aritmética correta ($Stock < Pedido$).

**Como funciona:**

+ Identifica uma estação que possui vagões do modelo 1104 (Leixões).

+ Chama o procedimento solicitando uma quantidade exagerada (ex: 1000), superior ao número real de vagões existentes.

+ Espera-se que o procedimento detete a insuficiência e lance um erro.

+ No bloco EXCEPTION, verifica se o código é -20041 ("Quantidade insuficiente"). Se for, o teste é SUCESSO.