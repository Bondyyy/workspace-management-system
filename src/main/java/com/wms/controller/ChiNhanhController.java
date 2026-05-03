package com.wms.controller;

import com.wms.dao.ChiNhanhDAO;
import com.wms.model.ChiNhanhDTO;
import java.util.List;

public class ChiNhanhController {

    private final ChiNhanhDAO chiNhanhDAO;

    public ChiNhanhController() {
        this.chiNhanhDAO = new ChiNhanhDAO();
    }

    public List<ChiNhanhDTO> layDanhSach() {
        return chiNhanhDAO.layDanhSachChiNhanh();
    }

    public List<ChiNhanhDTO> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return chiNhanhDAO.layDanhSachChiNhanh();
        }
        return chiNhanhDAO.timKiemChiNhanh(tuKhoa.trim());
    }

    public boolean themMoi(ChiNhanhDTO cn) throws Exception {
        validate(cn);
        return chiNhanhDAO.themChiNhanh(cn);
    }

    public boolean capNhat(ChiNhanhDTO cn) throws Exception {
        if (cn.getMaCN() == null || cn.getMaCN().trim().isEmpty()) {
            throw new Exception("Mã chi nhánh không được để trống khi cập nhật!");
        }
        validate(cn);
        return chiNhanhDAO.capNhatChiNhanh(cn);
    }

    public boolean voHieuHoa(String maCN) throws Exception {
        if (maCN == null || maCN.trim().isEmpty()) {
            throw new Exception("Mã chi nhánh không hợp lệ!");
        }
        return chiNhanhDAO.voHieuHoaChiNhanh(maCN);
    }

    // Hàm validate dữ liệu cơ bản
    private void validate(ChiNhanhDTO cn) throws Exception {
        if (cn.getTenCN() == null || cn.getTenCN().trim().isEmpty()) {
            throw new Exception("Tên chi nhánh không được để trống!");
        }
        if (cn.getDiaChi() == null || cn.getDiaChi().trim().isEmpty()) {
            throw new Exception("Địa chỉ không được để trống!");
        }
    }
}
