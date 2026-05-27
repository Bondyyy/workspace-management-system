package com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan;

import com.wms.controller.TrangChuQuanLy.QuanLyHoaDon.ThanhToanController;
import com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.TienMat.TienMatForm;
import com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.ChuyenKhoan.ChuyenKhoanForm;
import com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.XacNhanPhieuGiamGiaDTO;
import com.wms.util.HoaDonGiamGiaUtil;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;

public class ThanhToanHoaDonForm extends JPanel {

    private final Color mauNenChinh = Color.decode("#FAFAFA");
    private final Color mauHongChinh = new java.awt.Color(235, 94, 141);
    private final Color mauHongNhat = Color.decode("#FCE4EC");
    private final Color mauXamDam = Color.decode("#212529");
    private final Color mauXamNhat = Color.decode("#757575");
    private final Color mauXanhNhat = Color.decode("#E3F2FD");
    private final Color mauXanhLa = Color.decode("#4CAF50");
    private final Color mauDo = new java.awt.Color(255, 82, 82);

    private JPanel panelChiTietHD;
    private JLabel lblTongTien, lblThanhTien;
    private JPanel containerTienMat, containerChuyenKhoan;
    private JLabel checkTienMat, checkChuyenKhoan;
    private JButton nutInHoaDon, nutHoanTatHoaDon;
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
    private boolean dangXuLyThanhToan = false;
    private boolean dangExportHoaDon = false;
    private boolean daThanhToanThanhCong = false;

    private final ThanhToanController thanhToanController = new ThanhToanController();
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
        lblTrangThaiMaGG.setText("Đang tải hóa đơn...");
        lblTrangThaiMaGG.setForeground(mauXamNhat);
        long start = System.currentTimeMillis();
        new SwingWorker<ThongTinHoaDonDTO, Void>() {
            @Override
            protected ThongTinHoaDonDTO doInBackground() {
                return thanhToanController.loadDuLieuHoaDon(maHoaDon);
            }

            @Override
            protected void done() {
                try {
                    ThongTinHoaDonDTO hoaDon = get();
                    if (hoaDon != null) {
                        hienThiHoaDon(hoaDon);
                        lblTrangThaiMaGG.setText("");
                    } else {
                        JOptionPane.showMessageDialog(ThanhToanHoaDonForm.this, "Không tìm thấy hóa đơn: " + maHoaDon, "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    System.out.println("[ThanhToanHoaDonForm] load hoa don mat "
                            + (System.currentTimeMillis() - start) + " ms");
                } catch (Exception ex) {
                    lblTrangThaiMaGG.setText("");
                    com.wms.util.MessageUtil.showError(ThanhToanHoaDonForm.this, "Lỗi tải hóa đơn.", ex);
                }
            }
        }.execute();
    }

    public String getMaHoaDonHienTai() {
        return maHoaDonHienTai;
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(0, 20));
        setBackground(mauNenChinh);
        setPreferredSize(new Dimension(980, 600));
        setBorder(new EmptyBorder(20, 28, 20, 28));

