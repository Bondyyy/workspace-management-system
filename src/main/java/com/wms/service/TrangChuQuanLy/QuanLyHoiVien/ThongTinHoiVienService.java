package com.wms.service.TrangChuQuanLy.QuanLyHoiVien;

import com.wms.dao.TrangChuQuanLy.QuanLyHoiVien.ThongTinHoiVienDAO;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;

public class ThongTinHoiVienService {
    private final ThongTinHoiVienDAO thongTinDAO = new ThongTinHoiVienDAO();

    public HoiVienDTO layThongTin(String maND) {
        if (maND == null || maND.isEmpty()) return new HoiVienDTO();
        return thongTinDAO.layThongTin(maND);
    }

    public boolean capNhatThongTin(HoiVienDTO dto) {
        if (dto.getMaND() == null || dto.getMaND().isEmpty()) return false;
        return thongTinDAO.capNhatThongTin(dto);
    }

    public boolean doiMatKhau(String maND, String matKhauCu, String matKhauMoi) {
        if (maND == null || maND.isEmpty()) return false;
        return thongTinDAO.doiMatKhau(maND, matKhauCu, matKhauMoi);
    }
}
