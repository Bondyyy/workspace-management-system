ALTER TABLE Users 
ADD CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'BANNED', 'LOCKED'));
ALTER TABLE Employees 
ADD CONSTRAINT chk_emp_status CHECK (job_status IN ('ACTIVE', 'RESIGNED', 'ON_LEAVE'));
ALTER TABLE Customers 
ADD CONSTRAINT chk_cust_points CHECK (loyalty_points >= 0);

ALTER TABLE Permissions 
ADD CONSTRAINT fk_perm_function 
FOREIGN KEY (function_id) REFERENCES Functions(function_id);
ALTER TABLE RolePermissions 
ADD CONSTRAINT fk_rp_role 
FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE CASCADE;
ALTER TABLE RolePermissions 
ADD CONSTRAINT fk_rp_perm 
FOREIGN KEY (permission_id) REFERENCES Permissions(permission_id) ON DELETE CASCADE;
ALTER TABLE UserRoles 
ADD CONSTRAINT fk_ur_user 
FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE;
ALTER TABLE UserRoles 
ADD CONSTRAINT fk_ur_role 
FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE CASCADE;
ALTER TABLE Customers 
ADD CONSTRAINT fk_customers_users 
FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE;
ALTER TABLE Customers 
ADD CONSTRAINT fk_customers_tier 
FOREIGN KEY (membership_tier_id) REFERENCES MembershipTiers(tier_id);
ALTER TABLE Employees 
ADD CONSTRAINT fk_employees_users 
FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE;
ALTER TABLE Employees 
ADD CONSTRAINT fk_employees_branch 
FOREIGN KEY (branch_id) REFERENCES Branches(branch_id);