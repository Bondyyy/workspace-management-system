CREATE OR REPLACE PROCEDURE sp_GetActiveBranches(
    p_keyword IN VARCHAR2 DEFAULT NULL, -- Tham số hỗ trợ bộ lọc/tìm kiếm (theo tên hoặc địa chỉ)
    p_cursor OUT SYS_REFCURSOR,
    p_result_code OUT NUMBER,
    p_result_msg OUT VARCHAR2
) AS
    v_total_active NUMBER;
    v_filtered_active NUMBER;
BEGIN
    -- Hệ thống chưa có chi nhánh nào hoạt động
    SELECT COUNT(*) INTO v_total_active 
    FROM Branches 
    WHERE is_active = 1;

    IF v_total_active = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Hiện tại chưa có chi nhánh nào khả dụng.';
        RETURN;
    END IF;

    -- Tìm kiếm hoặc lọc không có kết quả
    SELECT COUNT(*) INTO v_filtered_active 
    FROM Branches 
    WHERE is_active = 1 
      AND (p_keyword IS NULL 
           OR LOWER(name) LIKE '%' || LOWER(p_keyword) || '%' 
           OR LOWER(address) LIKE '%' || LOWER(p_keyword) || '%');

    IF v_filtered_active = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không tìm thấy chi nhánh phù hợp với tiêu chí của bạn.';
        RETURN;
    END IF;

    -- Mở cursor trả về danh sách
    OPEN p_cursor FOR
    SELECT branch_id, name, address, hotline, location_map_url 
    FROM Branches 
    WHERE is_active = 1
      AND (p_keyword IS NULL 
           OR LOWER(name) LIKE '%' || LOWER(p_keyword) || '%' 
           OR LOWER(address) LIKE '%' || LOWER(p_keyword) || '%')
    ORDER BY name;

    p_result_code := 0;
    p_result_msg := 'Lấy danh sách chi nhánh thành công.';

EXCEPTION
    -- Lỗi kết nối máy chủ
    WHEN OTHERS THEN
        p_result_code := -1;
        p_result_msg := 'Lỗi kết nối máy chủ. Vui lòng thử lại sau.';
END sp_GetActiveBranches;
/