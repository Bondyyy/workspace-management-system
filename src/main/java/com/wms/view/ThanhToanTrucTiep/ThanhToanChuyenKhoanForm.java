package com.wms.view.ThanhToanTrucTiep;

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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ThanhToanChuyenKhoanForm extends JDialog {

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

    public ThanhToanChuyenKhoanForm(JFrame parent, double tongTien) {
        super(parent, "Thanh Toán Chuyển Khoản", true);
        this.tongTienHoaDon = tongTien;
        khoiTaoGiaoDien();
        setLocationRelativeTo(parent);
    }

    private void khoiTaoGiaoDien() {
        this.setSize(560, 780);
        this.setMinimumSize(new java.awt.Dimension(560, 780));
        this.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 25));
        mainPanel.setBackground(mauNenChinh);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // === HEADER ===
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
        panelHeader.setBackground(mauNenChinh);
        
        JLabel lblTieuDe = new JLabel("CHUYỂN KHOẢN NGÂN HÀNG", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTieuDe.setForeground(mauHongChinh);
        lblTieuDe.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panelHeader.add(lblTieuDe);
        
        JLabel lblMoTa = new JLabel("Quét mã QR để thanh toán", SwingConstants.CENTER);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMoTa.setForeground(mauXamNhat);
        lblMoTa.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        lblMoTa.setBorder(new EmptyBorder(10, 0, 0, 0));
        panelHeader.add(lblMoTa);
        
        mainPanel.add(panelHeader, BorderLayout.NORTH);

        // === CONTENT ===
        JPanel panelContent = new JPanel();
        panelContent.setLayout(new BoxLayout(panelContent, BoxLayout.Y_AXIS));
        panelContent.setBackground(Color.WHITE);
        panelContent.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(25, 30, 25, 30)
        ));
        panelContent.setAlignmentX(JPanel.CENTER_ALIGNMENT);

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
        panelContent.add(Box.createRigidArea(new Dimension(0, 20)));

        // Mã QR
        JPanel panelQR = new JPanel();
        panelQR.setLayout(new BoxLayout(panelQR, BoxLayout.Y_AXIS));
        panelQR.setBackground(Color.WHITE);
        panelQR.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        panelQR.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        
        // Khung QR — căn giữa
        JPanel wrapQR = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        wrapQR.setBackground(Color.WHITE);
        JPanel khungQR = taoKhungQR();
        wrapQR.add(khungQR);
        panelQR.add(wrapQR);
        panelQR.add(Box.createRigidArea(new Dimension(0, 12)));
        
        JLabel lblHuongDan = new JLabel("Quét mã QR bằng ứng dụng ngân hàng", SwingConstants.CENTER);
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblHuongDan.setForeground(mauXamNhat);
        lblHuongDan.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        lblHuongDan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        panelQR.add(lblHuongDan);
        
        panelContent.add(panelQR);
        panelContent.add(Box.createRigidArea(new Dimension(0, 20)));

        // Thông tin chuyển khoản
        JPanel panelThongTin = new JPanel();
        panelThongTin.setLayout(new BoxLayout(panelThongTin, BoxLayout.Y_AXIS));
        panelThongTin.setBackground(Color.decode("#E3F2FD"));
        panelThongTin.setBorder(new EmptyBorder(15, 20, 15, 20));
        panelThongTin.setMaximumSize(new Dimension(460, 170));
        panelThongTin.setPreferredSize(new Dimension(460, 170));
        panelThongTin.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        
        panelThongTin.add(taoHangThongTin("Ngân hàng:", "VPBank"));
        panelThongTin.add(Box.createRigidArea(new Dimension(0, 8)));
        panelThongTin.add(taoHangThongTin("Số tài khoản:", "1234567890"));
        panelThongTin.add(Box.createRigidArea(new Dimension(0, 8)));
        panelThongTin.add(taoHangThongTin("Chủ tài khoản:", "UIT COWORKING SPACE"));
        panelThongTin.add(Box.createRigidArea(new Dimension(0, 8)));
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
                
                // Vẽ khung viền bo góc
                g2.setColor(new Color(220, 220, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Vẽ nền trắng bên trong
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 10, 10);
                
                g2.dispose();
            }
        };
        
        panel.setPreferredSize(new Dimension(280, 280));
        panel.setMaximumSize(new Dimension(280, 280));
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        
        // Tạo QR code pattern
        JPanel qrGrid = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                
                int cellSize = 8;
                int gridSize = 25;
                int offsetX = (getWidth() - gridSize * cellSize) / 2;
                int offsetY = (getHeight() - gridSize * cellSize) / 2;
                
                // Pattern mẫu cho QR code
                boolean[][] pattern = generateQRPattern(gridSize);
                
                g2.setColor(Color.BLACK);
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        if (pattern[i][j]) {
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
        
        // Vẽ 3 góc định vị (Position Detection Patterns)
        drawPositionMarker(pattern, 0, 0);
        drawPositionMarker(pattern, 0, size - 7);
        drawPositionMarker(pattern, size - 7, 0);
        
        // Timing patterns (dòng và cột dấu chấm)
        for (int i = 8; i < size - 8; i++) {
            pattern[6][i] = (i % 2 == 0);
            pattern[i][6] = (i % 2 == 0);
        }
        
        // Vùng dữ liệu giả (data modules)
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Bỏ qua các vùng đã vẽ
                if (isInPositionMarker(i, j, size)) continue;
                if (i == 6 || j == 6) continue; // Timing pattern
                
                // Tạo pattern ngẫu nhiên nhưng có cấu trúc
                int seed = i * 31 + j * 17;
                pattern[i][j] = ((seed % 3) == 0);
            }
        }
        
        return pattern;
    }

    private void drawPositionMarker(boolean[][] pattern, int row, int col) {
        // Khung ngoài 7x7
        for (int i = 0; i < 7; i++) {
            if (row + i < pattern.length && col < pattern[0].length) {
                pattern[row + i][col] = true;
                pattern[row + i][col + 6] = true;
            }
            if (row < pattern.length && col + i < pattern[0].length) {
                pattern[row][col + i] = true;
                pattern[row + 6][col + i] = true;
            }
        }
        
        // Ô giữa 3x3
        for (int i = 2; i < 5; i++) {
            for (int j = 2; j < 5; j++) {
                if (row + i < pattern.length && col + j < pattern[0].length) {
                    pattern[row + i][col + j] = true;
                }
            }
        }
    }

    private boolean isInPositionMarker(int i, int j, int size) {
        // Góc trên trái
        if (i < 8 && j < 8) return true;
        // Góc trên phải
        if (i < 8 && j >= size - 8) return true;
        // Góc dưới trái
        if (i >= size - 8 && j < 8) return true;
        return false;
    }

    private JPanel taoHangThongTin(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.decode("#E3F2FD"));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        panel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 26));
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

        ThanhToanChuyenKhoanForm dialog = new ThanhToanChuyenKhoanForm(frame, 775000);
        dialog.setVisible(true);
        
        System.out.println("Đã thanh toán: " + dialog.isDaThanhToan());
        System.exit(0);
    }
}