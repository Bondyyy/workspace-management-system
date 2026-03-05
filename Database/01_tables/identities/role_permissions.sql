CREATE TABLE RolePermissions (
    role_id       NUMBER(10) NOT NULL,
    permission_id NUMBER(10) NOT NULL,
    assigned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)       
);