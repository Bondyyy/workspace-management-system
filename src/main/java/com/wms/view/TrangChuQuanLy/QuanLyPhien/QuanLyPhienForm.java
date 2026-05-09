package com.wms.view.TrangChuQuanLy.QuanLyPhien;

import com.wms.controller.TrangChuQuanLy.QuanLyPhien.QuanLyPhienController;
public class QuanLyPhienForm extends javax.swing.JPanel {

    /**
     * Creates new form QuanLyPhienForm
     */
    private final com.wms.controller.TrangChuQuanLy.QuanLyPhien.QuanLyPhienController controller;
    private java.util.List<com.wms.model.PhienLamViecFullDTO> currentList;
    private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy");
    private javax.swing.Timer realTimeTimer;

    public QuanLyPhienForm() {
        initComponents();
        setupTable();
        controller = new com.wms.controller.TrangChuQuanLy.QuanLyPhien.QuanLyPhienController(this);
        loadChiNhanhData();
        kiemTraQuyen();
        loadData("");
        initRealTimeTimer();
    }

    private void setupTable() {
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        
        // Căn lề cho các cột: Mã Phiên, Bắt đầu, Kết thúc, Trạng thái, Thanh toán, Hình thức
        int[] centerCols = {0, 3, 4, 5, 6, 7};
        for (int col : centerCols) {
            tblPhienLamViec.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);
        }
    }

    private void initRealTimeTimer() {
        realTimeTimer = new javax.swing.Timer(1000, e -> {
            updateRealTimeDurations();
        });
        realTimeTimer.start();
    }

    private void updateRealTimeDurations() {
        if (currentList == null || tblPhienLamViec == null)
            return;

        long now = System.currentTimeMillis();
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblPhienLamViec.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            if (i >= currentList.size())
                break;
            com.wms.model.PhienLamViecFullDTO p = currentList.get(i);

            if (p.getThoiGianKetThuc() == null && p.getThoiGianBatDau() != null
                    && !p.getThoiGianBatDau().after(new java.util.Date())) {
                long start = p.getThoiGianBatDau().getTime();
                long diff = now - start;

                long hours = diff / (3600 * 1000);
                long minutes = (diff % (3600 * 1000)) / (60 * 1000);
                long seconds = (diff % (60 * 1000)) / 1000;

                String duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                model.setValueAt("Chạy: " + duration, i, 4);

                int selectedRow = tblPhienLamViec.getSelectedRow();
                if (selectedRow == i) {
                    if (txtThoiGianDaDung != null) {
                        txtThoiGianDaDung.setText(duration);
                        txtThoiGianDaDung.setCaretPosition(0);
                    }
                    if (txtKetThuc != null) {
                        txtKetThuc.setText(new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy").format(new java.util.Date()));
                        txtKetThuc.setCaretPosition(0);
                    }
                }
            }
        }
    }

    private void loadChiNhanhData() {
        cbxChiNhanh.removeAllItems();
        cbxChiNhanh.addItem("--- Tất cả chi nhánh ---");
        for (String[] cn : controller.layDanhSachChiNhanh()) {
            cbxChiNhanh.addItem(cn[1]);
            cbxChiNhanh.putClientProperty("maCN_" + cn[1], cn[0]);
        }
        cbxChiNhanh.addActionListener(e -> loadData(txtTimKiem.getText().trim()));
    }

    private void kiemTraQuyen() {
        com.wms.model.NguoiDungDTO user = com.wms.controller.DangNhapController.getCurrentUser();
        if (user == null) return;

        boolean isAdmin = user.hasRole("VT1") || user.hasRole("Quản trị hệ thống");
        if (!isAdmin) {
            cbxChiNhanh.setEnabled(false);
            String maCN = controller.layMaCNNguoiDung(user);
            if (maCN != null) setComboByMaCN(maCN);
        }
    }

    private void setComboByMaCN(String maCN) {
        for (int i = 1; i < cbxChiNhanh.getItemCount(); i++) {
            String ten = cbxChiNhanh.getItemAt(i);
            if (maCN.equals(cbxChiNhanh.getClientProperty("maCN_" + ten))) {
                cbxChiNhanh.setSelectedIndex(i);
                break;
            }
        }
    }

    private String getMaCNDangChon() {
        if (cbxChiNhanh.getSelectedIndex() <= 0)
            return null;
        String ten = (String) cbxChiNhanh.getSelectedItem();
        return (String) cbxChiNhanh.getClientProperty("maCN_" + ten);
    }

    private void loadData(String keyword) {
        controller.loadDanhSachPhien(keyword, getMaCNDangChon());
    }

    public void hienThiDanhSachPhien(java.util.List<com.wms.model.PhienLamViecFullDTO> list) {
        this.currentList = list;
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblPhienLamViec.getModel();
        model.setRowCount(0);
        for (com.wms.model.PhienLamViecFullDTO p : list) {
            model.addRow(new Object[] {
                    p.getMaPhien(),
                    p.getTenKhongGian(),
                    p.getTenKhachHang(),
                    p.getThoiGianBatDau() != null ? sdf.format(p.getThoiGianBatDau()) : "",
                    p.getThoiGianKetThuc() != null ? sdf.format(p.getThoiGianKetThuc())
                            : (p.getTrangThaiPhien().equals("Đã đặt trước") && p.getThoiGianBatDau() != null
                                    && p.getThoiGianBatDau().after(new java.util.Date())
                                            ? "(Chưa bắt đầu)"
                                            : "(Đang chạy)"),
                    (p.getThoiGianBatDau() != null && !p.getThoiGianBatDau().after(new java.util.Date())
                            && p.getThoiGianKetThuc() == null) ? "Đang sử dụng" : p.getTrangThaiPhien(),
                    p.getTrangThaiThanhToan() != null ? p.getTrangThaiThanhToan() : "Chưa thanh toán",
                    p.getMaDatCho() != null ? "Đặt trước" : "Trực tiếp"
            });
        }
    }

    public void hienThiDichVuTrongPhien(java.util.List<com.wms.model.DichVuTrongPhienDTO> list) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblDichVu.getModel();
        model.setRowCount(0);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,### VNĐ");
        for (com.wms.model.DichVuTrongPhienDTO dv : list) {
            model.addRow(new Object[] {
                    dv.getTenDV(),
                    dv.getSoLuong(),
                    df.format(dv.getDonGia()),
                    df.format(dv.getThanhTien())
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        pnLeft = new javax.swing.JPanel();
        lblListTitle = new javax.swing.JLabel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        btnTaiLai = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPhienLamViec = new javax.swing.JTable();
        lblChiNhanh = new javax.swing.JLabel();
        cbxChiNhanh = new javax.swing.JComboBox<>();
        pnRight = new javax.swing.JPanel();
        lblDetailTitle = new javax.swing.JLabel();
        lblMaPhien = new javax.swing.JLabel();
        txtMaPhien = new javax.swing.JTextField();
        lblKhongGian = new javax.swing.JLabel();
        txtKhongGian = new javax.swing.JTextField();
        lblKhachHang = new javax.swing.JLabel();
        txtKhachHang = new javax.swing.JTextField();
        lblTrangThai = new javax.swing.JLabel();
        txtTrangThaiThanhToan = new javax.swing.JTextField();
        lblBatDau = new javax.swing.JLabel();
        txtBatDau = new javax.swing.JTextField();
        lblDuKien = new javax.swing.JLabel();
        txtDuKien = new javax.swing.JTextField();
        lblKetThuc = new javax.swing.JLabel();
        txtKetThuc = new javax.swing.JTextField();
        lblDichVu = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDichVu = new javax.swing.JTable();
        btnMoPhien = new javax.swing.JButton();
        btnKetThucPhien = new javax.swing.JButton();
        btnCapNhat = new javax.swing.JButton();
        btnXacNhan = new javax.swing.JButton();
        lblTrangThai1 = new javax.swing.JLabel();
        txtHinhThuc = new javax.swing.JTextField();
        lblDuKien1 = new javax.swing.JLabel();
        txtThoiGianDaDung = new javax.swing.JTextField();
        lblDuKien2 = new javax.swing.JLabel();
        btnXoa = new javax.swing.JButton();
        cbxTrangThai = new javax.swing.JComboBox<>();

        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(254, 248, 250));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 640));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("QUẢN LÝ PHIÊN LÀM VIỆC");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 50);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 50);

        pnLeft.setBackground(new java.awt.Color(255, 255, 255));
        pnLeft.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeft.setLayout(null);

        lblListTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblListTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblListTitle.setText("DANH SÁCH PHIÊN");
        pnLeft.add(lblListTitle);
        lblListTitle.setBounds(20, 15, 200, 30);

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtTimKiem.setToolTipText("Tìm mã phiên / khách hàng...");
        pnLeft.add(txtTimKiem);
        txtTimKiem.setBounds(20, 55, 380, 35);

        btnTimKiem.setBackground(new java.awt.Color(235, 94, 141));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        btnTimKiem.setText("Tìm");
        btnTimKiem.addActionListener(this::btnTimKiemActionPerformed);
        pnLeft.add(btnTimKiem);
        btnTimKiem.setBounds(410, 60, 80, 35);

        btnTaiLai.setBackground(new java.awt.Color(235, 94, 141));
        btnTaiLai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTaiLai.setForeground(new java.awt.Color(255, 255, 255));
        btnTaiLai.setText("Tải lại");
        btnTaiLai.addActionListener(this::btnTaiLaiActionPerformed);
        pnLeft.add(btnTaiLai);
        btnTaiLai.setBounds(500, 60, 95, 35);

        tblPhienLamViec.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã Phiên", "Không Gian", "Khách Hàng", "Bắt đầu", "Kết thúc", "Trạng thái", "Thanh toán", "Hình thức"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPhienLamViec.setRowHeight(30);
        tblPhienLamViec.setSelectionBackground(new java.awt.Color(235, 94, 141));
        tblPhienLamViec.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPhienLamViecMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPhienLamViec);

        pnLeft.add(jScrollPane1);
        jScrollPane1.setBounds(20, 105, 580, 425);

        lblChiNhanh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChiNhanh.setText("Chi nhánh:");
        pnLeft.add(lblChiNhanh);
        lblChiNhanh.setBounds(330, 20, 100, 18);

        cbxChiNhanh.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả chi nhánh", "CN001 - Quận 1", "CN002 - Thủ Đức" }));
        cbxChiNhanh.addActionListener(this::cbxChiNhanhActionPerformed);
        pnLeft.add(cbxChiNhanh);
        cbxChiNhanh.setBounds(410, 20, 180, 28);

        pnMain.add(pnLeft);
        pnLeft.setBounds(20, 70, 620, 550);

        pnRight.setBackground(new java.awt.Color(255, 255, 255));
        pnRight.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRight.setLayout(null);

        lblDetailTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDetailTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblDetailTitle.setText("CHI TIẾT PHIÊN LÀM VIỆC");
        pnRight.add(lblDetailTitle);
        lblDetailTitle.setBounds(20, 15, 250, 30);

        lblMaPhien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaPhien.setForeground(new java.awt.Color(136, 136, 136));
        lblMaPhien.setText("Mã phiên");
        pnRight.add(lblMaPhien);
        lblMaPhien.setBounds(20, 55, 80, 18);

        txtMaPhien.setEditable(false);
        txtMaPhien.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtMaPhien.setBorder(null);
        pnRight.add(txtMaPhien);
        txtMaPhien.setBounds(20, 75, 160, 30);

        lblKhongGian.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKhongGian.setForeground(new java.awt.Color(136, 136, 136));
        lblKhongGian.setText("Không gian");
        pnRight.add(lblKhongGian);
        lblKhongGian.setBounds(210, 55, 100, 18);

        txtKhongGian.setEditable(false);
        txtKhongGian.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtKhongGian.setBorder(null);
        pnRight.add(txtKhongGian);
        txtKhongGian.setBounds(210, 75, 160, 30);

        lblKhachHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKhachHang.setForeground(new java.awt.Color(136, 136, 136));
        lblKhachHang.setText("Khách hàng");
        pnRight.add(lblKhachHang);
        lblKhachHang.setBounds(20, 110, 80, 18);

        txtKhachHang.setEditable(false);
        txtKhachHang.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtKhachHang.setBorder(null);
        pnRight.add(txtKhachHang);
        txtKhachHang.setBounds(20, 130, 160, 30);

        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTrangThai.setForeground(new java.awt.Color(136, 136, 136));
        lblTrangThai.setText("Trạng thái thanh toán");
        pnRight.add(lblTrangThai);
        lblTrangThai.setBounds(210, 110, 150, 18);

        txtTrangThaiThanhToan.setEditable(false);
        txtTrangThaiThanhToan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtTrangThaiThanhToan.setForeground(new java.awt.Color(255, 51, 51));
        txtTrangThaiThanhToan.setBorder(null);
        txtTrangThaiThanhToan.addActionListener(this::txtTrangThaiThanhToanActionPerformed);
        pnRight.add(txtTrangThaiThanhToan);
        txtTrangThaiThanhToan.setBounds(210, 130, 160, 30);

        lblBatDau.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblBatDau.setForeground(new java.awt.Color(136, 136, 136));
        lblBatDau.setText("Giờ bắt đầu");
        pnRight.add(lblBatDau);
        lblBatDau.setBounds(20, 165, 130, 18);

        txtBatDau.setEditable(false);
        txtBatDau.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtBatDau.setBorder(null);
        pnRight.add(txtBatDau);
        txtBatDau.setBounds(20, 185, 160, 30);

        lblDuKien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDuKien.setForeground(new java.awt.Color(136, 136, 136));
        lblDuKien.setText("Dự kiến kết thúc");
        pnRight.add(lblDuKien);
        lblDuKien.setBounds(20, 280, 150, 18);

        txtDuKien.setEditable(false);
        txtDuKien.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtDuKien.setBorder(null);
        txtDuKien.addActionListener(this::txtDuKienActionPerformed);
        pnRight.add(txtDuKien);
        txtDuKien.setBounds(20, 300, 160, 30);

        lblKetThuc.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKetThuc.setForeground(new java.awt.Color(136, 136, 136));
        lblKetThuc.setText("Thời gian kết thúc thực tế");
        pnRight.add(lblKetThuc);
        lblKetThuc.setBounds(20, 220, 170, 18);

        txtKetThuc.setEditable(false);
        txtKetThuc.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        txtKetThuc.setForeground(new java.awt.Color(136, 136, 136));
        txtKetThuc.setText("(Chưa kết thúc)");
        txtKetThuc.setBorder(null);
        pnRight.add(txtKetThuc);
        txtKetThuc.setBounds(20, 240, 160, 30);

        lblDichVu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDichVu.setForeground(new java.awt.Color(0, 0, 58));
        lblDichVu.setText("Các dịch vụ đã sử dụng trong phiên:");
        pnRight.add(lblDichVu);
        lblDichVu.setBounds(20, 340, 370, 20);

        tblDichVu.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tên Dịch Vụ", "SL", "Đơn giá", "Thành tiền"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblDichVu.setRowHeight(25);
        tblDichVu.setSelectionBackground(new java.awt.Color(235, 94, 141));
        jScrollPane2.setViewportView(tblDichVu);

        pnRight.add(jScrollPane2);
        jScrollPane2.setBounds(20, 360, 350, 80);

        btnMoPhien.setBackground(new java.awt.Color(0, 153, 51));
        btnMoPhien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnMoPhien.setForeground(new java.awt.Color(255, 255, 255));
        btnMoPhien.setText("Mở Phiên Mới");
        btnMoPhien.addActionListener(this::btnMoPhienActionPerformed);
        pnRight.add(btnMoPhien);
        btnMoPhien.setBounds(20, 460, 120, 35);

        btnKetThucPhien.setBackground(new java.awt.Color(21, 101, 192));
        btnKetThucPhien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnKetThucPhien.setForeground(new java.awt.Color(255, 255, 255));
        btnKetThucPhien.setText("Kết Thúc Phiên");
        btnKetThucPhien.addActionListener(this::btnKetThucPhienActionPerformed);
        pnRight.add(btnKetThucPhien);
        btnKetThucPhien.setBounds(20, 500, 180, 35);

        btnCapNhat.setBackground(new java.awt.Color(220, 53, 69));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật phiên");
        btnCapNhat.addActionListener(this::btnCapNhatActionPerformed);
        pnRight.add(btnCapNhat);
        btnCapNhat.setBounds(250, 460, 130, 35);

        btnXacNhan.setBackground(new java.awt.Color(220, 53, 69));
        btnXacNhan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnXacNhan.setForeground(new java.awt.Color(255, 255, 255));
        btnXacNhan.setText("Xác nhận thanh toán");
        btnXacNhan.addActionListener(this::btnXacNhanActionPerformed);
        pnRight.add(btnXacNhan);
        btnXacNhan.setBounds(200, 500, 180, 35);

        lblTrangThai1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTrangThai1.setForeground(new java.awt.Color(136, 136, 136));
        lblTrangThai1.setText("Trạng thái");
        pnRight.add(lblTrangThai1);
        lblTrangThai1.setBounds(210, 160, 100, 20);

        txtHinhThuc.setEditable(false);
        txtHinhThuc.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtHinhThuc.setBorder(null);
        txtHinhThuc.addActionListener(this::txtHinhThucActionPerformed);
        pnRight.add(txtHinhThuc);
        txtHinhThuc.setBounds(210, 240, 160, 30);

        lblDuKien1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDuKien1.setForeground(new java.awt.Color(136, 136, 136));
        lblDuKien1.setText("Hình thức");
        pnRight.add(lblDuKien1);
        lblDuKien1.setBounds(210, 220, 150, 18);

        txtThoiGianDaDung.setEditable(false);
        txtThoiGianDaDung.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtThoiGianDaDung.setBorder(null);
        txtThoiGianDaDung.addActionListener(this::txtThoiGianDaDungActionPerformed);
        pnRight.add(txtThoiGianDaDung);
        txtThoiGianDaDung.setBounds(210, 300, 160, 30);

        lblDuKien2.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDuKien2.setForeground(new java.awt.Color(136, 136, 136));
        lblDuKien2.setText("Thời gian đã dùng");
        pnRight.add(lblDuKien2);
        lblDuKien2.setBounds(210, 280, 150, 18);

        btnXoa.setBackground(new java.awt.Color(21, 101, 192));
        btnXoa.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnXoa.setForeground(new java.awt.Color(255, 255, 255));
        btnXoa.setText("Xoá phiên");
        btnXoa.addActionListener(this::btnXoaActionPerformed);
        pnRight.add(btnXoa);
        btnXoa.setBounds(140, 460, 110, 35);

        cbxTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Đang hoạt động", "Đã đặt trước", "Đã kết thúc" }));
        pnRight.add(cbxTrangThai);
        cbxTrangThai.setBounds(210, 185, 160, 30);

        pnMain.add(pnRight);
        pnRight.setBounds(650, 70, 380, 550);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {
        String maPhien = txtMaPhien.getText().trim();
        if (maPhien.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiên cần xóa từ danh sách!");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn xóa phiên " + maPhien + "?\nLưu ý: Hóa đơn và các chi tiết dịch vụ liên quan cũng sẽ bị xóa!", 
                "Xác nhận xóa", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
        
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (controller.xoaPhien(maPhien)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Đã xóa phiên thành công!");
                loadData("");
                btnHuyActionPerformed();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi xóa phiên làm việc!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cbxChiNhanhActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }// GEN-LAST:event_cbxChiNhanhActionPerformed

    private void txtTrangThaiThanhToanActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }// GEN-LAST:event_txtTrangThaiThanhToanActionPerformed

    private void txtDuKienActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }// GEN-LAST:event_txtDuKienActionPerformed

    private void txtThoiGianDaDungActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }// GEN-LAST:event_txtThoiGianDaDungActionPerformed

    private void txtHinhThucActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void btnXacNhanActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblPhienLamViec.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiên đặt chỗ từ danh sách!");
            return;
        }

        com.wms.model.PhienLamViecFullDTO selected = currentList.get(row);
        if (selected.getMaDatCho() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Đây là phiên khách vào trực tiếp, không cần xác nhận thanh toán đặt trước!");
            return;
        }
        if (!"Đang chờ thanh toán".equals(selected.getTrangThaiDatCho())) {
            javax.swing.JOptionPane.showMessageDialog(this, "Phiên này không ở trạng thái 'Đang chờ thanh toán'!");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Xác nhận khách hàng đã thanh toán cho mã đặt " + selected.getMaDatCho() + "?",
                "Xác nhận thanh toán", javax.swing.JOptionPane.YES_NO_OPTION);

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (controller.xacNhanThanhToanDatTruoc(selected)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Xác nhận thanh toán thành công!");
                loadData("");
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật trạng thái!",
                        "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        loadData(txtTimKiem.getText().trim());
    }

    private void btnTaiLaiActionPerformed(java.awt.event.ActionEvent evt) {
        txtTimKiem.setText("");
        loadData("");
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {
        String maPhien = txtMaPhien.getText().trim();
        if (maPhien.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiên cần cập nhật!");
            return;
        }

        String trangThai = cbxTrangThai.getSelectedItem().toString();
        String tenKH = txtKhachHang.getText().trim();

        if (controller.capNhatPhien(maPhien, trangThai, tenKH)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật thông tin phiên thành công!");
            loadData("");
            btnHuyActionPerformed(); // Reset sau khi cập nhật
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật thông tin phiên!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnHuyActionPerformed() {
        txtMaPhien.setText("");
        txtKhongGian.setText("");
        txtKhachHang.setText("");
        txtKhachHang.setEditable(false);
        txtKhachHang.setBackground(new java.awt.Color(240, 240, 240));
        cbxTrangThai.setSelectedIndex(0);
        cbxTrangThai.setEnabled(false);
        btnCapNhat.setEnabled(true);
        txtTrangThaiThanhToan.setText("");
        txtBatDau.setText("");
        txtDuKien.setText("");
        txtKetThuc.setText("(Chưa kết thúc)");
        ((javax.swing.table.DefaultTableModel) tblDichVu.getModel()).setRowCount(0);
        tblPhienLamViec.clearSelection();
    }

    private void btnKetThucPhienActionPerformed(java.awt.event.ActionEvent evt) {
        String maPhien = txtMaPhien.getText().trim();
        if (maPhien.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiên đang chạy!");
            return;
        }

        if ("Đã kết thúc".equals(cbxTrangThai.getSelectedItem().toString())) {
            javax.swing.JOptionPane.showMessageDialog(this, "Phiên này đã kết thúc rồi!");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Xác nhận kết thúc phiên " + maPhien + "?",
                "Xác nhận", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (controller.ketThucPhien(maPhien)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Đã kết thúc phiên thành công!");
                loadData("");
                btnHuyActionPerformed();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi kết thúc phiên!");
            }
        }
    }

    private void btnMoPhienActionPerformed(java.awt.event.ActionEvent evt) {
        String maCN = getMaCNDangChon();
        if (maCN == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một chi nhánh cụ thể để mở phiên làm việc!",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JDialog dialog = new javax.swing.JDialog(
                (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "Mở phiên làm việc mới tại quầy", true);
        dialog.getContentPane().add(new MoPhienMoiForm(maCN));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        loadData("");
    }

    private void tblPhienLamViecMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tblPhienLamViec.getSelectedRow();
        if (row >= 0) {
            String maPhien = tblPhienLamViec.getValueAt(row, 0).toString();
            txtMaPhien.setText(maPhien);
            txtKhongGian.setText(tblPhienLamViec.getValueAt(row, 1).toString());
            txtKhachHang.setText(tblPhienLamViec.getValueAt(row, 2).toString());
            txtBatDau.setText(tblPhienLamViec.getValueAt(row, 3).toString());
            txtBatDau.setCaretPosition(0);

            // Nếu phiên đang chạy hoặc chưa bắt đầu, hiển thị giờ hiện tại là dự kiến kết thúc thực tế
            String ketThucCol = tblPhienLamViec.getValueAt(row, 4).toString();
            if (ketThucCol.startsWith("Chạy:") || ketThucCol.equals("(Chưa bắt đầu)")) {
                java.text.SimpleDateFormat currentSdf = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy");
                txtKetThuc.setText(currentSdf.format(new java.util.Date()) + " (Hiện tại)");
            } else {
                txtKetThuc.setText(ketThucCol);
            }
            txtKetThuc.setCaretPosition(0);

            String trangThaiTable = tblPhienLamViec.getValueAt(row, 5).toString();
            String hinhThuc = tblPhienLamViec.getValueAt(row, 7).toString();
            
            if ("Đã kết thúc".equals(trangThaiTable)) {
                // Nếu đã kết thúc, khóa toàn bộ việc chỉnh sửa
                cbxTrangThai.setSelectedItem("Đã kết thúc");
                cbxTrangThai.setEnabled(false);
                btnCapNhat.setEnabled(false);
                txtKhachHang.setEditable(false);
                txtKhachHang.setBackground(new java.awt.Color(240, 240, 240));
            } else {
                // Nếu chưa kết thúc, cho phép chỉnh sửa trạng thái
                if ("Đang sử dụng".equals(trangThaiTable)) {
                    cbxTrangThai.setSelectedItem("Đang hoạt động");
                } else {
                    cbxTrangThai.setSelectedItem(trangThaiTable);
                }
                cbxTrangThai.setEnabled(true);
                btnCapNhat.setEnabled(true);

                // Logic sửa tên khách hàng cho phiên trực tiếp
                if ("Trực tiếp".equals(hinhThuc)) {
                    txtKhachHang.setEditable(true);
                    txtKhachHang.setBackground(new java.awt.Color(255, 255, 255));
                } else {
                    txtKhachHang.setEditable(false);
                    txtKhachHang.setBackground(new java.awt.Color(240, 240, 240));
                }
            }
            
            txtTrangThaiThanhToan.setText(tblPhienLamViec.getValueAt(row, 6).toString());
            if (txtHinhThuc != null) {
                txtHinhThuc.setText(hinhThuc);
            }

            // Hiển thị thời gian dự kiến kết thúc từ danh sách gốc
            com.wms.model.PhienLamViecFullDTO selected = currentList.get(row);
            if (txtDuKien != null) {
                txtDuKien.setText(selected.getThoiGianDuKienKetThuc() != null
                        ? sdf.format(selected.getThoiGianDuKienKetThuc())
                        : "");
                txtDuKien.setCaretPosition(0);
            }

            // Cập nhật thời gian đã dùng
            if (txtThoiGianDaDung != null) {
                if (selected.getThoiGianBatDau() != null) {
                    long end = (selected.getThoiGianKetThuc() != null) ? selected.getThoiGianKetThuc().getTime()
                            : System.currentTimeMillis();
                    long start = selected.getThoiGianBatDau().getTime();
                    if (end > start) {
                        long diff = end - start;
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

            controller.loadChiTietDichVu(maPhien);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btnKetThucPhien;
    private javax.swing.JButton btnMoPhien;
    private javax.swing.JButton btnTaiLai;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXacNhan;
    private javax.swing.JButton btnXoa;
    private javax.swing.JComboBox<String> cbxChiNhanh;
    private javax.swing.JComboBox<String> cbxTrangThai;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblBatDau;
    private javax.swing.JLabel lblChiNhanh;
    private javax.swing.JLabel lblDetailTitle;
    private javax.swing.JLabel lblDichVu;
    private javax.swing.JLabel lblDuKien;
    private javax.swing.JLabel lblDuKien1;
    private javax.swing.JLabel lblDuKien2;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblKetThuc;
    private javax.swing.JLabel lblKhachHang;
    private javax.swing.JLabel lblKhongGian;
    private javax.swing.JLabel lblListTitle;
    private javax.swing.JLabel lblMaPhien;
    private javax.swing.JLabel lblTrangThai;
    private javax.swing.JLabel lblTrangThai1;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeft;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnRight;
    private javax.swing.JTable tblDichVu;
    private javax.swing.JTable tblPhienLamViec;
    private javax.swing.JTextField txtBatDau;
    private javax.swing.JTextField txtDuKien;
    private javax.swing.JTextField txtHinhThuc;
    private javax.swing.JTextField txtKetThuc;
    private javax.swing.JTextField txtKhachHang;
    private javax.swing.JTextField txtKhongGian;
    private javax.swing.JTextField txtMaPhien;
    private javax.swing.JTextField txtThoiGianDaDung;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txtTrangThaiThanhToan;
    // End of variables declaration//GEN-END:variables
}
