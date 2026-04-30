package com.wms.view.QuanLyNhanSu;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ThemNhanVienForm extends JPanel {

    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_PINK_BORDER = Color.decode("#fccde0");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58);

    // Các trường nhập liệu
    private JTextField txtMaNV;
    private JTextField txtHoTen;
    private JTextField txtEmail;
    private JTextField txtSDT;
    private JComboBox<String> cboChucVu;
    private JComboBox<String> cboNhomQuyen;
    private JComboBox<String> cboTrangThai;
    
    // Khung ảnh
    private JLabel lblAnhDaiDien;
    private JButton btnChonAnh;

    // Nút thao tác
    private JButton btnThem;
    private JButton btnQuayLai;
    private JButton btnLamMoi;

    public ThemNhanVienForm() {
        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- PHẦN HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);
        JLabel lblTitle = new JLabel("THÊM NHÂN VIÊN MỚI");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        this.add(pnlHeader, BorderLayout.NORTH);

        // --- PHẦN TRUNG TÂM (Chứa Ảnh bên trái và Form bên phải) ---
        JPanel pnlCenter = new JPanel(new BorderLayout(20, 0));
        pnlCenter.setBackground(COLOR_PINK_BG);
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(20, 30, 80, 30)); 

        // 1. Cụm hiển thị ảnh đại diện (Bên trái)
        JPanel pnlImageContainer = new JPanel(new BorderLayout(0, 10));
        pnlImageContainer.setBackground(COLOR_PINK_BG);
        
        lblAnhDaiDien = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        lblAnhDaiDien.setPreferredSize(new Dimension(180, 220));
        lblAnhDaiDien.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        lblAnhDaiDien.setOpaque(true);
        lblAnhDaiDien.setBackground(Color.WHITE);
        lblAnhDaiDien.setForeground(Color.GRAY);
        
        btnChonAnh = createStyledButton("Chọn ảnh", Color.decode("#00003A"));
        btnChonAnh.setFont(new Font("Arial", Font.PLAIN, 13));
        btnChonAnh.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mở hộp thoại chọn file ảnh..."));

        pnlImageContainer.add(lblAnhDaiDien, BorderLayout.NORTH);
        pnlImageContainer.add(btnChonAnh, BorderLayout.CENTER);
        
        JPanel pnlImageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlImageWrapper.setBackground(COLOR_PINK_BG);
        pnlImageWrapper.add(pnlImageContainer);

        pnlCenter.add(pnlImageWrapper, BorderLayout.WEST);

        // 2. Cụm Form nhập liệu (Bên phải)
        JPanel pnlForm = new JPanel(new GridLayout(7, 2, 10, 15));
        pnlForm.setBackground(COLOR_PINK_BG);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PINK_DARK, 2), "Thông Tin Chi Tiết");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        titledBorder.setTitleColor(COLOR_PINK_DARK);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(20, 20, 20, 20)));

    

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

        // Chức vụ
        pnlForm.add(createLabel("Chức Vụ:"));
        String[] chucVuList = {"Lễ tân", "Quản lý"};
        cboChucVu = new JComboBox<>(chucVuList);
        cboChucVu.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(cboChucVu);

        // Nhóm Quyền
        pnlForm.add(createLabel("Nhóm Quyền:"));
        String[] nhomQuyenList = {"Quản trị viên", "Quản lý hội viên", "Quản lý dịch vụ", "Chăm sóc khách hàng"};
        cboNhomQuyen = new JComboBox<>(nhomQuyenList);
        cboNhomQuyen.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(cboNhomQuyen);

     

        pnlCenter.add(pnlForm, BorderLayout.CENTER);
        this.add(pnlCenter, BorderLayout.CENTER);

        // --- PHẦN NÚT BẤM KẾT THÚC (Dưới cùng) ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnQuayLai = createStyledButton("Quay Lại", Color.decode("#00003A"));
        btnLamMoi = createStyledButton("Làm Mới", Color.decode("#00003A"));
        btnThem = createStyledButton("Thêm Nhân Viên", Color.decode("#00003A"));

        btnQuayLai.addActionListener(e -> {
            Container parentContainer = this.getParent();
            if (parentContainer != null && parentContainer.getLayout() instanceof CardLayout) {
                CardLayout layout = (CardLayout) parentContainer.getLayout();
                layout.show(parentContainer, "QuanLyNhanVien");
            } else {
                JOptionPane.showMessageDialog(this, "Hệ thống sẽ chuyển về trang [Danh sách nhân viên]", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        btnLamMoi.addActionListener(e -> lamMoiForm());
        btnThem.addActionListener(e -> themNhanVien());

        pnlActions.add(btnQuayLai);
        pnlActions.add(btnLamMoi);
        pnlActions.add(btnThem);

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
        btn.setForeground((bgColor == COLOR_PINK_DARK || bgColor == Color.GRAY) ? Color.decode("#00003A") : Color.decode("#00003A"));
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private void lamMoiForm() {
        txtMaNV.setText("");
        txtHoTen.setText("");
        txtEmail.setText("");
        txtSDT.setText("");
        cboChucVu.setSelectedIndex(0);
        cboNhomQuyen.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
    }

    private void themNhanVien() {
        String maNV = txtMaNV.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSDT.getText().trim();
        
        if (maNV.isEmpty() || hoTen.isEmpty() || email.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin nhân viên!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Logic gọi DAO để insert vào DB (Oracle PL/SQL) ở đây
        String msg = String.format("Thêm nhân viên thành công!\n- Mã NV: %s\n- Họ tên: %s\n- Chức vụ: %s\n- Nhóm quyền: %s",
                maNV, hoTen, cboChucVu.getSelectedItem(), cboNhomQuyen.getSelectedItem());
                
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
        lamMoiForm(); // Reset form sau khi thêm thành công
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test View - Thêm Nhân Viên");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.add(new ThemNhanVienForm());
            frame.setVisible(true);
        });
    }
}