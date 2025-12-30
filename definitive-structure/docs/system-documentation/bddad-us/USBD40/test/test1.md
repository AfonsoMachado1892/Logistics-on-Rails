**1. Teste de Registo com Stock Suficiente**

+ O que é testado: A criação de um *Freight* com ID manual e a associação de vagões quando a estação de origem possui stock suficiente.

**Importância:** Valida o fluxo principal da User Story: garantir que o transporte é registado com o ID fornecido pelo utilizador e apenas quando os recursos estão disponíveis.

**Como funciona:**

+ Identifica uma estação com stock (Leixões, ID 50) e fornece um ID manual novo (9001).

+ Chama o procedimento *pr_register_freight* solicitando 1 vagão do modelo 1104.

+ Verifica se o registo foi criado na tabela *Freight* com o ID 9001 e se a tabela *Freight_Wagon* tem 1 linha associada.

+ Para o teste passar, os dados têm de estar persistidos e as contagens corretas.