CREATE TABLE UserRoles (
    user_id       NUMBER(10) NOT NULL,
    role_id       NUMBER(10) NOT NULL,
    assigned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)             
);