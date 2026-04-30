package com.wms.view.QuanLyNhanSu;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class QuanLyNhanVienForm extends JPanel {

    // Bảng màu hồng đồng bộ hệ thống
    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_LIGHT = Color.decode("#f07baa");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_PINK_BORDER = Color.decode("#fccde0");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58);

    private JTable tableNhanVien;
    private DefaultTableModel tableModel;
    // Thêm btnXemChiTiet vào khai báo
    private JButton btnTraCuu, btnXemChiTiet, btnThem, btnSua; 
    private JTextField txtTimKiemMa;

    public QuanLyNhanVienForm() {
        initComponents();
        loadData(); 
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- PHẦN HEADER (Tiêu đề và Tra cứu mã/tên) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // Vùng tra cứu mã hoặc tên nhân viên
        JPanel pnlSearchContainer = new JPanel(new BorderLayout(0, 5));
        pnlSearchContainer.setBackground(COLOR_PINK_BG);

        JLabel lblSearchHint = new JLabel("Nhập mã hoặc tên nhân viên để tra cứu:");
        lblSearchHint.setFont(new Font("Arial", Font.ITALIC, 13));
        lblSearchHint.setForeground(COLOR_TEXT_NAVY);
        pnlSearchContainer.add(lblSearchHint, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlInput.setBackground(COLOR_PINK_BG);
        
        txtTimKiemMa = new JTextField(15);
        txtTimKiemMa.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnTraCuu = createStyledButton("Tìm kiếm", COLOR_PINK);
        btnTraCuu.addActionListener(e -> traCuuNhanVien());

        pnlInput.add(txtTimKiemMa);
        pnlInput.add(Box.createHorizontalStrut(10));
        pnlInput.add(btnTraCuu);

        pnlSearchContainer.add(pnlInput, BorderLayout.CENTER);
        pnlHeader.add(pnlSearchContainer, BorderLayout.EAST);

        this.add(pnlHeader, BorderLayout.NORTH);

        // --- PHẦN BẢNG DỮ LIỆU ---
        String[] columnNames = {"Mã NV", "Họ Tên", "Email", "Số Điện Thoại", "Chức Vụ", "Nhóm Quyền", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        tableNhanVien = new JTable(tableModel);
        tableNhanVien.setFont(new Font("Arial", Font.PLAIN, 14));
        tableNhanVien.setRowHeight(35); 
        tableNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableNhanVien.setGridColor(COLOR_PINK_BORDER);

        TableColumnModel columnModel = tableNhanVien.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);  // Mã NV
        columnModel.getColumn(1).setPreferredWidth(140); // Họ Tên
        columnModel.getColumn(2).setPreferredWidth(140); // Email
        columnModel.getColumn(3).setPreferredWidth(100); // SĐT
        columnModel.getColumn(4).setPreferredWidth(80);  // Chức Vụ
        columnModel.getColumn(5).setPreferredWidth(120); // Nhóm Quyền
        columnModel.getColumn(6).setPreferredWidth(100); // Trạng Thái

        JTableHeader header = tableNhanVien.getTableHeader();
        header.setBackground(COLOR_PINK_LIGHT);
        header.setForeground(COLOR_TEXT_NAVY);
        header.setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(tableNhanVien);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        this.add(scrollPane, BorderLayout.CENTER);

        // --- PHẦN NÚT BẤM (Xem chi tiết, Thêm, Sửa) ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        // Khởi tạo 3 nút
        btnXemChiTiet = createStyledButton("Xem chi tiết", Color.decode("#00003A"));
        btnThem = createStyledButton("Thêm nhân viên", Color.decode("#00003A"));
        btnSua = createStyledButton("Sửa thông tin", Color.decode("#00003A"));

        // Gán sự kiện cho các nút
        btnXemChiTiet.addActionListener(e -> xemChiTietNhanVien());
        btnThem.addActionListener(e -> themNhanVien());
        btnSua.addActionListener(e -> suaNhanVien());

        pnlActions.add(btnXemChiTiet);
        pnlActions.add(btnThem);
        pnlActions.add(btnSua);

        this.add(pnlActions, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.decode("#00003A")); // Đổi text thành màu trắng cho nền tối dễ nhìn
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private void loadData() {
        try {
            tableModel.setRowCount(0); 
            Object[][] mockData = {
                {"NV001", "Nguyễn Văn A", "nva@wms.com", "0901234567", "Lễ tân", "Quản lý hội viên", "Còn làm việc"},
                {"NV002", "Trần Thị B", "ttb@wms.com", "0907654321", "Quản lý", "Quản trị viên", "Còn làm việc"},
                {"NV003", "Lê Văn C", "lvc@wms.com", "0988888888", "Lễ tân", "Quản lý dịch vụ", "Đã nghỉ việc"}
            };
            for (Object[] row : mockData) {
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void traCuuNhanVien() {
        String tuKhoa = txtTimKiemMa.getText().trim();
        if (tuKhoa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã hoặc tên nhân viên để tìm kiếm!");
            return;
        }
        JOptionPane.showMessageDialog(this, "Đang tra cứu nhân viên với từ khóa: " + tuKhoa);
    }

    // --- HÀM XỬ LÝ XEM CHI TIẾT ---
    private void xemChiTietNhanVien() {
        int row = tableNhanVien.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xem chi tiết trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy thông tin cơ bản từ bảng để truyền sang form chi tiết
        String maNV = (String) tableModel.getValueAt(row, 0);
        String hoTen = (String) tableModel.getValueAt(row, 1);
        String chucVu = (String) tableModel.getValueAt(row, 4);
        String trangThai = (String) tableModel.getValueAt(row, 6);
        
        Container parent = this.getParent();
        while (parent != null) {
            if (parent.getLayout() instanceof CardLayout) {
                for (Component comp : parent.getComponents()) {
                    if (comp instanceof ChiTietNhanVienForm) {
                        ((ChiTietNhanVienForm) comp).loadThongTinChiTiet(maNV, hoTen, trangThai, chucVu);
                        break;
                    }
                }
                CardLayout layout = (CardLayout) parent.getLayout();
                layout.show(parent, "ChiTietNhanVien"); 
                return; 
            }
            parent = parent.getParent(); 
        }
        JOptionPane.showMessageDialog(this, "Sẽ chuyển sang [Xem chi tiết nhân viên] của mã: " + maNV, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void themNhanVien() {
        Container parent = this.getParent();
        while (parent != null) {
            if (parent.getLayout() instanceof CardLayout) {
                CardLayout layout = (CardLayout) parent.getLayout();
                layout.show(parent, "ThemNhanVien"); 
                return; 
            }
            parent = parent.getParent(); 
        }
        JOptionPane.showMessageDialog(this, "Hệ thống sẽ chuyển sang màn hình [Thêm Nhân Viên]", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void suaNhanVien() {
        int row = tableNhanVien.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maNV = (String) tableModel.getValueAt(row, 0);
        
        Container parent = this.getParent();
        while (parent != null) {
            if (parent.getLayout() instanceof CardLayout) {
                for (Component comp : parent.getComponents()) {
                    if (comp instanceof SuaNhanVienForm) {
                        ((SuaNhanVienForm) comp).loadNhanVienData(maNV);
                        break;
                    }
                }
                CardLayout layout = (CardLayout) parent.getLayout();
                layout.show(parent, "SuaNhanVien");
                return;
            }
            parent = parent.getParent(); 
        }
        JOptionPane.showMessageDialog(this, "Sẽ chuyển sang [Sửa nhân viên] của mã: " + maNV, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- MAIN TEST ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test View - Quản lý nhân viên");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new CardLayout());
            
            mainPanel.add(new QuanLyNhanVienForm(), "QuanLyNhanVien");
            mainPanel.add(new SuaNhanVienForm(), "SuaNhanVien"); 
            mainPanel.add(new ThemNhanVienForm(), "ThemNhanVien"); 
            
            // Đã thêm ChiTietNhanVienForm vào CardLayout để test
            mainPanel.add(new ChiTietNhanVienForm(), "ChiTietNhanVien"); 

            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }
}