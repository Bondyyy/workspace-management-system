package com.wms.service.TrangChuQuanLy.QuanLyChiNhanh;

import com.wms.dao.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;

import java.util.List;

public class ChiNhanhService {

    private final ChiNhanhDAO chiNhanhDAO = new ChiNhanhDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public List<ChiNhanhDTO> layDanhSach() {
        return chiNhanhDAO.layDanhSachChiNhanh();
    }

    public List<ChiNhanhDTO> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return chiNhanhDAO.layDanhSachChiNhanh();
        }
        return chiNhanhDAO.timKiemChiNhanh(tuKhoa.trim());
    }

    public String themMoi(ChiNhanhDTO cn) {
        String loi = validate(cn, false);
        if (loi != null) return loi;

        int count = chiNhanhDAO.demSoLuong();
        cn.setMaCN(String.format("CN%03d", count + 1));
        return chiNhanhDAO.themChiNhanh(cn) ? null : "Thêm chi nhánh thất bại, vui lòng thử lại.";
    }

    public String capNhat(ChiNhanhDTO cn) {
        if (cn.getMaCN() == null || cn.getMaCN().trim().isEmpty()) {
            return "Mã chi nhánh không được để trống khi cập nhật!";
        }
        String loi = validate(cn, true);
        if (loi != null) return loi;

        return chiNhanhDAO.capNhatChiNhanh(cn) ? null : "Cập nhật thất bại, vui lòng thử lại.";
    }

    public String voHieuHoa(String maCN) {
        if (maCN == null || maCN.trim().isEmpty()) {
            return "Mã chi nhánh không hợp lệ!";
        }
        return chiNhanhDAO.voHieuHoaChiNhanh(maCN) ? null : "Vô hiệu hóa thất bại, vui lòng thử lại.";
    }

    public String layMaCNTiepTheo() {
        int count = chiNhanhDAO.demSoLuong();
        return String.format("CN%03d", count + 1);
    }

    private String validate(ChiNhanhDTO cn, boolean isUpdate) {
        if (cn.getDiaChi() == null || cn.getDiaChi().trim().isEmpty()) {
            return "Địa chỉ không được để trống!";
        }
        if (cn.getDuongDayNong() != null && !cn.getDuongDayNong().matches("\\d{10,11}")) {
            return "Hotline phải là số điện thoại từ 10-11 chữ số!";
        }
        return null;
    }
}
