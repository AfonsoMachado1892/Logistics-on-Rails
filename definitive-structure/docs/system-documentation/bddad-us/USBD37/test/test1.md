**1. Teste de Registo com Sucesso (Múltiplas Bitolas)**

+ O que é testado: O registo correto de um novo Modelo de Vagão e a inserção das suas bitolas (padrão e adicionais) nas tabelas respetivas.

**Importância:** Verificar se a funcionalidade principal cumpre o objetivo: inserir os dados na tabela *Wagon_Model* e povoar corretamente a tabela de associação *Wagon_Model_Gauge*.

**Como funciona:**

+ Define os dados de um novo modelo (ID 9001) com bitola padrão 1668 mm e uma bitola extra 1435 mm.

+ Chama o procedimento *pr_register_wagon_model*.

+ Faz um SELECT COUNT(*) à tabela *Wagon_Model* (deve ser 1) e à tabela *Wagon_Model_Gauge* (deve ser 2, correspondendo às duas bitolas).

+ Para o teste passar, ambas as contagens têm de estar corretas.