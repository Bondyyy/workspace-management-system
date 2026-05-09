/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.wms.view.TrangChuQuanLy;

import com.wms.model.NguoiDungDTO;
import com.wms.controller.DangNhapController;

/**
 *
 * @author Thinkapd T14s
 */
public class TrangChuQuanLyForm extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(TrangChuQuanLyForm.class.getName());
    private javax.swing.JButton activeButton;

    public TrangChuQuanLyForm() {
        initComponents();
        setupCustomUI();

        NguoiDungDTO user = DangNhapController.getCurrentUser();
        if (user != null) {
            String name = (user.getHoTen() != null && !user.getHoTen().isEmpty()) ? user.getHoTen() : user.getTenTaiKhoan();
            lblGreeting.setText("Xin chào " + (name != null ? name : "Admin") + "!");
            // Xác định tên hiển thị vai trò cho header
            String roleDisplay = "Nhân viên";
            if (user.getVaiTro() != null && !user.getVaiTro().isEmpty()) {
                // Lấy TenVaiTro đầu tiên (là tên, không phải MaVaiTro)
                for (String v : user.getVaiTro()) {
                    if (v != null && !v.startsWith("VT")) {
                        roleDisplay = v;
                        break;
                    }
                }
            }
            // Áp dụng phân quyền dựa vào MaChucNang từ DB
            phanQuyenTheoDB(user);
            lblRole.setText("Vai trò: " + roleDisplay);
        }

        // Gọi form Tổng quan lên đầu tiên
        showPanel(new com.wms.view.TrangChuQuanLy.TongQuan.TongQuanForm());
        setActiveMenu(btnMenuTongQuan);
    }

    private void setActiveMenu(javax.swing.JButton btn) {
        java.awt.Color defaultColor = new java.awt.Color(255, 255, 255);
        java.awt.Color activeColor = new java.awt.Color(252, 235, 241);
        java.awt.Color activeTextColor = new java.awt.Color(235, 94, 141);
        java.awt.Color defaultTextColor = new java.awt.Color(48, 30, 35);

        javax.swing.JButton[] menuButtons = {
                btnMenuTongQuan, btnMenuChiNhanh, btnMenuKhongGian,
                btnMenuDichVu, btnMenuKho, btnMenuPhien, btnMenuDichVuDat,
                btnMenuHoaDon, btnMenuGiamGia, btnMenuHoiVien, btnMenuNhanVien
        };

        for (javax.swing.JButton b : menuButtons) {
            if (b == btn) {
                b.setBackground(activeColor);
                b.setForeground(activeTextColor);
                activeButton = b;
            } else {
                b.setBackground(defaultColor);
                b.setForeground(defaultTextColor);
            }
        }
    }

    private void setupCustomUI() {
        // Thiết lập hiệu ứng Hover màu trắng - hồng cho thanh Menu
        javax.swing.JButton[] menuButtons = {
                btnMenuTongQuan, btnMenuChiNhanh, btnMenuKhongGian,
                btnMenuDichVu, btnMenuKho, btnMenuPhien, btnMenuDichVuDat,
                btnMenuHoaDon, btnMenuGiamGia, btnMenuHoiVien, btnMenuNhanVien
        };

        java.awt.Color defaultColor = new java.awt.Color(255, 255, 255);
        java.awt.Color hoverColor = new java.awt.Color(252, 235, 241); // Hồng phấn rất nhạt

        for (javax.swing.JButton btn : menuButtons) {
            btn.setOpaque(true);
            btn.setBackground(defaultColor);
            btn.setFocusPainted(false);
            btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (btn != activeButton) {
                        btn.setBackground(hoverColor);
                    }
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (btn != activeButton) {
                        btn.setBackground(defaultColor);
                    }
                }
            });
        }

        btnDangXuat.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));
    }

    // Hàm phân quyền động dựa vào MaChucNang từ DB
    public void phanQuyenTheoDB(NguoiDungDTO user) {
        // Kiểm tra xem có phải Quản trị hệ thống không (luôn có toàn quyền)
        boolean laAdminHT = user.hasRole("Quản trị hệ thống") || user.hasRole("VT01");

        if (laAdminHT) {
            // Admin hệ thống: hiện tất cả
            btnMenuTongQuan.setVisible(true);
            btnMenuChiNhanh.setVisible(true);
            btnMenuKhongGian.setVisible(true);
            btnMenuDichVu.setVisible(true);
            btnMenuKho.setVisible(true);
            btnMenuPhien.setVisible(true);
            btnMenuDichVuDat.setVisible(true);
            btnMenuHoaDon.setVisible(true);
            btnMenuGiamGia.setVisible(true);
            btnMenuHoiVien.setVisible(true);
            btnMenuNhanVien.setVisible(true);
        } else if (user.daPhanQuyen()) {
            // Nhân viên đã được phân quyền: chỉ hiện menu có MaChucNang tương ứng
            // Mapping: CN01→Tổng quan, CN02→Chi nhánh, CN03→Không gian, CN04→DịchVụ,
            //          CN05→Kho, CN06→DịchVuDat, CN07→Phiên, CN08→HóaĐơn,
            //          CN09→Giảm giá, CN10→HộiViên, CN11→Nhân sự
            btnMenuTongQuan.setVisible(user.hasChucNang("CN01"));
            btnMenuChiNhanh.setVisible(user.hasChucNang("CN02"));
            btnMenuKhongGian.setVisible(user.hasChucNang("CN03"));
            btnMenuDichVu.setVisible(user.hasChucNang("CN04"));
            btnMenuKho.setVisible(user.hasChucNang("CN05"));
            btnMenuDichVuDat.setVisible(user.hasChucNang("CN06"));
            btnMenuPhien.setVisible(user.hasChucNang("CN07"));
            btnMenuHoaDon.setVisible(user.hasChucNang("CN08"));
            btnMenuGiamGia.setVisible(user.hasChucNang("CN09"));
            btnMenuHoiVien.setVisible(user.hasChucNang("CN10"));
            btnMenuNhanVien.setVisible(user.hasChucNang("CN11"));

            // Đảm bảo Tổng quan luôn hiện (ít nhất 1 menu phải có)
            boolean anyVisible = user.hasChucNang("CN01") || user.hasChucNang("CN02")
                    || user.hasChucNang("CN03") || user.hasChucNang("CN04")
                    || user.hasChucNang("CN05") || user.hasChucNang("CN06")
                    || user.hasChucNang("CN07") || user.hasChucNang("CN08")
                    || user.hasChucNang("CN09") || user.hasChucNang("CN10")
                    || user.hasChucNang("CN11");
            if (!anyVisible) {
                // Chưa có chức năng nào → hiện tổng quan mặc định
                btnMenuTongQuan.setVisible(true);
            }
        } else {
            // Có vai trò nhưng chưa cấu hình chức năng cụ thể → hiện tổng quan + phiên làm việc tối thiểu
            btnMenuTongQuan.setVisible(true);
            btnMenuChiNhanh.setVisible(false);
            btnMenuKhongGian.setVisible(false);
            btnMenuDichVu.setVisible(false);
            btnMenuKho.setVisible(false);
            btnMenuPhien.setVisible(true);
            btnMenuDichVuDat.setVisible(true);
            btnMenuHoaDon.setVisible(true);
            btnMenuGiamGia.setVisible(false);
            btnMenuHoiVien.setVisible(false);
            btnMenuNhanVien.setVisible(false);
        }

        pnMenuContainer.revalidate();
        pnMenuContainer.repaint();
    }

    // Giữ lại method cũ (tương thích ngược)
    public void phanQuyen(String vaiTro) {
        // Mặc định cho phép hiển thị hết
        btnMenuTongQuan.setVisible(true);
        btnMenuChiNhanh.setVisible(true);
        btnMenuKhongGian.setVisible(true);
        btnMenuDichVu.setVisible(true);
        btnMenuKho.setVisible(true);
        btnMenuPhien.setVisible(true);
        btnMenuDichVuDat.setVisible(true);
        btnMenuHoaDon.setVisible(true);
        btnMenuGiamGia.setVisible(true);
        btnMenuHoiVien.setVisible(true);
        btnMenuNhanVien.setVisible(true);

        lblRole.setText("Vai trò: " + vaiTro);

        // Chặn quyền dựa trên vai trò
        if (vaiTro.equalsIgnoreCase("Lễ Tân")) {
            btnMenuChiNhanh.setVisible(false);
            btnMenuNhanVien.setVisible(false);
            btnMenuKho.setVisible(false);
        } else if (vaiTro.equalsIgnoreCase("Thu Ngân")) {
            btnMenuChiNhanh.setVisible(false);
            btnMenuKhongGian.setVisible(false);
            btnMenuNhanVien.setVisible(false);
        }

        // Gọi lệnh để Sidebar sắp xếp (co cụm) lại
        pnMenuContainer.revalidate();
        pnMenuContainer.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        pnSidebar = new javax.swing.JPanel();
        pnLogo = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblLogoSub = new javax.swing.JLabel();
        pnMenuContainer = new javax.swing.JPanel();
        btnMenuTongQuan = new javax.swing.JButton();
        btnMenuChiNhanh = new javax.swing.JButton();
        btnMenuKhongGian = new javax.swing.JButton();
        btnMenuDichVu = new javax.swing.JButton();
        btnMenuKho = new javax.swing.JButton();
        btnMenuDichVuDat = new javax.swing.JButton();
        btnMenuPhien = new javax.swing.JButton();
        btnMenuHoaDon = new javax.swing.JButton();
        btnMenuGiamGia = new javax.swing.JButton();
        btnMenuHoiVien = new javax.swing.JButton();
        btnMenuNhanVien = new javax.swing.JButton();
        pnLogout = new javax.swing.JPanel();
        btnDangXuat = new javax.swing.JButton();
        pnRightMain = new javax.swing.JPanel();
        pnHeader = new javax.swing.JPanel();
        lblGreeting = new javax.swing.JLabel();
        pnUserInfo = new javax.swing.JPanel();
        lblRole = new javax.swing.JLabel();
        pnContent = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hệ thống Quản lý - Spring System");
        setResizable(false);

        pnMain.setBackground(new java.awt.Color(240, 240, 240));
        pnMain.setPreferredSize(new java.awt.Dimension(1300, 700));
        pnMain.setLayout(new java.awt.BorderLayout());

        pnSidebar.setBackground(new java.awt.Color(255, 255, 255));
        pnSidebar.setPreferredSize(new java.awt.Dimension(250, 700));
        pnSidebar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, new java.awt.Color(224, 224, 224)));
        pnSidebar.setLayout(new java.awt.BorderLayout());

        pnLogo.setBackground(new java.awt.Color(255, 255, 255));
        pnLogo.setPreferredSize(new java.awt.Dimension(250, 90));
        pnLogo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(224, 224, 224)));
        pnLogo.setLayout(null);

        lblLogo.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblLogo.setForeground(new java.awt.Color(235, 94, 141));
        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setText("SPRING");
        pnLogo.add(lblLogo);
        lblLogo.setBounds(0, 20, 250, 35);

        lblLogoSub.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblLogoSub.setForeground(new java.awt.Color(136, 136, 136));
        lblLogoSub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogoSub.setText("A D M I N   P O R T A L");
        pnLogo.add(lblLogoSub);
        lblLogoSub.setBounds(0, 55, 250, 20);

        pnSidebar.add(pnLogo, java.awt.BorderLayout.NORTH);

        pnMenuContainer.setBackground(new java.awt.Color(255, 255, 255));
        pnMenuContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 20, 0));
        pnMenuContainer.setLayout(new javax.swing.BoxLayout(pnMenuContainer, javax.swing.BoxLayout.Y_AXIS));

        btnMenuTongQuan.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuTongQuan.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuTongQuan.setText("●  Báo cáo");
        btnMenuTongQuan.setBorderPainted(false);
        btnMenuTongQuan.setContentAreaFilled(false);
        btnMenuTongQuan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuTongQuan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuTongQuan.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuTongQuan.addActionListener(this::btnMenuTongQuanActionPerformed);
        pnMenuContainer.add(btnMenuTongQuan);

        btnMenuChiNhanh.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuChiNhanh.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuChiNhanh.setText("●  Chi nhánh");
        btnMenuChiNhanh.setBorderPainted(false);
        btnMenuChiNhanh.setContentAreaFilled(false);
        btnMenuChiNhanh.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuChiNhanh.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuChiNhanh.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuChiNhanh.addActionListener(this::btnMenuChiNhanhActionPerformed);
        pnMenuContainer.add(btnMenuChiNhanh);

        btnMenuKhongGian.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuKhongGian.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuKhongGian.setText("●  Không gian");
        btnMenuKhongGian.setBorderPainted(false);
        btnMenuKhongGian.setContentAreaFilled(false);
        btnMenuKhongGian.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuKhongGian.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuKhongGian.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuKhongGian.addActionListener(this::btnMenuKhongGianActionPerformed);
        pnMenuContainer.add(btnMenuKhongGian);

        btnMenuDichVu.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuDichVu.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuDichVu.setText("●  Thông tin Dịch vụ");
        btnMenuDichVu.setBorderPainted(false);
        btnMenuDichVu.setContentAreaFilled(false);
        btnMenuDichVu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuDichVu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuDichVu.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuDichVu.addActionListener(this::btnMenuDichVuActionPerformed);
        pnMenuContainer.add(btnMenuDichVu);

        btnMenuKho.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuKho.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuKho.setText("●  Kho Dịch vụ");
        btnMenuKho.setBorderPainted(false);
        btnMenuKho.setContentAreaFilled(false);
        btnMenuKho.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuKho.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuKho.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuKho.addActionListener(this::btnMenuKhoActionPerformed);
        pnMenuContainer.add(btnMenuKho);

        btnMenuDichVuDat.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuDichVuDat.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuDichVuDat.setText("●  Dịch vụ Khách đặt");
        btnMenuDichVuDat.setBorderPainted(false);
        btnMenuDichVuDat.setContentAreaFilled(false);
        btnMenuDichVuDat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuDichVuDat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuDichVuDat.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuDichVuDat.addActionListener(this::btnMenuDichVuDatActionPerformed);
        pnMenuContainer.add(btnMenuDichVuDat);

        btnMenuPhien.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuPhien.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuPhien.setText("●  Phiên làm việc");
        btnMenuPhien.setBorderPainted(false);
        btnMenuPhien.setContentAreaFilled(false);
        btnMenuPhien.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuPhien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuPhien.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuPhien.addActionListener(this::btnMenuPhienActionPerformed);
        pnMenuContainer.add(btnMenuPhien);

        btnMenuHoaDon.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuHoaDon.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuHoaDon.setText("●  Hóa đơn & Thu ngân");
        btnMenuHoaDon.setBorderPainted(false);
        btnMenuHoaDon.setContentAreaFilled(false);
        btnMenuHoaDon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuHoaDon.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuHoaDon.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuHoaDon.addActionListener(this::btnMenuHoaDonActionPerformed);
        pnMenuContainer.add(btnMenuHoaDon);

        btnMenuGiamGia.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuGiamGia.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuGiamGia.setText("●  Khuyến mãi (Voucher)");
        btnMenuGiamGia.setBorderPainted(false);
        btnMenuGiamGia.setContentAreaFilled(false);
        btnMenuGiamGia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuGiamGia.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuGiamGia.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuGiamGia.addActionListener(this::btnMenuGiamGiaActionPerformed);
        pnMenuContainer.add(btnMenuGiamGia);

        btnMenuHoiVien.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuHoiVien.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuHoiVien.setText("●  Hội viên / Khách hàng");
        btnMenuHoiVien.setBorderPainted(false);
        btnMenuHoiVien.setContentAreaFilled(false);
        btnMenuHoiVien.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuHoiVien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuHoiVien.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuHoiVien.addActionListener(this::btnMenuHoiVienActionPerformed);
        pnMenuContainer.add(btnMenuHoiVien);

        btnMenuNhanVien.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuNhanVien.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuNhanVien.setText("●  Nhân sự / Phân quyền");
        btnMenuNhanVien.setBorderPainted(false);
        btnMenuNhanVien.setContentAreaFilled(false);
        btnMenuNhanVien.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuNhanVien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuNhanVien.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuNhanVien.addActionListener(this::btnMenuNhanVienActionPerformed);
        pnMenuContainer.add(btnMenuNhanVien);

        pnSidebar.add(pnMenuContainer, java.awt.BorderLayout.CENTER);

        pnLogout.setBackground(new java.awt.Color(255, 255, 255));
        pnLogout.setPreferredSize(new java.awt.Dimension(250, 60));
        pnLogout.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(224, 224, 224)));
        pnLogout.setLayout(new java.awt.BorderLayout());

        btnDangXuat.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnDangXuat.setForeground(new java.awt.Color(150, 150, 150));
        btnDangXuat.setText("← Đăng xuất");
        btnDangXuat.setBorderPainted(false);
        btnDangXuat.setContentAreaFilled(false);
        btnDangXuat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDangXuat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnDangXuat.addActionListener(this::btnDangXuatActionPerformed);
        pnLogout.add(btnDangXuat, java.awt.BorderLayout.CENTER);

        pnSidebar.add(pnLogout, java.awt.BorderLayout.SOUTH);

        pnMain.add(pnSidebar, java.awt.BorderLayout.WEST);

        pnRightMain.setLayout(new java.awt.BorderLayout());

        pnHeader.setBackground(new java.awt.Color(254, 248, 250));
        pnHeader.setPreferredSize(new java.awt.Dimension(1050, 60));
        pnHeader.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(235, 235, 235)));
        pnHeader.setLayout(new java.awt.BorderLayout());

        lblGreeting.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblGreeting.setForeground(new java.awt.Color(35, 30, 48));
        lblGreeting.setText("Xin chào Quản lý!");
        lblGreeting.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));
        pnHeader.add(lblGreeting, java.awt.BorderLayout.WEST);

        pnUserInfo.setBackground(new java.awt.Color(254, 248, 250));
        pnUserInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 30));
        pnUserInfo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 15));

        lblRole.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblRole.setForeground(new java.awt.Color(235, 94, 141));
        lblRole.setText("Vai trò: Quản lý hệ thống");
        pnUserInfo.add(lblRole);

        pnHeader.add(pnUserInfo, java.awt.BorderLayout.EAST);

        pnRightMain.add(pnHeader, java.awt.BorderLayout.NORTH);

        pnContent.setBackground(new java.awt.Color(254, 248, 250));
        pnContent.setLayout(new java.awt.BorderLayout());
        pnRightMain.add(pnContent, java.awt.BorderLayout.CENTER);

        pnMain.add(pnRightMain, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnMain, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public void showPanel(javax.swing.JPanel panel) {
        pnContent.removeAll();
        pnContent.add(panel, java.awt.BorderLayout.CENTER);
        pnContent.revalidate();
        pnContent.repaint();
    }

    private void btnMenuTongQuanActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.TongQuan.TongQuanForm());
        setActiveMenu(btnMenuTongQuan);
    }

    private void btnMenuChiNhanhActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyChiNhanh.QuanLyChiNhanhForm());
        setActiveMenu(btnMenuChiNhanh);
    }

    private void btnMenuKhongGianActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyKhongGian.QuanLyKhongGianForm());
        setActiveMenu(btnMenuKhongGian);
    }

    private void btnMenuDichVuActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyThongTinDichVu.QuanLyThongTinDichVuForm());
        setActiveMenu(btnMenuDichVu);
    }

    private void btnMenuKhoActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyKho.QuanLyKhoForm());
        setActiveMenu(btnMenuKho);
    }

    private void btnMenuPhienActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyPhien.QuanLyPhienForm());
        setActiveMenu(btnMenuPhien);
    }

    private void btnMenuDichVuDatActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyDichVuDat.QuanLyDichVuDatForm());
        setActiveMenu(btnMenuDichVuDat);
    }

    private void btnMenuHoaDonActionPerformed(java.awt.event.ActionEvent evt) {
        // Form hóa đơn từ file QuanLyHoaDonForm.java của bạn
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyHoaDon.QuanLyHoaDonForm());
        setActiveMenu(btnMenuHoaDon);
    }

    private void btnMenuGiamGiaActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyPhieuGiamGia.QuanLyPhieuGiamGiaForm());
        setActiveMenu(btnMenuGiamGia);
    }

    private void btnMenuHoiVienActionPerformed(java.awt.event.ActionEvent evt) {
        // Form Hội viên từ file QuanLyHoiVienForm.java của bạn
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyHoiVien.QuanLyHoiVienForm());
        setActiveMenu(btnMenuHoiVien);
    }

    private void btnMenuNhanVienActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyNhanVien.QuanLyNhanVienForm());
        setActiveMenu(btnMenuNhanVien);
    }

    private void btnDangXuatActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
        DangNhapController.logout();
        try {
            new com.wms.view.TrangChuGioiThieu.TrangGioiThieuForm().setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        java.awt.EventQueue.invokeLater(() -> new TrangChuQuanLyForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDangXuat;
    private javax.swing.JButton btnMenuChiNhanh;
    private javax.swing.JButton btnMenuDichVu;
    private javax.swing.JButton btnMenuDichVuDat;
    private javax.swing.JButton btnMenuGiamGia;
    private javax.swing.JButton btnMenuHoaDon;
    private javax.swing.JButton btnMenuHoiVien;
    private javax.swing.JButton btnMenuKho;
    private javax.swing.JButton btnMenuKhongGian;
    private javax.swing.JButton btnMenuNhanVien;
    private javax.swing.JButton btnMenuPhien;
    private javax.swing.JButton btnMenuTongQuan;
    private javax.swing.JLabel lblGreeting;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblLogoSub;
    private javax.swing.JLabel lblRole;
    private javax.swing.JPanel pnContent;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLogo;
    private javax.swing.JPanel pnLogout;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnMenuContainer;
    private javax.swing.JPanel pnRightMain;
    private javax.swing.JPanel pnSidebar;
    private javax.swing.JPanel pnUserInfo;
    // End of variables declaration//GEN-END:variables
}
