package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.DichVuDTO;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<DichVuDTO> layDanhSachDichVu(String maLoai, String trangThai, String tuKhoa) {
        List<DichVuDTO> list = new ArrayList<>();
        String sql = "{call SP_TraCuuDichVu(?, ?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, maLoai);
            cs.setString(2, trangThai);
            cs.setString(3, tuKhoa);
            cs.registerOutParameter(4, -10); // OracleTypes.CURSOR
            cs.registerOutParameter(5, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                while (rs.next()) {
                    DichVuDTO dv = new DichVuDTO();
                    dv.setMaDV(rs.getString("MaDV"));
                    dv.setTenDV(rs.getString("TenDV"));
                    dv.setDonGia(rs.getDouble("DonGia"));
                    dv.setTrangThaiDV(rs.getString("TrangThaiDV"));
                    dv.setHinhAnh(rs.getString("HinhAnh"));
                    dv.setMaLoaiDV(rs.getString("MaLoaiDV"));
                    list.add(dv);
                }
            }
        } catch (Exception e) {
            System.err.println("[DichVuDAO] Lỗi tra cứu: " + e.getMessage());
        }
        return list;
    }

    public boolean themDichVu(DichVuDTO dv) {
        String sql = "{call sp_ThemDichVu(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, dv.getMaDV());
            cs.setString(2, dv.getTenDV());
            cs.setString(3, dv.getMaLoaiDV());
            cs.setDouble(4, dv.getDonGia());
            cs.setString(5, dv.getTrangThaiDV());
            cs.registerOutParameter(6, java.sql.Types.VARCHAR);
            
            cs.execute();
            return true;
        } catch (Exception e) {
            System.err.println("[DichVuDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatDichVu(DichVuDTO dv) {
        String sql = "{call sp_CapNhatDichVu(?, ?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, dv.getMaDV());
            cs.setString(2, dv.getTenDV());
            cs.setDouble(3, dv.getDonGia());
            cs.setString(4, dv.getTrangThaiDV());
            cs.registerOutParameter(5, java.sql.Types.VARCHAR);
            
            cs.execute();
            return true;
        } catch (Exception e) {
            System.err.println("[DichVuDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }
}
