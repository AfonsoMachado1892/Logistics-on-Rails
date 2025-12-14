**2. Teste de Associação Inexistente (Lógica de Negócio)**

   + O que é testado: A tentativa de remover um Freight de um Comboio ao qual ele não pertence (embora ambos existam).

**Importância:** Previne erros operacionais. Se um utilizador tentar remover o Freight A do Comboio B, mas o Freight A está na verdade no Comboio C, o sistema deve avisar *Essa associação não existe*.

**Como funciona:**

+ Tenta remover o Freight 2006 do Comboio 5421 (sabendo que o 2006 está noutro comboio).

+ Espera-se que o procedimento lance um erro.

+ No bloco EXCEPTION, verifica se o código é -20011 ("Associação não encontrada"). Se for, o teste é SUCESSO.