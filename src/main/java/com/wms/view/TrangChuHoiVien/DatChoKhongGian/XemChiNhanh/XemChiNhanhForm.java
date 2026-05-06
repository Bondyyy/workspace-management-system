/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.wms.view.TrangChuHoiVien.DatChoKhongGian.XemChiNhanh;

import com.wms.dao.ChiNhanhDAO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.wms.view.TrangChuHoiVien.DatChoKhongGian.XemSoDoKhongGian.XemSoDoKhongGianForm;
/**
 *
 * @author Thinkapd T14s
 */
public class XemChiNhanhForm extends javax.swing.JPanel {

    public static class ModelChiNhanh {
        public String ma, ten, diaChi, gioMo, gioDong, hotline, trangThai;
        public ModelChiNhanh(String m, String t, String d, String gm, String gd, String h, String st) {
            ma=m; ten=t; diaChi=d; gioMo=gm; gioDong=gd; hotline=h; trangThai=st;
        }
    }

    private ModelChiNhanh chiNhanhDangChon = null;

    public XemChiNhanhForm() {
        initComponents();
        initCustomUI();
        loadDanhSachChiNhanh();
    }

    private void initCustomUI() {
        // NetBeans tạo pnDanhSachChiNhanh với FlowLayout, ta ép nó thành BoxLayout dọc
        pnDanhSachChiNhanh.setLayout(new BoxLayout(pnDanhSachChiNhanh, BoxLayout.Y_AXIS));
        scrollDanhSach.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void loadDanhSachChiNhanh() {
        try {
            com.wms.dao.ChiNhanhDAO chiNhanhDAO = new com.wms.dao.ChiNhanhDAO();
            List<com.wms.model.ChiNhanhDTO> dsTuDB = chiNhanhDAO.layDanhSachChiNhanh();
            if (dsTuDB != null && !dsTuDB.isEmpty()) {
                List<ModelChiNhanh> danhSach = new java.util.ArrayList<>();
                for (com.wms.model.ChiNhanhDTO db : dsTuDB) {
                    String trangThai = db.getTrangThai() != null ? db.getTrangThai() : "Hoạt động";
                    String gioMo = db.getThoiGianMoCua() != null ? db.getThoiGianMoCua() : "--";
                    String gioDong = db.getThoiGianDongCua() != null ? db.getThoiGianDongCua() : "--";
                    String hotline = db.getDuongDayNong() != null ? db.getDuongDayNong() : "--";
                    danhSach.add(new ModelChiNhanh(
                        db.getMaCN(), db.getTenCN(), db.getDiaChi(),
                        gioMo, gioDong, hotline, trangThai));
                }
                hienThiDanhSach(danhSach);
                return;
            }
        } catch (Exception e) {
            System.err.println("[XemChiNhanhForm] Lỗi tải DB, dùng dữ liệu mẫu: " + e.getMessage());
        }
        // Fallback: dữ liệu mẫu khi chưa kết nối được DB
        loadDummyData();
    }

    private void loadDummyData() {
        List<ModelChiNhanh> danhSach = new ArrayList<>();
        danhSach.add(new ModelChiNhanh("CN001", "Chi nhánh Quận 1", "123 Nguyễn Huệ, P. Bến Nghé, Q.1", "07:00", "22:00", "028 3822 5678", "Hoạt động"));
        danhSach.add(new ModelChiNhanh("CN002", "Chi nhánh Thủ Đức", "Khu phố 6, P. Linh Trung, TP. Thủ Đức", "06:30", "23:00", "028 3724 1234", "Hoạt động"));
        danhSach.add(new ModelChiNhanh("CN003", "Chi nhánh Quận 3", "456 Võ Văn Tần, P.5, Q.3", "08:00", "20:00", "028 3930 9876", "Hoạt động"));
        danhSach.add(new ModelChiNhanh("CN004", "Chi nhánh Gò Vấp", "789 Quang Trung, P.10, Q.Gò Vấp", "07:30", "22:30", "028 3589 4567", "Hoạt động"));
        danhSach.add(new ModelChiNhanh("CN005", "Chi nhánh Quận 7", "101 Nguyễn Văn Linh, Tân Phong, Q.7", "08:00", "21:00", "028 3771 8899", "Hoạt động"));
        hienThiDanhSach(danhSach);
    }

    public void hienThiDanhSach(List<ModelChiNhanh> danhSach) {
        pnDanhSachChiNhanh.removeAll();
        for (ModelChiNhanh cn : danhSach) {
            pnDanhSachChiNhanh.add(taoCardChiNhanh(cn));
            pnDanhSachChiNhanh.add(Box.createRigidArea(new Dimension(0, 15)));
        }
        pnDanhSachChiNhanh.revalidate();
        pnDanhSachChiNhanh.repaint();
    }

    private JPanel taoCardChiNhanh(ModelChiNhanh chiNhanh) {
        JPanel card = new JPanel(new BorderLayout(20, 0)) {
            private boolean isSelected = false;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) g2.setColor(Color.decode("#FCE4EC")); // Hồng rất nhạt khi chọn
                else g2.setColor(Color.WHITE); // Trắng mặc định
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                if (isSelected) {
                    g2.setColor(Color.decode("#EB5E8D")); // Viền hồng đậm
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 19, 19);
                } else {
                    g2.setColor(new Color(230, 230, 230)); // Viền xám nhạt
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                }
                g2.dispose();
            }
            public void setSelected(boolean selected) { this.isSelected = selected; repaint(); }
        };
        
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(950, 120));
        card.setMaximumSize(new Dimension(950, 120));
        card.setBorder(new EmptyBorder(20, 25, 20, 25));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // BÊN TRÁI: ICON
        JLabel lblIcon = new JLabel(chiNhanh.ma, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblIcon.setForeground(Color.decode("#EB5E8D"));
        lblIcon.setPreferredSize(new Dimension(120, 100));
        card.add(lblIcon, BorderLayout.WEST);

        // Ở GIỮA: THÔNG TIN
        JPanel pnGiua = new JPanel();
        pnGiua.setLayout(new BoxLayout(pnGiua, BoxLayout.Y_AXIS));
        pnGiua.setOpaque(false);
        
        JLabel lblTen = new JLabel(chiNhanh.ten);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTen.setForeground(Color.decode("#301E23"));
        pnGiua.add(lblTen);
        pnGiua.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JLabel lblInfo = new JLabel("Địa chỉ: " + chiNhanh.diaChi + "   |   Giờ: " + chiNhanh.gioMo + " - " + chiNhanh.gioDong + "   |   SĐT: " + chiNhanh.hotline);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInfo.setForeground(new Color(97, 97, 97));
        pnGiua.add(lblInfo);
        
        card.add(pnGiua, BorderLayout.CENTER);

        // BÊN PHẢI: TRẠNG THÁI
        boolean isActive = "Đang hoạt động".equalsIgnoreCase(chiNhanh.trangThai) || "Hoạt động".equalsIgnoreCase(chiNhanh.trangThai);
        JLabel lblTrangThai = new JLabel(isActive ? "Hoạt động" : "Đóng cửa", SwingConstants.CENTER);
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTrangThai.setOpaque(true);
        if (isActive) {
            lblTrangThai.setForeground(new Color(27, 94, 32)); lblTrangThai.setBackground(new Color(200, 230, 201));
        } else {
            lblTrangThai.setForeground(new Color(183, 28, 28)); lblTrangThai.setBackground(new Color(255, 205, 210));
        }
        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JPanel pnPhai = new JPanel(new GridBagLayout());
        pnPhai.setOpaque(false);
        pnPhai.setPreferredSize(new Dimension(120, 100));
        pnPhai.add(lblTrangThai);
        card.add(pnPhai, BorderLayout.EAST);

        // XỬ LÝ SỰ KIỆN CLICK
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Bỏ chọn tất cả các card cũ
                for (Component comp : pnDanhSachChiNhanh.getComponents()) {
                    if (comp instanceof JPanel) {
                        try {
                            comp.getClass().getMethod("setSelected", boolean.class).invoke(comp, false);
                        } catch (Exception ex) {}
                    }
                }
                // Chọn card vừa click
                try { card.getClass().getMethod("setSelected", boolean.class).invoke(card, true); } catch (Exception ex) {}
                chiNhanhDangChon = chiNhanh;
                btnXacNhan.setEnabled(true);
            }
        });

        return card;
    }

    private void btnXacNhanActionPerformed(java.awt.event.ActionEvent evt) {                                           
        if (chiNhanhDangChon != null) {
            boolean isClosed = "Đóng cửa".equalsIgnoreCase(chiNhanhDangChon.trangThai) || "Ngừng hoạt động".equalsIgnoreCase(chiNhanhDangChon.trangThai);
            if (isClosed) {
                JOptionPane.showMessageDialog(this, "Chi nhánh này hiện đang đóng cửa. Vui lòng chọn chi nhánh khác!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Xử lý chuyển Panel tại đây...
            System.out.println("Đã chọn chi nhánh: " + chiNhanhDangChon.ma);
            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (window instanceof com.wms.view.TrangChuHoiVien.TrangChuHoiVienForm) {
                ((com.wms.view.TrangChuHoiVien.TrangChuHoiVienForm) window).showPanel(new XemSoDoKhongGianForm(chiNhanhDangChon.ma, chiNhanhDangChon.ten)
                );
            }
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSubTitle = new javax.swing.JLabel();
        scrollDanhSach = new javax.swing.JScrollPane();
        pnDanhSachChiNhanh = new javax.swing.JPanel();
        btnXacNhan = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(254, 248, 250));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 640));
        pnMain.setLayout(null);

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblTitle.setText("Hệ thống Chi nhánh Spring");
        pnMain.add(lblTitle);
        lblTitle.setBounds(40, 20, 400, 32);

        lblSubTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSubTitle.setForeground(new java.awt.Color(136, 136, 136));
        lblSubTitle.setText("Vui lòng chọn một chi nhánh để xem sơ đồ và tiến hành đặt chỗ");
        pnMain.add(lblSubTitle);
        lblSubTitle.setBounds(40, 55, 500, 20);

        scrollDanhSach.setBorder(null);
        scrollDanhSach.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pnDanhSachChiNhanh.setBackground(new java.awt.Color(254, 248, 250));
        scrollDanhSach.setViewportView(pnDanhSachChiNhanh);

        pnMain.add(scrollDanhSach);
        scrollDanhSach.setBounds(40, 100, 970, 430);

        btnXacNhan.setBackground(new java.awt.Color(235, 94, 141));
        btnXacNhan.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnXacNhan.setForeground(new java.awt.Color(255, 255, 255));
        btnXacNhan.setText("Xem sơ đồ không gian");
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(this::btnXacNhanActionPerformed);
        pnMain.add(btnXacNhan);
        btnXacNhan.setBounds(760, 550, 250, 50);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnXacNhan;
    private javax.swing.JLabel lblSubTitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnDanhSachChiNhanh;
    private javax.swing.JPanel pnMain;
    private javax.swing.JScrollPane scrollDanhSach;
    // End of variables declaration//GEN-END:variables
}


