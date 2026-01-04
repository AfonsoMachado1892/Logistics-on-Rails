### **3. Teste de Restrição de Comprimento (Integração USBD33)**

* **O que é testado:** Se o sistema impede a associação de um frete caso o comprimento total dos seus vagões (somado ao que já está no comboio) exceda o limite máximo (`max_length_m`) definido para o comboio.

**Como funciona:**
* O teste altera temporariamente o limite de um comboio para um valor propositadamente baixo (ex: 10 metros).
* Tenta invocar a procedure para adicionar um frete cujos vagões ultrapassam esse limite.
* O teste é bem-sucedido se o sistema disparar um erro `ORA-20001` (proveniente do trigger de validação), impedindo a gravação dos dados.