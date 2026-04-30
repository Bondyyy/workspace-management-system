package com.wms.view.XemChiNhanh;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class XemChiNhanh extends JPanel {

    // BẢNG MÀU CHÍNH - Tone hồng tinh tế & chuyên nghiệp
    private final Color mauNenChinh = Color.decode("#FAFAFA");        // Xám trắng
    private final Color mauHongChinh = Color.decode("#D81B60");       // Hồng đậm sang trọng
    private final Color mauHongNhat = Color.decode("#FCE4EC");        // Hồng rất nhạt
    private final Color mauHongPhu = Color.decode("#F06292");         // Hồng vừa
    private final Color mauVangNhat = Color.decode("#FFF9C4");        // Vàng nhạt cho accent
    private final Color mauXanhNhat = Color.decode("#E3F2FD");        // Xanh nhạt
    private final Color mauXamDam = Color.decode("#424242");          // Xám đậm cho chữ
    private final Color mauXamNhat = Color.decode("#BDBDBD");         // Xám nhạt
    
    private JPanel panelDanhSach;
    private ModelChiNhanh chiNhanhDangChon = null;
    private JButton nutXacNhan;

    public XemChiNhanh() {
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        this.setLayout(new BorderLayout(0, 20));
        this.setBackground(mauNenChinh);
        this.setBorder(new EmptyBorder(30, 40, 30, 40));

        // === PHẦN HEADER ===
        JPanel panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setBackground(mauNenChinh);
        
        JLabel lblTieuDe = new JLabel("DANH SÁCH CHI NHÁNH", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTieuDe.setForeground(mauHongChinh);
        panelTieuDe.add(lblTieuDe, BorderLayout.CENTER);
        
        JLabel lblMoTa = new JLabel("Chọn chi nhánh để xem sơ đồ không gian", SwingConstants.CENTER);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMoTa.setForeground(mauXamNhat);
        lblMoTa.setBorder(new EmptyBorder(10, 0, 0, 0));
        panelTieuDe.add(lblMoTa, BorderLayout.SOUTH);
        
        this.add(panelTieuDe, BorderLayout.NORTH);

        // === PHẦN DANH SÁCH CHI NHÁNH ===
        panelDanhSach = new JPanel();
        panelDanhSach.setLayout(new BoxLayout(panelDanhSach, BoxLayout.Y_AXIS));
        panelDanhSach.setBackground(mauNenChinh);
        panelDanhSach.setBorder(new EmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(panelDanhSach);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(mauNenChinh);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);

        // === PHẦN FOOTER - NÚT XÁC NHẬN ===
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelFooter.setBackground(mauNenChinh);
        panelFooter.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        nutXacNhan = taoNutXacNhan();
        nutXacNhan.setEnabled(false);
        panelFooter.add(nutXacNhan);
        
        this.add(panelFooter, BorderLayout.SOUTH);
    }

    private JButton taoNutXacNhan() {
        JButton nut = new JButton("XÁC NHẬN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isEnabled()) {
                    g2.setColor(mauHongChinh);
                } else {
                    g2.setColor(mauXamNhat);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        nut.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nut.setForeground(Color.WHITE);
        nut.setPreferredSize(new Dimension(200, 50));
        nut.setContentAreaFilled(false);
        nut.setBorderPainted(false);
        nut.setFocusPainted(false);
        nut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        nut.addActionListener(e -> {
            if (chiNhanhDangChon != null) {
                // TODO: Chuyển sang màn hình xem sơ đồ không gian
                System.out.println("Đã chọn chi nhánh: " + chiNhanhDangChon.getTenChiNhanh());
            }
        });
        
        return nut;
    }

    public void hienThiDanhSach(List<ModelChiNhanh> danhSach) {
        panelDanhSach.removeAll();
        
        for (ModelChiNhanh chiNhanh : danhSach) {
            JPanel cardChiNhanh = taoCardChiNhanh(chiNhanh);
            panelDanhSach.add(cardChiNhanh);
            panelDanhSach.add(taoKhoangCach(15));
        }
        
        panelDanhSach.revalidate();
        panelDanhSach.repaint();
    }

    private JPanel taoCardChiNhanh(ModelChiNhanh chiNhanh) {
        JPanel card = new JPanel(new BorderLayout(20, 0)) {
            private boolean isSelected = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background với border radius
                if (isSelected) {
                    g2.setColor(mauHongNhat);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                // Vẽ viền
                if (isSelected) {
                    g2.setColor(mauHongChinh);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 19, 19);
                } else {
                    g2.setColor(new Color(230, 230, 230));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                }
                
                g2.dispose();
            }
            
            public void setSelected(boolean selected) {
                this.isSelected = selected;
                repaint();
            }
        };
        
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 140));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setBorder(new EmptyBorder(20, 25, 20, 25));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // PHẦN BÊN TRÁI - Icon & Mã chi nhánh
        JPanel panelTrai = new JPanel(new BorderLayout(0, 8));
        panelTrai.setOpaque(false);
        panelTrai.setPreferredSize(new Dimension(120, 100));
        
        JLabel lblIcon = new JLabel("🏢", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        panelTrai.add(lblIcon, BorderLayout.CENTER);
        
        JLabel lblMa = new JLabel(chiNhanh.getMaChiNhanh(), SwingConstants.CENTER);
        lblMa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMa.setForeground(mauHongChinh);
        panelTrai.add(lblMa, BorderLayout.SOUTH);
        
        card.add(panelTrai, BorderLayout.WEST);

        // PHẦN GIỮA - Thông tin chi tiết
        JPanel panelGiua = new JPanel();
        panelGiua.setLayout(new BoxLayout(panelGiua, BoxLayout.Y_AXIS));
        panelGiua.setOpaque(false);
        panelGiua.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        // Tên chi nhánh
        JLabel lblTen = new JLabel(chiNhanh.getTenChiNhanh());
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTen.setForeground(mauXamDam);
        panelGiua.add(lblTen);
        panelGiua.add(taoKhoangCach(8));
        
        // Địa chỉ
        panelGiua.add(taoThongTinRow("📍", chiNhanh.getDiaChi()));
        panelGiua.add(taoKhoangCach(5));
        
        // Giờ mở cửa
        String gioMoCua = chiNhanh.getGioMoCua() + " - " + chiNhanh.getGioDongCua();
        panelGiua.add(taoThongTinRow("🕐", gioMoCua));
        panelGiua.add(taoKhoangCach(5));
        
        // Hotline
        panelGiua.add(taoThongTinRow("📞", chiNhanh.getDuongDayNong()));
        
        card.add(panelGiua, BorderLayout.CENTER);

        // PHẦN BÊN PHẢI - Trạng thái
        JPanel panelPhai = new JPanel(new BorderLayout());
        panelPhai.setOpaque(false);
        panelPhai.setPreferredSize(new Dimension(100, 100));
        
        JLabel lblTrangThai = new JLabel();
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTrangThai.setHorizontalAlignment(SwingConstants.CENTER);
        lblTrangThai.setOpaque(true);
        lblTrangThai.setPreferredSize(new Dimension(95, 32));
        
        if ("Hoạt động".equals(chiNhanh.getTrangThai())) {
            lblTrangThai.setText("✓ Hoạt động");
            lblTrangThai.setForeground(new Color(27, 94, 32));
            lblTrangThai.setBackground(new Color(200, 230, 201));
        } else {
            lblTrangThai.setText("✕ Đóng cửa");
            lblTrangThai.setForeground(new Color(183, 28, 28));
            lblTrangThai.setBackground(new Color(255, 205, 210));
        }
        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panelPhai.add(lblTrangThai, BorderLayout.NORTH);
        card.add(panelPhai, BorderLayout.EAST);

        // Xử lý click để chọn chi nhánh
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Bỏ chọn tất cả các card khác
                for (java.awt.Component comp : panelDanhSach.getComponents()) {
                    if (comp instanceof JPanel) {
                        try {
                            java.lang.reflect.Method method = comp.getClass().getMethod("setSelected", boolean.class);
                            method.invoke(comp, false);
                        } catch (Exception ex) {
                            // Ignore
                        }
                    }
                }
                
                // Chọn card hiện tại
                try {
                    java.lang.reflect.Method method = card.getClass().getMethod("setSelected", boolean.class);
                    method.invoke(card, true);
                } catch (Exception ex) {
                    // Ignore
                }
                
                chiNhanhDangChon = chiNhanh;
                nutXacNhan.setEnabled(true);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(mauXanhNhat);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(null);
            }
        });

        return card;
    }

    private JPanel taoThongTinRow(String icon, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        
        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblText.setForeground(new Color(97, 97, 97));
        
        panel.add(lblIcon);
        panel.add(lblText);
        
        return panel;
    }

    private JPanel taoKhoangCach(int height) {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(0, height));
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return spacer;
    }

    // === MODEL CLASS ===
    public static class ModelChiNhanh {
        private String maChiNhanh;
        private String tenChiNhanh;
        private String diaChi;
        private String gioMoCua;
        private String gioDongCua;
        private String duongDayNong;
        private String trangThai;

        public ModelChiNhanh(String ma, String ten, String diaChi, String gioMo, String gioDong, 
                            String hotline, String trangThai) {
            this.maChiNhanh = ma;
            this.tenChiNhanh = ten;
            this.diaChi = diaChi;
            this.gioMoCua = gioMo;
            this.gioDongCua = gioDong;
            this.duongDayNong = hotline;
            this.trangThai = trangThai;
        }

        public String getMaChiNhanh() { return maChiNhanh; }
        public String getTenChiNhanh() { return tenChiNhanh; }
        public String getDiaChi() { return diaChi; }
        public String getGioMoCua() { return gioMoCua; }
        public String getGioDongCua() { return gioDongCua; }
        public String getDuongDayNong() { return duongDayNong; }
        public String getTrangThai() { return trangThai; }
    }

    // === MAIN TEST ===
    public static void main(String[] args) {
        JFrame frame = new JFrame("Danh Sách Chi Nhánh - UIT Coworking Space");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);

        XemChiNhanh view = new XemChiNhanh();
        
        // Tạo dữ liệu mẫu
        List<ModelChiNhanh> danhSach = new ArrayList<>();
        danhSach.add(new ModelChiNhanh(
            "CN001", 
            "Chi nhánh Quận 1", 
            "123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM",
            "07:00",
            "22:00",
            "028 3822 5678",
            "Hoạt động"
        ));
        danhSach.add(new ModelChiNhanh(
            "CN002",
            "Chi nhánh Thủ Đức",
            "Đại học Quốc gia, Khu phố 6, Phường Linh Trung, TP. Thủ Đức",
            "06:30",
            "23:00",
            "028 3724 1234",
            "Hoạt động"
        ));
        danhSach.add(new ModelChiNhanh(
            "CN003",
            "Chi nhánh Quận 3",
            "456 Võ Văn Tần, Phường 5, Quận 3, TP.HCM",
            "08:00",
            "20:00",
            "028 3930 9876",
            "Đóng cửa"
        ));
        danhSach.add(new ModelChiNhanh(
            "CN004",
            "Chi nhánh Bình Thạnh",
            "789 Xô Viết Nghệ Tĩnh, Phường 21, Quận Bình Thạnh, TP.HCM",
            "07:30",
            "22:30",
            "028 3512 4567",
            "Hoạt động"
        ));
        
        view.hienThiDanhSach(danhSach);
        
        frame.add(view);
        frame.setVisible(true);
    }
}