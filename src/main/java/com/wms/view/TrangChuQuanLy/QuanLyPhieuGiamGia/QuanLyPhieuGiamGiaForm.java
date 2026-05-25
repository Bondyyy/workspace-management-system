package com.wms.view.TrangChuQuanLy.QuanLyPhieuGiamGia;

import com.wms.controller.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaController;
import com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.List;

public class QuanLyPhieuGiamGiaForm extends javax.swing.JPanel {

    private final PhieuGiamGiaController controller = new PhieuGiamGiaController();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public QuanLyPhieuGiamGiaForm() {
        initComponents();
        setupTable();
        txtMaPGG.setText("");
        sdf.setLenient(false);
        com.wms.util.InputFormatUtil.attachThousandsFormatter(txtGiaTriGiamGia);
        com.wms.util.InputFormatUtil.attachThousandsFormatter(txtGiaTriApDungToiThieu);
        com.wms.util.InputFormatUtil.attachThousandsFormatter(txtSLToiDa);
        
        cbxTrangThai.setEnabled(false);
        loadDataToTable();
        com.wms.util.TienIchFormQuanLy.apDung(this);
    }

    private void setupTable() {
        // Căn lề cho các cột số và ngày tháng
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
        
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        
        tblPhieuGiamGia.getColumnModel().getColumn(2).setCellRenderer(rightRenderer); // Giá trị
        tblPhieuGiamGia.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // ĐK Áp dụng
        tblPhieuGiamGia.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Từ ngày
        tblPhieuGiamGia.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Đến ngày
        tblPhieuGiamGia.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // SL Đã dùng
        tblPhieuGiamGia.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // SL Tối đa
        tblPhieuGiamGia.getColumnModel().getColumn(8).setCellRenderer(centerRenderer); // Trạng thái
    }

    private void loadDataToTable() {
        DefaultTableModel model = (DefaultTableModel) tblPhieuGiamGia.getModel();
        model.setRowCount(0);
        List<PhieuGiamGiaDTO> list = controller.layDanhSach();
        for (PhieuGiamGiaDTO dto : list) {
            model.addRow(new Object[]{
                dto.getMaPGG(),
                dto.getMaChuSoPGG(),
                com.wms.util.InputFormatUtil.formatThousands(dto.getGiaTriGiamGia()),
                com.wms.util.InputFormatUtil.formatThousands(dto.getGiaTriApDungToiThieu()),
                sdf.format(dto.getNgayBatDauApDung()),
                sdf.format(dto.getNgayKetThucApDung()),
                dto.getSlDaDung(),
                dto.getSlToiDa(),
                dto.getTrangThai() != null ? dto.getTrangThai() : "Đang có hiệu lực"
            });
        }
    }

    private void laMoiForm() {
        txtMaPGG.setText("");
        txtMaChuSoPGG.setText("");
        txtGiaTriGiamGia.setText("");
        txtGiaTriApDungToiThieu.setText("");
        txtSLToiDa.setText("");
        txtNgayBatDauApDung.setText("");
        txtNgayKetThucApDung.setText("");
        cbxTrangThai.setSelectedIndex(0);
        tblPhieuGiamGia.clearSelection();
    }

