package com.wms.view.TrangChuQuanLy.QuanLyPhien;

import com.wms.controller.TrangChuQuanLy.QuanLyPhien.QuanLyPhienController;
import java.text.Normalizer;

public class QuanLyPhienForm extends javax.swing.JPanel {

    /**
     * Creates new form QuanLyPhienForm
     */
    private final com.wms.controller.TrangChuQuanLy.QuanLyPhien.QuanLyPhienController controller;
    private java.util.List<com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO> currentList;
    private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
    private javax.swing.Timer realTimeTimer;
    private javax.swing.SwingWorker<java.util.List<com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO>, Void> loadPhienWorker;
    private static final String LABEL_CHUA_KET_THUC = "Ch\u01b0a k\u1ebft th\u00fac";
    private static final String LABEL_DANG_HOAT_DONG = "\u0110ang ho\u1ea1t \u0111\u1ed9ng";
    private static final String LABEL_DA_KET_THUC = "\u0110\u00e3 k\u1ebft th\u00fac";
    private static final String LABEL_TAM_NGUNG = "T\u1ea1m ng\u1eebng";
    private final java.util.Map<Integer, String> maCNTheoIndex = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> indexTheoMaCN = new java.util.HashMap<>();
    private String maCNNguoiDungHienTai;
    private boolean thieuChiNhanhHoatDong;

    public QuanLyPhienForm() {
        initComponents();
        setupTable();
        controller = new com.wms.controller.TrangChuQuanLy.QuanLyPhien.QuanLyPhienController(this);
        loadChiNhanhData();
        kiemTraQuyen();
        loadData("");
        initRealTimeTimer();
        com.wms.util.TienIchFormQuanLy.apDung(this);
    }

    private void setupTable() {
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);

        // Căn lề cho các cột: Mã Phiên, Bắt đầu, Kết thúc, Trạng thái, Thanh toán, Hình
        // thức
        int[] centerCols = { 0, 3, 4, 5, 6, 7 };
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
            com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO p = currentList.get(i);

