package com.wms.view.QuanLyNhanSu;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SuaNhanVienForm extends JPanel {

    // Bảng màu hồng đồng bộ hệ thống
    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_PINK_BORDER = Color.decode("#fccde0");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58);

    // Các trường nhập liệu
    private JTextField txtHoTen;
    private JTextField txtEmail;
    private JTextField txtSDT;
    private JComboBox<String> cboChucVu;
    private JComboBox<String> cboNhomQuyen;
    private JComboBox<String> cboTrangThai;
    
    // Khung ảnh
    private JLabel lblAnhDaiDien;
    private JButton btnDoiAnh;

    // Nút thao tác
    private JButton btnLuu;
    private JButton btnQuayLai;

    // Biến lưu ngầm Mã nhân viên (Không hiển thị trên UI)
    private String maNhanVienHienTai;

    public SuaNhanVienForm() {
        initComponents();
        // Giả lập load dữ liệu khi vừa mở form
        loadNhanVienData("NV001");
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- PHẦN HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);
        JLabel lblTitle = new JLabel("CẬP NHẬT THÔNG TIN NHÂN VIÊN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        this.add(pnlHeader, BorderLayout.NORTH);

        // --- PHẦN TRUNG TÂM (Chứa Ảnh bên trái và Form bên phải) ---
        JPanel pnlCenter = new JPanel(new BorderLayout(20, 0));
        pnlCenter.setBackground(COLOR_PINK_BG);
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(20, 30, 80, 30)); // Ép form gọn lại

        // 1. Cụm hiển thị ảnh đại diện (Bên trái)
        JPanel pnlImageContainer = new JPanel(new BorderLayout(0, 10));
        pnlImageContainer.setBackground(COLOR_PINK_BG);
        
        lblAnhDaiDien = new JLabel("Ảnh đại diện", SwingConstants.CENTER);
        lblAnhDaiDien.setPreferredSize(new Dimension(180, 220));
        lblAnhDaiDien.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        lblAnhDaiDien.setOpaque(true);
        lblAnhDaiDien.setBackground(Color.WHITE);
        lblAnhDaiDien.setForeground(Color.GRAY);
        
        btnDoiAnh = createStyledButton("Chọn ảnh khác", Color.decode("#00003A"));
        btnDoiAnh.setFont(new Font("Arial", Font.PLAIN, 13));
        btnDoiAnh.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mở hộp thoại chọn file ảnh..."));

        pnlImageContainer.add(lblAnhDaiDien, BorderLayout.NORTH);
        pnlImageContainer.add(btnDoiAnh, BorderLayout.CENTER);
        
        // Đặt panel ảnh vào một panel wrapper để nó không bị kéo giãn xuống tuốt dưới
        JPanel pnlImageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlImageWrapper.setBackground(COLOR_PINK_BG);
        pnlImageWrapper.add(pnlImageContainer);

        pnlCenter.add(pnlImageWrapper, BorderLayout.WEST);

        // 2. Cụm Form nhập liệu (Bên phải)
        JPanel pnlForm = new JPanel(new GridLayout(6, 2, 10, 20));
        pnlForm.setBackground(COLOR_PINK_BG);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PINK_DARK, 2), "Thông Tin Chi Tiết");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        titledBorder.setTitleColor(COLOR_PINK_DARK);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Họ tên
        pnlForm.add(createLabel("Họ và Tên:"));
        txtHoTen = new JTextField();
        txtHoTen.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(txtHoTen);

        // Email
        pnlForm.add(createLabel("Email:"));
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(txtEmail);

        // Số điện thoại
        pnlForm.add(createLabel("Số Điện Thoại:"));
        txtSDT = new JTextField();
        txtSDT.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(txtSDT);

        // Chức vụ (Dropdown)
        pnlForm.add(createLabel("Chức Vụ:"));
        String[] chucVuList = {"Lễ tân", "Quản lý"};
        cboChucVu = new JComboBox<>(chucVuList);
        cboChucVu.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(cboChucVu);

        // Nhóm Quyền (Dropdown)
        pnlForm.add(createLabel("Nhóm Quyền:"));
        String[] nhomQuyenList = {"Quản trị viên", "Quản lý hội viên", "Quản lý dịch vụ", "Chăm sóc khách hàng"};
        cboNhomQuyen = new JComboBox<>(nhomQuyenList);
        cboNhomQuyen.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(cboNhomQuyen);

        // Trạng Thái (Dropdown)
        pnlForm.add(createLabel("Trạng Thái:"));
        String[] trangThaiList = {"Còn làm việc", "Đã nghỉ việc"};
        cboTrangThai = new JComboBox<>(trangThaiList);
        cboTrangThai.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(cboTrangThai);

        pnlCenter.add(pnlForm, BorderLayout.CENTER);
        this.add(pnlCenter, BorderLayout.CENTER);

        // --- PHẦN NÚT BẤM KẾT THÚC (Dưới cùng) ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnQuayLai = createStyledButton("Quay Lại", Color.GRAY);
        btnLuu = createStyledButton("Lưu Thay Đổi", COLOR_PINK_DARK);

        btnQuayLai.addActionListener(e -> {
            // Lấy Container cha đang chứa SuaNhanVienForm hiện tại
            Container parentContainer = this.getParent();
            
            // Kiểm tra xem Container cha có tồn tại và đang sử dụng CardLayout hay không
            if (parentContainer != null && parentContainer.getLayout() instanceof CardLayout) {
                CardLayout layout = (CardLayout) parentContainer.getLayout();
                
                // Chuyển về thẻ có tên "QuanLyNhanVien" (Phải khớp chính xác chuỗi đã đặt ở main)
                layout.show(parentContainer, "QuanLyNhanVien");
            } else {
                // Hiển thị thông báo dự phòng nếu đang chạy file test (hàm main) độc lập
                JOptionPane.showMessageDialog(this, 
                    "Hệ thống sẽ chuyển về trang [Danh sách nhân viên] khi chạy trong MainLayout", 
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        btnLuu.addActionListener(e -> luuThongTin());

        pnlActions.add(btnQuayLai);
        pnlActions.add(btnLuu);

        this.add(pnlActions, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(COLOR_TEXT_NAVY);
        return lbl;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.decode("#00003A"));
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    // Hàm gọi khi chuyển từ trang danh sách sang trang sửa (Ví dụ bấm nút Sửa ở form Danh sách)
    public void loadNhanVienData(String maNV) {
        // Giữ mã nhân viên vào biến nội bộ, không đưa lên UI
        this.maNhanVienHienTai = maNV;
        
        // Giả lập dữ liệu fetch từ Database
        txtHoTen.setText("Nguyễn Văn A");
        txtEmail.setText("nva@wms.com");
        txtSDT.setText("0901234567");
        cboChucVu.setSelectedItem("Lễ tân");
        cboNhomQuyen.setSelectedItem("Quản lý hội viên");
        cboTrangThai.setSelectedItem("Còn làm việc");
        
        // Giả lập load ảnh lên (Trong thực tế sẽ dùng ImageIcon)
        lblAnhDaiDien.setText("[Ảnh NV001]");
    }

    private void luuThongTin() {
        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSDT.getText().trim();
        
        if (hoTen.isEmpty() || email.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Logic gọi DAO để update với biến maNhanVienHienTai
        String msg = String.format("Cập nhật thành công cho Mã NV: %s\n- Họ tên: %s\n- Chức vụ: %s\n- Nhóm quyền: %s",
                maNhanVienHienTai, hoTen, cboChucVu.getSelectedItem(), cboNhomQuyen.getSelectedItem());
                
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- MAIN TEST ---
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test View - Sửa Thông Tin Nhân Viên");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            
            // Add form vào để test
            frame.add(new SuaNhanVienForm());
            frame.setVisible(true);
        });
    }
}