package com.wms.controller;

import com.wms.dao.PhieuGiamGiaDAO;
import com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO;
import java.util.List;

public class PhieuGiamGiaController {
    private final PhieuGiamGiaDAO dao;

    public PhieuGiamGiaController() {
        this.dao = new PhieuGiamGiaDAO();
    }

    public List<PhieuGiamGiaDTO> layDanhSach() {
        return dao.layDanhSach();
    }

    public boolean themMoi(PhieuGiamGiaDTO dto) {
        // Kiểm tra logic trước khi thêm
        if (dto.getGiaTriGiamGia() <= 0) return false;
        return dao.themMoi(dto);
    }

    public boolean capNhat(PhieuGiamGiaDTO dto) {
        return dao.capNhat(dto);
    }

    public boolean xoa(String maPGG) {
        return dao.xoa(maPGG);
    }

    public PhieuGiamGiaDTO timTheoMa(String maPGG) {
        return dao.timTheoMa(maPGG);
    }
}