    private PhieuGiamGiaDTO getFormData() throws Exception {
        PhieuGiamGiaDTO dto = new PhieuGiamGiaDTO();
        dto.setMaPGG(txtMaPGG.getText().trim());
        String maChuSo = txtMaChuSoPGG.getText().trim();
        if (maChuSo.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã số phiếu giảm giá.");
        }
        dto.setMaChuSoPGG(maChuSo);

        java.math.BigDecimal giaTri = parseMoneyRequired(txtGiaTriGiamGia, "giá trị giảm giá", "Giá trị giảm giá");
        if (giaTri.signum() <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải lớn hơn 0.");
        }
        java.math.BigDecimal apDungToiThieu = parseMoneyRequired(txtGiaTriApDungToiThieu,
                "giá trị áp dụng tối thiểu", "Giá trị áp dụng tối thiểu");
        if (apDungToiThieu.signum() < 0) {
            throw new IllegalArgumentException("Giá trị áp dụng tối thiểu không được âm.");
        }

        if (txtSLToiDa.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số lượng.");
        }
        Long slToiDa;
        try {
            slToiDa = com.wms.util.InputFormatUtil.getNumberValue(txtSLToiDa);
            if (slToiDa == null || slToiDa > Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên hợp lệ.");
        }
        if (slToiDa <= 0) {
            throw new IllegalArgumentException("Số lượng phát hành phải lớn hơn 0.");
        }

        if (txtNgayBatDauApDung.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập ngày bắt đầu áp dụng.");
        }
        if (txtNgayKetThucApDung.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập ngày kết thúc áp dụng.");
        }

        try {
            dto.setNgayBatDauApDung(sdf.parse(txtNgayBatDauApDung.getText().trim()));
            dto.setNgayKetThucApDung(sdf.parse(txtNgayKetThucApDung.getText().trim()));
        } catch (java.text.ParseException ex) {
            throw new IllegalArgumentException("Ngày áp dụng phải đúng định dạng dd/MM/yyyy.");
        }
        if (dto.getNgayKetThucApDung().before(dto.getNgayBatDauApDung())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }

        dto.setGiaTriGiamGia(giaTri.doubleValue());
        dto.setGiaTriApDungToiThieu(apDungToiThieu.doubleValue());
        dto.setSlToiDa(slToiDa.intValue());
        
        NguoiDungDTO user = com.wms.controller.TrangChuGioiThieu.DangNhapController.getCurrentUser();
        dto.setMaNV(user != null ? user.getMaNV() : "NV_ADMIN"); 
        dto.setTrangThai(cbxTrangThai.getSelectedItem() != null ? cbxTrangThai.getSelectedItem().toString() : "Đang có hiệu lực");
        return dto;
    }

    private java.math.BigDecimal parseMoneyRequired(javax.swing.JTextField field, String fieldNameLower, String fieldNameTitle) {
        if (field.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập " + fieldNameLower + ".");
        }
        try {
            java.math.BigDecimal value = com.wms.util.InputFormatUtil.getBigDecimalValue(field);
            if (value == null) {
                throw new NumberFormatException();
            }
            if (value.signum() < 0) {
                throw new IllegalArgumentException(fieldNameTitle + " không được âm.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldNameTitle + " phải là số hợp lệ.");
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
        btnNgung = new javax.swing.JButton();
        btnLamMoi = new javax.swing.JButton();
        lblTimKiem = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPhieuGiamGia = new javax.swing.JTable();
        cbxTrangThai = new javax.swing.JComboBox<>();
        lblTrangThai = new javax.swing.JLabel();

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
        lblMaPGG.setText("Mã Phiếu giảm giá");
        pnMain.add(lblMaPGG);
        lblMaPGG.setBounds(20, 70, 170, 18);

        txtMaPGG.setEditable(false);
        txtMaPGG.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtMaPGG.setBackground(new java.awt.Color(240, 240, 240));
        pnMain.add(txtMaPGG);
        txtMaPGG.setBounds(20, 90, 170, 35);

        lblMaChuSoPGG.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaChuSoPGG.setForeground(new java.awt.Color(35, 30, 48));
        lblMaChuSoPGG.setText("Mã số nhập khuyến mãi (*)");
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
        lblNgayBatDauApDung.setText("Ngày Bắt đầu (dd/MM/yyyy) (*)");
        pnMain.add(lblNgayBatDauApDung);
        lblNgayBatDauApDung.setBounds(20, 200, 360, 18);

        txtNgayBatDauApDung.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtNgayBatDauApDung);
        txtNgayBatDauApDung.setBounds(20, 220, 360, 35);

        lblNgayKetThucApDung.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayKetThucApDung.setForeground(new java.awt.Color(35, 30, 48));
        lblNgayKetThucApDung.setText("Ngày Kết thúc (dd/MM/yyyy) (*)");
        pnMain.add(lblNgayKetThucApDung);
        lblNgayKetThucApDung.setBounds(20, 270, 360, 18);

        txtNgayKetThucApDung.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtNgayKetThucApDung);
        txtNgayKetThucApDung.setBounds(20, 290, 360, 35);

        lblSLToiDa.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSLToiDa.setForeground(new java.awt.Color(35, 30, 48));
        lblSLToiDa.setText("Số lượng phát hành (*)");
        pnMain.add(lblSLToiDa);
        lblSLToiDa.setBounds(20, 330, 360, 18);

        txtSLToiDa.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtSLToiDa);
        txtSLToiDa.setBounds(20, 350, 360, 35);

        btnThemMoi.setBackground(new java.awt.Color(235, 94, 141));
        btnThemMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThemMoi.setForeground(new java.awt.Color(255, 255, 255));
        btnThemMoi.setText("Thêm mới");
        btnThemMoi.addActionListener(this::btnThemMoiActionPerformed);
        pnMain.add(btnThemMoi);
        btnThemMoi.setBounds(20, 460, 170, 40);

        btnCapNhat.setBackground(new java.awt.Color(235, 94, 141));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật");
        btnCapNhat.addActionListener(this::btnCapNhatActionPerformed);
        pnMain.add(btnCapNhat);
        btnCapNhat.setBounds(210, 460, 170, 40);

        btnNgung.setBackground(new java.awt.Color(220, 53, 69));
        btnNgung.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnNgung.setForeground(new java.awt.Color(255, 255, 255));
        btnNgung.setText("Ngừng chương trình");
        btnNgung.addActionListener(this::btnNgungActionPerformed);
        pnMain.add(btnNgung);
        btnNgung.setBounds(20, 510, 360, 40);

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(235, 94, 141));
        btnLamMoi.setText("Làm mới");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
        pnMain.add(btnLamMoi);
        btnLamMoi.setBounds(930, 80, 100, 30);

        lblTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTimKiem.setForeground(new java.awt.Color(35, 30, 48));
        lblTimKiem.setText("Tìm kiếm mã PGG:");
        pnMain.add(lblTimKiem);
        lblTimKiem.setBounds(420, 80, 130, 30);

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnMain.add(txtTimKiem);
        txtTimKiem.setBounds(550, 80, 280, 30);

        btnTimKiem.setBackground(new java.awt.Color(235, 94, 141));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        btnTimKiem.setText("Tìm");
        btnTimKiem.addActionListener(this::btnTimKiemActionPerformed);
        pnMain.add(btnTimKiem);
        btnTimKiem.setBounds(840, 80, 80, 30);

        tblPhieuGiamGia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã PGG", "Mã Nhập", "Giá trị", "ĐK Áp dụng", "Từ ngày", "Đến ngày", "SL Đã dùng", "SL Tối đa", "Trạng thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
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

        cbxTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Đang có hiệu lực", "Chưa đến hạn bắt đầu", "Hết hiệu lực", "Đã vô hiệu hoá" }));
        cbxTrangThai.setToolTipText("");
        cbxTrangThai.addActionListener(this::cbxTrangThaiActionPerformed);
        pnMain.add(cbxTrangThai);
        cbxTrangThai.setBounds(20, 410, 360, 40);

        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTrangThai.setForeground(new java.awt.Color(35, 30, 48));
        lblTrangThai.setText("Trạng thái (*)");
        pnMain.add(lblTrangThai);
        lblTrangThai.setBounds(20, 390, 360, 18);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cbxTrangThaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTrangThaiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxTrangThaiActionPerformed

    private void btnThemMoiActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            PhieuGiamGiaDTO dto = getFormData();
            if (controller.themMoi(dto)) {
                JOptionPane.showMessageDialog(this, "Thêm phiếu giảm giá thành công!");
                loadDataToTable();
                laMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại! Vui lòng kiểm tra lại thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            com.wms.util.MessageUtil.showError(this, e.getMessage(), e);
        }
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            PhieuGiamGiaDTO dto = getFormData();
            if (controller.capNhat(dto)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            com.wms.util.MessageUtil.showError(this, e.getMessage(), e);
        }
    }

