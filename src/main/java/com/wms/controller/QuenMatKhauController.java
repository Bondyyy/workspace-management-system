package com.wms.controller;

import com.wms.service.NguoiDungService;
import com.wms.service.NguoiDungService.ketQuaQuenMatKhau;
import com.wms.service.NguoiDungService.OtpQuenPassResponse;

public class QuenMatKhauController {

    private final NguoiDungService nguoiDungService = new NguoiDungService();

    public OtpQuenPassResponse yeuCauOTP(String email) {
        return nguoiDungService.yeuCauOtpQuenMatKhau(email);
    }

    public ketQuaQuenMatKhau datLaiMatKhau(String email, String matKhauMoi) {
        return nguoiDungService.datLaiMatKhau(email, matKhauMoi);
    }
}
