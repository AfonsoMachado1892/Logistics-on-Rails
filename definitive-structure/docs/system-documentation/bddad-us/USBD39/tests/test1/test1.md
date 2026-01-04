# Documentação de Teste - Associação de Locomotiva a Comboio

## **1. Teste de Associação com Sucesso (Ajuste Temporário de Origem)**

### **O que é testado**
A capacidade do sistema de associar uma locomotiva existente a um comboio, garantindo que o elo de ligação na tabela `Train_Locomotive` é criado corretamente, mesmo quando é necessário ajustar temporariamente a instalação de partida do comboio para corresponder a uma linha válida onde a locomotiva opera.

### **Como funciona**

1. **Preparação do ambiente:**
    - Define a locomotiva `5621` e o comboio `5435` como alvos do teste
    - Ajusta temporariamente a instalação de partida do comboio para Contumil (ID 13), que é o início da Linha 3, garantindo compatibilidade com a linha operacional da locomotiva

2. **Limpeza preventiva:**
    - O bloco realiza uma limpeza preventiva (`DELETE`) para garantir que a locomotiva `5621` não está associada ao comboio `5435` antes de começar

3. **Execução do teste:**
    - Invoca a procedure `prc_associate_locomotive_to_train` passando o número da locomotiva e o ID do comboio
    - O teste é bem-sucedido se a procedure terminar sem erros, indicando que a locomotiva está agora logicamente ligada ao comboio

4. **Reversão das alterações:**
    - Executa `ROLLBACK` para desfazer tanto a alteração temporária da instalação de partida quanto a inserção na tabela de associação, mantendo a base de dados no estado original

### **Objetivo do teste**
Validar que o procedimento de associação funciona corretamente quando existe compatibilidade entre a linha operacional da locomotiva e a instalação de partida do comboio, mesmo que seja necessário simular essa compatibilidade temporariamente para fins de teste.

### **Resultado esperado**
Mensagem de confirmação: `"Associação realizada com sucesso usando Contumil (ID 13) como origem."`