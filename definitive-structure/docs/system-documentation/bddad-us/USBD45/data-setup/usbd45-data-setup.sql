/* ==========================================================================
   USBD45 - Data Setup
   IMPORTANTE: Este script NÃO insere dados, apenas verifica o ambiente
   Os dados necessários já devem existir na usbd32
   ========================================================================== */

PROMPT ========================================
PROMPT USBD45 - Verificação do Ambiente
PROMPT ========================================
PROMPT

-- Verificar Railway_Gauge
PROMPT ========================================
PROMPT Bitolas (Railway_Gauge) necessárias:
PROMPT - 1668mm (Bitola Ibérica)
PROMPT - 1435mm (Bitola Standard Europeia)
PROMPT ========================================
SELECT gauge_value AS "Largura (mm)",
       description AS "Descrição"
FROM Railway_Gauge
WHERE gauge_value IN (1668, 1435)
ORDER BY gauge_value;

PROMPT
PROMPT Verificando bitolas...
DECLARE
    v_count NUMBER;
BEGIN
SELECT COUNT(*) INTO v_count
FROM Railway_Gauge
WHERE gauge_value IN (1668, 1435);

IF v_count = 2 THEN
        DBMS_OUTPUT.PUT_LINE('✓ Bitolas necessárias encontradas');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ AVISO: Faltam bitolas necessárias');
        DBMS_OUTPUT.PUT_LINE('  Certifique-se que executou usbd32.sql');
END IF;
END;
/

PROMPT
PROMPT ========================================
PROMPT Proprietário (Line_Owner) necessário:
PROMPT - PT503933813 (IP)
PROMPT ========================================
SELECT vat_number AS "VAT",
       short_name AS "Sigla",
       name AS "Nome"
FROM Line_Owner
WHERE vat_number = 'PT503933813';

PROMPT
PROMPT Verificando proprietário...
DECLARE
    v_count NUMBER;
BEGIN
SELECT COUNT(*) INTO v_count
FROM Line_Owner
WHERE vat_number = 'PT503933813';

IF v_count = 1 THEN
        DBMS_OUTPUT.PUT_LINE('✓ Proprietário encontrado');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ AVISO: Proprietário não encontrado');
        DBMS_OUTPUT.PUT_LINE('  Certifique-se que executou usbd32.sql');
END IF;
END;
/

PROMPT
PROMPT ========================================
PROMPT Estações (Facility) necessárias para testes:
PROMPT - ID 5:  Porto Campanhã
PROMPT - ID 8:  Barcelos
PROMPT - ID 10: Midões
PROMPT - ID 15: São Frutuoso
PROMPT - ID 20: Nine
PROMPT - ID 30: Braga
PROMPT ========================================
SELECT ID AS "ID",
       Name AS "Nome"
FROM Facility
WHERE ID IN (5, 8, 10, 15, 20, 30)
ORDER BY ID;

PROMPT
PROMPT Verificando estações...
DECLARE
    v_count NUMBER;
BEGIN
SELECT COUNT(*) INTO v_count
FROM Facility
WHERE ID IN (5, 8, 10, 15, 20, 30);

IF v_count = 6 THEN
        DBMS_OUTPUT.PUT_LINE('✓ Todas as estações necessárias encontradas');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ AVISO: Faltam ' || (6 - v_count) || ' estação(ões)');
        DBMS_OUTPUT.PUT_LINE('  Certifique-se que executou usbd32.sql');
END IF;
END;
/

PROMPT
PROMPT ========================================
PROMPT Resumo da Verificação
PROMPT ========================================
DECLARE
v_gauges NUMBER;
    v_owner NUMBER;
    v_facilities NUMBER;
    v_ready BOOLEAN := TRUE;
BEGIN
SELECT COUNT(*) INTO v_gauges FROM Railway_Gauge WHERE gauge_value IN (1668, 1435);
SELECT COUNT(*) INTO v_owner FROM Line_Owner WHERE vat_number = 'PT503933813';
SELECT COUNT(*) INTO v_facilities FROM Facility WHERE ID IN (5, 8, 10, 15, 20, 30);

DBMS_OUTPUT.PUT_LINE('Bitolas........: ' || v_gauges || '/2');
    DBMS_OUTPUT.PUT_LINE('Proprietário...: ' || v_owner || '/1');
    DBMS_OUTPUT.PUT_LINE('Estações.......: ' || v_facilities || '/6');
    DBMS_OUTPUT.PUT_LINE('');

    IF v_gauges < 2 THEN v_ready := FALSE; END IF;
    IF v_owner < 1 THEN v_ready := FALSE; END IF;
    IF v_facilities < 6 THEN v_ready := FALSE; END IF;

    IF v_ready THEN
        DBMS_OUTPUT.PUT_LINE('✓ Ambiente pronto para testes USBD45');
ELSE
        DBMS_OUTPUT.PUT_LINE('✗ Ambiente incompleto');
        DBMS_OUTPUT.PUT_LINE('  Execute usbd32.sql antes de continuar');
END IF;
END;
/

PROMPT
PROMPT ========================================
PROMPT Fim da Verificação
PROMPT ========================================