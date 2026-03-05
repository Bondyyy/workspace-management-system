CREATE OR REPLACE TRIGGER trg_UpdateVoucherUsage
AFTER UPDATE OF status ON Payments
FOR EACH ROW
WHEN (NEW.status = 'SUCCESS' AND OLD.status != 'SUCCESS')
BEGIN
    -- Tìm tất cả các Booking có áp dụng voucher nằm trong hóa đơn vừa được thanh toán thành công
    FOR rec IN (
        SELECT b.voucher_id
        FROM InvoiceLines il
        JOIN Bookings b ON il.reference_id = b.booking_id --Chỉ cho khách có đk
        WHERE il.invoice_id = :NEW.invoice_id
          AND il.reference_type = 'BOOKING'
          AND b.voucher_id IS NOT NULL
    ) LOOP
        -- Tăng biến đếm số lượt sử dụng lên 1
        UPDATE Vouchers
        SET used_count = used_count + 1
        WHERE voucher_id = rec.voucher_id;
    END LOOP;
END;
/