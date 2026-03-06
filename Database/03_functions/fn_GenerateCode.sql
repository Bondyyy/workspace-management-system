CREATE OR REPLACE FUNCTION fn_GenerateCode (
    p_type IN VARCHAR2
) RETURN VARCHAR2
IS
    v_code VARCHAR2(50);
    v_date_part VARCHAR2(20);
    v_random_part VARCHAR2(20);
BEGIN
    -- Lấy chuỗi ngày tháng năm hiện tại (VD: 20260306)
    v_date_part := TO_CHAR(SYSDATE, 'YYYYMMDD');
    
    -- Xử lý sinh mã tùy theo loại (p_type) truyền vào
    IF UPPER(p_type) = 'BOOKING' THEN
        -- Format: BKG-YYYYMMDD-XXXX (VD: BKG-20260306-A1B2)
        -- Dùng 'X' để sinh chuỗi gồm chữ in hoa và số
        v_random_part := UPPER(DBMS_RANDOM.STRING('X', 4));
        v_code := 'BKG-' || v_date_part || '-' || v_random_part;
        
    ELSIF UPPER(p_type) = 'EMPLOYEE' THEN
        -- Format: EMP-YY-XXXX (VD: EMP-26-9876)
        -- Mã nhân viên thường dùng số cho dễ gõ
        v_random_part := TO_CHAR(TRUNC(DBMS_RANDOM.VALUE(1000, 9999))); 
        v_code := 'EMP-' || TO_CHAR(SYSDATE, 'YY') || '-' || v_random_part;
        
    ELSIF UPPER(p_type) = 'VOUCHER' THEN
        -- Format: VCH-XXXXXXXX (VD: VCH-K9M2P4X1)
        -- Voucher thường cần mã ngẫu nhiên dài và khó đoán hơn, không cần dính ngày tháng
        v_random_part := UPPER(DBMS_RANDOM.STRING('X', 8));
        v_code := 'VCH-' || v_random_part;
        
    ELSIF UPPER(p_type) = 'CUSTOMER' THEN
        -- Format: CUS-YYMM-XXXX (VD: CUS-2603-A1B2)
        v_random_part := UPPER(DBMS_RANDOM.STRING('X', 4));
        v_code := 'CUS-' || TO_CHAR(SYSDATE, 'YYMM') || '-' || v_random_part;
        
    ELSE
        -- Mặc định nếu truyền sai type: SYS-YYYYMMDD-XXXXXX
        v_random_part := UPPER(DBMS_RANDOM.STRING('X', 6));
        v_code := 'SYS-' || v_date_part || '-' || v_random_part;
    END IF;

    RETURN v_code;
END fn_GenerateCode;
/
