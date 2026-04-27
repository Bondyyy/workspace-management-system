package com.wms.controller;

import com.wms.service.NguoiDungService;
import com.wms.service.NguoiDungService.ketQuaDangKy;

public class DangKyController {

    private final NguoiDungService nguoiDungService = new NguoiDungService();

    public ketQuaDangKy dangKy(String tenTaiKhoan, String hoTen, String email, String matKhau) {
        return nguoiDungService.register(tenTaiKhoan, hoTen, email, matKhau);
    }
}