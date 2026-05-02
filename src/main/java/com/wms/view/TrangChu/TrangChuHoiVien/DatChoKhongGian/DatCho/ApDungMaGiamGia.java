package com.wms.view.TrangChu.TrangChuHoiVien.DatChoKhongGian.DatCho;

import com.wms.dao.PhieuGiamGiaDAO;
import com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO;
import java.awt.Frame;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;

public class ApDungMaGiamGia extends javax.swing.JDialog {

    private double tongTienDonHang;
    private PhieuGiamGiaDTO voucherHienTai = null;
    private PhieuGiamGiaDTO voucherChot = null;

    public ApDungMaGiamGia(java.awt.Frame parent, boolean modal, double tongTien) {
        super(parent, modal);
        this.tongTienDonHang = tongTien;
        initComponents();
        this.setLocationRelativeTo(parent);
    }

    public PhieuGiamGiaDTO getVoucherApDung() {
        return voucherChot;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnVoucher = new javax.swing.JPanel();
        lblPromo = new javax.swing.JLabel();
        txtVoucherCode = new javax.swing.JTextField();
        btnCheckCode = new javax.swing.JButton();
        btnApDungNgay = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mã Giảm Giá");
        getContentPane().setLayout(null);

        pnVoucher.setBackground(new java.awt.Color(255, 240, 245));
        pnVoucher.setLayout(null);

        lblPromo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblPromo.setForeground(new java.awt.Color(199, 61, 110));
        lblPromo.setText("NHẬP MÃ PHIẾU GIẢM GIÁ");
        pnVoucher.add(lblPromo);
        lblPromo.setBounds(30, 20, 230, 20);

        txtVoucherCode.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        pnVoucher.add(txtVoucherCode);
        txtVoucherCode.setBounds(30, 60, 390, 50);

        btnCheckCode.setBackground(new java.awt.Color(240, 123, 170));
        btnCheckCode.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCheckCode.setForeground(new java.awt.Color(255, 255, 255));
        btnCheckCode.setText("KIỂM TRA THÔNG TIN PHIẾU GIẢM GIÁ");
        btnCheckCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckCodeActionPerformed(evt);
            }
        });
        pnVoucher.add(btnCheckCode);
        btnCheckCode.setBounds(30, 130, 390, 45);

        btnApDungNgay.setBackground(new java.awt.Color(199, 61, 110));
        btnApDungNgay.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnApDungNgay.setForeground(new java.awt.Color(255, 255, 255));
        btnApDungNgay.setText("ÁP DỤNG NGAY");
        btnApDungNgay.setEnabled(false);
        btnApDungNgay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApDungNgayActionPerformed(evt);
            }
        });
        pnVoucher.add(btnApDungNgay);
        btnApDungNgay.setBounds(30, 190, 390, 45);

        getContentPane().add(pnVoucher);
        pnVoucher.setBounds(0, 0, 450, 260);

        setSize(new java.awt.Dimension(466, 300));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCheckCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckCodeActionPerformed
        String code = txtVoucherCode.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã!");
            return;
        }

        PhieuGiamGiaDAO dao = new PhieuGiamGiaDAO();
        voucherHienTai = dao.layThongTinVoucher(code);

        if (voucherHienTai == null) {
            JOptionPane.showMessageDialog(this, "Mã không tồn tại!");
            btnApDungNgay.setEnabled(false);
            return;
        }

        long now = System.currentTimeMillis();
        if (voucherHienTai.getNgayBatDauApDung().getTime() > now) {
            JOptionPane.showMessageDialog(this, "Mã chưa đến thời gian áp dụng!");
            return;
        }
        if (voucherHienTai.getNgayKetThucApDung().getTime() < now) {
            JOptionPane.showMessageDialog(this, "Mã đã hết hạn!");
            return;
        }
        if (voucherHienTai.getSlDaDung() >= voucherHienTai.getSlToiDa()) {
            JOptionPane.showMessageDialog(this, "Mã đã hết lượt dùng!");
            return;
        }
        if (tongTienDonHang < voucherHienTai.getGiaTriApDungToiThieu()) {
            DecimalFormat df = new DecimalFormat("###,###,###");
            JOptionPane.showMessageDialog(this, "Đơn hàng tối thiểu: " + df.format(voucherHienTai.getGiaTriApDungToiThieu()) + " VNĐ");
            return;
        }

        JOptionPane.showMessageDialog(this, "Hợp lệ! Nhấn Áp dụng để giảm tiền.");
        btnApDungNgay.setEnabled(true);
    }//GEN-LAST:event_btnCheckCodeActionPerformed

    private void btnApDungNgayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApDungNgayActionPerformed
        this.voucherChot = voucherHienTai;
        this.dispose();
    }//GEN-LAST:event_btnApDungNgayActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApDungNgay;
    private javax.swing.JButton btnCheckCode;
    private javax.swing.JLabel lblPromo;
    private javax.swing.JPanel pnVoucher;
    private javax.swing.JTextField txtVoucherCode;
    // End of variables declaration//GEN-END:variables
}