package com.wms.service.TrangChuQuanLy.QuanLyVaiTro;

import com.wms.dao.TrangChuQuanLy.QuanLyVaiTro.VaiTroDAO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.ChucNangDTO;

import java.util.List;

public class VaiTroService {
    private final VaiTroDAO vaiTroDAO = new VaiTroDAO();

    public List<VaiTroDTO> layTatCaVaiTro() {
        return vaiTroDAO.layTatCaVaiTro();
    }

    public List<ChucNangDTO> layTatCaChucNang() {
        return vaiTroDAO.layTatCaChucNang();
    }

    public List<String[]> layChucNangCuaVaiTro(String maVaiTro) {
        return vaiTroDAO.layChucNangCuaVaiTro(maVaiTro);
    }

    public boolean themVaiTro(VaiTroDTO vt, List<String> danhSachMaChucNang) {
        return vaiTroDAO.themVaiTro(vt, danhSachMaChucNang);
    }

    public boolean capNhatVaiTro(VaiTroDTO vt, List<String> danhSachMaChucNang) {
        return vaiTroDAO.capNhatVaiTro(vt, danhSachMaChucNang);
    }

    public boolean xoaVaiTro(String maVaiTro) {
        return vaiTroDAO.xoaVaiTro(maVaiTro);
    }
}
