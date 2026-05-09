package com.wms.controller.TrangChuQuanLy.QuanLyPhien;

import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import com.wms.model.NguoiDungDTO;
import com.wms.service.TrangChuQuanLy.QuanLyPhien.PhienLamViecService;
import com.wms.view.TrangChuQuanLy.QuanLyPhien.MoPhienMoiForm;

import java.util.List;

public class MoPhienMoiController {

    private final MoPhienMoiForm view;
    private final PhienLamViecService service;

    public MoPhienMoiController(MoPhienMoiForm view) {
        this.view = view;
        this.service = new PhienLamViecService();
    }

    public List<KhongGianDTO> layKhongGian(String maCN) {
        return service.layKhongGian(maCN);
    }

    /** Xác định chi nhánh dựa vào người dùng đang đăng nhập (fallback). */
    public String layMaCNNguoiDung(NguoiDungDTO user) {
        return service.layMaCNTheNguoiDung(user);
    }

    /**
     * Tìm hoặc tạo khách hàng, rồi tạo phiên mới.
     * Trả về true nếu toàn bộ quy trình thành công.
     */
    public boolean moPhienMoi(String hoTenKH, String sdt, String maKG, int soGioSuDung, double giaThue) {
        String maKH = service.timHoacTaoKhachHang(hoTenKH, sdt);
        if (maKH == null)
            return false;
        return service.taoPhienMoi(maKH, maKG, soGioSuDung, giaThue);
    }
}
