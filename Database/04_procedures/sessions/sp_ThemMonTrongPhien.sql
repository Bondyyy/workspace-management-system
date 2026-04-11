CREATE OR REPLACE PROCEDURE sp_ThemMonTrongPhien (
    p_session_id  IN NUMBER,
    p_staff_id    IN NUMBER,
    p_product_id  IN NUMBER,
    p_quantity    IN NUMBER,
    p_note        IN VARCHAR2,
    p_out_message OUT VARCHAR2
)
AS
    v_session_status VARCHAR2(20);
    v_order_id       NUMBER;
    v_unit_price     NUMBER(15, 2);
    v_product_status VARCHAR2(20);
BEGIN
    -- 1. Kiểm tra session có đang hoạt động không
    SELECT status INTO v_session_status
    FROM Sessions
    WHERE session_id = p_session_id;

    IF v_session_status != 'ACTIVE' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Phiên làm việc không ở trạng thái ACTIVE!');
    END IF;

    -- 2. Kiểm tra món ăn có đang khả dụng không và lấy giá hiện tại
    SELECT price, status INTO v_unit_price, v_product_status
    FROM Products
    WHERE product_id = p_product_id;

    IF v_product_status != 'AVAILABLE' THEN
        RAISE_APPLICATION_ERROR(-20002, 'Sản phẩm này hiện không khả dụng (ngừng bán/hết hàng)!');
    END IF;

    IF p_quantity <= 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Số lượng phải lớn hơn 0!');
    END IF;

    -- 3. Tìm xem Phiên này đã có Order nào đang mở (chưa phục vụ xong) chưa
    BEGIN
        SELECT order_id INTO v_order_id
        FROM SessionOrders
        WHERE session_id = p_session_id AND order_status IN ('PENDING', 'PREPARING')
        FETCH FIRST 1 ROWS ONLY; 
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Chưa có thì khởi tạo 1 Order giỏ hàng mới
            INSERT INTO SessionOrders (session_id, staff_id, order_status, total_price, payment_status)
            VALUES (p_session_id, p_staff_id, 'PENDING', 0, 'UNPAID')
            RETURNING order_id INTO v_order_id;
    END;

    -- 4. Thêm chi tiết món vào Order 
    -- (Trigger sẽ tự tính subtotal và cộng dồn vào total_price của SessionOrders)
    INSERT INTO SessionOrderDetails (order_id, product_id, quantity, unit_price, note)
    VALUES (v_order_id, p_product_id, p_quantity, v_unit_price, p_note);

    COMMIT;
    p_out_message := 'Thêm món thành công vào Order #' || v_order_id;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20004, 'Không tìm thấy thông tin hợp lệ (Sai ID)!');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_ThemMonVaoPhien;
/