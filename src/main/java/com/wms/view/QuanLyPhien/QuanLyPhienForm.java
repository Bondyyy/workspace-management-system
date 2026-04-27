package com.wms.view.QuanLyPhien; 

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class QuanLyPhienForm extends JPanel {

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
        loadData(); 
    }

    private void initComponents() {
        // Thiết lập Layout trực tiếp cho JPanel này
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- PHẦN HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);

        JLabel lblTitle = new JLabel("QUẢN LÝ PHIÊN LÀM VIỆC");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22)); // Font chữ vừa vặn hơn
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlSearchContainer = new JPanel(new BorderLayout(0, 5));
        pnlSearchContainer.setBackground(COLOR_PINK_BG);

        JLabel lblSearchHint = new JLabel("Tìm mã phiên/ tên khách hàng:");
        lblSearchHint.setFont(new Font("Arial", Font.ITALIC, 13));
        lblSearchHint.setForeground(new Color(0, 0, 58));
        pnlSearchContainer.add(lblSearchHint, BorderLayout.NORTH); 

        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlInput.setBackground(COLOR_PINK_BG);
        
        txtTimKiem = new JTextField(15);
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnTraCuu = createStyledButton("Tra cứu");
        btnTraCuu.addActionListener(e -> traCuuPhien());

        pnlInput.add(txtTimKiem);
        pnlInput.add(Box.createHorizontalStrut(10)); // Khoảng trắng giữa textbox và nút
        pnlInput.add(btnTraCuu);

        pnlSearchContainer.add(pnlInput, BorderLayout.CENTER);
        pnlHeader.add(pnlSearchContainer, BorderLayout.EAST);

        this.add(pnlHeader, BorderLayout.NORTH);

        String[] columnNames = {"Mã Phiên", "Không Gian", "Khách Hàng", "T.Gian Bắt Đầu", "Dự Kiến Kết Thúc", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        tablePhienLamViec = new JTable(tableModel);
        tablePhienLamViec.setFont(new Font("Arial", Font.PLAIN, 14));
        tablePhienLamViec.setRowHeight(30);
        tablePhienLamViec.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePhienLamViec.setGridColor(COLOR_PINK_BORDER);

        TableColumnModel columnModel = tablePhienLamViec.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(70);  // Mã
        columnModel.getColumn(1).setPreferredWidth(100); // Không gian
        columnModel.getColumn(2).setPreferredWidth(120); // Khách Hàng
        columnModel.getColumn(3).setPreferredWidth(130); // TG Bắt đầu
        columnModel.getColumn(4).setPreferredWidth(130); // TG Kết thúc
        columnModel.getColumn(5).setPreferredWidth(100); // Trạng thái

        JTableHeader header = tablePhienLamViec.getTableHeader();
        header.setBackground(COLOR_PINK_LIGHT);
        header.setForeground(new Color(0, 0, 58));
        header.setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(tablePhienLamViec);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnTaiLai = createStyledButton("Tải lại");
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
                btn.setForeground(Color.WHITE); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(COLOR_PINK);
                btn.setForeground(new Color(0, 0, 58));
            }
        });
        return btn;
    }

    private void loadData() {
        try {
            tableModel.setRowCount(0); 

            // Dữ liệu đã được làm sạch, bỏ đi các mã số KG, KH
            Object[][] mockData = {
                {"P001", "Bàn đơn", "Nguyễn Văn A", "08:00 24/04/2026", "12:00 24/04/2026", "Đang hoạt động"},
                {"P002", "Phòng họp", "Trần Thị B", "09:00 24/04/2026", "11:00 24/04/2026", "Đã đặt trước"},
                {"P003", "Bàn đôi", "Lê Văn C", "07:30 24/04/2026", "10:30 24/04/2026", "Đã kết thúc"}
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
            JOptionPane.showMessageDialog(this, "Vui lòng nhập thông tin để tra cứu.");
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
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một phiên làm việc trên bảng để xem chi tiết.", 
                "Cảnh báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maPhien = (String) tableModel.getValueAt(selectedRow, 0);
        
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;

        ChiTietPhienForm chiTietDialog = new ChiTietPhienForm(parentFrame, maPhien);
        chiTietDialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
           
            JFrame frame = new JFrame("Test View - Quản lý phiên");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            
            frame.add(new QuanLyPhienForm()); 
            frame.setVisible(true);
        });
    }
}