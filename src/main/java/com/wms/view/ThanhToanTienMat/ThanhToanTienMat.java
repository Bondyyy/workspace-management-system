package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ThanhToanTienMat extends JDialog {

    // === BẢNG MÀU ===
    private final Color mauNenChinh = Color.decode("#FAFAFA");
    private final Color mauHongChinh = Color.decode("#D81B60");
    private final Color mauHongNhat = Color.decode("#FCE4EC");
    private final Color mauXamDam = Color.decode("#212529");
    private final Color mauXamNhat = Color.decode("#757575");
    private final Color mauXanhLa = Color.decode("#4CAF50");
    
    private JTextField txtSoTienKhachDua;
    private JLabel lblTienThua;
    private JButton btnXacNhan;
    
    private double tongTienHoaDon;
    private boolean daThanhToan = false;
    
    private NumberFormat formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private DecimalFormat formatSo = new DecimalFormat("#,###");

    public ThanhToanTienMat(JFrame parent, double tongTien) {
        super(parent, "Thanh Toán Tiền Mặt", true);
        this.tongTienHoaDon = tongTien;
        khoiTaoGiaoDien();
        setLocationRelativeTo(parent);
    }

    private void khoiTaoGiaoDien() {
        this.setSize(550, 500);
        this.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 25));
        mainPanel.setBackground(mauNenChinh);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // === HEADER ===
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
        panelHeader.setBackground(mauNenChinh);
        
        JLabel lblIcon = new JLabel("💵", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 56));
        lblIcon.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panelHeader.add(lblIcon);
        
        JLabel lblTieuDe = new JLabel("THANH TOÁN TIỀN MẶT", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTieuDe.setForeground(mauHongChinh);
        lblTieuDe.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        lblTieuDe.setBorder(new EmptyBorder(15, 0, 0, 0));
        panelHeader.add(lblTieuDe);
        
        mainPanel.add(panelHeader, BorderLayout.NORTH);

        // === CONTENT ===
        JPanel panelContent = new JPanel();
        panelContent.setLayout(new BoxLayout(panelContent, BoxLayout.Y_AXIS));
        panelContent.setBackground(Color.WHITE);
        panelContent.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));

        // Tổng tiền hóa đơn
        JPanel panelTongTien = taoHangThongTin("Tổng tiền hóa đơn:", formatTien.format(tongTienHoaDon), mauHongChinh, 18);
        panelContent.add(panelTongTien);
        panelContent.add(taoKhoangCach(20));

        // Đường phân cách
        panelContent.add(taoDuongPhanCach());
        panelContent.add(taoKhoangCach(20));

        // Số tiền khách đưa
        JLabel lblNhap = new JLabel("Số tiền khách đưa:");
        lblNhap.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNhap.setForeground(mauXamDam);
        lblNhap.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelContent.add(lblNhap);
        panelContent.add(taoKhoangCach(10));

        txtSoTienKhachDua = new JTextField();
        txtSoTienKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtSoTienKhachDua.setForeground(mauXamDam);
        txtSoTienKhachDua.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            new EmptyBorder(12, 15, 12, 15)
        ));
        txtSoTienKhachDua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        txtSoTienKhachDua.setAlignmentX(JTextField.LEFT_ALIGNMENT);
        
        // Listener để tính tiền thừa
        txtSoTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { tinhTienThua(); }
            public void removeUpdate(DocumentEvent e) { tinhTienThua(); }
            public void insertUpdate(DocumentEvent e) { tinhTienThua(); }
        });
        
        panelContent.add(txtSoTienKhachDua);
        panelContent.add(taoKhoangCach(20));

        // Gợi ý số tiền nhanh
        JPanel panelGoiY = new JPanel(new GridLayout(2, 3, 10, 10));
        panelGoiY.setBackground(Color.WHITE);
        panelGoiY.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        panelGoiY.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        double[] cacMenhGia = {
            Math.ceil(tongTienHoaDon / 100000) * 100000, // Làm tròn lên hàng trăm nghìn
            Math.ceil(tongTienHoaDon / 200000) * 200000, // Làm tròn lên 200k
            Math.ceil(tongTienHoaDon / 500000) * 500000, // Làm tròn lên 500k
            500000, 
            1000000,
            2000000
        };
        
        for (double menhGia : cacMenhGia) {
            if (menhGia >= tongTienHoaDon) {
                JButton btnMenhGia = taoNutMenhGia(menhGia);
                panelGoiY.add(btnMenhGia);
            }
        }
        
        panelContent.add(panelGoiY);
        panelContent.add(taoKhoangCach(20));

        // Đường phân cách
        panelContent.add(taoDuongPhanCach());
        panelContent.add(taoKhoangCach(20));

        // Tiền thừa
        JPanel panelTienThua = new JPanel(new BorderLayout());
        panelTienThua.setBackground(mauHongNhat);
        panelTienThua.setBorder(new EmptyBorder(15, 15, 15, 15));
        panelTienThua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panelTienThua.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        JLabel lblTextTienThua = new JLabel("Tiền thừa trả khách:");
        lblTextTienThua.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTextTienThua.setForeground(mauXamDam);
        panelTienThua.add(lblTextTienThua, BorderLayout.WEST);
        
        lblTienThua = new JLabel("0 ₫");
        lblTienThua.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTienThua.setForeground(mauXanhLa);
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        panelTienThua.add(lblTienThua, BorderLayout.CENTER);
        
        panelContent.add(panelTienThua);

        mainPanel.add(panelContent, BorderLayout.CENTER);

        // === FOOTER - Buttons ===
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelFooter.setBackground(mauNenChinh);
        
        JButton btnHuy = taoNutHuy();
        btnHuy.addActionListener(e -> {
            daThanhToan = false;
            dispose();
        });
        panelFooter.add(btnHuy);
        
        btnXacNhan = taoNutXacNhan();
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(e -> xacNhanThanhToan());
        panelFooter.add(btnXacNhan);
        
        mainPanel.add(panelFooter, BorderLayout.SOUTH);

        this.add(mainPanel);
    }

    private JPanel taoHangThongTin(String label, String value, Color mauValue, int fontSize) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblLabel.setForeground(mauXamNhat);
        panel.add(lblLabel, BorderLayout.WEST);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        lblValue.setForeground(mauValue);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblValue, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton taoNutMenhGia(double menhGia) {
        JButton btn = new JButton(formatSo.format(menhGia) + " đ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(mauHongChinh);
                } else {
                    g2.setColor(mauHongNhat);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.setColor(mauHongChinh);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(mauHongChinh);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            txtSoTienKhachDua.setText(String.valueOf((long)menhGia));
        });
        
        return btn;
    }

    private JButton taoNutXacNhan() {
        JButton btn = new JButton("XÁC NHẬN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isEnabled()) {
                    g2.setColor(mauHongChinh);
                } else {
                    g2.setColor(new Color(189, 189, 189));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(150, 45));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    private JButton taoNutHuy() {
        JButton btn = new JButton("HỦY") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(mauXamNhat);
        btn.setPreferredSize(new Dimension(100, 45));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    private JPanel taoDuongPhanCach() {
        JPanel sep = new JPanel();
        sep.setBackground(new Color(230, 230, 230));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        return sep;
    }

    private JPanel taoKhoangCach(int height) {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(0, height));
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        spacer.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        return spacer;
    }

    private void tinhTienThua() {
        try {
            String text = txtSoTienKhachDua.getText().trim().replaceAll("[^0-9]", "");
            if (text.isEmpty()) {
                lblTienThua.setText("0 ₫");
                btnXacNhan.setEnabled(false);
                return;
            }
            
            double tienKhachDua = Double.parseDouble(text);
            double tienThua = tienKhachDua - tongTienHoaDon;
            
            if (tienThua >= 0) {
                lblTienThua.setText(formatTien.format(tienThua));
                lblTienThua.setForeground(mauXanhLa);
                btnXacNhan.setEnabled(true);
            } else {
                lblTienThua.setText("Thiếu " + formatTien.format(Math.abs(tienThua)));
                lblTienThua.setForeground(Color.decode("#F44336"));
                btnXacNhan.setEnabled(false);
            }
        } catch (NumberFormatException ex) {
            lblTienThua.setText("0 ₫");
            lblTienThua.setForeground(mauXanhLa);
            btnXacNhan.setEnabled(false);
        }
    }

    private void xacNhanThanhToan() {
        try {
            String text = txtSoTienKhachDua.getText().trim().replaceAll("[^0-9]", "");
            double tienKhachDua = Double.parseDouble(text);
            
            if (tienKhachDua >= tongTienHoaDon) {
                daThanhToan = true;
                dispose();
            }
        } catch (NumberFormatException ex) {
            // Không làm gì
        }
    }

    public boolean isDaThanhToan() {
        return daThanhToan;
    }

    // === MAIN TEST ===
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        ThanhToanTienMat dialog = new ThanhToanTienMat(frame, 725000);
        dialog.setVisible(true);
        
        System.out.println("Đã thanh toán: " + dialog.isDaThanhToan());
        System.exit(0);
    }
}