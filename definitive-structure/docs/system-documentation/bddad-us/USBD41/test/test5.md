**10. Teste de Integridade de Dados (Teste Crítico)**

+ O que é testado: Garantir que "Remover do Comboio" não significa "Apagar a Carga".

**Importância:** Este é o teste de segurança mais importante desta US. O Freight é uma entidade financeira/logística. Se o retiramos de um comboio, ele volta a ficar "em espera", mas não pode desaparecer da tabela Freight.

**Como funciona:**

+ Remove o Freight 2002 do Comboio 5421.

+ Verifica duas condições simultâneas:

+ Tabela Train_Freight: Contagem deve ser 0 (desassociado).

+ Tabela Freight: Contagem para o ID 2002 deve ser 1 (ainda existe).

+ O teste só passa se ambas as condições forem verdadeiras.