package com.wms.view.QuanLyPhien; 
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ChiTietPhienForm extends JDialog {

    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_LIGHT = Color.decode("#f07baa");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_PINK_BORDER = Color.decode("#fccde0");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58); 

    private String maPhien;
    private JLabel lblMaPhien, lblKhachHang, lblKhongGian, lblTGBatDau, lblTGDuyKien, lblTGKetThuc, lblTrangThai;
    private JTable tableDichVu;
    private DefaultTableModel tableModelDichVu;
    private JButton btnDong, btnKetThucPhien;

    public ChiTietPhienForm(JFrame parent, String maPhien) {
        super(parent, "Chi Tiết Phiên Làm Việc", true);
        this.maPhien = maPhien;
        initComponents();
        loadChiTietPhien(); 
    }

    private void initComponents() {
        this.setSize(800, 600); 
        this.setLocationRelativeTo(getParent());
        this.setLayout(new BorderLayout(15, 15));
        this.getContentPane().setBackground(COLOR_PINK_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COLOR_PINK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel pnlInfo = new JPanel(new GridLayout(4, 2, 15, 15));
        pnlInfo.setBackground(COLOR_PINK_BG);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PINK_DARK, 2), "Thông Tin Chi Tiết");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        titledBorder.setTitleColor(COLOR_PINK_DARK);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        lblMaPhien = createInfoLabel("Mã Phiên:");
        lblKhongGian = createInfoLabel("Không Gian:");
        lblKhachHang = createInfoLabel("Khách Hàng:");
        lblTrangThai = createInfoLabel("Trạng Thái:");
        lblTGBatDau = createInfoLabel("Giờ Bắt Đầu:");
        lblTGDuyKien = createInfoLabel("Dự Kiến Kết Thúc:");
        lblTGKetThuc = createInfoLabel("Giờ Kết Thúc Thực Tế:");

        pnlInfo.add(lblMaPhien);
        pnlInfo.add(lblKhongGian);
        pnlInfo.add(lblKhachHang);
        pnlInfo.add(lblTrangThai);
        pnlInfo.add(lblTGBatDau);
        pnlInfo.add(lblTGDuyKien);
        pnlInfo.add(lblTGKetThuc);
        pnlInfo.add(new JLabel("")); // Ô trống giữ layout

        mainPanel.add(pnlInfo, BorderLayout.NORTH);

        JPanel pnlDichVu = new JPanel(new BorderLayout());
        pnlDichVu.setBackground(COLOR_PINK_BG);
        
        JLabel lblSubTitle = new JLabel("Các dịch vụ đã sử dụng trong phiên:");
        lblSubTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblSubTitle.setForeground(COLOR_TEXT_NAVY);
        lblSubTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        pnlDichVu.add(lblSubTitle, BorderLayout.NORTH);

        String[] columns = {"Mã DV", "Tên Dịch Vụ", "Số Lượng", "Đơn Giá", "Thành Tiền", "Ghi Chú"};
        tableModelDichVu = new DefaultTableModel(columns, 0);
        tableDichVu = new JTable(tableModelDichVu);
        tableDichVu.setFont(new Font("Arial", Font.PLAIN, 15)); // Cỡ chữ to theo yêu cầu
        tableDichVu.setRowHeight(35);

        JTableHeader header = tableDichVu.getTableHeader();
        header.setBackground(COLOR_PINK_LIGHT);
        header.setForeground(COLOR_TEXT_NAVY);
        header.setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollDichVu = new JScrollPane(tableDichVu);
        scrollDichVu.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        pnlDichVu.add(scrollDichVu, BorderLayout.CENTER);

        mainPanel.add(pnlDichVu, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnKetThucPhien = createStyledButton("Kết thúc phiên & Thanh toán", COLOR_PINK_DARK);
        btnDong = createStyledButton("Đóng", Color.GRAY);

        btnDong.addActionListener(e -> this.dispose());

        pnlActions.add(btnKetThucPhien);
        pnlActions.add(btnDong);

        mainPanel.add(pnlActions, BorderLayout.SOUTH);
        this.add(mainPanel);
    }

    private JLabel createInfoLabel(String prefix) {
        JLabel label = new JLabel(prefix + " ");
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(COLOR_TEXT_NAVY);
        return label;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void loadChiTietPhien() {
        // Giả lập 
        String trangThaiThucTe = "Đã kết thúc"; 
        
        lblMaPhien.setText("<html><b>Mã Phiên:</b> " + maPhien + "</html>");
        lblKhongGian.setText("<html><b>Không Gian:</b> KG01 - Bàn đơn</html>");
        lblKhachHang.setText("<html><b>Khách Hàng:</b> KH001 - bondy</html>");
        lblTrangThai.setText("<html><b>Trạng Thái:</b> <font color='red'>" + trangThaiThucTe + "</font></html>");
        lblTGBatDau.setText("<html><b>Giờ Bắt Đầu:</b> 08:30 23/04/2026</html>");
        lblTGDuyKien.setText("<html><b>Dự Kiến Kết Thúc:</b> 11:30 23/04/2026</html>");

        if (trangThaiThucTe.equalsIgnoreCase("Đang hoạt động")) {
            // Trường hợp 1: Đang hoạt động -> Giờ kết thúc là null (hiển thị chưa xác định)
            lblTGKetThuc.setText("<html><b>Giờ Kết Thúc Thực Tế:</b> <font color='gray'><i>(Chưa kết thúc)</i></font></html>");
            btnKetThucPhien.setVisible(true);
        } else if (trangThaiThucTe.equalsIgnoreCase("Đã kết thúc")) {
            // Trường hợp 2: Đã kết thúc -> Hiển thị giờ kết thúc cụ thể
            lblTGKetThuc.setText("<html><b>Giờ Kết Thúc Thực Tế:</b> 10:45 23/04/2026</html>");
            btnKetThucPhien.setVisible(false); // Đã kết thúc rồi thì không hiện nút Kết thúc nữa
        }

        tableModelDichVu.setRowCount(0);
        tableModelDichVu.addRow(new Object[]{"DV01", "Thuê thêm 1 giờ", "2", "35,000", "35,000", ""});
        tableModelDichVu.addRow(new Object[]{"DV03", "Trà đào", "2", "40,000", "35,000", "Ít đường"});
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        ChiTietPhienForm d = new ChiTietPhienForm(f, "P001");
        d.setVisible(true);
    }
}