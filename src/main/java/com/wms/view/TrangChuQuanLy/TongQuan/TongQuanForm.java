package com.wms.view.TrangChuQuanLy.TongQuan;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import com.wms.controller.TrangChuQuanLy.TongQuan.TongQuanController;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.TongQuanDTO;

public class TongQuanForm extends javax.swing.JPanel {

    private final TongQuanController controller = new TongQuanController();

    private final Color mauHong = Color.decode("#EB5E8D");
    private final Color mauHongNhat = new Color(235, 94, 141, 50); 
    private final Color mauXanh = Color.decode("#5E8DEB");
    private final Color mauCam = Color.decode("#EB8D5E");
    private final DecimalFormat formatTien = new DecimalFormat("#,### VNĐ");

    public TongQuanForm() {
        initComponents();
        initDefaultDates();
        loadComboChiNhanh();
        capNhatDuLieu(); 
    }

    private void loadComboChiNhanh() {
        cbxChiNhanh.removeAllItems();
        cbxChiNhanh.addItem("Tất cả chi nhánh");
        for (ChiNhanhDTO cn : controller.layDanhSachChiNhanh()) {
            cbxChiNhanh.addItem(cn.getMaCN() + " - " + cn.getTenCN());
        }
    }

    private void initDefaultDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String today = sdf.format(new Date());
        txtTuNgay.setText(today);
        txtDenNgay.setText(today);
    }

    private JPanel taoTheThongKe(String tieuDe, String giaTri, Color mauNhan) {
        JPanel pnl = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                g2.setColor(mauNhan);
                g2.fillRoundRect(0, 0, 8, getHeight(), 15, 15);
                g2.fillRect(4, 0, 4, getHeight());
                g2.dispose();
            }
        };
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(20, 25, 20, 20));

        JLabel lblTitle = new JLabel(tieuDe);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(mauHong);

        JLabel lblValue = new JLabel(giaTri);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(mauNhan);

        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(lblValue, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel taoVungBiDo(List<Double> pointsData) {
        JPanel pnl = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                g2.setColor(new Color(240, 240, 240));
                int padding = 40;
                int chartW = getWidth() - padding * 2;
                int chartH = getHeight() - padding * 2;
                
                for(int i=0; i<=5; i++) {
                    int y = padding + i * (chartH / 5);
                    g2.drawLine(padding, y, getWidth() - padding, y);
                }
                
                if (pointsData == null || pointsData.isEmpty()) {
                    g2.dispose();
                    return;
                }

                double maxVal = 0;
                for (Double d : pointsData) if (d > maxVal) maxVal = d;
                if (maxVal == 0) maxVal = 1000000; // Tránh chia cho 0

                int stepX = chartW / (pointsData.size() - 1);
                
                g2.setColor(mauHong);
                g2.setStroke(new BasicStroke(3f));
                
                Polygon p = new Polygon();
                p.addPoint(padding, padding + chartH); 
                
                for(int i=0; i < pointsData.size(); i++) {
                    int x1 = padding + i * stepX;
                    int y1 = (int) (padding + chartH - (pointsData.get(i) * chartH / (maxVal * 1.2)));
                    
                    if (i > 0) {
                        int xPrev = padding + (i - 1) * stepX;
                        int yPrev = (int) (padding + chartH - (pointsData.get(i-1) * chartH / (maxVal * 1.2)));
                        g2.drawLine(xPrev, yPrev, x1, y1);
                    }
                    
                    g2.fillOval(x1 - 4, y1 - 4, 8, 8); 
                    p.addPoint(x1, y1);
                }
                p.addPoint(padding + chartW, padding + chartH); 
                
                g2.setPaint(new GradientPaint(0, padding, mauHongNhat, 0, padding + chartH, new Color(255,255,255,0)));
                g2.fillPolygon(p);
                
                g2.dispose();
            }
        };
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("BIỂU ĐỒ DOANH THU 7 NGÀY GẦN NHẤT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(mauHong);
        pnl.add(lblTitle, BorderLayout.NORTH);
        
        return pnl;
    }

    private JPanel taoVungThongTinQuanLy(int ckPercent, int tmPercent) {
        JPanel pnl = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                int avatarSize = 65;
                int xAvatar = 20;
                int yAvatar = 20;
                g2.setColor(mauHong); 
                g2.fillOval(xAvatar, yAvatar, avatarSize, avatarSize);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("QL", xAvatar + (avatarSize - fm.stringWidth("QL")) / 2, yAvatar + (avatarSize - fm.getHeight()) / 2 + fm.getAscent());
                
                g2.setColor(new Color(235, 235, 235));
                g2.drawLine(240, 20, 240, getHeight() - 20);
                
                g2.dispose();
            }
        };
        pnl.setOpaque(false);
        pnl.setLayout(null);

        JLabel lblName = new JLabel("Admin Hệ Thống");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(mauHong);
        lblName.setBounds(100, 25, 140, 20);
        pnl.add(lblName);
        
        JLabel lblRole = new JLabel("admin@spring.com");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRole.setForeground(new Color(150, 150, 150));
        lblRole.setBounds(100, 45, 140, 20);
        pnl.add(lblRole);

        JPanel pnlPhone = taoDongInfo("SĐT:", "0901234567");
        pnlPhone.setBounds(20, 95, 200, 20);
        pnl.add(pnlPhone);
        
        JPanel pnlVaiTro = taoDongInfo("Quyền:", "Toàn quyền");
        pnlVaiTro.setBounds(20, 125, 200, 20);
        pnl.add(pnlVaiTro);

        JLabel lblThanhToan = new JLabel("CƠ CẤU THANH TOÁN (KỲ NÀY)");
        lblThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblThanhToan.setForeground(mauHong);
        lblThanhToan.setBounds(260, 20, 220, 20);
        pnl.add(lblThanhToan);

        JPanel pnlCK = taoTienTrinh("Chuyển khoản / Momo (" + ckPercent + "%)", ckPercent, mauXanh);
        pnlCK.setBounds(260, 55, 210, 40);
        pnl.add(pnlCK);
        
        JPanel pnlTM = taoTienTrinh("Tiền mặt (" + tmPercent + "%)", tmPercent, mauHong); 
        pnlTM.setBounds(260, 105, 210, 40);
        pnl.add(pnlTM);

        return pnl;
    }
    
    private JPanel taoDongInfo(String title, String value) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblT.setForeground(new Color(100, 100, 100));
        JLabel lblV = new JLabel(value);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblV.setForeground(new Color(50, 50, 50));
        pnl.add(lblT, BorderLayout.WEST);
        pnl.add(lblV, BorderLayout.EAST);
        return pnl;
    }
    
    private JPanel taoTienTrinh(String title, int phanTram, Color color) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(60, 60, 60));
        pnl.add(lbl, BorderLayout.NORTH);

        JProgressBar pb = new JProgressBar(0, 100);
        pb.setValue(phanTram);
        pb.setForeground(color);
        pb.setBackground(new Color(240, 240, 240));
        pb.setBorderPainted(false);
        pnl.add(pb, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel taoVungHoaDon(List<Object[]> dataList) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        JLabel lblTitle = new JLabel("GIAO DỊCH GẦN NHẤT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(mauHong);
        lblTitle.setBorder(new EmptyBorder(15, 20, 10, 20));
        pnl.add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Mã HĐ", "Khách hàng", "Số tiền", "Trạng thái"};
        Object[][] data = new Object[dataList.size()][4];
        for (int i = 0; i < dataList.size(); i++) data[i] = dataList.get(i);

        JTable tbl = new JTable(new DefaultTableModel(data, columns)) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tbl.setRowHeight(28);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setSelectionBackground(mauHongNhat);
        tbl.setSelectionForeground(mauHong);
        tbl.getTableHeader().setBackground(Color.WHITE);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        scroll.getViewport().setBackground(Color.WHITE);
        pnl.add(scroll, BorderLayout.CENTER);

        JPanel pnlWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        pnlWrapper.setOpaque(false);
        pnlWrapper.add(pnl, BorderLayout.CENTER);
        return pnlWrapper;
    }

    private void capNhatDuLieu() {
        String tuNgay  = txtTuNgay.getText();
        String denNgay = txtDenNgay.getText();
        String chiNhanh = (String) cbxChiNhanh.getSelectedItem();
        String loaiDT   = (String) cbxLoaiDichVu.getSelectedItem();

        TongQuanDTO dto = controller.layDuLieu(tuNgay, denNgay, chiNhanh, loaiDT);

        int ckPercent = dto.getCoCauThanhToan().getOrDefault("CK", 0);
        int tmPercent = dto.getCoCauThanhToan().getOrDefault("TM", 0);

        veGiaoDienBaoCao(
                dto.getDoanhThuThuc(),
                dto.getTruocGiam(),
                dto.getChietKhau(),
                dto.getDoanhThu7Ngay(),
                dto.getGiaoDichGanNhat(),
                ckPercent, tmPercent
        );
    }

    private void veGiaoDienBaoCao(double doanhThuThuc, double truocGiam, double chietKhau,
                                   List<Double> chartData, List<Object[]> giaoDich,
                                   int ckPercent, int tmPercent) {
        pnCard1.removeAll();
        pnCard1.add(taoTheThongKe("DOANH THU THỰC TẾ", formatTien.format(doanhThuThuc), mauHong), BorderLayout.CENTER);

        pnCard2.removeAll();
        pnCard2.add(taoTheThongKe("TRƯỚC GIẢM GIÁ", formatTien.format(truocGiam), mauXanh), BorderLayout.CENTER);

        pnCard3.removeAll();
        pnCard3.add(taoTheThongKe("TỔNG CHIẾT KHẤU", formatTien.format(chietKhau), mauCam), BorderLayout.CENTER);

        pnChart.removeAll();
        pnChart.add(taoVungBiDo(chartData), BorderLayout.CENTER);

        pnManagerInfo.removeAll();
        pnManagerInfo.add(taoVungThongTinQuanLy(ckPercent, tmPercent), BorderLayout.CENTER);

        pnInvoices.removeAll();
        pnInvoices.add(taoVungHoaDon(giaoDich), BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }


    private void btnXemBaoCaoActionPerformed(java.awt.event.ActionEvent evt) {                                             
        String tuNgay = txtTuNgay.getText().trim();
        String denNgay = txtDenNgay.getText().trim();

        if (tuNgay.isEmpty() || denNgay.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Từ ngày - Đến ngày!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        capNhatDuLieu();
        JOptionPane.showMessageDialog(this, "Đã cập nhật dữ liệu Tổng quan mới nhất!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }                                            

    private void btnXuatCSVActionPerformed(java.awt.event.ActionEvent evt) {                                             
        String tuNgay = txtTuNgay.getText().trim();
        String denNgay = txtDenNgay.getText().trim();
        String chiNhanh = (String) cbxChiNhanh.getSelectedItem();
        String loaiDT = (String) cbxLoaiDichVu.getSelectedItem();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }

            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(filePath), java.nio.charset.StandardCharsets.UTF_8))) {
                // Add UTF-8 BOM for Excel to read correctly
                bw.write('\ufeff');
                bw.write("Mã Hóa Đơn,Khách Hàng,Ngày Lập,Tổng Tiền,Thành Tiền,Phương Thức,Trạng Thái\n");

                List<Object[]> dsHoaDon = controller.layDanhSachHoaDonTheoDieuKien(tuNgay, denNgay, chiNhanh, loaiDT);
                for (Object[] row : dsHoaDon) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < row.length; i++) {
                        String value = row[i] != null ? row[i].toString() : "";
                        // Escape quotes and commas
                        if (value.contains(",") || value.contains("\"")) {
                            value = "\"" + value.replace("\"", "\"\"") + "\"";
                        }
                        sb.append(value);
                        if (i < row.length - 1) sb.append(",");
                    }
                    bw.write(sb.toString() + "\n");
                }

                JOptionPane.showMessageDialog(this, "Xuất dữ liệu CSV thành công!\n" + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }                                          
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        pnFilter = new javax.swing.JPanel();
        lblTuNgay = new javax.swing.JLabel();
        txtTuNgay = new javax.swing.JTextField();
        lblDenNgay = new javax.swing.JLabel();
        txtDenNgay = new javax.swing.JTextField();
        lblChiNhanh = new javax.swing.JLabel();
        cbxChiNhanh = new javax.swing.JComboBox<>();
        lblLoaiDichVu = new javax.swing.JLabel();
        cbxLoaiDichVu = new javax.swing.JComboBox<>();
        btnXemBaoCao = new javax.swing.JButton();
        btnXuatCSV = new javax.swing.JButton();
        pnCard1 = new javax.swing.JPanel();
        pnCard2 = new javax.swing.JPanel();
        pnCard3 = new javax.swing.JPanel();
        pnChart = new javax.swing.JPanel();
        pnManagerInfo = new javax.swing.JPanel();
        pnInvoices = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        pnMain.setBackground(new java.awt.Color(254, 248, 250));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 640));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("TỔNG QUAN & BÁO CÁO HOẠT ĐỘNG");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 50);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 50);

        pnFilter.setBackground(new java.awt.Color(255, 255, 255));
        pnFilter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnFilter.setLayout(null);

        lblTuNgay.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTuNgay.setText("Từ ngày:");
        pnFilter.add(lblTuNgay);
        lblTuNgay.setBounds(20, 5, 100, 18);

        txtTuNgay.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnFilter.add(txtTuNgay);
        txtTuNgay.setBounds(20, 25, 120, 28);

        lblDenNgay.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDenNgay.setText("Đến ngày:");
        pnFilter.add(lblDenNgay);
        lblDenNgay.setBounds(155, 5, 100, 18);

        txtDenNgay.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnFilter.add(txtDenNgay);
        txtDenNgay.setBounds(155, 25, 120, 28);

        lblChiNhanh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChiNhanh.setText("Chi nhánh:");
        pnFilter.add(lblChiNhanh);
        lblChiNhanh.setBounds(290, 5, 100, 18);

        cbxChiNhanh.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả chi nhánh", "CN001 - Quận 1", "CN002 - Thủ Đức" }));
        pnFilter.add(cbxChiNhanh);
        cbxChiNhanh.setBounds(290, 25, 170, 28);

        lblLoaiDichVu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoaiDichVu.setText("Loại doanh thu:");
        pnFilter.add(lblLoaiDichVu);
        lblLoaiDichVu.setBounds(475, 5, 150, 18);

        cbxLoaiDichVu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả", "Thuê không gian (Booking)", "Dịch vụ ăn uống (F&B)", "Gia hạn phiên" }));
        pnFilter.add(cbxLoaiDichVu);
        cbxLoaiDichVu.setBounds(475, 25, 180, 28);

        btnXemBaoCao.setBackground(new java.awt.Color(235, 94, 141));
        btnXemBaoCao.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXemBaoCao.setForeground(new java.awt.Color(255, 255, 255));
        btnXemBaoCao.setText("Lọc Kết Quả");
        btnXemBaoCao.addActionListener(this::btnXemBaoCaoActionPerformed);
        pnFilter.add(btnXemBaoCao);
        btnXemBaoCao.setBounds(690, 15, 150, 35);

        btnXuatCSV.setBackground(new java.awt.Color(33, 157, 86));
        btnXuatCSV.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXuatCSV.setForeground(new java.awt.Color(255, 255, 255));
        btnXuatCSV.setText("Xuất CSV");
        btnXuatCSV.addActionListener(this::btnXuatCSVActionPerformed);
        pnFilter.add(btnXuatCSV);
        btnXuatCSV.setBounds(850, 15, 140, 35);

        pnMain.add(pnFilter);
        pnFilter.setBounds(20, 65, 1010, 60);

        pnCard1.setBackground(new java.awt.Color(255, 255, 255));
        pnCard1.setLayout(new java.awt.BorderLayout());
        pnMain.add(pnCard1);
        pnCard1.setBounds(20, 140, 320, 100);

        pnCard2.setBackground(new java.awt.Color(255, 255, 255));
        pnCard2.setLayout(new java.awt.BorderLayout());
        pnMain.add(pnCard2);
        pnCard2.setBounds(365, 140, 320, 100);

        pnCard3.setBackground(new java.awt.Color(255, 255, 255));
        pnCard3.setLayout(new java.awt.BorderLayout());
        pnMain.add(pnCard3);
        pnCard3.setBounds(710, 140, 320, 100);

        pnChart.setBackground(new java.awt.Color(255, 255, 255));
        pnChart.setLayout(new java.awt.BorderLayout());
        pnMain.add(pnChart);
        pnChart.setBounds(20, 255, 500, 365);

        pnManagerInfo.setBackground(new java.awt.Color(255, 255, 255));
        pnManagerInfo.setLayout(new java.awt.BorderLayout());
        pnMain.add(pnManagerInfo);
        pnManagerInfo.setBounds(540, 255, 490, 175);

        pnInvoices.setBackground(new java.awt.Color(255, 255, 255));
        pnInvoices.setLayout(new java.awt.BorderLayout());
        pnMain.add(pnInvoices);
        pnInvoices.setBounds(540, 445, 490, 175);

        add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnXemBaoCao;
    private javax.swing.JButton btnXuatCSV;
    private javax.swing.JComboBox<String> cbxChiNhanh;
    private javax.swing.JComboBox<String> cbxLoaiDichVu;
    private javax.swing.JLabel lblChiNhanh;
    private javax.swing.JLabel lblDenNgay;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblLoaiDichVu;
    private javax.swing.JLabel lblTuNgay;
    private javax.swing.JPanel pnCard1;
    private javax.swing.JPanel pnCard2;
    private javax.swing.JPanel pnCard3;
    private javax.swing.JPanel pnChart;
    private javax.swing.JPanel pnFilter;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnInvoices;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnManagerInfo;
    private javax.swing.JTextField txtDenNgay;
    private javax.swing.JTextField txtTuNgay;
    // End of variables declaration//GEN-END:variables
}
