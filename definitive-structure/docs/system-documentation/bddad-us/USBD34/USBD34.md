
# USBD34: Associate freight to a train 
Este documento descreve a implementação, lógica de negócio e os testes realizados para a USBD34.

## 1. Descrição da User Story
   Como Planeador, eu quero associar um frete (freight) a um comboio (train).

Critérios de Aceitação e Regras de Negócio:

O sistema deve validar se o Frete existe na base de dados.

O sistema deve validar se o Comboio existe na base de dados.

Um frete não pode estar associado a mais do que um comboio em simultâneo (Garantia de unicidade).

A associação deve respeitar o limite de comprimento total do comboio, validado através de um gatilho (trigger).

A implementação deve ser realizada através de um procedimento PL/SQL.

## 2. Explicação do Script (Procedimento PL/SQL)
   A lógica central da USBD34 foi encapsulada no procedimento prc_associate_freight_to_train. Este procedimento assegura que todas as pre-condições de negócio são cumpridas antes de registar a associação na tabela Train_Freight.

Detalhes da Implementação:

Validação de Existência: O procedimento verifica se os IDs do Frete e do Comboio são válidos. Caso contrário, lança erros específicos (-20010 ou -20011).

Verificação de Disponibilidade: Antes da inserção, verifica-se se o frete já está atribuído a algum comboio. Se estiver, a operação é interrompida com o erro -20012.

Inserção e Segurança Física: O comando INSERT dispara o trigger trg_check_length_freight (USBD33). Este trigger impede a associação se o comprimento do frete somado ao atual do comboio exceder o max_length_m definido.

Tratamento de Exceções: Qualquer erro, seja de validação manual ou disparado pelo motor de base de dados (triggers), é capturado e re-lançado para garantir que o utilizador recebe o feedback adequado.

Código: 



    CREATE OR REPLACE PROCEDURE prc_associate_freight_to_train(
    p_freight_id IN NUMBER,
    p_train_id   IN NUMBER
    ) IS
    v_freight_exists NUMBER;
    v_train_exists   NUMBER;
    v_already_linked NUMBER;
    BEGIN
    -- 1. Validação: O Frete existe?
    SELECT COUNT(*) INTO v_freight_exists FROM Freight WHERE id = p_freight_id;
    IF v_freight_exists = 0 THEN
    RAISE_APPLICATION_ERROR(-20010, 'Erro: Frete com ID ' || p_freight_id || ' não encontrado.');
    END IF;

    -- 2. Validação: O Comboio existe?
    SELECT COUNT(*) INTO v_train_exists FROM Train WHERE id = p_train_id;
    IF v_train_exists = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Erro: Comboio com ID ' || p_train_id || ' não encontrado.');
    END IF;

    -- 3. Validação: O frete já está associado?
    SELECT COUNT(*) INTO v_already_linked FROM Train_Freight WHERE freight_id = p_freight_id;
    IF v_already_linked > 0 THEN
        RAISE_APPLICATION_ERROR(-20012, 'Erro: O frete ' || p_freight_id || ' já está associado a um comboio.');
    END IF;

    -- 4. Inserção na tabela de associação
    -- Nota: Dispara automaticamente o trigger trg_check_length_freight
    INSERT INTO Train_Freight (train_id, freight_id)
    VALUES (p_train_id, p_freight_id);

    DBMS_OUTPUT.PUT_LINE('Sucesso: Frete ' || p_freight_id || ' associado ao comboio ' || p_train_id);

    EXCEPTION
    WHEN OTHERS THEN
    RAISE;
    END;

## 3. Explicação dos Testes
   Foram realizados testes exaustivos para validar o comportamento do procedimento em cenários de sucesso e erro.

### Teste 1: ID Inexistente
Objetivo: Validar se o sistema impede a associação de entidades que não constam no sistema.

Descrição: Executa o procedimento passando um ID de frete fictício (99999).

Resultado Esperado: Mensagem de erro indicando que o ID não foi encontrado.

### Teste 2: Limite de Comprimento Excedido
Objetivo: Garantir que o comboio não aceita mais carga do que o seu limite físico.

Descrição: O teste altera o limite do comboio para 10 metros e tenta associar um frete que ultrapassa essa marca.

Resultado Esperado: Falha no INSERT devido ao trigger de comprimento e execução de ROLLBACK.

### Teste 3: Validação Técnica (Bitola/Gauge)
Objetivo: Garantir que todos os elementos associados ao comboio são tecnicamente compatíveis.

Descrição: Verifica se a bitola (gauge) do equipamento a ser associado coincide com a bitola da linha atribuída ao comboio.

Resultado Esperado: Erro -20303 caso as bitolas sejam divergentes.