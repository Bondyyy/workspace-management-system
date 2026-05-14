package com.wms.util;

import com.wms.config.DatabaseConnection;
import java.sql.*;
import java.text.Normalizer;

public class DataInitializer {

    private static Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public static void initializeAll(Connection conn) {
        System.out.println("[DataInitializer] Bắt đầu khởi tạo dữ liệu mặc định...");
        khoiTaoChucNang(conn);
        khoiTaoHangThanhVien(conn);
        khoiTaoDichVuMacDinh(conn);
        System.out.println("[DataInitializer] Hoàn tất khởi tạo dữ liệu.");
    }

    public static void khoiTaoDichVuMacDinh(Connection conn) {
        try {
            if (conn == null || conn.isClosed())
                return;

            // 1. Khởi tạo Loại Dịch Vụ mặc định (Khác / Hệ thống)
            String sqlLoaiDV = "MERGE INTO LOAIDICHVU dest USING (SELECT ? AS MaLoaiDV, ? AS TenLoaiDV, ? AS TrangThaiLDV FROM DUAL) src "
                    +
                    "ON (dest.MaLoaiDV = src.MaLoaiDV) " +
                    "WHEN MATCHED THEN UPDATE SET dest.TenLoaiDV = src.TenLoaiDV " +
                    "WHEN NOT MATCHED THEN INSERT (MaLoaiDV, TenLoaiDV, TrangThaiLDV) VALUES (src.MaLoaiDV, src.TenLoaiDV, src.TrangThaiLDV)";
            try (PreparedStatement ps = conn.prepareStatement(sqlLoaiDV)) {
                ps.setString(1, "LDV000");
                ps.setString(2, Normalizer.normalize("Tiện ích hệ thống", Normalizer.Form.NFC));
                ps.setString(3, Normalizer.normalize("Đang hoạt động", Normalizer.Form.NFC));
                ps.executeUpdate();
            }

            // 2. Khởi tạo Dịch vụ "Gia hạn giờ" (Không quản lý tồn kho, SoLuong = NULL)
            String sqlDV = "MERGE INTO DICHVU dest USING (SELECT ? AS MaDV, ? AS TenDV, ? AS DonGia, ? AS TrangThaiDV, ? AS MaLoaiDV, NULL AS SoLuong, NULL AS GiaNhap FROM DUAL) src "
                    +
                    "ON (dest.MaDV = src.MaDV) " +
                    "WHEN MATCHED THEN UPDATE SET dest.TenDV = src.TenDV " +
                    "WHEN NOT MATCHED THEN INSERT (MaDV, TenDV, DonGia, TrangThaiDV, MaLoaiDV, SoLuong, GiaNhap) VALUES (src.MaDV, src.TenDV, src.DonGia, src.TrangThaiDV, src.MaLoaiDV, src.SoLuong, src.GiaNhap)";
            try (PreparedStatement ps = conn.prepareStatement(sqlDV)) {
                ps.setString(1, "DV000");
                ps.setString(2, Normalizer.normalize("Gia hạn giờ", Normalizer.Form.NFC));
                ps.setDouble(3, 1000); 
                ps.setString(4, Normalizer.normalize("Đang hoạt động", Normalizer.Form.NFC));
                ps.setString(5, "LDV000");
                ps.executeUpdate();
            }

            System.out.println("[DataInitializer] Đồng bộ dữ liệu Dịch vụ mặc định thành công.");
        } catch (SQLException e) {
            System.err.println("[DataInitializer] Lỗi đồng bộ dữ liệu Dịch vụ: " + e.getMessage());
        }
    }

