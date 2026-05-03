package com.wms.controller;

import com.wms.dao.ThongTinHoiVienDAO;
import com.wms.model.NguoiDungDTO;
import com.wms.model.HoiVienDTO;

public class ThongTinHoiVienController {

    private final ThongTinHoiVienDAO thongTinDAO;

    public ThongTinHoiVienController() {
        this.thongTinDAO = new ThongTinHoiVienDAO();
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
        return thongTinDAO.layThongTin(maND);
    }

    public boolean capNhatThongTin(HoiVienDTO dto) {
        String maND = getMaND();
        if (maND == null || maND.isEmpty()) return false;
        dto.setMaND(maND);
        return thongTinDAO.capNhatThongTin(dto);
    }

    public boolean doiMatKhau(String matKhauCu, String matKhauMoi) {
        String maND = getMaND();
        if (maND == null || maND.isEmpty()) return false;
        return thongTinDAO.doiMatKhau(maND, matKhauCu, matKhauMoi);
    }
}
