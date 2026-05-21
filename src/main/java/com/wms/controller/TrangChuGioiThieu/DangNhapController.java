package com.wms.controller.TrangChuGioiThieu;

import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.service.TrangChuGioiThieu.NguoiDungService;

public class DangNhapController {

    private final NguoiDungService nguoiDungService = new NguoiDungService();

    private static NguoiDungDTO currentUser;

    public NguoiDungService.ketQuaDangNhap dangNhap(String tenTaiKhoan, String matKhau) {
        NguoiDungService.AuthResponse response = nguoiDungService.authenticate(tenTaiKhoan, matKhau);
        
        if (response.getResult() == NguoiDungService.ketQuaDangNhap.THANH_CONG) {
            currentUser = response.getUser();
        }
        
        return response.getResult();
    }

    public static NguoiDungDTO getCurrentUser() {
        return currentUser;
    }

    public static void dangXuat() {
        currentUser = null;
    }
}