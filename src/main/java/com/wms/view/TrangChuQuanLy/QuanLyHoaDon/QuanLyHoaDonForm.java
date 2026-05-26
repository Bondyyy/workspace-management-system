package com.wms.view.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.controller.TrangChuQuanLy.QuanLyHoaDon.HoaDonController;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.HoaDonDTO;
import com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.ThanhToanHoaDonForm;

import java.awt.Desktop;
import java.awt.Window;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class QuanLyHoaDonForm extends javax.swing.JPanel {

    private static final long MILLIS_PER_HOUR = 3_600_000L;
    private static final long MILLIS_PER_MIN  = 60_000L;
    private static final long MILLIS_PER_SEC  = 1_000L;

    private final HoaDonController hoaDonController = new HoaDonController();
    private DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#,### VNĐ",
            java.text.DecimalFormatSymbols.getInstance(new java.util.Locale("vi", "VN")));
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private HoaDonDTO currentHD;
    private List<HoaDonDTO> dsHoaDon = Collections.emptyList(); // cache, tránh gọi DB 2 lần
    private javax.swing.Timer realTimeTimer;
    private javax.swing.SwingWorker<List<HoaDonDTO>, Void> loadHoaDonWorker;
    private boolean dangExportHoaDon = false;

    public QuanLyHoaDonForm() {
        this(null);
    }

    public QuanLyHoaDonForm(String maPhienCanChon) {
        initComponents();
        tableModel = (DefaultTableModel) tblHoaDon.getModel();
        setupHoaDonTable();
        if (maPhienCanChon == null || maPhienCanChon.isBlank()) {
            loadDataToTable();
        } else {
            loadDataAndSelectByMaPhien(maPhienCanChon);
        }
        initRealTimeTimer();
    }

    private void setupHoaDonTable() {
        int[] widths = {80, 90, 120, 150, 105, 145, 110};
        for (int i = 0; i < widths.length && i < tblHoaDon.getColumnModel().getColumnCount(); i++) {
            tblHoaDon.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        tblHoaDon.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        javax.swing.table.DefaultTableCellRenderer tooltipRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (component instanceof javax.swing.JComponent jComponent) {
                    jComponent.setToolTipText(value == null ? "" : value.toString());
                }
                return component;
            }
        };
        for (int i = 0; i < tblHoaDon.getColumnModel().getColumnCount(); i++) {
            tblHoaDon.getColumnModel().getColumn(i).setCellRenderer(tooltipRenderer);
        }
    }

    private void initRealTimeTimer() {
        realTimeTimer = new javax.swing.Timer(1000, e -> updateRealTimeDuration());
        realTimeTimer.start();
    }

    private void updateRealTimeDuration() {
        if (currentHD == null || txtThoiGianDaDung == null) return;
        boolean isRunning = !"Đã kết thúc".equals(currentHD.getTrangThaiPhien())
                         && !"Đã thanh toán".equals(currentHD.getTrangThaiThanhToan());
        if (currentHD.getThoiGianBatDauPhien() != null && isRunning) {
            long diff = System.currentTimeMillis() - currentHD.getThoiGianBatDauPhien().getTime();
            txtThoiGianDaDung.setText(diff > 0
                    ? String.format("%02d:%02d:%02d",
                            diff / MILLIS_PER_HOUR,
                            (diff % MILLIS_PER_HOUR) / MILLIS_PER_MIN,
                            (diff % MILLIS_PER_MIN) / MILLIS_PER_SEC)
                    : "00:00:00");
        }
    }

    private void loadDataToTable() {
        loadDataAsync(null);
    }

    private void loadDataAsync(String maHoaDonCanChon) {
        loadDataAsync(maHoaDonCanChon, null);
    }

    private void loadDataAsync(String maHoaDonCanChon, String maPhienCanChon) {
        if (loadHoaDonWorker != null && !loadHoaDonWorker.isDone()) {
            loadHoaDonWorker.cancel(true);
        }

        String keyword = txtTimKiem.getText();
        String status = cbxLocTrangThai.getSelectedItem().toString();
        setLoadingTable(true);
        long start = System.currentTimeMillis();

        loadHoaDonWorker = new javax.swing.SwingWorker<>() {
            @Override
            protected List<HoaDonDTO> doInBackground() {
                return hoaDonController.layDanhSachHoaDon(keyword, status);
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        return;
                    }
                    dsHoaDon = get();
                    renderHoaDonTable();
                    if (maPhienCanChon != null) {
                        chonHoaDonTheoMaPhienSauKhiTai(maPhienCanChon);
                    } else if (maHoaDonCanChon != null) {
                        chonHoaDonSauKhiTai(maHoaDonCanChon);
                    }
                    System.out.println("[QuanLyHoaDonForm] load bang hoa don mat "
                            + (System.currentTimeMillis() - start) + " ms");
                } catch (Exception ex) {
                    dsHoaDon = Collections.emptyList();
                    tableModel.setRowCount(0);
                    com.wms.util.MessageUtil.showError(QuanLyHoaDonForm.this, "Lỗi tải danh sách hóa đơn.", ex);
                } finally {
                    setLoadingTable(false);
                }
            }
        };
        loadHoaDonWorker.execute();
    }

    private void renderHoaDonTable() {
        tableModel.setRowCount(0);
        for (HoaDonDTO hd : dsHoaDon) {
            tableModel.addRow(new Object[] {
                    hd.getMaHoaDon(),
                    hd.getMaPhien() != null ? hd.getMaPhien() : "",
                    hd.getNgayLapHoaDon() != null ? sdf.format(hd.getNgayLapHoaDon()) : "N/A",
                    hd.getHoTenKH(),
                    df.format(giaTriTien(hd.getTongTien())),
                    hienThiTrangThaiThanhToan(hd),
                    hienThiHinhThuc(hd)
            });
        }
    }

    private void chonHoaDonSauKhiTai(String maHD) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (maHD.equals(tableModel.getValueAt(i, 0))) {
                tblHoaDon.setRowSelectionInterval(i, i);
                showChiTietHoaDon(maHD);
                return;
            }
        }
        resetForm();
    }

    private void chonHoaDonTheoMaPhienSauKhiTai(String maPhien) {
        if (maPhien == null || maPhien.isBlank()) {
            resetForm();
            return;
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object value = tableModel.getValueAt(i, 1);
            if (value != null && maPhien.trim().equalsIgnoreCase(value.toString())) {
                tblHoaDon.setRowSelectionInterval(i, i);
                Object maHD = tableModel.getValueAt(i, 0);
                showChiTietHoaDon(maHD == null ? "" : maHD.toString());
                return;
            }
        }
        resetForm();
    }

    public void loadDataAndSelectByMaPhien(String maPhien) {
        String value = maPhien == null ? "" : maPhien.trim();
        txtTimKiem.setText(value);
        cbxLocTrangThai.setSelectedIndex(0);
        loadDataAsync(null, value);
    }

    private void setLoadingTable(boolean loading) {
        btnTimKiem.setEnabled(!loading);
        btnLamMoi.setEnabled(!loading);
        cbxLocTrangThai.setEnabled(!loading);
        txtTimKiem.setEnabled(!loading);
        tblHoaDon.setEnabled(!loading);
        if (loading) {
            tableModel.setRowCount(0);
            tableModel.addRow(new Object[]{"Đang tải...", "", "", "", "", "", ""});
        }
    }

    private void showChiTietHoaDon(String maHD) {
        int row = tblHoaDon.getSelectedRow();
        if (row == -1) return;

        txtMaHD.setText(maHD);
        
        for (HoaDonDTO hd : dsHoaDon) {
            if (!hd.getMaHoaDon().equals(maHD)) continue;
            this.currentHD = hd;
            txtKhachHang.setText(hd.getHoTenKH() != null ? hd.getHoTenKH() : "");
            
            txtNgayTao.setText(sdf.format(hd.getNgayLapHoaDon()));
            txtNgayTao.setCaretPosition(0);

            txtThanhToan.setText(hd.getPhuongThucThanhToan() != null ? hd.getPhuongThucThanhToan() : "Chưa chọn");
            
            // Xử lý tiền trả trước và hiển thị
            double soTienDaTraTruoc = hd.getSoTienDaTraTruoc();
            double conPhaiThuValue = Math.max(0, giaTriTien(hd.getThanhTien()));
            txtTruocGiamGia.setText(df.format(conPhaiThuValue));
            txtTruocGiamGia.setToolTipText("Tổng cộng: " + df.format(giaTriTien(hd.getTongTien()))
                    + " | Đã trả trước: " + df.format(soTienDaTraTruoc)
                    + " | Còn phải thu: " + df.format(conPhaiThuValue));

            txtTrangThai.setText(hienThiTrangThaiThanhToan(hd));
            if (txtHinhThuc != null) {
                txtHinhThuc.setText(hienThiHinhThuc(hd));
                txtHinhThuc.setToolTipText(hd.isDaTraTruoc()
                        ? "Đặt trước (Đã trả trước)"
                        : hienThiHinhThuc(hd));
            }
            if (txtTrangThaiPhien != null) txtTrangThaiPhien.setText(hd.getTrangThaiPhien() != null ? hd.getTrangThaiPhien() : "N/A");
            if (txtThoiGianBatDau != null) {
                txtThoiGianBatDau.setText(hd.getThoiGianBatDauPhien() != null ? sdf.format(hd.getThoiGianBatDauPhien()) : "N/A");
                txtThoiGianBatDau.setCaretPosition(0);
            }

            if (txtThoiGianDaDung != null) {
                if (hd.getThoiGianBatDauPhien() != null) {
                    long start = hd.getThoiGianBatDauPhien().getTime();
                    long end = hd.getThoiGianKetThucPhien() != null
                            ? hd.getThoiGianKetThucPhien().getTime() : System.currentTimeMillis();
                    long diff = end - start;
                    txtThoiGianDaDung.setText(diff > 0
                            ? String.format("%02d:%02d:%02d",
                                    diff / MILLIS_PER_HOUR,
                                    (diff % MILLIS_PER_HOUR) / MILLIS_PER_MIN,
                                    (diff % MILLIS_PER_MIN) / MILLIS_PER_SEC)
                            : "00:00:00");

                    // Logic làm tròn: < 15p thì giữ nguyên, >= 15p thì làm tròn lên 1 tiếng
                    long totalMinutes = diff / MILLIS_PER_MIN;
                    long hours = totalMinutes / 60;
                    long minutes = totalMinutes % 60;
                    long roundedHours = (minutes < 15) ? hours : hours + 1;
                    txtThoiGianLamTron.setText(roundedHours + " giờ");
                } else {
                    txtThoiGianDaDung.setText("00:00:00");
                    txtThoiGianLamTron.setText("0 giờ");
                }
            }

            if (txtThoiGianKetThuc != null) {
                txtThoiGianKetThuc.setText(hd.getThoiGianKetThucPhien() != null ? sdf.format(hd.getThoiGianKetThucPhien()) : "N/A");
                txtThoiGianKetThuc.setCaretPosition(0);
            }

            boolean trangThaiHopLeDeThanhToan = "Đang chờ thanh toán".equals(hd.getTrangThaiThanhToan())
                                || "Đã trả trước".equals(hd.getTrangThaiThanhToan())
                                || "Đang chờ thanh toán phụ thu".equals(hienThiTrangThaiThanhToan(hd));
            boolean phienDaKetThuc = !"Đang hoạt động".equals(hd.getTrangThaiPhien());
            String trangThaiThanhToan = hd.getTrangThaiThanhToan() == null ? "" : hd.getTrangThaiThanhToan();
            boolean conPhaiThu = giaTriTien(hd.getThanhTien()) > 0;
            boolean coTheHoanTatTraTruoc = hd.getSoTienDaTraTruoc() > 0
                    && giaTriTien(hd.getThanhTien()) <= 0;

            btnXacNhan.setEnabled(trangThaiHopLeDeThanhToan && phienDaKetThuc && (conPhaiThu || coTheHoanTatTraTruoc)
                    && !"Đã thanh toán thành công".equals(trangThaiThanhToan));
            if (coTheHoanTatTraTruoc && "Đã trả trước".equals(trangThaiThanhToan)) {
                btnXacNhan.setText("Hoàn tất hóa đơn");
            } else if ("Đã trả trước".equals(trangThaiThanhToan)) {
                btnXacNhan.setText("Thanh toán phụ thu");
            } else if (trangThaiThanhToan.equals("Đang chờ thanh toán phụ thu")
                || (trangThaiThanhToan.equals("Đang chờ thanh toán") && hd.getSoTienDaTraTruoc() > 0)) {
                btnXacNhan.setText("Thanh toán phụ thu");
            } else {
                btnXacNhan.setText("Xác nhận Thanh toán");
            }
            btnHuy.setEnabled("Đang chờ thanh toán".equals(trangThaiThanhToan)
                    || "Đang chờ thanh toán phụ thu".equals(trangThaiThanhToan));
            break;
        }
    }

    private String hienThiHinhThuc(HoaDonDTO hd) {
        if (hd == null) {
            return "";
        }
        if (hd.isDaTraTruoc()) {
            return "Đặt trước - đã trả";
        }
        return hd.getMaDatCho() != null ? "Đặt trước" : "Trực tiếp";
    }

    private String hienThiTrangThaiThanhToan(HoaDonDTO hd) {
        if (hd == null) {
            return "";
        }
        if (hd.getSoTienDaTraTruoc() > 0
                && hd.getThanhTien() != null
                && hd.getThanhTien() > 0
                && !"Đã thanh toán thành công".equals(hd.getTrangThaiThanhToan())) {
            return "Đang chờ thanh toán phụ thu";
        }
        return hd.getTrangThaiThanhToan() == null ? "Chưa thanh toán" : hd.getTrangThaiThanhToan();
    }

    private double giaTriTien(Double value) {
        return value == null ? 0 : value;
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
        txtTimKiem = new javax.swing.JTextField();
        cbxLocTrangThai = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHoaDon = new javax.swing.JTable();
        btnTimKiem = new javax.swing.JButton();
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
        btnXemHoaDon = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        txtHinhThuc = new javax.swing.JTextField();
        lblTrangThai1 = new javax.swing.JLabel();
        txtTrangThaiPhien = new javax.swing.JTextField();
        lblThanhToan1 = new javax.swing.JLabel();
        txtThoiGianDaDung = new javax.swing.JTextField();
        lblNgayTao1 = new javax.swing.JLabel();
        txtThoiGianBatDau = new javax.swing.JTextField();
        lblNgayTao2 = new javax.swing.JLabel();
        txtThoiGianKetThuc = new javax.swing.JTextField();
        lblNgayTao3 = new javax.swing.JLabel();
        lblNgayTao4 = new javax.swing.JLabel();
        txtThoiGianLamTron = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();

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
        txtTimKiem.setToolTipText("Tìm theo mã hóa đơn, mã phiên làm việc hoặc khách hàng...");
        pnLeft.add(txtTimKiem);
        txtTimKiem.setBounds(20, 55, 280, 35);

        cbxLocTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cbxLocTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả", "Chưa thanh toán", "Đã trả trước", "Đang chờ thanh toán phụ thu", "Đã thanh toán", "Đã hủy" }));
        pnLeft.add(cbxLocTrangThai);
        cbxLocTrangThai.setBounds(400, 55, 180, 35);

        tblHoaDon.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã HĐ", "Mã PLV", "Ngày tạo", "Khách hàng", "Tổng tiền", "Trạng thái", "Hình thức"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
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

        pnMain.add(pnLeft);
        pnLeft.setBounds(440, 70, 600, 540);

        pnRight.setBackground(new java.awt.Color(255, 255, 255));
        pnRight.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRight.setLayout(null);

        lblDetailTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDetailTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblDetailTitle.setText("CHI TIẾT HÓA ĐƠN");
        pnRight.add(lblDetailTitle);
        lblDetailTitle.setBounds(10, 20, 200, 30);

        lblMaHD.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMaHD.setForeground(new java.awt.Color(136, 136, 136));
        lblMaHD.setText("Mã hóa đơn");
        pnRight.add(lblMaHD);
        lblMaHD.setBounds(10, 60, 100, 18);

        txtMaHD.setEditable(false);
        txtMaHD.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        txtMaHD.setBorder(null);
        pnRight.add(txtMaHD);
        txtMaHD.setBounds(10, 80, 180, 30);

        lblKhachHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKhachHang.setForeground(new java.awt.Color(136, 136, 136));
        lblKhachHang.setText("Khách hàng");
        pnRight.add(lblKhachHang);
        lblKhachHang.setBounds(230, 60, 100, 18);

        txtKhachHang.setEditable(false);
        txtKhachHang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtKhachHang.setBorder(null);
        pnRight.add(txtKhachHang);
        txtKhachHang.setBounds(230, 80, 180, 30);

        lblNgayTao.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayTao.setForeground(new java.awt.Color(136, 136, 136));
        lblNgayTao.setText("Thời gian tạo");
        pnRight.add(lblNgayTao);
        lblNgayTao.setBounds(10, 180, 100, 18);

        txtNgayTao.setEditable(false);
        txtNgayTao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtNgayTao.setBorder(null);
        pnRight.add(txtNgayTao);
        txtNgayTao.setBounds(10, 200, 180, 30);

        lblThanhToan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblThanhToan.setForeground(new java.awt.Color(136, 136, 136));
        lblThanhToan.setText("Phương thức thanh toán");
        pnRight.add(lblThanhToan);
        lblThanhToan.setBounds(230, 180, 150, 18);

        txtThanhToan.setEditable(false);
        txtThanhToan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtThanhToan.setBorder(null);
        pnRight.add(txtThanhToan);
        txtThanhToan.setBounds(230, 200, 180, 30);

        lblTongTien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTongTien.setForeground(new java.awt.Color(136, 136, 136));
        lblTongTien.setText("Còn phải thu");
        pnRight.add(lblTongTien);
        lblTongTien.setBounds(10, 300, 200, 18);

        txtTruocGiamGia.setEditable(false);
        txtTruocGiamGia.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtTruocGiamGia.setForeground(new java.awt.Color(235, 94, 141));
        txtTruocGiamGia.setBorder(null);
        pnRight.add(txtTruocGiamGia);
        txtTruocGiamGia.setBounds(10, 320, 180, 40);

        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTrangThai.setForeground(new java.awt.Color(136, 136, 136));
        lblTrangThai.setText("Trạng thái thanh toán");
        pnRight.add(lblTrangThai);
        lblTrangThai.setBounds(10, 370, 140, 18);

        txtTrangThai.setEditable(false);
        txtTrangThai.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        txtTrangThai.setBorder(null);
        pnRight.add(txtTrangThai);
        txtTrangThai.setBounds(10, 390, 180, 40);

        btnXacNhan.setBackground(new java.awt.Color(0, 153, 51));
        btnXacNhan.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnXacNhan.setForeground(new java.awt.Color(255, 255, 255));
        btnXacNhan.setText("Xác nhận Thanh toán");
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(this::btnXacNhanActionPerformed);
        pnRight.add(btnXacNhan);
        btnXacNhan.setBounds(10, 440, 200, 40);

        btnXemHoaDon.setBackground(new java.awt.Color(235, 94, 141));
        btnXemHoaDon.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXemHoaDon.setForeground(new java.awt.Color(255, 255, 255));
        btnXemHoaDon.setText("Xem hóa đơn");
        btnXemHoaDon.addActionListener(this::btnXemHoaDonActionPerformed);
        pnRight.add(btnXemHoaDon);
        btnXemHoaDon.setBounds(220, 490, 190, 40);

        btnHuy.setBackground(new java.awt.Color(235, 94, 141));
        btnHuy.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnHuy.setForeground(new java.awt.Color(255, 255, 255));
        btnHuy.setText("Huỷ hóa đơn");
        btnHuy.addActionListener(this::btnHuyActionPerformed);
        pnRight.add(btnHuy);
        btnHuy.setBounds(10, 490, 200, 40);

        txtHinhThuc.setEditable(false);
        txtHinhThuc.setFont(new java.awt.Font("Segoe UI", 3, 13)); // NOI18N
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
        txtTrangThaiPhien.setBounds(230, 320, 180, 40);

        lblThanhToan1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblThanhToan1.setForeground(new java.awt.Color(136, 136, 136));
        lblThanhToan1.setText("Trạng thái phiên");
        pnRight.add(lblThanhToan1);
        lblThanhToan1.setBounds(230, 300, 150, 18);

        txtThoiGianDaDung.setEditable(false);
        txtThoiGianDaDung.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtThoiGianDaDung.setBorder(null);
        pnRight.add(txtThoiGianDaDung);
        txtThoiGianDaDung.setBounds(10, 260, 180, 30);

        lblNgayTao1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayTao1.setForeground(new java.awt.Color(136, 136, 136));
        lblNgayTao1.setText("Thời gian đã dùng");
        pnRight.add(lblNgayTao1);
        lblNgayTao1.setBounds(10, 240, 140, 18);

        txtThoiGianBatDau.setEditable(false);
        txtThoiGianBatDau.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtThoiGianBatDau.setBorder(null);
        txtThoiGianBatDau.addActionListener(this::txtThoiGianBatDauActionPerformed);
        pnRight.add(txtThoiGianBatDau);
        txtThoiGianBatDau.setBounds(10, 140, 180, 30);

        lblNgayTao2.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayTao2.setForeground(new java.awt.Color(136, 136, 136));
        lblNgayTao2.setText("Thời gian bắt đầu phiên");
        pnRight.add(lblNgayTao2);
        lblNgayTao2.setBounds(10, 120, 160, 18);

        txtThoiGianKetThuc.setEditable(false);
        txtThoiGianKetThuc.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtThoiGianKetThuc.setBorder(null);
        txtThoiGianKetThuc.addActionListener(this::txtThoiGianKetThucActionPerformed);
        pnRight.add(txtThoiGianKetThuc);
        txtThoiGianKetThuc.setBounds(230, 140, 180, 30);

        lblNgayTao3.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayTao3.setForeground(new java.awt.Color(136, 136, 136));
        lblNgayTao3.setText("Thời gian kết thúc phiên");
        pnRight.add(lblNgayTao3);
        lblNgayTao3.setBounds(230, 120, 160, 18);

        lblNgayTao4.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayTao4.setForeground(new java.awt.Color(136, 136, 136));
        lblNgayTao4.setText("Thời gian làm tròn");
        pnRight.add(lblNgayTao4);
        lblNgayTao4.setBounds(230, 240, 140, 18);

        txtThoiGianLamTron.setEditable(false);
        txtThoiGianLamTron.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtThoiGianLamTron.setBorder(null);
        pnRight.add(txtThoiGianLamTron);
        txtThoiGianLamTron.setBounds(230, 260, 180, 30);

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(235, 94, 141));
        btnLamMoi.setText("Làm mới");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
        pnRight.add(btnLamMoi);
        btnLamMoi.setBounds(220, 440, 190, 40);

        pnMain.add(pnRight);
        pnRight.setBounds(10, 70, 420, 540);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtHinhThucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtHinhThucActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtHinhThucActionPerformed

    private void txtThoiGianBatDauActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtThoiGianBatDauActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtThoiGianBatDauActionPerformed

    private void txtThoiGianKetThucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtThoiGianKetThucActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtThoiGianKetThucActionPerformed

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {
        String maHD = txtMaHD.getText();
        if (maHD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần hủy!");
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn hủy hóa đơn này?", "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION);
        if (xacNhan == JOptionPane.YES_OPTION) {
            btnHuy.setEnabled(false);
            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return hoaDonController.huyHoaDon(maHD);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(QuanLyHoaDonForm.this, "Đã hủy hóa đơn thành công!");
                            loadDataToTable();
                            resetForm();
                        } else {
                            JOptionPane.showMessageDialog(QuanLyHoaDonForm.this, "Hủy hóa đơn thất bại!");
                            btnHuy.setEnabled(true);
                        }
                    } catch (Exception ex) {
                        com.wms.util.MessageUtil.showError(QuanLyHoaDonForm.this, "Lỗi hủy hóa đơn.", ex);
                        btnHuy.setEnabled(true);
                    }
                }
            }.execute();
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
            int modelRow = tblHoaDon.convertRowIndexToModel(row);
            String maHD = tableModel.getValueAt(modelRow, 0).toString();
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
        loadDataAsync(maHD);
    }

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {
        String maHD = txtMaHD.getText();
        if (maHD.isEmpty())
            return;

        int xacNhan = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn XÓA VĨNH VIỄN hóa đơn này?",
                "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan == JOptionPane.YES_OPTION) {
            if (hoaDonController.xoaHoaDon(maHD)) {
                JOptionPane.showMessageDialog(this, "Đã xóa hóa đơn!");
                loadDataToTable();
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại (Hóa đơn có thể đang được tham chiếu hoặc phiên làm việc đang hoạt động)!");
            }
        }
    }

    private void btnXemHoaDonActionPerformed(java.awt.event.ActionEvent evt) {
        String maHD = txtMaHD.getText();
        if (maHD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xem!");
            return;
        }

        btnXemHoaDon.setEnabled(false);
        long start = System.currentTimeMillis();
        new javax.swing.SwingWorker<com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO, Void>() {
            @Override
            protected com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO doInBackground() {
                return hoaDonController.layChiTietHoaDon(maHD);
            }

            @Override
            protected void done() {
                btnXemHoaDon.setEnabled(true);
                try {
                    com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO tt = get();
                    System.out.println("[QuanLyHoaDonForm] load preview hoa don mat "
                            + (System.currentTimeMillis() - start) + " ms");
                    hienThiPreviewHoaDon(tt);
                } catch (Exception ex) {
                    com.wms.util.MessageUtil.showError(QuanLyHoaDonForm.this, "Lỗi xem hóa đơn.", ex);
                }
            }
        }.execute();
    }

    private void hienThiPreviewHoaDon(com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO tt) {
        if (tt == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin chi tiết!");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("      --- HÓA ĐƠN CHI TIẾT ---\n\n");
        sb.append("Mã HĐ: ").append(tt.getMaHoaDon()).append("\n");
        sb.append("Khách hàng: ").append(tt.getHoTenKH()).append("\n");
        sb.append("Không gian: ").append(tt.getTenKhongGian()).append("\n");
        sb.append("Thời gian: ").append(tt.getThoiGianSửDung()).append("\n");
        sb.append("Số giờ tính: ").append(tt.getTongSoGio()).append(" giờ\n");
        sb.append("--------------------------------\n");
        sb.append(String.format("%-28s %6s %18s %18s\n", "Dịch vụ/Không gian", "SL", "Đơn giá", "Thành tiền"));
        for (com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO dv : tt.getDanhSachDichVu()) {
            sb.append(String.format("%-28s %6d %18s %18s\n",
                    dv.getTenDichVu(),
                    dv.getSoLuong(),
                    com.wms.util.HoaDonGiamGiaUtil.formatTienVnd(dv.getDonGia()),
                    com.wms.util.HoaDonGiamGiaUtil.formatTienVnd(dv.getThanhTien())));
        }
        sb.append("--------------------------------\n");
        com.wms.util.HoaDonGiamGiaUtil.ThongTinGiamGia giamGia =
                com.wms.util.HoaDonGiamGiaUtil.taoThongTinGiamGia(tt, 0);
        double tongTienGoc = tt.getTongTienGoc() > 0 ? tt.getTongTienGoc() : tt.getTongTien();
        sb.append("TỔNG TIỀN GỐC: ").append(com.wms.util.HoaDonGiamGiaUtil.formatTienVnd(tongTienGoc)).append("\n\n");

        if (tt.getTienGocDatTruoc() > 0 || tt.getSoTienDaTraTruoc() > 0) {
            sb.append("GIẢM ĐẶT TRƯỚC:\n");
            if (giamGia.getTienGiamVoucherDatTruoc() > 0) {
                sb.append(giamGia.getNhanVoucherDatTruoc()).append(": ")
                        .append(com.wms.util.HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTienGiamVoucherDatTruoc()))
                        .append("\n");
            }
            if (giamGia.getTienGiamHangDatTruoc() > 0) {
                sb.append(giamGia.getNhanHangDatTruoc()).append(": ")
                        .append(com.wms.util.HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTienGiamHangDatTruoc()))
                        .append("\n");
            }
            sb.append("Tổng giảm đặt trước: ")
                    .append(com.wms.util.HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTongGiamDatTruoc()))
                    .append("\n");
            sb.append("ĐÃ TRẢ TRƯỚC: ")
                    .append(com.wms.util.HoaDonGiamGiaUtil.formatTienVnd(tt.getSoTienDaTraTruoc()))
                    .append("\n\n");
        }

        sb.append("PHÁT SINH TẠI QUÁN:\n");
        sb.append("Tổng phát sinh gốc: ")
                .append(com.wms.util.HoaDonGiamGiaUtil.formatTienVnd(tt.getTienGocPhatSinh()))
                .append("\n");
        if (giamGia.getTienGiamVoucherTaiQuay() > 0) {
            sb.append(giamGia.getNhanVoucherTaiQuay()).append(": ")
                    .append(com.wms.util.HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTienGiamVoucherTaiQuay()))
                    .append("\n");
        }
        if (giamGia.getTienGiamHangTaiQuay() > 0) {
            sb.append(giamGia.getNhanHangTaiQuay()).append(": ")
                    .append(com.wms.util.HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTienGiamHangTaiQuay()))
                    .append("\n");
        }
        sb.append("Tổng giảm tại quán: ")
                .append(com.wms.util.HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTongGiamTaiQuay()))
                .append("\n\n");

        sb.append("TỔNG GIẢM: ")
                .append(com.wms.util.HoaDonGiamGiaUtil.formatTienGiamVnd(giamGia.getTongTienGiam()))
                .append("\n");
        if (tt.getSoTienThanhToanTaiQuay() > 0) {
            sb.append("ĐÃ THANH TOÁN TẠI QUẦY: ")
                    .append(com.wms.util.HoaDonGiamGiaUtil.formatTienVnd(tt.getSoTienThanhToanTaiQuay()))
                    .append("\n");
        }
        sb.append("CÒN PHẢI THANH TOÁN: ")
                .append(com.wms.util.HoaDonGiamGiaUtil.formatTienVnd(
                        com.wms.util.HoaDonGiamGiaUtil.layConPhaiThanhToan(tt, giamGia, 0)))
                .append("\n\n");
        sb.append("      --- CẢM ƠN QUÝ KHÁCH ---");

        javax.swing.JTextArea textArea = new javax.swing.JTextArea(sb.toString());
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
        textArea.setEditable(false);
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 500));

        Object[] options = {"Xuất PDF", "Đóng"};
        int choice = JOptionPane.showOptionDialog(this, scrollPane, "Hóa đơn PDF Preview",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) { // Xuất PDF
            xuatPDFHoaDon(tt);
        }
    }

    private void xuatPDFHoaDon(com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO hoaDonHienTai) {
        if (hoaDonHienTai == null || dangExportHoaDon) return;
        Object[] options = {"Bill 80mm", "A4 PDF", "Hủy"};
        int choice = JOptionPane.showOptionDialog(this, "Chọn khổ giấy in hóa đơn:", "Xuất hóa đơn",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice != 0 && choice != 1) {
            return;
        }

        String typeName = choice == 0 ? "80mm" : "A4";
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file PDF (" + typeName + ")");
        fileChooser.setSelectedFile(new File("HoaDon_" + hoaDonHienTai.getMaHoaDon() + "_" + typeName + ".pdf"));
        if (fileChooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }

        xuatHoaDonAsync(ensurePdfExtension(fileChooser.getSelectedFile()), hoaDonHienTai, choice);
    }

    private void xuatHoaDonAsync(File file,
            com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO hoaDonHienTai,
            int choice) {
        dangExportHoaDon = true;
        btnXemHoaDon.setEnabled(false);
        long start = System.currentTimeMillis();

        new javax.swing.SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                try {
                    if (choice == 0) {
                        com.wms.util.HoaDonJasperExporter.xuatHoaDon80mmToFile(file, hoaDonHienTai, 0);
                    } else {
                        com.wms.util.HoaDonJasperExporter.xuatHoaDonA4ToFile(file, hoaDonHienTai, 0);
                    }
                } catch (Exception jasperEx) {
                    System.err.println("[QuanLyHoaDonForm] Jasper export loi, fallback iText: " + jasperEx.getMessage());
                    com.wms.util.HoaDonPDFExporter.xuatHoaDonPDFToFile(file, hoaDonHienTai, 0);
                }
                return file;
            }

            @Override
            protected void done() {
                dangExportHoaDon = false;
                btnXemHoaDon.setEnabled(true);
                try {
                    File exported = get();
                    System.out.println("[QuanLyHoaDonForm] export hoa don mat "
                            + (System.currentTimeMillis() - start) + " ms");
                    JOptionPane.showMessageDialog(QuanLyHoaDonForm.this,
                            "Xuất hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(exported);
                    }
                } catch (Exception ex) {
                    com.wms.util.MessageUtil.showError(QuanLyHoaDonForm.this, "Lỗi xuất hóa đơn.", ex);
                }
            }
        }.execute();
    }

    private File ensurePdfExtension(File file) {
        if (file.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
            return file;
        }
        return new File(file.getAbsolutePath() + ".pdf");
    }

    private void resetForm() {
        txtMaHD.setText("");
        txtKhachHang.setText("");
        txtNgayTao.setText("");
        txtThanhToan.setText("");
        txtTruocGiamGia.setText("");
        txtTrangThai.setText("");
        txtHinhThuc.setText("");
        txtTrangThaiPhien.setText("");
        txtThoiGianBatDau.setText("");
        txtThoiGianDaDung.setText("");
        txtThoiGianKetThuc.setText("");
        txtThoiGianLamTron.setText("");
        btnXacNhan.setEnabled(false);
        btnHuy.setEnabled(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXacNhan;
    private javax.swing.JButton btnXemHoaDon;
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
    private javax.swing.JLabel lblNgayTao3;
    private javax.swing.JLabel lblNgayTao4;
    private javax.swing.JLabel lblThanhToan;
    private javax.swing.JLabel lblThanhToan1;
    private javax.swing.JLabel lblTongTien;
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
    private javax.swing.JTextField txtThanhToan;
    private javax.swing.JTextField txtThoiGianBatDau;
    private javax.swing.JTextField txtThoiGianDaDung;
    private javax.swing.JTextField txtThoiGianKetThuc;
    private javax.swing.JTextField txtThoiGianLamTron;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txtTrangThai;
    private javax.swing.JTextField txtTrangThaiPhien;
    private javax.swing.JTextField txtTruocGiamGia;
    // End of variables declaration//GEN-END:variables
}





