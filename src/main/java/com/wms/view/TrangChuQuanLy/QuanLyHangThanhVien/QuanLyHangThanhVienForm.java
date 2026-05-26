/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.wms.view.TrangChuQuanLy.QuanLyHangThanhVien;

import com.wms.controller.TrangChuQuanLy.QuanLyHangThanhVien.QuanLyHangTVController;
import com.wms.model.TrangChuQuanLy.QuanLyHangThanhVien.HangThanhVienDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 *
 * @author Thinkapd T14s
 */
public class QuanLyHangThanhVienForm extends javax.swing.JPanel {

    private final QuanLyHangTVController controller = new QuanLyHangTVController();
    private List<HangThanhVienDTO> hangList;
    private HangThanhVienDTO selectedHang;

    /**
     * Creates new form QuanLyHangThanhVienForm
     */
    public QuanLyHangThanhVienForm() {
        initComponents();
        setupListeners();
        loadDataToTable();
        txtTenHang.setEditable(false);
        txtMaHang.setEditable(false);
        com.wms.util.InputFormatUtil.attachThousandsFormatter(txtChiTieu);
        
        hienThiHangMacDinh();
        com.wms.util.TienIchFormQuanLy.apDung(this);
    }

    private void setupListeners() {
        btnCapNhat.addActionListener(this::btnCapNhatActionPerformed);
        btnTimKiem.addActionListener(this::btnTimKiemActionPerformed);
    }

    private void loadDataToTable() {
        hangList = controller.layDanhSachHang();
        fillTable(null);
    }

    private void fillTable(String filterQuery) {
        DefaultTableModel model = (DefaultTableModel) tblHangTV.getModel();
        model.setRowCount(0);
        if (hangList != null) {
            for (HangThanhVienDTO dto : hangList) {
                if (filterQuery == null || filterQuery.trim().isEmpty() ||
                    dto.getMaHangThanhVien().toLowerCase().contains(filterQuery.toLowerCase()) ||
                    dto.getTenHangThanhVien().toLowerCase().contains(filterQuery.toLowerCase())) {
                    
                    model.addRow(new Object[]{
                        dto.getMaHangThanhVien(), 
                        dto.getTenHangThanhVien(), 
                        dto.getPhanTramTienGiam() + "%", 
                        com.wms.util.InputFormatUtil.formatThousands(dto.getTongChiTieuToiThieu())
                    });
                }
            }
        }
        tblHangTV.clearSelection();
        tblHangTV.revalidate();
        tblHangTV.repaint();
    }

    private void refreshTableTheoDieuKienHienTai() {
        hangList = controller.layDanhSachHang();
        fillTable(txtTimKiem.getText().trim());
    }

    private void lamMoiForm() {
        hienThiHangMacDinh();
    }

