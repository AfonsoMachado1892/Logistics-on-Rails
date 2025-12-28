CREATE OR REPLACE PROCEDURE pr_register_freight(
    p_freight_id IN NUMBER,
    p_origin_id  IN NUMBER,
    p_dest_id    IN NUMBER,
    p_date       IN DATE,
    p_model_id   IN NUMBER,
    p_qty        IN NUMBER
) IS
    v_available_wagons NUMBER;
    v_count            NUMBER;
    v_qty_to_assign    NUMBER; -- Variável local para controlo do loop
BEGIN
    -- 1. Validação de parâmetros
    IF p_freight_id IS NULL OR p_origin_id IS NULL OR p_dest_id IS NULL OR p_qty <= 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'USBD40: Parâmetros inválidos ou quantidade deve ser maior que zero.');
END IF;

    -- 2. Validar se Freight ID já existe
SELECT COUNT(*) INTO v_count FROM Freight WHERE id = p_freight_id;
IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'USBD40: Freight ID ' || p_freight_id || ' já existe.');
END IF;

    -- 3. Verificar disponibilidade de vagões na origem
SELECT COUNT(*)
INTO v_available_wagons
FROM Wagon
WHERE current_facility_id = p_origin_id
  AND model_id = p_model_id;

IF v_available_wagons < p_qty THEN
        RAISE_APPLICATION_ERROR(-20012, 'USBD40: Quantidade insuficiente. Disponíveis: ' || v_available_wagons || ', Solicitados: ' || p_qty);
END IF;

    -- 4. Inserir Freight
INSERT INTO Freight (id, freight_date, origin_facility_id, dest_facility_id)
VALUES (p_freight_id, p_date, p_origin_id, p_dest_id);

-- 5. Associar Vagões
-- Inicializamos a variável local com o valor do parâmetro de entrada
v_qty_to_assign := p_qty;

FOR r IN (
        SELECT wagon_number
        FROM Wagon
        WHERE current_facility_id = p_origin_id
          AND model_id = p_model_id
        ORDER BY wagon_number
    ) LOOP
        -- Sai do loop quando já tivermos atribuído a quantidade necessária
        EXIT WHEN v_qty_to_assign <= 0;

        -- Insere na tabela de associação
INSERT INTO Freight_Wagon (freight_id, wagon_number)
VALUES (p_freight_id, r.wagon_number);

-- Decrementa a variável local (permitido) em vez do parâmetro (proibido)
v_qty_to_assign := v_qty_to_assign - 1;
END LOOP;

    DBMS_OUTPUT.PUT_LINE('Sucesso: Freight ' || p_freight_id || ' criado com ' || p_qty || ' vagões.');
COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/