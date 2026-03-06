CREATE OR REPLACE FUNCTION fn_CheckSpaceAvailable ( 
    p_space_id IN NUMBER,
    p_start_time IN TIMESTAMP,
    p_end_time IN TIMESTAMP
) RETURN NUMBER -- Oracle SQL không có kiểu BOOLEAN thuần, ta dùng NUMBER: Trả về 1 (Trống), 0 (Đã bị chiếm)
IS
    v_count NUMBER;
    v_current_status VARCHAR2(20);
BEGIN
    -- ==============================================================================
    -- RULE 1: Kiểm tra tính hợp lệ của thời gian đầu vào
    -- ==============================================================================
    IF p_start_time >= p_end_time THEN
        RETURN 0; -- Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc
    END IF;

    -- ==============================================================================
    -- RULE 2: Kiểm tra trạng thái hiện tại (Chỉ áp dụng nếu khách muốn sử dụng NGAY)
    -- ==============================================================================
    -- Nếu thời gian bắt đầu <= thời gian hiện tại (cộng thêm 15 phút du di)
    IF p_start_time <= SYSTIMESTAMP + INTERVAL '15' MINUTE THEN
        SELECT current_status INTO v_current_status
        FROM Spaces
        WHERE space_id = p_space_id;

        IF v_current_status != 'AVAILABLE' THEN
            RETURN 0; -- Không gian hiện đang có người ngồi hoặc đang bảo trì
        END IF;
    END IF;

    -- ==============================================================================
    -- RULE 3: Quét bảng BookingDetails để tìm các khoảng thời gian bị TRÙNG LẶP
    -- ==============================================================================
    -- Công thức chuẩn để check overlap thời gian: Start_Mới < End_Cũ AND End_Mới > Start_Cũ
    SELECT COUNT(*)
    INTO v_count
    FROM BookingDetails bd
    JOIN Bookings b ON bd.booking_id = b.booking_id
    WHERE bd.space_id = p_space_id
      AND b.status NOT IN ('CANCELLED', 'COMPLETED', 'REJECTED') -- Bỏ qua các phiếu đã hủy hoặc đã xong
      AND p_start_time < bd.expected_end_time 
      AND p_end_time > bd.expected_start_time;

    IF v_count > 0 THEN
        RETURN 0; -- Bị trùng với một đơn đặt chỗ khác trong tương lai
    END IF;

    -- ==============================================================================
    -- RULE 4: Quét bảng Sessions & SessionExtensions (Phòng hờ khách gia hạn lố giờ)
    -- ==============================================================================
    -- Nếu có khách đang ngồi (ACTIVE) và họ vừa xin Lễ tân gia hạn (Extension) 
    -- lấn sang luôn cả cái khung giờ mà khách Online định đặt.
    SELECT COUNT(*)
    INTO v_count
    FROM Sessions s
    JOIN SessionExtensions ext ON s.session_id = ext.session_id
    WHERE s.space_id = p_space_id
      AND s.status = 'ACTIVE'
      AND p_start_time < ext.end_time -- Khung giờ đặt mới nằm trong khoảng khách cũ đang xin gia hạn
      AND p_end_time > ext.start_time;

    IF v_count > 0 THEN
        RETURN 0; -- Bị trùng do khách hiện tại đang gia hạn thêm giờ
    END IF;

    -- Vượt qua tất cả các bài kiểm tra -> Không gian này TRỐNG
    RETURN 1;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0; -- Không tìm thấy Space ID này
    WHEN OTHERS THEN
        RETURN 0; -- Lỗi hệ thống, an toàn nhất là báo Không Trống
END fn_CheckSpaceAvailable;
/
