/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.wms.view.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.model.HoaDonDTO;
import com.wms.controller.HoaDonController;

/**
 *
 * @author Thinkapd T14s
 */
import com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.ThanhToanHoaDonForm;
import java.awt.Window;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class QuanLyHoaDonForm extends javax.swing.JPanel {

    private final HoaDonController hoaDonController;
    private DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private HoaDonDTO currentHD;
    private javax.swing.Timer realTimeTimer;

    /**
     * Creates new form QuanLyHoaDonForm
     */
    public QuanLyHoaDonForm() {
        initComponents();
        hoaDonController = new HoaDonController();
        initTable();
        loadDataToTable();
        initRealTimeTimer();
    }

    private void initRealTimeTimer() {
        realTimeTimer = new javax.swing.Timer(1000, e -> {
            updateRealTimeDuration();
        });
        realTimeTimer.start();
    }

    private void updateRealTimeDuration() {
        if (currentHD == null || txtThoiGianDaDung == null) return;

        // Chỉ chạy timer nếu phiên chưa kết thúc VÀ hóa đơn chưa thanh toán
        boolean isRunning = !"Đã kết thúc".equals(currentHD.getTrangThaiPhien()) && 
                          !"Đã thanh toán".equals(currentHD.getTrangThaiThanhToan());

        if (currentHD.getThoiGianBatDauPhien() != null && isRunning) {
            long now = System.currentTimeMillis();
            long start = currentHD.getThoiGianBatDauPhien().getTime();
            long diff = now - start;

            if (diff > 0) {
                long hours = diff / (3600 * 1000);
                long minutes = (diff % (3600 * 1000)) / (60 * 1000);
                long seconds = (diff % (60 * 1000)) / 1000;
                txtThoiGianDaDung.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            } else {
                txtThoiGianDaDung.setText("00:00:00");
            }
        }
    }

    private void initTable() {
        tableModel = (DefaultTableModel) tblHoaDon.getModel();
    }

    private void loadDataToTable() {
        String searchText = txtTimKiem.getText();
        String statusFilter = cbxLocTrangThai.getSelectedItem().toString();

        List<HoaDonDTO> ds = hoaDonController.layDanhSachHoaDon(searchText, statusFilter);
        tableModel.setRowCount(0);

        for (HoaDonDTO hd : ds) {
            tableModel.addRow(new Object[] {
                    hd.getMaHoaDon(),
                    sdf.format(hd.getNgayLapHoaDon()),
                    hd.getHoTenKH(),
                    df.format(hd.getThanhTien()),
                    hd.getTrangThaiThanhToan(),
                    hd.getMaDatCho() != null ? "Đặt trước" : "Trực tiếp"
            });
        }
    }

    private void showChiTietHoaDon(String maHD) {
        // Lấy thông tin cơ bản từ danh sách
        int row = tblHoaDon.getSelectedRow();
        if (row == -1)
            return;

        txtMaHD.setText(maHD);
        txtKhachHang.setText(tableModel.getValueAt(row, 2).toString());
        txtNgayTao.setText(tableModel.getValueAt(row, 1).toString());
        txtNgayTao.setCaretPosition(0);

        // Tìm DTO tương ứng để lấy thông tin chi tiết hơn
        String searchText = txtTimKiem.getText();
        String statusFilter = cbxLocTrangThai.getSelectedItem().toString();
        List<HoaDonDTO> ds = hoaDonController.layDanhSachHoaDon(searchText, statusFilter);

        for (HoaDonDTO hd : ds) {
            if (hd.getMaHoaDon().equals(maHD)) {
                this.currentHD = hd;
                txtThanhToan.setText(hd.getPhuongThucThanhToan() != null ? hd.getPhuongThucThanhToan() : "Chưa chọn");
                txtTruocGiamGia.setText(df.format(hd.getTongTien()));
                txtSauGiamGia.setText(df.format(hd.getThanhTien()));
                txtTrangThai.setText(hd.getTrangThaiThanhToan());
                if (txtHinhThuc != null) {
                    txtHinhThuc.setText(hd.getMaDatCho() != null ? "Đặt trước" : "Trực tiếp");
                }
                if (txtTrangThaiPhien != null) {
                    txtTrangThaiPhien.setText(hd.getTrangThaiPhien() != null ? hd.getTrangThaiPhien() : "N/A");
                }
                if (txtThoiGianBatDau != null) {
                    txtThoiGianBatDau.setText(hd.getThoiGianBatDauPhien() != null ? sdf.format(hd.getThoiGianBatDauPhien()) : "N/A");
                    txtThoiGianBatDau.setCaretPosition(0);
                }

                // Thiết lập giá trị thời gian đã dùng ban đầu
                if (txtThoiGianDaDung != null) {
                    if (hd.getThoiGianBatDauPhien() != null) {
                        long start = hd.getThoiGianBatDauPhien().getTime();
                        long end = (hd.getThoiGianKetThucPhien() != null) ? hd.getThoiGianKetThucPhien().getTime() : System.currentTimeMillis();
                        long diff = end - start;
                        if (diff > 0) {
                            long hours = diff / (3600 * 1000);
                            long minutes = (diff % (3600 * 1000)) / (60 * 1000);
                            long seconds = (diff % (60 * 1000)) / 1000;
                            txtThoiGianDaDung.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                        } else {
                            txtThoiGianDaDung.setText("00:00:00");
                        }
                    } else {
                        txtThoiGianDaDung.setText("00:00:00");
                    }
                }

                boolean isChoThanhToan = hd.getTrangThaiThanhToan().equals("Chưa thanh toán") || 
                                         hd.getTrangThaiThanhToan().equals("Đang chờ thanh toán");
                btnXacNhan.setEnabled(isChoThanhToan);
                btnHuy.setEnabled(isChoThanhToan);
                break;
            }
        }
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
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        pnLeft = new javax.swing.JPanel();
        lblListTitle = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        cbxLocTrangThai = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHoaDon = new javax.swing.JTable();
        btnTimKiem = new javax.swing.JButton();
        btnLamMoi = new javax.swing.JButton();
        pnRight = new javax.swing.JPanel();
        lblDetailTitle = new javax.swing.JLabel();
        lblMaHD = new javax.swing.JLabel();
        txtMaHD = new javax.swing.JTextField();
        lblKhachHang = new javax.swing.JLabel();
        txtKhachHang = new javax.swing.JTextField();
        lblNgayTao = new javax.swing.JLabel();
        txtNgayTao = new javax.swing.JTextField();
        lblThanhToan = new javax.swing.JLabel();
        txtThanhToan = new javax.swing.JTextField();
        lblTongTien = new javax.swing.JLabel();
        txtTruocGiamGia = new javax.swing.JTextField();
        lblTrangThai = new javax.swing.JLabel();
        txtTrangThai = new javax.swing.JTextField();
        btnXacNhan = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        btnInHoaDon = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        lblTongTien1 = new javax.swing.JLabel();
        txtSauGiamGia = new javax.swing.JTextField();
        txtHinhThuc = new javax.swing.JTextField();
        lblTrangThai1 = new javax.swing.JLabel();
        txtTrangThaiPhien = new javax.swing.JTextField();
        lblThanhToan1 = new javax.swing.JLabel();
        txtThoiGianDaDung = new javax.swing.JTextField();
        lblNgayTao1 = new javax.swing.JLabel();
        txtThoiGianBatDau = new javax.swing.JTextField();
        lblNgayTao2 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(254, 248, 250));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 640));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ HÓA ĐƠN TẠI QUẦY");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 50);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 50);

        pnLeft.setBackground(new java.awt.Color(255, 255, 255));
        pnLeft.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeft.setLayout(null);

        lblListTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblListTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblListTitle.setText("DANH SÁCH HÓA ĐƠN");
        pnLeft.add(lblListTitle);
        lblListTitle.setBounds(20, 15, 200, 30);

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtTimKiem.setToolTipText("Tìm theo mã hóa đơn hoặc khách hàng...");
        pnLeft.add(txtTimKiem);
        txtTimKiem.setBounds(20, 55, 280, 35);

        cbxLocTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxLocTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả", "Chưa thanh toán", "Đã thanh toán", "Đã hủy" }));
        pnLeft.add(cbxLocTrangThai);
        cbxLocTrangThai.setBounds(400, 55, 180, 35);

        tblHoaDon.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã HĐ", "Ngày tạo", "Khách hàng", "Tổng tiền", "Trạng thái", "Hình thức"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblHoaDon.setRowHeight(30);
        tblHoaDon.setSelectionBackground(new java.awt.Color(235, 94, 141));
        tblHoaDon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHoaDonMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblHoaDon);

        pnLeft.add(jScrollPane1);
        jScrollPane1.setBounds(20, 110, 560, 410);

        btnTimKiem.setBackground(new java.awt.Color(235, 94, 141));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        btnTimKiem.setText("Tìm");
        btnTimKiem.addActionListener(this::btnTimKiemActionPerformed);
        pnLeft.add(btnTimKiem);
        btnTimKiem.setBounds(310, 55, 80, 35);

        btnLamMoi.setBackground(new java.awt.Color(235, 94, 141));
        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(255, 255, 255));
        btnLamMoi.setText("Làm mới danh sách");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
        pnLeft.add(btnLamMoi);
        btnLamMoi.setBounds(210, 20, 180, 20);

        pnMain.add(pnLeft);
        pnLeft.setBounds(20, 70, 600, 540);

        pnRight.setBackground(new java.awt.Color(255, 255, 255));
        pnRight.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRight.setLayout(null);

        lblDetailTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDetailTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblDetailTitle.setText("CHI TIẾT HÓA ĐƠN");
        pnRight.add(lblDetailTitle);
        lblDetailTitle.setBounds(20, 15, 200, 30);

        lblMaHD.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaHD.setForeground(new java.awt.Color(136, 136, 136));
        lblMaHD.setText("Mã hóa đơn");
        pnRight.add(lblMaHD);
        lblMaHD.setBounds(20, 60, 100, 18);

        txtMaHD.setEditable(false);
        txtMaHD.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        txtMaHD.setBorder(null);
        pnRight.add(txtMaHD);
        txtMaHD.setBounds(20, 80, 390, 30);

        lblKhachHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKhachHang.setForeground(new java.awt.Color(136, 136, 136));
        lblKhachHang.setText("Khách hàng");
        pnRight.add(lblKhachHang);
        lblKhachHang.setBounds(20, 120, 100, 18);

        txtKhachHang.setEditable(false);
        txtKhachHang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtKhachHang.setBorder(null);
        pnRight.add(txtKhachHang);
        txtKhachHang.setBounds(20, 140, 180, 30);

        lblNgayTao.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayTao.setForeground(new java.awt.Color(136, 136, 136));
        lblNgayTao.setText("Thời gian tạo");
        pnRight.add(lblNgayTao);
        lblNgayTao.setBounds(20, 180, 100, 18);

        txtNgayTao.setEditable(false);
        txtNgayTao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtNgayTao.setBorder(null);
        pnRight.add(txtNgayTao);
        txtNgayTao.setBounds(20, 200, 180, 30);

        lblThanhToan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblThanhToan.setForeground(new java.awt.Color(136, 136, 136));
        lblThanhToan.setText("Phương thức TT");
        pnRight.add(lblThanhToan);
        lblThanhToan.setBounds(20, 240, 150, 18);

        txtThanhToan.setEditable(false);
        txtThanhToan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtThanhToan.setBorder(null);
        pnRight.add(txtThanhToan);
        txtThanhToan.setBounds(20, 260, 180, 30);

        lblTongTien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTongTien.setForeground(new java.awt.Color(136, 136, 136));
        lblTongTien.setText("Tổng cộng (Trước giảm giá)");
        pnRight.add(lblTongTien);
        lblTongTien.setBounds(20, 300, 200, 18);

        txtTruocGiamGia.setEditable(false);
        txtTruocGiamGia.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtTruocGiamGia.setForeground(new java.awt.Color(235, 94, 141));
        txtTruocGiamGia.setBorder(null);
        pnRight.add(txtTruocGiamGia);
        txtTruocGiamGia.setBounds(20, 320, 180, 40);

        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTrangThai.setForeground(new java.awt.Color(136, 136, 136));
        lblTrangThai.setText("Trạng thái thanh toán");
        pnRight.add(lblTrangThai);
        lblTrangThai.setBounds(20, 370, 140, 18);

        txtTrangThai.setEditable(false);
        txtTrangThai.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        txtTrangThai.setBorder(null);
        pnRight.add(txtTrangThai);
        txtTrangThai.setBounds(20, 390, 180, 40);

        btnXacNhan.setBackground(new java.awt.Color(0, 153, 51));
        btnXacNhan.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnXacNhan.setForeground(new java.awt.Color(255, 255, 255));
        btnXacNhan.setText("Xác nhận Thanh toán & Gửi Mail");
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(this::btnXacNhanActionPerformed);
        pnRight.add(btnXacNhan);
        btnXacNhan.setBounds(20, 440, 390, 40);

        btnXoa.setBackground(new java.awt.Color(220, 53, 69));
        btnXoa.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXoa.setForeground(new java.awt.Color(255, 255, 255));
        btnXoa.setText("Xóa hóa đơn");
        btnXoa.addActionListener(this::btnXoaActionPerformed);
        pnRight.add(btnXoa);
        btnXoa.setBounds(160, 490, 130, 40);

        btnInHoaDon.setBackground(new java.awt.Color(235, 94, 141));
        btnInHoaDon.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnInHoaDon.setForeground(new java.awt.Color(255, 255, 255));
        btnInHoaDon.setText("In hóa đơn");
        btnInHoaDon.addActionListener(this::btnInHoaDonActionPerformed);
        pnRight.add(btnInHoaDon);
        btnInHoaDon.setBounds(300, 490, 110, 40);

        btnHuy.setBackground(new java.awt.Color(220, 53, 69));
        btnHuy.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnHuy.setForeground(new java.awt.Color(255, 255, 255));
        btnHuy.setText("Huỷ hóa đơn");
        btnHuy.addActionListener(this::btnHuyActionPerformed);
        pnRight.add(btnHuy);
        btnHuy.setBounds(20, 490, 130, 40);

        lblTongTien1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTongTien1.setForeground(new java.awt.Color(136, 136, 136));
        lblTongTien1.setText("Tổng cộng (Sau giảm giá)");
        pnRight.add(lblTongTien1);
        lblTongTien1.setBounds(230, 300, 180, 18);

        txtSauGiamGia.setEditable(false);
        txtSauGiamGia.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtSauGiamGia.setForeground(new java.awt.Color(235, 94, 141));
        txtSauGiamGia.setBorder(null);
        pnRight.add(txtSauGiamGia);
        txtSauGiamGia.setBounds(230, 320, 180, 40);

        txtHinhThuc.setEditable(false);
        txtHinhThuc.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        txtHinhThuc.setBorder(null);
        txtHinhThuc.addActionListener(this::txtHinhThucActionPerformed);
        pnRight.add(txtHinhThuc);
        txtHinhThuc.setBounds(230, 390, 180, 40);

        lblTrangThai1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTrangThai1.setForeground(new java.awt.Color(136, 136, 136));
        lblTrangThai1.setText("Hình thức");
        pnRight.add(lblTrangThai1);
        lblTrangThai1.setBounds(230, 370, 100, 18);

        txtTrangThaiPhien.setEditable(false);
        txtTrangThaiPhien.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTrangThaiPhien.setBorder(null);
        pnRight.add(txtTrangThaiPhien);
        txtTrangThaiPhien.setBounds(230, 260, 180, 30);

        lblThanhToan1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblThanhToan1.setForeground(new java.awt.Color(136, 136, 136));
        lblThanhToan1.setText("Trạng thái phiên");
        pnRight.add(lblThanhToan1);
        lblThanhToan1.setBounds(230, 240, 150, 18);

        txtThoiGianDaDung.setEditable(false);
        txtThoiGianDaDung.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtThoiGianDaDung.setBorder(null);
        pnRight.add(txtThoiGianDaDung);
        txtThoiGianDaDung.setBounds(230, 200, 180, 30);

        lblNgayTao1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayTao1.setForeground(new java.awt.Color(136, 136, 136));
        lblNgayTao1.setText("Thời gian đã dùng");
        pnRight.add(lblNgayTao1);
        lblNgayTao1.setBounds(230, 180, 140, 18);

        txtThoiGianBatDau.setBorder(null);
        txtThoiGianBatDau.addActionListener(this::txtThoiGianBatDauActionPerformed);
        pnRight.add(txtThoiGianBatDau);
        txtThoiGianBatDau.setBounds(230, 140, 180, 30);

        lblNgayTao2.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayTao2.setForeground(new java.awt.Color(136, 136, 136));
        lblNgayTao2.setText("Thời gian bắt đầu phiên");
        pnRight.add(lblNgayTao2);
        lblNgayTao2.setBounds(230, 120, 160, 18);

        pnMain.add(pnRight);
        pnRight.setBounds(630, 70, 420, 540);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtHinhThucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHinhThucActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtHinhThucActionPerformed

    private void txtThoiGianBatDauActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtThoiGianBatDauActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtThoiGianBatDauActionPerformed

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {
        String maHD = txtMaHD.getText();
        if (maHD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần hủy!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn hủy hóa đơn này?", "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (hoaDonController.huyHoaDon(maHD)) {
                JOptionPane.showMessageDialog(this, "Đã hủy hóa đơn thành công!");
                loadDataToTable();
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Hủy hóa đơn thất bại!");
            }
        }
    }

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {
        txtTimKiem.setText("");
        cbxLocTrangThai.setSelectedIndex(0);
        loadDataToTable();
        resetForm();
    }

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        loadDataToTable();
    }

    private void tblHoaDonMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tblHoaDon.getSelectedRow();
        if (row != -1) {
            String maHD = tableModel.getValueAt(row, 0).toString();
            showChiTietHoaDon(maHD);
        }
    }

    private void btnXacNhanActionPerformed(java.awt.event.ActionEvent evt) {
        String maHD = txtMaHD.getText();
        if (maHD.isEmpty())
            return;

        // Mở form thanh toán chi tiết
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Thanh Toán Hóa Đơn", JDialog.ModalityType.APPLICATION_MODAL);
        ThanhToanHoaDonForm thanhToanForm = new ThanhToanHoaDonForm(maHD);

        dialog.setContentPane(thanhToanForm);
        dialog.pack();
        dialog.setLocationRelativeTo(parentWindow);

        dialog.setVisible(true);

        // Sau khi đóng dialog, load lại bảng để cập nhật trạng thái
        loadDataToTable();
        showChiTietHoaDon(maHD);
    }

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {
        String maHD = txtMaHD.getText();
        if (maHD.isEmpty())
            return;

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn XÓA VĨNH VIỄN hóa đơn này?",
                "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (hoaDonController.xoaHoaDon(maHD)) {
                JOptionPane.showMessageDialog(this, "Đã xóa hóa đơn!");
                loadDataToTable();
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại (Hóa đơn có thể đang được tham chiếu hoặc phiên làm việc đang hoạt động)!");
            }
        }
    }

    private void btnInHoaDonActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Tính năng in hóa đơn đang được phát triển!");
    }

    private void resetForm() {
        txtMaHD.setText("");
        txtKhachHang.setText("");
        txtNgayTao.setText("");
        txtThanhToan.setText("");
        txtTruocGiamGia.setText("");
        txtSauGiamGia.setText("");
        txtTrangThai.setText("");
        btnXacNhan.setEnabled(false);
        btnHuy.setEnabled(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnInHoaDon;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXacNhan;
    private javax.swing.JButton btnXoa;
    private javax.swing.JComboBox<String> cbxLocTrangThai;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDetailTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblKhachHang;
    private javax.swing.JLabel lblListTitle;
    private javax.swing.JLabel lblMaHD;
    private javax.swing.JLabel lblNgayTao;
    private javax.swing.JLabel lblNgayTao1;
    private javax.swing.JLabel lblNgayTao2;
    private javax.swing.JLabel lblThanhToan;
    private javax.swing.JLabel lblThanhToan1;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JLabel lblTongTien1;
    private javax.swing.JLabel lblTrangThai;
    private javax.swing.JLabel lblTrangThai1;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeft;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnRight;
    private javax.swing.JTable tblHoaDon;
    private javax.swing.JTextField txtHinhThuc;
    private javax.swing.JTextField txtKhachHang;
    private javax.swing.JTextField txtMaHD;
    private javax.swing.JTextField txtNgayTao;
    private javax.swing.JTextField txtSauGiamGia;
    private javax.swing.JTextField txtThanhToan;
    private javax.swing.JTextField txtThoiGianBatDau;
    private javax.swing.JTextField txtThoiGianDaDung;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txtTrangThai;
    private javax.swing.JTextField txtTrangThaiPhien;
    private javax.swing.JTextField txtTruocGiamGia;
    // End of variables declaration//GEN-END:variables
}





