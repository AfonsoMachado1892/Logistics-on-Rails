/* ==========================================================================
   IMPLEMENTAÇÃO USBD40: Registar Freight (com validação de stock de vagões)
   ========================================================================== */
CREATE OR REPLACE PROCEDURE pr_register_freight(
    p_origin_id IN NUMBER,
    p_dest_id   IN NUMBER,
    p_date      IN DATE,
    p_model_id  IN NUMBER, -- Tipo de vagão solicitado
    p_qty       IN NUMBER, -- Quantidade de vagões necessária
    p_new_id    OUT NUMBER -- Retorna o ID do novo Freight
) IS
    v_available_wagons NUMBER;
    v_new_freight_id   NUMBER;
BEGIN
    -- 1. Validação de parâmetros
    IF p_origin_id IS NULL OR p_dest_id IS NULL OR p_model_id IS NULL OR p_qty IS NULL OR p_qty <= 0 THEN
        RAISE_APPLICATION_ERROR(-20040, 'USBD40: Parâmetros inválidos ou quantidade deve ser maior que zero.');
END IF;

    -- 2. Verificar disponibilidade de vagões "livres"
    -- Consideramos "livres" os vagões que estão estacionados na estação de origem (current_facility_id).
SELECT COUNT(*)
INTO v_available_wagons
FROM Wagon
WHERE current_facility_id = p_origin_id
  AND model_id = p_model_id;

IF v_available_wagons < p_qty THEN
        RAISE_APPLICATION_ERROR(-20041, 'USBD40: Quantidade insuficiente de vagões do modelo ' || p_model_id ||
                                        ' na estação ' || p_origin_id || '. Disponíveis: ' || v_available_wagons);
END IF;

    -- 3. Gerar ID e Criar Freight
    -- Utiliza a sequência definida na DDL (seq_freight_id)
SELECT seq_freight_id.NEXTVAL INTO v_new_freight_id FROM DUAL;

INSERT INTO Freight (id, freight_date, origin_facility_id, dest_facility_id)
VALUES (v_new_freight_id, p_date, p_origin_id, p_dest_id);

-- 4. Associar os Vagões ao Freight
-- Seleciona os vagões específicos para preencher a quantidade solicitada
FOR r IN (
        SELECT wagon_number
        FROM Wagon
        WHERE current_facility_id = p_origin_id
          AND model_id = p_model_id
        AND ROWNUM <= p_qty -- Limita à quantidade pedida
    ) LOOP
        INSERT INTO Freight_Wagon (freight_id, wagon_number)
        VALUES (v_new_freight_id, r.wagon_number);

        -- Opcional: Poderia atualizar o estado do vagão se houvesse uma coluna de 'estado',
        -- mas a localização mantém-se a mesma até o comboio partir.
END LOOP;

    -- Retorna o ID gerado
    p_new_id := v_new_freight_id;

    -- 5. Feedback e Commit
    DBMS_OUTPUT.PUT_LINE('Ação Realizada: Freight ' || v_new_freight_id || ' criado com ' || p_qty || ' vagões associados.');
COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/