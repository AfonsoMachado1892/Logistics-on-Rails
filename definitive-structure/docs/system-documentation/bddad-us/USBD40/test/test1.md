**1. Teste de Registo com Stock Suficiente (Caminho Feliz)**

+ O que é testado: A criação de um *Freight* e a associação de vagões quando a estação de origem possui stock suficiente do modelo solicitado.

**Importância:** Valida o fluxo principal da User Story: garantir que o transporte é registado apenas quando os recursos (vagões) estão disponíveis fisicamente na origem.

**Como funciona:**

+ Identifica uma estação com vagões disponíveis (Leixões, ID 50) e solicita 2 vagões do modelo 1104.

+ Chama o procedimento *pr_register_freight*.

+ Verifica se o registo foi criado na tabela *Freight* e se existem 2 linhas na tabela *Freight_Wagon* para esse novo ID.

+ Para o teste passar, os dados têm de estar persistidos na base de dados.