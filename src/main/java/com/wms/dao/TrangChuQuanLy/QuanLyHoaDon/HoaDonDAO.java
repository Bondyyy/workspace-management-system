package com.wms.dao.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DiscountLine;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.HoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.InvoiceLine;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.KetQuaThanhToanDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.util.HangThanhVienUtil;
import com.wms.util.HangThanhVienUtil.HangThanhVienSnapshot;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HoaDonDAO {

    private static final long MILLIS_IN_HOUR = 3600000;

    public List<HoaDonDTO> layDanhSachHoaDon(String searchText, String statusFilter) {
        long start = System.currentTimeMillis();
        List<HoaDonDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT h.*, nd.HoTen AS HoTenKH, p.MaDatCho, p.TrangThaiPhien, "
                        + "p.ThoiGianBatDau, p.ThoiGianKetThuc, p.ThoiGianDuKienKetThuc, "
                        + "CASE WHEN dc.TrangThaiDatTruoc IN ('Đã thanh toán thành công', 'Đã sử dụng') "
                        + "THEN NVL(dc.ThanhTien, 0) ELSE 0 END AS SoTienDaTraTruoc "
                        + "FROM HOADON h "
                        + "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien "
                        + "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho "
                        + "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH "
                        + "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND "
                        + "WHERE 1=1 ");

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append("AND (h.MaHoaDon LIKE ? OR h.SoHD LIKE ? OR h.MaPhien LIKE ? OR nd.HoTen LIKE ?) ");
        }
        if (statusFilter != null && !statusFilter.equals("Tất cả")) {
            if (statusFilter.equals("Chưa thanh toán")) {
                sql.append("AND h.TrangThaiThanhToan = 'Đang chờ thanh toán' ");
            } else if (statusFilter.equals("Đã trả trước")) {
                sql.append("AND h.TrangThaiThanhToan = 'Đã trả trước' ");
            } else if (statusFilter.equals("Đã thanh toán")) {
                sql.append("AND h.TrangThaiThanhToan = 'Đã thanh toán thành công' ");
            } else if (statusFilter.equals("Đang chờ thanh toán phụ thu")) {
                sql.append("AND NVL(dc.ThanhTien, 0) > 0 AND NVL(h.ThanhTien, 0) > 0 "
                        + "AND h.TrangThaiThanhToan <> 'Đã thanh toán thành công' ");
            } else if (statusFilter.equals("Đã hủy")) {
                sql.append("AND h.TrangThaiThanhToan = 'Thanh toán không thành công' ");
            } else {
                sql.append("AND h.TrangThaiThanhToan = ? ");
            }
        }
        sql.append("ORDER BY h.NgayLapHoaDon DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String q = "%" + searchText.trim() + "%";
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
            }
            if (statusFilter != null && !statusFilter.equals("Tất cả")
                    && !statusFilter.equals("Chưa thanh toán")
                    && !statusFilter.equals("Đã trả trước")
                    && !statusFilter.equals("Đã thanh toán")
                    && !statusFilter.equals("Đang chờ thanh toán phụ thu")
                    && !statusFilter.equals("Đã hủy")) {
                ps.setString(idx++, statusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(anhXaHoaDon(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logElapsed("load danh sach hoa don", start);
        }
        return list;
    }

    public boolean taoHoaDonMoi(HoaDonDTO hd) {
        long start = System.currentTimeMillis();
        String sql = "INSERT INTO HOADON (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon, "
                + "TrangThaiThanhToan, MaPhien) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (hd.getMaPhien() != null && !hd.getMaPhien().isBlank()) {
                try (PreparedStatement check = conn.prepareStatement(
                        "SELECT COUNT(*) FROM HOADON WHERE MaPhien = ?")) {
                    check.setString(1, hd.getMaPhien());
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return false;
                        }
                    }
                }
            }

            pstmt.setString(1, hd.getMaHoaDon());
            pstmt.setString(2, hd.getSoHD());
            pstmt.setDouble(3, giaTriTien(hd.getTongTien()));
            pstmt.setDouble(4, giaTriTien(hd.getThanhTien()));
            pstmt.setString(5, "Đang chờ thanh toán");
            pstmt.setString(6, hd.getMaPhien());
            return pstmt.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("[HoaDonDAO] Khong tao hoa don trung MaPhien: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            logElapsed("tao hoa don moi", start);
        }
    }

    public HoaDonDTO capNhatHoaDonDatTruocTheoPhien(String maPhien, double tongTien, double thanhTien) {
        String sqlUpdate = "UPDATE HOADON SET TongTien = ?, ThanhTien = ? WHERE MaPhien = ?";
        String sqlSelect = "SELECT h.*, nd.HoTen AS HoTenKH, p.MaDatCho, p.TrangThaiPhien, "
                + "p.ThoiGianBatDau, p.ThoiGianKetThuc, p.ThoiGianDuKienKetThuc, "
                + "CASE WHEN dc.TrangThaiDatTruoc IN ('Đã thanh toán thành công', 'Đã sử dụng') "
                + "THEN NVL(dc.ThanhTien, 0) ELSE 0 END AS SoTienDaTraTruoc "
                + "FROM HOADON h "
                + "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien "
                + "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho "
                + "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH "
                + "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND "
                + "WHERE h.MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
            psUpdate.setDouble(1, tongTien);
            psUpdate.setDouble(2, thanhTien);
            psUpdate.setString(3, maPhien);
            psUpdate.executeUpdate();

            try (PreparedStatement psSelect = conn.prepareStatement(sqlSelect)) {
                psSelect.setString(1, maPhien);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        return anhXaHoaDon(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean capNhatTrangThaiThanhToanTheoPhien(String maPhien, String trangThai) {
        String sql = "UPDATE HOADON SET TrangThaiThanhToan = ?, NgayLapHoaDon = CURRENT_TIMESTAMP WHERE MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trangThai);
            pstmt.setString(2, maPhien);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ThongTinHoaDonDTO layThongTinChiTietHoaDon(String maHoaDon) {
        long start = System.currentTimeMillis();
        String sqlChung = """
                SELECT h.MaHoaDon, h.NgayLapHoaDon, h.PhuongThucThanhToan,
                       h.TongTien AS TongTienLuu, h.ThanhTien AS SoTienCanThuTaiQuay,
                       h.MaPGG, p.MaPhien, p.ThoiGianBatDau, p.ThoiGianKetThuc,
                       p.ThoiGianDuKienKetThuc, p.TrangThaiPhien, h.TrangThaiThanhToan,
                       kh.MaKH, nd.HoTen AS HoTenKH, kg.TenKG, cn.TenCN, p.MaDatCho,
                       dc.KhoangThoiGianSuDung, NVL(dc.ThanhTien, 0) AS TienDatChoDaTra,
                       NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio,
                       htv.TenHangThanhVien,
                       NVL(htv.PhanTramTienGiam, 0) AS PhanTramGiamHangThanhVien,
                       FN_TinhTienKhongGian(p.MaPhien) AS TienKhongGianTinh,
                       FN_TinhTienDichVu(p.MaPhien) AS TienDichVuTinh,
                       FN_TinhTongTien(p.MaPhien) AS TongTienTinh
                FROM HOADON h
                LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien
                LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH
                LEFT JOIN HANGTHANHVIEN htv ON kh.MaHangThanhVien = htv.MaHangThanhVien
                LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND
                LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG
                LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG
                LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN
                LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho
                WHERE h.MaHoaDon = ?
                """;
        String sqlDichVu = """
                SELECT dv.TenDV, NVL(ct.SoLuong, 0) AS SoLuong,
                       NVL(dv.DonGia, 0) AS DonGia,
                       NVL(ct.SoLuong, 0) * NVL(dv.DonGia, 0) AS ThanhTien
                FROM CHITIETDICHVU ct
                JOIN DICHVU dv ON ct.MaDV = dv.MaDV
                WHERE ct.MaPhien = ?
                ORDER BY dv.TenDV
                """;
        String sqlVoucher = """
                SELECT ct.MaPGG, pgg.MaChuSoPGG, ct.NguonApDung,
                       SUM(NVL(ct.SoTienGiam, 0)) AS SoTienGiam
                FROM CHITIETAPDUNGPGG ct
                LEFT JOIN PHIEUGIAMGIA pgg ON pgg.MaPGG = ct.MaPGG
                WHERE ct.MaHoaDon = ? OR ct.MaDatCho = ?
                GROUP BY ct.MaPGG, pgg.MaChuSoPGG, ct.NguonApDung
                ORDER BY CASE WHEN ct.NguonApDung = 'DAT_TRUOC' THEN 0 ELSE 1 END, ct.MaPGG
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement psChung = conn.prepareStatement(sqlChung)) {
            psChung.setString(1, maHoaDon);
            try (ResultSet rsChung = psChung.executeQuery()) {
                if (!rsChung.next()) {
                    return null;
                }

                ThongTinHoaDonDTO thongTin = new ThongTinHoaDonDTO();
                thongTin.setMaHoaDon(rsChung.getString("MaHoaDon"));
                thongTin.setMaPhien(rsChung.getString("MaPhien"));
                thongTin.setHoTenKH(rsChung.getString("HoTenKH"));
                thongTin.setTenKhongGian(rsChung.getString("TenKG"));
                thongTin.setTenChiNhanh(rsChung.getString("TenCN"));
                thongTin.setPhuongThucThanhToan(rsChung.getString("PhuongThucThanhToan"));
                thongTin.setTrangThaiPhien(rsChung.getString("TrangThaiPhien"));
                thongTin.setTrangThaiThanhToan(rsChung.getString("TrangThaiThanhToan"));

                Timestamp ngayLap = rsChung.getTimestamp("NgayLapHoaDon");
                if (ngayLap != null) {
                    thongTin.setNgayLapHoaDon(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ngayLap));
                }

                Timestamp batDau = rsChung.getTimestamp("ThoiGianBatDau");
                Timestamp ketThuc = rsChung.getTimestamp("ThoiGianKetThuc");
                Timestamp duKienKetThuc = rsChung.getTimestamp("ThoiGianDuKienKetThuc");
                capNhatThoiGianSuDung(thongTin, batDau, ketThuc, duKienKetThuc);

                String maDatCho = rsChung.getString("MaDatCho");
                boolean laDatTruoc = maDatCho != null && !maDatCho.isBlank();
                double soGioDatTruoc = Math.max(0, rsChung.getDouble("KhoangThoiGianSuDung"));
                double soGioTinhTien = laDatTruoc && soGioDatTruoc > 0
                        ? soGioDatTruoc
                        : Math.max(1, thongTin.getTongSoGio());
                double donGiaTheoGio = Math.max(0, rsChung.getDouble("DonGiaTheoGio"));
                double tienKhongGian = Math.max(0, rsChung.getDouble("TienKhongGianTinh"));
                if (tienKhongGian <= 0 && donGiaTheoGio > 0) {
                    tienKhongGian = donGiaTheoGio * soGioTinhTien;
                }

                if (rsChung.getString("TenKG") != null || tienKhongGian > 0) {
                    String tenDong = "Thuê " + (rsChung.getString("TenKG") == null ? "không gian" : rsChung.getString("TenKG"));
                    if (laDatTruoc) {
                        tenDong += " (đặt trước)";
                    }
                    themDongChiPhi(thongTin, tenDong, (int) Math.max(1, Math.round(soGioTinhTien)),
                            soGioTinhTien > 0 ? tienKhongGian / soGioTinhTien : tienKhongGian,
                            tienKhongGian, laDatTruoc);
                }

                String maPhien = thongTin.getMaPhien();
                if (maPhien != null && !maPhien.isBlank()) {
                    try (PreparedStatement psDV = conn.prepareStatement(sqlDichVu)) {
                        psDV.setString(1, maPhien);
                        try (ResultSet rsDV = psDV.executeQuery()) {
                            while (rsDV.next()) {
                                String tenDv = rsDV.getString("TenDV");
                                int soLuong = rsDV.getInt("SoLuong");
                                double donGia = Math.max(0, rsDV.getDouble("DonGia"));
                                double thanhTien = Math.max(0, rsDV.getDouble("ThanhTien"));
                                themDongChiPhi(thongTin, tenDv, soLuong, donGia, thanhTien, false);
                                thongTin.getDanhSachDichVuPhatSinh().add(new DichVuDaDungDTO(tenDv, soLuong, donGia, thanhTien));
                            }
                        }
                    }
                }

                double tongChiPhi = thongTin.getDongChiPhi().stream().mapToDouble(InvoiceLine::getThanhTien).sum();
                if (tongChiPhi <= 0) {
                    tongChiPhi = Math.max(Math.max(0, rsChung.getDouble("TongTienTinh")),
                            Math.max(0, rsChung.getDouble("TongTienLuu")));
                    thongTin.setTongTien(tongChiPhi);
                    thongTin.setTongTienGoc(tongChiPhi);
                }

                double tongVoucher = 0;
                try (PreparedStatement psVoucher = conn.prepareStatement(sqlVoucher)) {
                    psVoucher.setString(1, maHoaDon);
                    psVoucher.setString(2, maDatCho);
                    try (ResultSet rsVoucher = psVoucher.executeQuery()) {
                        while (rsVoucher.next()) {
                            String nguon = rsVoucher.getString("NguonApDung");
                            boolean voucherDatTruoc = "DAT_TRUOC".equalsIgnoreCase(nguon);
                            String maPGG = rsVoucher.getString("MaPGG");
                            String maChuSo = rsVoucher.getString("MaChuSoPGG");
                            double soTienGiam = Math.max(0, rsVoucher.getDouble("SoTienGiam"));
                            if (soTienGiam <= 0) {
                                continue;
                            }
                            String maHienThi = maChuSo != null && !maChuSo.isBlank() ? maChuSo : maPGG;
                            String noiDung = "Áp dụng voucher " + (maHienThi == null ? "" : maHienThi);
                            if (voucherDatTruoc) {
                                noiDung += " (đặt trước)";
                            }
                            thongTin.getDongVoucher().add(new DiscountLine(maPGG, maChuSo, noiDung, soTienGiam, voucherDatTruoc));
                            tongVoucher += soTienGiam;
                        }
                    }
                }

                String maKH = rsChung.getString("MaKH");
                String tenHangThanhVien = rsChung.getString("TenHangThanhVien");
                double phanTramHang = Math.max(0, rsChung.getDouble("PhanTramGiamHangThanhVien"));
                if (maKH != null && HangThanhVienUtil.laHangKhongCo(tenHangThanhVien)) {
                    HangThanhVienSnapshot hangMacDinh = HangThanhVienUtil.layHangKhachHangMacDinh(conn);
                    tenHangThanhVien = hangMacDinh.tenHangThanhVien();
                    phanTramHang = Math.max(0,
                            hangMacDinh.phanTramTienGiam() == null ? 0 : hangMacDinh.phanTramTienGiam().doubleValue());
                } else if (maKH == null) {
                    tenHangThanhVien = HangThanhVienUtil.TEN_HANG_KHONG_CO;
                    phanTramHang = 0;
                }
                double soTienConLai = Math.max(0, tongChiPhi - tongVoucher);
                double tienGiamHang = lamTronTien(soTienConLai * Math.min(100, phanTramHang) / 100.0);
                double thanhTienSauGiam = Math.max(0, soTienConLai - tienGiamHang);
                double soTienCanThuTaiQuay = Math.max(0, rsChung.getDouble("SoTienCanThuTaiQuay"));
                double tienDatChoDaTra = laDatTruoc ? Math.max(0, rsChung.getDouble("TienDatChoDaTra")) : 0;

                thongTin.setTongTien(tongChiPhi);
                thongTin.setTongTienGoc(tongChiPhi);
                thongTin.setSoTienConLai(soTienConLai);
                thongTin.setThanhTien(thanhTienSauGiam);
                thongTin.setSoTienCanThanhToan(soTienCanThuTaiQuay);
                thongTin.setSoTienDaTraTruoc(tienDatChoDaTra);
                thongTin.setDaTraTruoc(tienDatChoDaTra > 0);
                thongTin.setTenHangThanhVien(tenHangThanhVien);
                thongTin.setPhanTramGiamHangThanhVien(phanTramHang);
                thongTin.setSoTienGiamVoucher(tongVoucher);
                thongTin.setTienGiamHang(tienGiamHang);
                thongTin.setTongTienGiam(tongVoucher + tienGiamHang);
                thongTin.setMaPGG(rsChung.getString("MaPGG"));
                ganThongTinVoucherTuDongMoi(thongTin);

                // Compatibility getters for old Swing panels are computed from source joins.
                thongTin.setTienGocDatTruoc(laDatTruoc ? tienKhongGian : 0);
                thongTin.setTienGocPhatSinh(Math.max(0, tongChiPhi - (laDatTruoc ? tienKhongGian : 0)));
                thongTin.setSoTienThanhToanTaiQuay(0);

                return thongTin;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logElapsed("load chi tiet hoa don", start);
        }
        return null;
    }

    public boolean xacNhanThanhToan(String maHoaDon, String phuongThucThanhToan, String maNV, String maPGG,
            double thanhTien) {
        String sqlUpdate = "UPDATE HOADON SET PhuongThucThanhToan = ?, MaNV = ?, MaPGG = ?, ThanhTien = 0, "
                + "TrangThaiThanhToan = 'Đã thanh toán thành công', NgayLapHoaDon = CURRENT_TIMESTAMP "
                + "WHERE MaHoaDon = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                boolean canTangVoucher = luuVoucherTaiQuayNeuCo(conn, maHoaDon, maPGG);
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                    pstmt.setString(1, phuongThucThanhToan);
                    pstmt.setString(2, maNV);
                    pstmt.setString(3, maPGG == null || maPGG.isBlank() ? null : maPGG.trim());
                    pstmt.setString(4, maHoaDon);
                    int updated = pstmt.executeUpdate();
                    if (updated > 0 && canTangVoucher) {
                        tangSoLanDungVoucher(conn, maPGG);
                    }
                    conn.commit();
                    return updated > 0;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean thanhToanVoiPhieuGiamGia(String maPhien, String maNV, String maPGG, String phuongThucThanhToan) {
        String sql = "{call SP_ThanhToanVoiPhieuGiamGia(?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, maPhien);
            cstmt.setString(2, maNV);
            if (maPGG == null || maPGG.trim().isEmpty()) {
                cstmt.setNull(3, Types.VARCHAR);
            } else {
                cstmt.setString(3, maPGG.trim());
            }
            cstmt.setString(4, phuongThucThanhToan);
            cstmt.registerOutParameter(5, Types.VARCHAR);
            cstmt.execute();

            String message = cstmt.getString(5);
            System.out.println("[HoaDonDAO] " + message);
            return laThongBaoThanhCong(message);
        } catch (Exception e) {
            System.err.println("[HoaDonDAO] Lỗi gọi SP_ThanhToanVoiPhieuGiamGia: " + e.getMessage());
            return false;
        }
    }

    public boolean huyHoaDon(String maHoaDon) {
        String sql = "UPDATE HOADON SET TrangThaiThanhToan = 'Thanh toán không thành công' WHERE MaHoaDon = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaHoaDon(String maHoaDon) {
        String sql = "DELETE FROM HOADON WHERE MaHoaDon = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public KetQuaThanhToanDTO thanhToanVoiPhieuGiamGiaMoi(String maPhien, String maNV, String maPGG, String phuongThucThanhToan) {
        long start = System.currentTimeMillis();
        String sql = "{call SP_ThanhToanVoiPhieuGiamGia(?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, maPhien);
            cstmt.setString(2, maNV);
            if (maPGG == null || maPGG.trim().isEmpty()) {
                cstmt.setNull(3, Types.VARCHAR);
            } else {
                cstmt.setString(3, maPGG.trim());
            }
            cstmt.setString(4, phuongThucThanhToan);
            cstmt.registerOutParameter(5, Types.VARCHAR);

            cstmt.execute();
            String message = cstmt.getString(5);
            System.out.println("[HoaDonDAO] SP_ThanhToanVoiPhieuGiamGia: " + message);

            if (!laThongBaoThanhCong(message) || laThongBaoLoi(message)) {
                return new KetQuaThanhToanDTO(false, message != null ? message : "Lỗi khi cập nhật thanh toán");
            }

            return new KetQuaThanhToanDTO(true, message != null ? message : "Thanh toán thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return new KetQuaThanhToanDTO(false, "Lỗi khi cập nhật thanh toán: " + e.getMessage());
        } finally {
            logElapsed("thanh toan SP", start);
        }
    }

    public KetQuaThanhToanDTO thanhToanTrucTiepMoi(String maHoaDon, String phuongThucThanhToan, String maNV, String maPGG, double thanhTien) {
        boolean success = xacNhanThanhToan(maHoaDon, phuongThucThanhToan, maNV, maPGG, thanhTien);
        if (success) {
            return new KetQuaThanhToanDTO(true, "Thanh toán thành công");
        }
        return new KetQuaThanhToanDTO(false, "Lỗi khi cập nhật thanh toán");
    }

    public String timMaHoaDonTheoMaPhien(String maPhien) {
        if (maPhien == null || maPhien.isBlank()) {
            return null;
        }
        String sql = "SELECT MaHoaDon FROM HOADON WHERE MaPhien = ? FETCH FIRST 1 ROWS ONLY";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhien.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("MaHoaDon") : null;
            }
        } catch (Exception e) {
            System.err.println("[HoaDonDAO] Lỗi tìm hóa đơn theo phiên: " + e.getMessage());
            return null;
        }
    }

    private HoaDonDTO anhXaHoaDon(ResultSet rs) throws SQLException {
        HoaDonDTO hd = new HoaDonDTO();
        hd.setMaHoaDon(rs.getString("MaHoaDon"));
        hd.setSoHD(rs.getString("SoHD"));
        hd.setTongTien(rs.getDouble("TongTien"));
        hd.setThanhTien(rs.getDouble("ThanhTien"));
        hd.setNgayLapHoaDon(rs.getTimestamp("NgayLapHoaDon"));
        hd.setPhuongThucThanhToan(rs.getString("PhuongThucThanhToan"));
        hd.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
        hd.setMaPhien(rs.getString("MaPhien"));
        hd.setMaPGG(rs.getString("MaPGG"));
        hd.setMaNV(rs.getString("MaNV"));
        hd.setHoTenKH(rs.getString("HoTenKH"));
        hd.setMaDatCho(rs.getString("MaDatCho"));
        hd.setTrangThaiPhien(rs.getString("TrangThaiPhien"));
        hd.setThoiGianBatDauPhien(rs.getTimestamp("ThoiGianBatDau"));
        hd.setThoiGianKetThucPhien(rs.getTimestamp("ThoiGianKetThuc"));
        hd.setThoiGianDuKienKetThucPhien(rs.getTimestamp("ThoiGianDuKienKetThuc"));
        hd.setSoTienDaTraTruoc(Math.max(0, rs.getDouble("SoTienDaTraTruoc")));
        hd.setDaTraTruoc(hd.getSoTienDaTraTruoc() > 0);
        return hd;
    }

    private void capNhatThoiGianSuDung(ThongTinHoaDonDTO thongTin, Timestamp batDau,
                                       Timestamp ketThuc, Timestamp duKienKetThuc) {
        if (batDau == null) {
            return;
        }
        Timestamp mocKetThuc = ketThuc != null ? ketThuc : duKienKetThuc;
        if (mocKetThuc != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            thongTin.setThoiGianSửDung(timeFormat.format(batDau) + " - " + timeFormat.format(mocKetThuc)
                    + " (" + dateFormat.format(batDau) + ")");
            long totalMinutes = Math.max(0, (mocKetThuc.getTime() - batDau.getTime()) / 60000);
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            thongTin.setTongSoGio(minutes <= 15 ? hours : hours + 1);
        }
    }

    private void themDongChiPhi(ThongTinHoaDonDTO thongTin, String noiDung, int soLuong,
                                double donGia, double thanhTien, boolean datTruoc) {
        int soLuongAnToan = Math.max(1, soLuong);
        double thanhTienAnToan = Math.max(0, thanhTien);
        double donGiaAnToan = Math.max(0, donGia);
        thongTin.getDongChiPhi().add(new InvoiceLine(noiDung, soLuongAnToan, donGiaAnToan, thanhTienAnToan, datTruoc));
        thongTin.getDanhSachDichVu().add(new DichVuDaDungDTO(noiDung, soLuongAnToan, donGiaAnToan, thanhTienAnToan));
    }

    private void ganThongTinVoucherTuDongMoi(ThongTinHoaDonDTO thongTin) {
        for (DiscountLine line : thongTin.getDongVoucher()) {
            if (thongTin.getMaVoucher() == null) {
                thongTin.setMaVoucher(line.getMaChuSoPGG() != null ? line.getMaChuSoPGG() : line.getMaPGG());
            }
            if (thongTin.getMaChuSoPGG() == null) {
                thongTin.setMaChuSoPGG(line.getMaChuSoPGG());
            }
            if (line.isDatTruoc()) {
                thongTin.setMaPGGDatTruoc(line.getMaPGG());
                thongTin.setMaChuSoPGGDatTruoc(line.getMaChuSoPGG());
                thongTin.setTienGiamVoucherDatTruoc(line.getSoTienGiam());
            } else {
                thongTin.setMaPGGTaiQuay(line.getMaPGG());
                thongTin.setMaChuSoPGGTaiQuay(line.getMaChuSoPGG());
                thongTin.setTienGiamVoucherTaiQuay(line.getSoTienGiam());
            }
        }
    }

    private boolean luuVoucherTaiQuayNeuCo(Connection conn, String maHoaDon, String maPGG) throws SQLException {
        if (maPGG == null || maPGG.isBlank()) {
            return false;
        }
        String maPhien = null;
        String maDatCho = null;
        String trangThaiThanhToan = null;
        double tongTien = 0;
        double tienDatCho = 0;
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT h.MaPhien, h.TrangThaiThanhToan,
                       NVL(NULLIF(h.TongTien, 0), FN_TinhTongTien(h.MaPhien)) AS TongTienTinh,
                       p.MaDatCho,
                       CASE WHEN dc.TrangThaiDatTruoc IN ('Đã thanh toán thành công', 'Đã sử dụng')
                            THEN NVL(dc.ThanhTien, 0) ELSE 0 END AS TienDatChoDaTra
                FROM HOADON h
                LEFT JOIN PHIENLAMVIEC p ON p.MaPhien = h.MaPhien
                LEFT JOIN DATCHO dc ON dc.MaDatCho = p.MaDatCho
                WHERE h.MaHoaDon = ?
                FOR UPDATE
                """)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maPhien = rs.getString("MaPhien");
                    maDatCho = rs.getString("MaDatCho");
                    trangThaiThanhToan = rs.getString("TrangThaiThanhToan");
                    tongTien = Math.max(0, rs.getDouble("TongTienTinh"));
                    tienDatCho = Math.max(0, rs.getDouble("TienDatChoDaTra"));
                }
            }
        }
        if (maPhien == null) {
            return false;
        }

        double voucherDatTruoc = tongVoucher(conn, maHoaDon, maDatCho, "DAT_TRUOC");
        double giaTriVoucher = giaTriVoucher(conn, maPGG.trim());
        double coSoGiam = Math.max(0, tongTien - voucherDatTruoc - tienDatCho);
        double soTienGiam = Math.min(giaTriVoucher, coSoGiam);

        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM CHITIETAPDUNGPGG WHERE MaHoaDon = ? AND NguonApDung = 'TAI_QUAY'")) {
            delete.setString(1, maHoaDon);
            delete.executeUpdate();
        }
        if (soTienGiam <= 0) {
            return false;
        }
        try (PreparedStatement insert = conn.prepareStatement("""
                INSERT INTO CHITIETAPDUNGPGG (
                    MaApDung, MaPGG, MaDatCho, MaHoaDon, NguonApDung, SoTienGiam, ThoiGianApDung
                ) VALUES (
                    RAWTOHEX(SYS_GUID()), ?, NULL, ?, 'TAI_QUAY', ?, CURRENT_TIMESTAMP
                )
                """)) {
            insert.setString(1, maPGG.trim());
            insert.setString(2, maHoaDon);
            insert.setDouble(3, soTienGiam);
            insert.executeUpdate();
        }
        return !"Đã thanh toán thành công".equals(trangThaiThanhToan);
    }

    private double tongVoucher(Connection conn, String maHoaDon, String maDatCho, String nguon) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT NVL(SUM(SoTienGiam), 0)
                FROM CHITIETAPDUNGPGG
                WHERE (MaHoaDon = ? OR MaDatCho = ?)
                  AND NguonApDung = ?
                """)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maDatCho);
            ps.setString(3, nguon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Math.max(0, rs.getDouble(1)) : 0;
            }
        }
    }

    private double giaTriVoucher(Connection conn, String maPGG) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT NVL(GiaTriGiamGia, 0) FROM PHIEUGIAMGIA WHERE MaPGG = ?")) {
            ps.setString(1, maPGG);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Math.max(0, rs.getDouble(1)) : 0;
            }
        }
    }

    private void tangSoLanDungVoucher(Connection conn, String maPGG) throws SQLException {
        if (maPGG == null || maPGG.isBlank()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE PHIEUGIAMGIA SET SLDaDung = NVL(SLDaDung, 0) + 1 WHERE MaPGG = ?")) {
            ps.setString(1, maPGG.trim());
            ps.executeUpdate();
        }
    }

    private boolean laThongBaoThanhCong(String message) {
        if (message == null) {
            return false;
        }
        String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized.contains("thanh toan thanh cong");
    }

    private boolean laThongBaoLoi(String message) {
        if (message == null) {
            return false;
        }
        String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized.contains("loi")
                || normalized.contains("du lieu loi")
                || normalized.contains("khong tim thay");
    }

    private double giaTriTien(Double value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private double lamTronTien(double value) {
        return Math.round(Math.max(0, value));
    }

    private void logElapsed(String action, long start) {
        System.out.println("[HoaDonDAO] " + action + " mat " + (System.currentTimeMillis() - start) + " ms");
    }
}
