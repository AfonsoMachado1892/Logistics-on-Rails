DECLARE
v_new_id NUMBER;
    v_check_freight NUMBER;
    v_check_wagons NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Registar Freight com Stock Suficiente (Cenário de Sucesso) ---');

    -- 1. Executar a ação
    -- Pede-se 2 vagões do modelo 1104 a partir da estação 50 (Leixões).
    -- Sabe-se que existem lá vagões deste tipo (ex: 356 3 077, etc.)
    pr_register_freight(
        p_origin_id => 50,  -- Origem: Leixões
        p_dest_id   => 11,  -- Destino: Valença
        p_date      => SYSDATE + 5,
        p_model_id  => 1104,
        p_qty       => 2,   -- Quantidade: 2
        p_new_id    => v_new_id
    );

    -- 2. Validar o resultado
    -- Verificar se o Freight foi criado
SELECT COUNT(*) INTO v_check_freight FROM Freight WHERE id = v_new_id;

-- Verificar se foram associados 2 vagões na tabela Freight_Wagon
SELECT COUNT(*) INTO v_check_wagons FROM Freight_Wagon WHERE freight_id = v_new_id;

IF v_check_freight = 1 AND v_check_wagons = 2 THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO. Freight ' || v_new_id || ' criado com 2 vagões associados.');
ELSE
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Registo incorreto (Freight: ' || v_check_freight || ', Vagões: ' || v_check_wagons || ').');
END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. Ocorreu um erro inesperado: ' || SQLERRM);
END;
/