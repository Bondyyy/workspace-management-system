package com.wms.view.ThanhToanTrucTiep;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class ThanhToanTienMatForm extends JDialog {

    // === BẢNG MÀU ===
    private final Color mauNenChinh    = Color.decode("#FAFAFA");
    private final Color mauHongChinh   = Color.decode("#D81B60");
    private final Color mauHongNhat    = Color.decode("#FCE4EC");
    private final Color mauXamDam      = Color.decode("#212529");
    private final Color mauXamNhat     = Color.decode("#757575");
    private final Color mauXanhLa      = Color.decode("#2E7D32");
    private final Color mauDo          = Color.decode("#C62828");

    private JTextField txtSoTienKhachDua;
    private JLabel     lblTienThua;
    private JButton    btnXacNhan;

    private double tongTienHoaDon;
    private boolean daThanhToan = false;

    private final NumberFormat  formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DecimalFormat formatSo   = new DecimalFormat("#,###");

    // Constructor chuẩn — nhận Frame cha (có thể null) và boolean modal
    public ThanhToanTienMatForm(Frame parent, boolean modal, double tongTien) {
        super(parent, "Thanh Toán Tiền Mặt", modal);
        this.tongTienHoaDon = tongTien;
        khoiTaoGiaoDien();
        setLocationRelativeTo(parent);
    }

    // Constructor phụ cho tương thích cũ (test main)
    public ThanhToanTienMatForm(JFrame parent, double tongTien) {
        this(parent, true, tongTien);
    }

    // =========================================================
    private void khoiTaoGiaoDien() {
        setSize(560, 720);
        setMinimumSize(new Dimension(560, 720));
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(mauNenChinh);
        main.setBorder(new EmptyBorder(28, 30, 20, 30));

        // ── HEADER ──────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(mauNenChinh);

        JLabel lblTieuDe = new JLabel("THANH TOÁN TIỀN MẶT", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTieuDe.setForeground(mauHongChinh);
        lblTieuDe.setAlignmentX(CENTER_ALIGNMENT);
        header.add(lblTieuDe);

        JLabel lblMoTa = new JLabel("Nhập số tiền khách hàng đưa", SwingConstants.CENTER);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMoTa.setForeground(mauXamNhat);
        lblMoTa.setAlignmentX(CENTER_ALIGNMENT);
        lblMoTa.setBorder(new EmptyBorder(6, 0, 0, 0));
        header.add(lblMoTa);

        main.add(header, BorderLayout.NORTH);

        // ── CONTENT ─────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(22, 22, 22, 22)));

        // Tổng tiền
        content.add(taoHangInfo("Tổng tiền hóa đơn:", formatTien.format(tongTienHoaDon), mauHongChinh, 17));
        content.add(separator());
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Input tiền khách
        JLabel lblNhap = new JLabel("Số tiền khách đưa:");
        lblNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNhap.setForeground(mauXamDam);
        lblNhap.setAlignmentX(LEFT_ALIGNMENT);
        content.add(lblNhap);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        txtSoTienKhachDua = new JTextField();
        txtSoTienKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtSoTienKhachDua.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(10, 14, 10, 14)));
        txtSoTienKhachDua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        txtSoTienKhachDua.setAlignmentX(LEFT_ALIGNMENT);
        txtSoTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { tinhTienThua(); }
            public void removeUpdate(DocumentEvent e)  { tinhTienThua(); }
            public void insertUpdate(DocumentEvent e)  { tinhTienThua(); }
        });
        content.add(txtSoTienKhachDua);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Gợi ý mệnh giá
        JLabel lblGoiY = new JLabel("Gợi ý số tiền:");
        lblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGoiY.setForeground(mauXamNhat);
        lblGoiY.setAlignmentX(LEFT_ALIGNMENT);
        content.add(lblGoiY);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel gridGoiY = new JPanel(new GridLayout(2, 3, 10, 10));
        gridGoiY.setBackground(Color.WHITE);
        gridGoiY.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        gridGoiY.setAlignmentX(LEFT_ALIGNMENT);

        double[] menhGias = {
            tongTienHoaDon,
            Math.ceil(tongTienHoaDon / 10000) * 10000,
            Math.ceil(tongTienHoaDon / 100000) * 100000,
            Math.ceil(tongTienHoaDon / 1000000) * 1000000,
        };
        for (double mg : menhGias) {
            if (mg >= tongTienHoaDon) gridGoiY.add(taoNutMenhGia(mg));
        }
        content.add(gridGoiY);
        content.add(Box.createRigidArea(new Dimension(0, 16)));
        content.add(separator());
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Tiền thừa
        JPanel pnThua = new JPanel(new BorderLayout());
        pnThua.setBackground(mauHongNhat);
        pnThua.setBorder(new EmptyBorder(14, 16, 14, 16));
        pnThua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        pnThua.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("Tiền thừa trả khách:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(mauXamDam);
        pnThua.add(lbl, BorderLayout.WEST);

        lblTienThua = new JLabel("0 ₫");
        lblTienThua.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblTienThua.setForeground(mauXanhLa);
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        pnThua.add(lblTienThua, BorderLayout.CENTER);

        content.add(pnThua);

        main.add(content, BorderLayout.CENTER);

        // ── FOOTER ──────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setBackground(mauNenChinh);
        footer.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btnHuy = taoNut("HỦY", new Color(238, 238, 238), mauXamNhat, 110, 44);
        btnHuy.addActionListener(e -> { daThanhToan = false; dispose(); });
        footer.add(btnHuy);

        btnXacNhan = taoNut("XÁC NHẬN", mauHongChinh, Color.WHITE, 150, 44);
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(e -> xacNhanThanhToan());
        footer.add(btnXacNhan);

        main.add(footer, BorderLayout.SOUTH);
        add(main);
    }

    // ── Helpers ─────────────────────────────────────────────
    private JPanel taoHangInfo(String lbl, String val, Color mauVal, int fs) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(mauXamNhat);
        p.add(l, BorderLayout.WEST);
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, fs));
        v.setForeground(mauVal);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JPanel separator() {
        JPanel s = new JPanel();
        s.setBackground(new Color(235, 235, 235));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        s.setPreferredSize(new Dimension(1, 1));
        s.setAlignmentX(LEFT_ALIGNMENT);
        return s;
    }

    private JButton taoNut(String text, Color bg, Color fg, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? bg : new Color(189, 189, 189));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton taoNutMenhGia(double mg) {
        JButton btn = new JButton(formatSo.format(mg) + " đ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? mauHongChinh : mauHongNhat);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(mauHongChinh);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(mauHongChinh);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> txtSoTienKhachDua.setText(String.valueOf((long) mg)));
        return btn;
    }

    private void tinhTienThua() {
        String text = txtSoTienKhachDua.getText().trim().replaceAll("[^0-9]", "");
        if (text.isEmpty()) {
            lblTienThua.setText("0 ₫"); lblTienThua.setForeground(mauXanhLa);
            btnXacNhan.setEnabled(false); return;
        }
        double khachDua = Double.parseDouble(text);
        double thua     = khachDua - tongTienHoaDon;
        if (thua >= 0) {
            lblTienThua.setText(formatTien.format(thua));
            lblTienThua.setForeground(mauXanhLa);
            btnXacNhan.setEnabled(true);
        } else {
            lblTienThua.setText("Thiếu " + formatTien.format(Math.abs(thua)));
            lblTienThua.setForeground(mauDo);
            btnXacNhan.setEnabled(false);
        }
    }

    private void xacNhanThanhToan() {
        String text = txtSoTienKhachDua.getText().trim().replaceAll("[^0-9]", "");
        try {
            if (Double.parseDouble(text) >= tongTienHoaDon) { daThanhToan = true; dispose(); }
        } catch (NumberFormatException ignored) {}
    }

    public boolean isDaThanhToan() { return daThanhToan; }

    // === MAIN TEST ===
    public static void main(String[] args) {
        JFrame f = new JFrame(); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 600); f.setLocationRelativeTo(null); f.setVisible(true);
        ThanhToanTienMatForm d = new ThanhToanTienMatForm(f, 775000);
        d.setVisible(true);
        System.out.println("Đã thanh toán: " + d.isDaThanhToan()); System.exit(0);
    }
}