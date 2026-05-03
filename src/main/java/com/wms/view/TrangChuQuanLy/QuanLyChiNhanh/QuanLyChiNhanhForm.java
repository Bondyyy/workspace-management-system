package com.wms.view.TrangChuQuanLy.QuanLyChiNhanh;

import com.wms.controller.ChiNhanhController;

public class QuanLyChiNhanhForm extends javax.swing.JPanel {

    private final com.wms.controller.ChiNhanhController controller;
    private javax.swing.table.DefaultTableModel tableModel;
    private String selectedMaCN = null;

    /**
     * Creates new form QuanLyChiNhanhForm
     */
    public QuanLyChiNhanhForm() {
        initComponents();
        controller = new com.wms.controller.ChiNhanhController();
        initTable();
        loadQuanLyData();
        loadTableData();
    }

    private void loadQuanLyData() {
        cbxQuanLy.removeAllItems();
        cbxQuanLy.addItem("Không có quản lý");
        try {
            com.wms.dao.NhanVienDAO nvDao = new com.wms.dao.NhanVienDAO();
            java.util.List<String[]> managers = nvDao.layDanhSachQuanLy();
            if (managers != null) {
                for (String[] m : managers) {
                    cbxQuanLy.addItem(m[0] + " - " + m[1]);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi load danh sách quản lý: " + e.getMessage());
        }
    }

    private void initTable() {
        tableModel = (javax.swing.table.DefaultTableModel) tblChiNhanh.getModel();
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        java.util.List<com.wms.model.ChiNhanhDTO> list = controller.layDanhSach();
        for (com.wms.model.ChiNhanhDTO cn : list) {
            tableModel.addRow(new Object[]{
                cn.getMaCN(),
                cn.getTenCN(),
                cn.getDiaChi(),
                cn.getThoiGianMoCua(),
                cn.getThoiGianDongCua(),
                cn.getDuongDayNong(),
                cn.getTrangThai(),
                cn.getMaNV_QuanLy()
            });
        }
    }

    private void fillForm(int row) {
        selectedMaCN = (String) tblChiNhanh.getValueAt(row, 0);
        txtTenChiNhanh.setText((String) tblChiNhanh.getValueAt(row, 1));
        txtDiaChi.setText((String) tblChiNhanh.getValueAt(row, 2));
        txtGioMoCua.setText((String) tblChiNhanh.getValueAt(row, 3));
        txtGioDongCua.setText((String) tblChiNhanh.getValueAt(row, 4));
        txtHotline1.setText((String) tblChiNhanh.getValueAt(row, 5));
        cbxTrangThai.setSelectedItem(tblChiNhanh.getValueAt(row, 6));
        
        String maNV = null;
        if (tableModel.getColumnCount() > 7 || ((java.util.Vector)((javax.swing.table.DefaultTableModel)tableModel).getDataVector().elementAt(row)).size() > 7) {
            maNV = (String) ((java.util.Vector)((javax.swing.table.DefaultTableModel)tableModel).getDataVector().elementAt(row)).elementAt(7);
        }
        boolean found = false;
        if (maNV != null && !maNV.trim().isEmpty()) {
            for (int i = 1; i < cbxQuanLy.getItemCount(); i++) {
                String item = cbxQuanLy.getItemAt(i);
                if (item.startsWith(maNV + " -")) {
                    cbxQuanLy.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
        }
        if (!found) cbxQuanLy.setSelectedIndex(0);
    }

    private void clearForm() {
        selectedMaCN = null;
        txtTenChiNhanh.setText("");
        txtDiaChi.setText("");
        txtGioMoCua.setText("");
        txtGioDongCua.setText("");
        txtHotline1.setText("");
        cbxTrangThai.setSelectedIndex(0);
        cbxQuanLy.setSelectedIndex(0);
        tblChiNhanh.clearSelection();
    }
    private void btnThemMoiActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            com.wms.model.ChiNhanhDTO cn = new com.wms.model.ChiNhanhDTO();
            cn.setMaCN("CN" + (System.currentTimeMillis() % 1000000)); 
            cn.setTenCN(txtTenChiNhanh.getText());
            cn.setDiaChi(txtDiaChi.getText());
            cn.setThoiGianMoCua(txtGioMoCua.getText());
            cn.setThoiGianDongCua(txtGioDongCua.getText());
            cn.setDuongDayNong(txtHotline1.getText());
            cn.setTrangThai(cbxTrangThai.getSelectedItem() != null ? cbxTrangThai.getSelectedItem().toString() : "Đang hoạt động");
            
            String sel = cbxQuanLy.getSelectedItem().toString();
            if (sel.equals("Không có quản lý")) {
                cn.setMaNV_QuanLy(null);
            } else {
                cn.setMaNV_QuanLy(sel.split(" - ")[0]);
            }

            if (controller.themMoi(cn)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Thêm mới chi nhánh thành công!");
                loadTableData();
                clearForm();
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {                                           
        if (selectedMaCN == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn chi nhánh cần cập nhật từ bảng!");
            return;
        }
        try {
            com.wms.model.ChiNhanhDTO cn = new com.wms.model.ChiNhanhDTO();
            cn.setMaCN(selectedMaCN);
            cn.setTenCN(txtTenChiNhanh.getText());
            cn.setDiaChi(txtDiaChi.getText());
            cn.setThoiGianMoCua(txtGioMoCua.getText());
            cn.setThoiGianDongCua(txtGioDongCua.getText());
            cn.setDuongDayNong(txtHotline1.getText());
            cn.setTrangThai(cbxTrangThai.getSelectedItem() != null ? cbxTrangThai.getSelectedItem().toString() : "Đang hoạt động");
            
            String sel = cbxQuanLy.getSelectedItem().toString();
            if (sel.equals("Không có quản lý")) {
                cn.setMaNV_QuanLy(null);
            } else {
                cn.setMaNV_QuanLy(sel.split(" - ")[0]);
            }

            if (controller.capNhat(cn)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadTableData();
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }                                          

    private void btnVoHieuHoaActionPerformed(java.awt.event.ActionEvent evt) {                                         
        if (selectedMaCN == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn chi nhánh cần vô hiệu hóa!");
            return;
        }
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn vô hiệu hóa chi nhánh này?", "Xác nhận", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                if (controller.voHieuHoa(selectedMaCN)) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Đã vô hiệu hóa chi nhánh!");
                    loadTableData();
                    clearForm();
                }
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
            }
        }
    }                                        

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {                                       
        clearForm();
    }                                      

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {                                           
        String keyword = txtTimKiem.getText();
        tableModel.setRowCount(0);
        java.util.List<com.wms.model.ChiNhanhDTO> list = controller.timKiem(keyword);
        for (com.wms.model.ChiNhanhDTO cn : list) {
            tableModel.addRow(new Object[]{
                cn.getMaCN(),
                cn.getTenCN(),
                cn.getDiaChi(),
                cn.getThoiGianMoCua(),
                cn.getThoiGianDongCua(),
                cn.getDuongDayNong(),
                cn.getTrangThai(),
                cn.getMaNV_QuanLy()
            });
        }
    }                                          
    
    private void btnSoDoActionPerformed(java.awt.event.ActionEvent evt) {                                        
        if (selectedMaCN == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn chi nhánh cần xem sơ đồ!");
            return;
        }
        String tenCN = txtTenChiNhanh.getText();
        java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
        java.awt.Frame parentFrame = null;
        if (parentWindow instanceof java.awt.Frame) {
            parentFrame = (java.awt.Frame) parentWindow;
        }
        
        QuanLySoDoKhongGianForm dialog = new QuanLySoDoKhongGianForm(parentFrame, true, selectedMaCN, tenCN);
        dialog.setVisible(true);
    }                                       

    private void tblChiNhanhMouseClicked(java.awt.event.MouseEvent evt) {                                         
        int row = tblChiNhanh.getSelectedRow();
        if (row >= 0) {
            fillForm(row);
        }
    }
    private void cbxQuanLyActionPerformed(java.awt.event.ActionEvent evt) {
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
        lblTenChiNhanh = new javax.swing.JLabel();
        txtTenChiNhanh = new javax.swing.JTextField();
        lblDiaChi = new javax.swing.JLabel();
        txtDiaChi = new javax.swing.JTextField();
        lblGioMoCua = new javax.swing.JLabel();
        txtGioMoCua = new javax.swing.JTextField();
        lblGioDongCua = new javax.swing.JLabel();
        txtGioDongCua = new javax.swing.JTextField();
        lblHotline = new javax.swing.JLabel();
        lblTrangThai = new javax.swing.JLabel();
        cbxQuanLy = new javax.swing.JComboBox<>();
        btnThemMoi = new javax.swing.JButton();
        btnCapNhat = new javax.swing.JButton();
        btnSoDo = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        lblTimKiem = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblChiNhanh = new javax.swing.JTable();
        txtHotline1 = new javax.swing.JTextField();
        lblGioMoCua1 = new javax.swing.JLabel();
        cbxTrangThai = new javax.swing.JComboBox<>();

        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(255, 255, 255));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 550));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ CHI NHÁNH");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        lblTenChiNhanh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenChiNhanh.setForeground(new java.awt.Color(35, 30, 48));
        lblTenChiNhanh.setText("Tên chi nhánh (*)");
        pnMain.add(lblTenChiNhanh);
        lblTenChiNhanh.setBounds(20, 70, 360, 18);

        txtTenChiNhanh.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtTenChiNhanh);
        txtTenChiNhanh.setBounds(20, 90, 360, 35);

