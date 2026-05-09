package com.wms.service.TrangChuQuanLy.QuanLyKhongGian;

import com.wms.dao.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyKhongGian.KhongGianDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyKhongGian.LoaiKhongGianDAO;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.LoaiKhongGianDTO;

import java.time.LocalTime;
import java.util.List;

public class KhongGianService {

    private final KhongGianDAO kgDAO = new KhongGianDAO();
    private final LoaiKhongGianDAO lkgDAO = new LoaiKhongGianDAO();
    private final ChiNhanhDAO cnDAO = new ChiNhanhDAO();

    public List<KhongGianDTO> timKiemKhongGian(String tuKhoa, String maCN, String maLoaiKG) {
        return kgDAO.timKiem(tuKhoa, maCN, maLoaiKG);
    }

    public boolean themKhongGian(KhongGianDTO dto) {
        if (dto.getMaKG() == null || dto.getMaKG().isBlank()) {
            dto.setMaKG(kgDAO.taoMaMoi());
        }
        return kgDAO.them(dto);
    }

    public boolean capNhatKhongGian(KhongGianDTO dto) {
        return kgDAO.capNhat(dto);
    }

    public KhongGianDTO layKhongGianTheoMa(String maKG) {
        return kgDAO.layTheoMa(maKG);
    }

    public List<LoaiKhongGianDTO> layTatCaLoai() {
        return lkgDAO.layTatCaLoaiKhongGian();
    }

    public List<LoaiKhongGianDTO> timKiemLoai(String tuKhoa) {
        return lkgDAO.timKiemLoaiKhongGian(tuKhoa);
    }

    public boolean themLoai(LoaiKhongGianDTO dto) {
        if (dto.getMaLoaiKG() == null || dto.getMaLoaiKG().isBlank()) {
            dto.setMaLoaiKG(lkgDAO.taoMaMoi());
        }
        return lkgDAO.them(dto);
    }

    public boolean capNhatLoai(LoaiKhongGianDTO dto) {
        return lkgDAO.capNhat(dto);
    }

    public List<ChiNhanhDTO> layDanhSachChiNhanh() {
        return cnDAO.layDanhSachChiNhanh();
    }

    public List<String> layTenChiNhanhHoatDong() {
        return kgDAO.layDanhSachChiNhanhHoatDong();
    }

    public List<String> layTenLoaiKhongGian() {
        return kgDAO.layDanhSachLoaiKhongGian();
    }

    public List<KhongGianDTO> layTheoChiNhanh(String maCN) {
        return kgDAO.layTheoChiNhanh(maCN);
    }

    public boolean kiemTraTinhTrang(String tenKhongGian, String ngayDat, String gioToi) {
        if (tenKhongGian == null || tenKhongGian.trim().isEmpty()) return false;
        return kgDAO.kiemTraTinhTrangKhongGian(tenKhongGian, ngayDat, gioToi);
    }

    public LocalTime layGioDongCua(String tenChiNhanh) {
        if (tenChiNhanh == null || tenChiNhanh.trim().isEmpty()) return LocalTime.of(22, 0);
        return kgDAO.layGioDongCuaCuaChiNhanh(tenChiNhanh);
    }

    public String luuToaDo(List<KhongGianDTO> danhSach) {
        boolean allOk = true;
        for (KhongGianDTO dto : danhSach) {
            if (!kgDAO.capNhat(dto)) allOk = false;
        }
        return allOk ? null : "Có lỗi khi lưu tọa độ không gian.";
    }

    public String sinhMaKG() {
        int count = kgDAO.demSoLuong();
        return String.format("KG%03d", count + 1);
    }

    public String sinhMaLoaiKG() {
        return lkgDAO.taoMaMoi();
    }
}
