SET SERVEROUTPUT ON;

PROMPT ===== OVERNIGHT BRANCH HOURS CHECK =====
PROMPT Purpose: verify CHINHANH hour constraints allow overnight operating hours such as 22:00 -> 06:00.
PROMPT Existing source constraints CHK_CN_MoCua and CHK_CN_DongCua validate HH:mm/HH:mm:ss format only.
PROMPT No column is removed, no datatype is changed, and no DROP/ALTER is executed by this script.
PROMPT Rollback note: no rollback action is required because this script is report-only.

PROMPT ===== CURRENT CHINHANH HOUR CONSTRAINTS =====
SELECT constraint_name, search_condition_vc
FROM user_constraints
WHERE table_name = 'CHINHANH'
  AND constraint_name IN ('CHK_CN_MOCUA', 'CHK_CN_DONGCUA', 'CHK_CN_TRANGTHAI')
ORDER BY constraint_name;

PROMPT ===== SAMPLE OVERNIGHT FORMAT VALIDATION =====
SELECT
    CASE
        WHEN REGEXP_LIKE('22:00', '^([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$')
         AND REGEXP_LIKE('06:00', '^([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$')
        THEN 'PASS: 22:00 -> 06:00 is allowed by format constraints'
        ELSE 'FAIL: overnight sample is blocked by format constraints'
    END AS overnight_check
FROM dual;

PROMPT ===== NOTE =====
PROMPT Business logic now interprets close time <= open time as overnight/24h in Java and PL/SQL flow checks.
