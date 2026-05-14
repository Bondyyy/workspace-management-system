package com.wms.service.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.HangThanhVienDAO;
import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.HangThanhVienDTO;
import java.util.List;

public class HangTVService {
    private final HangThanhVienDAO hangDAO = new HangThanhVienDAO();

    public List<HangThanhVienDTO> layDanhSachHang() {
        return hangDAO.getAll();
    }

    public void capNhatChinhSachGiamGia(String maHang, double discount) throws Exception {
        if (discount < 0 || discount > 100) throw new Exception("Tỉ lệ giảm giá phải từ 0% đến 100%!");
        hangDAO.updateDiscount(maHang, discount);
    }
}
