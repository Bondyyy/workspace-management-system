/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wms.controller;

/**
 *
 * @author kyduy
 */

import com.wms.service.KhongGianService;
import java.time.LocalTime;
import java.util.List;


public class KhongGianController {
    
    private final KhongGianService khongGianService;

    public KhongGianController() {
        this.khongGianService = new KhongGianService();
    }

    /**
     * Dùng để load ComboBox Chi Nhánh trên View
     * @return 
     */
    public List<String> taiDanhSachChiNhanh() {
        return khongGianService.layDanhSachChiNhanh();
    }

    /**
     * Dùng để load ComboBox Loại Không Gian trên View
     * @return 
     */
    public List<String> taiDanhSachLoaiKhongGian() {
        return khongGianService.layDanhSachLoaiKhongGian();
    }

    /**
     * Gọi khi người dùng bấm nút "Kiểm tra tình trạng không gian đã chọn"
     * @param loaiKhongGian
     */
    public boolean thucHienKiemTraChoTrong(String loaiKhongGian, String ngayDat, String gioToi) {
        return khongGianService.kiemTraTinhTrang(loaiKhongGian, ngayDat, gioToi);
    }

    /**
     * Gọi khi người dùng đổi lựa chọn Chi nhánh để tính toán lại giờ ở ComboBox Thời Gian
     * @param tenChiNhanh
     */
    public LocalTime layThoiGianDongCua(String tenChiNhanh) {
        return khongGianService.layGioDongCua(tenChiNhanh);
    }
}
