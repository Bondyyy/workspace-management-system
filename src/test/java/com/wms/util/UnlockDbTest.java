package com.wms.util;

import com.wms.config.DatabaseConnection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UnlockDbTest {

    @Test
    @Tag("manual")
    public void inspectAndUnlockDatabase() {
        System.out.println("=== BAT DAU KIEM TRA VA GIAI PHONG KHOA CSDL ===");

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn == null) {
                System.err.println("[-] Khong the ket noi den CSDL. Vui long kiem tra file db.properties hoac vi tri Wallet.");
                return;
            }
            System.out.println("[+] Ket noi CSDL thanh cong. Dang thuc hien kiem tra phien hoat dong va khoa...");

            // 1. Liet ke cac khoa dang co tren bang
            System.out.println("\n--- LICHSU / DANG KHOA CO DINH TREN CAC BANG ---");
            String lockSql = 
                "SELECT s.sid, s.serial#, s.status, s.username, s.osuser, s.program, s.machine, " +
                "o.object_name, lo.locked_mode " +
                "FROM v$locked_object lo " +
                "JOIN dba_objects o ON lo.object_id = o.object_id " +
                "JOIN v$session s ON lo.session_id = s.sid";
            
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(lockSql)) {
                boolean hasLocks = false;
                while (rs.next()) {
                    hasLocks = true;
                    int sid = rs.getInt("sid");
                    int serial = rs.getInt("serial#");
                    String status = rs.getString("status");
                    String username = rs.getString("username");
                    String osuser = rs.getString("osuser");
                    String program = rs.getString("program");
                    String machine = rs.getString("machine");
                    String objectName = rs.getString("object_name");
                    int mode = rs.getInt("locked_mode");
                    
                    System.out.printf("  -> LOCK PHAT HIEN: Bang '%s' bi khoa boi Session SID=%d, SERIAL#=%d (Trang thai: %s, User: %s, OS User: %s, Chuong trinh: %s, May: %s, Lock Mode: %d)\n",
                            objectName, sid, serial, status, username, osuser, program, machine, mode);
                }
                if (!hasLocks) {
                    System.out.println("  [+] Khong tim thay khoa nao dang treo tren cac doi tuong DB.");
                }
            } catch (Exception e) {
                System.err.println("[-] Khong the truy van v$locked_object: " + e.getMessage());
            }

            // 2. Tim cac phien blocking (dang chan phien khac)
            System.out.println("\n--- CAC PHIEN DANG CHAN (BLOCKING) NHAU ---");
            String blockSql = 
                "SELECT sid, serial#, username, osuser, program, machine, status, " +
                "blocking_session, blocking_session_status, event, seconds_in_wait " +
                "FROM v$session " +
                "WHERE blocking_session IS NOT NULL OR sid IN (SELECT blocking_session FROM v$session WHERE blocking_session IS NOT NULL)";
            
            List<String> sessionsToKill = new ArrayList<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(blockSql)) {
                boolean hasBlockers = false;
                while (rs.next()) {
                    hasBlockers = true;
                    int sid = rs.getInt("sid");
                    int serial = rs.getInt("serial#");
                    String username = rs.getString("username");
                    String osuser = rs.getString("osuser");
                    String program = rs.getString("program");
                    String machine = rs.getString("machine");
                    String status = rs.getString("status");
                    int blockingSid = rs.getInt("blocking_session");
                    String blockStatus = rs.getString("blocking_session_status");
                    String event = rs.getString("event");
                    int seconds = rs.getInt("seconds_in_wait");
                    
                    System.out.printf("  -> SESSION INFO: SID=%d, SERIAL#=%d (User: %s, OS User: %s, May: %s, Trang thai: %s, Su kien: %s, Cho: %d giay, Blocking SID: %d [%s])\n",
                            sid, serial, username, osuser, machine, status, event, seconds, blockingSid, blockStatus);
                    
                    // Neu day la session dang block session khac, hoac session bi treo khac
                    if (blockingSid == 0 && "ACTIVE".equals(status)) {
                        // Day la session goc re gay ra block!
                        sessionsToKill.add(sid + "," + serial);
                    }
                }
                if (!hasBlockers) {
                    System.out.println("  [+] Khong tim thay cap phien chan nhau nao.");
                }
            } catch (Exception e) {
                System.err.println("[-] Khong the truy van v$session blocking: " + e.getMessage());
            }

            // 3. Phien lam viec cu cua WMS hoac NetBeans (ke ca INACTIVE ma van giu khoa)
            System.out.println("\n--- QUET CAC PHIEN DU THUA TU CAC LAN CHAY TRUOC ---");
            String oldSessionsSql = 
                "SELECT s.sid, s.serial#, s.status, s.username, s.osuser, s.program, s.machine " +
                "FROM v$session s " +
                "WHERE s.username = 'ADMIN' " +
                "  AND s.program LIKE '%wms%' OR s.program LIKE '%NetBeans%' OR s.program LIKE '%JDBC%'";
            
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(oldSessionsSql)) {
                while (rs.next()) {
                    int sid = rs.getInt("sid");
                    int serial = rs.getInt("serial#");
                    String status = rs.getString("status");
                    String program = rs.getString("program");
                    String machine = rs.getString("machine");
                    
                    // Tranh tu diet phien hien tai
                    // Tim SID hien tai bang cach chay truy van rieng hoac so sanh machine
                    System.out.printf("  -> PHAT HIEN PHIEN WMS/NETBEANS: SID=%d, SERIAL#=%d, Trang thai: %s, Chuong trinh: %s, May: %s\n",
                            sid, serial, status, program, machine);
                }
            } catch (Exception e) {
                System.err.println("[-] Khong the quet phien cu: " + e.getMessage());
            }

            // 4. Tu dong giai phong bang cach diet cac session dang lock cac bang CHUCNANG/CHITIETCHUCNANG
            System.out.println("\n--- TIEN HANH GIAI PHONG KHOA NEU CO ---");
            String lockSessionsSql = 
                "SELECT DISTINCT s.sid, s.serial#, s.status, o.object_name " +
                "FROM v$locked_object lo " +
                "JOIN dba_objects o ON lo.object_id = o.object_id " +
                "JOIN v$session s ON lo.session_id = s.sid " +
                "WHERE o.object_name IN ('CHUCNANG', 'CHITIETCHUCNANG', 'HANGTHANHVIEN', 'VAITRO', 'NHOMCHUCNANG', 'CHITIETNHOMCHUCNANG')";
            
            List<String> targets = new ArrayList<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(lockSessionsSql)) {
                while (rs.next()) {
                    int sid = rs.getInt("sid");
                    int serial = rs.getInt("serial#");
                    String obj = rs.getString("object_name");
                    String status = rs.getString("status");
                    System.out.printf("  [!] Xac dinh Session can giai phong: SID=%d, SERIAL#=%d dang khoa bang '%s' (Trang thai: %s)\n",
                            sid, serial, obj, status);
                    String target = sid + "," + serial;
                    if (!targets.contains(target)) {
                        targets.add(target);
                    }
                }
            } catch (Exception e) {
                System.err.println("[-] Loi khi tim session dang khoa bang: " + e.getMessage());
            }

            // Ghep them vao targets danh sach sessionsToKill
            for (String s : sessionsToKill) {
                if (!targets.contains(s)) {
                    targets.add(s);
                }
            }

            // Tien hanh diet
            if (targets.isEmpty()) {
                System.out.println("[+] Khong co session nao can phai kill. CSDL hoan toan khoe manh.");
            } else {
                System.out.printf("[!] Tien hanh kill %d session gay khoa...\n", targets.size());
                for (String session : targets) {
                    String killSql = String.format("ALTER SYSTEM KILL SESSION '%s' IMMEDIATE", session);
                    try (Statement st = conn.createStatement()) {
                        st.execute(killSql);
                        System.out.printf("  [+] Da gui yeu cau KILL Session '%s' thanh cong.\n", session);
                    } catch (Exception e) {
                        System.err.printf("  [-] Khong the KILL Session '%s': %s (Co the session da tu ket thuc)\n", session, e.getMessage());
                    }
                }
            }
            
            System.out.println("\n=== HOAN TAT QUAT TRINH GIAI PHONG KHOA ===");

        } catch (Exception e) {
            System.err.println("[-] Loi khi ket noi hoac thuc thi giai phong khoa: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
