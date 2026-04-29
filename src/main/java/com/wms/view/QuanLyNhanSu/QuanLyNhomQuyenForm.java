package com.wms.view.QuanLyNhanSu;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class QuanLyNhomQuyenForm extends JPanel {

    // Bảng màu hồng đồng bộ hệ thống
    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_LIGHT = Color.decode("#f07baa");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_PINK_BORDER = Color.decode("#fccde0");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58);

    private JTable tableNhomQuyen;
    private DefaultTableModel tableModel;
    // Đã thêm btnQuayLai vào danh sách khai báo
    private JButton btnTraCuu, btnSua, btnTaiLai, btnThemMoi, btnQuayLai; 
    private JTextField txtTimKiem;

    public QuanLyNhomQuyenForm() {
        initComponents();
        loadData(); 
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- PHẦN HEADER (Tiêu đề và Tìm kiếm) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÓM QUYỀN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlSearchContainer = new JPanel(new BorderLayout(0, 5));
        pnlSearchContainer.setBackground(COLOR_PINK_BG);

        JLabel lblSearchHint = new JLabel("Tìm kiếm tên nhóm quyền:");
        lblSearchHint.setFont(new Font("Arial", Font.ITALIC, 13));
        lblSearchHint.setForeground(COLOR_TEXT_NAVY);
        pnlSearchContainer.add(lblSearchHint, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlInput.setBackground(COLOR_PINK_BG);
        
        txtTimKiem = new JTextField(15);
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnTraCuu = createStyledButton("Tìm kiếm", COLOR_PINK);
        pnlInput.add(txtTimKiem);
        pnlInput.add(Box.createHorizontalStrut(10));
        pnlInput.add(btnTraCuu);

        pnlSearchContainer.add(pnlInput, BorderLayout.CENTER);
        pnlHeader.add(pnlSearchContainer, BorderLayout.EAST);

        this.add(pnlHeader, BorderLayout.NORTH);

        // --- PHẦN BẢNG DỮ LIỆU ---
        // Thay đổi 1: Bỏ "Mã Nhóm" khỏi mảng tiêu đề cột
        String[] columnNames = {"Tên Nhóm Quyền", "Mô Tả Ngắn Gọn"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        tableNhomQuyen = new JTable(tableModel);
        tableNhomQuyen.setFont(new Font("Arial", Font.PLAIN, 14));
        tableNhomQuyen.setRowHeight(40); 
        tableNhomQuyen.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableNhomQuyen.setGridColor(COLOR_PINK_BORDER);

        // Thay đổi 1 (tiếp): Canh chỉnh lại độ rộng cột do chỉ còn 2 cột
        TableColumnModel columnModel = tableNhomQuyen.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(250); // Tên nhóm
        columnModel.getColumn(1).setPreferredWidth(500); // Mô tả (chiếm diện tích lớn nhất)

        JTableHeader header = tableNhomQuyen.getTableHeader();
        header.setBackground(COLOR_PINK_LIGHT);
        header.setForeground(COLOR_TEXT_NAVY);
        header.setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(tableNhomQuyen);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        this.add(scrollPane, BorderLayout.CENTER);

        // --- PHẦN NÚT BẤM THAO TÁC ---
        // Sử dụng BorderLayout cho pnlBottom để chia 2 bên: Trái (Quay lại), Phải (Các nút chức năng)
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(COLOR_PINK_BG);

        // Nút Quay lại đặt bên trái
        JPanel pnlQuayLai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlQuayLai.setBackground(COLOR_PINK_BG);
        btnQuayLai = createStyledButton("Quay lại", Color.GRAY);

        // Các nút chức năng đặt bên phải
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnTaiLai = createStyledButton("Tải lại", Color.decode("#00003A"));
        btnThemMoi = createStyledButton("Thêm nhóm quyền", Color.decode("#00003A"));
        btnSua = createStyledButton("Sửa nhóm quyền", Color.decode("#00003A")); 

        // Đăng ký sự kiện
        btnTaiLai.addActionListener(e -> loadData());
        btnSua.addActionListener(e -> suaNhomQuyen());
        
        // Thay đổi 3: Gắn sự kiện mở form ThêmNhómQuyền
        btnThemMoi.addActionListener(e -> moFormThemNhomQuyen());

        // Thay đổi 2: Gắn sự kiện cho nút Quay lại
        

        pnlActions.add(btnTaiLai);
        pnlActions.add(btnThemMoi);
        pnlActions.add(btnSua);

        pnlBottom.add(pnlQuayLai, BorderLayout.WEST);
        pnlBottom.add(pnlActions, BorderLayout.EAST);

        this.add(pnlBottom, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground((bgColor == COLOR_PINK_DARK || bgColor == Color.GRAY) ? Color.WHITE : COLOR_TEXT_NAVY);
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
            // Thay đổi 1 (tiếp): Dữ liệu mẫu đã loại bỏ "Mã nhóm" (như ROLE_ADMIN)
            Object[][] mockData = {
                {"Quản trị viên", "Có đầy đủ các quyền"},
                {"Quản lý hội viên", "Có đầy đủ quyền liên quan tới hội viên"},
                {"Quản lý dịch vụ", "Có đầy đủ quyền liên quan tới dịch vụ"},
                {"Chăm sóc khách hàng", "Có đầy đủ quyền liên quan tới chăm sóc khách hàng"}
            };
            for (Object[] row : mockData) {
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void suaNhomQuyen() {
        int row = tableNhomQuyen.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm quyền cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy dữ liệu từ dòng được chọn trong JTable
        String tenNhomCu = (String) tableModel.getValueAt(row, 0);
        String moTaCu = (String) tableModel.getValueAt(row, 1);

        // Chuyển panel
        Container parentContainer = this.getParent();
        if (parentContainer != null) {
            parentContainer.remove(this);
            
            // Khởi tạo form sửa và truyền tham số cũ vào
            SuaNhomQuyenForm pnlSua = new SuaNhomQuyenForm(tenNhomCu, moTaCu);
            parentContainer.add(pnlSua);
            
            parentContainer.revalidate();
            parentContainer.repaint();
        }
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN MỚI ---

    private void moFormThemNhomQuyen() {
       
        Container parentContainer = this.getParent();

        if (parentContainer != null) {
           
            parentContainer.remove(this);

            ThemNhomQuyenForm pnlThem = new ThemNhomQuyenForm();

            parentContainer.add(pnlThem);

            parentContainer.revalidate();
            parentContainer.repaint();
        }
    }

    

    // --- MAIN TEST ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hệ thống Quản lý - Test Nhóm Quyền");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.add(new QuanLyNhomQuyenForm());
            frame.setVisible(true);
        });
    }
}