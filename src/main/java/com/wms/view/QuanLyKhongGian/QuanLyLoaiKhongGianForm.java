/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.wms.view.QuanLyKhongGian;

/**
 *
 * @author Thinkapd T14s
 */
public class QuanLyLoaiKhongGianForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(QuanLyLoaiKhongGianForm.class.getName());

    /**
     * Creates new form QuanLyLoaiKhongGianForm
     */
    public QuanLyLoaiKhongGianForm() {
        initComponents();
    }

    private void btnThemMoiActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // TODO: Viết logic Thêm loại không gian (INSERT INTO LOAIKHONGGIAN)
    }                                          

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // TODO: Viết logic Cập nhật loại không gian (UPDATE LOAIKHONGGIAN)
    }                                          

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {                                       
        // TODO: Viết logic Xóa loại không gian (DELETE FROM LOAIKHONGGIAN)
    }                                      

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {                                       
        // TODO: Xóa trắng form nhập liệu
    }                                      

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // TODO: Viết logic Tìm kiếm
    }                                          

    private void tblLoaiKhongGianMouseClicked(java.awt.event.MouseEvent evt) {                                         
        // TODO: Lấy dữ liệu dòng được chọn và hiển thị ngược lên các TextBox
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        lblMaLoaiKG = new javax.swing.JLabel();
        txtMaLoaiKG = new javax.swing.JTextField();
        lblTenLoaiKG = new javax.swing.JLabel();
        txtTenLoaiKG = new javax.swing.JTextField();
        lblSucChua = new javax.swing.JLabel();
        txtSucChua = new javax.swing.JTextField();
        lblDonGia = new javax.swing.JLabel();
        txtDonGia = new javax.swing.JTextField();
        btnThemMoi = new javax.swing.JButton();
        btnCapNhat = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        lblTimKiem = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblLoaiKhongGian = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Quản lý Loại không gian - Không gian làm việc và học tập");
        setResizable(false);

        pnMain.setBackground(new java.awt.Color(255, 255, 255));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 550));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ LOẠI KHÔNG GIAN");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        lblMaLoaiKG.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaLoaiKG.setForeground(new java.awt.Color(35, 30, 48));
        lblMaLoaiKG.setText("Mã loại KG (*)");
        pnMain.add(lblMaLoaiKG);
        lblMaLoaiKG.setBounds(20, 80, 170, 18);

        txtMaLoaiKG.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtMaLoaiKG);
        txtMaLoaiKG.setBounds(20, 100, 170, 35);

        lblTenLoaiKG.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenLoaiKG.setForeground(new java.awt.Color(35, 30, 48));
        lblTenLoaiKG.setText("Tên loại KG (*)");
        pnMain.add(lblTenLoaiKG);
        lblTenLoaiKG.setBounds(210, 80, 170, 18);

        txtTenLoaiKG.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtTenLoaiKG);
        txtTenLoaiKG.setBounds(210, 100, 170, 35);

        lblSucChua.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSucChua.setForeground(new java.awt.Color(35, 30, 48));
        lblSucChua.setText("Sức chứa (Người)");
        pnMain.add(lblSucChua);
        lblSucChua.setBounds(20, 150, 170, 18);

        txtSucChua.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtSucChua);
        txtSucChua.setBounds(20, 170, 170, 35);

        lblDonGia.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDonGia.setForeground(new java.awt.Color(35, 30, 48));
        lblDonGia.setText("Đơn giá theo giờ (VNĐ)");
        pnMain.add(lblDonGia);
        lblDonGia.setBounds(210, 150, 170, 18);

        txtDonGia.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtDonGia);
        txtDonGia.setBounds(210, 170, 170, 35);

        btnThemMoi.setBackground(new java.awt.Color(235, 94, 141));
        btnThemMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThemMoi.setForeground(new java.awt.Color(255, 255, 255));
        btnThemMoi.setText("Thêm mới");
        btnThemMoi.addActionListener(this::btnThemMoiActionPerformed);
        pnMain.add(btnThemMoi);
        btnThemMoi.setBounds(20, 240, 170, 40);

        btnCapNhat.setBackground(new java.awt.Color(235, 94, 141));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật");
        btnCapNhat.addActionListener(this::btnCapNhatActionPerformed);
        pnMain.add(btnCapNhat);
        btnCapNhat.setBounds(210, 240, 170, 40);

        btnXoa.setBackground(new java.awt.Color(220, 53, 69));
        btnXoa.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXoa.setForeground(new java.awt.Color(255, 255, 255));
        btnXoa.setText("Xóa");
        btnXoa.addActionListener(this::btnXoaActionPerformed);
        pnMain.add(btnXoa);
        btnXoa.setBounds(20, 295, 170, 40);

        btnHuy.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnHuy.setForeground(new java.awt.Color(235, 94, 141));
        btnHuy.setText("Làm mới");
        btnHuy.addActionListener(this::btnHuyActionPerformed);
        pnMain.add(btnHuy);
        btnHuy.setBounds(210, 295, 170, 40);

        lblTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTimKiem.setForeground(new java.awt.Color(35, 30, 48));
        lblTimKiem.setText("Tìm kiếm (Tên loại):");
        pnMain.add(lblTimKiem);
        lblTimKiem.setBounds(420, 80, 140, 35);

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtTimKiem);
        txtTimKiem.setBounds(570, 80, 330, 35);

        btnTimKiem.setBackground(new java.awt.Color(235, 94, 141));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        btnTimKiem.setText("Tìm");
        btnTimKiem.addActionListener(this::btnTimKiemActionPerformed);
        pnMain.add(btnTimKiem);
        btnTimKiem.setBounds(910, 80, 100, 35);

        tblLoaiKhongGian.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã loại KG", "Tên loại KG", "Sức chứa", "Đơn giá (VNĐ/Giờ)"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblLoaiKhongGian.setRowHeight(30);
        tblLoaiKhongGian.setSelectionBackground(new java.awt.Color(235, 94, 141));
        tblLoaiKhongGian.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblLoaiKhongGianMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblLoaiKhongGian);

        pnMain.add(jScrollPane1);
        jScrollPane1.setBounds(420, 130, 590, 375);

        getContentPane().add(pnMain, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
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
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new QuanLyLoaiKhongGianForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnThemMoi;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXoa;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDonGia;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblMaLoaiKG;
    private javax.swing.JLabel lblSucChua;
    private javax.swing.JLabel lblTenLoaiKG;
    private javax.swing.JLabel lblTimKiem;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnMain;
    private javax.swing.JTable tblLoaiKhongGian;
    private javax.swing.JTextField txtDonGia;
    private javax.swing.JTextField txtMaLoaiKG;
    private javax.swing.JTextField txtSucChua;
    private javax.swing.JTextField txtTenLoaiKG;
    private javax.swing.JTextField txtTimKiem;
    // End of variables declaration//GEN-END:variables
}
