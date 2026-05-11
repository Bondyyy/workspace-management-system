    package com.wms.controller.TrangChuGioiThieu;

    import com.wms.service.TrangChuGioiThieu.NguoiDungService;
    import com.wms.service.TrangChuGioiThieu.NguoiDungService.ketQuaDangKy;
    import com.wms.service.TrangChuGioiThieu.NguoiDungService.OtpResponse;

    public class DangKyController {

        private final NguoiDungService nguoiDungService = new NguoiDungService();

        public ketQuaDangKy dangKy(String tenTaiKhoan, String hoTen, String email, String matKhau) {
            return nguoiDungService.register(tenTaiKhoan, hoTen, email, matKhau);
        }
        
        public OtpResponse yeuCauOTP(String tenTaiKhoan, String email) {
            return nguoiDungService.yeuCauOTP(tenTaiKhoan, email);
        }
    }