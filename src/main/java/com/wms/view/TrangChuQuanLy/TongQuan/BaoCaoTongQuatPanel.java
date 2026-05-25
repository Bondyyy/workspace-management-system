package com.wms.view.TrangChuQuanLy.TongQuan;

import com.wms.controller.TrangChuQuanLy.TongQuan.TongQuanController;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DongBaoCaoTongQuatDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;
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
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BaoCaoTongQuatPanel extends JPanel {

    private static final Color MAU_HONG = Color.decode("#EB5E8D");
    private static final Color MAU_XANH = Color.decode("#5E8DEB");
    private static final Color MAU_XANH_LA = new Color(76, 175, 80);

    private final TongQuanController controller = new TongQuanController();
    private final String loaiBaoCao;
    private final String moTa;
    private final boolean hienLoaiDoanhThu;
    private final boolean hienLoaiNhanVien;

    private JTextField txtTuNgay;
    private JTextField txtDenNgay;
    private JComboBox<String> cbxChiNhanh;
    private JComboBox<String> cbxLoaiPhu;
    private JButton btnTaiLai;
    private JButton btnXuatCsv;
    private JButton btnXuatPdf;
    private JLabel lblTrangThai;
    private JLabel lblTong1;
    private JLabel lblTong2;
    private JLabel lblTong3;
    private JLabel lblNhanTong1;
    private JLabel lblNhanTong2;
    private JLabel lblNhanTong3;
    private DefaultTableModel tableModel;
    private DuLieuBaoCaoTongQuatDTO duLieuHienTai;

    public BaoCaoTongQuatPanel(String loaiBaoCao, String moTa,
                               boolean hienLoaiDoanhThu, boolean hienLoaiNhanVien) {
        this.loaiBaoCao = loaiBaoCao;
        this.moTa = moTa;
        this.hienLoaiDoanhThu = hienLoaiDoanhThu;
        this.hienLoaiNhanVien = hienLoaiNhanVien;
        setLayout(new BorderLayout());
        setBackground(new Color(254, 248, 250));
        khoiTaoGiaoDien();
        initDefaultDates();
        loadComboChiNhanh();
        taiBaoCao();
    }

    private void khoiTaoGiaoDien() {
        JPanel main = new JPanel(new BorderLayout(14, 14));
        main.setBackground(new Color(254, 248, 250));
        main.setBorder(new EmptyBorder(16, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setOpaque(false);
        JLabel title = new JLabel(loaiBaoCao);
        title.setForeground(new Color(35, 30, 48));
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel subtitle = new JLabel(moTa);
        subtitle.setForeground(new Color(112, 104, 116));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        main.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setOpaque(false);
        content.add(taoBoLoc(), BorderLayout.NORTH);
        content.add(taoNoiDungBaoCao(), BorderLayout.CENTER);
        main.add(content, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

    private JPanel taoBoLoc() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(12, 14, 12, 14)));

        txtTuNgay = new JTextField();
        txtDenNgay = new JTextField();
        cbxChiNhanh = new JComboBox<>();
        cbxLoaiPhu = new JComboBox<>(layLuaChonLoaiPhu());
        cbxLoaiPhu.setEnabled(hienLoaiDoanhThu || hienLoaiNhanVien);

        btnTaiLai = button("Tải lại", MAU_XANH);
        btnTaiLai.addActionListener(e -> taiBaoCao());
        btnXuatCsv = button("Export CSV", MAU_HONG);
        btnXuatCsv.addActionListener(e -> xuatCsv());
        btnXuatPdf = button("Export PDF", MAU_XANH_LA);
        btnXuatPdf.addActionListener(e -> xuatPdf());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addFilterField(panel, gbc, 0, "Từ ngày", txtTuNgay);
        addFilterField(panel, gbc, 1, "Đến ngày", txtDenNgay);
        addFilterField(panel, gbc, 2, "Chi nhánh", cbxChiNhanh);
        addFilterField(panel, gbc, 3, hienLoaiNhanVien ? "Loại nhân viên" : "Loại doanh thu", cbxLoaiPhu);

        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);
        actions.add(btnTaiLai);
        actions.add(btnXuatCsv);
        actions.add(btnXuatPdf);
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        gbc.ipadx = 80;
        panel.add(actions, gbc);
        gbc.ipadx = 0;
        gbc.gridheight = 1;
        return panel;
    }

    private JPanel taoNoiDungBaoCao() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.add(taoVungTongQuan(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Dữ liệu"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel tableCard = new JPanel(new BorderLayout(8, 8));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(12, 14, 14, 14)));
        lblTrangThai = new JLabel("Đang tải báo cáo...");
        lblTrangThai.setForeground(new Color(112, 104, 116));
        tableCard.add(lblTrangThai, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel taoVungTongQuan() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        lblNhanTong1 = labelTong("Tổng 1");
        lblNhanTong2 = labelTong("Tổng 2");
        lblNhanTong3 = labelTong("Tổng 3");
        lblTong1 = giaTriTong();
        lblTong2 = giaTriTong();
        lblTong3 = giaTriTong();
        cards.add(cardTong(lblNhanTong1, lblTong1, MAU_HONG));
        cards.add(cardTong(lblNhanTong2, lblTong2, MAU_XANH));
        cards.add(cardTong(lblNhanTong3, lblTong3, MAU_XANH_LA));
        return cards;
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

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(80, 74, 84));
        return label;
    }

    private JLabel labelTong(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(112, 104, 116));
        return label;
    }

    private JLabel giaTriTong() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(new Color(35, 30, 48));
        return label;
    }

    private JPanel cardTong(JLabel title, JLabel value, Color lineColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, lineColor),
                new EmptyBorder(14, 12, 14, 12)));
        card.setPreferredSize(new Dimension(220, 82));
        card.add(title, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        return button;
    }

    private String[] layLuaChonLoaiPhu() {
        if (hienLoaiNhanVien) {
            return new String[]{"Tất cả", "Nhân viên", "Quản lý", "Quản trị viên Hệ thống"};
        }
        if (hienLoaiDoanhThu) {
            return new String[]{"Tất cả", "Thuê không gian", "Dịch vụ ăn uống", "Gia hạn phiên"};
        }
        return new String[]{"Tất cả"};
    }

    private void initDefaultDates() {
        String today = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
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

    private void taiBaoCao() {
        if (!boLocHopLe()) {
            return;
        }
        setDangTai(true);
        System.out.println("[BaoCao] Load bao cao: " + loaiBaoCao
                + ", tuNgay=" + txtTuNgay.getText().trim()
                + ", denNgay=" + txtDenNgay.getText().trim()
                + ", chiNhanh=" + cbxChiNhanh.getSelectedItem());
        new SwingWorker<DuLieuBaoCaoTongQuatDTO, Void>() {
            @Override
            protected DuLieuBaoCaoTongQuatDTO doInBackground() {
                return taoDuLieuBaoCao();
            }

            @Override
            protected void done() {
                try {
                    duLieuHienTai = get();
                    hienThiDuLieu(duLieuHienTai);
                    lblTrangThai.setText("Đã tải " + soDong(duLieuHienTai) + " dòng dữ liệu.");
                } catch (Exception ex) {
                    lblTrangThai.setText("Không tải được báo cáo.");
                    JOptionPane.showMessageDialog(BaoCaoTongQuatPanel.this,
                            com.wms.util.ErrorMessageUtil.toUserMessage(ex),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setDangTai(false);
                }
            }
        }.execute();
    }

    private DuLieuBaoCaoTongQuatDTO taoDuLieuBaoCao() {
        return controller.taoDuLieuBaoCao(
                loaiBaoCao,
                txtTuNgay.getText().trim(),
                txtDenNgay.getText().trim(),
                (String) cbxChiNhanh.getSelectedItem(),
                (String) cbxLoaiPhu.getSelectedItem(),
                nguoiXuatHienTai()
        );
    }

    private void hienThiDuLieu(DuLieuBaoCaoTongQuatDTO duLieu) {
        lblNhanTong1.setText(giaTri(duLieu == null ? null : duLieu.getNhanTongGiaTri1(), "Tổng 1"));
        lblNhanTong2.setText(giaTri(duLieu == null ? null : duLieu.getNhanTongGiaTri2(), "Tổng 2"));
        lblNhanTong3.setText(giaTri(duLieu == null ? null : duLieu.getNhanTongGiaTri3(), "Tổng 3"));
        lblTong1.setText(giaTri(duLieu == null ? null : duLieu.getTongGiaTri1(), "0"));
        lblTong2.setText(giaTri(duLieu == null ? null : duLieu.getTongGiaTri2(), "0"));
        lblTong3.setText(giaTri(duLieu == null ? null : duLieu.getTongGiaTri3(), "0"));

        List<String> headers = duLieu == null ? List.of() : duLieu.getDanhSachTieuDeCot();
        if (headers == null || headers.isEmpty()) {
            headers = List.of("Cột 1", "Cột 2", "Cột 3", "Cột 4", "Cột 5", "Cột 6", "Cột 7", "Cột 8");
        }
        tableModel.setColumnIdentifiers(headers.toArray());
        tableModel.setRowCount(0);

        if (duLieu == null || duLieu.getDanhSachDongBaoCao() == null) {
            return;
        }
        for (DongBaoCaoTongQuatDTO row : duLieu.getDanhSachDongBaoCao()) {
            tableModel.addRow(layGiaTriDong(row, headers.size()));
        }
    }

    private Object[] layGiaTriDong(DongBaoCaoTongQuatDTO row, int columnCount) {
        String[] values = {
                row.getCot1(), row.getCot2(), row.getCot3(), row.getCot4(),
                row.getCot5(), row.getCot6(), row.getCot7(), row.getCot8()
        };
        Object[] result = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            result[i] = i < values.length ? giaTri(values[i], "") : "";
        }
        return result;
    }

    private void xuatCsv() {
        if (duLieuHienTai == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tải báo cáo trước khi export.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo CSV");
        fileChooser.setSelectedFile(new File(tenFileBaoCao("csv")));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(fileChooser.getSelectedFile(), ".csv");
            try {
                BaoCaoCsvExporter.xuatCsv(file, duLieuHienTai);
                JOptionPane.showMessageDialog(this, "Xuất báo cáo CSV thành công!\n" + file.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                com.wms.util.MessageUtil.showError(this, "Lỗi khi xuất CSV.", ex);
            }
        }
    }

    private void xuatPdf() {
        if (!boLocHopLe()) {
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo PDF");
        fileChooser.setSelectedFile(new File(tenFileBaoCao("pdf")));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = ensureExtension(fileChooser.getSelectedFile(), ".pdf");
        setDangTai(true);
        lblTrangThai.setText("Đang export PDF...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                controller.xuatBaoCaoPdf(
                        file,
                        loaiBaoCao,
                        txtTuNgay.getText().trim(),
                        txtDenNgay.getText().trim(),
                        (String) cbxChiNhanh.getSelectedItem(),
                        (String) cbxLoaiPhu.getSelectedItem(),
                        nguoiXuatHienTai()
                );
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblTrangThai.setText("Export PDF thành công.");
                    JOptionPane.showMessageDialog(BaoCaoTongQuatPanel.this,
                            "Xuất báo cáo PDF thành công!\n" + file.getAbsolutePath(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    lblTrangThai.setText("Export PDF thất bại.");
                    JOptionPane.showMessageDialog(BaoCaoTongQuatPanel.this,
                            com.wms.util.ErrorMessageUtil.toUserMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setDangTai(false);
                }
            }
        }.execute();
    }

    private boolean boLocHopLe() {
        if (txtTuNgay.getText().trim().isEmpty() || txtDenNgay.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Từ ngày - Đến ngày.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void setDangTai(boolean loading) {
        btnTaiLai.setEnabled(!loading);
        btnXuatCsv.setEnabled(!loading);
        btnXuatPdf.setEnabled(!loading);
        lblTrangThai.setText(loading ? "Đang tải báo cáo..." : lblTrangThai.getText());
    }

    private int soDong(DuLieuBaoCaoTongQuatDTO duLieu) {
        return duLieu == null || duLieu.getDanhSachDongBaoCao() == null ? 0 : duLieu.getDanhSachDongBaoCao().size();
    }

    private String giaTri(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String nguoiXuatHienTai() {
        String userName = System.getProperty("user.name");
        return userName == null || userName.isBlank() ? "Người dùng hệ thống" : userName;
    }

    private String tenFileBaoCao(String extension) {
        String ngay = new SimpleDateFormat("ddMMyyyy").format(new Date());
        String slug = loaiBaoCao.replaceAll("[^A-Za-z0-9À-ỹ]+", "_");
        return "BaoCao_" + slug + "_" + ngay + "." + extension;
    }

    private File ensureExtension(File file, String extension) {
        if (file.getAbsolutePath().toLowerCase().endsWith(extension)) {
            return file;
        }
        return new File(file.getAbsolutePath() + extension);
    }
}
