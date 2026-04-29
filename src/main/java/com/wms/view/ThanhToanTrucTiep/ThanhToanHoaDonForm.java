package com.wms.view.ThanhToanTrucTiep;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ThanhToanHoaDonForm extends JPanel {

    // === BẢNG MÀU CHÍNH ===
    private final Color mauNenChinh = Color.decode("#FAFAFA");
    private final Color mauHongChinh = Color.decode("#D81B60");
    private final Color mauHongNhat = Color.decode("#FCE4EC");
    private final Color mauXamDam = Color.decode("#212529");
    private final Color mauXamNhat = Color.decode("#757575");
    private final Color mauXanhNhat = Color.decode("#E3F2FD");
    
    private JPanel panelChiTietHD;
    private JLabel lblTongTien;
    private JPanel containerTienMat, containerChuyenKhoan;
    private JLabel checkTienMat, checkChuyenKhoan;
    private JButton nutInHoaDon;
    
    private ModelHoaDon hoaDonHienTai;
    private boolean daDongTienMat = false;
    private boolean daDongChuyenKhoan = false;
    
    private NumberFormat formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ThanhToanHoaDonForm() {
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        this.setLayout(new BorderLayout(0, 20));
        this.setBackground(mauNenChinh);
        this.setBorder(new EmptyBorder(30, 40, 30, 40));

        // === HEADER ===
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(mauNenChinh);
        
        JLabel lblTieuDe = new JLabel("THANH TOÁN HÓA ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTieuDe.setForeground(mauHongChinh);
        panelHeader.add(lblTieuDe, BorderLayout.CENTER);
        
        this.add(panelHeader, BorderLayout.NORTH);

        // === CONTENT - Chia 2 cột ===
        JPanel panelNoiDung = new JPanel(new GridLayout(1, 2, 25, 0));
        panelNoiDung.setBackground(mauNenChinh);

        // CỘT TRÁI - Chi tiết hóa đơn
        JPanel cotTrai = taoPanelChiTietHoaDon();
        panelNoiDung.add(cotTrai);

        // CỘT PHẢI - Phương thức thanh toán
        JPanel cotPhai = taoPanelPhuongThucThanhToan();
        panelNoiDung.add(cotPhai);

        this.add(panelNoiDung, BorderLayout.CENTER);
    }

    private JPanel taoPanelChiTietHoaDon() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));

        // Tiêu đề
        JLabel lblTieuDe = new JLabel("CHI TIẾT HÓA ĐƠN");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTieuDe.setForeground(mauHongChinh);
        panel.add(lblTieuDe, BorderLayout.NORTH);

        // Nội dung cuộn
        panelChiTietHD = new JPanel();
        panelChiTietHD.setLayout(new BoxLayout(panelChiTietHD, BoxLayout.Y_AXIS));
        panelChiTietHD.setBackground(Color.WHITE);
        panelChiTietHD.setBorder(new EmptyBorder(20, 0, 20, 0));

        JScrollPane scroll = new JScrollPane(panelChiTietHD);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        // Footer - Tổng tiền
        JPanel panelTong = new JPanel(new BorderLayout(10, 10));
        panelTong.setBackground(mauHongNhat);
        panelTong.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTextTong = new JLabel("TỔNG THANH TOÁN:");
        lblTextTong.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTextTong.setForeground(mauXamDam);
        panelTong.add(lblTextTong, BorderLayout.WEST);
        
        lblTongTien = new JLabel("0 ₫");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTongTien.setForeground(mauHongChinh);
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        panelTong.add(lblTongTien, BorderLayout.CENTER);
        
        panel.add(panelTong, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel taoPanelPhuongThucThanhToan() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(mauNenChinh);

        // Tiêu đề
        JLabel lblTieuDe = new JLabel("PHƯƠNG THỨC THANH TOÁN");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTieuDe.setForeground(mauHongChinh);
        lblTieuDe.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(lblTieuDe);
        
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Container Tiền mặt
        containerTienMat = taoContainerPhuongThuc("", "TIỀN MẶT", "Thanh toán bằng tiền mặt tại quầy");
        containerTienMat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                moManHinhTienMat();
            }
        });
        panel.add(containerTienMat);
        
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Container Chuyển khoản
        containerChuyenKhoan = taoContainerPhuongThuc("","CHUYỂN KHOẢN", "Chuyển khoản qua ngân hàng");
        containerChuyenKhoan.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                moManHinhChuyenKhoan();
            }
        });
        panel.add(containerChuyenKhoan);
        
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Đường phân cách
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(220, 220, 220));
        sep.setAlignmentX(JSeparator.LEFT_ALIGNMENT);
        panel.add(sep);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Nút In hóa đơn
        nutInHoaDon = taoNutInHoaDon();
        nutInHoaDon.setEnabled(false);
        nutInHoaDon.addActionListener(e -> inHoaDon());
        panel.add(nutInHoaDon);

        // Thêm glue để đẩy nội dung lên trên
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel taoContainerPhuongThuc(String emoji, String text, String moTa) {
        JPanel container = new JPanel(new BorderLayout(15, 0));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        container.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        container.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon check (ẩn ban đầu)
        JLabel lblCheck = new JLabel("✓");
        lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblCheck.setForeground(new Color(76, 175, 80));
        lblCheck.setVisible(false);
        container.add(lblCheck, BorderLayout.WEST);
        
        // Lưu reference
        if ("💵".equals(emoji)) {
            checkTienMat = lblCheck;
        } else {
            checkChuyenKhoan = lblCheck;
        }

        // Text panel
        JPanel panelText = new JPanel();
        panelText.setLayout(new BoxLayout(panelText, BoxLayout.Y_AXIS));
        panelText.setBackground(Color.WHITE);
        
        JLabel lblText = new JLabel(emoji + " " + text);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblText.setForeground(mauXamDam);
        lblText.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelText.add(lblText);
        
        panelText.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JLabel lblMoTa = new JLabel(moTa);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMoTa.setForeground(mauXamNhat);
        lblMoTa.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelText.add(lblMoTa);
        
        container.add(panelText, BorderLayout.CENTER);

        // Hover effect
        container.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                container.setBackground(mauXanhNhat);
                panelText.setBackground(mauXanhNhat);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                container.setBackground(Color.WHITE);
                panelText.setBackground(Color.WHITE);
            }
        });

        return container;
    }

    private JButton taoNutInHoaDon() {
        JButton btn = new JButton("IN HÓA ĐƠN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isEnabled()) {
                    g2.setColor(mauHongChinh);
                } else {
                    g2.setColor(new Color(189, 189, 189));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(0, 60));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        btn.setAlignmentX(JButton.LEFT_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    public void hienThiHoaDon(ModelHoaDon hoaDon) {
        this.hoaDonHienTai = hoaDon;
        panelChiTietHD.removeAll();

        // Thông tin khách hàng
        panelChiTietHD.add(taoThongTinRow("Mã hóa đơn:", hoaDon.getMaHoaDon(), true));
        panelChiTietHD.add(taoKhoangCach(10));
        panelChiTietHD.add(taoThongTinRow("Khách hàng:", hoaDon.getTenKhachHang(), false));
        panelChiTietHD.add(taoKhoangCach(10));
        panelChiTietHD.add(taoThongTinRow("Không gian:", hoaDon.getTenKhongGian(), false));
        panelChiTietHD.add(taoKhoangCach(10));
        panelChiTietHD.add(taoThongTinRow("Thời gian:", hoaDon.getGioDat(), false));
        
        panelChiTietHD.add(taoKhoangCach(20));
        panelChiTietHD.add(taoDuongPhanCach());
        panelChiTietHD.add(taoKhoangCach(20));

        // Chi tiết dịch vụ
        JLabel lblDichVu = new JLabel("DỊCH VỤ SỬ DỤNG");
        lblDichVu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDichVu.setForeground(mauHongChinh);
        lblDichVu.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelChiTietHD.add(lblDichVu);
        panelChiTietHD.add(taoKhoangCach(15));

        // Dịch vụ chính
        panelChiTietHD.add(taoDichVuRow(
            hoaDon.getTenKhongGian(),
            hoaDon.getSoGio() + " giờ",
            hoaDon.getGiaKhongGian()
        ));

        // Dịch vụ kèm theo
        for (ModelDichVu dv : hoaDon.getDanhSachDichVu()) {
            panelChiTietHD.add(taoKhoangCach(8));
            panelChiTietHD.add(taoDichVuRow(dv.getTenDichVu(), "x" + dv.getSoLuong(), dv.getThanhTien()));
        }

        // Giờ gia hạn (nếu có)
        if (hoaDon.getSoGioGiaHan() > 0) {
            panelChiTietHD.add(taoKhoangCach(8));
            panelChiTietHD.add(taoDichVuRow(
                "Gia hạn thêm",
                hoaDon.getSoGioGiaHan() + " giờ",
                hoaDon.getTienGiaHan()
            ));
        }

        // Cập nhật tổng tiền
        lblTongTien.setText(formatTien.format(hoaDon.getTongTien()));

        panelChiTietHD.revalidate();
        panelChiTietHD.repaint();
    }

    private JPanel taoThongTinRow(String label, String value, boolean bold) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLabel.setForeground(mauXamNhat);
        panel.add(lblLabel, BorderLayout.WEST);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 14));
        lblValue.setForeground(mauXamDam);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblValue, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel taoDichVuRow(String ten, String soLuong, double gia) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        JPanel panelTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelTrai.setBackground(Color.WHITE);
        
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTen.setForeground(mauXamDam);
        panelTrai.add(lblTen);
        
        JLabel lblSL = new JLabel("(" + soLuong + ")");
        lblSL.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSL.setForeground(mauXamNhat);
        panelTrai.add(lblSL);
        
        panel.add(panelTrai, BorderLayout.WEST);
        
        JLabel lblGia = new JLabel(formatTien.format(gia));
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblGia.setForeground(mauXamDam);
        lblGia.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblGia, BorderLayout.EAST);
        
        return panel;
    }

    private JSeparator taoDuongPhanCach() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(JSeparator.LEFT_ALIGNMENT);
        sep.setForeground(new Color(230, 230, 230));
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

    private void moManHinhTienMat() {
        JFrame parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        ThanhToanTienMatForm dialog = new ThanhToanTienMatForm(parentFrame, hoaDonHienTai.getTongTien());
        dialog.setVisible(true);
        
        if (dialog.isDaThanhToan()) {
            daDongTienMat = true;
            capNhatTrangThaiThanhToan();
        }
    }

    private void moManHinhChuyenKhoan() {
        JFrame parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        ThanhToanChuyenKhoanForm dialog = new ThanhToanChuyenKhoanForm(parentFrame, hoaDonHienTai.getTongTien());
        dialog.setVisible(true);
        
        if (dialog.isDaThanhToan()) {
            daDongChuyenKhoan = true;
            capNhatTrangThaiThanhToan();
        }
    }

    private void capNhatTrangThaiThanhToan() {
        if (daDongTienMat && checkTienMat != null) {
            checkTienMat.setVisible(true);
        }
        
        if (daDongChuyenKhoan && checkChuyenKhoan != null) {
            checkChuyenKhoan.setVisible(true);
        }
        
        // Cho phép in hóa đơn khi đã thanh toán
        if (daDongTienMat || daDongChuyenKhoan) {
            nutInHoaDon.setEnabled(true);
        }
    }

    private void inHoaDon() {
        // TODO: Xử lý in hóa đơn
        System.out.println("In hóa đơn: " + hoaDonHienTai.getMaHoaDon());
        // Có thể xuất PDF hoặc in trực tiếp
    }

    // === MODEL CLASSES ===
    public static class ModelHoaDon {
        private String maHoaDon, tenKhachHang, tenKhongGian, gioDat;
        private int soGio, soGioGiaHan;
        private double giaKhongGian, tienGiaHan, tongTien;
        private List<ModelDichVu> danhSachDichVu;

        public ModelHoaDon(String ma, String kh, String kg, String gio, int soGio, double giaKG) {
            this.maHoaDon = ma;
            this.tenKhachHang = kh;
            this.tenKhongGian = kg;
            this.gioDat = gio;
            this.soGio = soGio;
            this.giaKhongGian = giaKG;
            this.danhSachDichVu = new ArrayList<>();
            this.soGioGiaHan = 0;
            this.tienGiaHan = 0;
            this.tongTien = giaKG;
        }

        public void themDichVu(ModelDichVu dv) {
            danhSachDichVu.add(dv);
            tongTien += dv.getThanhTien();
        }

        public void setGiaHan(int soGio, double tien) {
            this.soGioGiaHan = soGio;
            this.tienGiaHan = tien;
            tongTien += tien;
        }

        public String getMaHoaDon() { return maHoaDon; }
        public String getTenKhachHang() { return tenKhachHang; }
        public String getTenKhongGian() { return tenKhongGian; }
        public String getGioDat() { return gioDat; }
        public int getSoGio() { return soGio; }
        public double getGiaKhongGian() { return giaKhongGian; }
        public int getSoGioGiaHan() { return soGioGiaHan; }
        public double getTienGiaHan() { return tienGiaHan; }
        public double getTongTien() { return tongTien; }
        public List<ModelDichVu> getDanhSachDichVu() { return danhSachDichVu; }
    }

    public static class ModelDichVu {
        private String tenDichVu;
        private int soLuong;
        private double donGia, thanhTien;

        public ModelDichVu(String ten, int sl, double gia) {
            this.tenDichVu = ten;
            this.soLuong = sl;
            this.donGia = gia;
            this.thanhTien = sl * gia;
        }

        public String getTenDichVu() { return tenDichVu; }
        public int getSoLuong() { return soLuong; }
        public double getDonGia() { return donGia; }
        public double getThanhTien() { return thanhTien; }
    }

    // === MAIN TEST ===
    public static void main(String[] args) {
        JFrame frame = new JFrame("Thanh Toán Hóa Đơn - UIT Coworking Space");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 800);
        frame.setLocationRelativeTo(null);

        ThanhToanHoaDonForm view = new ThanhToanHoaDonForm();
        
        // Tạo hóa đơn mẫu
        ModelHoaDon hoaDon = new ModelHoaDon(
            "HD001234",
            "Nguyễn Văn A",
            "Phòng họp 16 người",
            "08:00 - 12:00 (27/04/2024)",
            4,
            400000
        );
        
        hoaDon.themDichVu(new ModelDichVu("Cà phê", 3, 25000));
        hoaDon.themDichVu(new ModelDichVu("Nước suối", 5, 10000));
        hoaDon.themDichVu(new ModelDichVu("Máy chiếu", 1, 50000));
        hoaDon.setGiaHan(2, 200000);
        
        view.hienThiHoaDon(hoaDon);
        
        frame.add(view);
        frame.setVisible(true);
    }
}