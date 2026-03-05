CREATE TABLE MembershipTiers (
    tier_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tier_name VARCHAR2(50) NOT NULL UNIQUE,  
    min_points NUMBER DEFAULT 0 NOT NULL,   
    discount_percent NUMBER(5, 2) DEFAULT 0, 
    description VARCHAR2(255),              
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);