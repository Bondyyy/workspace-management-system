import java.sql.*;
import com.wms.config.DatabaseConnection;

public class DebugDB {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT nd.MaND, nd.Email, nd.SDT, nv.MaNV FROM NGUOIDUNG nd JOIN NHANVIEN nv ON nd.MaND = nv.MaND WHERE nv.MaNV = 'NV000001'";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("MaNV: " + rs.getString("MaNV"));
                    System.out.println("MaND: [" + rs.getString("MaND") + "]");
                    System.out.println("Email: [" + rs.getString("Email") + "]");
                    System.out.println("SDT: [" + rs.getString("SDT") + "]");
                }
            }
            
            sql = "SELECT MaND, HoTen, Email, SDT FROM NGUOIDUNG WHERE Email = 'nhanvien1@gmail.com' OR SDT = '0123123120'";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                System.out.println("\nDuplicate Check Results:");
                while (rs.next()) {
                    System.out.println("Found MaND: [" + rs.getString("MaND") + "] Name: " + rs.getString("HoTen"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
