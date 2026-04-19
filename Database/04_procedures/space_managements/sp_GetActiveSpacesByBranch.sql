CREATE OR REPLACE PROCEDURE sp_GetActiveSpacesByBranch(
    p_branch_id           IN NUMBER,
    p_type_id             IN NUMBER DEFAULT NULL,       -- Bộ lọc: Loại không gian (Bàn đơn, Phòng họp...)
    p_expected_start_time IN TIMESTAMP DEFAULT NULL,    -- Bộ lọc: Giờ bắt đầu
    p_expected_end_time   IN TIMESTAMP DEFAULT NULL,    -- Bộ lọc: Giờ kết thúc
    p_only_available      IN NUMBER DEFAULT 0,          -- 1: Chỉ hiện chỗ trống, 0: Hiện tất cả
    p_cursor              OUT SYS_REFCURSOR,
    p_result_code         OUT NUMBER,
    p_result_msg          OUT VARCHAR2
) AS
    v_total_spaces    NUMBER;
    v_filtered_spaces NUMBER;
BEGIN
    -- Chi nhánh chưa có dữ liệu không gian
    SELECT COUNT(*) INTO v_total_spaces 
    FROM Spaces 
    WHERE branch_id = p_branch_id AND is_active = 1;

    IF v_total_spaces = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Hiện tại chi nhánh chưa có không gian nào khả dụng.';
        RETURN;
    END IF;

    -- Không tìm thấy không gian theo bộ lọc
    SELECT COUNT(*) INTO v_filtered_spaces
    FROM Spaces s
    WHERE s.branch_id = p_branch_id 
      AND s.is_active = 1
      -- Lọc theo loại không gian (nếu có truyền vào)
      AND (p_type_id IS NULL OR s.type_id = p_type_id)
      -- Lọc "Chỉ hiện chỗ trống"
      AND (
          p_only_available = 0 -- Nếu không chọn lọc chỗ trống thì bỏ qua check này
          OR 
          (
              p_only_available = 1 
              AND p_expected_start_time IS NOT NULL 
              AND p_expected_end_time IS NOT NULL 
              -- Gọi hàm fn_CheckSpaceAvailable để check đụng lịch thực tế
              AND fn_CheckSpaceAvailable(s.space_id, p_expected_start_time, p_expected_end_time) = 1
          )
          OR 
          (
              p_only_available = 1 
              AND (p_expected_start_time IS NULL OR p_expected_end_time IS NULL)
              -- Nếu chỉ lọc trống mà chưa chọn giờ thì lấy các bàn đang rảnh hiện tại
              AND s.current_status = 'AVAILABLE'
          )
      );

    IF v_filtered_spaces = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không tìm thấy không gian nào phù hợp với yêu cầu của bạn. Vui lòng thay đổi bộ lọc hoặc khung giờ.';
        RETURN;
    END IF;

    -- Trả về danh sách đã lọc
    OPEN p_cursor FOR
    SELECT s.space_id, s.name AS space_name, s.current_status, 
           st.type_id, st.name AS type_name, st.capacity, st.base_price_per_hour, st.img_url
    FROM Spaces s
    JOIN SpaceTypes st ON s.type_id = st.type_id
    WHERE s.branch_id = p_branch_id 
      AND s.is_active = 1
      AND (p_type_id IS NULL OR s.type_id = p_type_id)
      AND (
          p_only_available = 0 
          OR (p_only_available = 1 AND p_expected_start_time IS NOT NULL AND p_expected_end_time IS NOT NULL AND fn_CheckSpaceAvailable(s.space_id, p_expected_start_time, p_expected_end_time) = 1)
          OR (p_only_available = 1 AND (p_expected_start_time IS NULL OR p_expected_end_time IS NULL) AND s.current_status = 'AVAILABLE')
      )
    ORDER BY st.name, s.name;

    p_result_code := 0;
    p_result_msg := 'Lấy danh sách không gian thành công.';

EXCEPTION
    -- Lỗi truy xuất dữ liệu
    WHEN OTHERS THEN
        p_result_code := -1;
        p_result_msg := 'Không thể tải danh sách không gian lúc này. Vui lòng thử lại sau.';
END sp_GetActiveSpacesByBranch;
/