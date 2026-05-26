package com.wms.dao.TrangChuQuanLy.QuanLyPhien;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.KetQuaNhanChoDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.MaTuDongUtil;
import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhienLamViecDAO {

    public String generateNextMaPhien() throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return MaTuDongUtil.sinhMaTiepTheo(conn, MaTuDongUtil.MaDoiTuong.PHIEN_LAM_VIEC);
        }
    }

    public String[] layGioHoatDongTheoKhongGian(String maKG) {
        String sql = "SELECT cn.ThoiGianMoCua, cn.ThoiGianDongCua, cn.TenCN " +
                     "FROM KHONGGIAN kg " +
                     "JOIN CHINHANH cn ON cn.MaCN = kg.MaCN " +
                     "WHERE kg.MaKG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKG);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[] {
                        rs.getString("ThoiGianMoCua"),
                        rs.getString("ThoiGianDongCua"),
                        rs.getString("TenCN")
                    };
                }
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Loi lay gio hoat dong: " + e.getMessage());
        }
        return null;
    }

    public boolean taoPhienLamViecMoi(PhienLamViecDTO phien) {
        if (phien.getMaPhien() == null || phien.getMaPhien().isEmpty()) {
            try {
                phien.setMaPhien(generateNextMaPhien());
            } catch (SQLException e) {
                System.err.println("[PhienLamViecDAO] Lỗi sinh mã phiên: " + e.getMessage());
                return false;
            }
        }

        // SP nhan 7 tham so: MaKG, MaKH, ThoiGianBatDau, ThoiGianDuKienKetThuc, MaPhien, MaDatCho, OUT message
        String sql = "{call sp_MoPhienLamViecTrucTiep(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, phien.getMaKG());
            cstmt.setString(2, phien.getMaKH());
            // param 3: ThoiGianBatDau - dung thoi gian hien tai neu DTO chua co
            Timestamp batDau = phien.getThoiGianBatDau() != null
                    ? phien.getThoiGianBatDau()
                    : new Timestamp(System.currentTimeMillis());
            cstmt.setTimestamp(3, batDau);
            cstmt.setTimestamp(4, phien.getThoiGianDuKienKetThuc());
            cstmt.setString(5, phien.getMaPhien());
            cstmt.setString(6, phien.getMaDatCho());
            cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);

            try {
                cstmt.execute();
            } catch (SQLException e) {
                String outMessage = null;
                try {
                    outMessage = cstmt.getString(7);
                } catch (SQLException ignored) {
                    // Oracle may fail before the OUT parameter is available.
                }
                logSqlException(e);
                if (outMessage != null && !outMessage.isBlank()) {
                    throw new IllegalArgumentException(boDau(outMessage), e);
                }
                throw new IllegalArgumentException(chuyenLoiSqlMoPhien(e), e);
            }

            String message = cstmt.getString(7);
            if (laThongBaoThanhCong(message)) {
                return true;
            } else {
                System.err.println("[PhienLamViecDAO] Loi tu SP: " + boDau(message));
                throw new IllegalArgumentException(boDau(message));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            logSqlException(e);
            throw new IllegalArgumentException(chuyenLoiSqlMoPhien(e), e);
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Loi khi goi SP tao phien: " + boDau(e.getMessage()));
            throw new IllegalArgumentException("Khong the mo phien lam viec: " + boDau(e.getMessage()), e);
        }
    }

    private void logSqlException(SQLException e) {
        int index = 1;
        SQLException current = e;
        while (current != null) {
            System.err.println("[PhienLamViecDAO][SQLException #" + index + "] errorCode="
                    + current.getErrorCode()
                    + ", SQLState=" + current.getSQLState()
                    + ", message=" + current.getMessage());
            current = current.getNextException();
            index++;
        }
        e.printStackTrace(System.err);
    }

    private String chuyenLoiSqlMoPhien(SQLException e) {
        StringBuilder details = new StringBuilder();
        SQLException current = e;
        while (current != null) {
            if (current.getMessage() != null) {
                if (details.length() > 0) {
                    details.append(" | ");
                }
                details.append(current.getMessage());
            }
            current = current.getNextException();
        }
        String rawMessage = details.length() == 0 ? e.getMessage() : details.toString();
        String normalized = rawMessage == null ? "" : rawMessage.toUpperCase(Locale.ROOT);
        if (normalized.contains("ORA-06550")
                || normalized.contains("PLS-00905")
                || normalized.contains("ORA-04098")) {
            return "Procedure/trigger trong database đang bị lỗi hoặc chưa được compile lại. "
                    + "Vui lòng chạy lại script SQL và xem SHOW ERRORS."
                    + "\nChi tiet Oracle: " + boDau(rawMessage);
        }
        return "Loi database khi mo phien lam viec: " + boDau(rawMessage);
    }

    public KetQuaNhanChoDTO moPhienTuQrDatCho(String maDatCho, String noiDungQr) {
        if (maDatCho == null || maDatCho.isBlank() || noiDungQr == null || noiDungQr.isBlank()) {
            return new KetQuaNhanChoDTO(false, "Vui lòng nhập mã QR nhận chỗ hợp lệ.");
        }

        String bookingSql = """
                SELECT dc.MaDatCho, dc.MaQR, dc.TrangThaiDatTruoc, dc.KhoangThoiGianSuDung,
                       NVL(dc.ThanhTien, 0) AS ThanhTien,
                       NVL(dc.TongTienGoc, 0) AS TongTienGoc,
                       NVL(dc.ThanhTienSauGiam, NVL(dc.ThanhTien, 0)) AS ThanhTienSauGiam,
                       dc.MaPGG,
                       NVL(dc.TienGiamVoucher, 0) AS TienGiamVoucher,
                       NVL(dc.PhanTramGiamHangTV, 0) AS PhanTramGiamHangTV,
                       NVL(dc.TienGiamHangTV, 0) AS TienGiamHangTV,
                       dc.MaKG, dc.MaKH, dc.GhiChu,
                       dc.ThoiGianDuKienToi
                FROM DATCHO dc
                WHERE dc.MaDatCho = ?
                FOR UPDATE
                """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                String qrTrongDb;
                String trangThai;
                int soGio;
                double thanhTien;
                double tongTienGoc;
                double thanhTienSauGiam;
                String maPGGDatTruoc;
                double tienGiamVoucherDatTruoc;
                double phanTramGiamHangTVDatTruoc;
                double tienGiamHangTVDatTruoc;
                String maKG;
                String maKH;
                String ghiChu;
                Timestamp thoiGianDuKienToi;

                try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
                    ps.setString(1, maDatCho.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new KetQuaNhanChoDTO(false, "Không tìm thấy đặt chỗ " + maDatCho + ".");
                        }
                        qrTrongDb = rs.getString("MaQR");
                        trangThai = rs.getString("TrangThaiDatTruoc");
                        soGio = Math.max(1, rs.getInt("KhoangThoiGianSuDung"));
                        thanhTien = rs.getDouble("ThanhTien");
                        tongTienGoc = rs.getDouble("TongTienGoc");
                        thanhTienSauGiam = rs.getDouble("ThanhTienSauGiam");
                        maPGGDatTruoc = rs.getString("MaPGG");
                        tienGiamVoucherDatTruoc = rs.getDouble("TienGiamVoucher");
                        phanTramGiamHangTVDatTruoc = rs.getDouble("PhanTramGiamHangTV");
                        tienGiamHangTVDatTruoc = rs.getDouble("TienGiamHangTV");
                        maKG = rs.getString("MaKG");
                        maKH = rs.getString("MaKH");
                        ghiChu = rs.getString("GhiChu");
                        thoiGianDuKienToi = rs.getTimestamp("ThoiGianDuKienToi");
                    }
                }

                if (chuanHoa(trangThai).contains("qua han nhan cho")) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Đặt chỗ này đã quá hạn nhận chỗ.");
                }

                if (chuanHoa(trangThai).contains("su dung")) {
                    boolean coPhien = false;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT COUNT(*) FROM PHIENLAMVIEC WHERE MaDatCho = ?")) {
                        ps.setString(1, maDatCho.trim());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                coPhien = true;
                            }
                        }
                    }
                    conn.rollback();
                    if (coPhien) {
                        return new KetQuaNhanChoDTO(false, "Mã QR này đã được sử dụng.");
                    }
                    return new KetQuaNhanChoDTO(false, "Đặt chỗ này đã quá hạn nhận chỗ.");
                }

                if (thoiGianDuKienToi != null) {
                    long gioDuKienKetThucMillis = thoiGianDuKienToi.getTime() + (long) soGio * 3600 * 1000;
                    long bayGioMillis = System.currentTimeMillis();
                    if (bayGioMillis > gioDuKienKetThucMillis) {
                        try (PreparedStatement ps = conn.prepareStatement("""
                                UPDATE DATCHO
                                SET TrangThaiDatTruoc = 'Quá hạn nhận chỗ',
                                    MaQR = NULL,
                                    GhiChu = CASE
                                        WHEN GhiChu LIKE '%[SYSTEM_NO_SHOW]%' THEN GhiChu
                                        ELSE NVL(GhiChu, '') || ' | [SYSTEM_NO_SHOW] Tự động kết thúc do quá hạn nhận chỗ.'
                                    END,
                                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                                WHERE MaDatCho = ?
                                """)) {
                            ps.setString(1, maDatCho.trim());
                            ps.executeUpdate();
                        }
                        try (PreparedStatement ps = conn.prepareStatement("""
                                UPDATE KHONGGIAN kg
                                SET TrangThaiKG =
                                    CASE
                                        WHEN EXISTS (
                                            SELECT 1
                                            FROM PHIENLAMVIEC p
                                            WHERE p.MaKG = kg.MaKG
                                              AND p.TrangThaiPhien = 'Đang hoạt động'
                                        ) THEN 'Đang hoạt động'
                                
                                        ELSE 'Trống'
                                    END
                                WHERE kg.MaKG = ?
                                """)) {
                            ps.setString(1, maKG);
                            ps.executeUpdate();
                        }
                        conn.commit();
                        return new KetQuaNhanChoDTO(false, "Đặt chỗ đã quá giờ nhận chỗ. Hệ thống đã nhả không gian.");
                    }
                }

                if (qrTrongDb == null || qrTrongDb.isBlank()) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Mã QR đã bị vô hiệu hoặc đặt chỗ chưa thanh toán.");
                }
                if (!qrTrongDb.trim().equals(noiDungQr.trim())) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Mã QR không khớp với đặt chỗ trong hệ thống.");
                }
                if (!chuanHoa(trangThai).contains("thanh toan thanh cong")) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Đặt chỗ chưa ở trạng thái đã thanh toán thành công.");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM PHIENLAMVIEC WHERE MaDatCho = ?")) {
                    ps.setString(1, maDatCho.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            return new KetQuaNhanChoDTO(false, "Đặt chỗ này đã được nhận trước đó.");
                        }
                    }
                }

                String maPhien = MaTuDongUtil.sinhMaTiepTheo(conn, MaTuDongUtil.MaDoiTuong.PHIEN_LAM_VIEC);
                java.sql.Timestamp thoiGianDuKienKetThuc = thoiGianDuKienToi != null
                        ? java.sql.Timestamp.from(thoiGianDuKienToi.toInstant().plus(soGio, java.time.temporal.ChronoUnit.HOURS))
                        : java.sql.Timestamp.from(java.time.Instant.now().plus(soGio, java.time.temporal.ChronoUnit.HOURS));

                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO PHIENLAMVIEC
                            (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien,
                             CapNhatLanCuoi, MaKG, MaKH, MaDatCho)
                        VALUES (?, CURRENT_TIMESTAMP, ?, 'Đang hoạt động', CURRENT_TIMESTAMP, ?, ?, ?)
                        """)) {
                    ps.setString(1, maPhien);
                    ps.setTimestamp(2, thoiGianDuKienKetThuc);
                    ps.setString(3, maKG);
                    ps.setString(4, maKH);
                    ps.setString(5, maDatCho.trim());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE HOADON
                        SET DaTraTruoc = ?,
                            TongTien = ?,
                            TongTienGoc = ?,
                            TienGocDatTruoc = ?,
                            TienGocPhatSinh = 0,
                            MaPGGDatTruoc = ?,
                            TienGiamVoucherDatTruoc = ?,
                            PhanTramGiamHangTVDatTruoc = ?,
                            TienGiamHangTVDatTruoc = ?,
                            TongTienGiam = ?,
                            SoTienThanhToanTaiQuay = 0,
                            ThanhTien = ?,
                            PhuongThucThanhToan = 'Đặt trước',
                            TrangThaiThanhToan = 'Đã trả trước',
                            NgayLapHoaDon = CURRENT_TIMESTAMP
                        WHERE MaPhien = ?
                        """)) {
                    if (tongTienGoc <= 0) {
                        tongTienGoc = thanhTien;
                    }
                    if (thanhTienSauGiam <= 0 && thanhTien > 0) {
                        thanhTienSauGiam = thanhTien;
                    }
                    ps.setDouble(1, thanhTienSauGiam);
                    ps.setDouble(2, tongTienGoc);
                    ps.setDouble(3, tongTienGoc);
                    ps.setDouble(4, tongTienGoc);
                    ps.setString(5, maPGGDatTruoc);
                    ps.setDouble(6, tienGiamVoucherDatTruoc);
                    ps.setDouble(7, phanTramGiamHangTVDatTruoc);
                    ps.setDouble(8, tienGiamHangTVDatTruoc);
                    ps.setDouble(9, tienGiamVoucherDatTruoc + tienGiamHangTVDatTruoc);
                    ps.setDouble(10, 0);
                    ps.setString(11, maPhien);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE DATCHO
                        SET TrangThaiDatTruoc = 'Đã sử dụng',
                            MaQR = NULL,
                            CapNhatLanCuoi = CURRENT_TIMESTAMP
                        WHERE MaDatCho = ?
                        """)) {
                    ps.setString(1, maDatCho.trim());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE KHONGGIAN SET TrangThaiKG = 'Đang hoạt động' WHERE MaKG = ?")) {
                    ps.setString(1, maKG);
                    ps.executeUpdate();
                }

                conn.commit();
                return new KetQuaNhanChoDTO(true, "Mở phiên từ vé đặt chỗ thành công. Mã phiên: " + maPhien,
                        maDatCho.trim(), maPhien);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi mở phiên từ QR đặt chỗ: " + e.getClass().getSimpleName());
            return new KetQuaNhanChoDTO(false, com.wms.util.ErrorMessageUtil.toUserMessage(e));
        }
    }

    private boolean laThongBaoThanhCong(String message) {
        if (message == null) {
            return false;
        }
        return chuanHoa(message).contains("thanh cong");
    }

    private String chuanHoa(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String boDau(String str) {
        if (str == null) return "";
        return Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }

    public List<PhienLamViecFullDTO> layDanhSachPhien(String keyword, String maCN) {
        List<PhienLamViecFullDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, kg.TenKG, nd.HoTen AS HoTenKH, dc.TrangThaiDatTruoc, h.TrangThaiThanhToan, lkg.DonGiaTheoGio "
                        +
                        "FROM PHIENLAMVIEC p " +
                        "JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                        "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                        "JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                        "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho " +
                        "LEFT JOIN HOADON h ON p.MaPhien = h.MaPhien " +
                        "WHERE (p.MaPhien LIKE ? OR nd.HoTen LIKE ? OR kg.TenKG LIKE ?)");

        if (maCN != null && !maCN.isEmpty()) {
            sql.append(" AND kg.MaCN = ?");
        }
        sql.append(" ORDER BY p.ThoiGianBatDau DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            String search = "%" + (keyword == null ? "" : keyword) + "%";
            pstmt.setString(1, search);
            pstmt.setString(2, search);
            pstmt.setString(3, search);
            if (maCN != null && !maCN.isEmpty()) {
                pstmt.setString(4, maCN);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PhienLamViecFullDTO dto = new PhienLamViecFullDTO();
                    dto.setMaPhien(rs.getString("MaPhien"));
                    dto.setThoiGianBatDau(rs.getTimestamp("ThoiGianBatDau"));
                    dto.setThoiGianDuKienKetThuc(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                    dto.setThoiGianKetThuc(rs.getTimestamp("ThoiGianKetThuc"));
                    dto.setTrangThaiPhien(rs.getString("TrangThaiPhien"));
                    dto.setMaKG(rs.getString("MaKG"));
                    dto.setMaKH(rs.getString("MaKH"));
                    dto.setMaDatCho(rs.getString("MaDatCho"));
                    dto.setDonGiaTheoGio(rs.getDouble("DonGiaTheoGio"));
                    dto.setTenKhongGian(rs.getString("TenKG"));
                    dto.setTenKhachHang(rs.getString("HoTenKH"));
                    dto.setTrangThaiDatCho(rs.getString("TrangThaiDatTruoc"));
                    dto.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi lấy danh sách: " + e.getMessage());
        }
        return list;
    }


    public boolean ketThucPhien(String maPhien) {
        return ketThucPhien(maPhien, null);
    }

    public boolean ketThucPhien(String maPhien, String maNV) {
        String callSql = "{call SP_KetThucPhien(?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {
            cstmt.setString(1, maPhien);
            if (maNV == null || maNV.isBlank()) {
                cstmt.setNull(2, Types.VARCHAR);
            } else {
                cstmt.setString(2, maNV.trim());
            }
            cstmt.registerOutParameter(3, Types.VARCHAR);
            cstmt.execute();

            String message = cstmt.getString(3);
            if (laThongBaoThanhCong(message)) {
                return true;
            }
            System.err.println("[PhienLamViecDAO] Lỗi từ SP_KetThucPhien: " + boDau(message));
            return false;
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi gọi SP_KetThucPhien: " + boDau(e.getMessage()));
            return false;
        }
    }

    public List<DichVuTrongPhienDTO> layDichVuCuaPhien(String maPhien) {
        List<DichVuTrongPhienDTO> list = new ArrayList<>();
        String sql = "SELECT dv.TenDV, ct.SoLuong, dv.DonGia " +
                "FROM CHITIETDICHVU ct " +
                "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                "WHERE ct.MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maPhien);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String ten = rs.getString("TenDV");
                    int sl = rs.getInt("SoLuong");
                    double dg = rs.getDouble("DonGia");
                    list.add(new DichVuTrongPhienDTO(ten, sl, dg, sl * dg));
                }
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi lấy dịch vụ: " + e.getMessage());
        }
        return list;
    }

    public ThongTinXacNhanDatChoDTO layThongTinXacNhanDatCho(String maDatCho, String maPhien) {
        String sql = """
                SELECT p.MaPhien, dc.MaDatCho, dc.MaQR, nd.HoTen, nd.Email,
                       kg.TenKG, cn.TenCN, p.ThoiGianBatDau, p.ThoiGianDuKienKetThuc,
                       h.ThanhTien
                FROM PHIENLAMVIEC p
                JOIN DATCHO dc ON dc.MaDatCho = p.MaDatCho
                JOIN KHACHHANG kh ON kh.MaKH = p.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = p.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN HOADON h ON h.MaPhien = p.MaPhien
                WHERE dc.MaDatCho = ? AND p.MaPhien = ?
                """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDatCho);
            ps.setString(2, maPhien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ThongTinXacNhanDatChoDTO dto = new ThongTinXacNhanDatChoDTO();
                    dto.setMaPhien(rs.getString("MaPhien"));
                    dto.setMaDatCho(rs.getString("MaDatCho"));
                    dto.setMaQR(rs.getString("MaQR"));
                    dto.setHoTen(rs.getString("HoTen"));
                    dto.setEmail(rs.getString("Email"));
                    dto.setTenKhongGian(rs.getString("TenKG"));
                    dto.setTenChiNhanh(rs.getString("TenCN"));
                    dto.setThoiGianBatDau(rs.getTimestamp("ThoiGianBatDau"));
                    dto.setThoiGianDuKienKetThuc(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                    dto.setThanhTien(rs.getBigDecimal("ThanhTien"));
                    return dto;
                }
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi lấy thông tin xác nhận đặt chỗ: " + e.getMessage());
        }
        return null;
    }

    public int demSoLuong() {
        String sql = "SELECT COUNT(*) FROM PHIENLAMVIEC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi đếm số lượng: " + e.getMessage());
        }
        return 0;
    }

    public boolean xoaPhien(String maPhien) {
        String sqlChiTiet = "DELETE FROM CHITIETDICHVU WHERE PhienLamViec = ?";
        if (maPhien != null) {
            // we will run standard queries here
        }
        String sqlChiTietReal = "DELETE FROM CHITIETDICHVU WHERE MaPhien = ?";
        String sqlHoaDon = "DELETE FROM HOADON WHERE MaPhien = ?";
        String sqlPhien = "DELETE FROM PHIENLAMVIEC WHERE MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(sqlChiTietReal)) {
                    ps.setString(1, maPhien);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
                    ps.setString(1, maPhien);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlPhien)) {
                    ps.setString(1, maPhien);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi khi xóa phiên: " + e.getMessage());
            return false;
        }
    }

    public void tuDongKetThucPhienQuaHanDatCho() {
        String querySql = "SELECT MaPhien FROM PHIENLAMVIEC " +
                          "WHERE TrangThaiPhien = 'Đang hoạt động' " +
                          "  AND MaDatCho IS NOT NULL " +
                          "  AND SYSTIMESTAMP >= ThoiGianDuKienKetThuc";
        
        List<String> dsPhienQuaHan = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(querySql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                dsPhienQuaHan.add(rs.getString("MaPhien"));
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Loi khi quet phien qua han: " + e.getMessage());
        }

        if (!dsPhienQuaHan.isEmpty()) {
            System.out.println("[Scheduler/Auto-End] Phat hien " + dsPhienQuaHan.size() + " phien dat truoc qua han dang hoat dong.");
            String callSql = "{call SP_KetThucPhien(?, ?, ?)}";
            for (String maPhien : dsPhienQuaHan) {
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     CallableStatement cstmt = conn.prepareCall(callSql)) {
                    cstmt.setString(1, maPhien);
                    cstmt.setString(2, null); // p_MaNV = null for system auto-end
                    cstmt.registerOutParameter(3, java.sql.Types.VARCHAR);
                    
                    cstmt.execute();
                    String outMsg = cstmt.getString(3);
                    System.out.println("[Scheduler/Auto-End] Da tu dong ket thuc MaPhien=" + maPhien + ". Ket qua SP: " + outMsg);
                } catch (Exception e) {
                    System.err.println("[Scheduler/Auto-End] Loi khi tu dong ket thuc MaPhien=" + maPhien + ": " + e.getMessage());
                }
            }
        }
    }
}
