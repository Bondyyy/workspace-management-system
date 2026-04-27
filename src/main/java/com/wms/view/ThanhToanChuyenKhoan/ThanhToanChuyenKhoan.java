package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ThanhToanChuyenKhoan extends JDialog {

    // === BẢNG MÀU ===
    private final Color mauNenChinh = Color.decode("#FAFAFA");
    private final Color mauHongChinh = Color.decode("#D81B60");
    private final Color mauHongNhat = Color.decode("#FCE4EC");
    private final Color mauXamDam = Color.decode("#212529");
    private final Color mauXamNhat = Color.decode("#757575");
    private final Color mauXanhDuong = Color.decode("#1976D2");
    
    private double tongTienHoaDon;
    private boolean daThanhToan = false;
    
    private NumberFormat formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ThanhToanChuyenKhoan(JFrame parent, double tongTien) {
        super(parent, "Thanh Toán Chuyển Khoản", true);
        this.tongTienHoaDon = tongTien;
        khoiTaoGiaoDien();
        setLocationRelativeTo(parent);
    }

    private void khoiTaoGiaoDien() {
        this.setSize(550, 700);
        this.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 25));
        mainPanel.setBackground(mauNenChinh);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // === HEADER ===
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
        panelHeader.setBackground(mauNenChinh);
        
        JLabel lblIcon = new JLabel("🏦", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 56));
        lblIcon.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panelHeader.add(lblIcon);
        
        JLabel lblTieuDe = new JLabel("CHUYỂN KHOẢN NGÂN HÀNG", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTieuDe.setForeground(mauHongChinh);
        lblTieuDe.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        lblTieuDe.setBorder(new EmptyBorder(15, 0, 0, 0));
        panelHeader.add(lblTieuDe);
        
        JLabel lblMoTa = new JLabel("Quét mã QR để thanh toán", SwingConstants.CENTER);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMoTa.setForeground(mauXamNhat);
        lblMoTa.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        lblMoTa.setBorder(new EmptyBorder(8, 0, 0, 0));
        panelHeader.add(lblMoTa);
        
        mainPanel.add(panelHeader, BorderLayout.NORTH);

        // === CONTENT ===
        JPanel panelContent = new JPanel();
        panelContent.setLayout(new BoxLayout(panelContent, BoxLayout.Y_AXIS));
        panelContent.setBackground(Color.WHITE);
        panelContent.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));

        // Tổng tiền
        JPanel panelTongTien = new JPanel(new BorderLayout());
        panelTongTien.setBackground(mauHongNhat);
        panelTongTien.setBorder(new EmptyBorder(20, 20, 20, 20));
        panelTongTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panelTongTien.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        
        JLabel lblTextTong = new JLabel("Số tiền thanh toán:");
        lblTextTong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTextTong.setForeground(mauXamDam);
        panelTongTien.add(lblTextTong, BorderLayout.WEST);
        
        JLabel lblTongTien = new JLabel(formatTien.format(tongTienHoaDon));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTongTien.setForeground(mauHongChinh);
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        panelTongTien.add(lblTongTien, BorderLayout.CENTER);
        
        panelContent.add(panelTongTien);
        panelContent.add(taoKhoangCach(20));

        // Mã QR
        JPanel panelQR = new JPanel();
        panelQR.setLayout(new BoxLayout(panelQR, BoxLayout.Y_AXIS));
        panelQR.setBackground(Color.WHITE);
        panelQR.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        
        // Khung QR
        JPanel khungQR = taoKhungQR();
        khungQR.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        panelQR.add(khungQR);
        panelQR.add(taoKhoangCach(15));
        
        JLabel lblHuongDan = new JLabel("Quét mã QR bằng ứng dụng ngân hàng", SwingConstants.CENTER);
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblHuongDan.setForeground(mauXamNhat);
        lblHuongDan.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panelQR.add(lblHuongDan);
        
        panelContent.add(panelQR);
        panelContent.add(taoKhoangCach(20));

        // Thông tin chuyển khoản
        JPanel panelThongTin = new JPanel();
        panelThongTin.setLayout(new BoxLayout(panelThongTin, BoxLayout.Y_AXIS));
        panelThongTin.setBackground(Color.decode("#E3F2FD"));
        panelThongTin.setBorder(new EmptyBorder(15, 15, 15, 15));
        panelThongTin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        panelThongTin.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        panelThongTin.add(taoHangThongTin("Ngân hàng:", "VPBank"));
        panelThongTin.add(taoKhoangCach(8));
        panelThongTin.add(taoHangThongTin("Số tài khoản:", "1234567890"));
        panelThongTin.add(taoKhoangCach(8));
        panelThongTin.add(taoHangThongTin("Chủ tài khoản:", "UIT COWORKING SPACE"));
        panelThongTin.add(taoKhoangCach(8));
        panelThongTin.add(taoHangThongTin("Nội dung:", "UIT CW " + System.currentTimeMillis() % 100000));
        
        panelContent.add(panelThongTin);

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
        
        JButton btnXacNhan = taoNutXacNhan();
        btnXacNhan.addActionListener(e -> {
            daThanhToan = true;
            dispose();
        });
        panelFooter.add(btnXacNhan);
        
        mainPanel.add(panelFooter, BorderLayout.SOUTH);

        this.add(mainPanel);
    }

    private JPanel taoKhungQR() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ khung viền
                g2.setColor(new Color(220, 220, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Vẽ nền trắng bên trong
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 10, 10);
                
                g2.dispose();
            }
        };
        
        panel.setPreferredSize(new Dimension(250, 250));
        panel.setMaximumSize(new Dimension(250, 250));
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        
        // Tạo QR code giả (grid pattern)
        JPanel qrGrid = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                
                int cellSize = 10;
                int gridSize = 20;
                int offsetX = (getWidth() - gridSize * cellSize) / 2;
                int offsetY = (getHeight() - gridSize * cellSize) / 2;
                
                // Pattern mẫu cho QR code
                boolean[][] pattern = generateQRPattern(gridSize);
                
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        if (pattern[i][j]) {
                            g2.setColor(Color.BLACK);
                            g2.fillRect(offsetX + j * cellSize, offsetY + i * cellSize, cellSize, cellSize);
                        }
                    }
                }
                
                g2.dispose();
            }
        };
        
        qrGrid.setBackground(Color.WHITE);
        qrGrid.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.add(qrGrid, BorderLayout.CENTER);
        
        return panel;
    }

    private boolean[][] generateQRPattern(int size) {
        boolean[][] pattern = new boolean[size][size];
        
        // Tạo pattern giả cho QR code
        // 3 góc định vị
        drawPositionMarker(pattern, 0, 0);
        drawPositionMarker(pattern, 0, size - 7);
        drawPositionMarker(pattern, size - 7, 0);
        
        // Random pattern ở giữa
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!pattern[i][j]) {
                    pattern[i][j] = (i * j + i + j) % 3 == 0;
                }
            }
        }
        
        return pattern;
    }

    private void drawPositionMarker(boolean[][] pattern, int row, int col) {
        // Khung ngoài
        for (int i = 0; i < 7; i++) {
            pattern[row][col + i] = true;
            pattern[row + 6][col + i] = true;
            pattern[row + i][col] = true;
            pattern[row + i][col + 6] = true;
        }
        
        // Ô giữa
        for (int i = 2; i < 5; i++) {
            for (int j = 2; j < 5; j++) {
                pattern[row + i][col + j] = true;
            }
        }
    }

    private JPanel taoHangThongTin(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.decode("#E3F2FD"));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLabel.setForeground(mauXamNhat);
        panel.add(lblLabel, BorderLayout.WEST);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(mauXamDam);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblValue, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton taoNutXacNhan() {
        JButton btn = new JButton("XÁC NHẬN ĐÃ CHUYỂN KHOẢN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauXanhDuong);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(250, 45));
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

    private JPanel taoKhoangCach(int height) {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(0, height));
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        spacer.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        return spacer;
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

        ThanhToanChuyenKhoan dialog = new ThanhToanChuyenKhoan(frame, 725000);
        dialog.setVisible(true);
        
        System.out.println("Đã thanh toán: " + dialog.isDaThanhToan());
        System.exit(0);
    }
}