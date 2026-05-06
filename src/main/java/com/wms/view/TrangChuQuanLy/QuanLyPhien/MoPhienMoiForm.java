/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.wms.view.TrangChuQuanLy.QuanLyPhien;

/**
 *
 * @author Thinkapd T14s
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.Timer;

public class MoPhienMoiForm extends javax.swing.JPanel {

    // Khai báo bảng màu chuẩn Spring Workspace
    private final Color COLOR_AVAILABLE = Color.decode("#FFF0F5");
    private final Color COLOR_IN_USE = Color.decode("#E8F5E9");
    private final Color COLOR_MAINTENANCE = Color.decode("#F5F5F5");

    private final Color COLOR_PRIMARY = Color.decode("#eb5e8d");

    private String maCNHienTai = null;
    private com.wms.view.components.SoDoKhongGianPanel mapPanel;
    private com.wms.model.KhongGianDTO khongGianChonDTO = null;
    private java.util.List<com.wms.model.KhongGianDTO> dsKGHienTai;

    public MoPhienMoiForm(String maCN) {
        initComponents();

        if (maCN != null && !maCN.isEmpty()) {
            this.maCNHienTai = maCN;
        } else {
            // Xác định chi nhánh của nhân viên đang trực quầy (Fallback)
            com.wms.model.NguoiDungDTO user = com.wms.controller.DangNhapController.getCurrentUser();
            if (user != null) {
                this.maCNHienTai = new com.wms.dao.NhanVienDAO().layMaCNTuMaND(user.getMaND());
            }
        }

        initMap();
        initLegend();
        startClock();
    }

    // 1. Tải sơ đồ không gian từ Database theo tọa độ chi nhánh
    private void initMap() {
        pnSoDo.removeAll();
        pnSoDo.setLayout(new java.awt.BorderLayout()); 

        com.wms.dao.KhongGianDAO khongGianDAO = new com.wms.dao.KhongGianDAO();

        if (maCNHienTai != null) {
            dsKGHienTai = khongGianDAO.layTheoChiNhanh(maCNHienTai);
        } else {
            dsKGHienTai = khongGianDAO.layTatCaKhongGian();
        }

        if (dsKGHienTai == null || dsKGHienTai.isEmpty()) {
            pnSoDo.add(new JLabel("Không có dữ liệu sơ đồ cho chi nhánh này."), java.awt.BorderLayout.CENTER);
            pnSoDo.revalidate();
            pnSoDo.repaint();
            return;
        }

        mapPanel = new com.wms.view.components.SoDoKhongGianPanel();
        mapPanel.setOnTableClick(kg -> handleTableClick(kg));

        // Vẽ sơ đồ ban đầu
        mapPanel.veSoDo(dsKGHienTai, khongGianChonDTO != null ? khongGianChonDTO.getMaKG() : null);
        
        JScrollPane scroll = new JScrollPane(mapPanel);
        scroll.setBorder(null);
        pnSoDo.add(scroll, java.awt.BorderLayout.CENTER);

        pnSoDo.revalidate();
        pnSoDo.repaint();
    }

    // 2. Chú thích các loại màu bên dưới bản đồ
    private void initLegend() {
        pnChuThich.removeAll();
        pnChuThich.add(createLegendItem(COLOR_AVAILABLE, "Trống"));
        pnChuThich.add(createLegendItem(COLOR_IN_USE, "Đang sử dụng"));
        pnChuThich.add(createLegendItem(COLOR_MAINTENANCE, "Bảo trì"));
        pnChuThich.revalidate();
        pnChuThich.repaint();
    }

    private JLabel createLegendItem(Color color, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setIcon(new javax.swing.ImageIcon(createColorIcon(color)));
        return label;
    }

    private java.awt.Image createColorIcon(Color color) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(16, 16,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(0, 0, 15, 15);
        g2d.dispose();
        return img;
    }

    // 3. Xử lý khi click vào một bàn trên sơ đồ
    private void handleTableClick(com.wms.model.KhongGianDTO kg) {
        String status = kg.getTrangThaiKG() != null ? kg.getTrangThaiKG().trim() : "Trống";
        
        if ("Đang hoạt động".equals(status)) {
            JOptionPane.showMessageDialog(this, "Không gian này đang có khách sử dụng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("Bảo trì".equals(status)) {
            JOptionPane.showMessageDialog(this, "Không gian này đang bảo trì!", "Thông báo", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cập nhật DTO đang chọn
        khongGianChonDTO = kg;

        // Vẽ lại sơ đồ để hiển thị bàn đang chọn
        mapPanel.veSoDo(dsKGHienTai, kg.getMaKG());

        // Hiển thị thông tin sang Form bên phải
        txtKhongGianChon.setText(kg.getTenKG());
        txtLoaiKhongGian.setText(kg.getTenLoaiKG() != null ? kg.getTenLoaiKG() : "Không xác định");
    }

    // 4. Đồng hồ thời gian thực
    private void startClock() {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");
                txtThoiGianBatDau.setText(sdf.format(new Date()));
            }
        });
        timer.start();
    }

    // === CÁC SỰ KIỆN NÚT BẤM FORM BÊN PHẢI ===

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");
        txtKhongGianChon.setText("(Chưa chọn chỗ ngồi)");
        txtLoaiKhongGian.setText("");

        khongGianChonDTO = null;
        if (mapPanel != null) {
            mapPanel.veSoDo(dsKGHienTai, null);
        }
    }

    private void btnMoPhienActionPerformed(java.awt.event.ActionEvent evt) {
        String tenKH = txtTenKhachHang.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();
        String khongGian = txtKhongGianChon.getText();

        if (tenKH.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên và Số điện thoại khách hàng!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (khongGianChonDTO == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Không gian/Chỗ ngồi trên sơ đồ bên trái!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKG = khongGianChonDTO.getMaKG();

        // Tìm hoặc tạo mới khách hàng
        com.wms.dao.KhachHangDAO khDAO = new com.wms.dao.KhachHangDAO();
        java.util.List<com.wms.model.HoiVienDTO> dsKH = khDAO.search(sdt);
        String maKH = null;

        if (dsKH != null && !dsKH.isEmpty()) {
            maKH = dsKH.get(0).getMaKH();
        } else {
            com.wms.model.HoiVienDTO newKH = new com.wms.model.HoiVienDTO();
            newKH.setHoTen(tenKH);
            newKH.setSdt(sdt);
            newKH.setTrangThai("Đang hoạt động");
            try {
                khDAO.insert(newKH);
                dsKH = khDAO.search(sdt);
                if (dsKH != null && !dsKH.isEmpty()) {
                    maKH = dsKH.get(0).getMaKH();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tạo thông tin khách hàng: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (maKH == null) {
            JOptionPane.showMessageDialog(this, "Không thể xác định Khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Tạo phiên làm việc mới
        com.wms.model.PhienLamViecDTO phien = new com.wms.model.PhienLamViecDTO();
        String maPhien = "PH" + System.currentTimeMillis();
        phien.setMaPhien(maPhien);
        phien.setMaKH(maKH);
        phien.setMaKG(maKG);
        phien.setTrangThaiPhien("Đang hoạt động");
        phien.setThoiGianBatDau(new java.sql.Timestamp(System.currentTimeMillis()));

        com.wms.dao.PhienLamViecDAO pDAO = new com.wms.dao.PhienLamViecDAO();
        if (pDAO.taoPhienLamViecMoi(phien)) {
            JOptionPane.showMessageDialog(this, "Mở phiên thành công cho khách hàng: " + tenKH + "\nTại: " + khongGian,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // Reset form và vẽ lại sơ đồ để cập nhật trạng thái mới
            khongGianChonDTO = null;
            initMap(); // Tải lại dữ liệu mới nhất từ DB để hiện màu xanh (Đang dùng)
            btnLamMoiActionPerformed(null);
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi mở phiên làm việc! Vui lòng thử lại.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        pnLeftMap = new javax.swing.JPanel();
        lblMapTitle = new javax.swing.JLabel();
        pnSoDo = new javax.swing.JPanel();
        pnChuThich = new javax.swing.JPanel();
        pnRightForm = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblTenKhachHang = new javax.swing.JLabel();
        txtTenKhachHang = new javax.swing.JTextField();
        lblSDT = new javax.swing.JLabel();
        txtSoDienThoai = new javax.swing.JTextField();
        lblKhongGianChon = new javax.swing.JLabel();
        txtKhongGianChon = new javax.swing.JTextField();
        lblLoaiKhongGian = new javax.swing.JLabel();
        txtLoaiKhongGian = new javax.swing.JTextField();
        lblThoiGianBatDau = new javax.swing.JLabel();
        txtThoiGianBatDau = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();
        btnMoPhien = new javax.swing.JButton();

        setBackground(new java.awt.Color(254, 248, 250));
        setPreferredSize(new java.awt.Dimension(1050, 640));
        setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("MỞ PHIÊN LÀM VIỆC TẠI QUẦY");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        pnLeftMap.setBackground(new java.awt.Color(255, 255, 255));
        pnLeftMap.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeftMap.setLayout(null);

        lblMapTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblMapTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblMapTitle.setText("SƠ ĐỒ KHÔNG GIAN");
        pnLeftMap.add(lblMapTitle);
        lblMapTitle.setBounds(20, 15, 200, 30);

        pnSoDo.setBackground(new java.awt.Color(255, 255, 255));
        pnSoDo.setLayout(new java.awt.GridLayout(4, 4, 10, 10));
        pnLeftMap.add(pnSoDo);
        pnSoDo.setBounds(20, 60, 610, 400);

        pnChuThich.setBackground(new java.awt.Color(255, 255, 255));
        pnChuThich
                .setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(240, 240, 240)));
        pnChuThich.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 15));
        pnLeftMap.add(pnChuThich);
        pnChuThich.setBounds(20, 470, 610, 50);

        add(pnLeftMap);
        pnLeftMap.setBounds(20, 80, 650, 530);

        pnRightForm.setBackground(new java.awt.Color(255, 255, 255));
        pnRightForm
                .setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRightForm.setLayout(null);

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblFormTitle.setText("THÔNG TIN KHÁCH HÀNG");
        pnRightForm.add(lblFormTitle);
        lblFormTitle.setBounds(20, 15, 300, 30);

        lblTenKhachHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenKhachHang.setForeground(new java.awt.Color(35, 30, 48));
        lblTenKhachHang.setText("Tên khách hàng (*)");
        pnRightForm.add(lblTenKhachHang);
        lblTenKhachHang.setBounds(20, 60, 300, 20);

        txtTenKhachHang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRightForm.add(txtTenKhachHang);
        txtTenKhachHang.setBounds(20, 80, 300, 35);

        lblSDT.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSDT.setForeground(new java.awt.Color(35, 30, 48));
        lblSDT.setText("Số điện thoại (*)");
        pnRightForm.add(lblSDT);
        lblSDT.setBounds(20, 125, 300, 20);

        txtSoDienThoai.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRightForm.add(txtSoDienThoai);
        txtSoDienThoai.setBounds(20, 145, 300, 35);

        lblKhongGianChon.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKhongGianChon.setForeground(new java.awt.Color(35, 30, 48));
        lblKhongGianChon.setText("Không gian đã chọn từ Sơ đồ");
        pnRightForm.add(lblKhongGianChon);
        lblKhongGianChon.setBounds(20, 195, 300, 20);

        txtKhongGianChon.setEditable(false);
        txtKhongGianChon.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        txtKhongGianChon.setForeground(new java.awt.Color(235, 94, 141));
        txtKhongGianChon.setBackground(new java.awt.Color(240, 240, 240));
        txtKhongGianChon.setText("(Chưa chọn chỗ ngồi)");
        pnRightForm.add(txtKhongGianChon);
        txtKhongGianChon.setBounds(20, 215, 300, 35);

        lblLoaiKhongGian.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoaiKhongGian.setForeground(new java.awt.Color(35, 30, 48));
        lblLoaiKhongGian.setText("Loại không gian");
        pnRightForm.add(lblLoaiKhongGian);
        lblLoaiKhongGian.setBounds(20, 265, 300, 20);

        txtLoaiKhongGian.setEditable(false);
        txtLoaiKhongGian.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtLoaiKhongGian.setBackground(new java.awt.Color(240, 240, 240));
        pnRightForm.add(txtLoaiKhongGian);
        txtLoaiKhongGian.setBounds(20, 285, 300, 35);

        lblThoiGianBatDau.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblThoiGianBatDau.setForeground(new java.awt.Color(35, 30, 48));
        lblThoiGianBatDau.setText("Thời gian bắt đầu phiên");
        pnRightForm.add(lblThoiGianBatDau);
        lblThoiGianBatDau.setBounds(20, 335, 300, 20);

        txtThoiGianBatDau.setEditable(false);
        txtThoiGianBatDau.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        txtThoiGianBatDau.setForeground(new java.awt.Color(102, 102, 102));
        txtThoiGianBatDau.setBackground(new java.awt.Color(240, 240, 240));
        pnRightForm.add(txtThoiGianBatDau);
        txtThoiGianBatDau.setBounds(20, 355, 300, 35);

        btnLamMoi.setBackground(new java.awt.Color(220, 53, 69));
        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(255, 255, 255));
        btnLamMoi.setText("Làm mới form");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
        pnRightForm.add(btnLamMoi);
        btnLamMoi.setBounds(20, 420, 300, 35);

        btnMoPhien.setBackground(new java.awt.Color(235, 94, 141));
        btnMoPhien.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMoPhien.setForeground(new java.awt.Color(255, 255, 255));
        btnMoPhien.setText("TẠO KHÁCH VÀ MỞ PHIÊN");
        btnMoPhien.addActionListener(this::btnMoPhienActionPerformed);
        pnRightForm.add(btnMoPhien);
        btnMoPhien.setBounds(20, 465, 300, 45);

        add(pnRightForm);
        pnRightForm.setBounds(690, 80, 340, 530);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnMoPhien;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblKhongGianChon;
    private javax.swing.JLabel lblLoaiKhongGian;
    private javax.swing.JLabel lblMapTitle;
    private javax.swing.JLabel lblSDT;
    private javax.swing.JLabel lblTenKhachHang;
    private javax.swing.JLabel lblThoiGianBatDau;
    private javax.swing.JPanel pnChuThich;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeftMap;
    private javax.swing.JPanel pnRightForm;
    private javax.swing.JPanel pnSoDo;
    private javax.swing.JTextField txtKhongGianChon;
    private javax.swing.JTextField txtLoaiKhongGian;
    private javax.swing.JTextField txtSoDienThoai;
    private javax.swing.JTextField txtTenKhachHang;
    private javax.swing.JTextField txtThoiGianBatDau;
    // End of variables declaration//GEN-END:variables
}