    public static void khoiTaoChucNang(Connection conn) {
        String[][] data = {
                { "CN01", "Tổng quan", "Xem dashboard thống kê và báo cáo" },
                { "CN02", "Chi nhánh", "Quản lý thông tin và trạng thái các chi nhánh" },
                { "CN03", "Không gian", "Quản lý sơ đồ, vị trí và trạng thái chỗ ngồi/phòng" },
                { "CN04", "Thông tin Dịch vụ", "Quản lý danh mục dịch vụ F&B và tiện ích" },
                { "CN05", "Kho Dịch vụ", "Quản lý nhập xuất tồn kho hàng hóa dịch vụ" },
                { "CN06", "Dịch vụ Khách đặt", "Quản lý các yêu cầu dịch vụ từ khách hàng" },
                { "CN07", "Phiên làm việc", "Theo dõi và quản lý các ca trực, phiên sử dụng" },
                { "CN08", "Hóa đơn & Thu ngân", "Xử lý thanh toán, xuất hóa đơn và báo cáo doanh thu" },
                { "CN09", "Khuyến mãi (Voucher)", "Tạo và quản lý các chương trình ưu đãi, mã giảm giá" },
                { "CN10", "Hội viên / Khách hàng", "Quản lý thông tin, hạng thành viên và lịch sử tích điểm" },
                { "CN11", "Nhân sự / Phân quyền", "Quản lý tài khoản nhân viên và thiết lập quyền hạn" }
        };

        try {
            if (conn == null || conn.isClosed())
                return;
            String mergeSql = "MERGE INTO CHUCNANG dest USING (SELECT ? AS MaChucNang, ? AS TenChucNang, ? AS MoTa FROM DUAL) src "
                    +
                    "ON (dest.MaChucNang = src.MaChucNang) " +
                    "WHEN MATCHED THEN UPDATE SET dest.TenChucNang = src.TenChucNang, dest.MoTa = src.MoTa " +
                    "WHEN NOT MATCHED THEN INSERT (MaChucNang, TenChucNang, MoTa) VALUES (src.MaChucNang, src.TenChucNang, src.MoTa)";

            try (PreparedStatement ps = conn.prepareStatement(mergeSql)) {
                for (String[] row : data) {
                    ps.setString(1, row[0]);
                    ps.setString(2, row[1]);
                    ps.setString(3, row[2]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (Statement st = conn.createStatement()) {
                st.executeUpdate(
                        "DELETE FROM CHITIETCHUCNANG WHERE MaChucNang NOT IN ('CN01','CN02','CN03','CN04','CN05','CN06','CN07','CN08','CN09','CN10','CN11')");
                st.executeUpdate(
                        "DELETE FROM CHUCNANG WHERE MaChucNang NOT IN ('CN01','CN02','CN03','CN04','CN05','CN06','CN07','CN08','CN09','CN10','CN11')");
            }
            System.out.println("[DataInitializer] Đồng bộ dữ liệu bảng CHUCNANG thành công.");
        } catch (SQLException e) {
            System.err.println("[DataInitializer] Lỗi đồng bộ dữ liệu CHUCNANG: " + e.getMessage());
        }
    }

    public static void khoiTaoHangThanhVien(Connection conn) {
        Object[][] data = {
                { "HTV00", "Không có", 0, 0 },
                { "HTV01", "Đồng", 0, 2 },
                { "HTV02", "Bạc", 5000000, 5 },
                { "HTV03", "Vàng", 15000000, 10 },
                { "HTV04", "Kim cương", 50000000, 15 }
        };

        try {
            if (conn == null || conn.isClosed())
                return;
            String mergeSql = "MERGE INTO HANGTHANHVIEN dest USING (SELECT ? AS MaHangThanhVien, ? AS TenHangThanhVien, ? AS TongChiTieuToiThieu, ? AS PhanTramTienGiam FROM DUAL) src "
                    +
                    "ON (dest.MaHangThanhVien = src.MaHangThanhVien) " +
                    "WHEN MATCHED THEN UPDATE SET dest.TenHangThanhVien = src.TenHangThanhVien, dest.TongChiTieuToiThieu = src.TongChiTieuToiThieu, dest.PhanTramTienGiam = src.PhanTramTienGiam "
                    +
                    "WHEN NOT MATCHED THEN INSERT (MaHangThanhVien, TenHangThanhVien, TongChiTieuToiThieu, PhanTramTienGiam) VALUES (src.MaHangThanhVien, src.TenHangThanhVien, src.TongChiTieuToiThieu, src.PhanTramTienGiam)";

            try (PreparedStatement ps = conn.prepareStatement(mergeSql)) {
                for (Object[] row : data) {
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, Normalizer.normalize((String) row[1], Normalizer.Form.NFC));
                    ps.setDouble(3, ((Number) row[2]).doubleValue());
                    ps.setDouble(4, ((Number) row[3]).doubleValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("[DataInitializer] Đồng bộ dữ liệu bảng HANGTHANHVIEN thành công.");
        } catch (SQLException e) {
            System.err.println("[DataInitializer] Lỗi đồng bộ dữ liệu HANGTHANHVIEN: " + e.getMessage());
        }
    }

}
