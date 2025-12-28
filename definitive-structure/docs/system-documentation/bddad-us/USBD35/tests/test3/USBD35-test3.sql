SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 3: Null Input Validation ---');

    BEGIN
        -- Tenta registar uma instalação passando explicitamente NULL
        v_id := fn_register_facility(NULL);

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou um valor NULL.');
    EXCEPTION
        WHEN OTHERS THEN
            -- Verifica se o erro retornado é o código customizado -20001
            IF SQLCODE = -20001 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO (Entrada NULL bloqueada corretamente).');
                DBMS_OUTPUT.PUT_LINE('Mensagem: ' || SQLERRM);
            ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA (Código de erro inesperado): ' || SQLERRM);
            END IF;
    END;
END;
/