package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.VanHanh_DichVu.LoaiDichVuDTO;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LoaiDichVuDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<LoaiDichVuDTO> layTatCa() {
        List<LoaiDichVuDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM LOAIDICHVU";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                LoaiDichVuDTO ldv = new LoaiDichVuDTO();
                ldv.setMaLoaiDV(rs.getString("MaLoaiDV"));
                ldv.setTenLoaiDV(rs.getString("TenLoaiDV"));
                ldv.setTrangThaiLDV(rs.getString("TrangThaiLDV"));
                list.add(ldv);
            }
        } catch (Exception e) {
            System.err.println("[LoaiDichVuDAO] Lỗi lấy danh sách: " + e.getMessage());
        }
        return list;
    }

    public boolean them(LoaiDichVuDTO ldv) {
        String sql = "{call SP_ThemLoaiDichVu(?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, ldv.getMaLoaiDV());
            cs.setString(2, ldv.getTenLoaiDV());
            cs.setString(3, ldv.getTrangThaiLDV());
            cs.registerOutParameter(4, java.sql.Types.VARCHAR);
            cs.execute();
            return true;
        } catch (Exception e) {
            System.err.println("[LoaiDichVuDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(LoaiDichVuDTO ldv) {
        String sql = "{call SP_CapNhatLoaiDichVu(?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, ldv.getMaLoaiDV());
            cs.setString(2, ldv.getTenLoaiDV());
            cs.setString(3, ldv.getTrangThaiLDV());
            cs.registerOutParameter(4, java.sql.Types.VARCHAR);
            cs.execute();
            return true;
        } catch (Exception e) {
            System.err.println("[LoaiDichVuDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }
}
