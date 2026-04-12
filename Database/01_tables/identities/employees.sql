CREATE TABLE Employees (
    employee_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          NUMBER(10) NOT NULL UNIQUE,   
    employee_code    VARCHAR2(50) UNIQUE NOT NULL,
    branch_id        NUMBER,                       
    
    position         VARCHAR2(100),
    hire_date        DATE,
    contract_type    VARCHAR2(50),
    bank_number      VARCHAR2(50),                
    job_status       VARCHAR2(50) DEFAULT 'ACTIVE',
    identity_card    VARCHAR2(20) UNIQUE,          -- CCCD/CMND 
    
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted       NUMBER(1) DEFAULT 0 CHECK (is_deleted IN (0,1))
);

-- 1. Tạo Sequence cho Nhân viên
CREATE SEQUENCE seq_employee_code
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 2. Trigger tự động tạo employee_code
CREATE OR REPLACE TRIGGER trg_GenerateEmployeeCode
BEFORE INSERT ON Employees
FOR EACH ROW
WHEN (NEW.employee_code IS NULL)
BEGIN
    :NEW.employee_code := 'NV-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(seq_employee_code.NEXTVAL, 7, '0');
END;
/