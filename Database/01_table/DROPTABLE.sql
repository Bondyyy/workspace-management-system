SET SERVEROUTPUT ON;

DECLARE
    TYPE t_trigger_list IS TABLE OF VARCHAR2(128);
    v_enabled_triggers t_trigger_list;

    PROCEDURE set_triggers_status(p_status VARCHAR2) IS
    BEGIN
        IF v_enabled_triggers IS NOT NULL THEN
            FOR i IN 1 .. v_enabled_triggers.COUNT LOOP
                EXECUTE IMMEDIATE
                    'ALTER TRIGGER "' || v_enabled_triggers(i) || '" ' || p_status;
            END LOOP;
        END IF;
    END;

    PROCEDURE delete_table(p_table_name VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE 'DELETE FROM ' || p_table_name;
        DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from ' || p_table_name);
    END;

BEGIN
    -- Lưu lại danh sách trigger đang ENABLED
    SELECT trigger_name
    BULK COLLECT INTO v_enabled_triggers
    FROM user_triggers
    WHERE status = 'ENABLED';

    -- Tạm tắt trigger để tránh trigger nghiệp vụ chặn xóa dữ liệu reset
    set_triggers_status('DISABLE');

    -- 1. Xóa các bảng chi tiết / bảng con trước
    delete_table('CHITIETDICHVU');
    delete_table('HOADON');
    delete_table('CHUNGTUNHAPKHO');

    -- 2. Xóa phiên và đặt chỗ
    delete_table('PHIENLAMVIEC');
    delete_table('DATCHO');

    -- 3. Xóa phiếu giảm giá trước nhân viên
    delete_table('PHIEUGIAMGIA');

    -- 4. Xóa bảng phân quyền chi tiết
    delete_table('CHITIETVAITRO');
    delete_table('CHITIETNHOMCHUCNANG');
    delete_table('CHITIETCHUCNANG');

    -- 5. Xử lý self-FK của NHANVIEN nếu có MaNQL trỏ tới nhân viên khác
    UPDATE NHANVIEN SET MaNQL = NULL;
    DBMS_OUTPUT.PUT_LINE('Updated MaNQL = NULL in NHANVIEN: ' || SQL%ROWCOUNT || ' rows');

    -- 6. Xóa khách hàng, nhân viên, người dùng
    delete_table('NHANVIEN');
    delete_table('KHACHHANG');
    delete_table('NGUOIDUNG');

    -- 7. Xóa không gian, dịch vụ
    delete_table('KHONGGIAN');
    delete_table('DICHVU');

    -- 8. Xóa bảng cha / danh mục
    delete_table('LOAIKHONGGIAN');
    delete_table('LOAIDICHVU');
    delete_table('CHINHANH');
    delete_table('HANGTHANHVIEN');
    delete_table('VAITRO');
    delete_table('NHOMCHUCNANG');
    delete_table('CHUCNANG');

    COMMIT;

    -- Bật lại trigger
    set_triggers_status('ENABLE');

    DBMS_OUTPUT.PUT_LINE('DONE: Da xoa het du lieu trong database, khong drop table.');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;

        BEGIN
            set_triggers_status('ENABLE');
        EXCEPTION
            WHEN OTHERS THEN
                NULL;
        END;

        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        RAISE;
END;
/

