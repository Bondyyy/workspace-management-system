CREATE OR REPLACE PROCEDURE sp_ProcessCheckIn (
    p_qr_code             IN VARCHAR2,        -- Truyền mã QR nếu khách đã đặt trước (NULL nếu là khách vãng lai)
    p_space_id            IN NUMBER,          -- Bắt buộc truyền nếu là khách vãng lai (NULL nếu dùng QR)
    p_customer_id         IN NUMBER,          -- Truyền nếu khách vãng lai là Thành viên (NULL nếu vãng lai chưa đăng ký hoặc dùng QR)
    p_check_in_staff_id   IN NUMBER,          -- ID của nhân viên (Staff/Lễ tân) thực hiện thao tác
    p_applied_hourly_rate IN NUMBER,          -- Giá theo giờ áp dụng cho khách vãng lai (NULL nếu dùng QR vì lấy từ Bookings)
    p_out_message         OUT VARCHAR2        -- Trả về thông báo kết quả
)
AS
    v_booking_id    NUMBER;
    v_customer_id   NUMBER;
    v_bkg_status    VARCHAR2(20);
    v_space_status  VARCHAR2(20);
BEGIN
    -- ==========================================
    -- KỊCH BẢN 1: KHÁCH CÓ MÃ QR (ĐÃ ĐẶT TRƯỚC)
    -- ==========================================
    IF p_qr_code IS NOT NULL THEN
        -- 1. Tìm thông tin Booking dựa trên mã QR
        BEGIN
            SELECT booking_id, customer_id, status
            INTO v_booking_id, v_customer_id, v_bkg_status
            FROM Bookings
            WHERE qr_code = p_qr_code
            FOR UPDATE NOWAIT; -- Khóa dòng Booking đang thao tác
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20001, 'Mã QR không hợp lệ hoặc không tồn tại trên hệ thống!');
        END;

        -- 2. Kiểm tra trạng thái Booking có hợp lệ để Check-in không
        IF v_bkg_status NOT IN ('PENDING', 'CONFIRMED') THEN
            RAISE_APPLICATION_ERROR(-20002, 'Phiếu đặt chỗ này không ở trạng thái hợp lệ để Check-in (Có thể đã check-in hoặc bị hủy).');
        END;

        -- 3. Cập nhật thời gian check-in của toàn bộ Booking
        UPDATE Bookings
        SET check_in_time = SYSTIMESTAMP,
            status = 'IN_USE'
        WHERE booking_id = v_booking_id;

        -- 4. Quét qua các không gian trong BookingDetails để tạo Session
        FOR rec IN (SELECT space_id, price_at_booking FROM BookingDetails WHERE booking_id = v_booking_id) LOOP

            -- Khóa và kiểm tra Space (Đề phòng có lỗi dữ liệu trước đó)
            SELECT current_status INTO v_space_status FROM Spaces WHERE space_id = rec.space_id FOR UPDATE NOWAIT;

            -- Tạo mới Session
            INSERT INTO Sessions (
                customer_id, booking_id, space_id, checkin_time,
                applied_hourly_rate, check_in_staff_id, status
            ) VALUES (
                v_customer_id, v_booking_id, rec.space_id, SYSTIMESTAMP,
                rec.price_at_booking, p_check_in_staff_id, 'ACTIVE'
            );

            -- Đổi trạng thái bàn/phòng thành Đang sử dụng
            UPDATE Spaces
            SET current_status = 'IN_USE'
            WHERE space_id = rec.space_id;

        END LOOP;

        p_out_message := 'Check-in thành công qua mã QR. Đã khởi tạo phiên làm việc.';

    -- ==========================================
    -- KỊCH BẢN 2: KHÁCH VÃNG LAI (WALK-IN)
    -- ==========================================
    ELSE
        -- 1. Kiểm tra đầu vào
        IF p_space_id IS NULL THEN
            RAISE_APPLICATION_ERROR(-20003, 'Lỗi: Khách vãng lai bắt buộc phải chọn không gian (space_id).');
        END IF;

        IF p_applied_hourly_rate IS NULL THEN
            RAISE_APPLICATION_ERROR(-20004, 'Lỗi: Cần cung cấp giá thuê theo giờ (applied_hourly_rate) cho khách vãng lai.');
        END IF;

        -- 2. Kiểm tra tình trạng của Không gian (Space)
        BEGIN
            SELECT current_status INTO v_space_status
            FROM Spaces
            WHERE space_id = p_space_id
            FOR UPDATE NOWAIT;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20005, 'Không tìm thấy không gian với ID cung cấp.');
        END;

        IF v_space_status != 'AVAILABLE' THEN
            RAISE_APPLICATION_ERROR(-20006, 'Không gian này hiện không trống. Vui lòng chọn vị trí khác!');
        END IF;

        -- 3. Tạo Session trực tiếp (Không có booking_id)
        INSERT INTO Sessions (
            customer_id, booking_id, space_id, checkin_time,
            applied_hourly_rate, check_in_staff_id, status
        ) VALUES (
            p_customer_id, NULL, p_space_id, SYSTIMESTAMP,
            p_applied_hourly_rate, p_check_in_staff_id, 'ACTIVE'
        );

        -- 4. Đổi trạng thái bàn/phòng
        UPDATE Spaces
        SET current_status = 'IN_USE'
        WHERE space_id = p_space_id;

        p_out_message := 'Check-in thành công cho khách vãng lai. Đã mở phiên mới.';
    END IF;

    -- Hoàn tất giao dịch
    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_ProcessCheckIn;
/