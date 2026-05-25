package com.wms.view.TrangChuQuanLy.TongQuan;

import com.wms.controller.TrangChuQuanLy.TongQuan.TongQuanController;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.TongQuanDTO;
import com.wms.service.TrangChuQuanLy.TongQuan.TongQuanService;
import com.wms.util.BaoCaoCsvExporter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TongQuanForm extends JPanel {

    private final TongQuanController controller = new TongQuanController();
    private final DecimalFormat formatTien = new DecimalFormat("#,### VNĐ",
            java.text.DecimalFormatSymbols.getInstance(new java.util.Locale("vi", "VN")));
    private final Color mauHong = Color.decode("#EB5E8D");
    private final Color mauXanh = Color.decode("#5E8DEB");
    private final Color mauCam = Color.decode("#EB8D5E");

    private JTextField txtTuNgay;
    private JTextField txtDenNgay;
    private JComboBox<String> cbxChiNhanh;
    private JComboBox<String> cbxLoaiBaoCao;
    private JComboBox<String> cbxLoaiDichVu;
    private JLabel lblDoanhThuThuc;
    private JLabel lblTruocGiam;
    private JLabel lblChietKhau;
    private JLabel lblCoCauThanhToan;
    private ChartPanel chartPanel;
    private DefaultTableModel tableModel;
    private TongQuanDTO duLieuHienTai;

    public TongQuanForm() {
        setLayout(new BorderLayout());
        setBackground(new Color(254, 248, 250));
        initComponents();
        initDefaultDates();
        loadComboChiNhanh();
        capNhatTrangThaiLoaiDoanhThu();
        capNhatDuLieu();
    }

    private void initComponents() {
        JPanel pnMain = new JPanel(new BorderLayout(16, 16));
        pnMain.setBackground(new Color(254, 248, 250));
        pnMain.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel title = new JLabel("TỔNG QUAN & BÁO CÁO DOANH THU", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(mauHong);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setPreferredSize(new Dimension(1050, 52));
        pnMain.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);
        content.add(taoBoLoc(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);
        body.add(taoVungTheThongKe(), BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);
        chartPanel = new ChartPanel();
        lower.add(wrapCard("BIỂU ĐỒ DOANH THU 7 NGÀY GẦN NHẤT", chartPanel));
        lower.add(taoVungGiaoDich());
        body.add(lower, BorderLayout.CENTER);

        content.add(body, BorderLayout.CENTER);
        pnMain.add(content, BorderLayout.CENTER);
        add(pnMain, BorderLayout.CENTER);
    }

    private JPanel taoBoLoc() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(12, 14, 14, 14)));

        txtTuNgay = new JTextField();
        txtDenNgay = new JTextField();
        cbxChiNhanh = new JComboBox<>();
        cbxLoaiBaoCao = new JComboBox<>(new String[]{
                TongQuanService.BAO_CAO_DOANH_THU,
                TongQuanService.BAO_CAO_NHAP_KHO_DICH_VU,
                TongQuanService.BAO_CAO_CHI_PHI_NHAP_KHO,
                TongQuanService.BAO_CAO_DICH_VU_BAN_CHAY,
                TongQuanService.BAO_CAO_LOI_NHUAN_GOP_UOC_TINH
        });
        cbxLoaiBaoCao.addActionListener(e -> capNhatTrangThaiLoaiDoanhThu());

        cbxLoaiDichVu = new JComboBox<>(new String[]{
                "Tất cả",
                "Thuê không gian",
                "Dịch vụ ăn uống",
                "Gia hạn phiên"
        });

        JButton btnXemBaoCao = button("Lọc kết quả", mauXanh);
        btnXemBaoCao.addActionListener(e -> xemBaoCao());

        JButton btnXuatCSV = button("Xuất CSV", mauHong);
        btnXuatCSV.addActionListener(e -> xuatCsv());

        JButton btnXuatPDF = button("Xuất báo cáo PDF", new Color(80, 120, 80));
        btnXuatPDF.addActionListener(e -> xuatBaoCaoPdf());

        for (JButton b : new JButton[]{btnXemBaoCao, btnXuatCSV, btnXuatPDF}) {
            b.setPreferredSize(new Dimension(170, 36));
            b.setMinimumSize(new Dimension(140, 36));
            b.setToolTipText(b.getText());
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addFilterField(panel, gbc, 0, "Từ ngày", txtTuNgay);
        addFilterField(panel, gbc, 1, "Đến ngày", txtDenNgay);
        addFilterField(panel, gbc, 2, "Chi nhánh", cbxChiNhanh);
        addFilterField(panel, gbc, 3, "Loại báo cáo", cbxLoaiBaoCao);
        addFilterField(panel, gbc, 4, "Loại doanh thu", cbxLoaiDichVu);

        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        panel.add(btnXemBaoCao, gbc);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        actions.add(btnXuatCSV);
        actions.add(btnXuatPDF);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        panel.add(actions, gbc);
        return panel;
    }

    private void addFilterField(JPanel panel, GridBagConstraints gbc, int x, String text, java.awt.Component input) {
        gbc.gridx = x;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        panel.add(label(text), gbc);

        gbc.gridy = 1;
        panel.add(input, gbc);
    }

    private JPanel taoVungTheThongKe() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);

        lblDoanhThuThuc = cardValue();
        lblTruocGiam = cardValue();
        lblChietKhau = cardValue();
        lblCoCauThanhToan = cardValue();

        cards.add(statCard("Doanh thu thực tế", lblDoanhThuThuc, mauHong));
        cards.add(statCard("Trước giảm giá", lblTruocGiam, mauXanh));
        cards.add(statCard("Tổng chiết khấu", lblChietKhau, mauCam));
        cards.add(statCard("Cơ cấu thanh toán", lblCoCauThanhToan, new Color(76, 175, 80)));
        return cards;
    }

    private JPanel taoVungGiaoDich() {
        tableModel = new DefaultTableModel(new String[]{"Mã HĐ", "Khách hàng", "Số tiền", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(190);
        return wrapCard("GIAO DỊCH GẦN NHẤT", new JScrollPane(table));
    }

    private JPanel wrapCard(String title, java.awt.Component child) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(14, 16, 16, 16)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(mauHong);
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(child, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, accent),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(75, 75, 75));
        value.setForeground(accent);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private JLabel cardValue() {
        JLabel label = new JLabel("0 VNĐ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        return label;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        return button;
    }

    private void initDefaultDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String today = sdf.format(new Date());
        txtTuNgay.setText(today);
        txtDenNgay.setText(today);
    }

    private void loadComboChiNhanh() {
        cbxChiNhanh.removeAllItems();
        cbxChiNhanh.addItem("Tất cả chi nhánh");
        for (ChiNhanhDTO cn : controller.layDanhSachChiNhanh()) {
            cbxChiNhanh.addItem(cn.getMaCN() + " - " + cn.getTenCN());
        }
    }

    private void xemBaoCao() {
        if (!boLocHopLe()) {
            return;
        }
        capNhatDuLieu();
        JOptionPane.showMessageDialog(this, "Đã cập nhật dữ liệu tổng quan.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void capNhatDuLieu() {
        String tuNgay = txtTuNgay.getText().trim();
        String denNgay = txtDenNgay.getText().trim();
        String chiNhanh = (String) cbxChiNhanh.getSelectedItem();
        String loaiDT = (String) cbxLoaiDichVu.getSelectedItem();

        duLieuHienTai = controller.layDuLieu(tuNgay, denNgay, chiNhanh, loaiDT);

        int ckPercent = duLieuHienTai.getCoCauThanhToan().getOrDefault("CK", 0);
        int tmPercent = duLieuHienTai.getCoCauThanhToan().getOrDefault("TM", 0);

        lblDoanhThuThuc.setText(formatTien.format(duLieuHienTai.getDoanhThuThuc()));
        lblTruocGiam.setText(formatTien.format(duLieuHienTai.getTruocGiam()));
        lblChietKhau.setText(formatTien.format(duLieuHienTai.getChietKhau()));
        lblCoCauThanhToan.setText("CK " + ckPercent + "% / TM " + tmPercent + "%");

        chartPanel.setData(duLieuHienTai.getDoanhThu7Ngay());

        tableModel.setRowCount(0);
        for (Object[] row : duLieuHienTai.getGiaoDichGanNhat()) {
            tableModel.addRow(row);
        }
    }

    private void xuatCsv() {
        if (!boLocHopLe()) {
            return;
        }

        String loaiBaoCao = (String) cbxLoaiBaoCao.getSelectedItem();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo CSV");
        fileChooser.setSelectedFile(new File(tenFileBaoCao(loaiBaoCao, "csv")));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(fileChooser.getSelectedFile(), ".csv");
            try {
                DuLieuBaoCaoTongQuatDTO duLieu = taoDuLieuBaoCao();
                BaoCaoCsvExporter.xuatCsv(file, duLieu);
                JOptionPane.showMessageDialog(this, "Xuất báo cáo CSV thành công!\n" + file.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                com.wms.util.MessageUtil.showError(this, "Lỗi khi xuất CSV.", e);
            }
        }
    }

    private void xuatBaoCaoPdf() {
        if (!boLocHopLe()) {
            return;
        }

        String loaiBaoCao = (String) cbxLoaiBaoCao.getSelectedItem();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo PDF");
        fileChooser.setSelectedFile(new File(tenFileBaoCao(loaiBaoCao, "pdf")));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(fileChooser.getSelectedFile(), ".pdf");
            try {
                controller.xuatBaoCaoPdf(
                        file,
                        loaiBaoCao,
                        txtTuNgay.getText().trim(),
                        txtDenNgay.getText().trim(),
                        (String) cbxChiNhanh.getSelectedItem(),
                        (String) cbxLoaiDichVu.getSelectedItem(),
                        nguoiXuatHienTai()
                );
                JOptionPane.showMessageDialog(this, "Xuất báo cáo PDF thành công!\n" + file.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                com.wms.util.MessageUtil.showError(this, "Lỗi khi xuất báo cáo PDF.", e);
            }
        }
    }

    private DuLieuBaoCaoTongQuatDTO taoDuLieuBaoCao() {
        return controller.taoDuLieuBaoCao(
                (String) cbxLoaiBaoCao.getSelectedItem(),
                txtTuNgay.getText().trim(),
                txtDenNgay.getText().trim(),
                (String) cbxChiNhanh.getSelectedItem(),
                (String) cbxLoaiDichVu.getSelectedItem(),
                nguoiXuatHienTai()
        );
    }

    private boolean boLocHopLe() {
        if (txtTuNgay.getText().trim().isEmpty() || txtDenNgay.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Từ ngày - Đến ngày.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void capNhatTrangThaiLoaiDoanhThu() {
        if (cbxLoaiDichVu == null || cbxLoaiBaoCao == null) {
            return;
        }
        boolean laBaoCaoDoanhThu = TongQuanService.BAO_CAO_DOANH_THU.equals(cbxLoaiBaoCao.getSelectedItem());
        cbxLoaiDichVu.setEnabled(laBaoCaoDoanhThu);
        if (!laBaoCaoDoanhThu) {
            cbxLoaiDichVu.setSelectedIndex(0);
        }
    }

    private String nguoiXuatHienTai() {
        String userName = System.getProperty("user.name");
        return userName == null || userName.isBlank() ? "Người dùng hệ thống" : userName;
    }

    private String tenFileBaoCao(String loaiBaoCao, String extension) {
        String ngay = new SimpleDateFormat("ddMMyyyy").format(new Date());
        String slug;
        if (TongQuanService.BAO_CAO_NHAP_KHO_DICH_VU.equals(loaiBaoCao)) {
            slug = "NhapKhoDichVu";
        } else if (TongQuanService.BAO_CAO_CHI_PHI_NHAP_KHO.equals(loaiBaoCao)) {
            slug = "ChiPhiNhapKho";
        } else if (TongQuanService.BAO_CAO_DICH_VU_BAN_CHAY.equals(loaiBaoCao)) {
            slug = "DichVuBanChay";
        } else if (TongQuanService.BAO_CAO_LOI_NHUAN_GOP_UOC_TINH.equals(loaiBaoCao)) {
            slug = "LoiNhuanGopUocTinh";
        } else {
            slug = "DoanhThu";
        }
        return "BaoCao_" + slug + "_" + ngay + "." + extension;
    }

    private File ensureExtension(File file, String extension) {
        if (file.getAbsolutePath().toLowerCase().endsWith(extension)) {
            return file;
        }
        return new File(file.getAbsolutePath() + extension);
    }

    private class ChartPanel extends JPanel {
        private List<Double> data = List.of();

        ChartPanel() {
            setPreferredSize(new Dimension(480, 300));
            setBackground(Color.WHITE);
        }

        void setData(List<Double> data) {
            this.data = data == null ? List.of() : data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 36;
            int chartW = Math.max(1, getWidth() - padding * 2);
            int chartH = Math.max(1, getHeight() - padding * 2);

            g2.setColor(new Color(235, 235, 235));
            for (int i = 0; i <= 4; i++) {
                int y = padding + i * (chartH / 4);
                g2.drawLine(padding, y, padding + chartW, y);
            }

            if (data.isEmpty()) {
                g2.dispose();
                return;
            }

            double max = data.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            if (max <= 0) {
                max = 1;
            }
            int stepX = data.size() == 1 ? chartW : chartW / (data.size() - 1);
            int prevX = -1;
            int prevY = -1;

            g2.setColor(mauHong);
            g2.setStroke(new java.awt.BasicStroke(3f));
            for (int i = 0; i < data.size(); i++) {
                int x = padding + i * stepX;
                int y = (int) (padding + chartH - (data.get(i) * chartH / (max * 1.15)));
                if (prevX >= 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                g2.fillOval(x - 4, y - 4, 8, 8);
                prevX = x;
                prevY = y;
            }
            g2.dispose();
        }
    }
}
