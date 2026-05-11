/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.wms.view.TrangChuGioiThieu.QuenMatKhau;

import javax.swing.JOptionPane;

/**
 *
 * @author Thinkapd T14s
 */
public class QuenMatKhauForm extends javax.swing.JPanel {

    private String currentOTP = "";
    private boolean isOtpVerified = false;

    /**
     * Creates new form QuenMatKhauForm
     */
    public QuenMatKhauForm() {
        initComponents();
        setupListeners();
    }

    private void setupListeners() {
        btnGuiMa.addActionListener(this::btnGuiMaActionPerformed);
        btnXacNhan.addActionListener(this::btnXacNhanActionPerformed);
        btnLuuMatKhau.addActionListener(this::btnLuuMatKhauActionPerformed);
        btnLogin.addActionListener(this::btnLoginActionPerformed);
        btnShowNewPass.addActionListener(e -> toggleShowPassword(txtNewPass, btnShowNewPass));
        btnShowConfirmPass.addActionListener(e -> toggleShowPassword(txtConfirmPass, btnShowConfirmPass));
    }

    private void toggleShowPassword(javax.swing.JPasswordField field, javax.swing.JToggleButton btn) {
        if (btn.isSelected()) {
            field.setEchoChar((char) 0);
        } else {
            field.setEchoChar('•');
        }
    }

    private void btnGuiMaActionPerformed(java.awt.event.ActionEvent evt) {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email đăng ký!");
            return;
        }

        com.wms.controller.TrangChuGioiThieu.QuenMatKhauController controller = new com.wms.controller.TrangChuGioiThieu.QuenMatKhauController();
        com.wms.service.TrangChuGioiThieu.NguoiDungService.OtpQuenPassResponse response = controller.yeuCauOTP(email);

