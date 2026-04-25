package com.wms.view.QuanLyDichVuDat; 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class QuanLyDatMonForm extends JPanel {

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

    public QuanLyDatMonForm() {
        initComponents();
        
        loadData(); 
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);

        JLabel lblTitle = new JLabel("QUẢN LÝ DỊCH VỤ ĐƯỢC ĐẶT");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSearch.setBackground(COLOR_PINK_BG);
        txtTimKiem = new JTextField(20);
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnTraCuu = createStyledButton("Tra cứu", COLOR_PINK);
        btnTraCuu.addActionListener(e -> traCuuDichVu());

        pnlSearch.add(new JLabel("Tìm mã phiên/ tên dịch vụ:"));
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTraCuu);
        pnlHeader.add(pnlSearch, BorderLayout.EAST);

        this.add(pnlHeader, BorderLayout.NORTH);

        String[] columnNames = {"Mã Phiên", "Tên Dịch Vụ", "Số Lượng", "Thời Gian Đặt", "Tên Khách Hàng", "Tên Không Gian", "Ghi Chú"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        tableDichVu = new JTable(tableModel);
     
        tableDichVu.setFont(new Font("Arial", Font.PLAIN, 15));
        tableDichVu.setRowHeight(35); 
        tableDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDichVu.setGridColor(COLOR_PINK_BORDER);

        JTableHeader header = tableDichVu.getTableHeader();
        header.setBackground(COLOR_PINK_LIGHT);
        header.setForeground(COLOR_TEXT_NAVY);
        header.setFont(new Font("Arial", Font.BOLD, 14));

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
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return btn;
    }

    // Mô phỏng
    private void loadData() {
        try {
            tableModel.setRowCount(0); 

            Object[][] mockData = {
                {"P001", "Cà phê đen đá", 2, "09:15 24/04/2026", "Nguyễn Văn A", "KG01 (Bàn đơn)", "Ít đường"},
                {"P001", "Thêm 1 giờ", 4, "09:15 24/04/2026", "Nguyễn Văn A", "KG01 (Bàn đơn)", ""},
                {"P002", "In tài liệu A4", 15, "10:00 24/04/2026", "Trần Thị B", "KG05 (Phòng họp)", "In trắng đen 2 mặt"},
                {"P005", "Trà dâu tằm", 1, "10:30 24/04/2026", "Khách vãng lai 01", "KG02 (Bàn đôi)", ""}
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
     
        JOptionPane.showMessageDialog(this, "Hệ thống đang chuyển hướng sang giao diện [Thêm dịch vụ được đặt]...");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        JFrame frame = new JFrame("Module Lễ Tân - Quản lý dịch vụ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(10000, 600); 
        frame.setLocationRelativeTo(null);
        frame.add(new QuanLyDatMonForm());
        frame.setVisible(true);
    }
}