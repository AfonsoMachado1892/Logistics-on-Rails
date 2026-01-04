### **1. Teste de Associação com Sucesso**

* **O que é testado:** A capacidade do sistema de associar um frete existente a um comboio, garantindo que o elo de ligação na tabela `Train_Freight` é criado corretamente.

**Como funciona:**
* O bloco realiza uma limpeza preventiva (`DELETE`) para garantir que o frete `2007` não está associado a nenhum comboio antes de começar.
* Invoca a procedure `prc_associate_freight_to_train` passando o ID do frete e o ID do comboio.
* O teste é bem-sucedido se a procedure terminar sem erros, indicando que todos os vagões do frete estão agora logicamente ligados ao comboio.