        initializeLabels();

        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(mauNenChinh);
        JLabel lblTieuDe = new JLabel("THANH TOÁN HÓA ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 26));
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
                new EmptyBorder(25, 25, 25, 25)));

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

        JLabel lblTextTong = new JLabel("Tổng cộng:");
        lblTextTong.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTextTong.setForeground(mauXamDam);
        panelTong.add(lblTextTong, BorderLayout.WEST);

        lblTongTien = new JLabel("0 VNĐ");
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

        JPanel panelThanhTien = new JPanel(new GridBagLayout());
        panelThanhTien.setBackground(mauHongNhat);
        panelThanhTien.setBorder(new EmptyBorder(12, 14, 12, 14));
        panelThanhTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panelThanhTien.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel lblTextThanhTien = new JLabel("TỔNG TIỀN:");
        lblTextThanhTien.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTextThanhTien.setForeground(mauXamDam);

        lblThanhTien = new JLabel("0 VNĐ");
        lblThanhTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblThanhTien.setForeground(mauHongChinh);
        lblThanhTien.setHorizontalAlignment(SwingConstants.RIGHT);

        GridBagConstraints gbcText = new GridBagConstraints();
        gbcText.gridx = 0;
        gbcText.gridy = 0;
        gbcText.weightx = 0;
        gbcText.insets = new Insets(0, 0, 0, 10);
        gbcText.anchor = GridBagConstraints.WEST;
        gbcText.fill = GridBagConstraints.NONE;
        panelThanhTien.add(lblTextThanhTien, gbcText);

        GridBagConstraints gbcMoney = new GridBagConstraints();
        gbcMoney.gridx = 1;
        gbcMoney.gridy = 0;
        gbcMoney.weightx = 1;
        gbcMoney.anchor = GridBagConstraints.EAST;
        gbcMoney.fill = GridBagConstraints.HORIZONTAL;
        panelThanhTien.add(lblThanhTien, gbcMoney);

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
                new EmptyBorder(15, 15, 15, 15)));
        panelMaGG.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        panelMaGG.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel lblMaGG = new JLabel("Mã giảm giá (nếu có):");
        lblMaGG.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaGG.setForeground(mauXamDam);
        lblMaGG.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelMaGG.add(lblMaGG);
        panelMaGG.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel panelInputGG = new JPanel(new BorderLayout(10, 0));
        panelInputGG.setBackground(Color.WHITE);
        panelInputGG.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        panelInputGG.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        txtMaGiamGia = new JTextField();
        txtMaGiamGia.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtMaGiamGia.setForeground(mauXamDam);
        txtMaGiamGia.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(10, 12, 10, 12)));
        txtMaGiamGia.addActionListener(e -> kiemTraMaGiamGia());

        JButton btnApDung = new JButton("Áp dụng");
        btnApDung.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnApDung.setForeground(Color.WHITE);
        btnApDung.setBackground(mauDo);
        btnApDung.setFocusPainted(false);
        btnApDung.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApDung.addActionListener(e -> kiemTraMaGiamGia());
        
        panelInputGG.add(txtMaGiamGia, BorderLayout.CENTER);
        panelInputGG.add(btnApDung, BorderLayout.EAST);

        panelMaGG.add(panelInputGG);
        panelMaGG.add(Box.createRigidArea(new Dimension(0, 6)));

        lblTrangThaiMaGG = new JLabel("");
        lblTrangThaiMaGG.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTrangThaiMaGG.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panelMaGG.add(lblTrangThaiMaGG);

        panel.add(panelMaGG);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        containerTienMat = taoContainerPhuongThuc("TIENMAT", "TIỀN MẶT", "Thanh toán bằng tiền mặt tại quầy");
        containerTienMat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!dangXuLyThanhToan && !daThanhToanThanhCong && containerTienMat.isEnabled()) {
                    moManHinhTienMat();
                } else if (daThanhToanThanhCong) {
                    JOptionPane.showMessageDialog(ThanhToanHoaDonForm.this, "Hóa đơn đã thanh toán, không thể chọn phương thức khác.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        panel.add(containerTienMat);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        containerChuyenKhoan = taoContainerPhuongThuc("CHUYENKHOAN", "CHUYỂN KHOẢN", "Chuyển khoản qua ngân hàng");
        containerChuyenKhoan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!dangXuLyThanhToan && !daThanhToanThanhCong && containerChuyenKhoan.isEnabled()) {
                    moManHinhChuyenKhoan();
                } else if (daThanhToanThanhCong) {
                    JOptionPane.showMessageDialog(ThanhToanHoaDonForm.this, "Hóa đơn đã thanh toán, không thể chọn phương thức khác.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        panel.add(containerChuyenKhoan);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(220, 220, 220));
        sep.setAlignmentX(JSeparator.LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        nutHoanTatHoaDon = taoNutHoanTatHoaDon();
        nutHoanTatHoaDon.setVisible(false);
        nutHoanTatHoaDon.setEnabled(false);
        nutHoanTatHoaDon.addActionListener(e -> xuLyThanhToan("Đặt trước"));
        panel.add(nutHoanTatHoaDon);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        nutInHoaDon = taoNutInHoaDon();
        nutInHoaDon.setEnabled(false);
        nutInHoaDon.addActionListener(e -> inHoaDon());
        panel.add(nutInHoaDon);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JButton taoNutHoanTatHoaDon() {
        JButton btn = taoNutInHoaDon();
        btn.setText("HOÀN TẤT HÓA ĐƠN ĐÃ TRẢ TRƯỚC");
        return btn;
    }

    private void kiemTraMaGiamGia() {
        String maVoucher = txtMaGiamGia.getText().trim();
        if (maVoucher.isEmpty()) {
            if (maGiamGiaDangAp != null)
                xoaMaGiamGia();
            lblTrangThaiMaGG.setText("");
            return;
        }

        double tienCoTheApVoucher = layTienCoTheApVoucherTaiQuay();
        XacNhanPhieuGiamGiaDTO validationResult = thanhToanController.kiemTraPhieuGiamGia(maVoucher, tienCoTheApVoucher);
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
        if (lblMaGG != null)
            lblMaGG.setText(maGiamGiaDangAp.getMaPGG());

        tienGiamGia = validationResult.getDiscountAmount();
        JLabel lblGiaTriGG = (JLabel) panelHienThiGiamGia.getClientProperty("lblGiaTriGG");
        if (lblGiaTriGG != null)
            lblGiaTriGG.setText(HoaDonGiamGiaUtil.formatTienGiamVnd(tienGiamGia));

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
        double tienCoTheApVoucher = layTienCoTheApVoucherTaiQuay();
        if (maGiamGiaDangAp != null) {
            tienGiamGia = Math.min(Math.max(0, maGiamGiaDangAp.getGiaTriGiamGia()), tienCoTheApVoucher);
            double phanTramHang = hoaDonHienTai != null
                    ? Math.max(0, hoaDonHienTai.getPhanTramGiamHangTVTaiQuay())
                    : 0;
            if (phanTramHang <= 0 && hoaDonHienTai != null) {
                phanTramHang = Math.max(0, hoaDonHienTai.getPhanTramGiamHangThanhVien());
            }
            double tienGiamHang = Math.round(Math.max(0, tienCoTheApVoucher - tienGiamGia)
                    * Math.min(100, phanTramHang) / 100.0);
            thanhTien = Math.max(0, tienCoTheApVoucher - tienGiamGia - tienGiamHang);
            JLabel lblGiaTriGG = (JLabel) panelHienThiGiamGia.getClientProperty("lblGiaTriGG");
            if (lblGiaTriGG != null)
                lblGiaTriGG.setText(HoaDonGiamGiaUtil.formatTienGiamVnd(tienGiamGia));
        } else {
            tienGiamGia = 0;
            thanhTien = hoaDonHienTai != null
                    ? Math.max(0, hoaDonHienTai.getThanhTien())
                    : Math.max(0, tongTienGoc);
        }
        lblTongTien.setText(formatTienVnd(tongTienGoc));
        lblThanhTien.setText(formatTienVnd(tongTienGoc));
        capNhatKhaNangThanhToan();
    }

    private double layTienCoTheApVoucherTaiQuay() {
        if (hoaDonHienTai == null) {
            return Math.max(0, tongTienGoc);
        }
        double tienPhatSinh = Math.max(0, hoaDonHienTai.getTienGocPhatSinh());
        if (hoaDonHienTai.getSoTienDaTraTruoc() > 0 || hoaDonHienTai.getTienGocDatTruoc() > 0) {
            return tienPhatSinh;
        }
        return Math.max(0, tongTienGoc);
    }

    private JPanel taoContainerPhuongThuc(String type, String text, String moTa) {
        JPanel container = new JPanel(new BorderLayout(15, 0));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 20, 20, 20)));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        container.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        container.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblCheck = new JLabel("[OK]");
        lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblCheck.setForeground(new Color(76, 175, 80));
        lblCheck.setVisible(false);
        container.add(lblCheck, BorderLayout.WEST);

        if ("TIENMAT".equals(type))
            checkTienMat = lblCheck;
        else
            checkChuyenKhoan = lblCheck;

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
            @Override
            public void mouseEntered(MouseEvent e) {
                if (container.isEnabled()) {
                    container.setBackground(mauXanhNhat);
                    panelText.setBackground(mauXanhNhat);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Color bgColor = (Color) container.getClientProperty("originalBg");
                if (bgColor == null)
                    bgColor = Color.WHITE;
                container.setBackground(bgColor);
                panelText.setBackground(bgColor);
            }
        });
        container.putClientProperty("originalBg", Color.WHITE);
        return container;
    }

    private JButton taoNutInHoaDon() {
        JButton btn = new JButton("XUẤT HOÁ ĐƠN") {
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
        this.tongTienGoc = hoaDon.getTongTienGoc() > 0 ? hoaDon.getTongTienGoc() : hoaDon.getTongTien();
        this.thanhTien = Math.max(0, hoaDon.getThanhTien());
        if (this.tongTienGoc <= 0 && this.thanhTien > 0)
            this.tongTienGoc = this.thanhTien;
        this.daThanhToanThanhCong = "Đã thanh toán thành công".equals(hoaDon.getTrangThaiThanhToan());
        this.daDongTienMat = daThanhToanThanhCong && "Tiền mặt".equals(hoaDon.getPhuongThucThanhToan());
        this.daDongChuyenKhoan = daThanhToanThanhCong && "Chuyển khoản".equals(hoaDon.getPhuongThucThanhToan());

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
                String hienThiSL = dv.getTenDichVu().startsWith("Thuê") ? dv.getSoLuong() + " giờ" : "x" + dv.getSoLuong();
                panelChiTietHD.add(taoDichVuRow(dv.getTenDichVu(), hienThiSL, dv.getThanhTien()));
                panelChiTietHD.add(taoKhoangCach(8));
            }
        } else {
            JLabel lblKhongCoDV = new JLabel("Không có dịch vụ");
            lblKhongCoDV.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblKhongCoDV.setForeground(mauXamNhat);
            lblKhongCoDV.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            panelChiTietHD.add(lblKhongCoDV);
        }

        themDongTongKetHoaDon(hoaDon);

        tinhLaiTongTien();
        panelChiTietHD.revalidate();
        panelChiTietHD.repaint();
    }

    private void themDongTongKetHoaDon(ThongTinHoaDonDTO hoaDon) {
        HoaDonGiamGiaUtil.ThongTinGiamGia giamGia = HoaDonGiamGiaUtil.taoThongTinGiamGia(hoaDon, tienGiamGia);
        panelChiTietHD.add(taoKhoangCach(15));
        panelChiTietHD.add(taoDuongPhanCach());
        panelChiTietHD.add(taoKhoangCach(15));
        panelChiTietHD.add(taoThongTinRow("Tổng tiền gốc:", formatTienVnd(tongTienGoc), true));

        if (hoaDon.getTienGocDatTruoc() > 0 || hoaDon.getSoTienDaTraTruoc() > 0) {
            panelChiTietHD.add(taoKhoangCach(8));
            panelChiTietHD.add(taoThongTinRow("Gốc đặt trước:", formatTienVnd(hoaDon.getTienGocDatTruoc()), false));
            if (giamGia.getTienGiamVoucherDatTruoc() > 0) {
                panelChiTietHD.add(taoThongTinRow(giamGia.getNhanVoucherDatTruoc() + ":",
                        HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTienGiamVoucherDatTruoc()), false));
            }
            if (giamGia.getTienGiamHangDatTruoc() > 0) {
                panelChiTietHD.add(taoThongTinRow(giamGia.getNhanHangDatTruoc() + ":",
                        HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTienGiamHangDatTruoc()), false));
            }
            if (giamGia.getTongGiamDatTruoc() > 0) {
                panelChiTietHD.add(taoThongTinRow("Tổng giảm đặt trước:",
                        HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTongGiamDatTruoc()), true));
            }
            JPanel pnlTraTruoc = taoThongTinRow("Đã trả trước:", formatTienVnd(hoaDon.getSoTienDaTraTruoc()), true);
            JLabel lblTraTruoc = (JLabel) pnlTraTruoc.getComponent(1);
            if (lblTraTruoc != null) {
                lblTraTruoc.setForeground(mauXanhLa);
            }
            panelChiTietHD.add(pnlTraTruoc);
        }

        if (hoaDon.getTienGocDatTruoc() > 0 || hoaDon.getTienGocPhatSinh() > 0) {
            panelChiTietHD.add(taoKhoangCach(8));
            panelChiTietHD.add(taoThongTinRow("Phát sinh tại quán:", formatTienVnd(hoaDon.getTienGocPhatSinh()), false));
            if (giamGia.getTienGiamVoucherTaiQuay() > 0) {
                panelChiTietHD.add(taoThongTinRow(giamGia.getNhanVoucherTaiQuay() + ":",
                        HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTienGiamVoucherTaiQuay()), false));
            }
            if (giamGia.getTienGiamHangTaiQuay() > 0) {
                panelChiTietHD.add(taoThongTinRow(giamGia.getNhanHangTaiQuay() + ":",
                        HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTienGiamHangTaiQuay()), false));
            }
        }

        if (giamGia.coTongGiam()) {
            panelChiTietHD.add(taoThongTinRow("Tổng giảm:", HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTongTienGiam()), true));
        }
        if (hoaDon.getSoTienThanhToanTaiQuay() > 0) {
            panelChiTietHD.add(taoThongTinRow("Đã thanh toán tại quầy:",
                    formatTienVnd(hoaDon.getSoTienThanhToanTaiQuay()), true));
        }
    }

    private void capNhatKhaNangThanhToan() {
        boolean coTheThuTien = hoaDonHienTai != null
                && thanhTien > 0
                && !"Đã thanh toán thành công".equals(hoaDonHienTai.getTrangThaiThanhToan());
        boolean coTheHoanTatTraTruoc = hoaDonHienTai != null
                && thanhTien <= 0
                && hoaDonHienTai.getSoTienDaTraTruoc() > 0
                && !"Đã thanh toán thành công".equals(hoaDonHienTai.getTrangThaiThanhToan());
        if (containerTienMat != null) {
            containerTienMat.setEnabled(coTheThuTien && !dangXuLyThanhToan);
        }
        if (containerChuyenKhoan != null) {
            containerChuyenKhoan.setEnabled(coTheThuTien && !dangXuLyThanhToan);
        }
        if (txtMaGiamGia != null) {
            txtMaGiamGia.setEnabled(coTheThuTien && !dangXuLyThanhToan);
        }
        if (btnXoaMaGG != null) {
            btnXoaMaGG.setEnabled(coTheThuTien && !dangXuLyThanhToan);
        }
        if (nutHoanTatHoaDon != null) {
            nutHoanTatHoaDon.setVisible(coTheHoanTatTraTruoc);
            nutHoanTatHoaDon.setEnabled(coTheHoanTatTraTruoc && !dangXuLyThanhToan);
        }
        if (nutInHoaDon != null) {
            nutInHoaDon.setEnabled(daThanhToanThanhCong);
        }
        if (!coTheThuTien && lblTrangThaiMaGG != null && !daThanhToanThanhCong) {
            lblTrangThaiMaGG.setText(coTheHoanTatTraTruoc
                    ? "Hóa đơn đã trả trước đủ. Bấm hoàn tất để chốt hóa đơn."
                    : "Không có số tiền cần thanh toán.");
            lblTrangThaiMaGG.setForeground(mauXamNhat);
        }
        capNhatTrangThaiThanhToan();
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

        JLabel lblValue = new JLabel(value == null || value.isBlank() ? "-" : value);
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

        JLabel lblGia = new JLabel(formatTienVnd(gia));
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
        if (hoaDonHienTai == null || "Đã thanh toán thành công".equals(hoaDonHienTai.getTrangThaiThanhToan()))
            return;
        if (thanhTien <= 0) {
            JOptionPane.showMessageDialog(this, "Không có số tiền cần thanh toán.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TienMatForm dialog = new TienMatForm(JOptionPane.getFrameForComponent(this), true, thanhTien);
        dialog.setVisible(true);
        if (dialog.isDaThanhToan()) {
            xuLyThanhToan("Tiền mặt");
        }
    }

    private void moManHinhChuyenKhoan() {
        if (hoaDonHienTai == null || "Đã thanh toán thành công".equals(hoaDonHienTai.getTrangThaiThanhToan()))
            return;
        if (thanhTien <= 0) {
            JOptionPane.showMessageDialog(this, "Không có số tiền cần thanh toán.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ChuyenKhoanForm dialog = new ChuyenKhoanForm(JOptionPane.getFrameForComponent(this), true, thanhTien,
                hoaDonHienTai.getMaHoaDon());
        dialog.setVisible(true);
        if (dialog.isDaThanhToan()) {
            xuLyThanhToan("Chuyển khoản");
        }
    }

    private void xuLyThanhToan(String phuongThuc) {
        if (dangXuLyThanhToan || daThanhToanThanhCong) {
            return;
        }
        dangXuLyThanhToan = true;
        containerTienMat.setEnabled(false);
        containerChuyenKhoan.setEnabled(false);
        if (txtMaGiamGia != null) txtMaGiamGia.setEnabled(false);
        if (btnXoaMaGG != null) btnXoaMaGG.setEnabled(false);
        if (nutHoanTatHoaDon != null) nutHoanTatHoaDon.setEnabled(false);
        if (nutInHoaDon != null) nutInHoaDon.setEnabled(false);
        lblTrangThaiMaGG.setText("Đang xử lý thanh toán...");
        lblTrangThaiMaGG.setForeground(mauXamNhat);

        String maPGG = maGiamGiaDangAp != null ? maGiamGiaDangAp.getMaPGG() : null;
        String maHoaDon = hoaDonHienTai.getMaHoaDon();
        long start = System.currentTimeMillis();

        new SwingWorker<com.wms.model.TrangChuQuanLy.QuanLyHoaDon.KetQuaThanhToanDTO, Void>() {
            @Override
            protected com.wms.model.TrangChuQuanLy.QuanLyHoaDon.KetQuaThanhToanDTO doInBackground() {
                return thanhToanController.thucHienThanhToanMoi(maHoaDon, phuongThuc, maPGG, thanhTien);
            }

            @Override
            protected void done() {
                dangXuLyThanhToan = false;
                try {
                    com.wms.model.TrangChuQuanLy.QuanLyHoaDon.KetQuaThanhToanDTO result = get();
                    String message = com.wms.util.ErrorMessageUtil.toUserMessage(result.getMessage());
                    System.out.println("[ThanhToanHoaDonForm] thanh toan mat "
                            + (System.currentTimeMillis() - start) + " ms");

                    if (result.isSuccess()) {
                        daThanhToanThanhCong = true;
                        if ("Tiền mặt".equals(phuongThuc)) daDongTienMat = true;
                        if ("Chuyển khoản".equals(phuongThuc)) daDongChuyenKhoan = true;

                        lblTrangThaiMaGG.setText("");
                        capNhatTrangThaiThanhToan();
                        loadDuLieuHoaDon(maHoaDon);

                        JOptionPane.showMessageDialog(ThanhToanHoaDonForm.this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        capNhatKhaNangThanhToan();
                        lblTrangThaiMaGG.setText("");
                        JOptionPane.showMessageDialog(ThanhToanHoaDonForm.this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    capNhatKhaNangThanhToan();
                    lblTrangThaiMaGG.setText("");
                    com.wms.util.MessageUtil.showError(ThanhToanHoaDonForm.this, "Lỗi thanh toán.", ex);
                }
            }
        }.execute();
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
        if (daThanhToanThanhCong || daDongTienMat || daDongChuyenKhoan)
            nutInHoaDon.setEnabled(true);
    }

    private void disablePaymentMethod(JPanel container, boolean isCompleted) {
        if (container == null)
            return;
        Color bgColor = isCompleted ? mauXanhNhat : new Color(240, 240, 240);
        Color textColor = isCompleted ? new Color(50, 120, 50) : new Color(150, 150, 150);
        container.setBackground(bgColor);
        container.putClientProperty("originalBg", bgColor);

        JPanel panelText = (container == containerTienMat) ? panelTextTienMat : panelTextChuyenKhoan;
        JLabel lblStatus = (container == containerTienMat) ? lblStatusTienMat : lblStatusChuyenKhoan;
        if (panelText != null) {
            panelText.setBackground(bgColor);
            if (isCompleted && lblStatus != null)
                lblStatus.setVisible(true);
            for (Component comp : panelText.getComponents()) {
                if (comp instanceof JLabel)
                    ((JLabel) comp).setForeground(textColor);
            }
        }
        container.setEnabled(false);
    }

    private void inHoaDon() {
        if (hoaDonHienTai == null || dangExportHoaDon) return;
        Object[] options = {"Bill 80mm", "A4 PDF", "Hủy"};
        int choice = JOptionPane.showOptionDialog(this, "Chọn khổ giấy in hóa đơn:", "Xuất hóa đơn",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice != 0 && choice != 1) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String typeName = choice == 0 ? "80mm" : "A4";
        fileChooser.setDialogTitle("Chọn vị trí lưu file PDF (" + typeName + ")");
        fileChooser.setSelectedFile(new File("HoaDon_" + hoaDonHienTai.getMaHoaDon() + "_" + typeName + ".pdf"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = ensurePdfExtension(fileChooser.getSelectedFile());
        xuatHoaDonAsync(file, choice);
    }

    private void xuatHoaDonAsync(File file, int choice) {
        dangExportHoaDon = true;
        nutInHoaDon.setEnabled(false);
        lblTrangThaiMaGG.setText("Đang xuất hóa đơn...");
        lblTrangThaiMaGG.setForeground(mauXamNhat);
        long start = System.currentTimeMillis();

        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                try {
                    if (choice == 0) {
                        com.wms.util.HoaDonJasperExporter.xuatHoaDon80mmToFile(file, hoaDonHienTai, tienGiamGia);
                    } else {
                        com.wms.util.HoaDonJasperExporter.xuatHoaDonA4ToFile(file, hoaDonHienTai, tienGiamGia);
                    }
                } catch (Exception jasperEx) {
                    System.err.println("[ThanhToanHoaDonForm] Jasper export loi, fallback iText: " + jasperEx.getMessage());
                    com.wms.util.HoaDonPDFExporter.xuatHoaDonPDFToFile(file, hoaDonHienTai, tienGiamGia);
                }
                return file;
            }

            @Override
            protected void done() {
                dangExportHoaDon = false;
                nutInHoaDon.setEnabled(daThanhToanThanhCong);
                lblTrangThaiMaGG.setText("");
                try {
                    File exported = get();
                    System.out.println("[ThanhToanHoaDonForm] export hoa don mat "
                            + (System.currentTimeMillis() - start) + " ms");
                    JOptionPane.showMessageDialog(ThanhToanHoaDonForm.this,
                            "Xuất hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(exported);
                    }
                } catch (Exception ex) {
                    com.wms.util.MessageUtil.showError(ThanhToanHoaDonForm.this, "Lỗi xuất hóa đơn.", ex);
                }
            }
        }.execute();
    }

    private File ensurePdfExtension(File file) {
        if (file.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
            return file;
        }
        return new File(file.getAbsolutePath() + ".pdf");
    }

    private void initializeLabels() {
        lblMaHD = new JLabel("-");
        lblTenKhachHang = new JLabel("-");
        lblTenPhong = new JLabel("-");
        lblThoiGian = new JLabel("-");
        lblTongGio = new JLabel("-");
    }

    private String formatTienVnd(double value) {
        return HoaDonGiamGiaUtil.formatTienVnd(value);
    }
}
