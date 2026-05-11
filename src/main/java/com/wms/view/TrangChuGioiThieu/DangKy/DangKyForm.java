package com.wms.view.TrangChuGioiThieu.DangKy;

import com.wms.controller.TrangChuGioiThieu.DangKyController;

import javax.swing.JOptionPane;
import com.wms.service.TrangChuGioiThieu.NguoiDungService.ketQuaDangKy;
import com.wms.service.TrangChuGioiThieu.NguoiDungService.OtpResponse;

public class DangKyForm extends javax.swing.JPanel {

    private String currentOTP = "";
    private boolean isOtpVerified = false;

    public DangKyForm() {
        initComponents();
        setupListeners();
    }

    private void setupListeners() {
        btnGuiMa.addActionListener(this::btnGuiMaActionPerformed);
        btnXacNhanOTP.addActionListener(this::btnXacNhanOTPActionPerformed);
        btnRegister.addActionListener(this::btnRegisterActionPerformed);
        btnShowPass.addActionListener(e -> toggleShowPassword(txtPassword, btnShowPass));
        btnShowConfirmPass.addActionListener(e -> toggleShowPassword(txtConfirmPassword, btnShowConfirmPass));
    }

    private void toggleShowPassword(javax.swing.JPasswordField field, javax.swing.JToggleButton btn) {
        if (btn.isSelected()) {
            field.setEchoChar((char) 0);
        } else {
            field.setEchoChar('•');
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lblMessage = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtFullname = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        btnGuiMa = new javax.swing.JButton();
        lblOTP = new javax.swing.JLabel();
        lblOTPStatus = new javax.swing.JLabel();
        txtOTP = new javax.swing.JTextField();
        btnXacNhanOTP = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnShowPass = new javax.swing.JToggleButton();
        lblConfirmPass = new javax.swing.JLabel();
        txtConfirmPassword = new javax.swing.JPasswordField();
        btnShowConfirmPass = new javax.swing.JToggleButton();
        btnRegister = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(420, 550));
        setLayout(null);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(35, 30, 48));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TẠO TÀI KHOẢN");
        add(jLabel1);
        jLabel1.setBounds(0, 10, 420, 40);

