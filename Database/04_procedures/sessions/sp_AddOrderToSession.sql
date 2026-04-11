CREATE OR REPLACE PROCEDURE sp_AddOrderToSession (
    p_sessionId   IN NUMBER,
    p_productId   IN NUMBER,
    p_quantity    IN NUMBER,
    p_staffId     IN NUMBER,
    p_note        IN VARCHAR2,
    p_outMessage  OUT VARCHAR2
)
AS
    v_sessionStatus  VARCHAR2(20);
    v_orderId        NUMBER;
    v_unitPrice      NUMBER(15, 2);
    v_productStatus  VARCHAR2(20);
BEGIN
    -- 1. Validate session
    SELECT status INTO v_sessionStatus FROM Sessions WHERE session_id = p_sessionId;
    IF v_sessionStatus != 'ACTIVE' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Chỉ có thể thêm món cho phiên đang hoạt động!');
    END IF;

    -- 2. Validate product availability
    SELECT price, status INTO v_unitPrice, v_productStatus 
    FROM Products WHERE product_id = p_productId;
    IF v_productStatus != 'AVAILABLE' THEN
        RAISE_APPLICATION_ERROR(-20002, 'Sản phẩm này hiện đang hết hàng hoặc ngừng bán!');
    END IF;

    -- 3. Find existing pending order or create a new one
    BEGIN
        SELECT order_id INTO v_orderId FROM SessionOrders
        WHERE session_id = p_sessionId AND order_status IN ('PENDING', 'PREPARING')
        FETCH FIRST 1 ROWS ONLY; 
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            INSERT INTO SessionOrders (session_id, staff_id, order_status, total_price, payment_status)
            VALUES (p_sessionId, p_staffId, 'PENDING', 0, 'UNPAID')
            RETURNING order_id INTO v_orderId;
    END;

    -- 4. Add item to order details
    INSERT INTO SessionOrderDetails (order_id, product_id, quantity, unit_price, note)
    VALUES (v_orderId, p_productId, p_quantity, v_unitPrice, p_note);

    -- 5. Commit transaction
    COMMIT;
    p_outMessage := 'Đã thêm món vào đơn hàng thành công!';
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_AddOrderToSession;
/