/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.wms.view.TrangChuQuanLy.QuanLyVaiTro;

import com.wms.controller.TrangChuQuanLy.QuanLyVaiTro.VaiTroController;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.ChucNangDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;

public class QuanLyVaiTroForm extends javax.swing.JPanel {
    private final VaiTroController controller = new VaiTroController();
    private String maVaiTroDangChon = null;
    private final List<VaiTroDTO> danhSachVaiTro = new ArrayList<>();
    private final List<ChucNangDTO> danhSachChucNang = new ArrayList<>();

    public QuanLyVaiTroForm() {
        initComponents();
        txtMaVT.setEditable(false);
        txtMaVT.setBackground(new java.awt.Color(240, 240, 240));
        txtMaVT.setText(controller.sinhMaVT());
        loadCbxChucNang();
        loadDanhSachVaiTro();
        initEvents();
        com.wms.util.TienIchFormQuanLy.apDung(this);
    }

    private void initEvents() {
        tblNhomQuyen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblNhomQuyenMouseClicked(evt);
            }
        });
        btnThemQuyen.addActionListener(this::btnThemQuyenActionPerformed);
        btnXoaQuyen.addActionListener(this::btnXoaQuyenActionPerformed);
        btnThemMoi.addActionListener(this::btnThemMoiActionPerformed);
        btnCapNhat.addActionListener(this::btnCapNhatActionPerformed);
        btnXoaNhom.addActionListener(this::btnXoaNhomActionPerformed);
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
    }

    private void loadDanhSachVaiTro() {
        DefaultTableModel model = (DefaultTableModel) tblNhomQuyen.getModel();
        model.setRowCount(0);
        danhSachVaiTro.clear();
        List<VaiTroDTO> list = controller.layTatCaVaiTro();
        danhSachVaiTro.addAll(list);
        for (VaiTroDTO vt : list) {
            model.addRow(new Object[] { vt.getMaVaiTro(), vt.getTenVaiTro(), vt.getMoTa() });
        }
    }

    private void loadCbxChucNang() {
        cbxChucNang.removeAllItems();
        danhSachChucNang.clear();
        List<ChucNangDTO> list = controller.layTatCaChucNang();
        danhSachChucNang.addAll(list);
        for (ChucNangDTO cn : list) {
            cbxChucNang.addItem(cn.getTenChucNang());
        }
    }

    private void loadChucNangCuaVaiTro(String maVaiTro) {
        DefaultTableModel model = (DefaultTableModel) tblChucNangDaChon.getModel();
        model.setRowCount(0);
        List<String[]> list = controller.layChucNangCuaVaiTro(maVaiTro);
        for (String[] cn : list) {
            model.addRow(new Object[] { cn[0], cn[1] });
        }
    }

    private void lamMoiPanel() {
        maVaiTroDangChon = null;
        txtMaVT.setText(controller.sinhMaVT());
        txtTenNhom.setText("");
        txtMoTa.setText("");
        ((DefaultTableModel) tblChucNangDaChon.getModel()).setRowCount(0);
        tblNhomQuyen.clearSelection();
    }

    private List<String> layDanhSachMaCNTuBang() {
        List<String> list = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) tblChucNangDaChon.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            list.add((String) model.getValueAt(i, 0));
        }
        return list;
    }

    private void tblNhomQuyenMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tblNhomQuyen.getSelectedRow();
        if (row < 0 || row >= danhSachVaiTro.size())
            return;
        VaiTroDTO vt = danhSachVaiTro.get(row);
        maVaiTroDangChon = vt.getMaVaiTro();
        txtMaVT.setText(maVaiTroDangChon);
        txtTenNhom.setText(vt.getTenVaiTro());
        txtMoTa.setText(vt.getMoTa() != null ? vt.getMoTa() : "");
        loadChucNangCuaVaiTro(maVaiTroDangChon);
    }

    private void btnThemQuyenActionPerformed(java.awt.event.ActionEvent evt) {
        int idx = cbxChucNang.getSelectedIndex();
        if (idx < 0 || idx >= danhSachChucNang.size())
            return;
        ChucNangDTO cn = danhSachChucNang.get(idx);
        DefaultTableModel model = (DefaultTableModel) tblChucNangDaChon.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (cn.getMaChucNang().equals(model.getValueAt(i, 0))) {
                JOptionPane.showMessageDialog(this, "Chức năng này đã có trong danh sách!");
                return;
            }
        }
        model.addRow(new Object[] { cn.getMaChucNang(), cn.getTenChucNang() });
    }

    private void btnXoaQuyenActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblChucNangDaChon.getSelectedRow();
        if (row >= 0) {
            ((DefaultTableModel) tblChucNangDaChon.getModel()).removeRow(row);
        }
    }

    private void btnThemMoiActionPerformed(java.awt.event.ActionEvent evt) {
        String ten = txtTenNhom.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên vai trò!");
            return;
        }
        VaiTroDTO vt = new VaiTroDTO();
        vt.setMaVaiTro(txtMaVT.getText().trim());
        vt.setTenVaiTro(ten);
        vt.setMoTa(txtMoTa.getText().trim());
        if (controller.themVaiTro(vt, layDanhSachMaCNTuBang())) {
            JOptionPane.showMessageDialog(this, "Thêm mới thành công!");
            lamMoiPanel();
            loadDanhSachVaiTro();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {
        if (maVaiTroDangChon == null) {
            JOptionPane.showMessageDialog(this, "Chọn vai trò cần cập nhật!");
            return;
        }
        String ten = txtTenNhom.getText().trim();
        if (ten.isEmpty()) return;
        VaiTroDTO vt = new VaiTroDTO();
        vt.setMaVaiTro(maVaiTroDangChon);
        vt.setTenVaiTro(ten);
        vt.setMoTa(txtMoTa.getText().trim());
        if (controller.capNhatVaiTro(vt, layDanhSachMaCNTuBang())) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDanhSachVaiTro();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnXoaNhomActionPerformed(java.awt.event.ActionEvent evt) {
        if (maVaiTroDangChon == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa vai trò này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.xoaVaiTro(maVaiTroDangChon)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                lamMoiPanel();
                loadDanhSachVaiTro();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại! (Có thể vai trò đang được gán cho nhân viên)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {
        lamMoiPanel();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        pnLeft = new javax.swing.JPanel();
        lblListTitle = new javax.swing.JLabel();
        scrollNhomQuyen = new javax.swing.JScrollPane();
        tblNhomQuyen = new javax.swing.JTable();
        pnRight = new javax.swing.JPanel();
        lblDetailTitle = new javax.swing.JLabel();
        lblTenNhom = new javax.swing.JLabel();
        txtTenNhom = new javax.swing.JTextField();
        lblMoTa = new javax.swing.JLabel();
        txtMoTa = new javax.swing.JTextField();
        pnLine = new javax.swing.JPanel();
        lblChonQuyen = new javax.swing.JLabel();
        cbxChucNang = new javax.swing.JComboBox<>();
        btnThemQuyen = new javax.swing.JButton();
        scrollChucNang = new javax.swing.JScrollPane();
        tblChucNangDaChon = new javax.swing.JTable();
        btnXoaQuyen = new javax.swing.JButton();
        btnThemMoi = new javax.swing.JButton();
        btnCapNhat = new javax.swing.JButton();
        btnXoaNhom = new javax.swing.JButton();
        btnLamMoi = new javax.swing.JButton();
        txtMaVT = new javax.swing.JTextField();
        lblTenNhom1 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(1050, 640));
        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(254, 248, 250));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ VAI TRÒ");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        pnLeft.setBackground(new java.awt.Color(255, 255, 255));
        pnLeft.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeft.setLayout(null);

        lblListTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblListTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblListTitle.setText("DANH SÁCH VAI TRÒ");
        pnLeft.add(lblListTitle);
        lblListTitle.setBounds(20, 15, 250, 30);

        tblNhomQuyen.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã Vai Trò", "Tên Vai Trò", "Mô Tả"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblNhomQuyen.setRowHeight(35);
        tblNhomQuyen.setSelectionBackground(new java.awt.Color(235, 94, 141));
        scrollNhomQuyen.setViewportView(tblNhomQuyen);

        pnLeft.add(scrollNhomQuyen);
        scrollNhomQuyen.setBounds(20, 60, 550, 460);

        pnMain.add(pnLeft);
        pnLeft.setBounds(440, 70, 590, 540);

        pnRight.setBackground(new java.awt.Color(255, 255, 255));
        pnRight.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRight.setLayout(null);

        lblDetailTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDetailTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblDetailTitle.setText("THÔNG TIN CHI TIẾT");
        pnRight.add(lblDetailTitle);
        lblDetailTitle.setBounds(20, 15, 200, 30);

        lblTenNhom.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenNhom.setForeground(new java.awt.Color(35, 30, 48));
        lblTenNhom.setText("Tên vai trò (*)");
        pnRight.add(lblTenNhom);
        lblTenNhom.setBounds(120, 60, 260, 20);

        txtTenNhom.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTenNhom.addActionListener(this::txtTenNhomActionPerformed);
        pnRight.add(txtTenNhom);
        txtTenNhom.setBounds(120, 80, 260, 35);

        lblMoTa.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMoTa.setForeground(new java.awt.Color(35, 30, 48));
        lblMoTa.setText("Mô tả");
        pnRight.add(lblMoTa);
        lblMoTa.setBounds(20, 115, 360, 20);

        txtMoTa.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(txtMoTa);
        txtMoTa.setBounds(20, 135, 360, 35);

        pnLine.setBackground(new java.awt.Color(224, 224, 224));
        pnLine.setLayout(null);
        pnRight.add(pnLine);
        pnLine.setBounds(20, 180, 360, 1);

        lblChonQuyen.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChonQuyen.setForeground(new java.awt.Color(35, 30, 48));
        lblChonQuyen.setText("Phân quyền chức năng cho vai trò");
        pnRight.add(lblChonQuyen);
        lblChonQuyen.setBounds(20, 190, 360, 20);

        cbxChucNang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(cbxChucNang);
        cbxChucNang.setBounds(20, 210, 250, 35);

        btnThemQuyen.setBackground(new java.awt.Color(235, 94, 141));
        btnThemQuyen.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnThemQuyen.setForeground(new java.awt.Color(255, 255, 255));
        btnThemQuyen.setText("+ Thêm vào");
        pnRight.add(btnThemQuyen);
        btnThemQuyen.setBounds(280, 210, 100, 35);

        tblChucNangDaChon.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã CN", "Tên chức năng"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblChucNangDaChon.setRowHeight(25);
        tblChucNangDaChon.setSelectionBackground(new java.awt.Color(235, 94, 141));
        scrollChucNang.setViewportView(tblChucNangDaChon);

        pnRight.add(scrollChucNang);
        scrollChucNang.setBounds(20, 255, 360, 130);

        btnXoaQuyen.setBackground(new java.awt.Color(220, 53, 69));
        btnXoaQuyen.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnXoaQuyen.setForeground(new java.awt.Color(255, 255, 255));
        btnXoaQuyen.setText("- Gỡ bỏ chức năng");
        pnRight.add(btnXoaQuyen);
        btnXoaQuyen.setBounds(20, 395, 360, 30);

        btnThemMoi.setBackground(new java.awt.Color(235, 94, 141));
        btnThemMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThemMoi.setForeground(new java.awt.Color(255, 255, 255));
        btnThemMoi.setText("Thêm mới");
        pnRight.add(btnThemMoi);
        btnThemMoi.setBounds(20, 440, 175, 35);

        btnCapNhat.setBackground(new java.awt.Color(235, 94, 141));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật");
        pnRight.add(btnCapNhat);
        btnCapNhat.setBounds(205, 440, 175, 35);

        btnXoaNhom.setBackground(new java.awt.Color(220, 53, 69));
        btnXoaNhom.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXoaNhom.setForeground(new java.awt.Color(255, 255, 255));
        btnXoaNhom.setText("Xóa Vai Trò");
        pnRight.add(btnXoaNhom);
        btnXoaNhom.setBounds(20, 485, 175, 35);

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(235, 94, 141));
        btnLamMoi.setText("Làm mới form");
        pnRight.add(btnLamMoi);
        btnLamMoi.setBounds(205, 485, 175, 35);

        txtMaVT.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRight.add(txtMaVT);
        txtMaVT.setBounds(20, 80, 90, 35);

        lblTenNhom1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenNhom1.setForeground(new java.awt.Color(35, 30, 48));
        lblTenNhom1.setText("Mã Vai trò");
        pnRight.add(lblTenNhom1);
        lblTenNhom1.setBounds(20, 60, 90, 20);

        pnMain.add(pnRight);
        pnRight.setBounds(10, 70, 400, 540);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtTenNhomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTenNhomActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTenNhomActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnThemMoi;
    private javax.swing.JButton btnThemQuyen;
    private javax.swing.JButton btnXoaNhom;
    private javax.swing.JButton btnXoaQuyen;
    private javax.swing.JComboBox<String> cbxChucNang;
    private javax.swing.JLabel lblChonQuyen;
    private javax.swing.JLabel lblDetailTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblListTitle;
    private javax.swing.JLabel lblMoTa;
    private javax.swing.JLabel lblTenNhom;
    private javax.swing.JLabel lblTenNhom1;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeft;
    private javax.swing.JPanel pnLine;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnRight;
    private javax.swing.JScrollPane scrollChucNang;
    private javax.swing.JScrollPane scrollNhomQuyen;
    private javax.swing.JTable tblChucNangDaChon;
    private javax.swing.JTable tblNhomQuyen;
    private javax.swing.JTextField txtMaVT;
    private javax.swing.JTextField txtMoTa;
    private javax.swing.JTextField txtTenNhom;
    // End of variables declaration//GEN-END:variables
}
