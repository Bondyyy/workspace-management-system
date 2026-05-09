package com.wms.service.TrangChuQuanLy.QuanLyPhien;

import com.wms.dao.DatChoDAO;
import com.wms.dao.HoaDonDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyHoiVien.KhachHangDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyKhongGian.KhongGianDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyPhien.PhienLamViecDAO;
import com.wms.model.DichVuTrongPhienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import com.wms.model.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecDTO;
import com.wms.model.PhienLamViecFullDTO;

import java.util.List;

public class PhienLamViecService {

    private final PhienLamViecDAO phienDAO;
    private final KhachHangDAO khachHangDAO;
    private final KhongGianDAO khongGianDAO;
    private final NhanVienDAO nhanVienDAO;
    private final DatChoDAO datChoDAO;
    private final HoaDonDAO hoaDonDAO;

    public PhienLamViecService() {
        this.phienDAO = new PhienLamViecDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.khongGianDAO = new KhongGianDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.datChoDAO = new DatChoDAO();
        this.hoaDonDAO = new HoaDonDAO();
    }

    public List<PhienLamViecFullDTO> layDanhSachPhien(String keyword, String maCN) {
        return phienDAO.layDanhSachPhien(keyword, maCN);
    }

    public List<DichVuTrongPhienDTO> layDichVuCuaPhien(String maPhien) {
        return phienDAO.layDichVuCuaPhien(maPhien);
    }

    public boolean ketThucPhien(String maPhien) {
        return phienDAO.ketThucPhien(maPhien);
    }

    public boolean xacNhanThanhToanDatTruoc(String maDatCho, String maPhien) {
        boolean dcOk = datChoDAO.xacNhanThanhToan(maDatCho);
        boolean hdOk = hoaDonDAO.capNhatTrangThaiThanhToanTheoPhien(maPhien, "Đã thanh toán");
        return dcOk && hdOk;
    }

    public List<String[]> layDanhSachChiNhanh() {
        return nhanVienDAO.layDanhSachChiNhanh();
    }

    public String layMaCNTheNguoiDung(NguoiDungDTO user) {
        if (user == null) return null;
        return nhanVienDAO.layMaCNTuMaND(user.getMaND());
    }

    public List<KhongGianDTO> layKhongGian(String maCN) {
        if (maCN != null && !maCN.isEmpty()) {
            return khongGianDAO.layTheoChiNhanh(maCN);
        }
        return khongGianDAO.layTatCaKhongGian();
    }

    public String timHoacTaoKhachHang(String hoTen, String sdt) {
        List<HoiVienDTO> ds = khachHangDAO.search(sdt);
        if (ds != null && !ds.isEmpty()) {
            return ds.get(0).getMaKH();
        }

        HoiVienDTO newKH = new HoiVienDTO();
        newKH.setHoTen(hoTen);
        newKH.setSdt(sdt);
        newKH.setTrangThai("Đang hoạt động");
        try {
            khachHangDAO.insert(newKH);
            ds = khachHangDAO.search(sdt);
            if (ds != null && !ds.isEmpty()) {
                return ds.get(0).getMaKH();
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecService] Lỗi tạo khách hàng: " + e.getMessage());
        }
        return null;
    }

    public boolean taoPhienMoi(String maKH, String maKG, int soGioSuDung, double giaThue) {
        PhienLamViecDTO phien = new PhienLamViecDTO();
        
        int count = phienDAO.demSoLuong();
        phien.setMaPhien(String.format("PH%04d", count + 1));
        
        phien.setMaKH(maKH);
        phien.setMaKG(maKG);
        phien.setTrangThaiPhien("Đang hoạt động");
        phien.setGiaThue(giaThue);
        
        long now = System.currentTimeMillis();
        phien.setThoiGianBatDau(new java.sql.Timestamp(now));
        
        long durationMillis = (long) soGioSuDung * 3600 * 1000;
        phien.setThoiGianDuKienKetThuc(new java.sql.Timestamp(now + durationMillis));
        
        return phienDAO.taoPhienLamViecMoi(phien);
    }

    public boolean xoaPhien(String maPhien) {
        return phienDAO.xoaPhien(maPhien);
    }

    public boolean capNhatPhien(String maPhien, String trangThai, String tenKH) {
        return phienDAO.capNhatPhien(maPhien, trangThai, tenKH);
    }
}
