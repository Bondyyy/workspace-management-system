package com.wms.view.TrangChu.TrangChuQuanLy.QuanLyPhieuGiamGia;

public class QuanLyPhieuGiamGiaForm extends javax.swing.JPanel {

    private com.wms.controller.PhieuGiamGiaController controller;
    private java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

    /**
     * Creates new form QuanLyPhieuGiamGiaForm
     */
    public QuanLyPhieuGiamGiaForm() {
        initComponents();
        controller = new com.wms.controller.PhieuGiamGiaController();
        loadDataToTable();
        generateNewID();
    }

    private void loadDataToTable() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblPhieuGiamGia.getModel();
        model.setRowCount(0);
        java.util.List<com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO> list = controller.layDanhSach();
        for (com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO dto : list) {
            model.addRow(new Object[]{
                dto.getMaPGG(),
                dto.getMaChuSoPGG(),
                dto.getGiaTriGiamGia(),
                dto.getGiaTriApDungToiThieu(),
                sdf.format(dto.getNgayBatDauApDung()),
                sdf.format(dto.getNgayKetThucApDung()),
                dto.getSlDaDung(),
                dto.getSlToiDa()
            });
        }
    }

    private void generateNewID() {
        String newID = "PGG" + System.currentTimeMillis() % 1000000;
        txtMaPGG.setText(newID);
    }

    private void btnThemMoiActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO dto = getFormData();
            if (controller.themMoi(dto)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Thêm phiếu giảm giá thành công!");
                loadDataToTable();
                btnHuyActionPerformed(null);
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO dto = getFormData();
            if (controller.capNhat(dto)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDataToTable();
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi cập nhật!");
        }
    }

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {
        String ma = txtMaPGG.getText();
        if (ma.isEmpty()) return;
        
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phiếu " + ma + "?");
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (controller.xoa(ma)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Đã xóa!");
                loadDataToTable();
                btnHuyActionPerformed(null);
            }
        }
    }

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {
        generateNewID();
        txtMaChuSoPGG.setText("");
        txtGiaTriGiamGia.setText("");
        txtGiaTriApDungToiThieu.setText("");
        txtSLToiDa.setText("");
        txtNgayBatDauApDung.setText("");
        txtNgayKetThucApDung.setText("");
    }

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        loadDataToTable();
    }

    private void tblPhieuGiamGiaMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tblPhieuGiamGia.getSelectedRow();
        if (row >= 0) {
            String ma = tblPhieuGiamGia.getValueAt(row, 0).toString();
            com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO dto = controller.timTheoMa(ma);
            if (dto != null) {
                txtMaPGG.setText(dto.getMaPGG());
                txtMaChuSoPGG.setText(dto.getMaChuSoPGG());
                txtGiaTriGiamGia.setText(String.valueOf(dto.getGiaTriGiamGia()));
                txtGiaTriApDungToiThieu.setText(String.valueOf(dto.getGiaTriApDungToiThieu()));
                txtSLToiDa.setText(String.valueOf(dto.getSlToiDa()));
                txtNgayBatDauApDung.setText(sdf.format(dto.getNgayBatDauApDung()));
                txtNgayKetThucApDung.setText(sdf.format(dto.getNgayKetThucApDung()));
            }
        }
    }

    private com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO getFormData() throws Exception {
        com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO dto = new com.wms.model.ThanhToan_KhuyenMai.PhieuGiamGiaDTO();
        dto.setMaPGG(txtMaPGG.getText().trim());
        dto.setMaChuSoPGG(txtMaChuSoPGG.getText().trim());
        dto.setGiaTriGiamGia(Double.parseDouble(txtGiaTriGiamGia.getText()));
        dto.setGiaTriApDungToiThieu(Double.parseDouble(txtGiaTriApDungToiThieu.getText()));
        dto.setSlToiDa(Integer.parseInt(txtSLToiDa.getText()));
        dto.setNgayBatDauApDung(sdf.parse(txtNgayBatDauApDung.getText()));
        dto.setNgayKetThucApDung(sdf.parse(txtNgayKetThucApDung.getText()));
        dto.setMaNV("ADMIN");
        return dto;
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
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        lblMaPGG = new javax.swing.JLabel();
        txtMaPGG = new javax.swing.JTextField();
        lblMaChuSoPGG = new javax.swing.JLabel();
        txtMaChuSoPGG = new javax.swing.JTextField();
        lblGiaTriGiamGia = new javax.swing.JLabel();
        txtGiaTriGiamGia = new javax.swing.JTextField();
        lblGiaTriApDungToiThieu = new javax.swing.JLabel();
        txtGiaTriApDungToiThieu = new javax.swing.JTextField();
        lblNgayBatDauApDung = new javax.swing.JLabel();
        txtNgayBatDauApDung = new javax.swing.JTextField();
        lblNgayKetThucApDung = new javax.swing.JLabel();
        txtNgayKetThucApDung = new javax.swing.JTextField();
        lblSLToiDa = new javax.swing.JLabel();
        txtSLToiDa = new javax.swing.JTextField();
        btnThemMoi = new javax.swing.JButton();
        btnCapNhat = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        lblTimKiem = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPhieuGiamGia = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(255, 255, 255));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 640));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ PHIẾU GIẢM GIÁ");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        lblMaPGG.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaPGG.setForeground(new java.awt.Color(35, 30, 48));
        lblMaPGG.setText("Mã PGG (Tự động)");
        pnMain.add(lblMaPGG);
        lblMaPGG.setBounds(20, 70, 170, 18);

        txtMaPGG.setEditable(false);
        txtMaPGG.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtMaPGG.setBackground(new java.awt.Color(240, 240, 240));
        pnMain.add(txtMaPGG);
        txtMaPGG.setBounds(20, 90, 170, 35);

        lblMaChuSoPGG.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaChuSoPGG.setForeground(new java.awt.Color(35, 30, 48));
        lblMaChuSoPGG.setText("Mã nhập khuyến mãi (*)");
        pnMain.add(lblMaChuSoPGG);
        lblMaChuSoPGG.setBounds(210, 70, 170, 18);

        txtMaChuSoPGG.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtMaChuSoPGG);
        txtMaChuSoPGG.setBounds(210, 90, 170, 35);

        lblGiaTriGiamGia.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGiaTriGiamGia.setForeground(new java.awt.Color(35, 30, 48));
        lblGiaTriGiamGia.setText("Giá trị giảm (VNĐ) (*)");
        pnMain.add(lblGiaTriGiamGia);
        lblGiaTriGiamGia.setBounds(20, 135, 170, 18);

        txtGiaTriGiamGia.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtGiaTriGiamGia);
        txtGiaTriGiamGia.setBounds(20, 155, 170, 35);

        lblGiaTriApDungToiThieu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGiaTriApDungToiThieu.setForeground(new java.awt.Color(35, 30, 48));
        lblGiaTriApDungToiThieu.setText("Đơn tối thiểu (VNĐ) (*)");
        pnMain.add(lblGiaTriApDungToiThieu);
        lblGiaTriApDungToiThieu.setBounds(210, 135, 170, 18);

        txtGiaTriApDungToiThieu.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtGiaTriApDungToiThieu);
        txtGiaTriApDungToiThieu.setBounds(210, 155, 170, 35);

        lblNgayBatDauApDung.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayBatDauApDung.setForeground(new java.awt.Color(35, 30, 48));
        lblNgayBatDauApDung.setText("Ngày BĐ (dd/MM/yyyy) (*)");
        pnMain.add(lblNgayBatDauApDung);
        lblNgayBatDauApDung.setBounds(20, 200, 170, 18);

        txtNgayBatDauApDung.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtNgayBatDauApDung);
        txtNgayBatDauApDung.setBounds(20, 220, 170, 35);

        lblNgayKetThucApDung.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayKetThucApDung.setForeground(new java.awt.Color(35, 30, 48));
        lblNgayKetThucApDung.setText("Ngày KT (dd/MM/yyyy) (*)");
        pnMain.add(lblNgayKetThucApDung);
        lblNgayKetThucApDung.setBounds(210, 200, 170, 18);

        txtNgayKetThucApDung.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtNgayKetThucApDung);
        txtNgayKetThucApDung.setBounds(210, 220, 170, 35);

        lblSLToiDa.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSLToiDa.setForeground(new java.awt.Color(35, 30, 48));
        lblSLToiDa.setText("Số lượng phát hành (*)");
        pnMain.add(lblSLToiDa);
        lblSLToiDa.setBounds(20, 265, 360, 18);

        txtSLToiDa.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtSLToiDa);
        txtSLToiDa.setBounds(20, 285, 360, 35);

        btnThemMoi.setBackground(new java.awt.Color(235, 94, 141));
        btnThemMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThemMoi.setForeground(new java.awt.Color(255, 255, 255));
        btnThemMoi.setText("Thêm mới");
        btnThemMoi.addActionListener(this::btnThemMoiActionPerformed);
        pnMain.add(btnThemMoi);
        btnThemMoi.setBounds(20, 340, 170, 40);

        btnCapNhat.setBackground(new java.awt.Color(235, 94, 141));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật");
        btnCapNhat.addActionListener(this::btnCapNhatActionPerformed);
        pnMain.add(btnCapNhat);
        btnCapNhat.setBounds(210, 340, 170, 40);

        btnXoa.setBackground(new java.awt.Color(220, 53, 69));
        btnXoa.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXoa.setForeground(new java.awt.Color(255, 255, 255));
        btnXoa.setText("Xóa");
        btnXoa.addActionListener(this::btnXoaActionPerformed);
        pnMain.add(btnXoa);
        btnXoa.setBounds(20, 395, 170, 40);

        btnHuy.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnHuy.setForeground(new java.awt.Color(235, 94, 141));
        btnHuy.setText("Làm mới");
        btnHuy.addActionListener(this::btnHuyActionPerformed);
        pnMain.add(btnHuy);
        btnHuy.setBounds(210, 395, 170, 40);

        lblTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTimKiem.setForeground(new java.awt.Color(35, 30, 48));
        lblTimKiem.setText("Tìm kiếm mã PGG:");
        pnMain.add(lblTimKiem);
        lblTimKiem.setBounds(420, 80, 130, 35);

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtTimKiem);
        txtTimKiem.setBounds(550, 80, 350, 35);

        btnTimKiem.setBackground(new java.awt.Color(235, 94, 141));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        btnTimKiem.setText("Tìm");
        btnTimKiem.addActionListener(this::btnTimKiemActionPerformed);
        pnMain.add(btnTimKiem);
        btnTimKiem.setBounds(920, 80, 100, 35);

        tblPhieuGiamGia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã PGG", "Mã Nhập", "Giá trị", "ĐK Áp dụng", "Từ ngày", "Đến ngày", "SL Đã dùng", "SL Tối đa"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPhieuGiamGia.setRowHeight(30);
        tblPhieuGiamGia.setSelectionBackground(new java.awt.Color(235, 94, 141));
        tblPhieuGiamGia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPhieuGiamGiaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPhieuGiamGia);

        pnMain.add(jScrollPane1);
        jScrollPane1.setBounds(420, 130, 600, 480);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnThemMoi;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXoa;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblGiaTriApDungToiThieu;
    private javax.swing.JLabel lblGiaTriGiamGia;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblMaChuSoPGG;
    private javax.swing.JLabel lblMaPGG;
    private javax.swing.JLabel lblNgayBatDauApDung;
    private javax.swing.JLabel lblNgayKetThucApDung;
    private javax.swing.JLabel lblSLToiDa;
    private javax.swing.JLabel lblTimKiem;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnMain;
    private javax.swing.JTable tblPhieuGiamGia;
    private javax.swing.JTextField txtGiaTriApDungToiThieu;
    private javax.swing.JTextField txtGiaTriGiamGia;
    private javax.swing.JTextField txtMaChuSoPGG;
    private javax.swing.JTextField txtMaPGG;
    private javax.swing.JTextField txtNgayBatDauApDung;
    private javax.swing.JTextField txtNgayKetThucApDung;
    private javax.swing.JTextField txtSLToiDa;
    private javax.swing.JTextField txtTimKiem;
    // End of variables declaration//GEN-END:variables
}
