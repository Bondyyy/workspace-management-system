package com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan;

import com.wms.controller.ThanhToanController;
import com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.TienMat.TienMatForm;
import com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.ChuyenKhoan.ChuyenKhoanForm;
import com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDTO;
import com.wms.model.ThongTinHoaDonDTO;
import com.wms.model.DichVuDaDungDTO;
import com.wms.model.XacNhanPhieuGiamGiaDTO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ThanhToanHoaDonForm extends JPanel {

    private final Color mauNenChinh = Color.decode("#FAFAFA");
    private final Color mauHongChinh = Color.decode("#D81B60");
    private final Color mauHongNhat = Color.decode("#FCE4EC");
    private final Color mauXamDam = Color.decode("#212529");
    private final Color mauXamNhat = Color.decode("#757575");
    private final Color mauXanhNhat = Color.decode("#E3F2FD");
    private final Color mauXanhLa = Color.decode("#4CAF50");
    private final Color mauDo = Color.decode("#F44336");

    private JPanel panelChiTietHD;
    private JLabel lblTongTien, lblThanhTien;
    private JPanel containerTienMat, containerChuyenKhoan;
    private JLabel checkTienMat, checkChuyenKhoan;
    private JButton nutInHoaDon;
    private JPanel panelTextTienMat, panelTextChuyenKhoan;
    private JLabel lblStatusTienMat, lblStatusChuyenKhoan;

    private JTextField txtMaGiamGia;
    private JLabel lblTrangThaiMaGG;
    private JPanel panelHienThiGiamGia;
    private JButton btnXoaMaGG;

    private JLabel lblMaHD, lblTenKhachHang, lblTenPhong, lblThoiGian, lblTongGio;

    private ThongTinHoaDonDTO hoaDonHienTai;
    private PhieuGiamGiaDTO maGiamGiaDangAp = null;
    private double tongTienGoc = 0, tienGiamGia = 0, thanhTien = 0;
    private boolean daDongTienMat = false, daDongChuyenKhoan = false;

    private final ThanhToanController thanhToanController = new ThanhToanController();
    private final NumberFormat formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final String maHoaDonHienTai;

    public ThanhToanHoaDonForm() {
        this(null);
    }

    public ThanhToanHoaDonForm(String maHoaDon) {
        this.maHoaDonHienTai = maHoaDon;
        khoiTaoGiaoDien();
        if (maHoaDon != null && !maHoaDon.isEmpty()) {
            loadDuLieuHoaDon(maHoaDon);
        }
    }

    private void loadDuLieuHoaDon(String maHoaDon) {
        ThongTinHoaDonDTO hoaDon = thanhToanController.loadDuLieuHoaDon(maHoaDon);
        if (hoaDon != null) {
            hienThiHoaDon(hoaDon);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn: " + maHoaDon, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getMaHoaDonHienTai() {
        return maHoaDonHienTai;
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 20));
        setBackground(mauNenChinh);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initializeLabels();

        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(mauNenChinh);
        JLabel lblTieuDe = new JLabel("THANH TOÁN HÓA ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTieuDe.setForeground(mauHongChinh);
        panelHeader.add(lblTieuDe, BorderLayout.CENTER);
        add(panelHeader, BorderLayout.NORTH);

        JPanel panelNoiDung = new JPanel(new GridLayout(1, 2, 25, 0));
        panelNoiDung.setBackground(mauNenChinh);
        panelNoiDung.add(taoPanelChiTietHoaDon());
        panelNoiDung.add(taoPanelPhuongThucThanhToan());
        add(panelNoiDung, BorderLayout.CENTER);
    }

    private JPanel taoPanelChiTietHoaDon() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel lblTieuDe = new JLabel("CHI TIẾT HÓA ĐƠN");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTieuDe.setForeground(mauHongChinh);
        panel.add(lblTieuDe, BorderLayout.NORTH);

        panelChiTietHD = new JPanel();
        panelChiTietHD.setLayout(new BoxLayout(panelChiTietHD, BoxLayout.Y_AXIS));
        panelChiTietHD.setBackground(Color.WHITE);
        panelChiTietHD.setBorder(new EmptyBorder(20, 0, 20, 0));

        JScrollPane scroll = new JScrollPane(panelChiTietHD);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelFooter = new JPanel();
        panelFooter.setLayout(new BoxLayout(panelFooter, BoxLayout.Y_AXIS));
        panelFooter.setBackground(Color.WHITE);

        JPanel panelTong = new JPanel(new BorderLayout(10, 10));
        panelTong.setBackground(new Color(245, 245, 245));
        panelTong.setBorder(new EmptyBorder(15, 20, 15, 20));
        panelTong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panelTong.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel lblTextTong = new JLabel("Tổng tiền:");
        lblTextTong.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTextTong.setForeground(mauXamDam);
        panelTong.add(lblTextTong, BorderLayout.WEST);

        lblTongTien = new JLabel("0 ₫");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongTien.setForeground(mauXamDam);
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        panelTong.add(lblTongTien, BorderLayout.CENTER);

        panelFooter.add(panelTong);
        panelFooter.add(Box.createRigidArea(new Dimension(0, 5)));

        panelHienThiGiamGia = new JPanel(new BorderLayout(10, 5));
        panelHienThiGiamGia.setBackground(Color.WHITE);
        panelHienThiGiamGia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panelHienThiGiamGia.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        panelHienThiGiamGia.setVisible(false);

        JPanel panelGGLeft = new JPanel();
        panelGGLeft.setLayout(new BoxLayout(panelGGLeft, BoxLayout.Y_AXIS));
        panelGGLeft.setBackground(Color.WHITE);

        JLabel lblTextGG = new JLabel("Mã giảm giá:");
        lblTextGG.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTextGG.setForeground(mauXamNhat);
        lblTextGG.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelGGLeft.add(lblTextGG);

        JLabel lblMaGG = new JLabel("");
        lblMaGG.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMaGG.setForeground(mauXanhLa);
        lblMaGG.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelGGLeft.add(lblMaGG);
        panelHienThiGiamGia.putClientProperty("lblMaGG", lblMaGG);

        panelHienThiGiamGia.add(panelGGLeft, BorderLayout.WEST);

        JLabel lblGiaTriGG = new JLabel("");
        lblGiaTriGG.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGiaTriGG.setForeground(mauXanhLa);
        lblGiaTriGG.setHorizontalAlignment(SwingConstants.RIGHT);
        panelHienThiGiamGia.add(lblGiaTriGG, BorderLayout.CENTER);
        panelHienThiGiamGia.putClientProperty("lblGiaTriGG", lblGiaTriGG);

        btnXoaMaGG = new JButton("X") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? mauDo : new Color(255, 230, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnXoaMaGG.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXoaMaGG.setForeground(mauDo);
        btnXoaMaGG.setPreferredSize(new Dimension(35, 35));
        btnXoaMaGG.setContentAreaFilled(false);
        btnXoaMaGG.setBorderPainted(false);
        btnXoaMaGG.setFocusPainted(false);
        btnXoaMaGG.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXoaMaGG.setToolTipText("Xóa mã giảm giá");
        btnXoaMaGG.addActionListener(e -> xoaMaGiamGia());
        panelHienThiGiamGia.add(btnXoaMaGG, BorderLayout.EAST);

        panelFooter.add(panelHienThiGiamGia);
        panelFooter.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel panelThanhTien = new JPanel(new BorderLayout(10, 10));
        panelThanhTien.setBackground(mauHongNhat);
        panelThanhTien.setBorder(new EmptyBorder(20, 20, 20, 20));
        panelThanhTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panelThanhTien.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel lblTextThanhTien = new JLabel("THÀNH TIỀN:");
        lblTextThanhTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTextThanhTien.setForeground(mauXamDam);
        panelThanhTien.add(lblTextThanhTien, BorderLayout.WEST);

        lblThanhTien = new JLabel("0 ₫");
        lblThanhTien.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblThanhTien.setForeground(mauHongChinh);
        lblThanhTien.setHorizontalAlignment(SwingConstants.RIGHT);
        panelThanhTien.add(lblThanhTien, BorderLayout.CENTER);

        panelFooter.add(panelThanhTien);
        panel.add(panelFooter, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel taoPanelPhuongThucThanhToan() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(mauNenChinh);

        JLabel lblTieuDe = new JLabel("PHƯƠNG THỨC THANH TOÁN");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTieuDe.setForeground(mauHongChinh);
        lblTieuDe.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(lblTieuDe);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel panelMaGG = new JPanel();
        panelMaGG.setLayout(new BoxLayout(panelMaGG, BoxLayout.Y_AXIS));
        panelMaGG.setBackground(Color.WHITE);
        panelMaGG.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panelMaGG.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        panelMaGG.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel lblMaGG = new JLabel("Mã giảm giá (nếu có):");
        lblMaGG.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaGG.setForeground(mauXamDam);
        lblMaGG.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelMaGG.add(lblMaGG);
        panelMaGG.add(Box.createRigidArea(new Dimension(0, 8)));

        txtMaGiamGia = new JTextField();
        txtMaGiamGia.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtMaGiamGia.setForeground(mauXamDam);
        txtMaGiamGia.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(10, 12, 10, 12)
        ));
        txtMaGiamGia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        txtMaGiamGia.setAlignmentX(JTextField.LEFT_ALIGNMENT);
        txtMaGiamGia.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { kiemTraMaGiamGia(); }
            @Override public void removeUpdate(DocumentEvent e) { kiemTraMaGiamGia(); }
            @Override public void insertUpdate(DocumentEvent e) { kiemTraMaGiamGia(); }
        });

        panelMaGG.add(txtMaGiamGia);
        panelMaGG.add(Box.createRigidArea(new Dimension(0, 6)));

        lblTrangThaiMaGG = new JLabel("");
        lblTrangThaiMaGG.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTrangThaiMaGG.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelMaGG.add(lblTrangThaiMaGG);

        panel.add(panelMaGG);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        containerTienMat = taoContainerPhuongThuc("TIENMAT", "TIỀN MẶT", "Thanh toán bằng tiền mặt tại quầy");
        containerTienMat.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { moManHinhTienMat(); }
        });
        panel.add(containerTienMat);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        containerChuyenKhoan = taoContainerPhuongThuc("CHUYENKHOAN", "CHUYỂN KHOẢN", "Chuyển khoản qua ngân hàng");
        containerChuyenKhoan.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { moManHinhChuyenKhoan(); }
        });
        panel.add(containerChuyenKhoan);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(220, 220, 220));
        sep.setAlignmentX(JSeparator.LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        nutInHoaDon = taoNutInHoaDon();
        nutInHoaDon.setEnabled(false);
        nutInHoaDon.addActionListener(e -> inHoaDon());
        panel.add(nutInHoaDon);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void kiemTraMaGiamGia() {
        String maVoucher = txtMaGiamGia.getText().trim();
        if (maVoucher.isEmpty()) {
            if (maGiamGiaDangAp != null) xoaMaGiamGia();
            lblTrangThaiMaGG.setText("");
            return;
        }

        XacNhanPhieuGiamGiaDTO validationResult = thanhToanController.kiemTraPhieuGiamGia(maVoucher, tongTienGoc);
        if (!validationResult.isValid()) {
            lblTrangThaiMaGG.setText(validationResult.getMessage());
            lblTrangThaiMaGG.setForeground(mauDo);
            maGiamGiaDangAp = null;
            panelHienThiGiamGia.setVisible(false);
            tinhLaiTongTien();
            return;
        }

        maGiamGiaDangAp = validationResult.getVoucherInfo();
        lblTrangThaiMaGG.setText(validationResult.getMessage());
        lblTrangThaiMaGG.setForeground(mauXanhLa);

        JLabel lblMaGG = (JLabel) panelHienThiGiamGia.getClientProperty("lblMaGG");
        if (lblMaGG != null) lblMaGG.setText(maGiamGiaDangAp.getMaPGG());

        tienGiamGia = validationResult.getDiscountAmount();
        JLabel lblGiaTriGG = (JLabel) panelHienThiGiamGia.getClientProperty("lblGiaTriGG");
        if (lblGiaTriGG != null) lblGiaTriGG.setText("- " + formatTien.format(tienGiamGia));

        tinhLaiTongTien();
        panelHienThiGiamGia.setVisible(true);
        panelHienThiGiamGia.revalidate();
        panelHienThiGiamGia.repaint();
    }

    private void xoaMaGiamGia() {
        txtMaGiamGia.setText("");
        maGiamGiaDangAp = null;
        lblTrangThaiMaGG.setText("");
        panelHienThiGiamGia.setVisible(false);
        tinhLaiTongTien();
    }

    private void tinhLaiTongTien() {
        if (maGiamGiaDangAp != null) {
            tienGiamGia = maGiamGiaDangAp.getGiaTriGiamGia();
            thanhTien = Math.max(0, tongTienGoc - tienGiamGia);
            JLabel lblGiaTriGG = (JLabel) panelHienThiGiamGia.getClientProperty("lblGiaTriGG");
            if (lblGiaTriGG != null) lblGiaTriGG.setText("- " + formatTien.format(tienGiamGia));
        } else {
            tienGiamGia = 0;
            if (thanhTien == 0 && tongTienGoc > 0) thanhTien = tongTienGoc;
        }
        lblTongTien.setText(formatTien.format(tongTienGoc));
        lblThanhTien.setText(formatTien.format(thanhTien));
    }

    private JPanel taoContainerPhuongThuc(String type, String text, String moTa) {
        JPanel container = new JPanel(new BorderLayout(15, 0));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        container.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        container.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblCheck = new JLabel("[OK]");
        lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblCheck.setForeground(new Color(76, 175, 80));
        lblCheck.setVisible(false);
        container.add(lblCheck, BorderLayout.WEST);

        if ("TIENMAT".equals(type)) checkTienMat = lblCheck;
        else checkChuyenKhoan = lblCheck;

        JPanel panelText = new JPanel();
        panelText.setLayout(new BoxLayout(panelText, BoxLayout.Y_AXIS));
        panelText.setBackground(Color.WHITE);

        JLabel lblText = new JLabel(text);
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

        JLabel lblStatus = new JLabel("ĐÃ THANH TOÁN");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(mauXanhLa);
        lblStatus.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        lblStatus.setVisible(false);
        panelText.add(Box.createRigidArea(new Dimension(0, 3)));
        panelText.add(lblStatus);

        if ("TIENMAT".equals(type)) {
            panelTextTienMat = panelText;
            lblStatusTienMat = lblStatus;
        } else {
            panelTextChuyenKhoan = panelText;
            lblStatusChuyenKhoan = lblStatus;
        }
        container.add(panelText, BorderLayout.CENTER);

        container.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (container.isEnabled()) {
                    container.setBackground(mauXanhNhat);
                    panelText.setBackground(mauXanhNhat);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                Color bgColor = (Color) container.getClientProperty("originalBg");
                if (bgColor == null) bgColor = Color.WHITE;
                container.setBackground(bgColor);
                panelText.setBackground(bgColor);
            }
        });
        container.putClientProperty("originalBg", Color.WHITE);
        return container;
    }

    private JButton taoNutInHoaDon() {
        JButton btn = new JButton("IN HÓA ĐƠN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? mauHongChinh : new Color(189, 189, 189));
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

    public void hienThiHoaDon(ThongTinHoaDonDTO hoaDon) {
        if (hoaDon == null) {
            JOptionPane.showMessageDialog(this, "Dữ liệu hóa đơn trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.hoaDonHienTai = hoaDon;
        this.tongTienGoc = hoaDon.getTongTien();
        this.thanhTien = (hoaDon.getThanhTien() > 0) ? hoaDon.getThanhTien() : tongTienGoc;
        if (this.tongTienGoc == 0 && this.thanhTien > 0) this.tongTienGoc = this.thanhTien;

        lblMaHD.setText(hoaDon.getMaHoaDon() != null ? hoaDon.getMaHoaDon() : "-");
        lblTenKhachHang.setText(hoaDon.getHoTenKH() != null ? hoaDon.getHoTenKH() : "-");
        lblTenPhong.setText(hoaDon.getTenKhongGian() != null ? hoaDon.getTenKhongGian() : "-");
        lblThoiGian.setText(hoaDon.getThoiGianSửDung() != null ? hoaDon.getThoiGianSửDung() : "-");
        lblTongGio.setText(String.format("%.1f giờ", hoaDon.getTongSoGio()));

        panelChiTietHD.removeAll();
        panelChiTietHD.add(taoThongTinRow("Mã hóa đơn:", hoaDon.getMaHoaDon(), true));
        panelChiTietHD.add(taoKhoangCach(10));
        panelChiTietHD.add(taoThongTinRow("Khách hàng:", hoaDon.getHoTenKH(), false));
        panelChiTietHD.add(taoKhoangCach(10));
        panelChiTietHD.add(taoThongTinRow("Không gian:", hoaDon.getTenKhongGian(), false));
        panelChiTietHD.add(taoKhoangCach(10));
        panelChiTietHD.add(taoThongTinRow("Thời gian:", hoaDon.getThoiGianSửDung(), false));
        panelChiTietHD.add(taoKhoangCach(20));
        panelChiTietHD.add(taoDuongPhanCach());
        panelChiTietHD.add(taoKhoangCach(20));

        JLabel lblDichVu = new JLabel("DỊCH VỤ SỬ DỤNG");
        lblDichVu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDichVu.setForeground(mauHongChinh);
        lblDichVu.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelChiTietHD.add(lblDichVu);
        panelChiTietHD.add(taoKhoangCach(15));

        if (hoaDon.getDanhSachDichVu() != null && !hoaDon.getDanhSachDichVu().isEmpty()) {
            for (DichVuDaDungDTO dv : hoaDon.getDanhSachDichVu()) {
                panelChiTietHD.add(taoDichVuRow(dv.getTenDichVu(), "x" + dv.getSoLuong(), dv.getThanhTien()));
                panelChiTietHD.add(taoKhoangCach(8));
            }
        } else {
            JLabel lblKhongCoDV = new JLabel("Không có dịch vụ");
            lblKhongCoDV.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblKhongCoDV.setForeground(mauXamNhat);
            lblKhongCoDV.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            panelChiTietHD.add(lblKhongCoDV);
        }

        tinhLaiTongTien();
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
        if (hoaDonHienTai == null) return;
        TienMatForm dialog = new TienMatForm(JOptionPane.getFrameForComponent(this), true, thanhTien);
        dialog.setVisible(true);
        if (dialog.isDaThanhToan()) {
            daDongTienMat = true;
            capNhatTrangThaiThanhToan();
            xuLyThanhToan("Tiền mặt");
        }
    }

    private void moManHinhChuyenKhoan() {
        if (hoaDonHienTai == null) return;
        ChuyenKhoanForm dialog = new ChuyenKhoanForm(JOptionPane.getFrameForComponent(this), true, thanhTien, hoaDonHienTai.getMaHoaDon());
        dialog.setVisible(true);
        if (dialog.isDaThanhToan()) {
            daDongChuyenKhoan = true;
            capNhatTrangThaiThanhToan();
            xuLyThanhToan("Chuyển khoản");
        }
    }

    private void xuLyThanhToan(String phuongThuc) {
        String maPGG = maGiamGiaDangAp != null ? maGiamGiaDangAp.getMaPGG() : null;
        if (thanhToanController.xacNhanThanhToan(hoaDonHienTai.getMaHoaDon(), phuongThuc, maPGG)) {
            JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật thanh toán", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatTrangThaiThanhToan() {
        if (daDongTienMat && checkTienMat != null) {
            checkTienMat.setVisible(true);
            disablePaymentMethod(containerTienMat, true);
            disablePaymentMethod(containerChuyenKhoan, false);
        }
        if (daDongChuyenKhoan && checkChuyenKhoan != null) {
            checkChuyenKhoan.setVisible(true);
            disablePaymentMethod(containerChuyenKhoan, true);
            disablePaymentMethod(containerTienMat, false);
        }
        if (daDongTienMat || daDongChuyenKhoan) nutInHoaDon.setEnabled(true);
    }

    private void disablePaymentMethod(JPanel container, boolean isCompleted) {
        if (container == null) return;
        Color bgColor = isCompleted ? mauXanhNhat : new Color(240, 240, 240);
        Color textColor = isCompleted ? new Color(50, 120, 50) : new Color(150, 150, 150);
        container.setBackground(bgColor);
        container.putClientProperty("originalBg", bgColor);

        JPanel panelText = (container == containerTienMat) ? panelTextTienMat : panelTextChuyenKhoan;
        JLabel lblStatus = (container == containerTienMat) ? lblStatusTienMat : lblStatusChuyenKhoan;
        if (panelText != null) {
            panelText.setBackground(bgColor);
            if (isCompleted && lblStatus != null) lblStatus.setVisible(true);
            for (Component comp : panelText.getComponents()) {
                if (comp instanceof JLabel) ((JLabel) comp).setForeground(textColor);
            }
        }
        container.setEnabled(false);
    }

    private void inHoaDon() {
        JOptionPane.showMessageDialog(this, "Chức năng in hóa đơn đang được phát triển", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initializeLabels() {
        lblMaHD = new JLabel("-");
        lblTenKhachHang = new JLabel("-");
        lblTenPhong = new JLabel("-");
        lblThoiGian = new JLabel("-");
        lblTongGio = new JLabel("-");
    }
}





