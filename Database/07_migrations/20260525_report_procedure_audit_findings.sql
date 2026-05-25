SET SERVEROUTPUT ON;

PROMPT ===== CHECK PROCEDURE/FUNCTION/TRIGGER INVALID =====
SELECT object_name, object_type, status
FROM user_objects
WHERE object_type IN ('PROCEDURE', 'FUNCTION', 'TRIGGER')
  AND status <> 'VALID'
ORDER BY object_type, object_name;

PROMPT ===== CHECK COMPILE ERRORS =====
SELECT name, type, line, position, text
FROM user_errors
ORDER BY name, sequence;

PROMPT ===== CHECK SP_TRACUUDICHVU OBJECT =====
SELECT object_name, object_type, status
FROM user_objects
WHERE UPPER(object_name) = 'SP_TRACUUDICHVU';

PROMPT ===== NOTE =====
PROMPT This script is report-only.
PROMPT Do not delete, rename, or replace stored procedures without DBA/tech lead confirmation.
PROMPT Known audit finding: there were two source files declaring SP_TraCuuDichVu, which can cause deploy-order overwrite.
