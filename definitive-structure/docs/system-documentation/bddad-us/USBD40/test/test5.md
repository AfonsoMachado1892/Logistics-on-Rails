**5. Teste de Integridade Referencial (Destino Inexistente)**

+ O que é testado: A robustez da base de dados e do procedimento ao lidar com identificadores de estações de destino que não existem no sistema.

**Importância:** Verifica se as *Foreign Keys* estão ativas e se o procedimento propaga corretamente os erros de integridade do Oracle, impedindo dados inconsistentes (Freights para "lugar nenhum").

**Como funciona:**

+ Tenta registar um Freight enviando-o para uma estação de destino com ID 99999 (inexistente).

+ Espera-se que o motor da base de dados (Oracle) dispare um erro de violação de chave estrangeira (*Integrity Constraint Violation*).

+ No bloco EXCEPTION, verifica se o código capturado corresponde ao erro de FK (ORA-02291) ou se a transação falhou como esperado. Se falhar, o teste é SUCESSO.