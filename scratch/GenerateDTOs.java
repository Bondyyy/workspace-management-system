package scratch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateDTOs {
    public static void main(String[] args) {
        String baseDir = "d:/Project/workspace-management-system/src/main/java/com/wms/model";
        
        String[][] tables = {
            {"CoSoVatChat", "ChiNhanh", "String maCN; String tenCN; String thoiGianMoCua; String thoiGianDongCua; String duongDayNong; String trangThai; String diaChi"},
            {"CoSoVatChat", "KhongGian", "String maKG; String tenKG; String trangThaiKG; String viTri; String maLoaiKG; String maCN"},
            {"CoSoVatChat", "LoaiKhongGian", "String maLoaiKG; String tenLoaiKG; Integer sucChua; Double donGiaTheoGio"},
            
            {"NhanSu_KhachHang", "HangThanhVien", "String maHangThanhVien; String tenHangThanhVien; Double phanTramTienGiam; Double tongChiTieuToiThieu"},
            {"NhanSu_KhachHang", "KhachHang", "String maKH; String hoTenKH; String loaiKH; Double tongChiTieu; java.sql.Timestamp capNhatLanCuoi; String maHangThanhVien; String maND"},
            {"NhanSu_KhachHang", "NguoiDung", "String maND; String tenTaiKhoan; String matKhauMaHoa; String anhDaiDien; String gioiTinh; String email; String sdt; java.sql.Date ngaySinh; java.sql.Timestamp thoiGianTao; java.sql.Timestamp capNhatLanCuoi; java.sql.Timestamp lanCuoiDangNhap; String trangThaiND"},
            {"NhanSu_KhachHang", "NhanVien", "String maNV; String loaiNV; java.sql.Date ngayVaoLam; String trangThaiLamViec; Double phuCap; Double tienThuong; String caLamViec; Double luongCoBan; String maNQL; String maCN; String maND"},
            
            {"PhanQuyen_BaoMat", "ChiTietChucNang", "String maNhomChucNang; String maChucNang"},
            {"PhanQuyen_BaoMat", "ChiTietNhomChucNang", "String maVaiTro; String maNhomChucNang"},
            {"PhanQuyen_BaoMat", "ChiTietVaiTro", "String maND; String maVaiTro"},
            {"PhanQuyen_BaoMat", "ChucNang", "String maChucNang; String tenChucNang; String moTa"},
            {"PhanQuyen_BaoMat", "NhomChucNang", "String maNhomChucNang; String tenNhomChucNang; String moTa"},
            {"PhanQuyen_BaoMat", "VaiTro", "String maVaiTro; String tenVaiTro; String moTa"},
            
            {"ThanhToan_KhuyenMai", "HoaDon", "String maHoaDon; String soHD; Double tongTien; Double thanhTien; java.sql.Timestamp ngayLapHoaDon; String phuongThucThanhToan; String trangThaiThanhToan; String maPhien; String maPGG; String maNV"},
            {"ThanhToan_KhuyenMai", "PhieuGiamGia", "String maPGG; String maChuSoPGG; Double giaTriGiamGia; Double giaTriApDungToiThieu; java.sql.Timestamp ngayBatDauApDung; java.sql.Timestamp ngayKetThucApDung; Integer sLDaDung; Integer sLToiDa; java.sql.Timestamp ngayTaoPGG; String maNV"},
            
            {"VanHanh_DichVu", "ChiTietDichVu", "String maDV; String maPhien; Integer soLuong; String ghiChu"},
            {"VanHanh_DichVu", "DatCho", "String maDatCho; java.sql.Timestamp thoiGianDat; java.sql.Timestamp thoiGianDuKienToi; Integer khoangThoiGianSuDung; String trangThaiDatTruoc; Double thanhTien; String ghiChu; String maQR; java.sql.Timestamp capNhatLanCuoi; String maKH; String maKG"},
            {"VanHanh_DichVu", "DichVu", "String maDV; String tenDV; String hinhAnh; String trangThaiDV; Double donGia; String maLoaiDV"},
            {"VanHanh_DichVu", "LoaiDichVu", "String maLoaiDV; String tenLoaiDV; String trangThaiLDV"},
            {"VanHanh_DichVu", "PhienLamViec", "String maPhien; java.sql.Timestamp thoiGianBatDau; java.sql.Timestamp thoiGianDuKienKetThuc; String trangThaiPhien; java.sql.Timestamp thoiGianKetThuc; java.sql.Timestamp capNhatLanCuoi; String maKG; String maKH; String maDatCho"}
        };
        
        for (String[] t : tables) {
            String pkgName = t[0];
            String className = t[1] + "DTO";
            String fieldsStr = t[2];
            
            File dir = new File(baseDir + "/" + pkgName);
            if (!dir.exists()) dir.mkdirs();
            
            File file = new File(dir, className + ".java");
            try (FileWriter fw = new FileWriter(file)) {
                StringBuilder sb = new StringBuilder();
                sb.append("package com.wms.model.").append(pkgName).append(";\n\n");
                sb.append("public class ").append(className).append(" {\n");
                
                String[] fields = fieldsStr.split("; ");
                for (String f : fields) {
                    sb.append("    private ").append(f).append(";\n");
                }
                
                sb.append("\n    public ").append(className).append("() {}\n\n");
                
                for (String f : fields) {
                    String[] parts = f.split(" ");
                    String type = parts[0];
                    String name = parts[1];
                    String capName = name.substring(0, 1).toUpperCase() + name.substring(1);
                    
                    sb.append("    public ").append(type).append(" get").append(capName).append("() {\n");
                    sb.append("        return ").append(name).append(";\n");
                    sb.append("    }\n\n");
                    sb.append("    public void set").append(capName).append("(").append(type).append(" ").append(name).append(") {\n");
                    sb.append("        this.").append(name).append(" = ").append(name).append(";\n");
                    sb.append("    }\n\n");
                }
                sb.append("}\n");
                fw.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