        switch (response.getResult()) {
            case THANH_CONG:
                currentOTP = response.getOtp();
                isOtpVerified = false;
                lblMessage.setText("Mã OTP đã được gửi tới email của bạn!");
                lblMessage.setForeground(new java.awt.Color(0, 153, 51));
                break;
            case EMAIL_KHONG_TON_TAI:
                JOptionPane.showMessageDialog(this, "Email này chưa được đăng ký trong hệ thống!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                break;
            case LOI_GUI_MAIL:
                JOptionPane.showMessageDialog(this, "Lỗi gửi mail, vui lòng thử lại sau!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                break;
            case LOI_CSDL:
                JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi hệ thống",
                        JOptionPane.ERROR_MESSAGE);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnXacNhanActionPerformed(java.awt.event.ActionEvent evt) {
        String inputOTP = txtOTP.getText().trim();
        if (currentOTP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng gửi mã OTP trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (inputOTP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã OTP!");
            return;
        }

        if (inputOTP.equals(currentOTP)) {
            isOtpVerified = true;
            lblMessage.setText("Xác thực thành công! Hãy đặt mật khẩu mới.");
            lblMessage.setForeground(new java.awt.Color(0, 153, 51));
            txtEmail.setEnabled(false);
            btnGuiMa.setEnabled(false);
            txtOTP.setEnabled(false);
            btnXacNhan.setEnabled(false);
        } else {
            isOtpVerified = false;
            lblMessage.setText("Mã OTP không chính xác!");
            lblMessage.setForeground(java.awt.Color.RED);
        }
    }

    private void btnLuuMatKhauActionPerformed(java.awt.event.ActionEvent evt) {
        if (!isOtpVerified) {
            JOptionPane.showMessageDialog(this, "Vui lòng xác thực mã OTP trước!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = txtEmail.getText().trim();
        String newPass = new String(txtNewPass.getPassword());
        String confirmPass = new String(txtConfirmPass.getPassword());

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ mật khẩu mới!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        com.wms.controller.TrangChuGioiThieu.QuenMatKhauController controller = new com.wms.controller.TrangChuGioiThieu.QuenMatKhauController();
        com.wms.service.TrangChuGioiThieu.NguoiDungService.ketQuaQuenMatKhau result = controller.datLaiMatKhau(email, newPass);

        switch (result) {
            case THANH_CONG:
                JOptionPane.showMessageDialog(this, "Đặt lại mật khẩu thành công! Hãy đăng nhập lại.");
                btnLoginActionPerformed(null);
                break;
            case EMAIL_KHONG_TON_TAI:
                JOptionPane.showMessageDialog(this, "Lỗi: Email không còn tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                break;
            case LOI_CSDL:
                JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi hệ thống",
                        JOptionPane.ERROR_MESSAGE);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật mật khẩu!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (win instanceof com.wms.view.TrangChuGioiThieu.TrangGioiThieuForm) {
            ((com.wms.view.TrangChuGioiThieu.TrangGioiThieuForm) win).showLoginForm();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblMessage = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        btnGuiMa = new javax.swing.JButton();
        lblOTP = new javax.swing.JLabel();
        txtOTP = new javax.swing.JTextField();
        btnXacNhan = new javax.swing.JButton();
        lblNewPass = new javax.swing.JLabel();
        txtNewPass = new javax.swing.JPasswordField();
        btnShowNewPass = new javax.swing.JToggleButton();
        lblConfirmPass = new javax.swing.JLabel();
        txtConfirmPass = new javax.swing.JPasswordField();
        btnShowConfirmPass = new javax.swing.JToggleButton();
        btnLuuMatKhau = new javax.swing.JButton();
        lblBackText = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(420, 550));
        setLayout(null);

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(35, 30, 48));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("QUÊN MẬT KHẨU");
        add(lblTitle);
        lblTitle.setBounds(0, 20, 420, 40);

        lblMessage.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMessage.setText("Vui lòng nhập email để nhận mã khôi phục");
        add(lblMessage);
        lblMessage.setBounds(40, 65, 340, 15);

        lblEmail.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblEmail.setForeground(new java.awt.Color(35, 30, 48));
        lblEmail.setText("Email đăng ký");
        add(lblEmail);
        lblEmail.setBounds(40, 90, 340, 20);

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEmail.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        add(txtEmail);
        txtEmail.setBounds(40, 110, 230, 35);

        btnGuiMa.setBackground(new java.awt.Color(235, 94, 141));
        btnGuiMa.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuiMa.setForeground(new java.awt.Color(255, 255, 255));
        btnGuiMa.setText("Gửi mã");
        btnGuiMa.setBorderPainted(false);
        btnGuiMa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuiMa.setFocusPainted(false);
        add(btnGuiMa);
        btnGuiMa.setBounds(280, 110, 100, 35);

        lblOTP.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblOTP.setForeground(new java.awt.Color(35, 30, 48));
        lblOTP.setText("Mã OTP xác nhận");
        add(lblOTP);
        lblOTP.setBounds(40, 160, 340, 20);

        txtOTP.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtOTP.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        add(txtOTP);
        txtOTP.setBounds(40, 180, 230, 35);

        btnXacNhan.setBackground(new java.awt.Color(235, 94, 141));
        btnXacNhan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnXacNhan.setForeground(new java.awt.Color(255, 255, 255));
        btnXacNhan.setText("Kiểm tra");
        btnXacNhan.setBorderPainted(false);
        btnXacNhan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnXacNhan.setFocusPainted(false);
        add(btnXacNhan);
        btnXacNhan.setBounds(280, 180, 100, 35);

        lblNewPass.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNewPass.setForeground(new java.awt.Color(35, 30, 48));
        lblNewPass.setText("Mật khẩu mới");
        add(lblNewPass);
        lblNewPass.setBounds(40, 230, 340, 20);

        txtNewPass.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtNewPass.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        add(txtNewPass);
        txtNewPass.setBounds(40, 250, 290, 35);

        btnShowNewPass.setText("👁");
        btnShowNewPass.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        add(btnShowNewPass);
        btnShowNewPass.setBounds(340, 250, 40, 35);

        lblConfirmPass.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblConfirmPass.setForeground(new java.awt.Color(35, 30, 48));
        lblConfirmPass.setText("Xác nhận lại mật khẩu");
        add(lblConfirmPass);
        lblConfirmPass.setBounds(40, 300, 340, 20);

        txtConfirmPass.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtConfirmPass.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        add(txtConfirmPass);
        txtConfirmPass.setBounds(40, 320, 290, 35);

        btnShowConfirmPass.setText("👁");
        btnShowConfirmPass.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        add(btnShowConfirmPass);
        btnShowConfirmPass.setBounds(340, 320, 40, 35);

        btnLuuMatKhau.setBackground(new java.awt.Color(235, 94, 141));
        btnLuuMatKhau.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnLuuMatKhau.setForeground(new java.awt.Color(255, 255, 255));
        btnLuuMatKhau.setText("Đặt lại mật khẩu");
        btnLuuMatKhau.setBorderPainted(false);
        btnLuuMatKhau.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLuuMatKhau.setFocusPainted(false);
        add(btnLuuMatKhau);
        btnLuuMatKhau.setBounds(40, 390, 340, 45);

        lblBackText.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblBackText.setForeground(new java.awt.Color(136, 136, 136));
        lblBackText.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblBackText.setText("Đã nhớ mật khẩu?");
        add(lblBackText);
        lblBackText.setBounds(80, 460, 130, 30);

        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(235, 94, 141));
        btnLogin.setText("Đăng nhập");
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogin.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLogin.setMargin(new java.awt.Insets(2, 0, 2, 14));
        add(btnLogin);
        btnLogin.setBounds(215, 460, 130, 30);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuiMa;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnLuuMatKhau;
    private javax.swing.JToggleButton btnShowConfirmPass;
    private javax.swing.JToggleButton btnShowNewPass;
    private javax.swing.JButton btnXacNhan;
    private javax.swing.JLabel lblBackText;
    private javax.swing.JLabel lblConfirmPass;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblNewPass;
    private javax.swing.JLabel lblOTP;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPasswordField txtConfirmPass;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JPasswordField txtNewPass;
    private javax.swing.JTextField txtOTP;
    // End of variables declaration//GEN-END:variables
}
