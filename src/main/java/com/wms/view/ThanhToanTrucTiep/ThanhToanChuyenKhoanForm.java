package com.wms.view.ThanhToanTrucTiep;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

public class ThanhToanChuyenKhoanForm extends JDialog {

    // === BẢNG MÀU ===
    private final Color mauNenChinh  = Color.decode("#FAFAFA");
    private final Color mauHongChinh = Color.decode("#D81B60");
    private final Color mauHongNhat  = Color.decode("#FCE4EC");
    private final Color mauXamDam    = Color.decode("#212529");
    private final Color mauXamNhat   = Color.decode("#757575");
    private final Color mauXanhDuong = Color.decode("#1565C0");

    private double  tongTienHoaDon;
    private String  maHoaDon;
    private boolean daThanhToan = false;

    private final NumberFormat formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // Constructor chuẩn — nhận Frame cha, boolean modal, tổng tiền, mã hóa đơn
    public ThanhToanChuyenKhoanForm(Frame parent, boolean modal, double tongTien, String maHoaDon) {
        super(parent, "Thanh Toán Chuyển Khoản", modal);
        this.tongTienHoaDon = tongTien;
        this.maHoaDon = maHoaDon;
        khoiTaoGiaoDien();
        setLocationRelativeTo(parent);
    }

    // Constructor phụ cho tương thích cũ (test main)
    public ThanhToanChuyenKhoanForm(JFrame parent, double tongTien) {
        this(parent, true, tongTien, "HD-TEST");
    }

