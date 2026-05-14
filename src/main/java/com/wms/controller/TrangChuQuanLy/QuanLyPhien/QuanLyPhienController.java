package com.wms.controller.TrangChuQuanLy.QuanLyPhien;

import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO;
import com.wms.service.TrangChuQuanLy.QuanLyPhien.PhienLamViecService;
import com.wms.view.TrangChuQuanLy.QuanLyPhien.QuanLyPhienForm;

import java.util.List;

public class QuanLyPhienController {

    private final QuanLyPhienForm view;
    private final PhienLamViecService service;

    public QuanLyPhienController(QuanLyPhienForm view) {
        this.view = view;
        this.service = new PhienLamViecService();
    }

    public void loadDanhSachPhien(String keyword, String maCN) {
        view.hienThiDanhSachPhien(service.layDanhSachPhien(keyword, maCN));
    }

    public void loadChiTietDichVu(String maPhien) {
        view.hienThiDichVuTrongPhien(service.layDichVuCuaPhien(maPhien));
    }

    public boolean ketThucPhien(String maPhien) {
        return service.ketThucPhien(maPhien);
    }

    public boolean xacNhanThanhToanDatTruoc(PhienLamViecFullDTO phien) {
        return service.xacNhanThanhToanDatTruoc(phien.getMaDatCho(), phien.getMaPhien());
    }

    /** Trả về danh sách [maCN, tenCN] chi nhánh đang hoạt động. */
    public List<String[]> layDanhSachChiNhanh() {
        return service.layDanhSachChiNhanh();
    }

    /** Lấy mã chi nhánh của nhân viên hiện đang đăng nhập. */
    public String layMaCNNguoiDung(NguoiDungDTO user) {
        return service.layMaCNTheNguoiDung(user);
    }
    public boolean xoaPhien(String maPhien) {
        return service.xoaPhien(maPhien);
    }
    public boolean capNhatPhien(String maPhien, String trangThai, String tenKH) {
        return service.capNhatPhien(maPhien, trangThai, tenKH);
    }
}