    private void btnNgungActionPerformed(java.awt.event.ActionEvent evt) {
        String ma = txtMaPGG.getText();
        if (ma.isEmpty()) return;
        
        int xacNhan = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn ngừng chương trình giảm giá này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (xacNhan == JOptionPane.YES_OPTION) {
            if (controller.xoa(ma)) {
                JOptionPane.showMessageDialog(this, "Đã ngưng chương trình thành công!");
                loadDataToTable();
                laMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Ngừng chương trình thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {
        laMoiForm();
    }

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        String keyword = txtTimKiem.getText().trim();
        DefaultTableModel model = (DefaultTableModel) tblPhieuGiamGia.getModel();
        model.setRowCount(0);
        List<PhieuGiamGiaDTO> list = controller.timKiem(keyword);
        for (PhieuGiamGiaDTO dto : list) {
            model.addRow(new Object[]{
                dto.getMaPGG(),
                dto.getMaChuSoPGG(),
                com.wms.util.InputFormatUtil.formatThousands(dto.getGiaTriGiamGia()),
                com.wms.util.InputFormatUtil.formatThousands(dto.getGiaTriApDungToiThieu()),
                sdf.format(dto.getNgayBatDauApDung()),
                sdf.format(dto.getNgayKetThucApDung()),
                dto.getSlDaDung(),
                dto.getSlToiDa(),
                dto.getTrangThai() != null ? dto.getTrangThai() : "Đang có hiệu lực"
            });
        }
    }

    private void tblPhieuGiamGiaMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tblPhieuGiamGia.getSelectedRow();
        if (row >= 0) {
            String ma = tblPhieuGiamGia.getValueAt(row, 0).toString();
            PhieuGiamGiaDTO dto = controller.timTheoMa(ma);
            if (dto != null) {
                txtMaPGG.setText(dto.getMaPGG());
                txtMaChuSoPGG.setText(dto.getMaChuSoPGG());
                txtGiaTriGiamGia.setText(com.wms.util.InputFormatUtil.formatThousands(dto.getGiaTriGiamGia()));
                txtGiaTriApDungToiThieu.setText(com.wms.util.InputFormatUtil.formatThousands(dto.getGiaTriApDungToiThieu()));
                txtSLToiDa.setText(com.wms.util.InputFormatUtil.formatThousands(dto.getSlToiDa()));
                txtNgayBatDauApDung.setText(sdf.format(dto.getNgayBatDauApDung()));
                txtNgayKetThucApDung.setText(sdf.format(dto.getNgayKetThucApDung()));
                cbxTrangThai.setSelectedItem(dto.getTrangThai() != null ? dto.getTrangThai() : "Đang có hiệu lực");
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnNgung;
    private javax.swing.JButton btnThemMoi;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JComboBox<String> cbxTrangThai;
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
    private javax.swing.JLabel lblTrangThai;
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
