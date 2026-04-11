CREATE OR REPLACE PROCEDURE sp_DeleteVoucher (
    p_voucher_id IN NUMBER,
    p_result_code OUT NUMBER,   -- 0: thành công, 1: lỗi nghiệp vụ, -1: lỗi hệ thống
    p_result_msg OUT VARCHAR2
)
IS
    v_exists NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_exists
    FROM Vouchers
    WHERE voucher_id = p_voucher_id;

    IF v_exists = 0 THEN
        p_result_code := 1;
        p_result_msg := 'Không tìm thấy voucher.';
        RETURN;
    END IF;

    DELETE FROM Vouchers
    WHERE voucher_id = p_voucher_id;

    COMMIT;

    p_result_code := 0;
    p_result_msg := 'Xóa voucher thành công.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result_code := -1;
        p_result_msg := 'Lỗi hệ thống: ' || SQLERRM;
END sp_DeleteVoucher;
/