SET SERVEROUTPUT ON;
DECLARE
v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TEST 5: Empty String / Spaces Validation ---');

BEGIN
        -- Tenta registar uma instalação com uma string composta apenas por espaços
        v_id := fn_register_facility('      ');

        DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA. O sistema aceitou espaços vazios como nome.');
EXCEPTION
        WHEN OTHERS THEN
            -- A função deve disparar o erro -20001 após o TRIM detetar que a string é vazia
            IF SQLCODE = -20001 THEN
                DBMS_OUTPUT.PUT_LINE('RESULTADO: SUCESSO (Espaços em branco bloqueados corretamente).');
                DBMS_OUTPUT.PUT_LINE('Mensagem: ' || SQLERRM);
ELSE
                DBMS_OUTPUT.PUT_LINE('RESULTADO: FALHA (Código de erro inesperado): ' || SQLERRM);
END IF;
END;
END;
/