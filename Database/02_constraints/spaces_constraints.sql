ALTER TABLE Spaces 
ADD CONSTRAINT chk_space_current_status CHECK (current_status IN ('AVAILABLE', 'BOOKED', 'OCCUPIED', 'CLEANING'));
ALTER TABLE SpaceTypes 
ADD CONSTRAINT chk_spacetype_base_price_per_hour CHECK (base_price_per_hour >= 0);
ALTER TABLE SpaceTypes 
ADD CONSTRAINT chk_spacetype_capacity CHECK (capacity > 0);

ALTER TABLE Branches 
ADD CONSTRAINT fk_branches_manager FOREIGN KEY (manager_id) REFERENCES Employees(employee_id) ON DELETE SET NULL;
ALTER TABLE Spaces 
ADD CONSTRAINT fk_spaces_branches FOREIGN KEY (branch_id) REFERENCES Branches(branch_id);
ALTER TABLE Spaces 
ADD CONSTRAINT fk_spaces_types FOREIGN KEY (type_id) REFERENCES SpaceTypes(type_id);