        lblMessage.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        add(lblMessage);
        lblMessage.setBounds(40, 45, 340, 15);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(35, 30, 48));
        jLabel2.setText("Tên đăng nhập");
        add(jLabel2);
        jLabel2.setBounds(40, 60, 340, 20);

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtUsername.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        add(txtUsername);
        txtUsername.setBounds(40, 80, 340, 35);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(35, 30, 48));
        jLabel5.setText("Họ và tên");
        add(jLabel5);
        jLabel5.setBounds(40, 120, 340, 20);

        txtFullname.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtFullname.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        add(txtFullname);
        txtFullname.setBounds(40, 140, 340, 35);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(35, 30, 48));
        jLabel4.setText("Email");
        add(jLabel4);
        jLabel4.setBounds(40, 180, 340, 20);

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEmail.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        txtEmail.addActionListener(this::txtEmailActionPerformed);
        add(txtEmail);
        txtEmail.setBounds(40, 200, 230, 35);

        btnGuiMa.setBackground(new java.awt.Color(235, 94, 141));
        btnGuiMa.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGuiMa.setForeground(new java.awt.Color(255, 255, 255));
        btnGuiMa.setText("Gửi mã");
        btnGuiMa.setBorderPainted(false);
        btnGuiMa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuiMa.setFocusPainted(false);
        add(btnGuiMa);
        btnGuiMa.setBounds(280, 200, 100, 35);

        lblOTP.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblOTP.setForeground(new java.awt.Color(35, 30, 48));
        lblOTP.setText("Nhập mã OTP");
        add(lblOTP);
        lblOTP.setBounds(40, 240, 150, 20);

        lblOTPStatus.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblOTPStatus.setForeground(new java.awt.Color(255, 0, 0));
        lblOTPStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        add(lblOTPStatus);
        lblOTPStatus.setBounds(200, 240, 180, 20);

        txtOTP.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtOTP.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        txtOTP.addActionListener(this::txtOTPActionPerformed);
        add(txtOTP);
        txtOTP.setBounds(40, 260, 230, 35);

        btnXacNhanOTP.setBackground(new java.awt.Color(235, 94, 141));
        btnXacNhanOTP.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnXacNhanOTP.setForeground(new java.awt.Color(255, 255, 255));
        btnXacNhanOTP.setText("Xác nhận");
        btnXacNhanOTP.setBorderPainted(false);
        btnXacNhanOTP.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnXacNhanOTP.setFocusPainted(false);
        add(btnXacNhanOTP);
        btnXacNhanOTP.setBounds(280, 260, 100, 35);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(35, 30, 48));
        jLabel3.setText("Mật khẩu");
        add(jLabel3);
        jLabel3.setBounds(40, 300, 340, 20);

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPassword.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        txtPassword.addActionListener(this::txtPasswordActionPerformed);
        add(txtPassword);
        txtPassword.setBounds(40, 320, 290, 35);

        btnShowPass.setText("👁");
        btnShowPass.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        add(btnShowPass);
        btnShowPass.setBounds(340, 320, 40, 35);

        lblConfirmPass.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblConfirmPass.setForeground(new java.awt.Color(35, 30, 48));
        lblConfirmPass.setText("Xác nhận mật khẩu");
        add(lblConfirmPass);
        lblConfirmPass.setBounds(40, 360, 340, 20);

        txtConfirmPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtConfirmPassword.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)),
                javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        txtConfirmPassword.addActionListener(this::txtConfirmPasswordActionPerformed);
        add(txtConfirmPassword);
        txtConfirmPassword.setBounds(40, 380, 290, 35);

        btnShowConfirmPass.setText("👁");
        btnShowConfirmPass.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        add(btnShowConfirmPass);
        btnShowConfirmPass.setBounds(340, 380, 40, 35);

        btnRegister.setBackground(new java.awt.Color(235, 94, 141));
        btnRegister.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setText("Đăng ký tài khoản");
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRegister.setFocusPainted(false);
        add(btnRegister);
        btnRegister.setBounds(40, 435, 340, 45);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(136, 136, 136));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Đã có tài khoản?");
        add(jLabel6);
        jLabel6.setBounds(70, 495, 130, 30);

        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(235, 94, 141));
        btnLogin.setText("Đăng nhập");
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogin.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLogin.setMargin(new java.awt.Insets(2, 0, 2, 14));
        btnLogin.addActionListener(this::btnLoginActionPerformed);
        add(btnLogin);
        btnLogin.setBounds(205, 495, 140, 30);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        /* TODO: Sẽ mở khóa code sau khi NetBeans sinh xong giao diện */
        String username = txtUsername.getText().trim();
        String fullName = txtFullname.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ thông tin (Tên tài khoản, Họ tên, Email, Mật khẩu)!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isOtpVerified) {
            JOptionPane.showMessageDialog(this, "Vui lòng xác thực mã OTP trước khi đăng ký!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DangKyController controller = new DangKyController();
        ketQuaDangKy result = controller.dangKy(username, fullName, email, password);

        switch (result) {
            case THANH_CONG:
                JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.");
                btnLoginActionPerformed(null);
                break;
            case TAI_KHOAN_DA_TON_TAI:
                JOptionPane.showMessageDialog(this, "Tên tài khoản đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                break;
            case EMAIL_DA_TON_TAI:
                JOptionPane.showMessageDialog(this, "Email đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                break;
            case DU_LIEU_KHONG_HOP_LE:
                JOptionPane.showMessageDialog(this, "Thông tin nhập không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                break;
            case LOI_CSDL:
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi kết nối CSDL!", "Lỗi hệ thống",
                        JOptionPane.ERROR_MESSAGE);
                break;
        }

    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (win instanceof com.wms.view.TrangChuGioiThieu.TrangGioiThieuForm) {
            ((com.wms.view.TrangChuGioiThieu.TrangGioiThieuForm) win).showLoginForm();
        }
    }

    private void btnGuiMaActionPerformed(java.awt.event.ActionEvent evt) {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tài khoản và Email trước khi gửi mã!");
            return;
        }

        DangKyController controller = new DangKyController();
        OtpResponse response = controller.yeuCauOTP(username, email);

        switch (response.getResult()) {
            case YEU_CAU_OTP_THANH_CONG:
                currentOTP = response.getOtp(); // Lưu lại mã OTP để đối chiếu
                isOtpVerified = false; // Reset trạng thái xác thực
                lblOTPStatus.setText("Mã OTP đã được gửi đến email!");
                lblOTPStatus.setForeground(new java.awt.Color(0, 153, 51)); // Màu xanh
                break;
            case TAI_KHOAN_DA_TON_TAI:
                JOptionPane.showMessageDialog(this, "Tên tài khoản đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                break;
            case EMAIL_DA_TON_TAI:
                JOptionPane.showMessageDialog(this, "Email đã được sử dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                break;
            case LOI_GUI_MAIL:
                JOptionPane.showMessageDialog(this, "Lỗi gửi mail, vui lòng thử lại sau!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnShowPassActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void btnShowConfirmPassActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void btnXacNhanOTPActionPerformed(java.awt.event.ActionEvent evt) {
        String inputOTP = txtOTP.getText().trim();

        if (currentOTP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn chưa gửi mã OTP!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (inputOTP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã OTP!");
            return;
        }

        if (inputOTP.equals(currentOTP)) {
            isOtpVerified = true;
            lblOTPStatus.setText("Xác thực OTP thành công!");
            lblOTPStatus.setForeground(new java.awt.Color(0, 153, 51)); // Màu xanh
            txtOTP.setEnabled(false);
            btnXacNhanOTP.setEnabled(false);
            txtEmail.setEnabled(false);
            btnGuiMa.setEnabled(false);
        } else {
            isOtpVerified = false;
            lblOTPStatus.setText("Mã OTP không chính xác!");
            lblOTPStatus.setForeground(java.awt.Color.RED);
        }
    }

    // Các sự kiện trống để NetBeans không báo lỗi khi sinh code giao diện
    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtFullnameActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtOTPActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtConfirmPasswordActionPerformed(java.awt.event.ActionEvent evt) {
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuiMa;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnRegister;
    private javax.swing.JToggleButton btnShowConfirmPass;
    private javax.swing.JToggleButton btnShowPass;
    private javax.swing.JButton btnXacNhanOTP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lblConfirmPass;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblOTP;
    private javax.swing.JLabel lblOTPStatus;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFullname;
    private javax.swing.JTextField txtOTP;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
