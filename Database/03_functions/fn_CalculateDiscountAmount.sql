CREATE OR REPLACE FUNCTION fn_CalculateDiscountAmount(
    p_customer_id IN NUMBER,
    p_voucher_id IN NUMBER,
    p_subtotal_amount IN NUMBER
) RETURN NUMBER IS
    v_total_discount NUMBER(15, 2) := 0;
    v_member_discount NUMBER(15, 2) := 0;
    v_voucher_discount NUMBER(15, 2) := 0;

    -- Biến lưu trữ thông tin Voucher
    v_v_type VARCHAR2(20);
    v_v_value NUMBER(15, 2);
    v_v_min_order NUMBER(15, 2);
    v_v_max_discount NUMBER(15, 2);
    v_v_usage_limit NUMBER;
    v_v_used_count NUMBER;
    v_v_expiry DATE;
    v_v_active NUMBER(1);

    -- Biến lưu trữ phần trăm Hạng thành viên
    v_m_percent NUMBER(5, 2);
BEGIN
    -- PHẦN 1: TÍNH GIẢM GIÁ TỪ HẠNG THÀNH VIÊN
    IF p_customer_id IS NOT NULL THEN
        BEGIN
            -- Truy xuất phần trăm giảm giá từ MembershipTiers
            SELECT mt.discount_percent
            INTO v_m_percent
            FROM Customers c
            JOIN MembershipTiers mt ON c.membership_tier_id = mt.tier_id
            WHERE c.customer_id = p_customer_id;

            -- Tính số tiền giảm
            IF v_m_percent > 0 THEN
                v_member_discount := p_subtotal_amount * (v_m_percent / 100);
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- Nếu khách hàng là khách vãng lai hoặc chưa có hạng, tiền giảm = 0
                v_member_discount := 0;
        END;
    END IF;

    -- PHẦN 2: TÍNH GIẢM GIÁ TỪ VOUCHER
    IF p_voucher_id IS NOT NULL THEN
        BEGIN
            -- Truy xuất toàn bộ quy tắc của mã Voucher
            SELECT discount_type, discount_value, min_order_value, max_discount,
                   usage_limit, used_count, expiry_date, is_active
            INTO v_v_type, v_v_value, v_v_min_order, v_v_max_discount,
                 v_v_usage_limit, v_v_used_count, v_v_expiry, v_v_active
            FROM Vouchers
            WHERE voucher_id = p_voucher_id;

            -- KIỂM TRA ĐIỀU KIỆN ÁP DỤNG:
            -- 1. Voucher phải đang Active
            -- 2. Chưa hết hạn
            -- 3. Chưa vượt quá giới hạn số lượt dùng (nếu có set usage_limit)
            -- 4. Tổng tiền đơn hàng phải lớn hơn hoặc bằng mức tối thiểu
            IF v_v_active = 1
               AND v_v_expiry >= TRUNC(SYSDATE)
               AND (v_v_usage_limit IS NULL OR v_v_used_count < v_v_usage_limit)
               AND p_subtotal_amount >= v_v_min_order THEN

                -- Áp dụng logic tính toán theo loại
                IF v_v_type = 'PERCENT' THEN
                    v_voucher_discount := p_subtotal_amount * (v_v_value / 100);

                    -- Chốt chặn: Không cho phép giảm vượt quá mức tối đa (max_discount)
                    IF v_v_max_discount IS NOT NULL AND v_voucher_discount > v_v_max_discount THEN
                        v_voucher_discount := v_v_max_discount;
                    END IF;

                ELSIF v_v_type = 'FIXED' THEN
                    v_voucher_discount := v_v_value;
                END IF;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_voucher_discount := 0; -- Tránh lỗi crash hệ thống nếu truyền sai ID voucher
        END;
    END IF;

    -- PHẦN 3: TỔNG HỢP CHI PHÍ
    v_total_discount := v_member_discount + v_voucher_discount;

    -- Chốt chặn cuối cùng: Tiền giảm giá không bao giờ được lớn hơn tổng tiền của đơn hàng
    IF v_total_discount > p_subtotal_amount THEN
        v_total_discount := p_subtotal_amount;
    END IF;

    RETURN v_total_discount;
END;
