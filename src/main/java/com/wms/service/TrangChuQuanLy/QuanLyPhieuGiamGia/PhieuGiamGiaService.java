package com.wms.service.TrangChuQuanLy.QuanLyPhieuGiamGia;

import com.wms.dao.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDAO;
import com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDTO;
import java.util.List;

public class PhieuGiamGiaService {
    private final PhieuGiamGiaDAO dao = new PhieuGiamGiaDAO();

    public List<PhieuGiamGiaDTO> layDanhSach() {
        return dao.layDanhSach();
    }

    public boolean themMoi(PhieuGiamGiaDTO dto) {
        if (dto.getMaPGG() == null || dto.getMaPGG().isBlank()) {
            dto.setMaPGG(sinhMaMoi());
        }
        
        if (!kiemTraHopLe(dto)) return false;
        
        return dao.themMoi(dto);
    }

    public boolean capNhat(PhieuGiamGiaDTO dto) {
        if (!kiemTraHopLe(dto)) return false;
        return dao.capNhat(dto);
    }

    public boolean xoa(String maPGG) {
        if (maPGG == null || maPGG.isBlank()) return false;
        return dao.xoa(maPGG);
    }

    public PhieuGiamGiaDTO timTheoMa(String maPGG) {
        if (maPGG == null || maPGG.isBlank()) return null;
        return dao.timTheoMa(maPGG);
    }

    public List<PhieuGiamGiaDTO> timKiem(String keyword) {
        if (keyword == null || keyword.isBlank()) return layDanhSach();
        return dao.timKiem(keyword);
    }

    private boolean kiemTraHopLe(PhieuGiamGiaDTO dto) {
        if (dto.getMaChuSoPGG() == null || dto.getMaChuSoPGG().isBlank()) {
            System.err.println("Mã nhập khuyến mãi không được để trống.");
            return false;
        }
        if (dto.getGiaTriGiamGia() <= 0) {
            System.err.println("Giá trị giảm phải lớn hơn 0.");
            return false;
        }
        if (dto.getSlToiDa() <= 0) {
            System.err.println("Số lượng phát hành phải lớn hơn 0.");
            return false;
        }
        if (dto.getGiaTriApDungToiThieu() < dto.getGiaTriGiamGia()) {
            System.err.println("Đơn tối thiểu không được nhỏ hơn giá trị giảm.");
            return false;
        }
        
        if (dto.getNgayBatDauApDung() != null && dto.getNgayKetThucApDung() != null) {
            if (dto.getNgayKetThucApDung().before(dto.getNgayBatDauApDung())) {
                System.err.println("Ngày kết thúc phải sau ngày bắt đầu.");
                return false;
            }
        }
        return true;
    }

    public String sinhMaMoi() {
        int count = dao.demSoLuong();
        return String.format("PGG%03d", count + 1);
    }
}
