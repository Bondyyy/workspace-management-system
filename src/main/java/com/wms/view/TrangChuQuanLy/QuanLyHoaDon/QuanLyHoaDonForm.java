package com.wms.view.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.controller.TrangChuQuanLy.QuanLyHoaDon.HoaDonController;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.HoaDonDTO;
import com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.ThanhToanHoaDonForm;

import java.awt.Window;
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
    private final DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private HoaDonDTO currentHD;
    private List<HoaDonDTO> dsHoaDon = Collections.emptyList(); // cache, tránh gọi DB 2 lần
    private javax.swing.Timer realTimeTimer;

    public QuanLyHoaDonForm() {
        initComponents();
        tableModel = (DefaultTableModel) tblHoaDon.getModel();
        loadDataToTable();
        initRealTimeTimer();
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
        dsHoaDon = hoaDonController.layDanhSachHoaDon(
                txtTimKiem.getText(), cbxLocTrangThai.getSelectedItem().toString());
        tableModel.setRowCount(0);
        for (HoaDonDTO hd : dsHoaDon) {
            tableModel.addRow(new Object[] {
                    hd.getMaHoaDon(),
                    hd.getNgayLapHoaDon() != null ? sdf.format(hd.getNgayLapHoaDon()) : "N/A",
                    hd.getHoTenKH(),
                    df.format(hd.getTongTien()),
                    hd.getTrangThaiThanhToan(),
                    hd.isDaTraTruoc() ? "Đặt trước (Đã trả trước)" : (hd.getMaDatCho() != null ? "Đặt trước" : "Trực tiếp")
            });
        }
    }

    private void showChiTietHoaDon(String maHD) {
        int row = tblHoaDon.getSelectedRow();
        if (row == -1) return;

        txtMaHD.setText(maHD);
        txtKhachHang.setText(tableModel.getValueAt(row, 2).toString());
        
        for (HoaDonDTO hd : dsHoaDon) {
            if (!hd.getMaHoaDon().equals(maHD)) continue;
            this.currentHD = hd;
            
            txtNgayTao.setText(sdf.format(hd.getNgayLapHoaDon()));
            txtNgayTao.setCaretPosition(0);

            txtThanhToan.setText(hd.getPhuongThucThanhToan() != null ? hd.getPhuongThucThanhToan() : "Chưa chọn");
            
            // Xử lý tiền trả trước và hiển thị
            double soTienDaTraTruoc = hd.getSoTienDaTraTruoc();
            if (soTienDaTraTruoc > 0) {
                txtTruocGiamGia.setText("Tổng cộng: " + df.format(hd.getTongTien())
                        + "  |  Đã trả trước: " + df.format(soTienDaTraTruoc)
                        + "  |  Còn phải thanh toán: " + df.format(Math.max(0, hd.getThanhTien())));
            } else {
                txtTruocGiamGia.setText("Tổng cộng: " + df.format(hd.getTongTien())
                        + "  |  Còn phải thanh toán: " + df.format(Math.max(0, hd.getThanhTien())));
            }

            txtTrangThai.setText(hd.getTrangThaiThanhToan());
            if (txtHinhThuc != null) {
                if (hd.isDaTraTruoc()) {
                    txtHinhThuc.setText("Đặt trước (Đã trả trước)");
                } else {
                    txtHinhThuc.setText(hd.getMaDatCho() != null ? "Đặt trước" : "Trực tiếp");
                }
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
                                || "Đang chờ thanh toán phụ thu".equals(hd.getTrangThaiThanhToan());
            boolean phienDaKetThuc = !"Đang hoạt động".equals(hd.getTrangThaiPhien());
            boolean conPhaiThu = hd.getThanhTien() > 0;

            btnXacNhan.setEnabled(trangThaiHopLeDeThanhToan && phienDaKetThuc && conPhaiThu
                    && !"Đã thanh toán thành công".equals(hd.getTrangThaiThanhToan()));
            if ("Đã trả trước".equals(hd.getTrangThaiThanhToan())) {
                btnXacNhan.setText("Thanh toán phát sinh");
            } else if (hd.getTrangThaiThanhToan().equals("Đang chờ thanh toán phụ thu")
                || (hd.getTrangThaiThanhToan().equals("Đang chờ thanh toán") && hd.getSoTienDaTraTruoc() > 0)) {
                btnXacNhan.setText("Thanh toán phụ thu");
            } else {
                btnXacNhan.setText("Xác nhận Thanh toán");
            }
            btnHuy.setEnabled("Đang chờ thanh toán".equals(hd.getTrangThaiThanhToan())
                    || "Đang chờ thanh toán phụ thu".equals(hd.getTrangThaiThanhToan()));
            break;
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
        txtTimKiem.setToolTipText("Tìm theo mã hóa đơn hoặc khách hàng...");
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
        lblTongTien.setText("Tổng cộng (Trước giảm giá)");
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

        com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO tt = hoaDonController.layChiTietHoaDon(maHD);
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
        sb.append(String.format("%-20s %3s %10s\n", "Dịch vụ", "SL", "Đơn giá"));
        for (com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO dv : tt.getDanhSachDichVu()) {
            sb.append(String.format("%-20s %3d %,10.0f\n", dv.getTenDichVu(), dv.getSoLuong(), dv.getDonGia()));
        }
        sb.append("--------------------------------\n");
        sb.append("TỔNG CỘNG: ").append(String.format("%,.0f VNĐ", tt.getTongTien())).append("\n");
        sb.append("ĐÃ TRẢ TRƯỚC: ").append(String.format("%,.0f VNĐ", tt.getSoTienDaTraTruoc())).append("\n");
        sb.append("CÒN PHẢI THANH TOÁN: ").append(String.format("%,.0f VNĐ", Math.max(0, tt.getThanhTien()))).append("\n\n");
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
        if (hoaDonHienTai == null) return;
        Object[] options = {"Bill 80mm", "A4 PDF", "Hủy"};
        int choice = JOptionPane.showOptionDialog(this, "Chọn khổ giấy in hóa đơn:", "Xuất hóa đơn",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                
        if (choice == 0) {
            try {
                com.wms.util.HoaDonJasperExporter.xuatHoaDon80mm(this, hoaDonHienTai, 0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất Jasper 80mm, tự động dùng iText...\n" + e.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                com.wms.util.HoaDonPDFExporter.xuatHoaDonPDF(this, hoaDonHienTai, 0);
            }
        } else if (choice == 1) {
            try {
                com.wms.util.HoaDonJasperExporter.xuatHoaDonA4(this, hoaDonHienTai, 0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất Jasper A4, tự động dùng iText...\n" + e.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                com.wms.util.HoaDonPDFExporter.xuatHoaDonPDF(this, hoaDonHienTai, 0);
            }
        }
    }

    private void resetForm() {
        txtMaHD.setText("");
        txtKhachHang.setText("");
        txtNgayTao.setText("");
        txtThanhToan.setText("");
        txtTruocGiamGia.setText("");
        txtTrangThai.setText("");
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





