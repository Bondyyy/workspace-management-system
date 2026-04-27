package com.wms.view.QuanLyDichVu; 
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

public class ThemDichVuForm extends JDialog {

    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58);

    private JTextField txtMaPhien;
    private JComboBox<String> cboLoaiDichVu;
    private JComboBox<String> cboTenDichVu;
    private JSpinner spinSoLuong;
    private JTextField txtGhiChu;
    private JButton btnLuu, btnHuy;

    private Map<String, String[]> dichVuDataMap;

    public ThemDichVuForm(Frame parent) {
        super(parent, "Thêm Dịch Vụ Cho Phiên Làm Việc", true);
        initMockData();
        initComponents();
    }

    private void initMockData() {
        dichVuDataMap = new HashMap<>();
        dichVuDataMap.put("Gia hạn thời gian", new String[]{"Thêm 1 giờ"});
        dichVuDataMap.put("Đồ uống (F&B)", new String[]{"Cà phê đen đá", "Cà phê sữa đá", "Trà dâu tằm", "Nước suối"});
        dichVuDataMap.put("Đồ ăn (F&B)", new String[]{"Bánh mì ốp la", "Mì xào bò", "Bánh ngọt"});
        dichVuDataMap.put("Tiện ích & Thiết bị", new String[]{"Thuê máy chiếu", "In tài liệu A4", "Thuê màn hình rời"});
    }

    private void initComponents() {
        this.setSize(500, 400);
        this.setLocationRelativeTo(getParent());
        this.setLayout(new BorderLayout(10, 10));
        this.getContentPane().setBackground(COLOR_PINK_BG);

        JPanel pnlForm = new JPanel(new GridLayout(5, 2, 10, 20));
        pnlForm.setBackground(COLOR_PINK_BG);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PINK_DARK, 2), "Thông Tin Dịch Vụ Mới");
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        titledBorder.setTitleColor(COLOR_PINK_DARK);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // 1. Mã phiên
        pnlForm.add(createLabel("Mã Phiên:"));
        txtMaPhien = new JTextField();
        txtMaPhien.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(txtMaPhien);

        // 2. Loại Dịch Vụ (Thanh xổ xuống)
        pnlForm.add(createLabel("Loại Dịch Vụ:"));
        String[] loaiDVs = dichVuDataMap.keySet().toArray(new String[0]);
        cboLoaiDichVu = new JComboBox<>(loaiDVs);
        cboLoaiDichVu.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(cboLoaiDichVu);

        // 3. Tên Dịch Vụ (Thanh xổ xuống - Cập nhật theo Loại)
        pnlForm.add(createLabel("Tên Dịch Vụ:"));
        cboTenDichVu = new JComboBox<>();
        cboTenDichVu.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(cboTenDichVu);

        // Xử lý sự kiện khi đổi Loại Dịch Vụ
        cboLoaiDichVu.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateDichVuList((String) e.getItem());
            }
        });
        // Cập nhật lần đầu
        updateDichVuList((String) cboLoaiDichVu.getSelectedItem());

        // 4. Số lượng (Dùng JSpinner để chống nhập chữ và số âm)
        pnlForm.add(createLabel("Số Lượng:"));
        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1); // Mặc định 1, min 1, max 100, bước nhảy 1
        spinSoLuong = new JSpinner(spinnerModel);
        spinSoLuong.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(spinSoLuong);

        // 5. Ghi chú
        pnlForm.add(createLabel("Ghi Chú:"));
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlForm.add(txtGhiChu);

        this.add(pnlForm, BorderLayout.CENTER);

        // --- PANEL NÚT BẤM ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnLuu = new JButton("Thêm Dịch Vụ");
        btnLuu.setBackground(COLOR_PINK_DARK);
        btnLuu.setForeground(Color.decode("#00003A"));
        btnLuu.setFont(new Font("Arial", Font.BOLD, 14));
        btnLuu.setFocusPainted(false);

        btnHuy = new JButton("Hủy");
        btnHuy.setBackground(Color.GRAY);
        btnHuy.setForeground(Color.decode("#00003A"));
        btnHuy.setFont(new Font("Arial", Font.BOLD, 14));
        btnHuy.setFocusPainted(false);

        btnHuy.addActionListener(e -> this.dispose());
        btnLuu.addActionListener(e -> xuLyThemDichVu());

        pnlActions.add(btnLuu);
        pnlActions.add(btnHuy);

        this.add(pnlActions, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(COLOR_TEXT_NAVY);
        return lbl;
    }

    private void updateDichVuList(String loaiDV) {
        cboTenDichVu.removeAllItems();
        String[] dsDichVu = dichVuDataMap.get(loaiDV);
        if (dsDichVu != null) {
            for (String dv : dsDichVu) {
                cboTenDichVu.addItem(dv);
            }
        }
    }

    private void xuLyThemDichVu() {
        String maPhien = txtMaPhien.getText().trim();
        String loaiDV = (String) cboLoaiDichVu.getSelectedItem();
        String tenDV = (String) cboTenDichVu.getSelectedItem();
        int soLuong = (int) spinSoLuong.getValue();
        String ghiChu = txtGhiChu.getText().trim();

        if (maPhien.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã Phiên để thêm dịch vụ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String thongBao = String.format("Ghi nhận dịch vụ thành công!\n- Mã Phiên: %s\n- Dịch vụ: %s\n- Số lượng: %d\n- Ghi chú: %s", 
                                        maPhien, tenDV, soLuong, ghiChu.isEmpty() ? "Không có" : ghiChu);
                                        
        // Nhấn mạnh nếu là gia hạn
        if (loaiDV.equals("Gia hạn thời gian") && tenDV.equals("Thêm 1 giờ")) {
            thongBao += "\n\n*Hệ thống sẽ cộng thêm " + soLuong + " giờ vào phiên làm việc hiện tại.";
        }

        JOptionPane.showMessageDialog(this, thongBao, "Thành công", JOptionPane.INFORMATION_MESSAGE);
        this.dispose(); // Đóng form sau khi thành công
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ThemDichVuForm dialog = new ThemDichVuForm(frame);
            dialog.setVisible(true);
        });
    }
}