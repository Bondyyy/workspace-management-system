package com.wms.service.TrangChuQuanLy.QuanLyThongTinDichVu;

import com.wms.dao.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyThongTinDichVu.LoaiDichVuDAO;
import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO;
import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.LoaiDichVuDTO;
import java.util.List;

public class DichVuService {
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final LoaiDichVuDAO loaiDichVuDAO = new LoaiDichVuDAO();

    // Quản lý Dịch vụ
    public List<DichVuDTO> layDanhSachDichVu(String maLoai, String trangThai, String tuKhoa) {
        return dichVuDAO.layDanhSachDichVu(maLoai, trangThai, tuKhoa);
    }

    public boolean themDichVu(DichVuDTO dv) throws Exception {
        validateDichVu(dv);
        return dichVuDAO.themDichVu(dv);
    }

    public boolean capNhatDichVu(DichVuDTO dv) throws Exception {
        if (dv.getMaDV() == null || dv.getMaDV().isBlank()) {
            throw new Exception("Mã dịch vụ không hợp lệ!");
        }
        validateDichVu(dv);
        return dichVuDAO.capNhatDichVu(dv);
    }

    private void validateDichVu(DichVuDTO dv) throws Exception {
        if (dv.getTenDV() == null || dv.getTenDV().isBlank())
            throw new Exception("Tên dịch vụ không được để trống!");
        if (dv.getMaLoaiDV() == null || dv.getMaLoaiDV().isBlank())
            throw new Exception("Vui lòng chọn loại dịch vụ!");
        if (dv.getDonGia() < 0)
            throw new Exception("Đơn giá không được âm!");
    }

    public String generateMaDV() {
        return "DV" + (System.currentTimeMillis() % 1000000);
    }

    // Quản lý Loại dịch vụ
    public List<LoaiDichVuDTO> layTatCaLoai() {
        return loaiDichVuDAO.layTatCa();
    }

    public boolean themLoaiDichVu(LoaiDichVuDTO l) throws Exception {
        if (l.getTenLoaiDV() == null || l.getTenLoaiDV().isBlank())
            throw new Exception("Tên loại không được để trống!");
        return loaiDichVuDAO.them(l);
    }

    public boolean capNhatLoaiDichVu(LoaiDichVuDTO l) throws Exception {
        if (l.getMaLoaiDV() == null || l.getMaLoaiDV().isBlank())
            throw new Exception("Mã loại không hợp lệ!");
        if (l.getTenLoaiDV() == null || l.getTenLoaiDV().isBlank())
            throw new Exception("Tên loại không được để trống!");
        return loaiDichVuDAO.capNhat(l);
    }

    public String generateMaLoai() {
        return "LDV" + (System.currentTimeMillis() % 1000000);
    }
}
