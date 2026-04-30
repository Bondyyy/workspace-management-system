package com.wms.view.QuanLyNhanSu;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ChiTietNhanVienForm extends JPanel {

    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_PINK_BORDER = Color.decode("#fccde0");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58);

    // Các trường hiển thị dữ liệu (Chỉ đọc)
    private JTextField txtMaNV;
    private JTextField txtHoTen;
    private JTextField txtNgayVaoLam;
    private JTextField txtTrangThai;
    private JTextField txtLuongCoBan;
    private JTextField txtMaNguoiQuanLy;
    
    // Label cho Mã người quản lý để dễ dàng ẩn/hiện
    private JLabel lblMaNguoiQuanLy;

    // Khung ảnh
    private JLabel lblAnhDaiDien;

    // Nút thao tác
    private JButton btnQuayLai;

    public ChiTietNhanVienForm() {
        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- PHẦN HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PINK_BG);
        JLabel lblTitle = new JLabel("CHI TIẾT NHÂN VIÊN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_PINK_DARK);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        this.add(pnlHeader, BorderLayout.NORTH);

        // --- PHẦN TRUNG TÂM ---
        JPanel pnlCenter = new JPanel(new BorderLayout(20, 0));
        pnlCenter.setBackground(COLOR_PINK_BG);
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(20, 30, 80, 30)); 

        // 1. Cụm hiển thị ảnh đại diện (Bên trái)
        JPanel pnlImageContainer = new JPanel(new BorderLayout(0, 10));
        pnlImageContainer.setBackground(COLOR_PINK_BG);
        
        lblAnhDaiDien = new JLabel("Ảnh đại diện", SwingConstants.CENTER);
        lblAnhDaiDien.setPreferredSize(new Dimension(180, 220));
        lblAnhDaiDien.setBorder(BorderFactory.createLineBorder(COLOR_PINK_BORDER, 2));
        lblAnhDaiDien.setOpaque(true);
        lblAnhDaiDien.setBackground(Color.WHITE);
        lblAnhDaiDien.setForeground(Color.GRAY);

        pnlImageContainer.add(lblAnhDaiDien, BorderLayout.NORTH);
        
        JPanel pnlImageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlImageWrapper.setBackground(COLOR_PINK_BG);
        pnlImageWrapper.add(pnlImageContainer);

        pnlCenter.add(pnlImageWrapper, BorderLayout.WEST);

        // 2. Cụm Form hiển thị liệu (Bên phải)
        JPanel pnlForm = new JPanel(new GridLayout(6, 2, 10, 20));
        pnlForm.setBackground(COLOR_PINK_BG);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PINK_DARK, 2), "Thông Tin Nội Bộ");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        titledBorder.setTitleColor(COLOR_PINK_DARK);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Họ và Tên
        pnlForm.add(createLabel("Họ và Tên:"));
        txtHoTen = createReadOnlyTextField();
        pnlForm.add(txtHoTen);

        // Ngày vào làm
        pnlForm.add(createLabel("Ngày Vào Làm:"));
        txtNgayVaoLam = createReadOnlyTextField();
        pnlForm.add(txtNgayVaoLam);

        // Trạng thái làm việc
        pnlForm.add(createLabel("Trạng Thái:"));
        txtTrangThai = createReadOnlyTextField();
        pnlForm.add(txtTrangThai);

        // Lương cơ bản
        pnlForm.add(createLabel("Lương Cơ Bản:"));
        txtLuongCoBan = createReadOnlyTextField();
        pnlForm.add(txtLuongCoBan);

        // Mã người quản lý (Sẽ ẩn/hiện tùy điều kiện)
        lblMaNguoiQuanLy = createLabel("Mã Người Quản Lý:");
        pnlForm.add(lblMaNguoiQuanLy);
        txtMaNguoiQuanLy = createReadOnlyTextField();
        pnlForm.add(txtMaNguoiQuanLy);

        pnlCenter.add(pnlForm, BorderLayout.CENTER);
        this.add(pnlCenter, BorderLayout.CENTER);

        // --- PHẦN NÚT BẤM KẾT THÚC (Dưới cùng) ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnQuayLai = new JButton("Quay Lại");
        btnQuayLai.setBackground(Color.decode("#00003A"));
        btnQuayLai.setForeground(Color.decode("#00003A"));
        btnQuayLai.setFont(new Font("Arial", Font.BOLD, 14));
        btnQuayLai.setFocusPainted(false);
        btnQuayLai.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        btnQuayLai.addActionListener(e -> {
            Container parent = this.getParent();
            while (parent != null) {
                if (parent.getLayout() instanceof CardLayout) {
                    CardLayout layout = (CardLayout) parent.getLayout();
                    layout.show(parent, "QuanLyNhanVien"); 
                    return; 
                }
                parent = parent.getParent(); 
            }
            JOptionPane.showMessageDialog(this, "Hệ thống sẽ chuyển về trang [Danh sách nhân viên]", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        pnlActions.add(btnQuayLai);
        this.add(pnlActions, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(COLOR_TEXT_NAVY);
        return lbl;
    }

    // Hàm tạo TextField nhưng bị khóa (chỉ để xem)
    private JTextField createReadOnlyTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Arial", Font.BOLD, 14));
        txt.setForeground(Color.DARK_GRAY);
        txt.setBackground(Color.WHITE);
        txt.setEditable(false); // Không cho phép sửa
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return txt;
    }

    // Hàm được gọi từ màn hình Danh sách truyền sang
    public void loadThongTinChiTiet(String maNV, String hoTen, String trangThai, String chucVu) {
        // 1. Giả lập query Database để lấy các thông tin nội bộ dựa trên maNV
        // Dưới đây là gán cứng (mock data) để test UI
        
        txtHoTen.setText(hoTen);
        txtTrangThai.setText(trangThai);
        txtNgayVaoLam.setText("15/02/2026"); // Mock data
        txtLuongCoBan.setText("8,500,000 VND"); // Mock data
        lblAnhDaiDien.setText("[Ảnh của " + maNV + "]");

        // 2. Logic kiểm tra: Nếu là Lễ tân thì hiện Mã quản lý, ngược lại ẩn đi
        if (chucVu.equalsIgnoreCase("Lễ tân")) {
            lblMaNguoiQuanLy.setVisible(true);
            txtMaNguoiQuanLy.setVisible(true);
            txtMaNguoiQuanLy.setText("QL001"); // Mock data mã quản lý
        } else {
            // Là Quản lý hoặc chức vụ khác thì ẩn thông tin này
            lblMaNguoiQuanLy.setVisible(false);
            txtMaNguoiQuanLy.setVisible(false);
            txtMaNguoiQuanLy.setText("");
        }
    }
}