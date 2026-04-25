package com.wms.view.QuanLyPhien; 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class QuanLyPhienForm extends JPanel {

    // Định nghĩa bảng màu theo yêu cầu thiết kế
    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_LIGHT = Color.decode("#f07baa");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_PINK_BORDER = Color.decode("#fccde0");

    private JTable tablePhienLamViec;
    private DefaultTableModel tableModel;
    private JButton btnTraCuu, btnMoPhien, btnXemChiTiet, btnTaiLai;
    private JTextField txtTimKiem;

    public QuanLyPhienForm() {
        initComponents();
        // Giả lập 
        loadData(); 
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);

        JLabel lblTitle = new JLabel("QUẢN LÝ PHIÊN LÀM VIỆC");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSearch.setBackground(COLOR_PINK_BG);
        txtTimKiem = new JTextField(20);
        btnTraCuu = createStyledButton("Tra cứu");
        
        btnTraCuu.addActionListener(e -> traCuuPhien());

        pnlSearch.add(new JLabel("Mã phiên/Mã KH:"));
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTraCuu);
        pnlHeader.add(pnlSearch, BorderLayout.EAST);

        this.add(pnlHeader, BorderLayout.NORTH);

        String[] columnNames = {"Mã Phiên", "Mã Không Gian", "Mã Khách Hàng", "T.Gian Bắt Đầu", "Dự Kiến Kết Thúc", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        
        tablePhienLamViec = new JTable(tableModel);
        tablePhienLamViec.setFont(new Font("Arial", Font.PLAIN, 15));
        tablePhienLamViec.setRowHeight(30);
        tablePhienLamViec.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePhienLamViec.setGridColor(COLOR_PINK_BORDER);

        JTableHeader header = tablePhienLamViec.getTableHeader();
        header.setBackground(COLOR_PINK_LIGHT);
        header.setForeground(new Color(0, 0, 58));
        header.setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(tablePhienLamViec);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnTaiLai = createStyledButton("Tải lại danh sách");
        btnMoPhien = createStyledButton("Mở phiên trực tiếp");
        btnXemChiTiet = createStyledButton("Xem chi tiết");

        btnTaiLai.addActionListener(e -> loadData());
        btnMoPhien.addActionListener(e -> moPhienTrucTiep());
        btnXemChiTiet.addActionListener(e -> xemChiTietPhien());

        pnlActions.add(btnTaiLai);
        pnlActions.add(btnMoPhien);
        pnlActions.add(btnXemChiTiet);

        this.add(pnlActions, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_PINK);
        btn.setForeground(new Color(0, 0, 58));
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(COLOR_PINK_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(COLOR_PINK);
            }
        });
        return btn;
    }

    /** Mô phỏng **/
    private void loadData() {
        try {
         
            tableModel.setRowCount(0); 

            // Giả lập dữ liệu truy xuất thành công từ CSDL [Bảng PHIENLAMVIEC]
            Object[][] mockData = {
                {"P001", "KG01 (Bàn đơn)", "KH001", "08:00 24/04/2026", "12:00 24/04/2026", "Đang hoạt động"},
                {"P002", "KG05 (Phòng họp)", "KH008", "09:00 24/04/2026", "11:00 24/04/2026", "Đã đặt trước"},
                {"P003", "KG02 (Bàn đôi)", "KH012", "07:30 24/04/2026", "10:30 24/04/2026", "Đã kết thúc"}
            };

            for (Object[] row : mockData) {
                tableModel.addRow(row);
            }
            
        } catch (Exception ex) {
         
            JOptionPane.showMessageDialog(this, 
                "Không thể tải danh sách phiên làm việc, vui lòng kiểm tra lại kết nối cơ sở dữ liệu.", 
                "Lỗi hệ thống", 
                JOptionPane.ERROR_MESSAGE);
        }
    }


    private void traCuuPhien() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã phiên hoặc mã khách hàng để tra cứu.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Hệ thống đang chuyển hướng tới luồng xử lý Use-case [Tra cứu phiên làm việc] cho từ khóa: " + keyword);
    }

    private void moPhienTrucTiep() {
        JOptionPane.showMessageDialog(this, "Hệ thống đang kích hoạt Use-case [Mở phiên làm việc trực tiếp]...");
    }

    private void xemChiTietPhien() {
        int selectedRow = tablePhienLamViec.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiên làm việc trên bảng để xem chi tiết.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maPhien = (String) tableModel.getValueAt(selectedRow, 0);
        
        JOptionPane.showMessageDialog(this, "Hệ thống đang kích hoạt Use-case [Xem chi tiết phiên làm việc] cho Mã Phiên: " + maPhien);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        JFrame frame = new JFrame("Module Lễ Tân - Hệ thống quản lý không gian");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.add(new QuanLyPhienForm());
        frame.setVisible(true);
    }
}