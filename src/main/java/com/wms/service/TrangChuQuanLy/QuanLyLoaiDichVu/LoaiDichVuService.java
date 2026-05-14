package com.wms.service.TrangChuQuanLy.QuanLyLoaiDichVu;

import com.wms.dao.TrangChuQuanLy.QuanLyLoaiDichVu.LoaiDichVuDAO;
import com.wms.model.TrangChuQuanLy.QuanLyLoaiDichVu.LoaiDichVuDTO;

import java.sql.SQLException;
import java.util.List;

public class LoaiDichVuService {
    private final LoaiDichVuDAO dao;

    public LoaiDichVuService() {
        this.dao = new LoaiDichVuDAO();
    }

    public List<LoaiDichVuDTO> getAllLoaiDichVu() throws SQLException {
        return dao.layTatCa();
    }

    public List<LoaiDichVuDTO> searchLoaiDichVu(String keyword) throws SQLException {
        return dao.search(keyword);
    }

    public boolean addLoaiDichVu(LoaiDichVuDTO loai) throws SQLException {
        validate(loai);
        loai.setMaLoaiDV(dao.generateNextMa());
        return dao.them(loai);
    }

    public boolean updateLoaiDichVu(LoaiDichVuDTO loai) throws SQLException {
        validate(loai);
        return dao.capNhat(loai);
    }

    private void validate(LoaiDichVuDTO loai) {
        if (loai.getTenLoaiDV() == null || loai.getTenLoaiDV().trim().isEmpty()) {
            throw new RuntimeException("Tên loại dịch vụ không được để trống!");
        }
    }
}
