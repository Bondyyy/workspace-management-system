package com.wms.view.QuanLyNhanSu;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SuaNhomQuyenForm extends JPanel {

    private final Color COLOR_PINK = Color.decode("#e85588");
    private final Color COLOR_PINK_DARK = Color.decode("#c73d6e");
    private final Color COLOR_PINK_BG = Color.decode("#fff0f5");
    private final Color COLOR_TEXT_NAVY = new Color(0, 0, 58);

    private JTextField txtTenNhom;
    private JTextField txtMoTa;
    private JComboBox<String> cboPhanQuyen;
    private JButton btnThemQuyen, btnXoaQuyen; // Thêm nút Xóa Quyền
    private JTable tableQuyenDaChon;
    private DefaultTableModel tableModel;
    private JButton btnLuu, btnHuy, btnQuayLai;

    // Dữ liệu giả lập các phân quyền trong hệ thống
    private final String[] danhSachTatCaQuyen = {
        "Quản lý đặt chỗ không gian", "Thanh toán hóa đơn", "Quản lý dịch vụ F&B", 
        "Quản lý khách hàng hội viên", "Quản lý nhân viên", "Quản lý phiếu giảm giá", 
        "Cập nhật trạng thái bảo trì", "Xem báo cáo doanh thu"
    };

    // Constructor nhận dữ liệu từ form Quản Lý truyền sang
    public SuaNhomQuyenForm(String tenNhomCu, String moTaCu) {
        initComponents();
        loadDataCu(tenNhomCu, moTaCu);
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(COLOR_PINK_BG);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- PANEL THÔNG TIN CHUNG ---
        JPanel pnlInfo = new JPanel(new GridLayout(2, 2, 10, 15));
        pnlInfo.setBackground(COLOR_PINK_BG);
        TitledBorder borderInfo = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_PINK_DARK, 2), "Cập Nhật Thông Tin Nhóm Quyền");
        borderInfo.setTitleFont(new Font("Arial", Font.BOLD, 14));
        borderInfo.setTitleColor(COLOR_PINK_DARK);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(borderInfo, BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        pnlInfo.add(createLabel("Tên Nhóm Quyền:"));
        txtTenNhom = new JTextField();
        txtTenNhom.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlInfo.add(txtTenNhom);

        pnlInfo.add(createLabel("Mô Tả Ngắn:"));
        txtMoTa = new JTextField();
        txtMoTa.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlInfo.add(txtMoTa);

        // --- PANEL CHỌN PHÂN QUYỀN ---
        JPanel pnlPhanQuyen = new JPanel(new BorderLayout(10, 10));
        pnlPhanQuyen.setBackground(COLOR_PINK_BG);
        TitledBorder borderQuyen = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_PINK_DARK, 2), "Chi Tiết Phân Quyền");
        borderQuyen.setTitleFont(new Font("Arial", Font.BOLD, 14));
        borderQuyen.setTitleColor(COLOR_PINK_DARK);
        pnlPhanQuyen.setBorder(BorderFactory.createCompoundBorder(borderQuyen, BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // Khu vực Dropdown Autocomplete và Nút Thêm/Xóa
        JPanel pnlChonQuyen = new JPanel(new BorderLayout(10, 0));
        pnlChonQuyen.setBackground(COLOR_PINK_BG);
        
        cboPhanQuyen = new JComboBox<>(danhSachTatCaQuyen);
        cboPhanQuyen.setFont(new Font("Arial", Font.PLAIN, 14));
        setupAutoComplete(cboPhanQuyen, danhSachTatCaQuyen); 
        
        JPanel pnlActionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlActionButtons.setBackground(COLOR_PINK_BG);

        btnThemQuyen = new JButton("Thêm Quyền");
        btnThemQuyen.setBackground(COLOR_PINK);
        btnThemQuyen.setForeground(Color.decode("#00003A"));
        btnThemQuyen.setFocusPainted(false);
        btnThemQuyen.setFont(new Font("Arial", Font.BOLD, 13));

        btnXoaQuyen = new JButton("Xóa Quyền Chọn");
        btnXoaQuyen.setBackground(Color.decode("#c73d6e"));
        btnXoaQuyen.setForeground(Color.decode("#00003A"));
        btnXoaQuyen.setFocusPainted(false);
        btnXoaQuyen.setFont(new Font("Arial", Font.BOLD, 13));
        
        pnlActionButtons.add(btnThemQuyen);
        pnlActionButtons.add(btnXoaQuyen);

        pnlChonQuyen.add(createLabel("Tìm & Chọn Quyền:"), BorderLayout.WEST);
        pnlChonQuyen.add(cboPhanQuyen, BorderLayout.CENTER);
        pnlChonQuyen.add(pnlActionButtons, BorderLayout.EAST);

        // Bảng hiển thị quyền đã chọn
        tableModel = new DefaultTableModel(new String[]{"STT", "Tên Phân Quyền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableQuyenDaChon = new JTable(tableModel);
        tableQuyenDaChon.setRowHeight(25);
        tableQuyenDaChon.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableQuyenDaChon.getColumnModel().getColumn(1).setPreferredWidth(450);
        JScrollPane scrollPane = new JScrollPane(tableQuyenDaChon);
        
        pnlPhanQuyen.add(pnlChonQuyen, BorderLayout.NORTH);
        pnlPhanQuyen.add(scrollPane, BorderLayout.CENTER);

        // Sự kiện Thêm/Xóa quyền
        btnThemQuyen.addActionListener(e -> xuLyThemQuyenVaoBang());
        btnXoaQuyen.addActionListener(e -> xuLyXoaQuyenKhoiBang());

        // Layout tổng phần giữa
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(COLOR_PINK_BG);
        pnlCenter.add(pnlInfo, BorderLayout.NORTH);
        pnlCenter.add(pnlPhanQuyen, BorderLayout.CENTER);
        this.add(pnlCenter, BorderLayout.CENTER);

        // --- PANEL NÚT BẤM DƯỚI CÙNG ---
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(COLOR_PINK_BG);

        // Nút Quay lại bên trái
        JPanel pnlQuayLai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlQuayLai.setBackground(COLOR_PINK_BG);
        btnQuayLai = new JButton("Quay lại");
        btnQuayLai.setBackground(Color.GRAY);
        btnQuayLai.setForeground(Color.decode("#00003A"));
        btnQuayLai.setFont(new Font("Arial", Font.BOLD, 14));
        btnQuayLai.setFocusPainted(false);
        pnlQuayLai.add(btnQuayLai);

        // Nút Lưu và Hủy bên phải
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setBackground(COLOR_PINK_BG);

        btnLuu = new JButton("Cập Nhật Nhóm Quyền");
        btnLuu.setBackground(COLOR_PINK_DARK);
        btnLuu.setForeground(Color.decode("#00003A"));
        btnLuu.setFont(new Font("Arial", Font.BOLD, 14));
        btnLuu.setFocusPainted(false);

        btnHuy = new JButton("Khôi Phục Ban Đầu"); // Nút hủy ở form sửa mang ý nghĩa reset data
        btnHuy.setBackground(Color.LIGHT_GRAY);
        btnHuy.setForeground(COLOR_TEXT_NAVY);
        btnHuy.setFont(new Font("Arial", Font.BOLD, 14));
        btnHuy.setFocusPainted(false);

        pnlActions.add(btnLuu);
        pnlActions.add(btnHuy);

        pnlBottom.add(pnlQuayLai, BorderLayout.WEST);
        pnlBottom.add(pnlActions, BorderLayout.EAST);
        this.add(pnlBottom, BorderLayout.SOUTH);

        // Gắn sự kiện cho các nút điều hướng
        btnHuy.addActionListener(e -> loadDataCu(txtTenNhom.getText(), txtMoTa.getText())); // Reset lại form
        btnLuu.addActionListener(e -> xuLyLuu());
        btnQuayLai.addActionListener(e -> xuLyQuayLai());
    }

    private void loadDataCu(String tenNhomCu, String moTaCu) {
        // Gán text
        txtTenNhom.setText(tenNhomCu);
        txtMoTa.setText(moTaCu);

        // Giả lập load danh sách quyền cũ của nhóm này từ DB lên bảng
        tableModel.setRowCount(0);
        // Tùy theo nhóm quyền mà mock data khác nhau (Ở thực tế bạn sẽ gọi SQL SELECT)
        if(tenNhomCu.contains("Quản trị")) {
             tableModel.addRow(new Object[]{1, "Quản lý nhân viên"});
             tableModel.addRow(new Object[]{2, "Xem báo cáo doanh thu"});
        } else {
             tableModel.addRow(new Object[]{1, "Quản lý đặt chỗ không gian"});
             tableModel.addRow(new Object[]{2, "Thanh toán hóa đơn"});
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(COLOR_TEXT_NAVY);
        return lbl;
    }

    private void setupAutoComplete(JComboBox<String> comboBox, String[] data) {
        comboBox.setEditable(true); 
        JTextField txtEditor = (JTextField) comboBox.getEditor().getEditorComponent();
        
        txtEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    return;
                }
                
                String input = txtEditor.getText(); 
                comboBox.removeAllItems(); 
                
                if (input.isEmpty()) {
                    for (String item : data) comboBox.addItem(item); 
                } else {
                    for (String item : data) {
                        if (item.toLowerCase().contains(input.toLowerCase())) {
                            comboBox.addItem(item);
                        }
                    }
                }
                
                txtEditor.setText(input);
                comboBox.showPopup();
            }
        });
    }

    private void xuLyThemQuyenVaoBang() {
        String quyenDuocChon = (String) cboPhanQuyen.getSelectedItem();
        
        if (quyenDuocChon == null || quyenDuocChon.trim().isEmpty()) {
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 1).equals(quyenDuocChon)) {
                JOptionPane.showMessageDialog(this, "Quyền này đã có trong danh sách!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int stt = tableModel.getRowCount() + 1;
        tableModel.addRow(new Object[]{stt, quyenDuocChon});
        
        JTextField txtEditor = (JTextField) cboPhanQuyen.getEditor().getEditorComponent();
        txtEditor.setText("");
    }

    private void xuLyXoaQuyenKhoiBang() {
        int selectedRow = tableQuyenDaChon.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một quyền trong bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Xóa dòng được chọn
        tableModel.removeRow(selectedRow);
        
        // Cập nhật lại cột STT
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(i + 1, i, 0);
        }
    }

    private void xuLyLuu() {
        String tenNhom = txtTenNhom.getText().trim();
        if (tenNhom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhóm quyền!");
            return;
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 phân quyền cho nhóm này!");
            return;
        }

        // Logic cập nhật vào CSDL sẽ viết ở đây (Update bảng VAITRO, Delete và Insert lại bảng CHITIETNHOMCHUCNANG...)
        
        JOptionPane.showMessageDialog(this, "Cập nhật nhóm quyền " + tenNhom + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        
        // Sau khi lưu xong thì tự động quay lại trang quản lý
        xuLyQuayLai();
    }

    private void xuLyQuayLai() {
        Container parentContainer = this.getParent();
        if (parentContainer != null) {
            parentContainer.remove(this);
            parentContainer.add(new QuanLyNhomQuyenForm());
            parentContainer.revalidate();
            parentContainer.repaint();
        }
    }

    // MAIN TEST ĐỘC LẬP
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Sửa Nhóm Quyền");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(750, 600);
            frame.setLocationRelativeTo(null);
            // Giả lập truyền dữ liệu cũ vào
            frame.add(new SuaNhomQuyenForm("Quản lý dịch vụ", "Có đầy đủ quyền liên quan tới dịch vụ"));
            frame.setVisible(true);
        });
    }
}