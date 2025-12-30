**1. Teste de Registo com Sucesso**

+ O que é testado: O registo correto de um novo Modelo de Vagão com uma única bitola associada.

**Importância:** Verificar se a funcionalidade principal cumpre o objetivo: inserir os dados na tabela *Wagon_Model* e povoar corretamente a tabela de associação *Wagon_Model_Gauge* com apenas um registo de bitola.

**Como funciona:**

+ Define os dados de um novo modelo (ID 8001) e passa o ID da bitola (1668 mm) como parâmetro direto.

+ Chama o procedimento *pr_register_wagon_model*.

+ Verifica se o registo foi criado na tabela *Wagon_Model* e se existe exatamente 1 linha na tabela *Wagon_Model_Gauge* para este modelo.

+ Para o teste passar, a mensagem "SUCESSO" deve ser devolvida.
