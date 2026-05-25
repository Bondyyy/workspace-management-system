/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.wms.view.TrangChuGioiThieu;

import java.awt.Image;
import javax.swing.ImageIcon;

public class TrangGioiThieuForm extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(TrangGioiThieuForm.class.getName());

    public TrangGioiThieuForm() {
        initComponents();
        setSize(1200, 720);
        setLocationRelativeTo(null);



        // Cấu hình các panel cho phép hiển thị mờ
        pnLeft.setOpaque(false);
        pnRight.setOpaque(false);
        pnAuthContainer.setOpaque(false);

        // Đưa ảnh nền ra pnMain và mở rộng full kích thước
        pnMain.add(lblBackground);
        lblBackground.setBounds(0, 0, 1200, 700);
        // Đưa lblBackground xuống lớp dưới cùng
        pnMain.setComponentZOrder(lblBackground, pnMain.getComponentCount() - 1);

        // Tải ảnh nền hero
        setBackgroundImage("/images/TrangGioiThieu.png");

        // Làm mờ khung chứa chữ Spring Workspace và bo góc
        pnTextOverlay.setOpaque(false);
        pnTextOverlay.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(255, 255, 255, 170)); // Mờ hơn (alpha 170)
                g2.fillRoundRect(x, y, width, height, 25, 25); // Bo góc 25px
                g2.dispose();
            }
        });

        // Làm mờ khung đăng nhập
        pnLeft.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setColor(new java.awt.Color(255, 255, 255, 225)); // Khung đăng nhập trắng mờ (alpha 225)
                g2.fillRect(x, y, width, height);
                g2.dispose();
            }
        });

        // Mở form đăng nhập
        showLoginForm();

        // === TEAM SECTION mới (thêm trực tiếp vào pnMain, y = 700) ===
        javax.swing.JPanel teamSection = buildTeamSection();
        teamSection.setBounds(0, 700, 1200, 580);
        pnMain.add(teamSection);

        // Cập nhật lại kích thước của pnMain để JScrollPane cuộn được
        pnMain.setPreferredSize(new java.awt.Dimension(1200, 1280));

        // Cuộn mượt hơn
        scrollPaneMain.getVerticalScrollBar().setUnitIncrement(20);

        // Scroll về đầu trang
        javax.swing.SwingUtilities.invokeLater(() -> scrollPaneMain.getVerticalScrollBar().setValue(0));
    }

    /**
     * Team section phong cách nhẹ nhàng, nền hồng nhạt — người dùng scroll xuống
     * mới thấy
     */
    private javax.swing.JPanel buildTeamSection() {
        javax.swing.JPanel section = new javax.swing.JPanel(null);
        // Nền hồng nhạt — như nh trong ảnh tham khảo
        section.setBackground(new java.awt.Color(252, 237, 243));

        // Label nhỏ phía trên: "VỀ CHÚNG TÔI"
        javax.swing.JLabel lblAbout = new javax.swing.JLabel("VỀ CHÚNG TÔI");
        lblAbout.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        lblAbout.setForeground(new java.awt.Color(220, 70, 110));
        lblAbout.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAbout.setBounds(0, 40, 1200, 20);
        section.add(lblAbout);

        // Tiêu đề chính: "Đội hình Spring"
        javax.swing.JLabel title = new javax.swing.JLabel("Đội hình Spring");
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 36));
        title.setForeground(new java.awt.Color(25, 25, 40));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setBounds(0, 65, 1200, 48);
        section.add(title);

        // Mô tả
        javax.swing.JLabel desc = new javax.swing.JLabel(
                "Nhóm Spring với rất nhiều ☕ và 💻");
        desc.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        desc.setForeground(new java.awt.Color(100, 100, 120));
        desc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        desc.setBounds(0, 118, 1200, 22);
        section.add(desc);

        // 4 thành viên — tên đầy đủ
        String[] names = { "Lai Mộc Huy", "Huỳnh Đức Dũng", "Sơn Nguyễn Kỳ Duyên", "Nguyễn Thành Đức" };
        String[] roles = { "Nhóm trưởng", "Nhóm phó", "Thư ký", "Thành viên" };
        String[] paths = {
                "/images/ThanhVienNhom/Huy2.png",
                "/images/ThanhVienNhom/Dung2.png",
                "/images/ThanhVienNhom/Duyen2.png",
                "/images/ThanhVienNhom/Duc.png"
        };

        // Card lớn hơn — ảnh full-width phần trên
        int cardW = 230, cardH = 320, gap = 22;
        int startX = (1200 - (4 * cardW + 3 * gap)) / 2;
        for (int i = 0; i < 4; i++) {
            javax.swing.JPanel card = buildMemberCard(names[i], roles[i], paths[i], cardW, cardH);
            card.setBounds(startX + i * (cardW + gap), 160, cardW, cardH);
            section.add(card);
        }

        // Footer
        javax.swing.JLabel footer = new javax.swing.JLabel(
                "© 2026 Hệ thống quản lý Không gian Làm việc và Học tập  —  Được phát triển bởi Nhóm Spring");
        footer.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        footer.setForeground(new java.awt.Color(160, 140, 150));
        footer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        footer.setBounds(0, 510, 1200, 25);
        section.add(footer);

        return section;
    }

    /**
     * Card kiểu như ảnh tham khảo:
     * - Ảnh full-width phần trên (không padding), tỷ lệ 4:3
     * - Tên + role ở dải trắng bên dưới
     * - Bo góc 14px, bóng nhẹ
     */
    private javax.swing.JPanel buildMemberCard(String name, String role, String imgPath, int w, int h) {
        int photoH = (int) (h * 0.72); // Ảnh chiếm 72% chiều cao card
        int infoH = h - photoH; // Phần text bên dưới

        javax.swing.JPanel card = new javax.swing.JPanel(null) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                // Bóng nhẹ
                g2.setColor(new java.awt.Color(0, 0, 0, 18));
                g2.fillRoundRect(3, 5, getWidth() - 2, getHeight() - 2, 14, 14);
                // Nền trắng
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 4, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        // Ảnh phía trên — full-width, không padding, bo góc trên
        javax.swing.JLabel photo = new javax.swing.JLabel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                if (getIcon() != null) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    // Clip bo góc trên
                    java.awt.geom.RoundRectangle2D rr = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(),
                            getHeight() + 20, 14, 14);
                    g2.setClip(rr);
                    getIcon().paintIcon(this, g2, 0, 0);
                    g2.dispose();
                }
            }
        };
        photo.setBounds(0, 0, w, photoH);
        loadAvatarInto(photo, imgPath, w, photoH); // <-- scale theo w×h
        card.add(photo);

        // Tên
        javax.swing.JLabel lName = new javax.swing.JLabel(name);
        lName.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lName.setForeground(new java.awt.Color(22, 22, 40));
        lName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lName.setBounds(0, photoH + 10, w, 22);
        card.add(lName);

        // Vai trò — hồng accent
        javax.swing.JLabel lRole = new javax.swing.JLabel(role);
        lRole.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        lRole.setForeground(new java.awt.Color(220, 60, 105));
        lRole.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lRole.setBounds(0, photoH + 34, w, 18);
        card.add(lRole);

        return card;
    }

    /** Tải avatar, scale và crop lấp đầy kích thước w×h (như object-fit: cover) */
    private void loadAvatarInto(javax.swing.JLabel label, String imagePath, int w, int h) {
        try {
            java.net.URL imgURL = getClass().getResource(imagePath);
            if (imgURL != null) {
                java.awt.image.BufferedImage originalImg = javax.imageio.ImageIO.read(imgURL);
                if (originalImg != null) {
                    int imgW = originalImg.getWidth();
                    int imgH = originalImg.getHeight();

                    double scaleX = (double) w / imgW;
                    double scaleY = (double) h / imgH;
                    double scale = Math.max(scaleX, scaleY);

                    int scaledW = (int) Math.ceil(imgW * scale);
                    int scaledH = (int) Math.ceil(imgH * scale);

                    // Sử dụng SCALE_SMOOTH để thu nhỏ mượt mà, chống rỗ pixel (aliasing) với ảnh
                    // gốc lớn
                    java.awt.Image smoothImg = originalImg.getScaledInstance(scaledW, scaledH,
                            java.awt.Image.SCALE_SMOOTH);
                    // Bọc vào ImageIcon để đảm bảo ảnh scale được load xong hoàn toàn trước khi vẽ
                    smoothImg = new javax.swing.ImageIcon(smoothImg).getImage();

                    int x = (w - scaledW) / 2;
                    int y = (h - scaledH) / 2;

                    java.awt.image.BufferedImage bimg = new java.awt.image.BufferedImage(w, h,
                            java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = bimg.createGraphics();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.drawImage(smoothImg, x, y, null);
                    g2.dispose();

                    label.setIcon(new javax.swing.ImageIcon(bimg));
                }
            } else {
                logger.warning("Không tìm thấy avatar: " + imagePath);
            }
        } catch (Exception e) {
            logger.warning("Lỗi tải avatar: " + e.getMessage());
        }
    }

    public void showLoginForm() {
        com.wms.view.TrangChuGioiThieu.DangNhap.DangNhapForm DangNhapWebForm = new com.wms.view.TrangChuGioiThieu.DangNhap.DangNhapForm();
        DangNhapWebForm.setOpaque(false); // Cần thiết để hiển thị nền mờ phía dưới
        pnAuthContainer.removeAll();
        pnAuthContainer.add(DangNhapWebForm, java.awt.BorderLayout.CENTER);
        pnAuthContainer.revalidate();
        pnAuthContainer.repaint();
    }

    public void showRegisterForm() {
        com.wms.view.TrangChuGioiThieu.DangKy.DangKyForm DangKyWebForm = new com.wms.view.TrangChuGioiThieu.DangKy.DangKyForm();
        DangKyWebForm.setOpaque(false); // Cần thiết để hiển thị nền mờ phía dưới
        pnAuthContainer.removeAll();
        pnAuthContainer.add(DangKyWebForm, java.awt.BorderLayout.CENTER);
        pnAuthContainer.revalidate();
        pnAuthContainer.repaint();
    }

    public void showForgotPasswordForm() {
        com.wms.view.TrangChuGioiThieu.QuenMatKhau.QuenMatKhauForm forgotForm = new com.wms.view.TrangChuGioiThieu.QuenMatKhau.QuenMatKhauForm();
        forgotForm.setOpaque(false);
        pnAuthContainer.removeAll();
        pnAuthContainer.add(forgotForm, java.awt.BorderLayout.CENTER);
        pnAuthContainer.revalidate();
        pnAuthContainer.repaint();
    }

    public void setBackgroundImage(String imagePath) {
        try {
            java.net.URL imgURL = getClass().getResource(imagePath);
            if (imgURL != null) {
                Image img = new ImageIcon(imgURL).getImage()
                        .getScaledInstance(1200, 700, Image.SCALE_SMOOTH);
                lblBackground.setIcon(new ImageIcon(img));
                lblBackground.setText("");
            } else {
                logger.warning("Không tìm thấy ảnh: " + imagePath);
            }
        } catch (Exception e) {
            logger.warning("Lỗi tải ảnh nền: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPaneMain = new javax.swing.JScrollPane();
        pnMain = new javax.swing.JPanel();
        pnLeft = new javax.swing.JPanel();
        pnAuthContainer = new javax.swing.JPanel();
        pnRight = new javax.swing.JPanel();
        pnTextOverlay = new javax.swing.JPanel();
        lblTitleRight = new javax.swing.JLabel();
        lblDescRight = new javax.swing.JLabel();
        lblBackground = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Spring Workspace - Welcome");
        setResizable(false);

        scrollPaneMain.setBorder(null);

        pnMain.setBackground(new java.awt.Color(255, 255, 255));
        pnMain.setPreferredSize(new java.awt.Dimension(1200, 1050));
        pnMain.setLayout(null);

        pnLeft.setBackground(new java.awt.Color(255, 255, 255));
        pnLeft.setLayout(new java.awt.GridBagLayout());

        pnAuthContainer.setBackground(new java.awt.Color(255, 255, 255));
        pnAuthContainer.setPreferredSize(new java.awt.Dimension(420, 550));
        pnAuthContainer.setLayout(new java.awt.BorderLayout());
        pnLeft.add(pnAuthContainer, new java.awt.GridBagConstraints());

        pnMain.add(pnLeft);
        pnLeft.setBounds(0, 0, 500, 700);

        pnRight.setLayout(null);

        pnTextOverlay.setBackground(new java.awt.Color(255, 255, 255));
        pnTextOverlay.setLayout(null);

        lblTitleRight.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblTitleRight.setForeground(new java.awt.Color(235, 94, 141));
        lblTitleRight.setText("SPRING WORKSPACE");
        pnTextOverlay.add(lblTitleRight);
        lblTitleRight.setBounds(30, 30, 540, 60);

        lblDescRight.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblDescRight.setForeground(new java.awt.Color(35, 30, 48));
        lblDescRight.setText("<html><div style='line-height: 1.6;'>Nơi hội tụ của những ý tưởng đột phá và cộng đồng sáng tạo. Chúng tôi mang đến trải nghiệm tiện nghi và chuyên nghiệp nhất.</div></html>");
        pnTextOverlay.add(lblDescRight);
        lblDescRight.setBounds(30, 100, 540, 80);

        pnRight.add(pnTextOverlay);
        pnTextOverlay.setBounds(50, 220, 600, 200);

        lblBackground.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pnRight.add(lblBackground);
        lblBackground.setBounds(0, 0, 700, 700);

        pnMain.add(pnRight);
        pnRight.setBounds(500, 0, 700, 700);

        scrollPaneMain.setViewportView(pnMain);

        getContentPane().add(scrollPaneMain, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new TrangGioiThieuForm().setVisible(true);
        });
        Thread initThread = new Thread(() -> {
            long start = System.currentTimeMillis();
            try {
                com.wms.util.SuperAdminCreator.initialize(); // Tự động kiểm tra quyền & admin khi mở app
                logger.info("Khởi tạo dữ liệu nền hoàn tất trong " + (System.currentTimeMillis() - start) + " ms");
            } catch (Exception ex) {
                logger.log(java.util.logging.Level.SEVERE, "Lỗi khởi tạo dữ liệu nền", ex);
            }
        }, "WMS-Intro-Init");
        initThread.setDaemon(true);
        initThread.start();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblDescRight;
    private javax.swing.JLabel lblTitleRight;
    private javax.swing.JPanel pnAuthContainer;
    private javax.swing.JPanel pnLeft;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnRight;
    private javax.swing.JPanel pnTextOverlay;
    private javax.swing.JScrollPane scrollPaneMain;
    // End of variables declaration//GEN-END:variables
}
