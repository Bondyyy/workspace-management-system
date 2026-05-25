/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.wms.view.TrangChuQuanLy;

import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.controller.TrangChuGioiThieu.DangNhapController;

/**
 *
 * @author Thinkapd T14s
 */
public class TrangChuQuanLyForm extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(TrangChuQuanLyForm.class.getName());
    private javax.swing.JButton activeButton;
    private javax.swing.JButton btnMenuDatChoTruoc;
    private javax.swing.JPanel pnGroupTongQuan;
    private javax.swing.JPanel pnGroupVanHanh;
    private javax.swing.JPanel pnGroupDichVu;
    private javax.swing.JPanel pnGroupKhachHang;
    private javax.swing.JPanel pnGroupTaiChinh;
    private javax.swing.JPanel pnGroupNhanSu;

    private static final java.awt.Color SIDEBAR_BG = new java.awt.Color(255, 250, 252);
    private static final java.awt.Color MENU_TEXT = new java.awt.Color(62, 49, 56);
    private static final java.awt.Color MENU_MUTED = new java.awt.Color(167, 139, 151);
    private static final java.awt.Color MENU_ACTIVE_BG = new java.awt.Color(255, 234, 242);
    private static final java.awt.Color MENU_HOVER_BG = new java.awt.Color(255, 244, 248);
    private static final java.awt.Color MENU_ACTIVE_TEXT = new java.awt.Color(226, 82, 130);
    private static final java.awt.Color MENU_BADGE_BG = new java.awt.Color(255, 240, 246);

    private static class SidebarButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        @Override
        public void installDefaults(javax.swing.AbstractButton b) {
            super.installDefaults(b);
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setRolloverEnabled(true);
        }

        @Override
        public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
            javax.swing.AbstractButton button = (javax.swing.AbstractButton) c;
            boolean active = Boolean.TRUE.equals(button.getClientProperty("menu.active"));
            boolean hover = button.getModel().isRollover();

            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            if (active || hover) {
                int x = 3;
                int y = 2;
                int width = c.getWidth() - 6;
                int height = c.getHeight() - 4;

                g2.setColor(active ? MENU_ACTIVE_BG : MENU_HOVER_BG);
                g2.fillRoundRect(x, y, width, height, 12, 12);

                if (active) {
                    g2.setColor(MENU_ACTIVE_TEXT);
                    g2.fillRoundRect(x + 2, y + 7, 4, height - 14, 4, 4);
                }
            }

            String badge = (String) button.getClientProperty("menu.badge");
            if (badge != null && !badge.isEmpty()) {
                int badgeSize = 20;
                int badgeX = 16;
                int badgeY = (c.getHeight() - badgeSize) / 2;

                g2.setColor(active ? java.awt.Color.WHITE : MENU_BADGE_BG);
                g2.fillRoundRect(badgeX, badgeY, badgeSize, badgeSize, 8, 8);

                g2.setColor(active ? MENU_ACTIVE_TEXT : MENU_MUTED);
                g2.setFont(button.getFont().deriveFont(java.awt.Font.BOLD, badge.length() > 1 ? 8.5f : 9.5f));
                java.awt.FontMetrics metrics = g2.getFontMetrics();
                int textX = badgeX + (badgeSize - metrics.stringWidth(badge)) / 2;
                int textY = badgeY + ((badgeSize - metrics.getHeight()) / 2) + metrics.getAscent();
                g2.drawString(badge, textX, textY);
            }

            g2.dispose();
            super.paint(g, c);
        }
    }

    public TrangChuQuanLyForm() {
        initComponents();
        themMenuDatChoTruoc();
        sapXepMenuTheoNhom();
        setupCustomUI();

        NguoiDungDTO user = DangNhapController.getCurrentUser();
        if (user != null) {
            String name = (user.getHoTen() != null && !user.getHoTen().isEmpty()) ? user.getHoTen()
                    : user.getTenTaiKhoan();
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
        showPanel(new com.wms.view.TrangChuQuanLy.TongQuan.TongQuanTabbedForm());
        setActiveMenu(btnMenuTongQuan);
    }

    private void themMenuDatChoTruoc() {
        btnMenuDatChoTruoc = new javax.swing.JButton();
        btnMenuDatChoTruoc.setFont(new java.awt.Font("Segoe UI", 1, 15));
        btnMenuDatChoTruoc.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuDatChoTruoc.setText("●  Đặt chỗ trước");
        btnMenuDatChoTruoc.setBorderPainted(false);
        btnMenuDatChoTruoc.setContentAreaFilled(false);
        btnMenuDatChoTruoc.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuDatChoTruoc.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuDatChoTruoc.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuDatChoTruoc.addActionListener(evt -> {
            showPanel(new com.wms.view.TrangChuQuanLy.QuanLyDatChoTruoc.QuanLyDatChoTruocForm());
            setActiveMenu(btnMenuDatChoTruoc);
        });
        pnMenuContainer.add(btnMenuDatChoTruoc, 7);
    }

    private void sapXepMenuTheoNhom() {
        pnSidebar.setPreferredSize(new java.awt.Dimension(250, 700));
        pnSidebar.setBackground(SIDEBAR_BG);
        pnLogo.setPreferredSize(new java.awt.Dimension(250, 88));
        pnLogo.setBackground(SIDEBAR_BG);
        lblLogo.setBounds(0, 17, 250, 35);
        lblLogoSub.setBounds(0, 53, 250, 20);

        btnMenuTongQuan.setText("B\u00e1o c\u00e1o");
        btnMenuChiNhanh.setText("Chi nh\u00e1nh");
        btnMenuKhongGian.setText("Kh\u00f4ng gian");
        btnMenuDichVu.setText("Th\u00f4ng tin d\u1ecbch v\u1ee5");
        btnMenuLoaiDichVu.setText("Lo\u1ea1i d\u1ecbch v\u1ee5");
        btnMenuKho.setText("Kho d\u1ecbch v\u1ee5");
        btnMenuDichVuDat.setText("D\u1ecbch v\u1ee5 kh\u00e1ch \u0111\u1eb7t");
        btnMenuPhien.setText("Phi\u00ean l\u00e0m vi\u1ec7c");
        btnMenuDatChoTruoc.setText("\u0110\u1eb7t ch\u1ed7 tr\u01b0\u1edbc");
        btnMenuHoaDon.setText("H\u00f3a \u0111\u01a1n & thu ng\u00e2n");
        btnMenuGiamGia.setText("Khuy\u1ebfn m\u00e3i");
        btnMenuHoiVien.setText("H\u1ed9i vi\u00ean");
        btnMenuHangThanhVien.setText("H\u1ea1ng th\u00e0nh vi\u00ean");
        btnMenuNhanVien.setText("Nh\u00e2n vi\u00ean");
        btnMenuNguoiDung.setText("Ng\u01b0\u1eddi d\u00f9ng");
        btnMenuVaiTro.setText("Vai tr\u00f2");
        ganKyHieuMenu();

        pnMenuContainer.removeAll();
        pnMenuContainer.setBackground(SIDEBAR_BG);
        pnMenuContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));

        pnGroupTongQuan = taoNhomMenu("T\u1ed5ng quan", btnMenuTongQuan);
        pnGroupVanHanh = taoNhomMenu("V\u1eadn h\u00e0nh", btnMenuChiNhanh, btnMenuKhongGian, btnMenuPhien, btnMenuDatChoTruoc);
        pnGroupDichVu = taoNhomMenu("D\u1ecbch v\u1ee5", btnMenuDichVu, btnMenuLoaiDichVu, btnMenuKho, btnMenuDichVuDat);
        pnGroupKhachHang = taoNhomMenu("Kh\u00e1ch h\u00e0ng", btnMenuHoiVien, btnMenuHangThanhVien, btnMenuGiamGia);
        pnGroupTaiChinh = taoNhomMenu("T\u00e0i ch\u00ednh", btnMenuHoaDon);
        pnGroupNhanSu = taoNhomMenu("Nh\u00e2n s\u1ef1 & quy\u1ec1n", btnMenuNhanVien, btnMenuNguoiDung, btnMenuVaiTro);

        pnMenuContainer.add(pnGroupTongQuan);
        pnMenuContainer.add(pnGroupVanHanh);
        pnMenuContainer.add(pnGroupDichVu);
        pnMenuContainer.add(pnGroupKhachHang);
        pnMenuContainer.add(pnGroupTaiChinh);
        pnMenuContainer.add(pnGroupNhanSu);

        themScrollChoMenu();
        capNhatHienThiNhomMenu();
    }

    private void ganKyHieuMenu() {
        btnMenuTongQuan.putClientProperty("menu.badge", "BC");
        btnMenuChiNhanh.putClientProperty("menu.badge", "CN");
        btnMenuKhongGian.putClientProperty("menu.badge", "KG");
        btnMenuDichVu.putClientProperty("menu.badge", "DV");
        btnMenuLoaiDichVu.putClientProperty("menu.badge", "LD");
        btnMenuKho.putClientProperty("menu.badge", "K");
        btnMenuDichVuDat.putClientProperty("menu.badge", "DD");
        btnMenuPhien.putClientProperty("menu.badge", "P");
        btnMenuDatChoTruoc.putClientProperty("menu.badge", "DC");
        btnMenuHoaDon.putClientProperty("menu.badge", "HD");
        btnMenuGiamGia.putClientProperty("menu.badge", "KM");
        btnMenuHoiVien.putClientProperty("menu.badge", "HV");
        btnMenuHangThanhVien.putClientProperty("menu.badge", "H");
        btnMenuNhanVien.putClientProperty("menu.badge", "NV");
        btnMenuNguoiDung.putClientProperty("menu.badge", "ND");
        btnMenuVaiTro.putClientProperty("menu.badge", "VT");
    }

    private javax.swing.JPanel taoNhomMenu(String title, javax.swing.JButton... buttons) {
        javax.swing.JPanel group = new javax.swing.JPanel();
        group.setOpaque(false);
        group.setLayout(new javax.swing.BoxLayout(group, javax.swing.BoxLayout.Y_AXIS));
        group.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        javax.swing.JLabel label = new javax.swing.JLabel(title);
        label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        label.setForeground(MENU_MUTED);
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 3, 0));
        label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        group.add(label);

        for (javax.swing.JButton button : buttons) {
            button.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            group.add(button);
        }

        group.add(javax.swing.Box.createVerticalStrut(1));
        group.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, group.getPreferredSize().height));
        return group;
    }

    private void themScrollChoMenu() {
        if (pnMenuContainer.getParent() instanceof javax.swing.JViewport) {
            return;
        }

        java.awt.Container parent = pnMenuContainer.getParent();
        if (parent != null) {
            parent.remove(pnMenuContainer);
        }

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(pnMenuContainer);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(SIDEBAR_BG);
        scrollPane.getViewport().setBackground(SIDEBAR_BG);
        pnSidebar.add(scrollPane, java.awt.BorderLayout.CENTER);
    }

    private void capNhatHienThiNhomMenu() {
        javax.swing.JPanel[] groups = {
                pnGroupTongQuan, pnGroupVanHanh, pnGroupDichVu,
                pnGroupKhachHang, pnGroupTaiChinh, pnGroupNhanSu
        };

        for (javax.swing.JPanel group : groups) {
            if (group == null) {
                continue;
            }

            boolean hasVisibleButton = false;
            for (java.awt.Component component : group.getComponents()) {
                if (component instanceof javax.swing.JButton && component.isVisible()) {
                    hasVisibleButton = true;
                    break;
                }
            }
            group.setVisible(hasVisibleButton);
            group.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, group.getPreferredSize().height));
        }

        pnMenuContainer.revalidate();
        pnMenuContainer.repaint();
    }

    private void setActiveMenu(javax.swing.JButton btn) {
        javax.swing.JButton[] menuButtons = {
                btnMenuTongQuan, btnMenuChiNhanh, btnMenuKhongGian,
                btnMenuDichVu, btnMenuLoaiDichVu, btnMenuKho, btnMenuPhien, btnMenuDatChoTruoc, btnMenuDichVuDat,
                btnMenuHoaDon, btnMenuGiamGia, btnMenuHoiVien, btnMenuHangThanhVien, btnMenuNhanVien, btnMenuNguoiDung, btnMenuVaiTro
        };

        for (javax.swing.JButton b : menuButtons) {
            if (b == null) {
                continue;
            }

            if (b == btn) {
                b.putClientProperty("menu.active", true);
                b.setForeground(MENU_ACTIVE_TEXT);
                b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                activeButton = b;
            } else {
                b.putClientProperty("menu.active", false);
                b.setForeground(MENU_TEXT);
                b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            }
            b.repaint();
        }
    }

    private void setupCustomUI() {
        // Thiết lập hiệu ứng Hover màu trắng - hồng cho thanh Menu
        javax.swing.JButton[] menuButtons = {
                btnMenuTongQuan, btnMenuChiNhanh, btnMenuKhongGian,
                btnMenuDichVu, btnMenuLoaiDichVu, btnMenuKho, btnMenuPhien, btnMenuDatChoTruoc, btnMenuDichVuDat,
                btnMenuHoaDon, btnMenuGiamGia, btnMenuHoiVien, btnMenuHangThanhVien, btnMenuNhanVien, btnMenuNguoiDung, btnMenuVaiTro
        };


        for (javax.swing.JButton btn : menuButtons) {
            if (btn == null) {
                continue;
            }

            btn.setUI(new SidebarButtonUI());
            btn.setOpaque(false);
            btn.setForeground(MENU_TEXT);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 48, 0, 10));
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            btn.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 32));
            btn.setPreferredSize(new java.awt.Dimension(226, 32));
            btn.setMinimumSize(new java.awt.Dimension(180, 32));
        }

        pnLogout.setPreferredSize(new java.awt.Dimension(250, 56));
        pnLogout.setBackground(SIDEBAR_BG);
        btnDangXuat.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 28, 0, 0));
        btnDangXuat.setBorderPainted(false);
        btnDangXuat.setContentAreaFilled(false);
        capNhatHienThiNhomMenu();
    }

    // Hàm phân quyền động dựa vào MaChucNang từ DB
    public void phanQuyenTheoDB(NguoiDungDTO user) {
        // Áp dụng phân quyền dựa vào MaChucNang từ DB
        // Mapping: CN01→Tổng quan, CN02→Chi nhánh, CN03→Không gian, CN04→DịchVụ,
        // CN05→Kho, CN06→DịchVuDat, CN07→Phiên, CN08→HóaĐơn,
        // CN09→Giảm giá, CN10→HộiViên, CN11→Nhân sự, CN12→Người dùng, CN13→Vai trò, CN14→Loại dịch vụ
        
        btnMenuTongQuan.setVisible(user.hasChucNang("CN01") || user.hasRole("VT02") || user.hasRole("Quản lý Chi nhánh") || user.hasRole("Quản lý"));
        btnMenuChiNhanh.setVisible(user.hasChucNang("CN02"));
        btnMenuKhongGian.setVisible(user.hasChucNang("CN03"));
        btnMenuDichVu.setVisible(user.hasChucNang("CN04"));
        btnMenuKho.setVisible(user.hasChucNang("CN05"));
        btnMenuDichVuDat.setVisible(user.hasChucNang("CN06"));
        btnMenuPhien.setVisible(user.hasChucNang("CN07"));
        btnMenuDatChoTruoc.setVisible(user.hasChucNang("CN07") || user.hasChucNang("CN08"));
        btnMenuHoaDon.setVisible(user.hasChucNang("CN08"));
        btnMenuGiamGia.setVisible(user.hasChucNang("CN09"));
        btnMenuHoiVien.setVisible(user.hasChucNang("CN10"));
        btnMenuHangThanhVien.setVisible(user.hasChucNang("CN15") || user.hasRole("VT01") || user.hasRole("Quản trị viên Hệ thống"));
        btnMenuNhanVien.setVisible(user.hasChucNang("CN11"));
        btnMenuLoaiDichVu.setVisible(user.hasChucNang("CN14"));
        btnMenuNguoiDung.setVisible(user.hasChucNang("CN12"));
        btnMenuVaiTro.setVisible(user.hasChucNang("CN13"));

        // Đảm bảo ít nhất Tổng quan luôn hiện nếu chưa được cấu hình gì (Failsafe)
        if (!user.daPhanQuyen()) {
            boolean isManager = user.hasRole("VT02") || user.hasRole("Quản lý Chi nhánh") || user.hasRole("Quản lý");
            boolean isAdmin = user.hasRole("VT01") || user.hasRole("Quản trị viên Hệ thống");
            
            if (isManager) {
                btnMenuTongQuan.setVisible(true);
                btnMenuKhongGian.setVisible(true);
                btnMenuKho.setVisible(true);
                btnMenuDichVuDat.setVisible(true);
                btnMenuPhien.setVisible(true);
                btnMenuDatChoTruoc.setVisible(true);
                btnMenuHoaDon.setVisible(true);
                btnMenuHoiVien.setVisible(true);
                btnMenuNhanVien.setVisible(true);
            } else if (isAdmin) {
                btnMenuTongQuan.setVisible(true);
                btnMenuChiNhanh.setVisible(true);
                btnMenuGiamGia.setVisible(true);
                btnMenuNhanVien.setVisible(true);
                btnMenuNguoiDung.setVisible(true);
                btnMenuVaiTro.setVisible(true);
                btnMenuLoaiDichVu.setVisible(true);
                btnMenuHangThanhVien.setVisible(true);
            } else {
                btnMenuTongQuan.setVisible(true);
            }
        }

        capNhatHienThiNhomMenu();
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
        btnMenuDatChoTruoc.setVisible(true);
        btnMenuDichVuDat.setVisible(true);
        btnMenuHoaDon.setVisible(true);
        btnMenuGiamGia.setVisible(true);
        btnMenuHoiVien.setVisible(true);
        btnMenuHangThanhVien.setVisible(true);
        btnMenuNhanVien.setVisible(true);
        btnMenuLoaiDichVu.setVisible(true);
        btnMenuNguoiDung.setVisible(true);
        btnMenuVaiTro.setVisible(true);

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
        capNhatHienThiNhomMenu();
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
        btnMenuHangThanhVien = new javax.swing.JButton();
        btnMenuNhanVien = new javax.swing.JButton();
        btnMenuLoaiDichVu = new javax.swing.JButton();
        btnMenuNguoiDung = new javax.swing.JButton();
        btnMenuVaiTro = new javax.swing.JButton();
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

        btnMenuHangThanhVien.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuHangThanhVien.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuHangThanhVien.setText("●  Hạng thành viên");
        btnMenuHangThanhVien.setBorderPainted(false);
        btnMenuHangThanhVien.setContentAreaFilled(false);
        btnMenuHangThanhVien.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuHangThanhVien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuHangThanhVien.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuHangThanhVien.addActionListener(this::btnMenuHangThanhVienActionPerformed);
        pnMenuContainer.add(btnMenuHangThanhVien);

        btnMenuNhanVien.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuNhanVien.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuNhanVien.setText("●  Nhân viên");
        btnMenuNhanVien.setBorderPainted(false);
        btnMenuNhanVien.setContentAreaFilled(false);
        btnMenuNhanVien.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuNhanVien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuNhanVien.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuNhanVien.addActionListener(this::btnMenuNhanVienActionPerformed);
        pnMenuContainer.add(btnMenuNhanVien);

        btnMenuLoaiDichVu.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuLoaiDichVu.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuLoaiDichVu.setText("●  Loại Dịch vụ");
        btnMenuLoaiDichVu.setBorderPainted(false);
        btnMenuLoaiDichVu.setContentAreaFilled(false);
        btnMenuLoaiDichVu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuLoaiDichVu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuLoaiDichVu.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuLoaiDichVu.addActionListener(this::btnMenuLoaiDichVuActionPerformed);
        pnMenuContainer.add(btnMenuLoaiDichVu);

        btnMenuNguoiDung.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuNguoiDung.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuNguoiDung.setText("●  Người dùng");
        btnMenuNguoiDung.setBorderPainted(false);
        btnMenuNguoiDung.setContentAreaFilled(false);
        btnMenuNguoiDung.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuNguoiDung.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuNguoiDung.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuNguoiDung.addActionListener(this::btnMenuNguoiDungActionPerformed);
        pnMenuContainer.add(btnMenuNguoiDung);

        btnMenuVaiTro.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMenuVaiTro.setForeground(new java.awt.Color(48, 30, 35));
        btnMenuVaiTro.setText("●  Quản lý Vai trò");
        btnMenuVaiTro.setBorderPainted(false);
        btnMenuVaiTro.setContentAreaFilled(false);
        btnMenuVaiTro.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenuVaiTro.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuVaiTro.setMaximumSize(new java.awt.Dimension(32767, 45));
        btnMenuVaiTro.addActionListener(this::btnMenuVaiTroActionPerformed);
        pnMenuContainer.add(btnMenuVaiTro);

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
        showPanel(new com.wms.view.TrangChuQuanLy.TongQuan.TongQuanTabbedForm());
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

    private void btnMenuHangThanhVienActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyHangThanhVien.QuanLyHangThanhVienForm());
        setActiveMenu(btnMenuHangThanhVien);
    }

    private void btnMenuNhanVienActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyNhanVien.QuanLyNhanVienForm());
        setActiveMenu(btnMenuNhanVien);
    }

    private void btnMenuLoaiDichVuActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyLoaiDichVu.QuanLyLoaiDichVuForm());
        setActiveMenu(btnMenuLoaiDichVu);
    }

    private void btnMenuNguoiDungActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyNguoiDung.QuanLyNguoiDungForm());
        setActiveMenu(btnMenuNguoiDung);
    }

    private void btnMenuVaiTroActionPerformed(java.awt.event.ActionEvent evt) {
        showPanel(new com.wms.view.TrangChuQuanLy.QuanLyVaiTro.QuanLyVaiTroForm());
        setActiveMenu(btnMenuVaiTro);
    }

    private void btnDangXuatActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
        DangNhapController.dangXuat();
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
    private javax.swing.JButton btnMenuHangThanhVien;
    private javax.swing.JButton btnMenuKho;
    private javax.swing.JButton btnMenuKhongGian;
    private javax.swing.JButton btnMenuNhanVien;
    private javax.swing.JButton btnMenuPhien;
    private javax.swing.JButton btnMenuTongQuan;
    private javax.swing.JButton btnMenuLoaiDichVu;
    private javax.swing.JButton btnMenuNguoiDung;
    private javax.swing.JButton btnMenuVaiTro;
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
