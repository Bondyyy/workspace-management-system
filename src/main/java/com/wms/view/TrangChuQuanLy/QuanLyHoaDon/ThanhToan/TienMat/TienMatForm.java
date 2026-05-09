package com.wms.view.TrangChuQuanLy.QuanLyHoaDon.ThanhToan.TienMat;

import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class TienMatForm extends JDialog {

    private final double tongTien;
    private boolean daThanhToan = false;
    private final NumberFormat formatTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private JButton btnGoiY1, btnGoiY2, btnGoiY3, btnGoiY4, btnHuy, btnXacNhan;
    private JLabel lblGoiY, lblHeaderTitle, lblNhap, lblTienThua, lblTienThuaTitle, lblTongTienTitle;
    private JPanel pnContent, pnHeader, pnMain;
    private JTextField txtSoTienKhachDua, txtTongTien;

    public TienMatForm(Frame parent, boolean modal, double tongTien) {
        super(parent, modal);
        this.tongTien = tongTien;
        initComponents();
        setupLogic();
        pack();
        setLocationRelativeTo(parent);
    }

    private void setupLogic() {
        txtTongTien.setText(formatTien.format(tongTien));
        txtSoTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { tinhTienThua(); }
            @Override public void removeUpdate(DocumentEvent e) { tinhTienThua(); }
            @Override public void changedUpdate(DocumentEvent e) { tinhTienThua(); }
        });

        double[] suggestions = calculateSuggestions(tongTien);
        btnGoiY1.setText(formatShort(suggestions[0]));
        btnGoiY1.addActionListener(e -> txtSoTienKhachDua.setText(String.format("%.0f", suggestions[0])));
        btnGoiY2.setText(formatShort(suggestions[1]));
        btnGoiY2.addActionListener(e -> txtSoTienKhachDua.setText(String.format("%.0f", suggestions[1])));
        btnGoiY3.setText(formatShort(suggestions[2]));
        btnGoiY3.addActionListener(e -> txtSoTienKhachDua.setText(String.format("%.0f", suggestions[2])));
        btnGoiY4.setText(formatShort(suggestions[3]));
        btnGoiY4.addActionListener(e -> txtSoTienKhachDua.setText(String.format("%.0f", suggestions[3])));
    }

    private double[] calculateSuggestions(double amount) {
        double[] s = new double[4];
        s[0] = amount;
        s[1] = Math.ceil(amount / 10000.0) * 10000.0;
        if (s[1] <= s[0]) s[1] += 10000.0;
        s[2] = Math.ceil(amount / 100000.0) * 100000.0;
        if (s[2] <= s[1]) s[2] += 100000.0;
        double[] thresholds = {50000, 100000, 200000, 500000, 1000000, 2000000, 5000000};
        s[3] = s[2] + 500000.0;
        for (double t : thresholds) {
            if (t > s[2]) {
                s[3] = t;
                break;
            }
        }
        return s;
    }

    private String formatShort(double value) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value);
    }

    private void tinhTienThua() {
        try {
            String text = txtSoTienKhachDua.getText().replaceAll("[^0-9]", "");
            if (text.isEmpty()) {
                lblTienThua.setText("0 VNĐ");
                btnXacNhan.setEnabled(false);
                return;
            }
            double khachDua = Double.parseDouble(text);
            double thua = khachDua - tongTien;
            if (thua >= 0) {
                lblTienThua.setText(formatTien.format(thua));
                lblTienThua.setForeground(new Color(0, 153, 51));
                btnXacNhan.setEnabled(true);
            } else {
                lblTienThua.setText("Thiếu " + formatTien.format(Math.abs(thua)));
                lblTienThua.setForeground(new Color(220, 53, 69));
                btnXacNhan.setEnabled(false);
            }
        } catch (Exception e) {
            lblTienThua.setText("Lỗi định dạng");
            btnXacNhan.setEnabled(false);
        }
    }

    private void initComponents() {
        pnMain = new JPanel();
        pnHeader = new JPanel();
        lblHeaderTitle = new JLabel();
        pnContent = new JPanel();
        lblTongTienTitle = new JLabel();
        txtTongTien = new JTextField();
        lblNhap = new JLabel();
        txtSoTienKhachDua = new JTextField();
        lblGoiY = new JLabel();
        btnGoiY1 = new JButton();
        btnGoiY2 = new JButton();
        btnGoiY3 = new JButton();
        btnGoiY4 = new JButton();
        lblTienThuaTitle = new JLabel();
        lblTienThua = new JLabel();
        btnHuy = new JButton();
        btnXacNhan = new JButton();

        setPreferredSize(new Dimension(560, 600));
        pnMain.setBackground(new Color(254, 248, 250));
        pnMain.setLayout(null);

        pnHeader.setBackground(new Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new Font("Segoe UI", 1, 20));
        lblHeaderTitle.setForeground(Color.WHITE);
        lblHeaderTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblHeaderTitle.setText("THANH TOÁN TIỀN MẶT");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 560, 50);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 560, 50);

        pnContent.setBackground(Color.WHITE);
        pnContent.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, new Color(235, 94, 141)));
        pnContent.setLayout(null);

        lblTongTienTitle.setFont(new Font("Segoe UI", 1, 14));
        lblTongTienTitle.setForeground(new Color(136, 136, 136));
        lblTongTienTitle.setText("Tổng tiền hóa đơn");
        pnContent.add(lblTongTienTitle);
        lblTongTienTitle.setBounds(20, 20, 200, 30);

        txtTongTien.setEditable(false);
        txtTongTien.setFont(new Font("Segoe UI", 1, 24));
        txtTongTien.setForeground(new Color(235, 94, 141));
        txtTongTien.setBorder(null);
        txtTongTien.setText("0 VNĐ");
        pnContent.add(txtTongTien);
        txtTongTien.setBounds(20, 45, 460, 35);

        lblNhap.setFont(new Font("Segoe UI", 1, 14));
        lblNhap.setForeground(new Color(136, 136, 136));
        lblNhap.setText("Số tiền khách đưa");
        pnContent.add(lblNhap);
        lblNhap.setBounds(20, 100, 200, 30);

        txtSoTienKhachDua.setFont(new Font("Segoe UI", 1, 20));
        pnContent.add(txtSoTienKhachDua);
        txtSoTienKhachDua.setBounds(20, 125, 460, 45);

        lblGoiY.setFont(new Font("Segoe UI", 1, 13));
        lblGoiY.setForeground(new Color(136, 136, 136));
        lblGoiY.setText("Gợi ý mệnh giá");
        pnContent.add(lblGoiY);
        lblGoiY.setBounds(20, 190, 200, 30);

        btnGoiY1.setBackground(new Color(254, 248, 250));
        btnGoiY1.setFont(new Font("Segoe UI", 1, 14));
        btnGoiY1.setForeground(new Color(235, 94, 141));
        btnGoiY1.setText("50.000");
        pnContent.add(btnGoiY1);
        btnGoiY1.setBounds(20, 215, 105, 40);

        btnGoiY2.setBackground(new Color(254, 248, 250));
        btnGoiY2.setFont(new Font("Segoe UI", 1, 14));
        btnGoiY2.setForeground(new Color(235, 94, 141));
        btnGoiY2.setText("100.000");
        pnContent.add(btnGoiY2);
        btnGoiY2.setBounds(135, 215, 105, 40);

        btnGoiY3.setBackground(new Color(254, 248, 250));
        btnGoiY3.setFont(new Font("Segoe UI", 1, 14));
        btnGoiY3.setForeground(new Color(235, 94, 141));
        btnGoiY3.setText("200.000");
        pnContent.add(btnGoiY3);
        btnGoiY3.setBounds(250, 215, 105, 40);

        btnGoiY4.setBackground(new Color(254, 248, 250));
        btnGoiY4.setFont(new Font("Segoe UI", 1, 14));
        btnGoiY4.setForeground(new Color(235, 94, 141));
        btnGoiY4.setText("500.000");
        pnContent.add(btnGoiY4);
        btnGoiY4.setBounds(365, 215, 115, 40);

        lblTienThuaTitle.setFont(new Font("Segoe UI", 1, 14));
        lblTienThuaTitle.setForeground(new Color(136, 136, 136));
        lblTienThuaTitle.setText("Tiền thừa trả khách");
        pnContent.add(lblTienThuaTitle);
        lblTienThuaTitle.setBounds(20, 280, 200, 30);

        lblTienThua.setFont(new Font("Segoe UI", 1, 24));
        lblTienThua.setForeground(new Color(0, 153, 51));
        lblTienThua.setText("0 VNĐ");
        pnContent.add(lblTienThua);
        lblTienThua.setBounds(20, 305, 460, 35);

        btnHuy.setBackground(new Color(220, 53, 69));
        btnHuy.setFont(new Font("Segoe UI", 1, 14));
        btnHuy.setForeground(Color.WHITE);
        btnHuy.setText("Hủy bỏ");
        btnHuy.addActionListener(e -> {
            daThanhToan = false;
            dispose();
        });
        pnContent.add(btnHuy);
        btnHuy.setBounds(20, 400, 130, 45);

        btnXacNhan.setBackground(new Color(0, 153, 51));
        btnXacNhan.setFont(new Font("Segoe UI", 1, 16));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setText("Xác nhận & Thanh toán");
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(e -> {
            daThanhToan = true;
            dispose();
        });
        pnContent.add(btnXacNhan);
        btnXacNhan.setBounds(165, 400, 315, 45);

        pnMain.add(pnContent);
        pnContent.setBounds(30, 80, 500, 470);

        getContentPane().add(pnMain, BorderLayout.CENTER);
    }

    public boolean isDaThanhToan() {
        return daThanhToan;
    }
}