    private HangThanhVienDTO getFormData() throws Exception {
        HangThanhVienDTO dto = (selectedHang != null) ? selectedHang : new HangThanhVienDTO();
        dto.setMaHangThanhVien(txtMaHang.getText().trim());
        dto.setTenHangThanhVien(txtTenHang.getText().trim());
        dto.setPhanTramTienGiam(Double.valueOf(spnPhanTram.getValue().toString()));
        
        if (txtChiTieu.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tổng chi tiêu tối thiểu.");
        }
        try {
            java.math.BigDecimal tongChiTieu = com.wms.util.InputFormatUtil.getBigDecimalValue(txtChiTieu);
            if (tongChiTieu == null) {
                throw new NumberFormatException();
            }
            if (tongChiTieu.signum() < 0) {
                throw new IllegalArgumentException("Tổng chi tiêu tối thiểu không được âm.");
            }
            dto.setTongChiTieuToiThieu(tongChiTieu.doubleValue());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tổng chi tiêu tối thiểu phải là số hợp lệ.");
        }
        return dto;
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedHang == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hạng thành viên từ danh sách!");
            return;
        }
        try {
            HangThanhVienDTO dto = getFormData();
            controller.capNhatHangThanhVien(dto);
            JOptionPane.showMessageDialog(this, "Cập nhật hạng thành viên thành công!");
            String currentMaHang = selectedHang.getMaHangThanhVien();
            refreshTableTheoDieuKienHienTai();
            selectTier(currentMaHang);
        } catch (Exception e) {
            com.wms.util.MessageUtil.showError(this, e.getMessage(), e);
        }
    }

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        String query = txtTimKiem.getText().trim();
        fillTable(query);
    }

    private void hienThiHangMacDinh() {
        selectTier("HTV00");
    }

    private void selectTier(String maHang) {
        if (hangList != null && !hangList.isEmpty()) {
            for (int i = 0; i < tblHangTV.getRowCount(); i++) {
                if (maHang.equals(tblHangTV.getValueAt(i, 0).toString())) {
                    tblHangTV.setRowSelectionInterval(i, i);
                    for (HangThanhVienDTO dto : hangList) {
                        if (dto.getMaHangThanhVien().equals(maHang)) {
                            selectedHang = dto;
                            break;
                        }
                    }
                    if (selectedHang != null) {
                        txtMaHang.setText(selectedHang.getMaHangThanhVien());
                        txtTenHang.setText(selectedHang.getTenHangThanhVien());
                        spnPhanTram.setValue(selectedHang.getPhanTramTienGiam() != null ? selectedHang.getPhanTramTienGiam() : 0.0);
                        txtChiTieu.setText(com.wms.util.InputFormatUtil.formatThousands(selectedHang.getTongChiTieuToiThieu()));
                    }
                    break;
                }
            }
        }
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
        pnLeft = new javax.swing.JPanel();
        lblListTitle = new javax.swing.JLabel();
        lblTimKiem = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHangTV = new javax.swing.JTable();
        btnLamMoi = new javax.swing.JButton();
        pnRight = new javax.swing.JPanel();
        lblDetailTitle = new javax.swing.JLabel();
        lblMaHang = new javax.swing.JLabel();
        txtTenHang = new javax.swing.JTextField();
        lblTenHang = new javax.swing.JLabel();
        lblPhanTram = new javax.swing.JLabel();
        spnPhanTram = new javax.swing.JSpinner();
        lblChiTieu = new javax.swing.JLabel();
        txtChiTieu = new javax.swing.JTextField();
        btnCapNhat = new javax.swing.JButton();
        txtMaHang = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(1050, 640));
        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(254, 248, 250));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ HẠNG THÀNH VIÊN");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        pnLeft.setBackground(new java.awt.Color(255, 255, 255));
        pnLeft.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeft.setLayout(null);

        lblListTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblListTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblListTitle.setText("DANH SÁCH HẠNG");
        pnLeft.add(lblListTitle);
        lblListTitle.setBounds(20, 15, 250, 30);

        lblTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTimKiem.setForeground(new java.awt.Color(35, 30, 48));
        lblTimKiem.setText("Tìm kiếm hạng:");
        pnLeft.add(lblTimKiem);
        lblTimKiem.setBounds(20, 60, 120, 35);

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnLeft.add(txtTimKiem);
        txtTimKiem.setBounds(140, 60, 230, 35);

        btnTimKiem.setBackground(new java.awt.Color(235, 94, 141));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        btnTimKiem.setText("Tìm");
        pnLeft.add(btnTimKiem);
        btnTimKiem.setBounds(380, 60, 80, 35);

        tblHangTV.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã Hạng", "Tên Hạng", "% Giảm giá", "Chi tiêu tối thiểu"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblHangTV.setRowHeight(35);
        tblHangTV.setSelectionBackground(new java.awt.Color(235, 94, 141));
        tblHangTV.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHangTVMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblHangTV);

        pnLeft.add(jScrollPane1);
        jScrollPane1.setBounds(20, 110, 550, 410);

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(235, 94, 141));
        btnLamMoi.setText("Làm mới");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
        pnLeft.add(btnLamMoi);
        btnLamMoi.setBounds(470, 60, 100, 35);

        pnMain.add(pnLeft);
        pnLeft.setBounds(420, 70, 590, 540);

        pnRight.setBackground(new java.awt.Color(255, 255, 255));
        pnRight.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRight.setLayout(null);

        lblDetailTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDetailTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblDetailTitle.setText("THÔNG TIN CHI TIẾT");
        pnRight.add(lblDetailTitle);
        lblDetailTitle.setBounds(20, 15, 200, 30);

        lblMaHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaHang.setForeground(new java.awt.Color(35, 30, 48));
        lblMaHang.setText("Mã hạng (*)");
        pnRight.add(lblMaHang);
        lblMaHang.setBounds(20, 60, 360, 20);

        txtTenHang.setBackground(new java.awt.Color(240, 240, 240));
        txtTenHang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(txtTenHang);
        txtTenHang.setBounds(20, 150, 360, 35);

        lblTenHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenHang.setForeground(new java.awt.Color(35, 30, 48));
        lblTenHang.setText("Tên hạng (*)");
        pnRight.add(lblTenHang);
        lblTenHang.setBounds(20, 130, 360, 20);

        lblPhanTram.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPhanTram.setForeground(new java.awt.Color(35, 30, 48));
        lblPhanTram.setText("% Giảm giá (0 - 100)");
        pnRight.add(lblPhanTram);
        lblPhanTram.setBounds(20, 200, 360, 20);

        spnPhanTram.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        spnPhanTram.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 100.0d, 0.5d));
        pnRight.add(spnPhanTram);
        spnPhanTram.setBounds(20, 220, 360, 35);

        lblChiTieu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChiTieu.setForeground(new java.awt.Color(35, 30, 48));
        lblChiTieu.setText("Chi tiêu tối thiểu (VNĐ)");
        pnRight.add(lblChiTieu);
        lblChiTieu.setBounds(20, 270, 360, 20);

        txtChiTieu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(txtChiTieu);
        txtChiTieu.setBounds(20, 290, 360, 35);

        btnCapNhat.setBackground(new java.awt.Color(235, 94, 141));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật");
        pnRight.add(btnCapNhat);
        btnCapNhat.setBounds(20, 350, 360, 35);

        txtMaHang.setBackground(new java.awt.Color(240, 240, 240));
        txtMaHang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(txtMaHang);
        txtMaHang.setBounds(20, 80, 360, 35);

        pnMain.add(pnRight);
        pnRight.setBounds(10, 70, 400, 540);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {
        lamMoiForm();
        refreshTableTheoDieuKienHienTai();
    }

    private void tblHangTVMouseClicked(java.awt.event.MouseEvent evt) {
        int idx = tblHangTV.getSelectedRow();
        if (idx >= 0) {
            String maHang = tblHangTV.getValueAt(idx, 0).toString();
            for (HangThanhVienDTO dto : hangList) {
                if (dto.getMaHangThanhVien().equals(maHang)) {
                    selectedHang = dto;
                    break;
                }
            }
            if (selectedHang != null) {
                txtMaHang.setText(selectedHang.getMaHangThanhVien());
                txtTenHang.setText(selectedHang.getTenHangThanhVien());
                spnPhanTram.setValue(selectedHang.getPhanTramTienGiam() != null ? selectedHang.getPhanTramTienGiam() : 0.0);
                txtChiTieu.setText(com.wms.util.InputFormatUtil.formatThousands(selectedHang.getTongChiTieuToiThieu()));
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblChiTieu;
    private javax.swing.JLabel lblDetailTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblListTitle;
    private javax.swing.JLabel lblMaHang;
    private javax.swing.JLabel lblPhanTram;
    private javax.swing.JLabel lblTenHang;
    private javax.swing.JLabel lblTimKiem;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeft;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnRight;
    private javax.swing.JSpinner spnPhanTram;
    private javax.swing.JTable tblHangTV;
    private javax.swing.JTextField txtChiTieu;
    private javax.swing.JTextField txtMaHang;
    private javax.swing.JTextField txtTenHang;
    private javax.swing.JTextField txtTimKiem;
    // End of variables declaration//GEN-END:variables
}
