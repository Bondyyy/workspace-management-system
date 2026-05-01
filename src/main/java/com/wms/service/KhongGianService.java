/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wms.service;

import com.wms.dao.KhongGianDAO;
import java.time.LocalTime;
import java.util.List;


/**
 *
 * @author kyduy
 */
public class KhongGianService {
    
    private KhongGianDAO khongGianDAO;

    public KhongGianService() {
        this.khongGianDAO = new KhongGianDAO();
    }

    // Lấy danh sách chi nhánh đang hoạt động
    public List<String> layDanhSachChiNhanh() {
        // Có thể thêm logic kiểm tra, sắp xếp danh sách ở đây nếu cần
        return khongGianDAO.layDanhSachChiNhanhHoatDong();
    }

    // Lấy danh sách loại không gian
    public List<String> layDanhSachLoaiKhongGian() {
        return khongGianDAO.layDanhSachLoaiKhongGian();
    }

    // Xử lý logic kiểm tra tình trạng trống
    public boolean kiemTraTinhTrang(String tenKhongGian, String ngayDat, String gioToi) {
        // Validate cơ bản trước khi gọi xuống DAO để tiết kiệm tài nguyên DB
        if (tenKhongGian == null || tenKhongGian.trim().isEmpty() || tenKhongGian.contains("--")) {
            return false;
        }
        if (ngayDat == null || ngayDat.trim().isEmpty()) {
            return false;
        }
        
        return khongGianDAO.kiemTraTinhTrangKhongGian(tenKhongGian, ngayDat, gioToi);
    }

    // Xử lý logic lấy giờ đóng cửa
    public LocalTime layGioDongCua(String tenChiNhanh) {
        // Nếu tên chi nhánh không hợp lệ, trả về mặc định 22:00
        if (tenChiNhanh == null || tenChiNhanh.trim().isEmpty() || tenChiNhanh.contains("--")) {
            return LocalTime.of(22, 0); 
        }
        
        return khongGianDAO.layGioDongCuaCuaChiNhanh(tenChiNhanh);
    }
}
