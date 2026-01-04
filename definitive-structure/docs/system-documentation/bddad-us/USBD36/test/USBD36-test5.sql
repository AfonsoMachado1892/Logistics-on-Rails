SET SERVEROUTPUT ON;
DECLARE
v_total NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 5: Inserir múltiplos edifícios na mesma estação ---');

    -- Inserir dois edifícios seguidos na estação 5 (Porto Campanhã)
    -- Manual IDs 1005 and 1006
    pr_add_building_to_facility(1005, 5, 'Bilheteira Central', 'Ticket Office');
    pr_add_building_to_facility(1006, 5, 'Cafetaria', 'Services');

    -- Validar se a estação 5 tem agora os nossos 2 edifícios
SELECT COUNT(*) INTO v_total
FROM Building
WHERE id IN (1005, 1006) AND facility_id = 5;

IF v_total = 2 THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Campanhã recebeu 2 novos edifícios.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Contagem incorreta (' || v_total || ').');
END IF;

ROLLBACK; -- Cleanup
END;
/