create or replace PROCEDURE SP_ThanhToanVoiPhieuGiamGia(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_MaPGG IN VARCHAR2,
    p_PhuongThucTT IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_TongTien NUMBER(18, 2);
    v_ThanhTienGoc NUMBER(18, 2);
    v_ThanhTienSauGiam NUMBER(18, 2);
    v_SoLuongDaDung NUMBER;
    v_SoLuongToiDa NUMBER;

    -- Khai báo ngoại lệ người dùng tự định nghĩa
    ex_het_ma_giam_gia EXCEPTION;
BEGIN
    -- t0: Khởi tạo giao dịch
    SET TRANSACTION NAME 'thanh_toan_phieu_giam_gia';

    -- t1: Đóng phiên làm việc và giải phóng không gian
    UPDATE PHIENLAMVIEC
    SET ThoiGianKetThuc = SYSTIMESTAMP,
        TrangThaiPhien = 'Đã kết thúc',
        CapNhatLanCuoi = SYSTIMESTAMP
    WHERE MaPhien = p_MaPhien;

    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Dọn dẹp'
    WHERE MaKG = (SELECT MaKG FROM PHIENLAMVIEC WHERE MaPhien = p_MaPhien);

    -- Lấy thông tin giá trị phiên
    v_TongTien := FN_TinhTongTien(p_MaPhien);
    v_ThanhTienGoc := FN_TinhThanhTien(p_MaPhien, NULL);

    -- t2: Đặt điểm neo bảo vệ các thao tác giải phóng không gian ở trên
    SAVEPOINT sv_truoc_khi_ap_ma;

    -- Bắt đầu luồng kiểm tra mã giảm giá
    IF p_MaPGG IS NOT NULL THEN
        -- t3: Giữ khóa độc quyền trên dòng mã giảm giá để kiểm tra
        SELECT SLDaDung, SLToiDa
        INTO v_SoLuongDaDung, v_SoLuongToiDa
        FROM PHIEUGIAMGIA
        WHERE MaPGG = p_MaPGG FOR UPDATE NOWAIT;

        -- t5: Kích hoạt ngoại lệ nếu mã không còn lượt sử dụng
        IF v_SoLuongDaDung >= v_SoLuongToiDa THEN
            RAISE ex_het_ma_giam_gia;
        END IF;

        -- t4: Trừ lượt mã giảm giá
        UPDATE PHIEUGIAMGIA
        SET SLDaDung = SLDaDung + 1
        WHERE MaPGG = p_MaPGG;

        -- Tính tiền có giảm giá và chốt hóa đơn
        v_ThanhTienSauGiam := FN_TinhThanhTien(p_MaPhien, p_MaPGG);

        UPDATE HOADON
        SET TongTien = v_TongTien,
            ThanhTien = v_ThanhTienSauGiam,
            MaPGG = p_MaPGG,
            TrangThaiThanhToan = 'Đã thanh toán thành công',
            PhuongThucThanhToan = p_PhuongThucTT,
            NgayLapHoaDon = SYSTIMESTAMP,
            MaNV = p_MaNV
        WHERE MaPhien = p_MaPhien;

        p_outMessage := 'Thanh toán thành công (Đã áp mã)! Thành tiền: ' || v_ThanhTienSauGiam || ' VNĐ';
    ELSE
        -- Khách hàng không truyền vào mã giảm giá
        UPDATE HOADON
        SET TongTien = v_TongTien,
            ThanhTien = v_ThanhTienGoc,
            TrangThaiThanhToan = 'Đã thanh toán thành công',
            PhuongThucThanhToan = p_PhuongThucTT,
            NgayLapHoaDon = SYSTIMESTAMP,
            MaNV = p_MaNV
        WHERE MaPhien = p_MaPhien;

        p_outMessage := 'Thanh toán thành công (Giá gốc)! Thành tiền: ' || v_ThanhTienGoc || ' VNĐ';
    END IF;

    -- t8: Xác nhận toàn bộ giao dịch nếu không có ngoại lệ nào xảy ra
    COMMIT;

EXCEPTION
    WHEN ex_het_ma_giam_gia THEN
        -- t6: Hủy bỏ việc thao tác trên bảng PHIEUGIAMGIA, nhả Row Lock
        ROLLBACK TO sv_truoc_khi_ap_ma;

        -- t7: Chuyển hướng nghiệp vụ -> Thu tiền giá gốc
        UPDATE HOADON
        SET TongTien = v_TongTien,
            ThanhTien = v_ThanhTienGoc,
            TrangThaiThanhToan = 'Đã thanh toán thành công',
            PhuongThucThanhToan = p_PhuongThucTT,
            NgayLapHoaDon = SYSTIMESTAMP,
            MaNV = p_MaNV
        WHERE MaPhien = p_MaPhien;


        -- Chốt giao dịch với giá gốc
        DBMS_SESSION.SLEEP(10);
        COMMIT;
        p_outMessage := 'Mã giảm giá đã hết lượt. Thanh toán thành công giá gốc: ' || v_ThanhTienGoc || ' VNĐ';

    WHEN OTHERS THEN
        -- Bắt các lỗi hệ thống nghiêm trọng khác (Deadlock, đứt mạng...) -> Hủy toàn bộ
        ROLLBACK;
        p_outMessage := 'Lỗi thanh toán: ' || SQLERRM;
END SP_ThanhToanVoiPhieuGiamGia;
/

