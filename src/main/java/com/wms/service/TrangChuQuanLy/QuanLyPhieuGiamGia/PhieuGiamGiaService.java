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
        kiemTraHopLe(dto);
        return dao.themMoi(dto);
    }

    public boolean capNhat(PhieuGiamGiaDTO dto) {
        kiemTraHopLe(dto);
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

    private void kiemTraHopLe(PhieuGiamGiaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Vui lòng nhập thông tin phiếu giảm giá.");
        }
        if (dto.getMaChuSoPGG() == null || dto.getMaChuSoPGG().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã số phiếu giảm giá.");
        }
        if (dto.getGiaTriGiamGia() <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải lớn hơn 0.");
        }
        if (dto.getSlToiDa() <= 0) {
            throw new IllegalArgumentException("Số lượng phát hành phải lớn hơn 0.");
        }
        if (dto.getGiaTriApDungToiThieu() < dto.getGiaTriGiamGia()) {
            throw new IllegalArgumentException("Đơn tối thiểu không được nhỏ hơn giá trị giảm.");
        }
        
        if (dto.getNgayBatDauApDung() != null && dto.getNgayKetThucApDung() != null) {
            if (dto.getNgayKetThucApDung().before(dto.getNgayBatDauApDung())) {
                throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
            }
        }
    }

    public String sinhMaMoi() {
        return dao.taoMaMoi();
    }
}
