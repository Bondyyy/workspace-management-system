package com.wms.view.QuanLyKho;

import com.wms.controller.QuanLyKhoController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.List;

public class ThemVaoKhoForm extends JDialog {

    private JComboBox<String> cbLoaiDichVu;
    private JComboBox<String> cbTenDichVu;
    private JComboBox<String> cbNhanVien;
    private JSpinner spnSoLuong;
    
    private JLabel lblChungTuTitle;
    private JPanel pnlFileContainer;
    private JButton btnChonFile;
    private JLabel lblTrangThaiFile;
    private JButton btnSuaFile;
    
    private JButton btnLuu;
    private JButton btnHuy;

    private File currentSelectedFile = null;
    private QuanLyKhoController controller;

    public ThemVaoKhoForm(JFrame parent, QuanLyKhoController controller) {
        super(parent, "Thêm Sản Phẩm Vào Kho", true);
        this.controller = controller;
        initComponents();
        loadDataNhanVienVaLoaiDV(); // Đổ dữ liệu tĩnh ban đầu
        setupDynamicBehavior(); // Thiết lập sự kiện thay đổi Form và Cascade Combobox
        setSize(550, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 5));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 10, 30));

        JLabel lblTitle = new JLabel("NHẬP KHO DỊCH VỤ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(new Color(51, 51, 51));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 10, 8, 10);

        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        Font inputFont = new Font("SansSerif", Font.PLAIN, 14);

        // 1. Nhân viên
        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("Nhân viên nhập:"){ {setFont(labelFont);} }, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        cbNhanVien = new JComboBox<>();
        cbNhanVien.setEditable(true);
        cbNhanVien.setFont(inputFont);
        cbNhanVien.setPreferredSize(new Dimension(280, 35));
        pnlForm.add(cbNhanVien, gbc);

        // 2. Loại dịch vụ
        gbc.gridx = 0; gbc.gridy = 1;
        pnlForm.add(new JLabel("Loại dịch vụ:"){ {setFont(labelFont);} }, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        cbLoaiDichVu = new JComboBox<>();
        cbLoaiDichVu.setEditable(true);
        cbLoaiDichVu.setFont(inputFont);
        cbLoaiDichVu.setPreferredSize(new Dimension(280, 35));
        pnlForm.add(cbLoaiDichVu, gbc);

        // 3. Tên dịch vụ
        gbc.gridx = 0; gbc.gridy = 2;
        pnlForm.add(new JLabel("Tên dịch vụ:"){ {setFont(labelFont);} }, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        cbTenDichVu = new JComboBox<>();
        cbTenDichVu.setEditable(true);
        cbTenDichVu.setFont(inputFont);
        cbTenDichVu.setPreferredSize(new Dimension(280, 35));
        pnlForm.add(cbTenDichVu, gbc);

        // 4. Số lượng
        gbc.gridx = 0; gbc.gridy = 3;
        pnlForm.add(new JLabel("Số lượng thêm:"){ {setFont(labelFont);} }, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        spnSoLuong.setFont(inputFont);
        spnSoLuong.setPreferredSize(new Dimension(280, 35));
        pnlForm.add(spnSoLuong, gbc);

        // 5. Chứng từ (File)
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        lblChungTuTitle = new JLabel("Giấy/Hóa đơn:");
        lblChungTuTitle.setFont(labelFont);
        lblChungTuTitle.setBorder(new EmptyBorder(8, 0, 0, 0));
        pnlForm.add(lblChungTuTitle, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        
        pnlFileContainer = new JPanel();
        pnlFileContainer.setLayout(new BoxLayout(pnlFileContainer, BoxLayout.Y_AXIS));
        pnlFileContainer.setBackground(Color.WHITE);

        btnChonFile = new JButton("Chọn Ảnh/File...");
        btnChonFile.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnChonFile.setMaximumSize(new Dimension(130, 35));
        btnChonFile.setPreferredSize(new Dimension(130, 35));
        btnChonFile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChonFile.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel pnlTrangThaiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTrangThaiRow.setBackground(Color.WHITE);
        pnlTrangThaiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTrangThaiRow.setBorder(new EmptyBorder(5, 0, 0, 0));

        lblTrangThaiFile = new JLabel("Chưa chọn file (Bắt buộc)");
        lblTrangThaiFile.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblTrangThaiFile.setForeground(Color.RED);
        lblTrangThaiFile.setPreferredSize(new Dimension(220, 25));
        
        btnSuaFile = new JButton("Sửa");
        btnSuaFile.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnSuaFile.setPreferredSize(new Dimension(55, 25));
        btnSuaFile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSuaFile.setVisible(false);

        pnlTrangThaiRow.add(lblTrangThaiFile);
        pnlTrangThaiRow.add(btnSuaFile);
        pnlFileContainer.add(btnChonFile);
        pnlFileContainer.add(pnlTrangThaiRow);

        pnlForm.add(pnlFileContainer, gbc);

        ActionListener chonFileAction = e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn hóa đơn / chứng từ");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Ảnh & PDF (*.jpg, *.png, *.pdf)", "jpg", "jpeg", "png", "pdf"));
            
            if (fileChooser.showOpenDialog(ThemVaoKhoForm.this) == JFileChooser.APPROVE_OPTION) {
                currentSelectedFile = fileChooser.getSelectedFile();
                String fileName = currentSelectedFile.getName();
                lblTrangThaiFile.setText("Thành công: " + shortenString(fileName, 20));
                lblTrangThaiFile.setForeground(new Color(0, 128, 0));
                lblTrangThaiFile.setToolTipText(fileName);
                btnSuaFile.setVisible(true);
            }
        };

        btnChonFile.addActionListener(chonFileAction);
        btnSuaFile.addActionListener(chonFileAction);

        mainPanel.add(pnlForm, BorderLayout.CENTER);

        // ================= FOOTER =================
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        pnlButtons.setBackground(Color.WHITE);
        pnlButtons.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnLuu = new JButton("Xác nhận nhập");
        btnLuu.setPreferredSize(new Dimension(140, 38));
        btnLuu.setBackground(new Color(13, 110, 253));
        btnLuu.setForeground(Color.black);
        btnLuu.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLuu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnHuy = new JButton("Hủy bỏ");
        btnHuy.setPreferredSize(new Dimension(100, 38));
        btnHuy.setBackground(new Color(108, 117, 125));
        btnHuy.setForeground(Color.black);
        btnHuy.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> xuLyNhapKho());

        pnlButtons.add(btnLuu);
        pnlButtons.add(btnHuy);
        mainPanel.add(pnlButtons, BorderLayout.SOUTH);

        this.add(mainPanel);
    }

    /**
     * Nạp dữ liệu Cố định từ DB (Nhân viên, Loại DV) bằng DefaultComboBoxModel
     * Dùng Model sẽ giúp tránh lỗi không hiển thị xổ xuống khi setEditable(true)
     */
    private void loadDataNhanVienVaLoaiDV() {
        if (controller == null) return;
        
        // 1. Nạp Nhân viên
        List<String> dsNhanVien = controller.getDSNhanVien();
        DefaultComboBoxModel<String> nvModel = new DefaultComboBoxModel<>();
        nvModel.addElement(""); // Dòng trống ban đầu
        for (String nv : dsNhanVien) nvModel.addElement(nv);
        cbNhanVien.setModel(nvModel);

        // 2. Nạp Loại Dịch Vụ
        List<String> dsLoai = controller.getDSLoaiDichVu();
        DefaultComboBoxModel<String> loaiModel = new DefaultComboBoxModel<>();
        loaiModel.addElement("");
        for (String loai : dsLoai) loaiModel.addElement(loai);
        cbLoaiDichVu.setModel(loaiModel);
    }

    /**
     * Bắt sự kiện khi Loại Dịch Vụ thay đổi -> Load Dịch Vụ tương ứng + Ẩn/hiện File
     */
    private void setupDynamicBehavior() {
        // Lắng nghe thao tác CLICK chọn item từ chuột
        cbLoaiDichVu.addActionListener(e -> {
            updateTheoLoaiDichVu();
        });

        // Lắng nghe thao tác GÕ PHÍM (khi bấm ra khỏi ô nhập liệu)
        Component editorComp = cbLoaiDichVu.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField) {
            JTextField txtField = (JTextField) editorComp;
            txtField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateTheoLoaiDichVu();
                }
            });
        }
    }

    /**
     * Hàm trung tâm xử lý khi Loại DV thay đổi
     */
    private void updateTheoLoaiDichVu() {
        Object item = cbLoaiDichVu.getSelectedItem();
        String loaiDV = (item != null) ? item.toString().trim() : "";
        
        // 1. Ẩn / Hiện Form File
        boolean isTienIch = "Tiện ích".equalsIgnoreCase(loaiDV);
        lblChungTuTitle.setVisible(!isTienIch);
        pnlFileContainer.setVisible(!isTienIch);
        this.revalidate();
        this.repaint();
        
        // 2. Load lại Combo Tên Dịch Vụ dựa theo Loại Dịch Vụ
        if (controller != null && !loaiDV.isEmpty()) {
            List<String> dsTen = controller.getDSTenDichVuTheoLoai(loaiDV);
            DefaultComboBoxModel<String> tenModel = new DefaultComboBoxModel<>();
            tenModel.addElement("");
            for (String ten : dsTen) {
                tenModel.addElement(ten);
            }
            // Lưu lại text đang gõ (nếu có) để không bị mất
            Object currentTyped = cbTenDichVu.getSelectedItem();
            cbTenDichVu.setModel(tenModel);
            if (currentTyped != null && !currentTyped.toString().isEmpty()) {
                cbTenDichVu.setSelectedItem(currentTyped);
            }
        } else {
            // Nếu xóa rỗng ô Loại DV thì xóa luôn danh sách Tên DV
            cbTenDichVu.setModel(new DefaultComboBoxModel<>(new String[]{""}));
        }
    }

    private String shortenString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        int half = (maxLength - 3) / 2;
        return str.substring(0, half) + "..." + str.substring(str.length() - half);
    }

    private void xuLyNhapKho() {
        Object nvSelected = cbNhanVien.getSelectedItem();
        Object loaiSelected = cbLoaiDichVu.getSelectedItem();
        Object tenSelected = cbTenDichVu.getSelectedItem();
        
        String nhanVien = (nvSelected != null) ? nvSelected.toString().trim() : "";
        String loaiDichVu = (loaiSelected != null) ? loaiSelected.toString().trim() : "";
        String tenDichVu = (tenSelected != null) ? tenSelected.toString().trim() : "";
        int soLuong = (int) spnSoLuong.getValue();
        
        if (nhanVien.isEmpty() || loaiDichVu.isEmpty() || tenDichVu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập/chọn đầy đủ Nhân viên, Loại dịch vụ và Tên dịch vụ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isTienIch = "Tiện ích".equalsIgnoreCase(loaiDichVu);
        
        // KIỂM TRA BẮT BUỘC CHỨNG TỪ (NẾU KHÁC TIỆN ÍCH)
        if (!isTienIch && currentSelectedFile == null) {
            JOptionPane.showMessageDialog(this, "Loại dịch vụ '" + loaiDichVu + "' bắt buộc phải đính kèm File chứng từ/hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tenFile = (currentSelectedFile != null && !isTienIch) ? currentSelectedFile.getName() : null;

        // Lưu vào DB
        if (controller.nhapKho(nhanVien, loaiDichVu, tenDichVu, soLuong, tenFile)) {
            JOptionPane.showMessageDialog(this, "Nhập kho thành công!\nSản phẩm: " + tenDichVu + "\nSố lượng: " + soLuong, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); 
        } else {
            JOptionPane.showMessageDialog(this, "Nhập kho thất bại! Vui lòng kiểm tra lại kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}