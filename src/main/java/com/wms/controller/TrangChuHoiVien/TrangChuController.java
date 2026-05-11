package com.wms.controller.TrangChuHoiVien;

import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.dao.TrangChuHoiVien.TrangChuDAO;
import com.wms.model.TrangChuGioiThieu.NguoiDungDTO;
import java.util.ArrayList;
import java.util.List;

public class TrangChuController {

    private final TrangChuDAO trangChuDAO;

    public TrangChuController() {
        this.trangChuDAO = new TrangChuDAO();
    }

    private String getMaND() {
        NguoiDungDTO user = DangNhapController.getCurrentUser();
        return user != null ? user.getMaND() : "";
    }

    public int layDiemTichLuy() {
        return trangChuDAO.layDiemTichLuy(getMaND());
    }

    public String layHangHienTai() {
        return trangChuDAO.layHangHienTai(getMaND());
    }

    public int layTongGioSuDung() {
        return trangChuDAO.layTongGioSuDung(getMaND());
    }

    public int laySoUuDai() {
        return trangChuDAO.laySoUuDai(getMaND());
    }

    public List<Object[]> layLichSuDatCho() {
        String maND = getMaND();
        if (maND == null || maND.isEmpty()) {
            return new ArrayList<>();
        }
        return trangChuDAO.layLichSuDatCho(maND);
    }
}
