package com.wms.view.QuanLyDichVu; 

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class QuanLyDichVuDat extends JPanel {

    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_LIGHT = Color.decode("#f07baa");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_PINK_BORDER = Color.decode("#fccde0");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58);

    private JTable tableDichVu;
    private DefaultTableModel tableModel;
    private JButton btnTraCuu, btnThemDichVu, btnTaiLai;
    private JTextField txtTimKiem;

    public QuanLyDichVuDat() {
        initComponents();
        loadData(); 
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- PHẦN HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);

        JLabel lblTitle = new JLabel("QUẢN LÝ DỊCH VỤ ĐƯỢC ĐẶT");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22)); // Thu nhỏ font một chút cho vừa 800px
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // Vùng chứa chức năng tìm kiếm (Gồm Label ở trên, TextField + Nút ở dưới)
        JPanel pnlSearchContainer = new JPanel(new BorderLayout(0, 5));
        pnlSearchContainer.setBackground(COLOR_PINK_BG);

        JLabel lblSearchHint = new JLabel("Tìm mã phiên/ tên dịch vụ:");
        lblSearchHint.setFont(new Font("Arial", Font.ITALIC, 13));
        lblSearchHint.setForeground(COLOR_TEXT_NAVY);
        pnlSearchContainer.add(lblSearchHint, BorderLayout.NORTH); // Đưa chữ lên trên

        // Panel con chứa ô nhập và nút bấm
        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlInput.setBackground(COLOR_PINK_BG);
        
        txtTimKiem = new JTextField(15); // Giảm xuống 15 cột để tiết kiệm không gian
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnTraCuu = createStyledButton("Tra cứu", COLOR_PINK);
        btnTraCuu.addActionListener(e -> traCuuDichVu());

        pnlInput.add(txtTimKiem);
        pnlInput.add(Box.createHorizontalStrut(10)); // Khoảng cách giữa ô text và nút
        pnlInput.add(btnTraCuu);

        pnlSearchContainer.add(pnlInput, BorderLayout.CENTER);
        pnlHeader.add(pnlSearchContainer, BorderLayout.EAST);

        this.add(pnlHeader, BorderLayout.NORTH);

        // --- PHẦN BẢNG DỮ LIỆU ---
        String[] columnNames = {"Mã Phiên", "Tên Dịch Vụ", "SL", "Thời Gian Đặt", "Khách Hàng", "Tên Không Gian", "Ghi Chú"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        tableDichVu = new JTable(tableModel);
        tableDichVu.setFont(new Font("Arial", Font.PLAIN, 14)); // Chỉnh font bảng nhỏ lại 1 xíu
        tableDichVu.setRowHeight(30); 
        tableDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDichVu.setGridColor(COLOR_PINK_BORDER);

        TableColumnModel columnModel = tableDichVu.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(70);  // Mã
        columnModel.getColumn(1).setPreferredWidth(130); // Tên DV
        columnModel.getColumn(2).setPreferredWidth(30);  // SL (Rất hẹp)
        columnModel.getColumn(3).setPreferredWidth(120); // Thời gian
        columnModel.getColumn(4).setPreferredWidth(100); // KH
        columnModel.getColumn(5).setPreferredWidth(100); // Không gian
        columnModel.getColumn(6).setPreferredWidth(120); // Ghi chú

        JTableHeader header = tableDichVu.getTableHeader();
        header.setBackground(COLOR_PINK_LIGHT);
        header.setForeground(COLOR_TEXT_NAVY);
        header.setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(tableDichVu);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnTaiLai = createStyledButton("Tải lại danh sách", Color.GRAY);
        btnThemDichVu = createStyledButton("+ Thêm dịch vụ", COLOR_PINK_DARK);

        btnTaiLai.addActionListener(e -> loadData());
        btnThemDichVu.addActionListener(e -> themDichVuMoi());

        pnlActions.add(btnTaiLai);
        pnlActions.add(btnThemDichVu);

        this.add(pnlActions, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(new Color(0, 0 , 58));
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // Thu gọn lề nút một chút
        return btn;
    }

    private void loadData() {
        try {
            tableModel.setRowCount(0); 

            // Cập nhật mảng mockData: Đã bỏ đi các mã KG01, KG05...
            Object[][] mockData = {
                {"P001", "Cà phê đen đá", 2, "09:15 24/04/2026", "Nguyễn Văn A", "Bàn đơn", "Ít đường"},
                {"P001", "Thêm 1 giờ", 4, "09:15 24/04/2026", "Nguyễn Văn A", "Bàn đơn", ""},
                {"P002", "In tài liệu A4", 15, "10:00 24/04/2026", "Trần Thị B", "Phòng họp", "In trắng đen 2 mặt"},
                {"P005", "Trà dâu tằm", 1, "10:30 24/04/2026", "Khách vãng lai 01", "Bàn đôi", ""}
            };

            for (Object[] row : mockData) {
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi kết nối cơ sở dữ liệu khi tải danh sách dịch vụ: " + ex.getMessage(), 
                "Lỗi hệ thống", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void traCuuDichVu() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập thông tin để tra cứu.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Kích hoạt Use-case [Tra cứu dịch vụ được đặt] với từ khóa: " + keyword);
    }

    private void themDichVuMoi() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;

        ThemDichVuForm dialog = new ThemDichVuForm(parentFrame);
        
        dialog.setVisible(true);
        
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        JFrame frame = new JFrame("Module Lễ Tân - Quản lý dịch vụ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); 
        frame.setLocationRelativeTo(null);
        frame.add(new QuanLyDichVuDat());
        frame.setVisible(true);
    }
}