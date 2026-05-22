package com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.ChuyenKhoan;

import com.wms.util.ChuyenKhoanQrUtil;
import com.wms.util.MaQRUtil;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class ChuyenKhoanForm extends JDialog {

    private final double tongTien;
    private final String maHoaDon;
    private boolean daThanhToan = false;
    private final NumberFormat formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ChuyenKhoanForm(Frame parent, boolean modal, double tongTien, String maHoaDon) {
        super(parent, modal);
        this.tongTien = tongTien;
        this.maHoaDon = maHoaDon;
        initComponents();
        txtTongTien.setText(formatTien.format(tongTien));
        txtNganHang.setText(ChuyenKhoanQrUtil.TEN_NGAN_HANG_NHAN);
        txtSoTK.setText(ChuyenKhoanQrUtil.SO_TAI_KHOAN_NHAN);
        txtChuTK.setText(ChuyenKhoanQrUtil.CHU_TAI_KHOAN_NHAN);
        String noiDungChuyenKhoan = ChuyenKhoanQrUtil.taoNoiDungHoaDon(maHoaDon);
        txtNoiDung.setText(noiDungChuyenKhoan);
        loadQRCode(noiDungChuyenKhoan);
        pack();
        setLocationRelativeTo(parent);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnMain = new JPanel();
        pnHeader = new JPanel();
        lblHeaderTitle = new JLabel();
        lblSub = new JLabel();
        pnContent = new JPanel();
        lblTongTienTitle = new JLabel();
        txtTongTien = new JTextField();
        lblQRCode = new JLabel();
        lblHD = new JLabel();
        pnInfo = new JPanel();
        lblNganHang = new JLabel();
        txtNganHang = new JLabel();
        lblSoTK = new JLabel();
        txtSoTK = new JLabel();
        lblChuTK = new JLabel();
        txtChuTK = new JLabel();
        lblNoiDung = new JLabel();
        txtNoiDung = new JLabel();
        btnHuy = new JButton();
        btnXacNhan = new JButton();

        setPreferredSize(new Dimension(560, 720));
        pnMain.setBackground(new Color(254, 248, 250));
        pnMain.setLayout(null);

        pnHeader.setBackground(new Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new Font("Segoe UI", 1, 20));
        lblHeaderTitle.setForeground(Color.WHITE);
        lblHeaderTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblHeaderTitle.setText("CHUYỂN KHOẢN NGÂN HÀNG");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 8, 560, 30);

        lblSub.setFont(new Font("Segoe UI", 0, 13));
        lblSub.setForeground(Color.WHITE);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setText("Quét mã QR để thanh toán");
        pnHeader.add(lblSub);
        lblSub.setBounds(0, 35, 560, 20);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 560, 65);

        pnContent.setBackground(Color.WHITE);
        pnContent.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, new Color(235, 94, 141)));
        pnContent.setLayout(null);

        lblTongTienTitle.setFont(new Font("Segoe UI", 1, 16));
        lblTongTienTitle.setForeground(new Color(48, 30, 35));
        lblTongTienTitle.setText("Số tiền thanh toán:");
        pnContent.add(lblTongTienTitle);
        lblTongTienTitle.setBounds(25, 25, 200, 30);

        txtTongTien.setEditable(false);
        txtTongTien.setFont(new Font("Segoe UI", 1, 24));
        txtTongTien.setForeground(new Color(235, 94, 141));
        txtTongTien.setHorizontalAlignment(JTextField.TRAILING);
        txtTongTien.setBorder(null);
        txtTongTien.setText("0 VNĐ");
        pnContent.add(txtTongTien);
        txtTongTien.setBounds(200, 20, 275, 40);

        lblQRCode.setHorizontalAlignment(SwingConstants.CENTER);
        lblQRCode.setText("[Khung mã QR của bạn ở đây]");
        lblQRCode.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));
        pnContent.add(lblQRCode);
        lblQRCode.setBounds(125, 70, 250, 250);

        lblHD.setFont(new Font("Segoe UI", 2, 13));
        lblHD.setForeground(new Color(136, 136, 136));
        lblHD.setHorizontalAlignment(SwingConstants.CENTER);
        lblHD.setText("Mở ứng dụng ngân hàng và quét mã để tiến hành thanh toán");
        pnContent.add(lblHD);
        lblHD.setBounds(0, 335, 500, 20);

        pnInfo.setBackground(new Color(227, 242, 253));
        pnInfo.setLayout(null);

        lblNganHang.setFont(new Font("Segoe UI", 0, 14));
        lblNganHang.setForeground(new Color(136, 136, 136));
        lblNganHang.setText("Ngân hàng:");
        pnInfo.add(lblNganHang);
        lblNganHang.setBounds(20, 15, 120, 20);

        txtNganHang.setFont(new Font("Segoe UI", 1, 14));
        txtNganHang.setForeground(new Color(48, 30, 35));
        txtNganHang.setHorizontalAlignment(SwingConstants.TRAILING);
        txtNganHang.setText("Vietcombank");
        pnInfo.add(txtNganHang);
        txtNganHang.setBounds(150, 15, 280, 20);

        lblSoTK.setFont(new Font("Segoe UI", 0, 14));
        lblSoTK.setForeground(new Color(136, 136, 136));
        lblSoTK.setText("Số tài khoản:");
        pnInfo.add(lblSoTK);
        lblSoTK.setBounds(20, 45, 120, 20);

        txtSoTK.setFont(new Font("Segoe UI", 1, 14));
        txtSoTK.setForeground(new Color(48, 30, 35));
        txtSoTK.setHorizontalAlignment(SwingConstants.TRAILING);
        txtSoTK.setText("9375037830");
        pnInfo.add(txtSoTK);
        txtSoTK.setBounds(150, 45, 280, 20);

        lblChuTK.setFont(new Font("Segoe UI", 0, 14));
        lblChuTK.setForeground(new Color(136, 136, 136));
        lblChuTK.setText("Chủ tài khoản:");
        pnInfo.add(lblChuTK);
        lblChuTK.setBounds(20, 75, 120, 20);

        txtChuTK.setFont(new Font("Segoe UI", 1, 14));
        txtChuTK.setForeground(new Color(48, 30, 35));
        txtChuTK.setHorizontalAlignment(SwingConstants.TRAILING);
        txtChuTK.setText("LAI MOC HUY");
        pnInfo.add(txtChuTK);
        txtChuTK.setBounds(150, 75, 280, 20);

        lblNoiDung.setFont(new Font("Segoe UI", 0, 14));
        lblNoiDung.setForeground(new Color(136, 136, 136));
        lblNoiDung.setText("Nội dung CK:");
        pnInfo.add(lblNoiDung);
        lblNoiDung.setBounds(20, 105, 120, 20);

        txtNoiDung.setFont(new Font("Segoe UI", 1, 14));
        txtNoiDung.setForeground(new Color(21, 101, 192));
        txtNoiDung.setHorizontalAlignment(SwingConstants.TRAILING);
        txtNoiDung.setText("UIT CW [MA_HOA_DON]");
        pnInfo.add(txtNoiDung);
        txtNoiDung.setBounds(150, 105, 280, 20);

        pnContent.add(pnInfo);
        pnInfo.setBounds(25, 365, 450, 140);

        btnHuy.setBackground(new Color(220, 53, 69));
        btnHuy.setFont(new Font("Segoe UI", 1, 15));
        btnHuy.setForeground(Color.WHITE);
        btnHuy.setText("Hủy bỏ");
        btnHuy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHuyActionPerformed(evt);
            }
        });
        pnContent.add(btnHuy);
        btnHuy.setBounds(25, 520, 130, 45);

        btnXacNhan.setBackground(new Color(21, 101, 192));
        btnXacNhan.setFont(new Font("Segoe UI", 1, 16));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setText("XÁC NHẬN ĐÃ NHẬN TIỀN");
        btnXacNhan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXacNhanActionPerformed(evt);
            }
        });
        pnContent.add(btnXacNhan);
        btnXacNhan.setBounds(165, 520, 310, 45);

        pnMain.add(pnContent);
        pnContent.setBounds(30, 85, 500, 580);

        getContentPane().add(pnMain, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHuyActionPerformed
        daThanhToan = false;
        dispose();
    }//GEN-LAST:event_btnHuyActionPerformed

    private void btnXacNhanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXacNhanActionPerformed
        daThanhToan = true;
        dispose();
    }//GEN-LAST:event_btnXacNhanActionPerformed

    public boolean isDaThanhToan() {
        return daThanhToan;
    }

    private void loadQRCode(String noiDungChuyenKhoan) {
        lblQRCode.setIcon(null);
        lblQRCode.setText("Đang tạo mã QR...");
        BigDecimal soTien = BigDecimal.valueOf(tongTien);
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                Image qrImage = taiAnhVietQr(soTien, noiDungChuyenKhoan);
                if (qrImage == null) {
                    qrImage = taoAnhQrDuPhong(soTien, noiDungChuyenKhoan);
                }
                return qrImage == null ? null : taoIconQr(qrImage);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon == null) {
                        lblQRCode.setText("Không tạo được QR");
                        return;
                    }
                    lblQRCode.setIcon(icon);
                    lblQRCode.setText("");
                } catch (Exception ex) {
                    lblQRCode.setText("Lỗi tạo QR");
                }
            }
        }.execute();
    }

    private Image taiAnhVietQr(BigDecimal soTien, String noiDungChuyenKhoan) {
        try {
            String qrUrl = ChuyenKhoanQrUtil.taoVietQrUrl(soTien, noiDungChuyenKhoan);
            return ImageIO.read(new URL(qrUrl));
        } catch (Exception ex) {
            return null;
        }
    }

    private Image taoAnhQrDuPhong(BigDecimal soTien, String noiDungChuyenKhoan) {
        try {
            String payload = ChuyenKhoanQrUtil.taoNoiDungQrDuPhong(soTien, noiDungChuyenKhoan);
            byte[] bytes = MaQRUtil.taoAnhPng(payload, 250);
            return bytes.length == 0 ? null : ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Exception ex) {
            return null;
        }
    }

    private ImageIcon taoIconQr(Image image) {
        int width = Math.max(250, lblQRCode.getWidth());
        int height = Math.max(250, lblQRCode.getHeight());
        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnXacNhan;
    private javax.swing.JLabel lblChuTK;
    private javax.swing.JLabel lblHD;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblNganHang;
    private javax.swing.JLabel lblNoiDung;
    private javax.swing.JLabel lblQRCode;
    private javax.swing.JLabel lblSoTK;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTongTienTitle;
    private javax.swing.JPanel pnContent;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnInfo;
    private javax.swing.JPanel pnMain;
    private javax.swing.JLabel txtChuTK;
    private javax.swing.JLabel txtNganHang;
    private javax.swing.JLabel txtNoiDung;
    private javax.swing.JLabel txtSoTK;
    private javax.swing.JTextField txtTongTien;
    // End of variables declaration//GEN-END:variables
}
