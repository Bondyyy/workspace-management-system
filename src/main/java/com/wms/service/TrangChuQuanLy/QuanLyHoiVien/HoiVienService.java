package com.wms.service.TrangChuQuanLy.QuanLyHoiVien;

import com.wms.dao.TrangChuQuanLy.QuanLyHoiVien.KhachHangDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyHangThanhVien.HangThanhVienDAO;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;
import com.wms.util.HangThanhVienUtil;
import java.util.List;

public class HoiVienService {
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final HangThanhVienDAO hangDAO = new HangThanhVienDAO();

    public List<HoiVienDTO> layDanhSach() {
        return khachHangDAO.getAll();
    }

    public List<HoiVienDTO> timKiem(String keyword) {
        if (keyword == null || keyword.isBlank()) return khachHangDAO.getAll();
        return khachHangDAO.search(keyword.trim());
    }

    public void themMoi(HoiVienDTO dto) throws Exception {
        validate(dto);
        // Tự động tìm mã hạng nếu chỉ có tên hạng
        if (dto.getMaHangThanhVien() == null && dto.getHangThanhVien() != null) {
            dto.setMaHangThanhVien(hangDAO.getMaHangByName(dto.getHangThanhVien()));
        }
        ganHangMacDinhNeuCan(dto);
        khachHangDAO.insert(dto);
    }

    public void capNhat(HoiVienDTO dto) throws Exception {
        if (dto.getMaKH() == null || dto.getMaND() == null) {
            throw new Exception("Thiếu thông tin định danh!");
        }
        validate(dto);
        if (dto.getMaHangThanhVien() == null && dto.getHangThanhVien() != null) {
            dto.setMaHangThanhVien(hangDAO.getMaHangByName(dto.getHangThanhVien()));
        }
        ganHangMacDinhNeuCan(dto);
        khachHangDAO.update(dto);
    }

    public void xoa(String maKH, String maND) throws Exception {
        khachHangDAO.delete(maKH, maND);
    }

    public String generateMaKH() {
        return "";
    }

    private void validate(HoiVienDTO dto) throws Exception {
        if (dto.getHoTen() == null || dto.getHoTen().isBlank()) throw new Exception("Họ tên không được để trống!");
        if (dto.getSdt() == null || !dto.getSdt().matches("\\d{10,11}")) throw new Exception("Số điện thoại không hợp lệ!");
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Email không hợp lệ!");
        }
    }

    private void ganHangMacDinhNeuCan(HoiVienDTO dto) throws Exception {
        if (dto.getMaHangThanhVien() == null
                || dto.getMaHangThanhVien().isBlank()
                || HangThanhVienUtil.TEN_HANG_KHONG_CO.equalsIgnoreCase(dto.getHangThanhVien())) {
            String maHangMacDinh = hangDAO.getMaHangKhachHangMacDinh();
            if (maHangMacDinh == null || maHangMacDinh.isBlank()) {
                throw new Exception("Không tìm thấy hạng thành viên mặc định Đồng.");
            }
            dto.setMaHangThanhVien(maHangMacDinh);
            dto.setHangThanhVien(HangThanhVienUtil.TEN_HANG_DONG);
        }
    }
}
