-- 1. Bảng Branches 
CREATE TABLE Branches (
    branch_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    manager_id NUMBER, 
    name VARCHAR2(100) NOT NULL,
    address VARCHAR2(255) NOT NULL,
    location_map_url VARCHAR2(500),
    hotline VARCHAR2(20),
    is_active NUMBER(1) DEFAULT 1, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);