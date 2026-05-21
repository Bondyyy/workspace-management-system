package com.wms.view.TrangChuHoiVien.TrangChu;

import com.wms.controller.TrangChuHoiVien.TrangChuController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class TrangChuForm extends javax.swing.JPanel {

    private TrangChuController controller;
    private JButton btnDatNgay;
    private JLabel lblCard1Sub;
    private JLabel lblCard1Title;
    private JLabel lblCard1Value;
    private JLabel lblCard2Sub;
    private JLabel lblCard2Title;
    private JLabel lblCard2Value;
    private JLabel lblCard3Sub;
    private JLabel lblCard3Title;
    private JLabel lblCard3Value;
    private JLabel lblPromoBanner;
    private JLabel lblPromoTitle;
    private JLabel lblTableTitle;
    private JLabel lblTitle;
    private JPanel pnCard1;
    private JPanel pnCard2;
    private JPanel pnCard3;
    private JPanel pnMain;
    private JPanel pnPromo;
    private JScrollPane scrollTable;
    private JTable tblLichSu;

    public TrangChuForm() {
        initComponents();
        controller = new TrangChuController();
        loadData();
    }

    private void initComponents() {
        pnMain = new JPanel();
        lblTitle = new JLabel();
        pnCard1 = new JPanel();
        lblCard1Title = new JLabel();
        lblCard1Value = new JLabel();
        lblCard1Sub = new JLabel();
        pnCard2 = new JPanel();
        lblCard2Title = new JLabel();
        lblCard2Value = new JLabel();
        lblCard2Sub = new JLabel();
        pnCard3 = new JPanel();
        lblCard3Title = new JLabel();
        lblCard3Value = new JLabel();
        lblCard3Sub = new JLabel();
        lblTableTitle = new JLabel();
        scrollTable = new JScrollPane();
        tblLichSu = new JTable();
        lblPromoTitle = new JLabel();
        pnPromo = new JPanel();
        lblPromoBanner = new JLabel();
        btnDatNgay = new JButton();

        setLayout(new BorderLayout());

        pnMain.setBackground(new Color(254, 248, 250));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 640));
        pnMain.setLayout(null);

        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(48, 30, 35));
        lblTitle.setText("Tổng quan hoạt động");
        pnMain.add(lblTitle);
        lblTitle.setBounds(30, 20, 400, 32);

        pnCard1.setBackground(Color.WHITE);
        pnCard1.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(235, 94, 141)));
        pnCard1.setLayout(null);

        lblCard1Title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCard1Title.setForeground(new Color(136, 136, 136));
        lblCard1Title.setText("ĐIỂM TÍCH LŨY");
        pnCard1.add(lblCard1Title);
        lblCard1Title.setBounds(20, 20, 200, 20);

        lblCard1Value.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblCard1Value.setForeground(new Color(235, 94, 141));
        lblCard1Value.setText("0");
        pnCard1.add(lblCard1Value);
        lblCard1Value.setBounds(20, 45, 200, 48);

        lblCard1Sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCard1Sub.setForeground(new Color(170, 170, 170));
        lblCard1Sub.setText("Hạng hiện tại: Thành viên mới");
        pnCard1.add(lblCard1Sub);
        lblCard1Sub.setBounds(20, 95, 250, 18);

        pnMain.add(pnCard1);
        pnCard1.setBounds(30, 70, 310, 130);

        pnCard2.setBackground(Color.WHITE);
        pnCard2.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(94, 141, 235)));
        pnCard2.setLayout(null);

        lblCard2Title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCard2Title.setForeground(new Color(136, 136, 136));
        lblCard2Title.setText("TỔNG GIỜ SỬ DỤNG");
        pnCard2.add(lblCard2Title);
        lblCard2Title.setBounds(20, 20, 200, 20);

        lblCard2Value.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblCard2Value.setForeground(new Color(94, 141, 235));
        lblCard2Value.setText("0");
        pnCard2.add(lblCard2Value);
        lblCard2Value.setBounds(20, 45, 200, 48);

        lblCard2Sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCard2Sub.setForeground(new Color(170, 170, 170));
        lblCard2Sub.setText("Thống kê các lượt đã sử dụng");
        pnCard2.add(lblCard2Sub);
        lblCard2Sub.setBounds(20, 95, 250, 18);

        pnMain.add(pnCard2);
        pnCard2.setBounds(370, 70, 310, 130);

        pnCard3.setBackground(Color.WHITE);
        pnCard3.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(235, 184, 94)));
        pnCard3.setLayout(null);

        lblCard3Title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCard3Title.setForeground(new Color(136, 136, 136));
        lblCard3Title.setText("ƯU ĐÃI CỦA TÔI");
        pnCard3.add(lblCard3Title);
        lblCard3Title.setBounds(20, 20, 200, 20);

        lblCard3Value.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblCard3Value.setForeground(new Color(235, 184, 94));
        lblCard3Value.setText("0");
        pnCard3.add(lblCard3Value);
        lblCard3Value.setBounds(20, 45, 200, 48);

        lblCard3Sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCard3Sub.setForeground(new Color(170, 170, 170));
        lblCard3Sub.setText("Phiếu giảm giá đang có hiệu lực");
        pnCard3.add(lblCard3Sub);
        lblCard3Sub.setBounds(20, 95, 250, 18);

        pnMain.add(pnCard3);
        pnCard3.setBounds(710, 70, 310, 130);

        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTableTitle.setForeground(new Color(48, 30, 35));
        lblTableTitle.setText("Lịch sử đặt chỗ gần đây");
        pnMain.add(lblTableTitle);
        lblTableTitle.setBounds(30, 230, 300, 25);

        tblLichSu.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã đặt chỗ", "Không gian", "Thời gian", "Trạng thái"}
        ) {
            private final boolean[] canEdit = new boolean[]{false, false, false, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tblLichSu.setRowHeight(30);
        tblLichSu.setSelectionBackground(new Color(235, 94, 141));
        scrollTable.setViewportView(tblLichSu);

        pnMain.add(scrollTable);
        scrollTable.setBounds(30, 270, 650, 340);

        lblPromoTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPromoTitle.setForeground(new Color(48, 30, 35));
        lblPromoTitle.setText("Không gian nổi bật");
        pnMain.add(lblPromoTitle);
        lblPromoTitle.setBounds(710, 230, 300, 25);

        pnPromo.setBackground(Color.WHITE);
        pnPromo.setLayout(null);

        lblPromoBanner.setText("""
                <html><div style='padding:15px; text-align:center;'>
                <h2 style='color:#EB5E8D; margin-bottom:5px;'>Phòng họp SPRING 01</h2>
                <h4 style='color:#888; margin-top:0;'>Sức chứa: 10 người</h4>
                <p style='font-size:13px; color:#444; line-height:1.5;'>
                Không gian riêng tư, yên tĩnh. Trang bị đầy đủ máy chiếu, bảng trắng,
                âm thanh hiện đại.<br><br><b>Giảm 10%</b> cho hội viên VIP.
                </p></div></html>
                """);
        lblPromoBanner.setVerticalAlignment(SwingConstants.TOP);
        lblPromoBanner.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnPromo.add(lblPromoBanner);
        lblPromoBanner.setBounds(0, 0, 310, 260);

        btnDatNgay.setBackground(new Color(235, 94, 141));
        btnDatNgay.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDatNgay.setForeground(Color.WHITE);
        btnDatNgay.setText("Đặt ngay bây giờ");
        btnDatNgay.addActionListener(this::btnDatNgayActionPerformed);
        pnPromo.add(btnDatNgay);
        btnDatNgay.setBounds(0, 290, 310, 50);

        pnMain.add(pnPromo);
        pnPromo.setBounds(710, 270, 310, 340);

        add(pnMain, BorderLayout.CENTER);
    }

    private void loadData() {
        lblCard1Value.setText(String.format("%,d", controller.layDiemTichLuy()));
        lblCard1Sub.setText("Hạng hiện tại: " + controller.layHangHienTai());
        lblCard2Value.setText(String.valueOf(controller.layTongGioSuDung()));
        lblCard3Value.setText(String.valueOf(controller.laySoUuDai()));

        DefaultTableModel model = (DefaultTableModel) tblLichSu.getModel();
        model.setRowCount(0);
        for (Object[] row : controller.layLichSuDatCho()) {
            model.addRow(row);
        }

        loadKhongGianNoiBat();
    }

    private void loadKhongGianNoiBat() {
    }

    private void btnDatNgayActionPerformed(java.awt.event.ActionEvent evt) {
    }
}
