/* ==========================================================================
   USBD35: Data Setup
   Goal: Clean up strictly the TEST data to avoid FK conflicts with seed data.
   ========================================================================== */
SET SERVEROUTPUT ON;

BEGIN
    -- We use specific "Test" names to avoid clashing with real data (usbd32.sql)
    -- We delete children first (just in case USBD36 tests ran and added buildings)

FOR r IN (SELECT id FROM Facility WHERE name IN ('Viana Test Station', 'Aveiro Test Node')) LOOP
        -- Delete any buildings attached to these test stations
DELETE FROM Building WHERE facility_id = r.id;

-- Delete the facility itself
DELETE FROM Facility WHERE id = r.id;
END LOOP;

COMMIT;
DBMS_OUTPUT.PUT_LINE('Setup Complete: Test data cleaned.');
END;
/