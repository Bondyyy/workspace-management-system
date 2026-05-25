package com.wms.view.TrangChuQuanLy.TongQuan;

import com.wms.controller.TrangChuQuanLy.TongQuan.TongQuanController;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DongBaoCaoTongQuatDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;
import com.wms.service.TrangChuQuanLy.TongQuan.TongQuanService;
import com.wms.util.BaoCaoCsvExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class BaoCaoLuongNhanVienForm extends JPanel {

    private final TongQuanController controller = new TongQuanController();
    private final Color mauHong = Color.decode("#EB5E8D");
    private final Color mauXanh = Color.decode("#5E8DEB");

    private JTextField txtTuNgay;
    private JTextField txtDenNgay;
    private JComboBox<String> cbxChiNhanh;
    private JComboBox<String> cbxLoaiNhanVien;
    
    private DefaultTableModel tableModel;
    private JLabel lblTongNhanVien;
    private JLabel lblTongLieuCoBan;
    private JLabel lblTongPhaiTra;

    public BaoCaoLuongNhanVienForm() {
        setLayout(new BorderLayout());
        setBackground(new Color(254, 248, 250));
        initComponents();
        initDefaultDates();
        loadComboChiNhanh();
        capNhatDuLieu();
    }

    private void initComponents() {
        JPanel pnMain = new JPanel(new BorderLayout(16, 16));
        pnMain.setBackground(new Color(254, 248, 250));
        pnMain.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);
        content.add(taoBoLoc(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);
        body.add(taoVungTheThongKe(), BorderLayout.NORTH);
        body.add(taoVungBang(), BorderLayout.CENTER);

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
        com.wms.util.DateInputUtil.attachDatePicker(txtTuNgay);
        com.wms.util.DateInputUtil.attachDatePicker(txtDenNgay);
        cbxChiNhanh = new JComboBox<>();
        cbxLoaiNhanVien = new JComboBox<>(new String[]{"Tất cả", "Quản lý", "Nhân viên"});

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
        addFilterField(panel, gbc, 3, "Loại nhân viên", cbxLoaiNhanVien);

        gbc.gridx = 4;
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
        gbc.gridwidth = 5;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        panel.add(actions, gbc);
        return panel;
    }

    private void addFilterField(JPanel panel, GridBagConstraints gbc, int x, String text, Component input) {
        gbc.gridx = x;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lbl, gbc);

        gbc.gridy = 1;
        panel.add(input, gbc);
    }

    private JPanel taoVungTheThongKe() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);

        lblTongNhanVien = cardValue();
        lblTongLieuCoBan = cardValue();
        lblTongPhaiTra = cardValue();

        cards.add(statCard("Tổng nhân viên trả lương", lblTongNhanVien, mauHong));
        cards.add(statCard("Tổng lương cơ bản", lblTongLieuCoBan, mauXanh));
        cards.add(statCard("Tổng lương phải trả", lblTongPhaiTra, new Color(76, 175, 80)));
        return cards;
    }

    private JPanel taoVungBang() {
        tableModel = new DefaultTableModel(new String[]{
            "Mã NV", "Họ tên (Loại NV)", "Chi nhánh", "Ngày vào làm", "Lương CB", "Phụ cấp & Thưởng", "Số ngày TL", "Tổng lương"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(14, 16, 16, 16)));

        JLabel lblTitle = new JLabel("CHI TIẾT LƯƠNG NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(mauHong);
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
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
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
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
        txtTuNgay.setText("01" + today.substring(2)); // Đầu tháng hiện tại
        txtDenNgay.setText(today);
    }

    private void loadComboChiNhanh() {
        cbxChiNhanh.removeAllItems();
        cbxChiNhanh.addItem("Tất cả chi nhánh");
        for (ChiNhanhDTO cn : controller.layDanhSachChiNhanh()) {
            cbxChiNhanh.addItem(cn.getMaCN() + " - " + cn.getTenCN());
        }
    }

    private boolean boLocHopLe() {
        try {
            LocalDate tuNgay = com.wms.util.DateInputUtil.requireDate(
                    txtTuNgay.getText(), "Từ ngày", "Vui lòng nhập từ ngày.");
            LocalDate denNgay = com.wms.util.DateInputUtil.requireDate(
                    txtDenNgay.getText(), "Đến ngày", "Vui lòng nhập đến ngày.");
            if (tuNgay.isAfter(denNgay)) {
                JOptionPane.showMessageDialog(this, "Từ ngày không được sau đến ngày.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            txtTuNgay.setText(com.wms.util.DateInputUtil.formatDate(tuNgay));
            txtDenNgay.setText(com.wms.util.DateInputUtil.formatDate(denNgay));
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void xemBaoCao() {
        if (!boLocHopLe()) return;
        capNhatDuLieu();
        JOptionPane.showMessageDialog(this, "Đã cập nhật dữ liệu lương nhân viên.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void capNhatDuLieu() {
        DuLieuBaoCaoTongQuatDTO duLieu = taoDuLieuBaoCao();
        lblTongNhanVien.setText(duLieu.getTongGiaTri1());
        lblTongLieuCoBan.setText(duLieu.getTongGiaTri2());
        lblTongPhaiTra.setText(duLieu.getTongGiaTri3());

        tableModel.setRowCount(0);
        for (DongBaoCaoTongQuatDTO row : duLieu.getDanhSachDongBaoCao()) {
            tableModel.addRow(new Object[]{
                row.getCot1(), row.getCot2(), row.getCot3(), row.getCot4(),
                row.getCot5(), row.getCot6(), row.getCot7(), row.getCot8()
            });
        }
    }

    private DuLieuBaoCaoTongQuatDTO taoDuLieuBaoCao() {
        String tuNgay = txtTuNgay.getText().trim();
        String denNgay = txtDenNgay.getText().trim();
        String chiNhanh = (String) cbxChiNhanh.getSelectedItem();
        String loaiNV = (String) cbxLoaiNhanVien.getSelectedItem();
        
        return controller.taoDuLieuBaoCao(
                TongQuanService.BAO_CAO_TRA_LUONG_NHAN_VIEN,
                tuNgay, denNgay, chiNhanh, loaiNV, nguoiXuatHienTai()
        );
    }

    private void xuatCsv() {
        if (!boLocHopLe()) return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo CSV");
        fileChooser.setSelectedFile(new File(tenFileBaoCao("csv")));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(fileChooser.getSelectedFile(), ".csv");
            try {
                BaoCaoCsvExporter.xuatCsv(file, taoDuLieuBaoCao());
                JOptionPane.showMessageDialog(this, "Xuất báo cáo CSV thành công!\n" + file.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                com.wms.util.MessageUtil.showError(this, "Lỗi khi xuất CSV.", e);
            }
        }
    }

    private void xuatBaoCaoPdf() {
        if (!boLocHopLe()) return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo PDF");
        fileChooser.setSelectedFile(new File(tenFileBaoCao("pdf")));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(fileChooser.getSelectedFile(), ".pdf");
            try {
                controller.xuatBaoCaoPdf(
                        file,
                        TongQuanService.BAO_CAO_TRA_LUONG_NHAN_VIEN,
                        txtTuNgay.getText().trim(),
                        txtDenNgay.getText().trim(),
                        (String) cbxChiNhanh.getSelectedItem(),
                        (String) cbxLoaiNhanVien.getSelectedItem(),
                        nguoiXuatHienTai()
                );
                JOptionPane.showMessageDialog(this, "Xuất báo cáo PDF thành công!\n" + file.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                com.wms.util.MessageUtil.showError(this, "Lỗi khi xuất báo cáo PDF.", e);
            }
        }
    }

    private String nguoiXuatHienTai() {
        String userName = System.getProperty("user.name");
        return userName == null || userName.isBlank() ? "Người dùng hệ thống" : userName;
    }

    private String tenFileBaoCao(String extension) {
        String ngay = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return "BaoCaoLuongNhanVien_" + ngay + "." + extension;
    }

    private File ensureExtension(File file, String extension) {
        if (file.getAbsolutePath().toLowerCase().endsWith(extension)) {
            return file;
        }
        return new File(file.getAbsolutePath() + extension);
    }
}
