package com.wms.controller.TrangChuQuanLy.QuanLyVaiTro;

import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.ChucNangDTO;
import com.wms.service.TrangChuQuanLy.QuanLyVaiTro.VaiTroService;

import java.util.List;

public class VaiTroController {
    private final VaiTroService service = new VaiTroService();

    public List<VaiTroDTO> layTatCaVaiTro() {
        return service.layTatCaVaiTro();
    }

    public String sinhMaVT() {
        return service.sinhMaVT();
    }

    public List<ChucNangDTO> layTatCaChucNang() {
        return service.layTatCaChucNang();
    }

    public List<String[]> layChucNangCuaVaiTro(String maVaiTro) {
        return service.layChucNangCuaVaiTro(maVaiTro);
    }

    public boolean themVaiTro(VaiTroDTO vt, List<String> danhSachMaChucNang) throws java.sql.SQLException {
        return service.themVaiTro(vt, danhSachMaChucNang);
    }

    public boolean capNhatVaiTro(VaiTroDTO vt, List<String> danhSachMaChucNang) throws java.sql.SQLException {
        return service.capNhatVaiTro(vt, danhSachMaChucNang);
    }

    public boolean xoaVaiTro(String maVaiTro) throws java.sql.SQLException {
        return service.xoaVaiTro(maVaiTro);
    }
}
