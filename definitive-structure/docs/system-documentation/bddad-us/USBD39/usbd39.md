# USBD39: Associate locomotive to a train
Este documento descreve a implementação, lógica de negócio e os testes realizados para a USBD39.

## 1. Descrição da User Story
   Como Planeador, eu quero associar uma locomotiva (locomotive) a um comboio (train).

Critérios de Aceitação e Regras de Negócio:

O sistema deve validar a existência do comboio e da locomotiva na base de dados.

O sistema deve ser insensível a maiúsculas/minúsculas e espaços extra no número da locomotiva (Normalização).

Regra de Bitola (Gauge): A locomotiva só pode ser associada se a sua bitola for idêntica à bitola da linha onde o comboio opera.

A associação deve respeitar as restrições de comprimento total, validadas por gatilhos externos no momento da inserção.

A implementação deve ser feita através de um procedimento PL/SQL.

## 2. Explicação do Script (Procedimento PL/SQL)
   O procedimento prc_associate_locomotive_to_train foca-se na integridade técnica da composição ferroviária, garantindo que o material circulante é compatível com a infraestrutura.

Detalhes da Implementação:

Normalização de Input: O número da locomotiva é limpo de espaços redundantes e convertido para maiúsculas antes de qualquer validação.

Validação de Existência: Verifica se o comboio (erro -20301) e a locomotiva (erro -20302) existem antes de prosseguir.

Verificação de Bitola (Gauge): * Obtém a bitola da locomotiva através do seu modelo.

Obtém a bitola da linha ferroviária baseando-se na infraestrutura de partida (start_facility_id) do comboio.

Se as bitolas forem diferentes, a associação é impedida com o erro -20303.

Segurança de Infraestrutura: Caso não seja possível determinar a bitola da linha para o comboio, o sistema bloqueia a associação por segurança (erro -20304).

Inserção Final: Realiza o registo na tabela Train_Locomotive, onde gatilhos como o trg_check_length_loco validarão os limites físicos de comprimento.

Código: 


    CREATE OR REPLACE PROCEDURE prc_associate_locomotive_to_train(
    p_loco_number IN VARCHAR2,
    p_train_id    IN NUMBER
    ) IS
    v_clean_loco      VARCHAR2(50);
    v_count           NUMBER;
    v_train_gauge     NUMBER;
    v_loco_gauge      NUMBER;
    BEGIN
    -- 1. Normalização
    v_clean_loco := UPPER(REGEXP_REPLACE(TRIM(p_loco_number), '\s+', ' '));

    -- 2. Validações de Existência
    SELECT COUNT(*) INTO v_count FROM Train WHERE id = p_train_id;
    IF v_count = 0 THEN RAISE_APPLICATION_ERROR(-20301, 'Erro: Comboio não encontrado.'); END IF;

    SELECT COUNT(*) INTO v_count FROM Locomotive WHERE UPPER(loco_number) = v_clean_loco;
    IF v_count = 0 THEN RAISE_APPLICATION_ERROR(-20302, 'Erro: Locomotiva não encontrada.'); END IF;

    -- 3. Verificação de Bitola
    SELECT lm.gauge INTO v_loco_gauge FROM Locomotive l 
    JOIN Locomotive_Model lm ON l.model_id = lm.id WHERE UPPER(l.loco_number) = v_clean_loco;

    BEGIN
        SELECT gauge INTO v_train_gauge FROM Line 
        WHERE start_facility_id = (SELECT start_facility_id FROM Train WHERE id = p_train_id) AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20304, 'Erro: Bitola da linha não determinada.');
    END;

    IF v_train_gauge != v_loco_gauge THEN
        RAISE_APPLICATION_ERROR(-20303, 'Erro: Bitola incompatível.');
    END IF;

    -- 4. Inserção
    INSERT INTO Train_Locomotive (train_id, loco_number) VALUES (p_train_id, v_clean_loco);
    END;
## 3. Explicação dos Testes
   Teste 1: Associação com Sucesso e Ajuste de Infraestrutura
   Objetivo: Validar o fluxo completo de associação quando a locomotiva e a linha são compatíveis.

Descrição: O teste atualiza temporariamente o ponto de partida do comboio para 'Contumil' (ID 13) para garantir que a linha associada possui a mesma bitola que a locomotiva '5621'.

Resultado Esperado: O procedimento deve confirmar a associação com sucesso e, em seguida, realizar ROLLBACK para manter a integridade dos dados de teste.

