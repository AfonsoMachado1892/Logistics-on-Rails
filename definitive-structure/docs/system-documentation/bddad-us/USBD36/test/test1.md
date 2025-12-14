**1. Teste de Inserção com Sucesso**

 - O que é testado: A capacidade do sistema de inserir um registo válido quando todos os dados estão corretos.

**Importância**: É o teste fundamental para garantir que a funcionalidade básica está operacional. Precisamos de confirmar que o procedimento não só corre sem erros, como efetivamente guarda os dados na tabela Building.

**Como funciona**:

* O bloco define variáveis com dados válidos (ex: Estação ID 50, Nome "Armazém Norte").

* Chama o procedimento *pr_add_building_to_facility*.

* Imediatamente a seguir, executa um SELECT à tabela Building procurando pelo nome que acabou de ser inserido.

* Se o SELECT retornar um ID, o teste imprime "SUCESSO". Se lançar a exceção NO_DATA_FOUND, o teste imprime "FALHA".