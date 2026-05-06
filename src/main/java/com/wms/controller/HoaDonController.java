package com.wms.controller;

import com.wms.dao.HoaDonDAO;
import com.wms.model.HoaDonDTO;

import com.wms.model.ThongTinHoaDonDTO;
import java.util.List;

public class HoaDonController {

    private final HoaDonDAO hoaDonDAO;

    public HoaDonController() {
        this.hoaDonDAO = new HoaDonDAO();
    }

    public List<HoaDonDTO> layDanhSachHoaDon(String searchText, String statusFilter) {
        return hoaDonDAO.layDanhSachHoaDon(searchText, statusFilter);
    }

    public ThongTinHoaDonDTO layChiTietHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            return null;
        }
        return hoaDonDAO.layThongTinChiTietHoaDon(maHoaDon);
    }

    public boolean xacNhanThanhToan(String maHoaDon, String phuongThucThanhToan, String maNV, String maPGG) {
        if (maHoaDon == null || phuongThucThanhToan == null) {
            return false;
        }
        return hoaDonDAO.xacNhanThanhToan(maHoaDon, phuongThucThanhToan, maNV, maPGG);
    }

    public boolean huyHoaDon(String maHoaDon) {
        if (maHoaDon == null) return false;
        return hoaDonDAO.huyHoaDon(maHoaDon);
    }

    public boolean xoaHoaDon(String maHoaDon) {
        if (maHoaDon == null) return false;
        return hoaDonDAO.xoaHoaDon(maHoaDon);
    }
}





