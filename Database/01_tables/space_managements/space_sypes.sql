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