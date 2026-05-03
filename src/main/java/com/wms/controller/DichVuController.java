package com.wms.controller;

import com.wms.dao.DichVuDAO;
import com.wms.dao.LoaiDichVuDAO;
import com.wms.model.DichVuDTO;
import com.wms.model.LoaiDichVuDTO;
import java.util.List;

public class DichVuController {
    private final DichVuDAO dichVuDAO;
    private final LoaiDichVuDAO loaiDichVuDAO;

    public DichVuController() {
        this.dichVuDAO = new DichVuDAO();
        this.loaiDichVuDAO = new LoaiDichVuDAO();
    }

    public List<DichVuDTO> layDanhSach(String maLoai, String trangThai, String tuKhoa) {
        return dichVuDAO.layDanhSachDichVu(maLoai, trangThai, tuKhoa);
    }

    public List<LoaiDichVuDTO> layDanhSachLoai() {
        return loaiDichVuDAO.layTatCa();
    }

    public boolean themMoi(DichVuDTO dv) {
        return dichVuDAO.themDichVu(dv);
    }

    public boolean capNhat(DichVuDTO dv) {
        return dichVuDAO.capNhatDichVu(dv);
    }
}
