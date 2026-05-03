package com.wms.view.TrangChuGioiThieu.DangKy;

import com.wms.controller.DangKyController;

import javax.swing.JOptionPane;
import com.wms.service.NguoiDungService.ketQuaDangKy;
import com.wms.service.NguoiDungService.OtpResponse;

public class DangKyForm extends javax.swing.JPanel {

    private String currentOTP = "";
    private boolean isOtpVerified = false;
    public DangKyForm() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtFullname = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        btnGuiMa = new javax.swing.JButton();
        lblOTP = new javax.swing.JLabel();
        txtOTP = new javax.swing.JTextField();
        btnXacNhanOTP = new javax.swing.JButton();
        lblOTPStatus = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnShowPass = new javax.swing.JToggleButton();
        lblConfirmPass = new javax.swing.JLabel();
        txtConfirmPassword = new javax.swing.JPasswordField();
        btnShowConfirmPass = new javax.swing.JToggleButton();
        lblMessage = new javax.swing.JLabel();
        btnRegister = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(235, 94, 141));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("ĐĂNG KÝ TÀI KHOẢN");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 420, 40));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Tài khoản:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 100, 30));

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsernameActionPerformed(evt);
            }
        });
        jPanel1.add(txtUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 220, 30));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Họ tên:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, 100, 30));

        txtFullname.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtFullname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFullnameActionPerformed(evt);
            }
        });
        jPanel1.add(txtFullname, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 130, 220, 30));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Email:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 180, 100, 30));

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmailActionPerformed(evt);
            }
        });
        jPanel1.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 180, 130, 30));

        btnGuiMa.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuiMa.setText("Gửi mã");
        btnGuiMa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuiMaActionPerformed(evt);
            }
        });
        jPanel1.add(btnGuiMa, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 180, 80, 30));

        lblOTP.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblOTP.setText("Mã OTP:");
        jPanel1.add(lblOTP, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 230, 100, 30));

        txtOTP.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtOTP.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtOTP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOTPActionPerformed(evt);
            }
        });
        jPanel1.add(txtOTP, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 230, 130, 30));

        btnXacNhanOTP.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnXacNhanOTP.setText("Xác nhận");
        btnXacNhanOTP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXacNhanOTPActionPerformed(evt);
            }
        });
        jPanel1.add(btnXacNhanOTP, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 230, 80, 30));

        lblOTPStatus.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblOTPStatus.setForeground(new java.awt.Color(0, 153, 51));
        jPanel1.add(lblOTPStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 260, 220, 20));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Mật khẩu:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 280, 100, 30));

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPasswordActionPerformed(evt);
            }
        });
        jPanel1.add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 280, 180, 30));

        btnShowPass.setText("O");
        btnShowPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowPassActionPerformed(evt);
            }
        });
        jPanel1.add(btnShowPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 280, 40, 30));

        lblConfirmPass.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblConfirmPass.setText("Nhập lại MK:");
        jPanel1.add(lblConfirmPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 330, 100, 30));

        txtConfirmPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtConfirmPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtConfirmPasswordActionPerformed(evt);
            }
        });
        jPanel1.add(txtConfirmPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 330, 180, 30));

        btnShowConfirmPass.setText("O");
        btnShowConfirmPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowConfirmPassActionPerformed(evt);
            }
        });
        jPanel1.add(btnShowConfirmPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 330, 40, 30));

        lblMessage.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblMessage.setForeground(new java.awt.Color(0, 153, 51));
        lblMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMessage.setText("Đăng ký thành công! Vui lòng Đăng nhập.");
        jPanel1.add(lblMessage, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 380, 420, 30));

        btnRegister.setBackground(new java.awt.Color(235, 94, 141));
        btnRegister.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setText("Đăng ký");
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        jPanel1.add(btnRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 430, 140, 40));

        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(235, 94, 141));
        btnLogin.setText("Đăng nhập");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        jPanel1.add(btnLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 430, 140, 40));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 28, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 540, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        /* TODO: Sẽ mở khóa code sau khi NetBeans sinh xong giao diện*/
        String username = txtUsername.getText().trim();
        String fullName = txtFullname.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin (Tên tài khoản, Họ tên, Email, Mật khẩu)!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!isOtpVerified) {
            JOptionPane.showMessageDialog(this, "Vui lòng xác thực mã OTP trước khi đăng ký!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi kết nối CSDL!", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Lỗi gửi mail, vui lòng thử lại sau!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void btnShowPassActionPerformed(java.awt.event.ActionEvent evt) {}
    
    private void btnShowConfirmPassActionPerformed(java.awt.event.ActionEvent evt) {}
    
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
        } else {
            isOtpVerified = false;
            lblOTPStatus.setText("Mã OTP không chính xác!");
            lblOTPStatus.setForeground(java.awt.Color.RED);
        }
    }

    // Các sự kiện trống để NetBeans không báo lỗi khi sinh code giao diện
    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {}
    private void txtFullnameActionPerformed(java.awt.event.ActionEvent evt) {}
    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {}
    private void txtOTPActionPerformed(java.awt.event.ActionEvent evt) {}
    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {}
    private void txtConfirmPasswordActionPerformed(java.awt.event.ActionEvent evt) {}

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
    private javax.swing.JPanel jPanel1;
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