    // =========================================================
    private void khoiTaoGiaoDien() {
        setSize(600, 750);
        setMinimumSize(new Dimension(600, 750));
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(mauNenChinh);
        main.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ── HEADER ──────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(mauNenChinh);

        JLabel lblTieuDe = new JLabel("CHUYỂN KHOẢN NGÂN HÀNG", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTieuDe.setForeground(mauHongChinh);
        lblTieuDe.setAlignmentX(CENTER_ALIGNMENT);
        header.add(lblTieuDe);

        JLabel lblSub = new JLabel("Quét mã QR để thanh toán", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(mauXamNhat);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);
        lblSub.setBorder(new EmptyBorder(6, 0, 0, 0));
        header.add(lblSub);

        main.add(header, BorderLayout.NORTH);

        // ── CONTENT ─────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(18, 18, 18, 18)));

        // Tổng tiền
        JPanel pnTong = new JPanel(new BorderLayout());
        pnTong.setBackground(mauHongNhat);
        pnTong.setBorder(new EmptyBorder(12, 15, 12, 15));
        pnTong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        pnTong.setAlignmentX(CENTER_ALIGNMENT);
        JLabel lblTxt = new JLabel("Số tiền thanh toán:");
        lblTxt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTxt.setForeground(mauXamDam);
        pnTong.add(lblTxt, BorderLayout.WEST);
        JLabel lblSoTien = new JLabel(formatTien.format(tongTienHoaDon));
        lblSoTien.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblSoTien.setForeground(mauHongChinh);
        lblSoTien.setHorizontalAlignment(SwingConstants.RIGHT);
        pnTong.add(lblSoTien, BorderLayout.CENTER);
        content.add(pnTong);
        content.add(Box.createRigidArea(new Dimension(0, 15)));

        // QR CODE — căn giữa
        JPanel wrapQR = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapQR.setBackground(Color.WHITE);
        wrapQR.setAlignmentX(CENTER_ALIGNMENT);
        wrapQR.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        wrapQR.add(taoKhungQR());
        content.add(wrapQR);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel lblHD = new JLabel("Quét mã QR bằng ứng dụng ngân hàng", SwingConstants.CENTER);
        lblHD.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHD.setForeground(mauXamNhat);
        lblHD.setAlignmentX(CENTER_ALIGNMENT);
        lblHD.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        content.add(lblHD);
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        // Thông tin chuyển khoản — căn giữa
        JPanel wrapInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapInfo.setBackground(Color.WHITE);
        wrapInfo.setAlignmentX(CENTER_ALIGNMENT);
        wrapInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JPanel pnInfo = new JPanel();
        pnInfo.setLayout(new BoxLayout(pnInfo, BoxLayout.Y_AXIS));
        pnInfo.setBackground(Color.decode("#E3F2FD"));
        pnInfo.setBorder(new EmptyBorder(14, 32, 14, 32));
        pnInfo.setPreferredSize(new Dimension(540, 180));

        String noiDung = "UIT CW " + (maHoaDon != null ? maHoaDon : System.currentTimeMillis() % 100000);
        String[][] rows = {
            {"Ngân hàng:",      "VPBank"},
            {"Số tài khoản:",   "1234567890"},
            {"Chủ tài khoản:",  "UIT COWORKING SPACE"},
            {"Nội dung CK:",    noiDung}
        };
        for (int i = 0; i < rows.length; i++) {
            pnInfo.add(taoHangCK(rows[i][0], rows[i][1]));
            if (i < rows.length - 1) pnInfo.add(Box.createRigidArea(new Dimension(0, 7)));
        }

        wrapInfo.add(pnInfo);
        content.add(wrapInfo);

        main.add(content, BorderLayout.CENTER);

        // ── FOOTER ──────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setBackground(mauNenChinh);
        footer.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnHuy = taoNut("HỦY", new Color(238, 238, 238), mauXamNhat, 110, 45);
        btnHuy.addActionListener(e -> { daThanhToan = false; dispose(); });
        footer.add(btnHuy);

        JButton btnXN = taoNut("XÁC NHẬN", mauXanhDuong, Color.WHITE, 240, 45);
        btnXN.addActionListener(e -> { daThanhToan = true; dispose(); });
        footer.add(btnXN);

        main.add(footer, BorderLayout.SOUTH);
        add(main);
    }

    // ── Helpers ─────────────────────────────────────────────
    private JPanel taoKhungQR() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 220, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 10, 10);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(270, 270));
        panel.setMaximumSize(new Dimension(270, 270));
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        JPanel qrGrid = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                int cell = 8, grid = 25;
                int ox = (getWidth()  - grid * cell) / 2;
                int oy = (getHeight() - grid * cell) / 2;
                boolean[][] pat = buildQR(grid);
                g2.setColor(Color.BLACK);
                for (int r = 0; r < grid; r++)
                    for (int c = 0; c < grid; c++)
                        if (pat[r][c]) g2.fillRect(ox + c * cell, oy + r * cell, cell, cell);
                g2.dispose();
            }
        };
        qrGrid.setBackground(Color.WHITE);
        qrGrid.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(qrGrid, BorderLayout.CENTER);
        return panel;
    }

    private boolean[][] buildQR(int n) {
        boolean[][] p = new boolean[n][n];
        marker(p, 0, 0); marker(p, 0, n - 7); marker(p, n - 7, 0);
        for (int i = 8; i < n - 8; i++) { p[6][i] = i % 2 == 0; p[i][6] = i % 2 == 0; }
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            if (inMarker(i, j, n) || i == 6 || j == 6) continue;
            p[i][j] = (i * 31 + j * 17) % 3 == 0;
        }
        return p;
    }

    private void marker(boolean[][] p, int r, int c) {
        for (int i = 0; i < 7; i++) {
            if (r + i < p.length) { p[r+i][c] = true; p[r+i][c+6] = true; }
            if (c + i < p[0].length) { p[r][c+i] = true; p[r+6][c+i] = true; }
        }
        for (int i = 2; i < 5; i++) for (int j = 2; j < 5; j++) p[r+i][c+j] = true;
    }

    private boolean inMarker(int i, int j, int n) {
        return (i < 8 && j < 8) || (i < 8 && j >= n-8) || (i >= n-8 && j < 8);
    }

    private JPanel taoHangCK(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(Color.decode("#E3F2FD"));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(mauXamNhat);
        p.add(l, BorderLayout.WEST);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(mauXamDam);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JButton taoNut(String text, Color bg, Color fg, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public boolean isDaThanhToan() { return daThanhToan; }

    // === MAIN TEST ===
    public static void main(String[] args) {
        JFrame f = new JFrame(); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 600); f.setLocationRelativeTo(null); f.setVisible(true);
        ThanhToanChuyenKhoanForm d = new ThanhToanChuyenKhoanForm(f, 775000);
        d.setVisible(true);
        System.out.println("Đã thanh toán: " + d.isDaThanhToan()); System.exit(0);
    }
}