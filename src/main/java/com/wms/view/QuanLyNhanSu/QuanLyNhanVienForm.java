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
    private JButton btnTraCuu, btnSua, btnXoa, btnTaiLai;
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
        
        txtTimKiemMa = new JTextField(15); // Nới rộng ô text một chút để nhập tên cho thoải mái
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
        // Cập nhật cấu trúc: Thêm "Chức Vụ"
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

        // Chia lại độ rộng các cột cho 7 cột vừa vặn với 800px
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

        // --- PHẦN NÚT BẤM (Sửa và Xóa) ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnTaiLai = createStyledButton("Tải lại", Color.decode("#00003A"));
        btnSua = createStyledButton("Sửa thông tin nhân viên", COLOR_PINK);
        btnXoa = createStyledButton("Xóa nhân viên", Color.decode("#00003A"));

        btnTaiLai.addActionListener(e -> loadData());
        btnSua.addActionListener(e -> suaNhanVien());
        btnXoa.addActionListener(e -> xoaNhanVien());

        pnlActions.add(btnTaiLai);
        pnlActions.add(btnSua);
        pnlActions.add(btnXoa);

        this.add(pnlActions, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground((bgColor == COLOR_PINK_DARK || bgColor == Color.GRAY) ? Color.WHITE : COLOR_TEXT_NAVY);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    private void loadData() {
        try {
            tableModel.setRowCount(0); 
            // Giả lập dữ liệu: Tách biệt rõ Chức vụ (Lễ tân/Quản lý) và Nhóm Quyền trên hệ thống
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

    private void suaNhanVien() {
        int row = tableNhanVien.getSelectedRow();
        
        if (row == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn nhân viên cần sửa trên bảng!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNV = (String) tableModel.getValueAt(row, 0);
        
        // 3. Lấy Container cha (nơi setup CardLayout)
        Container parentContainer = this.getParent();
        
        // 4. Kiểm tra và thực hiện chuyển trang
        if (parentContainer != null && parentContainer.getLayout() instanceof CardLayout) {
            
            // Tìm form SuaNhanVienForm trong các component của parentContainer để truyền dữ liệu
            for (Component comp : parentContainer.getComponents()) {
                if (comp instanceof SuaNhanVienForm) {
                    ((SuaNhanVienForm) comp).loadNhanVienData(maNV);
                    break;
                }
            }
            
            CardLayout layout = (CardLayout) parentContainer.getLayout();
            layout.show(parentContainer, "SuaNhanVien");
            
        } else {
            // Trường hợp dự phòng khi bạn đang chạy file test (hàm main) độc lập, chưa nhúng vào CardLayout tổng
            JOptionPane.showMessageDialog(this, 
                "Đã bắt được sự kiện Sửa!\n(Hệ thống sẽ nạp dữ liệu của Mã NV [" + maNV + "] và chuyển sang [Sửa nhân viên] khi chạy trong MainLayout)", 
                "Thông báo chuyển trang", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void xoaNhanVien() {
        int row = tableNhanVien.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maNV = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn xóa nhân viên " + maNV + " không?", 
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            
            JOptionPane.showMessageDialog(this, "Đã xóa nhân viên thành công.");
        }
    }

    // --- MAIN TEST ---
    // --- MAIN TEST ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test View - Quản lý nhân viên");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            
            JPanel mainPanel = new JPanel(new CardLayout());
            
            mainPanel.add(new QuanLyNhanVienForm(), "QuanLyNhanVien");
         
            mainPanel.add(new SuaNhanVienForm(), "SuaNhanVien"); 

            frame.add(mainPanel);

            frame.setVisible(true);
        });
    }
}