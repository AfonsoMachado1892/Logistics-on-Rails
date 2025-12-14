DECLARE
v_total NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 5: Inserir múltiplos edifícios na mesma estação ---');

    -- Inserir dois edifícios seguidos na estação 5 (Porto Campanhã)
    pr_add_building_to_facility(5, 'Bilheteira Central', 'Ticket Office');
    pr_add_building_to_facility(5, 'Cafetaria', 'Services');

    -- Validar se a estação 5 tem agora pelo menos 2 edifícios
SELECT COUNT(*) INTO v_total
FROM Building
WHERE facility_id = 5;

IF v_total >= 2 THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Campanhã tem ' || v_total || ' edifícios registados.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Contagem incorreta.');
END IF;
END;
/