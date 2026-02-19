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

-- 2. Bảng SpaceTypes 
CREATE TABLE SpaceTypes (
    type_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    base_price_per_hour NUMBER(15, 2) NOT NULL,
    base_price_per_day NUMBER(15, 2), 
    capacity NUMBER NOT NULL,
    description VARCHAR2(500),
    img_url VARCHAR2(500),
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Bảng Spaces 
CREATE TABLE Spaces (
    space_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    branch_id NUMBER NOT NULL,
    type_id NUMBER NOT NULL,   
    current_status VARCHAR2(20) DEFAULT 'AVAILABLE', 
    qr_code_token VARCHAR2(255) UNIQUE, 
    is_active NUMBER(1) DEFAULT 1, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);