            if (p.getThoiGianKetThuc() == null && p.getThoiGianBatDau() != null
                    && !p.getThoiGianBatDau().after(new java.util.Date())) {
                long start = p.getThoiGianBatDau().getTime();
                long diff = now - start;

                long hours = diff / (3600 * 1000);
                long minutes = (diff % (3600 * 1000)) / (60 * 1000);
                long seconds = (diff % (60 * 1000)) / 1000;

                String duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                int selectedRow = tblPhienLamViec.getSelectedRow();
                int selectedModelRow = selectedRow >= 0 ? tblPhienLamViec.convertRowIndexToModel(selectedRow) : -1;
                if (selectedModelRow == i) {
                    if (txtThoiGianDaDung != null) {
                        txtThoiGianDaDung.setText(duration);
                        txtThoiGianDaDung.setCaretPosition(0);
                    }
                    if (txtKetThuc != null) {
                        txtKetThuc.setText(LABEL_CHUA_KET_THUC);
                        txtKetThuc.setCaretPosition(0);
                    }
                }
            }
        }
    }

    private void loadChiNhanhData() {
        cbxChiNhanh.removeAllItems();
        maCNTheoIndex.clear();
        indexTheoMaCN.clear();
        cbxChiNhanh.addItem("--- Tất cả chi nhánh ---");
        for (String[] cn : controller.layDanhSachChiNhanh()) {
            if (cn == null || cn.length < 2 || cn[0] == null || cn[0].isBlank()) {
                continue;
            }
            String maCN = cn[0].trim();
            String tenCN = cn[1] == null || cn[1].isBlank() ? maCN : cn[1].trim();
            int index = cbxChiNhanh.getItemCount();
            cbxChiNhanh.addItem(maCN + " - " + tenCN);
            maCNTheoIndex.put(index, maCN);
            indexTheoMaCN.put(maCN.toUpperCase(java.util.Locale.ROOT), index);
        }
        cbxChiNhanh.addActionListener(e -> loadData(txtTimKiem.getText().trim()));
    }

    private void kiemTraQuyen() {
        com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO user = com.wms.controller.TrangChuGioiThieu.DangNhapController
                .getCurrentUser();
        if (user == null)
            return;

        // VT01 là mã chuẩn cho Admin, kiểm tra thêm tên vai trò đầy đủ
        boolean isAdmin = user.hasRole("VT01") || user.hasRole("Quản trị hệ thống")
                || user.hasRole("Quản trị Viên hệ thống");

        if (!isAdmin) {
            cbxChiNhanh.setEnabled(false);
            String maCN = controller.layMaCNNguoiDung(user);
            maCNNguoiDungHienTai = maCN == null ? null : maCN.trim();
            if (maCNNguoiDungHienTai == null || maCNNguoiDungHienTai.isBlank()
                    || !setComboByMaCN(maCNNguoiDungHienTai)) {
                maCNNguoiDungHienTai = null;
                thieuChiNhanhHoatDong = true;
                btnMoPhien.setEnabled(false);
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Tài khoản của bạn chưa được gán chi nhánh hoạt động, vui lòng liên hệ quản trị viên.",
                        "Thiếu chi nhánh hoạt động", javax.swing.JOptionPane.WARNING_MESSAGE);
            } else {
                thieuChiNhanhHoatDong = false;
                btnMoPhien.setEnabled(true);
            }
        } else {
            maCNNguoiDungHienTai = null;
            thieuChiNhanhHoatDong = false;
            cbxChiNhanh.setEnabled(true); // Đảm bảo Admin luôn được chọn
        }
    }

    private boolean setComboByMaCN(String maCN) {
        if (maCN == null || maCN.isBlank()) {
            return false;
        }
        Integer index = indexTheoMaCN.get(maCN.trim().toUpperCase(java.util.Locale.ROOT));
        if (index == null) {
            return false;
        }
        cbxChiNhanh.setSelectedIndex(index);
        return true;
    }

    private String getMaCNDangChon() {
        if (!cbxChiNhanh.isEnabled() && maCNNguoiDungHienTai != null && !maCNNguoiDungHienTai.isBlank()) {
            return maCNNguoiDungHienTai;
        }
        int selectedIndex = cbxChiNhanh.getSelectedIndex();
        if (selectedIndex <= 0) {
            return null;
        }
        return maCNTheoIndex.get(selectedIndex);
    }

    private void loadData(String keyword) {
        if (loadPhienWorker != null && !loadPhienWorker.isDone()) {
            loadPhienWorker.cancel(true);
        }
        if (thieuChiNhanhHoatDong) {
            hienThiDanhSachPhien(java.util.List.of());
            setDangTaiDanhSach(false);
            return;
        }
        String maCN = getMaCNDangChon();
        long start = System.currentTimeMillis();
        setDangTaiDanhSach(true);
        loadPhienWorker = new javax.swing.SwingWorker<>() {
            @Override
            protected java.util.List<com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO> doInBackground() {
                return controller.layDanhSachPhien(keyword, maCN);
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        return;
                    }
                    hienThiDanhSachPhien(get());
                    System.out.println("[QuanLyPhienForm] load danh sach phien mat "
                            + (System.currentTimeMillis() - start) + " ms");
                } catch (Exception ex) {
                    com.wms.util.MessageUtil.showError(QuanLyPhienForm.this, "Lỗi tải danh sách phiên.", ex);
                } finally {
                    setDangTaiDanhSach(false);
                }
            }
        };
        loadPhienWorker.execute();
    }

    private void setDangTaiDanhSach(boolean dangTai) {
        btnTimKiem.setEnabled(!dangTai);
        txtTimKiem.setEnabled(!dangTai);
        tblPhienLamViec.setEnabled(!dangTai);
        if (dangTai) {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblPhienLamViec.getModel();
            model.setRowCount(0);
            model.addRow(new Object[]{"Đang tải...", "", "", "", "", "", "", ""});
        }
    }

    public void hienThiDanhSachPhien(
            java.util.List<com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO> list) {
        this.currentList = list;
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblPhienLamViec.getModel();
        model.setRowCount(0);
        for (com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO p : list) {
            model.addRow(new Object[] {
                    p.getMaPhien(),
                    p.getTenKhongGian(),
                    p.getTenKhachHang(),
                    p.getThoiGianBatDau() != null ? sdf.format(p.getThoiGianBatDau()) : "",
                    p.getThoiGianKetThuc() != null ? sdf.format(p.getThoiGianKetThuc()) : LABEL_CHUA_KET_THUC,
                    hienTrangThaiPhien(p),
                    p.getTrangThaiThanhToan() != null ? p.getTrangThaiThanhToan() : "Chưa thanh toán",
                    p.getMaDatCho() != null ? "Đặt trước" : "Trực tiếp"
            });
        }
    }

    public void hienThiDichVuTrongPhien(
            java.util.List<com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO> list) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblDichVu.getModel();
        model.setRowCount(0);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,### VNĐ",
                java.text.DecimalFormatSymbols.getInstance(new java.util.Locale("vi", "VN")));
        for (com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO dv : list) {
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
        txtTrangThai = new javax.swing.JTextField();
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
        lblTrangThai1 = new javax.swing.JLabel();
        txtHinhThuc = new javax.swing.JTextField();
        lblDuKien1 = new javax.swing.JLabel();
        txtThoiGianDaDung = new javax.swing.JTextField();
        lblDuKien2 = new javax.swing.JLabel();
        txtTrangThaiThanhToan = new javax.swing.JTextField();

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
        btnTimKiem.setBounds(410, 55, 80, 35);

        btnTaiLai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTaiLai.setForeground(new java.awt.Color(235, 94, 141));
        btnTaiLai.setText("Làm mới");
        btnTaiLai.addActionListener(this::btnTaiLaiActionPerformed);
        pnLeft.add(btnTaiLai);
        btnTaiLai.setBounds(500, 55, 95, 35);

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
        cbxChiNhanh.setBounds(410, 10, 180, 28);

        pnMain.add(pnLeft);
        pnLeft.setBounds(430, 70, 620, 550);

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

        txtTrangThai.setEditable(false);
        txtTrangThai.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtTrangThai.setForeground(new java.awt.Color(255, 51, 51));
        txtTrangThai.setBorder(null);
        txtTrangThai.addActionListener(this::txtTrangThaiActionPerformed);
        pnRight.add(txtTrangThai);
        txtTrangThai.setBounds(210, 180, 160, 30);

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
        txtKetThuc.setText("Chưa kết thúc");
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
        jScrollPane2.setBounds(20, 360, 350, 100);

        btnMoPhien.setBackground(new java.awt.Color(35, 30, 48));
        btnMoPhien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnMoPhien.setForeground(new java.awt.Color(255, 255, 255));
        btnMoPhien.setText("Mở Phiên Mới");
        btnMoPhien.addActionListener(this::btnMoPhienActionPerformed);
        pnRight.add(btnMoPhien);
        btnMoPhien.setBounds(200, 480, 170, 35);

        btnKetThucPhien.setBackground(new java.awt.Color(220, 53, 69));
        btnKetThucPhien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnKetThucPhien.setForeground(new java.awt.Color(255, 255, 255));
        btnKetThucPhien.setText("Kết Thúc Phiên");
        btnKetThucPhien.addActionListener(this::btnKetThucPhienActionPerformed);
        pnRight.add(btnKetThucPhien);
        btnKetThucPhien.setBounds(20, 480, 170, 35);

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

        txtTrangThaiThanhToan.setEditable(false);
        txtTrangThaiThanhToan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtTrangThaiThanhToan.setForeground(new java.awt.Color(255, 51, 51));
        txtTrangThaiThanhToan.setBorder(null);
        txtTrangThaiThanhToan.addActionListener(this::txtTrangThaiThanhToanActionPerformed);
        pnRight.add(txtTrangThaiThanhToan);
        txtTrangThaiThanhToan.setBounds(210, 130, 160, 30);

        pnMain.add(pnRight);
        pnRight.setBounds(20, 70, 380, 550);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtTrangThaiThanhToanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTrangThaiThanhToanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTrangThaiThanhToanActionPerformed

    private void txtTrangThaiActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void cbxChiNhanhActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }// GEN-LAST:event_cbxChiNhanhActionPerformed

    private void txtDuKienActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }// GEN-LAST:event_txtDuKienActionPerformed

    private void txtThoiGianDaDungActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }// GEN-LAST:event_txtThoiGianDaDungActionPerformed

    private void txtHinhThucActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }


    private boolean laDaKetThuc(com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO phien) {
        if (phien == null) {
            return false;
        }
        return phien.getThoiGianKetThuc() != null
                || chuanHoaTrangThai(phien.getTrangThaiPhien()).contains("da ket thuc");
    }

    private boolean laDaThanhToan(com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO phien) {
        if (phien == null) {
            return false;
        }
        String trangThaiThanhToan = chuanHoaTrangThai(phien.getTrangThaiThanhToan());
        return trangThaiThanhToan.contains("thanh toan thanh cong")
                || trangThaiThanhToan.equals("da thanh toan");
    }

    private com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO timPhienTheoMa(String maPhien) {
        if (maPhien == null || currentList == null) {
            return null;
        }
        for (com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO phien : currentList) {
            if (maPhien.equals(phien.getMaPhien())) {
                return phien;
            }
        }
        return null;
    }

    private void khoaThaoTacPhien(boolean khoa) {
        btnKetThucPhien.setEnabled(!khoa);
    }

    private String chuanHoaTrangThai(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String hienTrangThai(String value) {
        return value == null || value.isBlank() ? "Chưa có" : value;
    }

    private String hienTrangThaiPhien(com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO phien) {
        if (phien == null) {
            return "Chưa có";
        }
        return laDaKetThuc(phien) ? LABEL_DA_KET_THUC : hienTrangThai(phien.getTrangThaiPhien());
    }

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        loadData(txtTimKiem.getText().trim());
    }

    private void btnTaiLaiActionPerformed(java.awt.event.ActionEvent evt) {
        txtTimKiem.setText("");
        loadData("");
    }

    // removed btnCapNhatActionPerformed
    private void btnHuyActionPerformed() {
        txtMaPhien.setText("");
        txtKhongGian.setText("");
        txtKhachHang.setText("");
        txtKhachHang.setEditable(false);
        txtKhachHang.setBackground(new java.awt.Color(240, 240, 240));
        btnKetThucPhien.setEnabled(false);
        txtTrangThai.setText("");
        if (txtTrangThaiThanhToan != null) txtTrangThaiThanhToan.setText("");
        if (txtHinhThuc != null) txtHinhThuc.setText("");
        txtBatDau.setText("");
        txtDuKien.setText("");
        txtKetThuc.setText(LABEL_CHUA_KET_THUC);
        if (txtThoiGianDaDung != null) txtThoiGianDaDung.setText("");
        ((javax.swing.table.DefaultTableModel) tblDichVu.getModel()).setRowCount(0);
        tblPhienLamViec.clearSelection();
    }

    private void btnKetThucPhienActionPerformed(java.awt.event.ActionEvent evt) {
        String maPhien = txtMaPhien.getText().trim();
        if (maPhien.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiên đang chạy!");
            return;
        }

        com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO phien = timPhienTheoMa(maPhien);
        if (laDaKetThuc(phien)) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Phiên đã kết thúc, không thể kết thúc lại.");
            khoaThaoTacPhien(true);
            return;
        }

        int xacNhan = javax.swing.JOptionPane.showConfirmDialog(this, "Xác nhận kết thúc phiên " + maPhien + "?",
                "Xác nhận", javax.swing.JOptionPane.YES_NO_OPTION);
        if (xacNhan == javax.swing.JOptionPane.YES_OPTION) {
            btnKetThucPhien.setEnabled(false);
            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return controller.ketThucPhien(maPhien);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            javax.swing.JOptionPane.showMessageDialog(QuanLyPhienForm.this, "Đã kết thúc phiên thành công!");
                            if (!chuyenSangHoaDonTheoPhien(maPhien)) {
                                loadData("");
                                btnHuyActionPerformed();
                            }
                        } else {
                            javax.swing.JOptionPane.showMessageDialog(QuanLyPhienForm.this, "Lỗi khi kết thúc phiên!");
                            btnKetThucPhien.setEnabled(true);
                        }
                    } catch (java.util.concurrent.ExecutionException exWrapper) {
                        Throwable cause = exWrapper.getCause();
                        String msg = cause != null ? cause.getMessage() : exWrapper.getMessage();
                        if (msg != null && (msg.contains("không ở trạng thái") || msg.contains("khong o trang thai"))) {
                            javax.swing.JOptionPane.showMessageDialog(QuanLyPhienForm.this,
                                    "Phiên " + maPhien + " đã được kết thúc trước đó.\nDanh sách sẽ được tải lại.",
                                    "Phiên đã kết thúc", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                            loadData("");
                            btnHuyActionPerformed();
                        } else {
                            // Hiển thị message thực từ SP/exception, không dùng chuỗi cứng
                            String userMsg = (msg != null && !msg.isBlank()) ? msg : "Không thể kết thúc phiên, vui lòng thử lại.";
                            javax.swing.JOptionPane.showMessageDialog(QuanLyPhienForm.this,
                                    userMsg, "Lỗi kết thúc phiên", javax.swing.JOptionPane.ERROR_MESSAGE);
                            btnKetThucPhien.setEnabled(true);
                        }
                    } catch (Exception ex) {
                        String msg = ex.getMessage();
                        String userMsg = (msg != null && !msg.isBlank()) ? msg : "Không thể kết thúc phiên, vui lòng thử lại.";
                        javax.swing.JOptionPane.showMessageDialog(QuanLyPhienForm.this,
                                userMsg, "Lỗi kết thúc phiên", javax.swing.JOptionPane.ERROR_MESSAGE);
                        btnKetThucPhien.setEnabled(true);
                    }
                }
            }.execute();
        }
    }

    private void btnMoPhienActionPerformed(java.awt.event.ActionEvent evt) {
        if (thieuChiNhanhHoatDong) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Tài khoản của bạn chưa được gán chi nhánh hoạt động, vui lòng liên hệ quản trị viên.",
                    "Thiếu chi nhánh hoạt động", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
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

    private boolean chuyenSangHoaDonTheoPhien(String maPhien) {
        try {
            java.awt.Window parent = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (parent instanceof com.wms.view.TrangChuQuanLy.TrangChuQuanLyForm mainFrame) {
                mainFrame.moHoaDonTheoPhien(maPhien);
                return true;
            }
        } catch (Exception ex) {
            System.err.println("[QuanLyPhienForm] Không thể mở hóa đơn theo phiên: " + ex.getMessage());
        }
        return false;
    }

    private void tblPhienLamViecMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tblPhienLamViec.getSelectedRow();
        if (row >= 0) {
            int modelRow = tblPhienLamViec.convertRowIndexToModel(row);
            if (currentList == null || modelRow < 0 || modelRow >= currentList.size()) {
                return;
            }
            com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO selected = currentList.get(modelRow);
            String maPhien = selected.getMaPhien();
            txtMaPhien.setText(maPhien);
            txtKhongGian.setText(tblPhienLamViec.getValueAt(row, 1).toString());
            txtKhachHang.setText(tblPhienLamViec.getValueAt(row, 2).toString());
            txtBatDau.setText(tblPhienLamViec.getValueAt(row, 3).toString());
            txtBatDau.setCaretPosition(0);

            txtKetThuc.setText(selected.getThoiGianKetThuc() != null
                    ? sdf.format(selected.getThoiGianKetThuc())
                    : LABEL_CHUA_KET_THUC);
            txtKetThuc.setCaretPosition(0);

            String trangThaiTable = hienTrangThaiPhien(selected);
            String hinhThuc = tblPhienLamViec.getValueAt(row, 7).toString();

            boolean khoaThaoTac = laDaKetThuc(selected);
            if (khoaThaoTac) {
                khoaThaoTacPhien(true);
                txtKhachHang.setEditable(false);
                txtKhachHang.setBackground(new java.awt.Color(240, 240, 240));
            } else {
                khoaThaoTacPhien(false);

                // Logic sửa tên khách hàng cho phiên trực tiếp
                if (selected.getMaDatCho() == null) {
                    txtKhachHang.setEditable(true);
                    txtKhachHang.setBackground(new java.awt.Color(255, 255, 255));
                } else {
                    txtKhachHang.setEditable(false);
                    txtKhachHang.setBackground(new java.awt.Color(240, 240, 240));
                }
            }

            txtTrangThai.setText(trangThaiTable);
            if (txtTrangThaiThanhToan != null) {
                txtTrangThaiThanhToan.setText(tblPhienLamViec.getValueAt(row, 6).toString());
            }
            if (txtHinhThuc != null) {
                txtHinhThuc.setText(hinhThuc);
            }

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

            loadChiTietDichVuAsync(maPhien);
        }
    }

    private void loadChiTietDichVuAsync(String maPhien) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblDichVu.getModel();
        model.setRowCount(0);
        model.addRow(new Object[]{"Đang tải...", "", "", ""});
        new javax.swing.SwingWorker<java.util.List<com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO>, Void>() {
            @Override
            protected java.util.List<com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO> doInBackground() {
                return controller.layDichVuCuaPhien(maPhien);
            }

            @Override
            protected void done() {
                try {
                    hienThiDichVuTrongPhien(get());
                } catch (Exception ex) {
                    com.wms.util.MessageUtil.showError(QuanLyPhienForm.this, "Lỗi tải dịch vụ trong phiên.", ex);
                    model.setRowCount(0);
                }
            }
        }.execute();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnKetThucPhien;
    private javax.swing.JButton btnMoPhien;
    private javax.swing.JButton btnTaiLai;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JComboBox<String> cbxChiNhanh;
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
    private javax.swing.JTextField txtTrangThai;
    private javax.swing.JTextField txtTrangThaiThanhToan;
    // End of variables declaration//GEN-END:variables
}
