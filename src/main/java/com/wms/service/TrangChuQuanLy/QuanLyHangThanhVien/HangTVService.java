package com.wms.service.TrangChuQuanLy.QuanLyHangThanhVien;

import com.wms.dao.TrangChuQuanLy.QuanLyHangThanhVien.HangThanhVienDAO;
import com.wms.model.TrangChuQuanLy.QuanLyHangThanhVien.HangThanhVienDTO;
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

    public void capNhatHangThanhVien(HangThanhVienDTO dto) throws Exception {
        if (dto.getPhanTramTienGiam() < 0 || dto.getPhanTramTienGiam() > 100) {
            throw new Exception("Tỉ lệ giảm giá phải từ 0% đến 100%!");
        }
        if (dto.getTongChiTieuToiThieu() < 0) {
            throw new Exception("Chi tiêu tối thiểu không được nhỏ hơn 0 VNĐ!");
        }
        hangDAO.update(dto);
    }
}
