package com.wms.dao.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.dao.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDAO;
import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.NhanVienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Object[]> layDanhSachNhanVien(String maCN) {
        return timKiemNhanVien(null, maCN);
    }

    public List<Object[]> timKiemNhanVien(String tuKhoa, String maCN) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT nv.MaNV, nd.HoTen, nd.SDT, nv.LoaiNV, nv.CaLamViec, " +
                        "       cn.TenCN, vt.TenVaiTro, nv.TrangThaiLamViec, " +
                        "       nd.GioiTinh, nd.Email, nv.LuongCoBan, " +
                        "       nv.MaCN, cvt.MaVaiTro, nv.MaND, nd.AnhDaiDien, nd.NgaySinh, nd.TenTaiKhoan " +
                        "FROM NHANVIEN nv " +
                        "JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND " +
                        "LEFT JOIN CHINHANH cn ON nv.MaCN = cn.MaCN " +
                        "LEFT JOIN CHITIETVAITRO cvt ON nd.MaND = cvt.MaND " +
                        "LEFT JOIN VAITRO vt ON cvt.MaVaiTro = vt.MaVaiTro");

        sql.append(" WHERE nv.MaNV != 'NV_ADMIN'");

        sql.append(" AND EXISTS (SELECT 1 FROM CHITIETVAITRO ctv_ex JOIN VAITRO vt_ex ON vt_ex.MaVaiTro = ctv_ex.MaVaiTro WHERE ctv_ex.MaND = nd.MaND AND LOWER(vt_ex.TenVaiTro) NOT LIKE '%hội viên%' AND LOWER(vt_ex.TenVaiTro) NOT LIKE '%quản trị viên hệ thống%')");
        sql.append(" AND NOT EXISTS (SELECT 1 FROM CHITIETVAITRO ctv_nx JOIN VAITRO vt_nx ON vt_nx.MaVaiTro = ctv_nx.MaVaiTro WHERE ctv_nx.MaND = nd.MaND AND LOWER(vt_nx.TenVaiTro) LIKE '%quản trị viên hệ thống%')");
        sql.append(" AND nv.TrangThaiLamViec != 'Ngừng làm việc'");
        
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append(" AND (LOWER(nd.HoTen) LIKE ? OR nd.SDT LIKE ?)");
        }
        if (maCN != null && !maCN.trim().isEmpty()) {
            sql.append(" AND nv.MaCN = ?");
        }
        sql.append(" ORDER BY nv.MaNV");

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
                String pattern = "%" + tuKhoa.trim().toLowerCase() + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, "%" + tuKhoa.trim() + "%");
            }
            if (maCN != null && !maCN.trim().isEmpty()) {
                ps.setString(paramIndex++, maCN);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[] {
                            rs.getString("MaNV"), rs.getString("HoTen"), rs.getString("SDT"),
                            rs.getString("LoaiNV"), rs.getString("CaLamViec"), rs.getString("TenCN"),
                            rs.getString("TenVaiTro"), rs.getString("TrangThaiLamViec"),
                            rs.getString("GioiTinh"), rs.getString("Email"), rs.getDouble("LuongCoBan"),
                            rs.getString("MaCN"), rs.getString("MaVaiTro"), rs.getString("MaND"),
                            rs.getBytes("AnhDaiDien"), rs.getDate("NgaySinh"), rs.getString("TenTaiKhoan")
                     });
                }
            }
        } catch (Exception e) {
            System.err.println("[NhanVienDAO] Lỗi tìm kiếm: " + e.getMessage());
        }
        return list;
    }

    public boolean themNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) {
        NguoiDungDAO ndDAO = new NguoiDungDAO();
        try {
            if (ndDAO.kiemTraEmailTonTai(nd.getEmail()))
                throw new SQLException("Email đã tồn tại trên hệ thống!");
            if (ndDAO.kiemTraSdtTonTai(nd.getSdt()))
                throw new SQLException("Số điện thoại đã tồn tại trên hệ thống!");
            if (nd.getTenTaiKhoan() != null && !nd.getTenTaiKhoan().trim().isEmpty()) {
                if (ndDAO.kiemTraTaiKhoanTonTai(nd.getTenTaiKhoan())) {
                    throw new SQLException("Tên tài khoản đã tồn tại trên hệ thống!");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        boolean autoCommit = true;
        try (Connection conn = getConn()) {
            autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION DISABLE PARALLEL DML");
                } catch (Exception e) {
                    System.err.println("Cannot disable parallel DML: " + e.getMessage());
                }

                String hashedPw = (matKhau != null && !matKhau.isEmpty()) ? PasswordUtil.hash(matKhau)
                        : PasswordUtil.hash("123456");

                String username = (nd.getTenTaiKhoan() != null && !nd.getTenTaiKhoan().trim().isEmpty()) 
                        ? nd.getTenTaiKhoan().trim() 
                        : nd.getSdt();

                String sqlND = """
                        BEGIN
                            INSERT INTO NGUOIDUNG (
                                HoTen, TenTaiKhoan, MatKhauMaHoa, SDT, Email,
                                GioiTinh, AnhDaiDien, NgaySinh, TrangThaiND,
                                ThoiGianTao, CapNhatLanCuoi
                            ) VALUES (
                                ?, ?, ?, ?, ?, ?, ?, ?, 'Đang hoạt động',
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                            )
                            RETURNING MaND INTO ?;
                        END;
                        """;
                String maND;
                try (CallableStatement cs = conn.prepareCall(sqlND)) {
                    cs.setString(1, hoTen);
                    cs.setString(2, username);
                    cs.setString(3, hashedPw);
                    cs.setString(4, nd.getSdt());
                    cs.setString(5, nd.getEmail());
                    cs.setString(6, nd.getGioiTinh());
                    cs.setBytes(7, nd.getAnhDaiDien());
                    cs.setDate(8, nd.getNgaySinh());
                    cs.registerOutParameter(9, Types.VARCHAR);
                    cs.execute();
                    maND = cs.getString(9);
                    nd.setMaND(maND);
                }

                String sqlKH = """
                        BEGIN
                            INSERT INTO KHACHHANG (
                                MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND
                            ) VALUES (
                                'HTV00', 0, CURRENT_TIMESTAMP, ?
                            )
                            RETURNING MaKH INTO ?;
                        END;
                        """;
                try (CallableStatement cs = conn.prepareCall(sqlKH)) {
                    cs.setString(1, maND);
                    cs.registerOutParameter(2, Types.VARCHAR);
                    cs.execute();
                }

                String sqlNV = """
                        BEGIN
                            INSERT INTO NHANVIEN (
                                LoaiNV, NgayVaoLam, TrangThaiLamViec, CaLamViec,
                                LuongCoBan, MaCN, MaND
                            ) VALUES (
                                ?, CURRENT_DATE, ?, ?, ?, ?, ?
                            )
                            RETURNING MaNV INTO ?;
                        END;
                        """;
                try (CallableStatement cs = conn.prepareCall(sqlNV)) {
                    cs.setString(1, nv.getLoaiNV());
                    cs.setString(2, nv.getTrangThaiLamViec() != null ? nv.getTrangThaiLamViec() : "Đang làm việc");
                    cs.setString(3, nv.getCaLamViec());
                    if (nv.getLuongCoBan() != null) {
                        cs.setDouble(4, nv.getLuongCoBan());
                    } else {
                        cs.setNull(4, Types.NUMERIC);
                    }
                    cs.setString(5, nv.getMaCN());
                    cs.setString(6, maND);
                    cs.registerOutParameter(7, Types.VARCHAR);
                    cs.execute();
                    nv.setMaNV(cs.getString(7));
                }

                if (maVaiTro != null && !maVaiTro.isEmpty()) {
                    String sqlVT = "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro, MoTa) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlVT)) {
                        ps.setString(1, maND);
                        ps.setString(2, maVaiTro);
                        ps.setString(3, "Phân quyền vai trò mặc định khi thêm nhân viên");
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
                System.err.println("[NhanVienDAO] Lỗi thêm nhân viên: " + e.getMessage());
                return false;
            } finally {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[NhanVienDAO] Lỗi kết nối thêm nhân viên: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) {
        boolean autoCommit = true;
        try (Connection conn = getConn()) {
            // Kiểm tra trùng email/sdt (loại trừ chính nhân viên này)
            String sqlCheck = "SELECT MaND, HoTen FROM NGUOIDUNG WHERE (LOWER(TRIM(Email)) = LOWER(TRIM(?)) OR LOWER(TRIM(SDT)) = LOWER(TRIM(?))) AND LOWER(TRIM(MaND)) != LOWER(TRIM(?))";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setString(1, nd.getEmail());
                ps.setString(2, nd.getSdt());
                ps.setString(3, nv.getMaND());
                
                System.out.println("[DEBUG] Cập nhật nhân viên: " + nv.getMaNV() + " (MaND=" + (nv.getMaND() != null ? nv.getMaND().trim() : "null") + ")");
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String tenTrung = rs.getString("HoTen");
                        String maTrung = rs.getString("MaND");
                        throw new SQLException("Email/SĐT đã tồn tại! (Trùng với: " + tenTrung + " [" + maTrung + "], Bạn gửi: [" + nv.getMaND() + "])");
                    }
                }
            }

            // Kiểm tra trùng TenTaiKhoan (loại trừ chính nhân viên này)
            if (nd.getTenTaiKhoan() != null && !nd.getTenTaiKhoan().trim().isEmpty()) {
                String sqlCheckUser = "SELECT MaND, HoTen FROM NGUOIDUNG WHERE LOWER(TRIM(TenTaiKhoan)) = LOWER(TRIM(?)) AND LOWER(TRIM(MaND)) != LOWER(TRIM(?))";
                try (PreparedStatement ps = conn.prepareStatement(sqlCheckUser)) {
                    ps.setString(1, nd.getTenTaiKhoan().trim());
                    ps.setString(2, nv.getMaND());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException("Tên tài khoản '" + nd.getTenTaiKhoan() + "' đã được sử dụng bởi người dùng khác!");
                        }
                    }
                }
            }

            autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION DISABLE PARALLEL DML");
                } catch (Exception e) {
                    System.err.println("Cannot disable parallel DML: " + e.getMessage());
                }

                boolean hasNewPw = (matKhau != null && !matKhau.isEmpty());
                String sqlND = "UPDATE NGUOIDUNG SET HoTen = ?, TenTaiKhoan = ?, SDT = ?, Email = ?, GioiTinh = ?, AnhDaiDien = ?, NgaySinh = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP"
                        + (hasNewPw ? ", MatKhauMaHoa = ?" : "") + " WHERE MaND = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                    ps.setString(1, hoTen);
                    ps.setString(2, nd.getTenTaiKhoan());
                    ps.setString(3, nd.getSdt());
                    ps.setString(4, nd.getEmail());
                    ps.setString(5, nd.getGioiTinh());
                    ps.setBytes(6, nd.getAnhDaiDien());
                    ps.setDate(7, nd.getNgaySinh());
                    if (hasNewPw) {
                        ps.setString(8, PasswordUtil.hash(matKhau));
                        ps.setString(9, nv.getMaND());
                    } else {
                        ps.setString(8, nv.getMaND());
                    }
                    ps.executeUpdate();
                }

                String sqlKH = "UPDATE KHACHHANG SET CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaND = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlKH)) {
                    ps.setString(1, nv.getMaND());
                    ps.executeUpdate();
                }

                String sqlNV = "UPDATE NHANVIEN SET LoaiNV = ?, CaLamViec = ?, LuongCoBan = ?, MaCN = ?, TrangThaiLamViec = ? WHERE MaNV = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {
                    ps.setString(1, nv.getLoaiNV());
                    ps.setString(2, nv.getCaLamViec());
                    if (nv.getLuongCoBan() != null) {
                        ps.setDouble(3, nv.getLuongCoBan());
                    } else {
                        ps.setNull(3, Types.NUMERIC);
                    }
                    ps.setString(4, nv.getMaCN());
                    ps.setString(5, nv.getTrangThaiLamViec());
                    ps.setString(6, nv.getMaNV());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CHITIETVAITRO WHERE MaND = ?")) {
                    ps.setString(1, nv.getMaND());
                    ps.executeUpdate();
                }

                if (maVaiTro != null && !maVaiTro.isEmpty()) {
                    try (PreparedStatement ps = conn
                            .prepareStatement("INSERT INTO CHITIETVAITRO (MaND, MaVaiTro, MoTa) VALUES (?, ?, ?)")) {
                        ps.setString(1, nv.getMaND());
                        ps.setString(2, maVaiTro);
                        ps.setString(3, "Cập nhật vai trò nhân viên");
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
                throw new RuntimeException(e.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[NhanVienDAO] Lỗi cập nhật nhân viên: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean xoaNhanVien(String maNV, String maND) {
        boolean autoCommit = true;
        try (Connection conn = getConn()) {
            autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION DISABLE PARALLEL DML");
                } catch (Exception e) {
                    System.err.println("Cannot disable parallel DML: " + e.getMessage());
                }

                String[] queries = {
                        "DELETE FROM CHITIETVAITRO WHERE MaND = ?",
                        "DELETE FROM NHANVIEN WHERE MaNV = ?",
                        "DELETE FROM KHACHHANG WHERE MaND = ?",
                        "DELETE FROM NGUOIDUNG WHERE MaND = ?"
                };
                for (String sql : queries) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, sql.contains("MaNV") ? maNV : maND);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
                System.err.println("[NhanVienDAO] Lỗi xóa nhân viên: " + e.getMessage());
                return false;
            } finally {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[NhanVienDAO] Lỗi kết nối xóa nhân viên: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> layDanhSachChiNhanh() {
        List<String[]> list = new ArrayList<>();
        String sql = """
                SELECT MaCN, TenCN
                FROM CHINHANH
                WHERE TrangThai = 'Đang hoạt động'
                ORDER BY
                    CASE
                        WHEN REGEXP_LIKE(MaCN, '^CN[0-9]+$')
                        THEN TO_NUMBER(REGEXP_SUBSTR(MaCN, '[0-9]+$'))
                    END NULLS LAST,
                    MaCN
                """;
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new String[] { rs.getString("MaCN"), rs.getString("TenCN") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String taoMaNVMoi(Connection conn) throws SQLException {
        return "";
    }

    public String layMaNVTuMaND(String maND) {
        String sql = "SELECT MaNV FROM NHANVIEN WHERE MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString("MaNV");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String layMaCNTuMaND(String maND) {
        String sql = "SELECT MaCN FROM NHANVIEN WHERE MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString("MaCN");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String layTenCNTuMaND(String maND) {
        String sql = """
                SELECT cn.TenCN
                FROM NHANVIEN nv
                LEFT JOIN CHINHANH cn ON cn.MaCN = nv.MaCN
                WHERE nv.MaND = ?
                """;
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TenCN");
                }
            }
        } catch (Exception e) {
            System.err.println("[NhanVienDAO] Lỗi lấy tên chi nhánh nhân viên: " + e.getMessage());
        }
        return null;
    }

    public List<String[]> layDanhSachQuanLy() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT nv.MaNV, nd.HoTen " +
                "FROM NHANVIEN nv " +
                "JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND " +
                "WHERE nv.TrangThaiLamViec = 'Đang làm việc'";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString("MaNV"), rs.getString("HoTen") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
