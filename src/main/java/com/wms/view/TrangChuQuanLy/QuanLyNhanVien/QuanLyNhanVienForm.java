package com.wms.view.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.view.TrangChuQuanLy.TrangChuQuanLyForm;
import com.wms.view.TrangChuQuanLy.QuanLyVaiTro.QuanLyVaiTroForm;
import com.wms.controller.TrangChuQuanLy.QuanLyNhanVien.QuanLyNhanVienController;
import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.NhanVienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class QuanLyNhanVienForm extends javax.swing.JPanel {
    private final QuanLyNhanVienController controller = new QuanLyNhanVienController();
    private String maNVDangChon = null;
    private String maNDDangChon = null;
    private String maCNFilter = null;

    public QuanLyNhanVienForm() {
        initComponents();
        loadComboBoxData();
        setupListeners();
        apDungPhanQuyen();
        loadData();
        txtMaNV.setText(controller.generateMaNV());
    }

    private void setupListeners() {
        btnQuanLyHang.addActionListener(e -> btnQuanLyHangActionPerformed(e));
    }

    private void btnQuanLyHangActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.Frame parent = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        QuanLyHangTVForm dialog = new QuanLyHangTVForm(parent, true);
        dialog.setVisible(true);
    }

    private void apDungPhanQuyen() {
        com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO user = com.wms.controller.TrangChuGioiThieu.DangNhapController
                .getCurrentUser();
        if (user == null)
            return;

        // Nếu không phải Admin (VT01)
        if (!user.hasRole(com.wms.config.AppConstants.ROLE_ADMIN_CODE)) {
            // 1. Cố định chi nhánh
            maCNFilter = controller.layMaCNTuMaND(user.getMaND());
            if (maCNFilter != null) {
                for (int i = 0; i < cbxChiNhanh.getItemCount(); i++) {
                    String tenCN = cbxChiNhanh.getItemAt(i);
                    Object ma = cbxChiNhanh.getClientProperty("maCN_" + tenCN);
                    if (maCNFilter.equals(ma)) {
                        cbxChiNhanh.setSelectedIndex(i);
                        break;
                    }
                }
                cbxChiNhanh.setEnabled(false);
            }

            // 2. Chỉ hiển thị vai trò "Nhân viên" (Lọc bỏ Admin và Quản lý)
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("-- Chọn vai trò --");

            // Lấy danh sách gốc để lọc
            List<VaiTroDTO> dsVT = controller.layDanhSachVaiTroNhanVien();
            for (VaiTroDTO vt : dsVT) {
                String ten = vt.getTenVaiTro().toLowerCase();
                String ma = vt.getMaVaiTro();

                // Loại bỏ Admin (VT01) và các vai trò có chữ "Quản lý" hoặc "Admin"
                if (!ma.equals(com.wms.config.AppConstants.ROLE_ADMIN_CODE)
                        && !ten.contains("quản lý")
                        && !ten.contains("admin")) {
                    model.addElement(vt.getTenVaiTro());
                    cbxNhomQuyen.putClientProperty("maVT_" + vt.getTenVaiTro(), vt.getMaVaiTro());
                }
            }
            cbxNhomQuyen.setModel(model);

            // 3. Loại NV (Đã xóa combo này nên không cần lọc)

            // 4. Chỉ Admin mới được quản lý hạng thành viên
            btnQuanLyHang.setVisible(false);
        }
    }

    private void loadData() {
        List<Object[]> ds = controller.layDanhSachNhanVien(maCNFilter);
        hienThiLenBang(ds);
    }

    private void hienThiLenBang(List<Object[]> ds) {
        DefaultTableModel model = (DefaultTableModel) tblNhanVien.getModel();
        model.setRowCount(0);
        int row = 0;
        for (Object[] r : ds) {
            model.addRow(new Object[] {
                    r[0], r[1], r[2], r[4], r[5], r[6], r[7]
            });
            String key = (String) r[0]; // MaNV làm key duy nhất
            tblNhanVien.putClientProperty("maCN_" + key, r[11]);
            tblNhanVien.putClientProperty("maVT_" + key, r[12]);
            tblNhanVien.putClientProperty("maND_" + key, r[13]);
            tblNhanVien.putClientProperty("gioiTinh_" + key, r[8]);
            tblNhanVien.putClientProperty("email_" + key, r[9]);
            tblNhanVien.putClientProperty("luong_" + key, r[10] != null ? String.valueOf(r[10]) : "");
            tblNhanVien.putClientProperty("trangThai_" + key, r[7]);
            tblNhanVien.putClientProperty("ngaySinh_" + key, r[15]); // r[15] là NgaySinh
            tblNhanVien.putClientProperty("anhData_" + key, r[14]); // r[14] là AnhDaiDien (byte[])
            row++;
        }
    }

    private void loadComboBoxData() {
        cbxChiNhanh.removeAllItems();
        cbxChiNhanh.addItem("-- Chọn chi nhánh --");
        for (String[] cn : controller.layDanhSachChiNhanh()) {
            cbxChiNhanh.addItem(cn[1]);
            cbxChiNhanh.putClientProperty("maCN_" + cn[1], cn[0]);
        }
        cbxNhomQuyen.removeAllItems();
        cbxNhomQuyen.addItem("-- Chưa cấp quyền --");
        for (VaiTroDTO vt : controller.layDanhSachVaiTroNhanVien()) {
            cbxNhomQuyen.addItem(vt.getTenVaiTro());
            cbxNhomQuyen.putClientProperty("maVT_" + vt.getTenVaiTro(), vt.getMaVaiTro());
        }
    }

    private void lamMoiForm() {
        maNVDangChon = null;
        maNDDangChon = null;
        txtMaNV.setText(controller.generateMaNV());
        txtHoTen.setText("");
        txtSDT.setText("");
        txtEmail.setText("");
        txtLuong.setText("");
        txtMatKhau.setText("");
        txtNgaySinh.setText("");
        txtTimKiem.setText("");
        cbxGioiTinh.setSelectedIndex(0);
        cbxCaLam.setSelectedIndex(0);
        cbxTrangThai.setSelectedIndex(0);

        // Chỉ reset nếu không bị disable (Admin mới reset được)
        if (cbxChiNhanh.isEnabled()) {
            cbxChiNhanh.setSelectedIndex(0);
        }

        cbxNhomQuyen.setSelectedIndex(0);
        lblAnhDaiDien.setIcon(null);
        lblAnhDaiDien.setText("[Ảnh 3x4]");
        tblNhanVien.clearSelection();
    }

    private String getMaCNDangChon() {
        String tenCN = (String) cbxChiNhanh.getSelectedItem();
        if (tenCN == null || tenCN.startsWith("--"))
            return null;
        Object ma = cbxChiNhanh.getClientProperty("maCN_" + tenCN);
        return ma != null ? ma.toString() : null;
    }

    private String getMaVaiTroDangChon() {
        String tenVT = (String) cbxNhomQuyen.getSelectedItem();
        if (tenVT == null || tenVT.startsWith("--"))
            return null;
        Object ma = cbxNhomQuyen.getClientProperty("maVT_" + tenVT);
        return ma != null ? ma.toString() : null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        lblAnhDaiDien = new javax.swing.JLabel();
        btnDoiAnh = new javax.swing.JButton();
        lblMaNV = new javax.swing.JLabel();
        txtMaNV = new javax.swing.JTextField();
        lblGioiTinh = new javax.swing.JLabel();
        cbxGioiTinh = new javax.swing.JComboBox<>();
        lblHoTen = new javax.swing.JLabel();
        txtHoTen = new javax.swing.JTextField();
        lblSDT = new javax.swing.JLabel();
        txtSDT = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblLuong = new javax.swing.JLabel();
        txtLuong = new javax.swing.JTextField();
        lblCaLam = new javax.swing.JLabel();
        cbxCaLam = new javax.swing.JComboBox<>();
        lblChiNhanh = new javax.swing.JLabel();
        cbxChiNhanh = new javax.swing.JComboBox<>();
        lblTrangThai = new javax.swing.JLabel();
        cbxTrangThai = new javax.swing.JComboBox<>();
        lblNhomQuyen = new javax.swing.JLabel();
        cbxNhomQuyen = new javax.swing.JComboBox<>();
        btnThemMoi = new javax.swing.JButton();
        btnCapNhat = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        lblTimKiem = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblNhanVien = new javax.swing.JTable();
        txtMatKhau = new javax.swing.JTextField();
        lblSDT1 = new javax.swing.JLabel();
        txtNgaySinh = new javax.swing.JTextField();
        lblNgaySinh = new javax.swing.JLabel();
        btnQuanLyHang = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(255, 255, 255));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 640));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ NHÂN VIÊN");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        lblAnhDaiDien.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAnhDaiDien.setText("[Ảnh 3x4]");
        lblAnhDaiDien.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnMain.add(lblAnhDaiDien);
        lblAnhDaiDien.setBounds(20, 80, 130, 160);

        btnDoiAnh.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDoiAnh.setForeground(new java.awt.Color(235, 94, 141));
        btnDoiAnh.setText("Đổi ảnh");
        btnDoiAnh.addActionListener(this::btnDoiAnhActionPerformed);
        pnMain.add(btnDoiAnh);
        btnDoiAnh.setBounds(20, 250, 130, 30);

        lblMaNV.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaNV.setForeground(new java.awt.Color(35, 30, 48));
        lblMaNV.setText("Mã Nhân viên");
        pnMain.add(lblMaNV);
        lblMaNV.setBounds(170, 70, 100, 18);

        txtMaNV.setEditable(false);
        txtMaNV.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtMaNV.setBackground(new java.awt.Color(240, 240, 240));
        pnMain.add(txtMaNV);
        txtMaNV.setBounds(170, 90, 100, 30);

        lblGioiTinh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGioiTinh.setForeground(new java.awt.Color(35, 30, 48));
        lblGioiTinh.setText("Giới tính");
        pnMain.add(lblGioiTinh);
        lblGioiTinh.setBounds(290, 70, 100, 18);

        cbxGioiTinh.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxGioiTinh.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nam", "Nữ", "Khác" }));
        pnMain.add(cbxGioiTinh);
        cbxGioiTinh.setBounds(290, 90, 100, 30);

        lblHoTen.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblHoTen.setForeground(new java.awt.Color(35, 30, 48));
        lblHoTen.setText("Họ và tên (*)");
        pnMain.add(lblHoTen);
        lblHoTen.setBounds(170, 130, 220, 18);

        txtHoTen.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtHoTen.addActionListener(this::txtHoTenActionPerformed);
        pnMain.add(txtHoTen);
        txtHoTen.setBounds(170, 150, 220, 30);

        lblSDT.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSDT.setForeground(new java.awt.Color(35, 30, 48));
        lblSDT.setText("Mật khẩu tài khoản (*)");
        pnMain.add(lblSDT);
        lblSDT.setBounds(20, 290, 180, 18);

        txtSDT.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtSDT);
        txtSDT.setBounds(170, 200, 220, 30);

        lblEmail.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblEmail.setForeground(new java.awt.Color(35, 30, 48));
        lblEmail.setText("Email (*)");
        pnMain.add(lblEmail);
        lblEmail.setBounds(170, 230, 170, 18);

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtEmail);
        txtEmail.setBounds(170, 250, 220, 30);

        lblLuong.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLuong.setForeground(new java.awt.Color(35, 30, 48));
        lblLuong.setText("Lương cơ bản (VNĐ)");
        pnMain.add(lblLuong);
        lblLuong.setBounds(210, 290, 180, 18);

        txtLuong.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtLuong);
        txtLuong.setBounds(210, 310, 180, 30);

        lblCaLam.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblCaLam.setForeground(new java.awt.Color(35, 30, 48));
        lblCaLam.setText("Ca làm việc");
        pnMain.add(lblCaLam);
        lblCaLam.setBounds(20, 410, 170, 20);

        cbxCaLam.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxCaLam.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Ca sáng", "Ca chiều", "Ca tối", "Hành chính" }));
        pnMain.add(cbxCaLam);
        cbxCaLam.setBounds(20, 430, 180, 30);

        lblChiNhanh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChiNhanh.setForeground(new java.awt.Color(35, 30, 48));
        lblChiNhanh.setText("Chi nhánh công tác (*)");
        pnMain.add(lblChiNhanh);
        lblChiNhanh.setBounds(20, 350, 170, 18);

        cbxChiNhanh.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxChiNhanh.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Chọn chi nhánh --", "Chi nhánh Trung tâm" }));
        pnMain.add(cbxChiNhanh);
        cbxChiNhanh.setBounds(20, 370, 180, 30);

        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTrangThai.setForeground(new java.awt.Color(35, 30, 48));
        lblTrangThai.setText("Trạng thái làm việc (*)");
        pnMain.add(lblTrangThai);
        lblTrangThai.setBounds(210, 410, 180, 20);

        cbxTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxTrangThai.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Đang làm việc", "Nghỉ phép", "Đã nghỉ việc" }));
        pnMain.add(cbxTrangThai);
        cbxTrangThai.setBounds(210, 430, 180, 30);

        lblNhomQuyen.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNhomQuyen.setForeground(new java.awt.Color(35, 30, 48));
        lblNhomQuyen.setText("Vai trò (*)");
        pnMain.add(lblNhomQuyen);
        lblNhomQuyen.setBounds(20, 470, 370, 18);

        cbxNhomQuyen.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxNhomQuyen.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "Admin (Toàn quyền)", "Quản lý Chi nhánh", "Nhân viên phục vụ" }));
        pnMain.add(cbxNhomQuyen);
        cbxNhomQuyen.setBounds(20, 490, 180, 40);

        btnThemMoi.setBackground(new java.awt.Color(235, 94, 141));
        btnThemMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThemMoi.setForeground(new java.awt.Color(255, 255, 255));
        btnThemMoi.setText("Thêm mới");
        btnThemMoi.addActionListener(this::btnThemMoiActionPerformed);
        pnMain.add(btnThemMoi);
        btnThemMoi.setBounds(210, 490, 180, 40);

        btnCapNhat.setBackground(new java.awt.Color(235, 94, 141));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật");
        btnCapNhat.addActionListener(this::btnCapNhatActionPerformed);
        pnMain.add(btnCapNhat);
        btnCapNhat.setBounds(210, 540, 180, 40);

        btnHuy.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnHuy.setForeground(new java.awt.Color(235, 94, 141));
        btnHuy.setText("Làm mới");
        btnHuy.addActionListener(this::btnHuyActionPerformed);
        pnMain.add(btnHuy);
        btnHuy.setBounds(20, 540, 180, 40);

        lblTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTimKiem.setForeground(new java.awt.Color(35, 30, 48));
        lblTimKiem.setText("Tìm kiếm (Tên/SĐT):");
        pnMain.add(lblTimKiem);
        lblTimKiem.setBounds(420, 80, 150, 35);

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtTimKiem);
        txtTimKiem.setBounds(575, 80, 325, 35);

        btnTimKiem.setBackground(new java.awt.Color(235, 94, 141));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        btnTimKiem.setText("Tìm");
        btnTimKiem.addActionListener(this::btnTimKiemActionPerformed);
        pnMain.add(btnTimKiem);
        btnTimKiem.setBounds(920, 80, 100, 35);

        tblNhanVien.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {

                },
                new String[] {
                        "Mã NV", "Họ và tên", "SĐT", "Ca làm", "Chi nhánh", "Vai trò", "Trạng thái"
                }) {
            boolean[] canEdit = new boolean[] {
                    false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tblNhanVien.setRowHeight(30);
        tblNhanVien.setSelectionBackground(new java.awt.Color(235, 94, 141));
        tblNhanVien.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblNhanVienMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblNhanVien);

        pnMain.add(jScrollPane1);
        jScrollPane1.setBounds(420, 130, 600, 500);

        txtMatKhau.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtMatKhau.addActionListener(this::txtMatKhauActionPerformed);
        pnMain.add(txtMatKhau);
        txtMatKhau.setBounds(20, 310, 180, 30);

        lblSDT1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSDT1.setForeground(new java.awt.Color(35, 30, 48));
        lblSDT1.setText("Số điện thoại (*)");
        pnMain.add(lblSDT1);
        lblSDT1.setBounds(170, 180, 220, 18);

        txtNgaySinh.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtNgaySinh);
        txtNgaySinh.setBounds(210, 370, 180, 30);

        lblNgaySinh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgaySinh.setForeground(new java.awt.Color(35, 30, 48));
        lblNgaySinh.setText("Ngày sinh (dd/MM/yyyy)");
        pnMain.add(lblNgaySinh);
        lblNgaySinh.setBounds(210, 350, 180, 18);

        btnQuanLyHang.setBackground(new java.awt.Color(35, 30, 48));
        btnQuanLyHang.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnQuanLyHang.setForeground(new java.awt.Color(255, 255, 255));
        btnQuanLyHang.setText("Quản lý Hạng Thành viên");
        pnMain.add(btnQuanLyHang);
        btnQuanLyHang.setBounds(20, 590, 370, 40);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtHoTenActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void btnDoiAnhActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Ảnh (*.jpg, *.png)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
                ImageIcon icon = new ImageIcon(data);
                Image img = icon.getImage().getScaledInstance(130, 160, Image.SCALE_SMOOTH);
                lblAnhDaiDien.setIcon(new ImageIcon(img));
                lblAnhDaiDien.setText("");
                lblAnhDaiDien.putClientProperty("anhData", data);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi đọc file ảnh!");
            }
        }
    }

    private void btnThemMoiActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            NhanVienDTO nv = getNVFormData();
            NguoiDungDTO nd = getNDFormData();

            if (controller.themNhanVien(nv, nd, txtHoTen.getText().trim(), getMaVaiTroDangChon(),
                    txtMatKhau.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                lamMoiForm();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {
        if (maNVDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            NhanVienDTO nv = getNVFormData();
            nv.setMaNV(maNVDangChon);
            nv.setMaND(maNDDangChon);
            NguoiDungDTO nd = getNDFormData();
            String hoTen = txtHoTen.getText().trim();
            String matKhau = txtMatKhau.getText().trim();

            if (controller.capNhatNhanVien(nv, nd, hoTen, getMaVaiTroDangChon(), matKhau)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                lamMoiForm();
                loadData();
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.isEmpty())
                msg = "Cập nhật thất bại (Lỗi không xác định)";
            JOptionPane.showMessageDialog(this, "Lỗi: " + msg, "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {
        lamMoiForm();
    }

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        String tuKhoa = txtTimKiem.getText().trim();
        hienThiLenBang(controller.timKiemNhanVien(tuKhoa, maCNFilter));
    }

    private void txtMatKhauActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private NhanVienDTO getNVFormData() {
        NhanVienDTO nv = new NhanVienDTO();
        nv.setMaNV(txtMaNV.getText().trim());
        String tenVT = (String) cbxNhomQuyen.getSelectedItem();
        if (tenVT != null && tenVT.toLowerCase().contains("quản lý")) {
            nv.setLoaiNV("Quản lý");
        } else if (tenVT != null
                && (tenVT.toLowerCase().contains("admin") || tenVT.toLowerCase().contains("quản trị"))) {
            nv.setLoaiNV("Quản trị Viên hệ thống");
        } else {
            nv.setLoaiNV("Nhân viên");
        }
        nv.setCaLamViec((String) cbxCaLam.getSelectedItem());
        nv.setTrangThaiLamViec((String) cbxTrangThai.getSelectedItem());
        nv.setMaCN(getMaCNDangChon());
        try {
            nv.setLuongCoBan(Double.parseDouble(txtLuong.getText().trim()));
        } catch (Exception e) {
            nv.setLuongCoBan(0.0);
        }
        return nv;
    }

    private NguoiDungDTO getNDFormData() {
        NguoiDungDTO nd = new NguoiDungDTO();
        nd.setEmail(txtEmail.getText().trim());
        nd.setSdt(txtSDT.getText().trim());
        nd.setGioiTinh((String) cbxGioiTinh.getSelectedItem());

        String nsStr = txtNgaySinh.getText().trim();
        if (!nsStr.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                java.util.Date d = sdf.parse(nsStr);
                nd.setNgaySinh(new java.sql.Date(d.getTime()));
            } catch (Exception e) {
                // Có thể ném exception hoặc để null
            }
        }

        Object data = lblAnhDaiDien.getClientProperty("anhData");
        nd.setAnhDaiDien(data instanceof byte[] ? (byte[]) data : null);
        return nd;
    }

    private void tblNhanVienMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tblNhanVien.getSelectedRow();
        if (row < 0)
            return;
        DefaultTableModel model = (DefaultTableModel) tblNhanVien.getModel();
        maNVDangChon = (String) model.getValueAt(row, 0);
        txtMaNV.setText(maNVDangChon);
        txtHoTen.setText((String) model.getValueAt(row, 1));
        txtSDT.setText((String) model.getValueAt(row, 2));
        // Đã cập nhật index theo cấu trúc 7 cột mới
        cbxCaLam.setSelectedItem(model.getValueAt(row, 3));
        cbxChiNhanh.setSelectedItem(model.getValueAt(row, 4));
        cbxNhomQuyen.setSelectedItem(model.getValueAt(row, 5));
        cbxTrangThai.setSelectedItem(model.getValueAt(row, 6));

        String key = (String) model.getValueAt(row, 0); // Lấy MaNV của dòng đang chọn

        Object objMaND = tblNhanVien.getClientProperty("maND_" + key);
        maNDDangChon = (objMaND != null) ? objMaND.toString() : null;

        Object objEmail = tblNhanVien.getClientProperty("email_" + key);
        txtEmail.setText(objEmail != null ? objEmail.toString() : "");

        Object objLuong = tblNhanVien.getClientProperty("luong_" + key);
        txtLuong.setText(objLuong != null ? objLuong.toString() : "");

        Object objGioiTinh = tblNhanVien.getClientProperty("gioiTinh_" + key);
        cbxGioiTinh.setSelectedItem(objGioiTinh);

        Object ns = tblNhanVien.getClientProperty("ngaySinh_" + key);
        if (ns instanceof java.util.Date) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            txtNgaySinh.setText(sdf.format((java.util.Date) ns));
        } else {
            txtNgaySinh.setText("");
        }

        Object anhData = tblNhanVien.getClientProperty("anhData_" + key);
        if (anhData instanceof byte[]) {
            byte[] data = (byte[]) anhData;
            ImageIcon icon = new ImageIcon(data);
            Image img = icon.getImage().getScaledInstance(130, 160, Image.SCALE_SMOOTH);
            lblAnhDaiDien.setIcon(new ImageIcon(img));
            lblAnhDaiDien.setText("");
            lblAnhDaiDien.putClientProperty("anhData", data);
        } else {
            lblAnhDaiDien.setIcon(null);
            lblAnhDaiDien.setText("[Ảnh 3x4]");
            lblAnhDaiDien.putClientProperty("anhData", null);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btnDoiAnh;
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnQuanLyHang;
    private javax.swing.JButton btnThemMoi;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JComboBox<String> cbxCaLam;
    private javax.swing.JComboBox<String> cbxChiNhanh;
    private javax.swing.JComboBox<String> cbxGioiTinh;
    private javax.swing.JComboBox<String> cbxNhomQuyen;
    private javax.swing.JComboBox<String> cbxTrangThai;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAnhDaiDien;
    private javax.swing.JLabel lblCaLam;
    private javax.swing.JLabel lblChiNhanh;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblGioiTinh;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblHoTen;
    private javax.swing.JLabel lblLuong;
    private javax.swing.JLabel lblMaNV;
    private javax.swing.JLabel lblNgaySinh;
    private javax.swing.JLabel lblNhomQuyen;
    private javax.swing.JLabel lblSDT;
    private javax.swing.JLabel lblSDT1;
    private javax.swing.JLabel lblTimKiem;
    private javax.swing.JLabel lblTrangThai;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnMain;
    private javax.swing.JTable tblNhanVien;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtHoTen;
    private javax.swing.JTextField txtLuong;
    private javax.swing.JTextField txtMaNV;
    private javax.swing.JTextField txtMatKhau;
    private javax.swing.JTextField txtNgaySinh;
    private javax.swing.JTextField txtSDT;
    private javax.swing.JTextField txtTimKiem;
    // End of variables declaration//GEN-END:variables
}
