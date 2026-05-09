package com.wms.controller.TrangChuQuanLy.QuanLyChiNhanh;

import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.service.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhService;

import java.util.List;

public class ChiNhanhController {

    private final ChiNhanhService service = new ChiNhanhService();

    public List<ChiNhanhDTO> layDanhSach() {
        return service.layDanhSach();
    }

    public List<ChiNhanhDTO> timKiem(String tuKhoa) {
        return service.timKiem(tuKhoa);
    }

    /** Trả về null nếu thành công, chuỗi lỗi nếu thất bại. */
    public String themMoi(ChiNhanhDTO cn) {
        return service.themMoi(cn);
    }

    public String capNhat(ChiNhanhDTO cn) {
        return service.capNhat(cn);
    }

    public String voHieuHoa(String maCN) {
        return service.voHieuHoa(maCN);
    }

    public List<String[]> layDanhSachQuanLy() {
        return service.layDanhSachQuanLy();
    }
}
