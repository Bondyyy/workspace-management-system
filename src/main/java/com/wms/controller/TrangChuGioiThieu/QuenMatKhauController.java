package com.wms.controller.TrangChuGioiThieu;

import com.wms.service.TrangChuGioiThieu.NguoiDungService;
import com.wms.service.TrangChuGioiThieu.NguoiDungService.ketQuaQuenMatKhau;
import com.wms.service.TrangChuGioiThieu.NguoiDungService.OtpQuenPassResponse;

public class QuenMatKhauController {

    private final NguoiDungService nguoiDungService = new NguoiDungService();

    public OtpQuenPassResponse yeuCauOTP(String email) {
        return nguoiDungService.yeuCauOtpQuenMatKhau(email);
    }

    public ketQuaQuenMatKhau datLaiMatKhau(String email, String matKhauMoi) {
        return nguoiDungService.datLaiMatKhau(email, matKhauMoi);
    }
}
