package com.wms.view.QuanLyPhien; 

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

        // --- PHẦN THÔNG TIN CHI TIẾT CỦA PHIÊN ---
        JPanel pnlInfo = new JPanel(new GridLayout(4, 2, 15, 15));
        pnlInfo.setBackground(COLOR_PINK_BG);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PINK_DARK, 2), "Thông Tin Chi Tiết");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        titledBorder.setTitleColor(COLOR_PINK_DARK);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        lblMaPhien = createInfoLabel("");
        lblKhongGian = createInfoLabel("");
        lblKhachHang = createInfoLabel("");
        lblTrangThai = createInfoLabel("");
        lblTGBatDau = createInfoLabel("");
        lblTGDuyKien = createInfoLabel("");
        lblTGKetThuc = createInfoLabel("");

        pnlInfo.add(lblMaPhien);
        pnlInfo.add(lblKhongGian);
        pnlInfo.add(lblKhachHang);
        pnlInfo.add(lblTrangThai);
        pnlInfo.add(lblTGBatDau);
        pnlInfo.add(lblTGDuyKien);
        pnlInfo.add(lblTGKetThuc);
        pnlInfo.add(new JLabel("")); // Ô trống giữ layout lưới cho cân xứng

        mainPanel.add(pnlInfo, BorderLayout.NORTH);

        // --- PHẦN BẢNG DỊCH VỤ SỬ DỤNG THÊM ---
        JPanel pnlDichVu = new JPanel(new BorderLayout());
        pnlDichVu.setBackground(COLOR_PINK_BG);
        
        JLabel lblSubTitle = new JLabel("Các dịch vụ đã sử dụng trong phiên:");
        lblSubTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblSubTitle.setForeground(COLOR_TEXT_NAVY);
        lblSubTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        pnlDichVu.add(lblSubTitle, BorderLayout.NORTH);

        String[] columns = {"Mã DV", "Tên Dịch Vụ", "SL", "Đơn Giá", "Thành Tiền", "Ghi Chú"};
        tableModelDichVu = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        tableDichVu = new JTable(tableModelDichVu);
        tableDichVu.setFont(new Font("Arial", Font.PLAIN, 14)); // Chỉnh font đồng bộ với các form trước
        tableDichVu.setRowHeight(30);
        tableDichVu.setGridColor(COLOR_PINK_BORDER);

        // Canh chỉnh tỉ lệ cột cho vừa khung 800px
        TableColumnModel columnModel = tableDichVu.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);  // Mã DV
        columnModel.getColumn(1).setPreferredWidth(180); // Tên Dịch Vụ
        columnModel.getColumn(2).setPreferredWidth(40);  // SL
        columnModel.getColumn(3).setPreferredWidth(100); // Đơn Giá
        columnModel.getColumn(4).setPreferredWidth(100); // Thành Tiền
        columnModel.getColumn(5).setPreferredWidth(150); // Ghi chú

        JTableHeader header = tableDichVu.getTableHeader();
        header.setBackground(COLOR_PINK_LIGHT);
        header.setForeground(COLOR_TEXT_NAVY);
        header.setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollDichVu = new JScrollPane(tableDichVu);
        scrollDichVu.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        scrollDichVu.getViewport().setBackground(Color.WHITE);
        pnlDichVu.add(scrollDichVu, BorderLayout.CENTER);

        mainPanel.add(pnlDichVu, BorderLayout.CENTER);

        // --- PHẦN NÚT BẤM DƯỚI CÙNG ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnKetThucPhien = createStyledButton("Kết thúc phiên", COLOR_PINK_DARK, Color.decode("#0039A1"));
        btnDong = createStyledButton("Đóng", Color.LIGHT_GRAY, COLOR_TEXT_NAVY);

        btnDong.addActionListener(e -> this.dispose());

        pnlActions.add(btnKetThucPhien);
        pnlActions.add(btnDong);

        mainPanel.add(pnlActions, BorderLayout.SOUTH);
        this.add(mainPanel);
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(COLOR_TEXT_NAVY);
        return label;
    }

    // Nâng cấp hàm tạo nút để hỗ trợ truyền màu chữ và thêm hiệu ứng Hover
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                // Làm cho màu nền tối đi một chút khi hover
                btn.setBackground(bgColor.darker());
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent evt) {
                // Trả về màu gốc khi chuột rời đi
                btn.setBackground(bgColor);
                btn.setForeground(fgColor);
            }
        });
        
        return btn;
    }

    private void loadChiTietPhien() {
        // Giả lập trạng thái từ CSDL
        String trangThaiThucTe = "Đang hoạt động"; 
        
        lblMaPhien.setText("<html><b>Mã Phiên:</b> " + maPhien + "</html>");
        lblKhongGian.setText("<html><b>Không Gian:</b> Bàn đơn</html>"); // Đã bỏ mã KG01
        lblKhachHang.setText("<html><b>Khách Hàng:</b> bondy</html>"); // Đã bỏ mã KH001
        lblTrangThai.setText("<html><b>Trạng Thái:</b> <font color='red'>" + trangThaiThucTe + "</font></html>");
        lblTGBatDau.setText("<html><b>Giờ Bắt Đầu:</b> 08:30 23/04/2026</html>");
        lblTGDuyKien.setText("<html><b>Dự Kiến Kết Thúc:</b> 11:30 23/04/2026</html>");

        if (trangThaiThucTe.equalsIgnoreCase("Đang hoạt động")) {
            lblTGKetThuc.setText("<html><b>Giờ Kết Thúc Thực Tế:</b> <font color='gray'><i>(Chưa kết thúc)</i></font></html>");
            btnKetThucPhien.setVisible(true);
        } else if (trangThaiThucTe.equalsIgnoreCase("Đã kết thúc")) {
            lblTGKetThuc.setText("<html><b>Giờ Kết Thúc Thực Tế:</b> 10:45 23/04/2026</html>");
            btnKetThucPhien.setVisible(false); 
        }

        tableModelDichVu.setRowCount(0);
        tableModelDichVu.addRow(new Object[]{"DV01", "Thuê thêm 1 giờ", "2", "35,000", "70,000", ""}); 
        tableModelDichVu.addRow(new Object[]{"DV03", "Trà đào", "2", "40,000", "80,000", "Ít đường"}); 
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame parentFrame = new JFrame();
            parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            parentFrame.setSize(1000, 700); 
            parentFrame.setLocationRelativeTo(null);
            
            
            ChiTietPhienForm dialog = new ChiTietPhienForm(parentFrame, "P001");
            dialog.setVisible(true);
        });
    }
}