package com.wms.controller.TrangChuQuanLy.QuanLyHoiVien;

import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.service.TrangChuQuanLy.QuanLyHoiVien.ThongTinHoiVienService;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;

public class ThongTinHoiVienController {

    private final ThongTinHoiVienService service;

    public ThongTinHoiVienController() {
        this.service = new ThongTinHoiVienService();
    }

    private String getMaND() {
        NguoiDungDTO user = DangNhapController.getCurrentUser();
        return user != null ? user.getMaND() : "";
    }

    public HoiVienDTO layThongTin() {
        String maND = getMaND();
        if (maND == null || maND.isEmpty()) {
            return new HoiVienDTO();
        }
        return service.layThongTin(maND);
    }

    public boolean capNhatThongTin(HoiVienDTO dto) {
        String maND = getMaND();
        if (maND == null || maND.isEmpty()) return false;
        dto.setMaND(maND);
        return service.capNhatThongTin(dto);
    }

    public boolean doiMatKhau(String matKhauCu, String matKhauMoi) {
        String maND = getMaND();
        if (maND == null || maND.isEmpty()) return false;
        return service.doiMatKhau(maND, matKhauCu, matKhauMoi);
    }
}
