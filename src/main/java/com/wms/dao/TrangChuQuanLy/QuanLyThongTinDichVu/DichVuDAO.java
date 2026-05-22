package com.wms.dao.TrangChuQuanLy.QuanLyThongTinDichVu;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO;
import com.wms.util.MaTuDongUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    public List<DichVuDTO> layDanhSachDichVu(String maLoai, String trangThai, String tuKhoa) {
        List<DichVuDTO> list = new ArrayList<>();
        String sql = "{call SP_TraCuuDichVu(?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
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
                    dv.setHinhAnh(rs.getBytes("HinhAnh"));
                    dv.setMaLoaiDV(rs.getString("MaLoaiDV"));
                    dv.setSoLuong(rs.getInt("SoLuong"));
                    dv.setGiaNhap(rs.getDouble("GiaNhap"));
                    list.add(dv);
                }
            }
        } catch (Exception e) {
            System.err.println("[DichVuDAO] Lỗi tra cứu: " + e.getMessage());
        }
        return list;
    }

    public boolean themDichVu(DichVuDTO dv) {
        String sql = "{call sp_ThemDichVu(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, dv.getMaDV());
            cs.setString(2, dv.getTenDV());
            cs.setString(3, dv.getMaLoaiDV());
            cs.setDouble(4, dv.getDonGia());
            cs.setString(5, dv.getTrangThaiDV());
            if (dv.getHinhAnh() != null) cs.setBytes(6, dv.getHinhAnh());
            else cs.setNull(6, java.sql.Types.BLOB);
            
            if (dv.getSoLuong() != null) cs.setInt(7, dv.getSoLuong());
            else cs.setNull(7, java.sql.Types.INTEGER);
            
            if (dv.getGiaNhap() != null) cs.setDouble(8, dv.getGiaNhap());
            else cs.setNull(8, java.sql.Types.DOUBLE);
            
            cs.registerOutParameter(9, java.sql.Types.VARCHAR);
            
            cs.execute();
            return true;
        } catch (Exception e) {
            System.err.println("[DichVuDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatDichVu(DichVuDTO dv) {
        String sql = "{call sp_CapNhatDichVu(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, dv.getMaDV());
            cs.setString(2, dv.getTenDV());
            cs.setDouble(3, dv.getDonGia());
            cs.setString(4, dv.getTrangThaiDV());
            if (dv.getHinhAnh() != null) cs.setBytes(5, dv.getHinhAnh());
            else cs.setNull(5, java.sql.Types.BLOB);
            
            cs.setString(6, dv.getMaLoaiDV());
            
            if (dv.getSoLuong() != null) cs.setInt(7, dv.getSoLuong());
            else cs.setNull(7, java.sql.Types.INTEGER);
            
            if (dv.getGiaNhap() != null) cs.setDouble(8, dv.getGiaNhap());
            else cs.setNull(8, java.sql.Types.DOUBLE);
            
            cs.registerOutParameter(9, java.sql.Types.VARCHAR);
            
            cs.execute();
            return true;
        } catch (Exception e) {
            System.err.println("[DichVuDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    public String layMaxMaDV() {
        String sql = "SELECT MAX(TO_NUMBER(SUBSTR(MaDV, 3))) FROM DICHVU WHERE MaDV LIKE 'DV%'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             java.sql.Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("DV%03d", maxNum);
            }
        } catch (Exception e) {
            System.err.println("[DichVuDAO] Lỗi lấy mã MAX: " + e.getMessage());
        }
        return null;
    }

    public String taoMaMoi() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return MaTuDongUtil.sinhMaTiepTheo(conn, MaTuDongUtil.MaDoiTuong.DICH_VU);
        } catch (Exception e) {
            System.err.println("[DichVuDAO] Lỗi tạo mã mới: " + e.getMessage());
            return "DV000001";
        }
    }
}
