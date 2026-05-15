/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.wms.view.TrangChuQuanLy.QuanLyKho;

import com.wms.controller.TrangChuQuanLy.QuanLyKho.QuanLyKhoController;

/**
 *
 * @author Thinkapd T14s
 */
public class QuanLyKhoForm extends javax.swing.JPanel {

    /**
     * Creates new form QuanLyKhoForm
     */
    private com.wms.controller.TrangChuQuanLy.QuanLyKho.QuanLyKhoController controller;
    private java.io.File currentSelectedFile = null;

    public QuanLyKhoForm() {
        initComponents();
        controller = new com.wms.controller.TrangChuQuanLy.QuanLyKho.QuanLyKhoController(this);
        setupDynamicBehavior();
        loadDataNhanVienVaLoaiDV();
        controller.loadData("");
        
        
        java.awt.event.ActionListener chonFileAction = e -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Chọn hóa đơn / chứng từ");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh & PDF (*.jpg, *.png, *.pdf)", "jpg", "jpeg", "png", "pdf"));
            
            if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                currentSelectedFile = fileChooser.getSelectedFile();
                String fileName = currentSelectedFile.getName();
                lblTrangThaiFile.setText("Thành công: " + shortenString(fileName, 20));
                lblTrangThaiFile.setForeground(new java.awt.Color(0, 128, 0));
                lblTrangThaiFile.setToolTipText(fileName);
                btnSuaFile.setVisible(true);
            }
        };
        btnChonFile.addActionListener(chonFileAction);
        btnSuaFile.addActionListener(chonFileAction);
        btnSuaFile.setVisible(false);

        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    controller.loadData(txtTimKiem.getText().trim());
                }
            }
        });

        txtGiaNhap.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { formatGiaNhap(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { formatGiaNhap(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { formatGiaNhap(); }
        });
    }

    private boolean isFormatting = false;
    private static final java.text.DecimalFormat FORMAT_TIEN = new java.text.DecimalFormat("#,###");

    private void formatGiaNhap() {
        if (isFormatting) return;
        javax.swing.SwingUtilities.invokeLater(() -> {
            isFormatting = true;
            try {
                String text = txtGiaNhap.getText().replace(",", "").replace(".", "");
                if (!text.isEmpty()) {
                    long value = Long.parseLong(text);
                    String formatted = FORMAT_TIEN.format(value);
                    if (!txtGiaNhap.getText().equals(formatted)) {
                        txtGiaNhap.setText(formatted);
                    }
                }
            } catch (NumberFormatException ex) {
                // Ignore
            } finally {
                isFormatting = false;
            }
        });
    }

    private void loadDataNhanVienVaLoaiDV() {
        if (controller == null) return;
        
        com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO currentUser = com.wms.controller.TrangChuGioiThieu.DangNhapController.getCurrentUser();
        if (currentUser != null) {
            cbNhanVien.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{currentUser.getHoTen()}));
            cbNhanVien.setEnabled(false);
        } else {
            java.util.List<String> dsNhanVien = controller.getDSNhanVien();
            javax.swing.DefaultComboBoxModel<String> nvModel = new javax.swing.DefaultComboBoxModel<>();
            nvModel.addElement("-- Chọn nhân viên --"); 
            for (String nv : dsNhanVien) nvModel.addElement(nv);
            cbNhanVien.setModel(nvModel);
        }

        cbNhanVien.setEditable(false);
        cbLoaiDichVu.setEditable(false);
        cbTenDichVu.setEditable(false);
        txtNiemYet.setEditable(false);
        txtNiemYet.setBackground(new java.awt.Color(240, 240, 240));

        java.util.List<String> dsLoai = controller.getDSLoaiDichVu();
        javax.swing.DefaultComboBoxModel<String> loaiModel = new javax.swing.DefaultComboBoxModel<>();
        loaiModel.addElement("-- Chọn loại dịch vụ --");
        for (String loai : dsLoai) loaiModel.addElement(loai);
        cbLoaiDichVu.setModel(loaiModel);
        
        cbTenDichVu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"-- Chọn tên dịch vụ --"}));
        txtGiaNhap.setText("");
        txtNiemYet.setText("");
    }

    private void setupDynamicBehavior() {
        cbLoaiDichVu.addActionListener(e -> updateTheoLoaiDichVu());
        cbTenDichVu.addActionListener(e -> updateDonGia());
    }

    private void updateDonGia() {
        Object item = cbTenDichVu.getSelectedItem();
        if (item == null || item.toString().isEmpty() || item.toString().startsWith("--")) {
            txtNiemYet.setText("");
            return;
        }
        String tenDV = item.toString().trim();
        double donGia = controller.layDonGiaDichVu(tenDV);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        txtNiemYet.setText(donGia > 0 ? df.format(donGia) : "0");
    }

    private void updateTheoLoaiDichVu() {
        Object item = cbLoaiDichVu.getSelectedItem();
        String loaiDV = (item != null) ? item.toString().trim() : "";
        
        boolean isTienIch = loaiDV.toLowerCase().contains("tiện ích");
        lblChungTuTitle.setVisible(!isTienIch);
        btnChonFile.setVisible(!isTienIch);
        lblTrangThaiFile.setVisible(!isTienIch);
        btnSuaFile.setVisible(!isTienIch && currentSelectedFile != null);
        
        if (controller != null && !loaiDV.isEmpty() && !loaiDV.startsWith("--")) {
            java.util.List<String> dsTen = controller.getDSTenDichVuTheoLoai(loaiDV);
            javax.swing.DefaultComboBoxModel<String> tenModel = new javax.swing.DefaultComboBoxModel<>();
            tenModel.addElement("-- Chọn tên dịch vụ --");
            for (String ten : dsTen) {
                tenModel.addElement(ten);
            }
            Object currentTyped = cbTenDichVu.getSelectedItem();
            cbTenDichVu.setModel(tenModel);
            if (currentTyped != null && !currentTyped.toString().isEmpty()) {
                cbTenDichVu.setSelectedItem(currentTyped);
            }
            updateDonGia();
        } else {
            cbTenDichVu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"-- Chọn tên dịch vụ --"}));
            txtNiemYet.setText("");
        }
    }

    private String shortenString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        int half = (maxLength - 3) / 2;
        return str.substring(0, half) + "..." + str.substring(str.length() - half);
    }

    public void hienThiDuLieu(java.util.List<com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO> danhSach) {
        if (danhSach != null && !danhSach.isEmpty()) {
            danhSach.sort((d1, d2) -> {
                boolean isThueGio1 = "Thuê thêm giờ".equalsIgnoreCase(d1.getTenDV());
                boolean isThueGio2 = "Thuê thêm giờ".equalsIgnoreCase(d2.getTenDV());
                if (isThueGio1 && !isThueGio2) return -1;
                if (!isThueGio1 && isThueGio2) return 1;

                boolean isTienIch1 = "Tiện ích".equalsIgnoreCase(d1.getTenLoaiDV());
                boolean isTienIch2 = "Tiện ích".equalsIgnoreCase(d2.getTenLoaiDV());
                if (isTienIch1 && !isTienIch2) return -1;
                if (!isTienIch1 && isTienIch2) return 1;

                String ten1 = d1.getTenDV() != null ? d1.getTenDV() : "";
                String ten2 = d2.getTenDV() != null ? d2.getTenDV() : "";
                return ten1.compareToIgnoreCase(ten2);
            });
        }

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblKho.getModel();
        model.setRowCount(0); 
        int stt = 1;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###"); 

        if (danhSach != null) {
            for (com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO dv : danhSach) {
                String hienThiSoLuong = (dv.getSoLuong() == null) ? "-" : String.valueOf(dv.getSoLuong());
                model.addRow(new Object[]{
                    stt++, dv.getMaDV(), dv.getTenDV(), dv.getTenLoaiDV(),
                    df.format(dv.getDonGia()), hienThiSoLuong, dv.getTrangThaiDV()
                });
            }
        }
    }

    public void hienThiThongBaoLoi(String thongBao) {
        javax.swing.JOptionPane.showMessageDialog(this, thongBao, "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        pnLeft = new javax.swing.JPanel();
        lblListTitle = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKho = new javax.swing.JTable();
        pnRight = new javax.swing.JPanel();
        lblDetailTitle = new javax.swing.JLabel();
        lblNhanVien = new javax.swing.JLabel();
        cbNhanVien = new javax.swing.JComboBox<>();
        lblLoaiDichVu = new javax.swing.JLabel();
        cbLoaiDichVu = new javax.swing.JComboBox<>();
        lblTenDichVu = new javax.swing.JLabel();
        cbTenDichVu = new javax.swing.JComboBox<>();
        lblSoLuong = new javax.swing.JLabel();
        spnSoLuong = new javax.swing.JSpinner();
        lblChungTuTitle = new javax.swing.JLabel();
        btnChonFile = new javax.swing.JButton();
        lblTrangThaiFile = new javax.swing.JLabel();
        btnSuaFile = new javax.swing.JButton();
        btnLuu = new javax.swing.JButton();
        cbLoaiDichVu1 = new javax.swing.JComboBox<>();
        lblLoaiDichVu1 = new javax.swing.JLabel();
        txtGiaNhap = new javax.swing.JTextField();
        lblGiaNhap = new javax.swing.JLabel();
        txtNiemYet = new javax.swing.JTextField();
        lblToaDoX1 = new javax.swing.JLabel();
        btnLamMoi = new javax.swing.JButton();
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(254, 248, 250));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 640));
        pnMain.setLayout(null);

        pnLeft.setBackground(new java.awt.Color(255, 255, 255));
        pnLeft.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeft.setLayout(null);

        lblListTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblListTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblListTitle.setText("DANH SÁCH TRONG KHO");
        pnLeft.add(lblListTitle);
        lblListTitle.setBounds(20, 15, 250, 30);

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtTimKiem.setToolTipText("Tìm kiếm dịch vụ...");
        pnLeft.add(txtTimKiem);
        txtTimKiem.setBounds(20, 60, 460, 35);

        btnTimKiem.setBackground(new java.awt.Color(235, 94, 141));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        btnTimKiem.setText("Tìm");
        btnTimKiem.addActionListener(this::btnTimKiemActionPerformed);
        pnLeft.add(btnTimKiem);
        btnTimKiem.setBounds(490, 60, 80, 35);

        tblKho.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "STT", "Mã DV", "Tên Dịch Vụ", "Loại DV", "Đơn Giá (VNĐ)", "Số Lượng Tồn", "Trạng thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblKho.setRowHeight(35);
        tblKho.setSelectionBackground(new java.awt.Color(235, 94, 141));
        jScrollPane1.setViewportView(tblKho);

        pnLeft.add(jScrollPane1);
        jScrollPane1.setBounds(20, 115, 550, 395);

        pnMain.add(pnLeft);
        pnLeft.setBounds(450, 70, 590, 530);

        pnRight.setBackground(new java.awt.Color(255, 255, 255));
        pnRight.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRight.setLayout(null);

        lblDetailTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDetailTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblDetailTitle.setText("NHẬP KHO DỊCH VỤ");
        pnRight.add(lblDetailTitle);
        lblDetailTitle.setBounds(20, 15, 200, 30);

        lblNhanVien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNhanVien.setForeground(new java.awt.Color(35, 30, 48));
        lblNhanVien.setText("Nhân viên nhập");
        pnRight.add(lblNhanVien);
        lblNhanVien.setBounds(20, 60, 360, 20);

        cbNhanVien.setEditable(true);
        cbNhanVien.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(cbNhanVien);
        cbNhanVien.setBounds(20, 85, 360, 35);

        lblLoaiDichVu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoaiDichVu.setForeground(new java.awt.Color(35, 30, 48));
        lblLoaiDichVu.setText("Loại dịch vụ");
        pnRight.add(lblLoaiDichVu);
        lblLoaiDichVu.setBounds(20, 135, 360, 20);

        cbLoaiDichVu.setEditable(true);
        cbLoaiDichVu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(cbLoaiDichVu);
        cbLoaiDichVu.setBounds(20, 160, 360, 35);

        lblTenDichVu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenDichVu.setForeground(new java.awt.Color(35, 30, 48));
        lblTenDichVu.setText("Tên dịch vụ");
        pnRight.add(lblTenDichVu);
        lblTenDichVu.setBounds(20, 210, 180, 20);

        cbTenDichVu.setEditable(true);
        cbTenDichVu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(cbTenDichVu);
        cbTenDichVu.setBounds(20, 235, 170, 35);

        lblSoLuong.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSoLuong.setForeground(new java.awt.Color(35, 30, 48));
        lblSoLuong.setText("Số lượng thêm");
        pnRight.add(lblSoLuong);
        lblSoLuong.setBounds(200, 210, 180, 20);

        spnSoLuong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(spnSoLuong);
        spnSoLuong.setBounds(200, 235, 180, 35);

        lblChungTuTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChungTuTitle.setForeground(new java.awt.Color(35, 30, 48));
        lblChungTuTitle.setText("Giấy/Hóa đơn");
        pnRight.add(lblChungTuTitle);
        lblChungTuTitle.setBounds(20, 365, 360, 20);

        btnChonFile.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnChonFile.setText("Chọn Ảnh/File...");
        pnRight.add(btnChonFile);
        btnChonFile.setBounds(20, 390, 140, 35);

        lblTrangThaiFile.setFont(new java.awt.Font("Segoe UI", 2, 13)); // NOI18N
        lblTrangThaiFile.setForeground(new java.awt.Color(255, 0, 0));
        lblTrangThaiFile.setText("Chưa chọn file (Bắt buộc)");
        pnRight.add(lblTrangThaiFile);
        lblTrangThaiFile.setBounds(20, 430, 250, 25);

        btnSuaFile.setText("Thay đổi ảnh/ file");
        btnSuaFile.addActionListener(this::btnSuaFileActionPerformed);
        pnRight.add(btnSuaFile);
        btnSuaFile.setBounds(230, 390, 150, 35);

        btnLuu.setBackground(new java.awt.Color(235, 94, 141));
        btnLuu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLuu.setForeground(new java.awt.Color(255, 255, 255));
        btnLuu.setText("Xác nhận nhập kho");
        btnLuu.addActionListener(this::btnLuuActionPerformed);
        pnRight.add(btnLuu);
        btnLuu.setBounds(160, 475, 220, 40);

        cbLoaiDichVu1.setEditable(true);
        cbLoaiDichVu1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(cbLoaiDichVu1);
        cbLoaiDichVu1.setBounds(20, 160, 360, 35);

        lblLoaiDichVu1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoaiDichVu1.setForeground(new java.awt.Color(35, 30, 48));
        lblLoaiDichVu1.setText("Loại dịch vụ");
        pnRight.add(lblLoaiDichVu1);
        lblLoaiDichVu1.setBounds(20, 135, 360, 20);

        txtGiaNhap.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnRight.add(txtGiaNhap);
        txtGiaNhap.setBounds(20, 310, 170, 30);

        lblGiaNhap.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGiaNhap.setForeground(new java.awt.Color(35, 30, 48));
        lblGiaNhap.setText("Giá nhập");
        pnRight.add(lblGiaNhap);
        lblGiaNhap.setBounds(20, 290, 170, 18);

        txtNiemYet.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnRight.add(txtNiemYet);
        txtNiemYet.setBounds(200, 310, 180, 30);

        lblToaDoX1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblToaDoX1.setForeground(new java.awt.Color(35, 30, 48));
        lblToaDoX1.setText("Giá niêm yết");
        pnRight.add(lblToaDoX1);
        lblToaDoX1.setBounds(200, 290, 170, 18);

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(235, 94, 141));
        btnLamMoi.setText("Làm mới");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
        pnRight.add(btnLamMoi);
        btnLamMoi.setBounds(20, 475, 130, 40);

        pnMain.add(pnRight);
        pnRight.setBounds(20, 70, 400, 530);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ KHO DỊCH VỤ");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSuaFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaFileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSuaFileActionPerformed

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        if (controller != null) {
            controller.loadData(txtTimKiem.getText().trim());
        }
    }

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {
        txtTimKiem.setText("");
        if (controller != null) {
            controller.loadData("");
        }
        
        if (cbNhanVien.getItemCount() > 0) cbNhanVien.setSelectedIndex(0);
        if (cbLoaiDichVu.getItemCount() > 0) cbLoaiDichVu.setSelectedIndex(0);
        if (cbTenDichVu.getItemCount() > 0) cbTenDichVu.setSelectedIndex(0);
        spnSoLuong.setValue(0);
        txtGiaNhap.setText("");
        txtNiemYet.setText("");
        
        currentSelectedFile = null;
        lblTrangThaiFile.setText("Chưa chọn file (Bắt buộc)");
        lblTrangThaiFile.setForeground(java.awt.Color.RED);
        lblTrangThaiFile.setToolTipText(null);
        btnSuaFile.setVisible(false);
    }

    private void btnLuuActionPerformed(java.awt.event.ActionEvent evt) {
        Object nvSelected = cbNhanVien.getSelectedItem();
        Object loaiSelected = cbLoaiDichVu.getSelectedItem();
        Object tenSelected = cbTenDichVu.getSelectedItem();
        
        String nhanVien = (nvSelected != null) ? nvSelected.toString().trim() : "";
        String loaiDichVu = (loaiSelected != null) ? loaiSelected.toString().trim() : "";
        String tenDichVu = (tenSelected != null) ? tenSelected.toString().trim() : "";
        int soLuong = (int) spnSoLuong.getValue();
        
        if (nhanVien.isEmpty() || nhanVien.startsWith("--") || 
            loaiDichVu.isEmpty() || loaiDichVu.startsWith("--") || 
            tenDichVu.isEmpty() || tenDichVu.startsWith("--")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ Nhân viên, Loại dịch vụ và Tên dịch vụ!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (soLuong <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Số lượng nhập phải lớn hơn 0!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Gia hạn giờ".equalsIgnoreCase(tenDichVu) || "Tiện ích hệ thống".equalsIgnoreCase(loaiDichVu)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Dịch vụ hệ thống (Gia hạn giờ) không yêu cầu và không được phép nhập kho!", "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean isTienIch = loaiDichVu.toLowerCase().contains("tiện ích");
        if (!isTienIch && currentSelectedFile == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn Hóa đơn / chứng từ cho loại dịch vụ này!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tenFile = isTienIch ? "" : (currentSelectedFile != null ? currentSelectedFile.getName() : "");
        byte[] fileData = null;
        if (!isTienIch && currentSelectedFile != null) {
            try {
                fileData = java.nio.file.Files.readAllBytes(currentSelectedFile.toPath());
            } catch (java.io.IOException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        double GiaNhap = 0;
        try {
            String GiaNhapStr = txtGiaNhap.getText().trim().replace(",", "").replace(".", "");
            if (!GiaNhapStr.isEmpty()) {
                GiaNhap = Double.parseDouble(GiaNhapStr);
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Giá nhập không hợp lệ!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = controller.nhapKho(nhanVien, loaiDichVu, tenDichVu, soLuong, tenFile, GiaNhap, fileData);

        if (success) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nhập kho dịch vụ thành công!", "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            btnLamMoiActionPerformed(null);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Nhập kho thất bại. Vui lòng kiểm tra lại!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChonFile;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLuu;
    private javax.swing.JButton btnSuaFile;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JComboBox<String> cbLoaiDichVu;
    private javax.swing.JComboBox<String> cbLoaiDichVu1;
    private javax.swing.JComboBox<String> cbNhanVien;
    private javax.swing.JComboBox<String> cbTenDichVu;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblChungTuTitle;
    private javax.swing.JLabel lblDetailTitle;
    private javax.swing.JLabel lblGiaNhap;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblListTitle;
    private javax.swing.JLabel lblLoaiDichVu;
    private javax.swing.JLabel lblLoaiDichVu1;
    private javax.swing.JLabel lblNhanVien;
    private javax.swing.JLabel lblSoLuong;
    private javax.swing.JLabel lblTenDichVu;
    private javax.swing.JLabel lblToaDoX1;
    private javax.swing.JLabel lblTrangThaiFile;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeft;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnRight;
    private javax.swing.JSpinner spnSoLuong;
    private javax.swing.JTable tblKho;
    private javax.swing.JTextField txtGiaNhap;
    private javax.swing.JTextField txtNiemYet;
    private javax.swing.JTextField txtTimKiem;
    // End of variables declaration//GEN-END:variables
}


