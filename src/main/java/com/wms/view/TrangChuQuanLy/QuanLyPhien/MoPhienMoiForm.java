package com.wms.view.TrangChuQuanLy.QuanLyPhien;

import com.wms.controller.TrangChuGioiThieu.DangNhapController;
import com.wms.controller.TrangChuQuanLy.QuanLyPhien.MoPhienMoiController;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.KetQuaNhanChoDTO;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.Timer;

public class MoPhienMoiForm extends javax.swing.JPanel {

    private final Color COLOR_AVAILABLE = Color.decode("#FFF0F5");
    private final Color COLOR_IN_USE = Color.decode("#E8F5E9");
    private final Color COLOR_MAINTENANCE = Color.decode("#F5F5F5");
    private final Color COLOR_PRIMARY = Color.decode("#eb5e8d");

    private final MoPhienMoiController controller;
    private String maCNHienTai;
    private com.wms.util.SoDoKhongGianPanel mapPanel;
    private com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO khongGianChonDTO = null;
    private List<com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO> dsKGHienTai;
    private String soDienThoaiDaXacNhan = "";
    private boolean tenKhachHangTuDongDien = false;
    private javax.swing.JLabel lblGioHoatDong;

    public MoPhienMoiForm(String maCN) {
        initComponents();
        controller = new MoPhienMoiController(this);

        // Khoi tao nhan hien thi gio hoat dong
        lblGioHoatDong = new javax.swing.JLabel();
        lblGioHoatDong.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 14));
        lblGioHoatDong.setForeground(java.awt.Color.decode("#666666"));
        lblGioHoatDong.setText("Gio hoat dong: --:-- - --:--");
        pnLeftMap.add(lblGioHoatDong);
        lblGioHoatDong.setBounds(220, 15, 200, 30);

        if (maCN != null && !maCN.isEmpty()) {
            this.maCNHienTai = maCN;
        } else {
            com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO user = DangNhapController.getCurrentUser();
            this.maCNHienTai = controller.layMaCNNguoiDung(user);
        }

        initMap();
        initLegend();
        initTimeCalculation();
        startClock();
        initTraCuuKhachHang();
        com.wms.util.TienIchFormQuanLy.apDung(this);
    }

    private void initTimeCalculation() {
        txtThoiGianSuDung.setEditable(true);
        txtThoiGianSuDung.setBackground(Color.WHITE);
        com.wms.util.InputFormatUtil.attachThousandsFormatter(txtThoiGianSuDung);
        txtThoiGianSuDung.setText("1"); // Mặc định 1h
        
        // Cập nhật giờ kết thúc lần đầu
        updateEndTime();

        // Thêm listener để tính giờ kết thúc khi thay đổi số giờ
        txtThoiGianSuDung.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateEndTime(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateEndTime(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateEndTime(); }
        });
    }

    private void updateEndTime() {
        try {
            java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
            java.time.ZonedDateTime nowHcm = java.time.ZonedDateTime.now(zoneId);
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            txtThoiGianBatDau.setText(sdf.format(java.util.Date.from(nowHcm.toInstant())));

            String suDungStr = txtThoiGianSuDung.getText().trim();
            if (suDungStr.isEmpty()) {
                txtThoiGianKetThuc.setText("");
                txtThoiGianKetThuc.setForeground(new java.awt.Color(102, 102, 102));
                btnMoPhien.setEnabled(true);
                return;
            }
            
            Long parsedHours = com.wms.util.InputFormatUtil.getNumberValue(txtThoiGianSuDung);
            if (parsedHours == null || parsedHours > Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }
            int hours = parsedHours.intValue();
            if (hours <= 0) {
                txtThoiGianKetThuc.setText("So gio > 0");
                txtThoiGianKetThuc.setForeground(java.awt.Color.RED);
                btnMoPhien.setEnabled(false);
                return;
            }
            
            java.time.ZonedDateTime expectedEnd = nowHcm.plusHours(hours);
            txtThoiGianKetThuc.setText(sdf.format(java.util.Date.from(expectedEnd.toInstant())));

            if (khongGianChonDTO != null) {
                String[] gioHoatDong = controller.layGioHoatDongTheoKhongGian(khongGianChonDTO.getMaKG());
                if (gioHoatDong != null) {
                    String gioMoCua = gioHoatDong[0];
                    String gioDongCua = gioHoatDong[1];

                    // Xu ly "24:00": Java LocalTime khong ho tro 24:00 nen phai xu ly rieng.
                    // "24:00" = het ngay = nua dem ngay hom sau (00:00 sang hom sau)
                    final boolean dongCuaLaNuaDemSauNgay = "24:00".equals(gioDongCua.trim());

                    java.time.LocalTime openLocalTime = com.wms.util.DateInputUtil.parseTime(gioMoCua.trim(), "Giờ mở cửa");
                    java.time.ZonedDateTime openDateTime = nowHcm.with(openLocalTime).withSecond(0).withNano(0);

                    // closeDateTime: neu la "24:00" thi tinh la 00:00 cua ngay hom sau
                    java.time.ZonedDateTime closeDateTime;
                    if (dongCuaLaNuaDemSauNgay) {
                        closeDateTime = nowHcm.toLocalDate().plusDays(1)
                                .atStartOfDay(nowHcm.getZone());
                    } else {
                        java.time.LocalTime closeLocalTime = com.wms.util.DateInputUtil.parseTime(gioDongCua.trim(), "Giờ đóng cửa");
                        closeDateTime = nowHcm.with(closeLocalTime).withSecond(0).withNano(0);
                        // Neu gio dong cua < gio mo cua (vi du: dong cua 02:00, mo cua 08:00)
                        // thi closeDateTime thuoc sang hom sau
                        if (closeDateTime.isBefore(openDateTime)) {
                            closeDateTime = closeDateTime.plusDays(1);
                        }
                    }

                    // Kiem tra: gio hien tai truoc gio mo cua?
                    // (Neu mo cua la 00:00 va dong cua la 24:00 = hoat dong ca ngay, bo qua kiem tra nay)
                    boolean hoatDongCaNgay = openLocalTime.equals(java.time.LocalTime.MIDNIGHT) && dongCuaLaNuaDemSauNgay;
                    if (!hoatDongCaNgay && nowHcm.isBefore(openDateTime)) {
                        txtThoiGianKetThuc.setText("Chua den gio hoat dong (Mo: " + gioMoCua + ")");
                        txtThoiGianKetThuc.setForeground(java.awt.Color.RED);
                        btnMoPhien.setEnabled(false);
                        return;
                    }

                    // Kiem tra: gio hien tai da qua gio dong cua?
                    if (!nowHcm.isBefore(closeDateTime)) {
                        txtThoiGianKetThuc.setText("Da qua gio hoat dong (Dong: " + gioDongCua + ")");
                        txtThoiGianKetThuc.setForeground(java.awt.Color.RED);
                        btnMoPhien.setEnabled(false);
                        return;
                    }

                    // Kiem tra: gio ket thuc du kien co vuot qua gio dong cua?
                    if (expectedEnd.isAfter(closeDateTime)) {
                        txtThoiGianKetThuc.setText("Vuot qua gio dong cua chi nhanh (" + gioDongCua + ")");
                        txtThoiGianKetThuc.setForeground(java.awt.Color.RED);
                        btnMoPhien.setEnabled(false);
                        return;
                    }

                    // Kiem tra: con du thoi gian de mo phien moi (toi thieu 1 gio)?
                    long remainingSeconds = java.time.Duration.between(nowHcm, closeDateTime).getSeconds();
                    if (remainingSeconds < 3600) {
                        txtThoiGianKetThuc.setText("Thoi gian con lai den gio dong cua khong du de mo phien moi.");
                        txtThoiGianKetThuc.setForeground(java.awt.Color.RED);
                        btnMoPhien.setEnabled(false);
                        return;
                    }
                }
            }
            
            txtThoiGianKetThuc.setForeground(new java.awt.Color(102, 102, 102));
            btnMoPhien.setEnabled(true);
        } catch (NumberFormatException e) {
            txtThoiGianKetThuc.setText("Chi nhap so nguyen");
            txtThoiGianKetThuc.setForeground(java.awt.Color.RED);
            btnMoPhien.setEnabled(false);
        }
    }

    private void initMap() {
        pnSoDo.removeAll();
        pnSoDo.setLayout(new java.awt.BorderLayout());

        dsKGHienTai = controller.layKhongGian(maCNHienTai);

        if (dsKGHienTai == null || dsKGHienTai.isEmpty()) {
            pnSoDo.add(new JLabel("Không có dữ liệu sơ đồ cho chi nhánh này."), java.awt.BorderLayout.CENTER);
            pnSoDo.revalidate();
            pnSoDo.repaint();
            return;
        }

        mapPanel = new com.wms.util.SoDoKhongGianPanel();
        mapPanel.setOnTableClick(kg -> handleTableClick(kg));
        mapPanel.veSoDo(dsKGHienTai, khongGianChonDTO != null ? khongGianChonDTO.getMaKG() : null);

        JScrollPane scroll = new JScrollPane(mapPanel);
        scroll.setBorder(null);
        pnSoDo.add(scroll, java.awt.BorderLayout.CENTER);
        pnSoDo.revalidate();
        pnSoDo.repaint();
    }

    // 2. Chú thích các loại màu bên dưới bản đồ
    private void initLegend() {
        pnChuThich.removeAll();
        pnChuThich.add(createLegendItem(COLOR_AVAILABLE, "Trống"));
        pnChuThich.add(createLegendItem(COLOR_IN_USE, "Đang hoạt động"));
        pnChuThich.add(createLegendItem(COLOR_MAINTENANCE, "Bảo trì"));
        pnChuThich.revalidate();
        pnChuThich.repaint();
    }

    private JLabel createLegendItem(Color color, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setIcon(new javax.swing.ImageIcon(createColorIcon(color)));
        return label;
    }

    private java.awt.Image createColorIcon(Color color) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(16, 16,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(0, 0, 15, 15);
        g2d.dispose();
        return img;
    }

    private void handleTableClick(com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO kg) {
        String status = kg.getTrangThaiKG() != null ? kg.getTrangThaiKG().trim() : "Trống";

        if ("Đang hoạt động".equals(status)) {
            com.wms.util.MessageUtil.showWarning(this, "Không gian này đang có khách sử dụng!");
            return;
        }
        if ("Bảo trì".equals(status)) {
            com.wms.util.MessageUtil.showError(this, "Không gian này đang bảo trì, không thể mở phiên!");
            return;
        }

        khongGianChonDTO = kg;
        mapPanel.veSoDo(dsKGHienTai, kg.getMaKG());
        txtKhongGianChon.setText(kg.getTenKG());
        txtLoaiKhongGian.setText(kg.getTenLoaiKG() != null ? kg.getTenLoaiKG() : "Không xác định");
        txtMaKGian.setText(kg.getMaKG());
        txtGiaTien.setText(kg.getDonGia() != null ? com.wms.util.InputFormatUtil.formatThousands(kg.getDonGia()) : "0");

        // Cap nhat gio hoat dong cua chi nhanh tuong ung voi khong gian da chon
        String[] gioHoatDong = controller.layGioHoatDongTheoKhongGian(kg.getMaKG());
        if (gioHoatDong != null) {
            lblGioHoatDong.setText("Gio hoat dong: " + gioHoatDong[0] + " - " + gioHoatDong[1]);
        } else {
            lblGioHoatDong.setText("Gio hoat dong: --:-- - --:--");
        }
        
        // Cap nhat va kiem tra thoi gian ngay lap tuc
        updateEndTime();
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            updateEndTime(); // Hàm này giờ sẽ cập nhật cả 2 ô dựa trên cùng 1 thời điểm 'now'
        });
        timer.start();
    }

    private void initTraCuuKhachHang() {
        txtSoDienThoai.addActionListener(this::btnXacNhanActionPerformed);
        txtSoDienThoai.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { xuLyThayDoiSoDienThoai(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { xuLyThayDoiSoDienThoai(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { xuLyThayDoiSoDienThoai(); }
        });
    }

    private void xuLyThayDoiSoDienThoai() {
        String sdt = txtSoDienThoai.getText().trim();
        if (sdt.equals(soDienThoaiDaXacNhan)) {
            return;
        }

        if (tenKhachHangTuDongDien) {
            txtTenKhachHang.setText("");
        }
        soDienThoaiDaXacNhan = "";
        tenKhachHangTuDongDien = false;
        datTrangThaiNhapTenKhach(true);
    }

    private void datTrangThaiNhapTenKhach(boolean choNhapThuCong) {
        txtTenKhachHang.setEditable(choNhapThuCong);
        txtTenKhachHang.setBackground(choNhapThuCong ? Color.WHITE : new Color(240, 240, 240));
    }

    private void btnXacNhanActionPerformed(java.awt.event.ActionEvent evt) {
        String sdt = txtSoDienThoai.getText().trim();
        if (sdt.isEmpty()) {
            com.wms.util.MessageUtil.showWarning(this, "Vui lòng nhập số điện thoại trước khi xác nhận!");
            txtSoDienThoai.requestFocusInWindow();
            return;
        }

        HoiVienDTO khachHang = controller.timKhachHangTheoSdt(sdt);
        soDienThoaiDaXacNhan = sdt;
        if (khachHang != null) {
            String hoTen = khachHang.getHoTen() != null ? khachHang.getHoTen().trim() : "";
            txtTenKhachHang.setText(hoTen);
            tenKhachHangTuDongDien = !hoTen.isEmpty();
            datTrangThaiNhapTenKhach(hoTen.isEmpty());

            if (hoTen.isEmpty()) {
                com.wms.util.MessageUtil.showInfo(this, "Đã tìm thấy khách hàng nhưng chưa có tên. Vui lòng nhập tên khách hàng.");
                txtTenKhachHang.requestFocusInWindow();
            } else {
                com.wms.util.MessageUtil.showInfo(this, "Đã tìm thấy khách hàng: " + hoTen);
                txtThoiGianSuDung.requestFocusInWindow();
            }
            return;
        }

        tenKhachHangTuDongDien = false;
        txtTenKhachHang.setText("");
        datTrangThaiNhapTenKhach(true);
        com.wms.util.MessageUtil.showInfo(this, "Chưa có khách hàng với số điện thoại này. Vui lòng nhập tên khách vãng lai.");
        txtTenKhachHang.requestFocusInWindow();
    }

    // === CÁC SỰ KIỆN NÚT BẤM FORM BÊN PHẢI ===

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");
        soDienThoaiDaXacNhan = "";
        tenKhachHangTuDongDien = false;
        datTrangThaiNhapTenKhach(true);
        txtKhongGianChon.setText("(Chưa chọn chỗ ngồi)");
        txtLoaiKhongGian.setText("");
        txtMaKGian.setText("");
        if (lblGioHoatDong != null) {
            lblGioHoatDong.setText("Gio hoat dong: --:-- - --:--");
        }

        khongGianChonDTO = null;
        if (mapPanel != null) {
            mapPanel.veSoDo(dsKGHienTai, null);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        pnLeftMap = new javax.swing.JPanel();
        lblMapTitle = new javax.swing.JLabel();
        btnNhanChoQr = new javax.swing.JButton();
        pnSoDo = new javax.swing.JPanel();
        pnChuThich = new javax.swing.JPanel();
        pnRightForm = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblTenKhachHang = new javax.swing.JLabel();
        txtTenKhachHang = new javax.swing.JTextField();
        lblSDT = new javax.swing.JLabel();
        txtSoDienThoai = new javax.swing.JTextField();
        lblKhongGianChon = new javax.swing.JLabel();
        txtKhongGianChon = new javax.swing.JTextField();
        lblLoaiKhongGian = new javax.swing.JLabel();
        txtLoaiKhongGian = new javax.swing.JTextField();
        lblThoiGianBatDau = new javax.swing.JLabel();
        txtThoiGianBatDau = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();
        btnXacNhan = new javax.swing.JButton();
        txtThoiGianKetThuc = new javax.swing.JTextField();
        lblThoiGianBatDau1 = new javax.swing.JLabel();
        lblLoaiKhongGian1 = new javax.swing.JLabel();
        txtThoiGianSuDung = new javax.swing.JTextField();
        txtMaKGian = new javax.swing.JTextField();
        lblLoaiKhongGian2 = new javax.swing.JLabel();
        txtGiaTien = new javax.swing.JTextField();
        lblKhongGianChon1 = new javax.swing.JLabel();
        btnMoPhien = new javax.swing.JButton();

        setBackground(new java.awt.Color(254, 248, 250));
        setPreferredSize(new java.awt.Dimension(1050, 640));
        setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("MỞ PHIÊN LÀM VIỆC TẠI QUẦY");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        pnLeftMap.setBackground(new java.awt.Color(255, 255, 255));
        pnLeftMap.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeftMap.setLayout(null);

        lblMapTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblMapTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblMapTitle.setText("SƠ ĐỒ KHÔNG GIAN");
        pnLeftMap.add(lblMapTitle);
        lblMapTitle.setBounds(20, 15, 200, 30);

        btnNhanChoQr.setBackground(new java.awt.Color(35, 30, 48));
        btnNhanChoQr.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNhanChoQr.setForeground(new java.awt.Color(255, 255, 255));
        btnNhanChoQr.setText("Nhận chỗ bằng QR");
        btnNhanChoQr.addActionListener(this::btnNhanChoQrActionPerformed);
        pnLeftMap.add(btnNhanChoQr);
        btnNhanChoQr.setBounds(430, 15, 190, 30);
        btnNhanChoQr.setVisible(false);
        btnNhanChoQr.setEnabled(false);

        pnSoDo.setBackground(new java.awt.Color(255, 255, 255));
        pnSoDo.setLayout(new java.awt.GridLayout(4, 4, 10, 10));
        pnLeftMap.add(pnSoDo);
        pnSoDo.setBounds(20, 60, 610, 400);

        pnChuThich.setBackground(new java.awt.Color(255, 255, 255));
        pnChuThich.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(240, 240, 240)));
        pnChuThich.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 15));
        pnLeftMap.add(pnChuThich);
        pnChuThich.setBounds(20, 470, 610, 50);

        add(pnLeftMap);
        pnLeftMap.setBounds(390, 70, 650, 530);

        pnRightForm.setBackground(new java.awt.Color(255, 255, 255));
        pnRightForm.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRightForm.setLayout(null);

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblFormTitle.setText("THÔNG TIN KHÁCH HÀNG");
        pnRightForm.add(lblFormTitle);
        lblFormTitle.setBounds(20, 15, 300, 30);

        lblTenKhachHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenKhachHang.setForeground(new java.awt.Color(35, 30, 48));
        lblTenKhachHang.setText("Tên khách hàng (*)");
        pnRightForm.add(lblTenKhachHang);
        lblTenKhachHang.setBounds(20, 120, 230, 20);

        txtTenKhachHang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRightForm.add(txtTenKhachHang);
        txtTenKhachHang.setBounds(20, 140, 310, 35);

        lblSDT.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSDT.setForeground(new java.awt.Color(35, 30, 48));
        lblSDT.setText("Số điện thoại (*)");
        pnRightForm.add(lblSDT);
        lblSDT.setBounds(20, 50, 310, 20);

        txtSoDienThoai.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRightForm.add(txtSoDienThoai);
        txtSoDienThoai.setBounds(20, 70, 200, 35);

        lblKhongGianChon.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKhongGianChon.setForeground(new java.awt.Color(35, 30, 48));
        lblKhongGianChon.setText("Tên Không gian ");
        pnRightForm.add(lblKhongGianChon);
        lblKhongGianChon.setBounds(20, 240, 150, 20);

        txtKhongGianChon.setEditable(false);
        txtKhongGianChon.setBackground(new java.awt.Color(240, 240, 240));
        txtKhongGianChon.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        txtKhongGianChon.setForeground(new java.awt.Color(235, 94, 141));
        txtKhongGianChon.addActionListener(this::txtKhongGianChonActionPerformed);
        pnRightForm.add(txtKhongGianChon);
        txtKhongGianChon.setBounds(20, 260, 150, 35);

        lblLoaiKhongGian.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoaiKhongGian.setForeground(new java.awt.Color(35, 30, 48));
        lblLoaiKhongGian.setText("Tên Loại không gian");
        pnRightForm.add(lblLoaiKhongGian);
        lblLoaiKhongGian.setBounds(20, 300, 130, 20);

        txtLoaiKhongGian.setEditable(false);
        txtLoaiKhongGian.setBackground(new java.awt.Color(240, 240, 240));
        txtLoaiKhongGian.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRightForm.add(txtLoaiKhongGian);
        txtLoaiKhongGian.setBounds(20, 320, 150, 35);

        lblThoiGianBatDau.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblThoiGianBatDau.setForeground(new java.awt.Color(35, 30, 48));
        lblThoiGianBatDau.setText("Thời gian bắt đầu phiên");
        pnRightForm.add(lblThoiGianBatDau);
        lblThoiGianBatDau.setBounds(20, 360, 310, 20);

        txtThoiGianBatDau.setEditable(false);
        txtThoiGianBatDau.setBackground(new java.awt.Color(240, 240, 240));
        txtThoiGianBatDau.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        txtThoiGianBatDau.setForeground(new java.awt.Color(102, 102, 102));
        pnRightForm.add(txtThoiGianBatDau);
        txtThoiGianBatDau.setBounds(20, 380, 310, 35);

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi.setForeground(new java.awt.Color(235, 94, 141));
        btnLamMoi.setText("Làm mới form");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);
        pnRightForm.add(btnLamMoi);
        btnLamMoi.setBounds(20, 540, 300, 35);

        btnXacNhan.setBackground(new java.awt.Color(235, 94, 141));
        btnXacNhan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnXacNhan.setForeground(new java.awt.Color(255, 255, 255));
        btnXacNhan.setText("Xác nhận");
        btnXacNhan.addActionListener(this::btnXacNhanActionPerformed);
        pnRightForm.add(btnXacNhan);
        btnXacNhan.setBounds(230, 70, 90, 35);

        txtThoiGianKetThuc.setEditable(false);
        txtThoiGianKetThuc.setBackground(new java.awt.Color(240, 240, 240));
        txtThoiGianKetThuc.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        txtThoiGianKetThuc.setForeground(new java.awt.Color(102, 102, 102));
        txtThoiGianKetThuc.addActionListener(this::txtThoiGianKetThucActionPerformed);
        pnRightForm.add(txtThoiGianKetThuc);
        txtThoiGianKetThuc.setBounds(20, 440, 310, 35);

        lblThoiGianBatDau1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblThoiGianBatDau1.setForeground(new java.awt.Color(35, 30, 48));
        lblThoiGianBatDau1.setText("Thời gian kết thúc");
        pnRightForm.add(lblThoiGianBatDau1);
        lblThoiGianBatDau1.setBounds(20, 420, 310, 20);

        lblLoaiKhongGian1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoaiKhongGian1.setForeground(new java.awt.Color(35, 30, 48));
        lblLoaiKhongGian1.setText("Thời gian sử dụng (*)");
        pnRightForm.add(lblLoaiKhongGian1);
        lblLoaiKhongGian1.setBounds(180, 300, 130, 20);

        txtThoiGianSuDung.setEditable(false);
        txtThoiGianSuDung.setBackground(new java.awt.Color(240, 240, 240));
        txtThoiGianSuDung.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtThoiGianSuDung.addActionListener(this::txtThoiGianSuDungActionPerformed);
        pnRightForm.add(txtThoiGianSuDung);
        txtThoiGianSuDung.setBounds(180, 320, 150, 35);

        txtMaKGian.setEditable(false);
        txtMaKGian.setBackground(new java.awt.Color(240, 240, 240));
        txtMaKGian.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtMaKGian.addActionListener(this::txtMaKGianActionPerformed);
        pnRightForm.add(txtMaKGian);
        txtMaKGian.setBounds(20, 200, 310, 35);

        lblLoaiKhongGian2.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoaiKhongGian2.setForeground(new java.awt.Color(35, 30, 48));
        lblLoaiKhongGian2.setText("Mã không gian");
        pnRightForm.add(lblLoaiKhongGian2);
        lblLoaiKhongGian2.setBounds(20, 180, 290, 20);

        txtGiaTien.setEditable(false);
        txtGiaTien.setBackground(new java.awt.Color(240, 240, 240));
        txtGiaTien.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        txtGiaTien.setForeground(new java.awt.Color(235, 94, 141));
        txtGiaTien.addActionListener(this::txtGiaTienActionPerformed);
        pnRightForm.add(txtGiaTien);
        txtGiaTien.setBounds(180, 260, 150, 35);

        lblKhongGianChon1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKhongGianChon1.setForeground(new java.awt.Color(35, 30, 48));
        lblKhongGianChon1.setText("Phí mỗi một giờ");
        pnRightForm.add(lblKhongGianChon1);
        lblKhongGianChon1.setBounds(180, 240, 150, 20);

        btnMoPhien.setBackground(new java.awt.Color(235, 94, 141));
        btnMoPhien.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnMoPhien.setForeground(new java.awt.Color(255, 255, 255));
        btnMoPhien.setText("Mở phiên cho khách");
        btnMoPhien.addActionListener(this::btnMoPhienActionPerformed);
        pnRightForm.add(btnMoPhien);
        btnMoPhien.setBounds(20, 490, 310, 40);

        add(pnRightForm);
        pnRightForm.setBounds(10, 70, 340, 530);
    }// </editor-fold>//GEN-END:initComponents

    private void txtThoiGianSuDungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtThoiGianSuDungActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtThoiGianSuDungActionPerformed

    private void txtThoiGianKetThucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtThoiGianKetThucActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtThoiGianKetThucActionPerformed

    private void txtMaKGianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMaKGianActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMaKGianActionPerformed

    private void txtKhongGianChonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKhongGianChonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKhongGianChonActionPerformed

    private void txtGiaTienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGiaTienActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtGiaTienActionPerformed

    private void btnMoPhienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoPhienActionPerformed
        String tenKH = txtTenKhachHang.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();
        String khongGian = txtKhongGianChon.getText();
        String suDungStr = txtThoiGianSuDung.getText().trim();

        if (tenKH.isEmpty() || sdt.isEmpty()) {
            com.wms.util.MessageUtil.showWarning(this, "Vui lòng nhập đầy đủ tên và số điện thoại khách hàng!");
            return;
        }
        if (khongGianChonDTO == null) {
            com.wms.util.MessageUtil.showWarning(this, "Vui lòng chọn một vị trí trên sơ đồ!");
            return;
        }

        int soGio = 0;
        try {
            Long parsedHours = com.wms.util.InputFormatUtil.getNumberValue(txtThoiGianSuDung);
            if (parsedHours == null || parsedHours > Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }
            soGio = parsedHours.intValue();
            if (soGio <= 0) {
                throw new NumberFormatException();
            }
        } catch (Exception e) {
            com.wms.util.MessageUtil.showWarning(this, "Thời gian sử dụng phải là số nguyên dương (giờ)!");
            return;
        }

        double giaTien = 0;
        try {
            java.math.BigDecimal giaValue = com.wms.util.InputFormatUtil.getBigDecimalValue(txtGiaTien);
            giaTien = giaValue != null ? giaValue.doubleValue() : 0;
        } catch (Exception e) {
            // Giữ giá 0 nếu ô giá chưa có dữ liệu hợp lệ.
        }

        try {
            if (controller.moPhienMoi(tenKH, sdt, khongGianChonDTO.getMaKG(), soGio, giaTien)) {
                com.wms.util.MessageUtil.showInfo(this, "Mở phiên thành công cho khách: " + tenKH + "\nVị trí: " + khongGian);
                khongGianChonDTO = null;
                initMap();
                btnLamMoiActionPerformed(evt);
            } else {
                com.wms.util.MessageUtil.showError(this, "[DATABASE/SYSTEM ERROR]: Loi khi mo phien lam viec! Vui long kiem tra lai ket noi hoac du lieu.");
            }
        } catch (IllegalArgumentException e) {
            com.wms.util.MessageUtil.showError(this, e.getMessage(), e);
        } catch (Exception e) {
            com.wms.util.MessageUtil.showError(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_btnMoPhienActionPerformed

    private void btnNhanChoQrActionPerformed(java.awt.event.ActionEvent evt) {
        javax.swing.JTextArea input = new javax.swing.JTextArea(5, 42);
        input.setLineWrap(true);
        input.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(input);
        int choice = JOptionPane.showConfirmDialog(
                this,
                scroll,
                "Dán nội dung QR nhận chỗ",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        KetQuaNhanChoDTO result = controller.nhanChoBangQr(input.getText());
        if (result.isThanhCong()) {
            com.wms.util.MessageUtil.showInfo(this, result.getThongBao());
            khongGianChonDTO = null;
            initMap();
            btnLamMoiActionPerformed(evt);
        } else {
            com.wms.util.MessageUtil.showError(this, result.getThongBao());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnMoPhien;
    private javax.swing.JButton btnNhanChoQr;
    private javax.swing.JButton btnXacNhan;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblKhongGianChon;
    private javax.swing.JLabel lblKhongGianChon1;
    private javax.swing.JLabel lblLoaiKhongGian;
    private javax.swing.JLabel lblLoaiKhongGian1;
    private javax.swing.JLabel lblLoaiKhongGian2;
    private javax.swing.JLabel lblMapTitle;
    private javax.swing.JLabel lblSDT;
    private javax.swing.JLabel lblTenKhachHang;
    private javax.swing.JLabel lblThoiGianBatDau;
    private javax.swing.JLabel lblThoiGianBatDau1;
    private javax.swing.JPanel pnChuThich;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeftMap;
    private javax.swing.JPanel pnRightForm;
    private javax.swing.JPanel pnSoDo;
    private javax.swing.JTextField txtGiaTien;
    private javax.swing.JTextField txtKhongGianChon;
    private javax.swing.JTextField txtLoaiKhongGian;
    private javax.swing.JTextField txtMaKGian;
    private javax.swing.JTextField txtSoDienThoai;
    private javax.swing.JTextField txtTenKhachHang;
    private javax.swing.JTextField txtThoiGianBatDau;
    private javax.swing.JTextField txtThoiGianKetThuc;
    private javax.swing.JTextField txtThoiGianSuDung;
    // End of variables declaration//GEN-END:variables
}
