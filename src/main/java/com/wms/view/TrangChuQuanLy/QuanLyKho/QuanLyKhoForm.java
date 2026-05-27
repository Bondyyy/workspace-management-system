/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.wms.view.TrangChuQuanLy.QuanLyKho;

import com.wms.controller.TrangChuQuanLy.QuanLyKho.QuanLyKhoController;
import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO;
import java.util.List;

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
    private String currentMaDV = "";
    private String selectedTenDichVu = "";
    private boolean fillingFormData = false;

    public QuanLyKhoForm() {
        initComponents();
        controller = new com.wms.controller.TrangChuQuanLy.QuanLyKho.QuanLyKhoController(this);
        com.wms.util.InputFormatUtil.attachThousandsFormatter(txtGiaNhap);
        setupDynamicBehavior();
        loadDataNhanVienVaLoaiDV();
        controller.loadData("");
        com.wms.util.TienIchFormQuanLy.apDung(this);

        java.awt.event.ActionListener chonFileAction = e -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Chọn hóa đơn / chứng từ");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Ảnh & PDF (*.jpg, *.png, *.pdf)", "jpg", "jpeg", "png", "pdf"));

            if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                currentSelectedFile = fileChooser.getSelectedFile();
                String fileName = currentSelectedFile.getName();
                lblTrangThaiFile.setText("Thành công: " + shortenString(fileName, 20));
                lblTrangThaiFile.setForeground(new java.awt.Color(0, 128, 0));
                lblTrangThaiFile.setToolTipText(fileName);
            }
        };
        btnChonFile.addActionListener(chonFileAction);

        // Wire btn Xem hoa don
        btnXemHoaDon.setEnabled(false);
        btnXemHoaDon.addActionListener(e -> xemHoaDonAction());

        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    controller.loadData(txtTimKiem.getText().trim());
                }
            }
        });

    }

    private void loadDataNhanVienVaLoaiDV() {
        if (controller == null)
            return;

        com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO currentUser = com.wms.controller.TrangChuGioiThieu.DangNhapController
                .getCurrentUser();
        if (currentUser != null) {
            cbNhanVien.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { currentUser.getHoTen() }));
            cbNhanVien.setEnabled(false);
        } else {
            java.util.List<String> dsNhanVien = controller.getDSNhanVien();
            javax.swing.DefaultComboBoxModel<String> nvModel = new javax.swing.DefaultComboBoxModel<>();
            nvModel.addElement("-- Chọn nhân viên --");
            for (String nv : dsNhanVien)
                nvModel.addElement(nv);
            cbNhanVien.setModel(nvModel);
        }

        cbNhanVien.setEditable(false);
        cbLoaiDichVu.setEditable(false);
        cbTenDichVu.setEditable(false);
        txtNiemYet.setEditable(false);
        txtNiemYet.setBackground(new java.awt.Color(240, 240, 240));

        txtSluongHienTai.setEditable(false);
        txtSluongHienTai.setBackground(new java.awt.Color(240, 240, 240));

        java.util.List<String> dsLoai = controller.getDSLoaiDichVu();
        javax.swing.DefaultComboBoxModel<String> loaiModel = new javax.swing.DefaultComboBoxModel<>();
        loaiModel.addElement("-- Chọn loại dịch vụ --");
        for (String loai : dsLoai)
            loaiModel.addElement(loai);
        cbLoaiDichVu.setModel(loaiModel);

        cbTenDichVu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"-- Chọn dịch vụ --"}));
        txtGiaNhap.setText("");
        txtNiemYet.setText("");
    }

    private void setupDynamicBehavior() {
        cbLoaiDichVu.addActionListener(e -> updateTheoLoaiDichVu());
        cbTenDichVu.addActionListener(e -> {
            if (fillingFormData) return;
            Object sel = cbTenDichVu.getSelectedItem();
            if (sel != null && !sel.toString().startsWith("--")) {
                onChonTenDichVu(sel.toString());
            }
        });

        tblKho.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tblKho.getSelectedRow();
                if (row >= 0) {
                    fillDataFromTable(row);
                }
            }
        });
    }

    private void onChonTenDichVu(String tenDV) {
        if (tenDV == null || tenDV.isEmpty()) return;
        // Tìm trong bảng dịch vụ theo tên để lấy mã DV, giá, số lượng tồn
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblKho.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String tenTrongBang = model.getValueAt(i, 2).toString();
            if (tenDV.equalsIgnoreCase(tenTrongBang)) {
                currentMaDV = model.getValueAt(i, 1).toString();
                selectedTenDichVu = tenDV;
                txtNiemYet.setText(model.getValueAt(i, 4).toString());
                txtSluongHienTai.setText(model.getValueAt(i, 6).toString());
                String giaNhap = model.getValueAt(i, 5).toString();
                txtGiaNhap.setText(giaNhap.equals("0") ? "" : giaNhap);
                btnXemHoaDon.setEnabled(true);
                tblKho.setRowSelectionInterval(i, i);
                tblKho.scrollRectToVisible(tblKho.getCellRect(i, 0, true));

                // Kiểm tra hóa đơn
                Object[] hoaDon = controller.xemHoaDon(currentMaDV);
                if (hoaDon != null) {
                    String tenFile = (String) hoaDon[1];
                    lblTrangThaiFile.setText("Hiện có: " + shortenString(tenFile, 20));
                    lblTrangThaiFile.setForeground(new java.awt.Color(0, 102, 0));
                } else {
                    lblTrangThaiFile.setText("Chưa chọn file (Bắt buộc)");
                    lblTrangThaiFile.setForeground(java.awt.Color.RED);
                }
                return;
            }
        }
        // Nếu không tìm thấy trong bảng, cập nhật đơn giá từ DB
        updateDonGia();
    }

    private void xemHoaDonAction() {
        if (currentMaDV == null || currentMaDV.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ trong bảng!",
                    "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] result = controller.xemHoaDon(currentMaDV);
        if (result == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nào cho dịch vụ này.",
                    "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        byte[] fileData = (byte[]) result[0];
        String tenFile = (String) result[1];
        try {
            String ext = "tmp";
            if (tenFile != null && tenFile.contains(".")) {
                ext = tenFile.substring(tenFile.lastIndexOf('.') + 1);
            }
            java.io.File tempFile = java.io.File.createTempFile("hoadon_" + currentMaDV + "_", "." + ext);
            tempFile.deleteOnExit();
            java.nio.file.Files.write(tempFile.toPath(), fileData);
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Hệ thống không hỗ trợ mở file tự động. File đã lưu tại: " + tempFile.getAbsolutePath(),
                        "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.io.IOException ex) {
            com.wms.util.MessageUtil.showError(this, "Lỗi khi mở hóa đơn.", ex);
        }
    }

    private void fillDataFromTable(int row) {
        try {
            fillingFormData = true;
            // Tạm thời gỡ bỏ action listener để tránh xung đột khi fill dữ liệu
            java.awt.event.ActionListener[] loaiListeners = cbLoaiDichVu.getActionListeners();
            for (java.awt.event.ActionListener l : loaiListeners)
                cbLoaiDichVu.removeActionListener(l);

            // Lấy dữ liệu từ bảng (STT, MaDV, TenDV, LoaiDV, Niêm yết, Nhập, Tồn, Trạng
            // thái)
            currentMaDV = tblKho.getValueAt(row, 1).toString();
            String ten = tblKho.getValueAt(row, 2).toString();
            selectedTenDichVu = ten;
            String loai = tblKho.getValueAt(row, 3).toString();
            String giaNiemYet = tblKho.getValueAt(row, 4).toString();

            // Điền loại dịch vụ
            cbLoaiDichVu.setSelectedItem(loai);

            // Cập nhật danh sách tên dịch vụ theo loại vừa chọn
            updateTheoLoaiDichVu();

            // Điền tên dịch vụ
            cbTenDichVu.setSelectedItem(ten);
            txtNiemYet.setText(giaNiemYet);

            // Hiển thị số lượng tồn hiện tại vào ô mới
            String soLuongTon = tblKho.getValueAt(row, 6).toString();
            txtSluongHienTai.setText(soLuongTon);

            // Điền Giá nhập từ bảng
            String giaNhap = tblKho.getValueAt(row, 5).toString();
            txtGiaNhap.setText(giaNhap.equals("0") ? "" : giaNhap);

            // Kiểm tra và hiển thị tên hóa đơn hiện tại
            Object[] hoaDon = controller.xemHoaDon(currentMaDV);
            if (hoaDon != null) {
                String tenFile = (String) hoaDon[1];
                lblTrangThaiFile.setText("Hiện có: " + shortenString(tenFile, 20));
                lblTrangThaiFile.setForeground(new java.awt.Color(0, 102, 0)); // Màu xanh đậm
            } else {
                lblTrangThaiFile.setText("Chưa chọn file (Bắt buộc)");
                lblTrangThaiFile.setForeground(java.awt.Color.RED);
            }

            // Bật nút xem hóa đơn khi chọn dịch vụ
            btnXemHoaDon.setEnabled(true);

            // Đăng ký lại action listener
            for (java.awt.event.ActionListener l : loaiListeners)
                cbLoaiDichVu.addActionListener(l);

        } catch (Exception e) {
            System.err.println("[QuanLyKhoForm] Loi khi dien du lieu tu bang: " + e.getMessage());
        } finally {
            fillingFormData = false;
        }
    }

    private void updateDonGia() {
        Object sel = cbTenDichVu.getSelectedItem();
        String tenDV = (sel != null) ? sel.toString().trim() : "";
        if (tenDV.isEmpty() || tenDV.startsWith("--")) {
            txtNiemYet.setText("");
            return;
        }
        double donGia = controller.layDonGiaDichVu(tenDV);
        txtNiemYet.setText(donGia > 0 ? com.wms.util.InputFormatUtil.formatThousands(donGia) : "0");
    }

    private void updateTheoLoaiDichVu() {
        Object item = cbLoaiDichVu.getSelectedItem();
        String loaiDV = (item != null) ? item.toString().trim() : "";

        boolean isTienIch = loaiDV.toLowerCase().contains("tiện ích");
        lblChungTuTitle.setVisible(!isTienIch);
        btnChonFile.setVisible(!isTienIch);
        lblTrangThaiFile.setVisible(!isTienIch);

        // Cập nhật danh sách tên dịch vụ theo loại
        javax.swing.DefaultComboBoxModel<String> tenModel = new javax.swing.DefaultComboBoxModel<>();
        tenModel.addElement("-- Chọn dịch vụ --");
        if (!loaiDV.isEmpty() && !loaiDV.startsWith("--")) {
            java.util.List<String> dsTen = controller.getDSTenDichVuTheoLoai(loaiDV);
            for (String ten : dsTen) {
                tenModel.addElement(ten);
            }
        }
        cbTenDichVu.setModel(tenModel);

        // Reset các trường liên quan
        if (!fillingFormData) {
            currentMaDV = "";
            selectedTenDichVu = "";
            txtNiemYet.setText("");
            txtSluongHienTai.setText("");
            txtGiaNhap.setText("");
            btnXemHoaDon.setEnabled(false);
        }
    }

    private String shortenString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength)
            return str;
        int half = (maxLength - 3) / 2;
        return str.substring(0, half) + "..." + str.substring(str.length() - half);
    }

    public void hienThiDuLieu(List<DichVuDTO> danhSach) {
        if (danhSach != null && !danhSach.isEmpty()) {
            danhSach.sort((d1, d2) -> {
                boolean isThueGio1 = "Thuê thêm giờ".equalsIgnoreCase(d1.getTenDV());
                boolean isThueGio2 = "Thuê thêm giờ".equalsIgnoreCase(d2.getTenDV());
                if (isThueGio1 && !isThueGio2)
                    return -1;
                if (!isThueGio1 && isThueGio2)
                    return 1;

                boolean isTienIch1 = "Tiện ích".equalsIgnoreCase(d1.getTenLoaiDV());
                boolean isTienIch2 = "Tiện ích".equalsIgnoreCase(d2.getTenLoaiDV());
                if (isTienIch1 && !isTienIch2)
                    return -1;
                if (!isTienIch1 && isTienIch2)
                    return 1;

                String ten1 = d1.getTenDV() != null ? d1.getTenDV() : "";
                String ten2 = d2.getTenDV() != null ? d2.getTenDV() : "";
                return ten1.compareToIgnoreCase(ten2);
            });
        }

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblKho.getModel();
        model.setRowCount(0);
        int stt = 1;
        if (danhSach != null) {
            for (com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO dv : danhSach) {
                // Logic hien thi So Luong: Chi Tien ich moi "Khong quan ly"
                boolean isTienIch = dv.getTenLoaiDV() != null && dv.getTenLoaiDV().toLowerCase().contains("tiện ích");
                String hienThiSoLuong;
                if (isTienIch) {
                    hienThiSoLuong = "Không quản lý";
                } else {
                    hienThiSoLuong = (dv.getSoLuong() == null) ? "0"
                            : com.wms.util.InputFormatUtil.formatThousands(dv.getSoLuong());
                }

                String hienThiGiaNhap = (dv.getGiaNhap() == null || dv.getGiaNhap() <= 0) ? "0"
                        : com.wms.util.InputFormatUtil.formatThousands(dv.getGiaNhap());

                model.addRow(new Object[] {
                        stt++,
                        dv.getMaDV(),
                        dv.getTenDV(),
                        dv.getTenLoaiDV(),
                        com.wms.util.InputFormatUtil.formatThousands(dv.getDonGia()),
                        hienThiGiaNhap,
                        hienThiSoLuong,
                        dv.getTrangThaiDV()
                });
            }
        }
        tblKho.clearSelection();
        tblKho.revalidate();
        tblKho.repaint();
    }

    private void refreshTableTheoDieuKienHienTai() {
        if (controller != null) {
            controller.loadData(txtTimKiem.getText().trim());
        }
    }

    private void clearForm() {
        if (cbNhanVien.getItemCount() > 0)
            cbNhanVien.setSelectedIndex(0);
        if (cbLoaiDichVu.getItemCount() > 0)
            cbLoaiDichVu.setSelectedIndex(0);
        cbTenDichVu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"-- Chọn dịch vụ --"}));
        spnSoLuong.setValue(0);
        txtGiaNhap.setText("");
        txtNiemYet.setText("");
        txtSluongHienTai.setText("");
        currentMaDV = "";
        selectedTenDichVu = "";

        currentSelectedFile = null;
        lblTrangThaiFile.setText("Chưa chọn file (Bắt buộc)");
        lblTrangThaiFile.setForeground(java.awt.Color.RED);
        lblTrangThaiFile.setToolTipText(null);
        btnXemHoaDon.setEnabled(false);
        tblKho.clearSelection();
    }

    public void hienThiThongBaoLoi(String thongBao) {
        com.wms.util.MessageUtil.showError(this, thongBao);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
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
        btnLuu = new javax.swing.JButton();
        lblLoaiDichVu1 = new javax.swing.JLabel();
        txtGiaNhap = new javax.swing.JTextField();
        lblGiaNhap = new javax.swing.JLabel();
        txtNiemYet = new javax.swing.JTextField();
        lblToaDoX1 = new javax.swing.JLabel();
        btnLamMoi = new javax.swing.JButton();
        txtSluongHienTai = new javax.swing.JTextField();
        lblToaDoX2 = new javax.swing.JLabel();
        btnXemHoaDon = new javax.swing.JButton();
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
                "STT", "Mã DV", "Tên Dịch Vụ", "Loại DV", "Đơn Giá (VNĐ)", "Giá Nhập (VNĐ)", "Số Lượng Tồn", "Trạng thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
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
        cbLoaiDichVu.setBounds(20, 160, 170, 35);

        lblTenDichVu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenDichVu.setForeground(new java.awt.Color(35, 30, 48));
        lblTenDichVu.setText("Tên dịch vụ");
        pnRight.add(lblTenDichVu);
        lblTenDichVu.setBounds(210, 130, 180, 20);

        cbTenDichVu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(cbTenDichVu);
        cbTenDichVu.setBounds(210, 160, 170, 35);

        lblSoLuong.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSoLuong.setForeground(new java.awt.Color(35, 30, 48));
        lblSoLuong.setText("Số lượng thêm");
        pnRight.add(lblSoLuong);
        lblSoLuong.setBounds(20, 210, 180, 20);

        spnSoLuong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(spnSoLuong);
        spnSoLuong.setBounds(20, 230, 170, 35);

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

        btnLuu.setBackground(new java.awt.Color(235, 94, 141));
        btnLuu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLuu.setForeground(new java.awt.Color(255, 255, 255));
        btnLuu.setText("Xác nhận nhập kho");
        btnLuu.addActionListener(this::btnLuuActionPerformed);
        pnRight.add(btnLuu);
        btnLuu.setBounds(180, 475, 200, 40);

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
        txtNiemYet.setBounds(210, 310, 170, 30);

        lblToaDoX1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblToaDoX1.setForeground(new java.awt.Color(35, 30, 48));
        lblToaDoX1.setText("Giá niêm yết");
        pnRight.add(lblToaDoX1);
        lblToaDoX1.setBounds(210, 290, 170, 18);

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(235, 94, 141));
        btnLamMoi.setText("Làm mới");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
        pnRight.add(btnLamMoi);
        btnLamMoi.setBounds(20, 475, 140, 40);

        txtSluongHienTai.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnRight.add(txtSluongHienTai);
        txtSluongHienTai.setBounds(210, 230, 170, 35);

        lblToaDoX2.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblToaDoX2.setForeground(new java.awt.Color(35, 30, 48));
        lblToaDoX2.setText("Số lượng hiện tại");
        pnRight.add(lblToaDoX2);
        lblToaDoX2.setBounds(210, 210, 170, 18);

        btnXemHoaDon.setBackground(new java.awt.Color(35, 30, 48));
        btnXemHoaDon.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXemHoaDon.setForeground(new java.awt.Color(255, 255, 255));
        btnXemHoaDon.setText("Xem hoá đơn");
        pnRight.add(btnXemHoaDon);
        btnXemHoaDon.setBounds(180, 390, 200, 35);

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

    private void btnSuaFileActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnSuaFileActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnSuaFileActionPerformed

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        if (controller != null) {
            controller.loadData(txtTimKiem.getText().trim());
        }
    }

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {
        clearForm();
        refreshTableTheoDieuKienHienTai();
    }

    private void btnLuuActionPerformed(java.awt.event.ActionEvent evt) {
        Object nvSelected = cbNhanVien.getSelectedItem();
        Object loaiSelected = cbLoaiDichVu.getSelectedItem();

        String nhanVien = (nvSelected != null) ? nvSelected.toString().trim() : "";
        String loaiDichVu = (loaiSelected != null) ? loaiSelected.toString().trim() : "";
        Object tenDVSelected = cbTenDichVu.getSelectedItem();
        String tenDichVu = (tenDVSelected != null) ? tenDVSelected.toString().trim() : "";
        int soLuong = (int) spnSoLuong.getValue();

        if (nhanVien.isEmpty() || nhanVien.startsWith("--") ||
                loaiDichVu.isEmpty() || loaiDichVu.startsWith("--") ||
                tenDichVu.isEmpty() || tenDichVu.startsWith("--")) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn nhân viên, loại dịch vụ và chọn dịch vụ có sẵn trong bảng!", "Cảnh báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentMaDV == null || currentMaDV.isBlank()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Kho dịch vụ chỉ nhập tồn cho dịch vụ đã có. Vui lòng chọn một dịch vụ trong bảng.",
                    "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (soLuong <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Số lượng nhập phải lớn hơn 0!", "Cảnh báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Gia hạn giờ".equalsIgnoreCase(tenDichVu) || "Tiện ích hệ thống".equalsIgnoreCase(loaiDichVu)) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Dịch vụ hệ thống (Gia hạn giờ) không yêu cầu và không được phép nhập kho!", "Thông báo",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean isTienIch = loaiDichVu.toLowerCase().contains("tiện ích");
        if (!isTienIch && currentSelectedFile == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn Hóa đơn / chứng từ cho loại dịch vụ này!",
                    "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tenFile = isTienIch ? "" : (currentSelectedFile != null ? currentSelectedFile.getName() : "");
        byte[] fileData = null;
        if (!isTienIch && currentSelectedFile != null) {
            try {
                fileData = java.nio.file.Files.readAllBytes(currentSelectedFile.toPath());
            } catch (java.io.IOException e) {
                com.wms.util.MessageUtil.showError(this, "Lỗi khi đọc file chứng từ.", e);
                return;
            }
        }

        double GiaNhap = 0;
        if (txtGiaNhap.getText().trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập giá nhập.", "Cảnh báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            java.math.BigDecimal giaNhapValue = com.wms.util.InputFormatUtil.getBigDecimalValue(txtGiaNhap);
            if (giaNhapValue == null) {
                throw new NumberFormatException();
            }
            if (giaNhapValue.signum() < 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Giá nhập không được âm.", "Cảnh báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            GiaNhap = giaNhapValue.doubleValue();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Giá nhập phải là số hợp lệ.", "Cảnh báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success;
        try {
            success = controller.nhapKho(currentMaDV, nhanVien, loaiDichVu, tenDichVu, soLuong, tenFile, GiaNhap,
                    fileData);
        } catch (Exception e) {
            com.wms.util.MessageUtil.showError(this, e.getMessage(), e);
            return;
        }

        if (success) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nhập kho dịch vụ thành công!", "Thành công",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            btnLamMoiActionPerformed(null);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Nhập kho thất bại. Vui lòng kiểm tra lại!", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChonFile;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLuu;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXemHoaDon;
    private javax.swing.JComboBox<String> cbLoaiDichVu;
    private javax.swing.JComboBox<String> cbNhanVien;
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
    private javax.swing.JLabel lblToaDoX2;
    private javax.swing.JLabel lblTrangThaiFile;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeft;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnRight;
    private javax.swing.JSpinner spnSoLuong;
    private javax.swing.JTable tblKho;
    private javax.swing.JTextField txtGiaNhap;
    private javax.swing.JTextField txtNiemYet;
    private javax.swing.JTextField txtSluongHienTai;
    private javax.swing.JComboBox<String> cbTenDichVu;
    private javax.swing.JTextField txtTimKiem;
    // End of variables declaration//GEN-END:variables
}
