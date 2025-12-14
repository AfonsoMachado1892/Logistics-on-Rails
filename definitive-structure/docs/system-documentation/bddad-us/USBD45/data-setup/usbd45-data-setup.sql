/* ==========================================================================
   USBD45 - Data Setup
   This script ensures all necessary data exists for testing USBD45
   ========================================================================== */

-- Ensure Railway_Gauge table has the required gauges
BEGIN
INSERT INTO Railway_Gauge (gauge_value, description)
VALUES (1668, 'Bitola Ibérica');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL; -- Gauge already exists
END;
/

BEGIN
INSERT INTO Railway_Gauge (gauge_value, description)
VALUES (1435, 'Bitola Standard Europeia');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL; -- Gauge already exists
END;
/

-- Ensure Line_Owner exists
BEGIN
INSERT INTO Line_Owner (vat_number, name, short_name)
VALUES ('PT503933813', 'Infraestruturas de Portugal, SA', 'IP');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL; -- Owner already exists
END;
/

-- Ensure test facilities exist
BEGIN
INSERT INTO Facility (ID, Name) VALUES (5, 'Porto Campanhã');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL;
END;
/

BEGIN
INSERT INTO Facility (ID, Name) VALUES (8, 'Barcelos');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL;
END;
/

BEGIN
INSERT INTO Facility (ID, Name) VALUES (10, 'Midões');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL;
END;
/

BEGIN
INSERT INTO Facility (ID, Name) VALUES (15, 'São Frutuoso');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL;
END;
/

BEGIN
INSERT INTO Facility (ID, Name) VALUES (20, 'Nine');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL;
END;
/

BEGIN
INSERT INTO Facility (ID, Name) VALUES (30, 'Braga');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        NULL;
END;
/

COMMIT;

-- Verify setup
SELECT 'Railway Gauges:' AS info FROM DUAL;
SELECT * FROM Railway_Gauge;

SELECT 'Line Owners:' AS info FROM DUAL;
SELECT * FROM Line_Owner WHERE vat_number = 'PT503933813';

SELECT 'Test Facilities:' AS info FROM DUAL;
SELECT * FROM Facility WHERE ID IN (5, 8, 10, 15, 20, 30);