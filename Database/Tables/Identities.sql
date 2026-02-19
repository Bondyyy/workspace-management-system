-- 1. Bảng Roles
CREATE TABLE Roles (
    role_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR2(100) NOT NULL,
    description VARCHAR2(255),
    is_active NUMBER(1) DEFAULT 1 
);

-- 2. Bảng Permissions
CREATE TABLE Permissions (
    permission_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    permission_code VARCHAR2(50) NOT NULL UNIQUE,
    permission_name VARCHAR2(100) NOT NULL,
    module VARCHAR2(50),
    permission_description VARCHAR2(255)
);

-- 3. Bảng RolePermissions
CREATE TABLE RolePermissions (
    role_id NUMBER NOT NULL,
    permission_id NUMBER NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

-- 4. Bảng Users 
CREATE TABLE Users (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_id NUMBER NOT NULL,
    user_name VARCHAR2(50) NOT NULL UNIQUE,
    full_name VARCHAR2(100) NOT NULL,
    password_hash VARCHAR2(255) NOT NULL,
    email VARCHAR2(100) UNIQUE,
    phone_number VARCHAR2(20),
    status VARCHAR2(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 5. Bảng Customers
CREATE TABLE Customers (
    customer_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL UNIQUE,
    membership_tier_id NUMBER,
    customer_code VARCHAR2(50) UNIQUE,
    loyalty_points NUMBER DEFAULT 0,
    lifetime_spending NUMBER(15, 2) DEFAULT 0,
    company_info VARCHAR2(255),
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 6. Bảng Employees 
CREATE TABLE Employees (
    employee_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL UNIQUE,
    employee_code VARCHAR2(50) UNIQUE NOT NULL,
    branch_id NUMBER, 
    position VARCHAR2(100),
    hire_date DATE,
    contract_type VARCHAR2(50),
    bank_number VARCHAR2(50),
    job_status VARCHAR2(50) DEFAULT 'ACTIVE',
    termination_date DATE,
    base_salary NUMBER(15, 2),
    CONSTRAINT fk_employee_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

ALTER TABLE Users ADD CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED'));
ALTER TABLE Employees ADD CONSTRAINT chk_emp_job_status CHECK (job_status IN ('ACTIVE', 'RESIGNED', 'ON_LEAVE'));
ALTER TABLE Customers ADD CONSTRAINT chk_cust_loyalty_points CHECK (loyalty_points >= 0);