        lblDiaChi.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDiaChi.setForeground(new java.awt.Color(35, 30, 48));
        lblDiaChi.setText("Địa chỉ (*)");
        pnMain.add(lblDiaChi);
        lblDiaChi.setBounds(20, 135, 360, 18);

        txtDiaChi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtDiaChi);
        txtDiaChi.setBounds(20, 155, 360, 35);

        lblGioMoCua.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGioMoCua.setForeground(new java.awt.Color(35, 30, 48));
        lblGioMoCua.setText("Tên Quản lý chi nhánh");
        pnMain.add(lblGioMoCua);
        lblGioMoCua.setBounds(20, 330, 170, 18);

        txtGioMoCua.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtGioMoCua);
        txtGioMoCua.setBounds(20, 220, 170, 35);

        lblGioDongCua.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGioDongCua.setForeground(new java.awt.Color(35, 30, 48));
        lblGioDongCua.setText("Giờ đóng cửa (VD: 22:00)");
        pnMain.add(lblGioDongCua);
        lblGioDongCua.setBounds(210, 200, 170, 18);

        txtGioDongCua.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtGioDongCua);
        txtGioDongCua.setBounds(210, 220, 170, 35);

        lblHotline.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblHotline.setForeground(new java.awt.Color(35, 30, 48));
        lblHotline.setText("Hotline (*)");
        pnMain.add(lblHotline);
        lblHotline.setBounds(20, 265, 170, 18);

        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTrangThai.setForeground(new java.awt.Color(35, 30, 48));
        lblTrangThai.setText("Trạng thái (*)");
        pnMain.add(lblTrangThai);
        lblTrangThai.setBounds(210, 265, 170, 18);

        cbxQuanLy.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxQuanLy.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Đang hoạt động", "Ngừng hoạt động" }));
        cbxQuanLy.addActionListener(this::cbxQuanLyActionPerformed);
        pnMain.add(cbxQuanLy);
        cbxQuanLy.setBounds(20, 350, 360, 40);

        btnThemMoi.setBackground(new java.awt.Color(235, 94, 141));
        btnThemMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThemMoi.setForeground(new java.awt.Color(255, 255, 255));
        btnThemMoi.setText("Thêm mới");
        btnThemMoi.addActionListener(this::btnThemMoiActionPerformed);
        pnMain.add(btnThemMoi);
        btnThemMoi.setBounds(20, 410, 170, 40);

        btnCapNhat.setBackground(new java.awt.Color(235, 94, 141));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật");
        btnCapNhat.addActionListener(this::btnCapNhatActionPerformed);
        pnMain.add(btnCapNhat);
        btnCapNhat.setBounds(210, 410, 170, 40);

        btnSoDo.setBackground(new java.awt.Color(220, 53, 69));
        btnSoDo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSoDo.setForeground(new java.awt.Color(255, 255, 255));
        btnSoDo.setText("Q.Lý Sơ đồ");
        btnSoDo.addActionListener(this::btnSoDoActionPerformed);
        pnMain.add(btnSoDo);
        btnSoDo.setBounds(20, 465, 170, 40);

        btnHuy.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnHuy.setForeground(new java.awt.Color(235, 94, 141));
        btnHuy.setText("Làm mới");
        btnHuy.addActionListener(this::btnHuyActionPerformed);
        pnMain.add(btnHuy);
        btnHuy.setBounds(210, 465, 170, 40);

        lblTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTimKiem.setForeground(new java.awt.Color(35, 30, 48));
        lblTimKiem.setText("Tìm kiếm (Tên/Địa chỉ):");
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

        tblChiNhanh.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã CN", "Tên chi nhánh", "Địa chỉ", "Giờ mở", "Giờ đóng", "Hotline", "Trạng thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblChiNhanh.setRowHeight(30);
        tblChiNhanh.setSelectionBackground(new java.awt.Color(235, 94, 141));
        tblChiNhanh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblChiNhanhMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblChiNhanh);

        pnMain.add(jScrollPane1);
        jScrollPane1.setBounds(420, 130, 600, 375);

        txtHotline1.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtHotline1);
        txtHotline1.setBounds(20, 285, 170, 35);

        lblGioMoCua1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGioMoCua1.setForeground(new java.awt.Color(35, 30, 48));
        lblGioMoCua1.setText("Giờ mở cửa (VD: 07:00)");
        pnMain.add(lblGioMoCua1);
        lblGioMoCua1.setBounds(20, 200, 170, 18);

        cbxTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Đang hoạt động", "Ngừng hoạt động" }));
        cbxTrangThai.addActionListener(this::cbxTrangThaiActionPerformed);
        pnMain.add(cbxTrangThai);
        cbxTrangThai.setBounds(210, 285, 170, 35);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cbxTrangThaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTrangThaiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxTrangThaiActionPerformed

  


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnSoDo;
    private javax.swing.JButton btnThemMoi;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JComboBox<String> cbxQuanLy;
    private javax.swing.JComboBox<String> cbxTrangThai;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDiaChi;
    private javax.swing.JLabel lblGioDongCua;
    private javax.swing.JLabel lblGioMoCua;
    private javax.swing.JLabel lblGioMoCua1;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblHotline;
    private javax.swing.JLabel lblTenChiNhanh;
    private javax.swing.JLabel lblTimKiem;
    private javax.swing.JLabel lblTrangThai;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnMain;
    private javax.swing.JTable tblChiNhanh;
    private javax.swing.JTextField txtDiaChi;
    private javax.swing.JTextField txtGioDongCua;
    private javax.swing.JTextField txtGioMoCua;
    private javax.swing.JTextField txtHotline1;
    private javax.swing.JTextField txtTenChiNhanh;
    private javax.swing.JTextField txtTimKiem;
    // End of variables declaration//GEN-END:variables
}


