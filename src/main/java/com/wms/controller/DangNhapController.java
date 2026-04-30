package com.wms.controller;

import com.wms.model.NhanSu_KhachHang.NguoiDungDTO;
import com.wms.service.NguoiDungService;

public class DangNhapController {

    private final NguoiDungService nguoiDungService = new NguoiDungService();

    private static NguoiDungDTO currentUser;

    public NguoiDungService.ketQuaDangNhap login(String tenTaiKhoan, String matKhau) {
        NguoiDungService.AuthResponse response = nguoiDungService.authenticate(tenTaiKhoan, matKhau);
        
        if (response.getResult() == NguoiDungService.ketQuaDangNhap.THANH_CONG) {
            currentUser = response.getUser();
        }
        
        return response.getResult();
    }

    public static NguoiDungDTO getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}