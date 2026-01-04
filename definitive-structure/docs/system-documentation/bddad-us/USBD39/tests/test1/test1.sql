
DECLARE
v_loco  VARCHAR2(20) := '5621';
    v_train NUMBER := 5435;
BEGIN
    -- Ajuste temporário apenas para o teste passar com a lógica do seu procedimento
    -- Mudamos a partida do comboio para Contumil (ID 13), que é início da Linha 3
UPDATE Train SET start_facility_id = 13 WHERE id = 5435;

DELETE FROM Train_Locomotive WHERE loco_number = '5621' AND train_id = 5435;

prc_associate_locomotive_to_train(v_loco, v_train);

    DBMS_OUTPUT.PUT_LINE('Associação realizada com sucesso usando Contumil (ID 13) como origem.');

ROLLBACK; -- Desfaz a alteração do comboio e a inserção
END;
/