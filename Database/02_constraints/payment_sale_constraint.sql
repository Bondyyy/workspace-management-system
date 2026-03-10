ALTER TABLE Invoices 
ADD CONSTRAINT chk_invoice_status CHECK (status IN ('UNPAID', 'PAID', 'CANCELLED'));
ALTER TABLE InvoiceLines 
ADD CONSTRAINT chk_reference_type CHECK (reference_type IN ('BOOKING', 'SESSION', 'F&B_ORDER', 'EXTENSION'));
ALTER TABLE Payments 
ADD CONSTRAINT chk_payment_method CHECK (payment_method IN ('CASH', 'CREDIT_CARD', 'VNPAY', 'MOMO'));
ALTER TABLE Payments 
ADD CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'));
ALTER TABLE Payments 
ADD CONSTRAINT chk_payment_amount CHECK (amount > 0);
ALTER TABLE Vouchers 
ADD CONSTRAINT chk_voucher_type CHECK (discount_type IN ('PERCENT', 'FIXED'));
ALTER TABLE Vouchers 
ADD CONSTRAINT chk_voucher_value CHECK (discount_value > 0);
ALTER TABLE Vouchers
ADD CONSTRAINT chk_voucher_dates CHECK (expiry_date >= effective_date);
ALTER TABLE MembershipTiers
ADD CONSTRAINT chk_membership_tiers_tiersname CHECK (tier_name IN ('BRONZE','SILVER','GOLD','DIAMOND'))

ALTER TABLE Payments 
ADD CONSTRAINT fk_payments_invoices 
FOREIGN KEY (invoice_id) REFERENCES Invoices(invoice_id);
ALTER TABLE Invoices 
ADD CONSTRAINT fk_invoices_customers 
FOREIGN KEY (customer_id) REFERENCES Customers(customer_id);
ALTER TABLE Invoices 
ADD CONSTRAINT fk_invoices_staff 
FOREIGN KEY (staff_id) REFERENCES Employees(employee_id);
ALTER TABLE InvoiceLines 
ADD CONSTRAINT fk_invoicelines_invoices 
FOREIGN KEY (invoice_id) REFERENCES Invoices(invoice_id) ON DELETE CASCADE;