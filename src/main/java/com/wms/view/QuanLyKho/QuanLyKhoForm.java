package com.wms.view.QuanLyKho;

import com.wms.controller.QuanLyKhoController;
import com.wms.model.VanHanh_DichVu.DichVuDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.List;

public class QuanLyKhoForm extends JPanel {

    private JTable tblKho;
    private DefaultTableModel tableModel;
    private JButton btnNhapKho;
    private JTextField txtTimKiem;
    private JButton btnTimKiem;
    private String placeholder = "Tìm kiếm tên dịch vụ...";

    private QuanLyKhoController controller;

    public QuanLyKhoForm() {
        initComponents();
        controller = new QuanLyKhoController(this);
        controller.loadData(""); 
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlHeader = new JPanel(new BorderLayout(10, 10));
        pnlHeader.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHO DỊCH VỤ");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(51, 51, 51));

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSearch.setBackground(Color.WHITE);

        txtTimKiem = new JTextField(placeholder, 25);
        txtTimKiem.setPreferredSize(new Dimension(300, 35));
        txtTimKiem.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtTimKiem.setForeground(Color.GRAY);

        txtTimKiem.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtTimKiem.getText().equals(placeholder)) {
                    txtTimKiem.setText("");
                    txtTimKiem.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtTimKiem.getText().isEmpty()) {
                    txtTimKiem.setForeground(Color.GRAY);
                    txtTimKiem.setText(placeholder);
                }
            }
        });
        
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) thucHienTimKiem();
            }
        });

        btnTimKiem = new JButton("Tìm kiếm");
        btnTimKiem.setPreferredSize(new Dimension(100, 35));
        btnTimKiem.setBackground(new Color(108, 117, 125));
        btnTimKiem.setForeground(Color.black);
        btnTimKiem.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnTimKiem.setFocusPainted(false);
        btnTimKiem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnTimKiem.addActionListener(e -> thucHienTimKiem());

        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTimKiem);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlSearch, BorderLayout.EAST);

        String[] columnNames = {
            "STT", "Mã DV", "Tên Dịch Vụ", "Loại DV", "Đơn Giá (VNĐ)", "Số Lượng Tồn", "Trạng Thái"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKho = new JTable(tableModel);
        tblKho.setRowHeight(35);
        tblKho.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tblKho.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tblKho.getTableHeader().setBackground(new Color(245, 245, 245));
        tblKho.getTableHeader().setPreferredSize(new Dimension(100, 40));
        tblKho.setSelectionBackground(new Color(199, 61, 110)); 
        tblKho.setSelectionForeground(Color.black); 
        tblKho.setShowVerticalLines(false);
        tblKho.setShowHorizontalLines(true);
        tblKho.setGridColor(new Color(230, 230, 230));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        tblKho.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblKho.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblKho.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); 
        tblKho.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tblKho.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        tblKho.getColumnModel().getColumn(0).setMaxWidth(50);
        tblKho.getColumnModel().getColumn(1).setPreferredWidth(100); 
        tblKho.getColumnModel().getColumn(3).setPreferredWidth(120); 
        tblKho.getColumnModel().getColumn(4).setPreferredWidth(110); 
        tblKho.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tblKho);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnNhapKho = new JButton(" Thêm vào kho");
        btnNhapKho.setPreferredSize(new Dimension(150, 40)); 
        btnNhapKho.setBackground(new Color(13, 110, 253));
        btnNhapKho.setForeground(Color.black); 
        btnNhapKho.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnNhapKho.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnNhapKho.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window window = SwingUtilities.getWindowAncestor(QuanLyKhoForm.this);
                JFrame parentFrame = (window instanceof JFrame) ? (JFrame) window : null;
                
                // ĐÃ FIX: Truyền controller vào để Form con có thể gọi lấy dữ liệu Database
                ThemVaoKhoForm themKhoDialog = new ThemVaoKhoForm(parentFrame, controller);
                themKhoDialog.setVisible(true);
                
                // Load lại dữ liệu
                controller.loadData(""); 
            }
        });

        pnlBottom.add(btnNhapKho);

        this.add(pnlHeader, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(pnlBottom, BorderLayout.SOUTH);
    }

    private void thucHienTimKiem() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.equals(placeholder)) keyword = "";
        controller.loadData(keyword);
    }

    public void hienThiDuLieu(List<DichVuDTO> danhSach) {
        if (danhSach != null && !danhSach.isEmpty()) {
            danhSach.sort((d1, d2) -> {
                boolean isThueGio1 = "Thuê thêm giờ".equalsIgnoreCase(d1.getTenDV());
                boolean isThueGio2 = "Thuê thêm giờ".equalsIgnoreCase(d2.getTenDV());
                if (isThueGio1 && !isThueGio2) return -1;
                if (!isThueGio1 && isThueGio2) return 1;

                boolean isTienIch1 = "Tiện ích".equalsIgnoreCase(d1.getTenLoaiDV());
                boolean isTienIch2 = "Tiện ích".equalsIgnoreCase(d2.getTenLoaiDV());
                if (isTienIch1 && !isTienIch2) return -1;
                if (!isTienIch1 && isTienIch2) return 1;

                String ten1 = d1.getTenDV() != null ? d1.getTenDV() : "";
                String ten2 = d2.getTenDV() != null ? d2.getTenDV() : "";
                return ten1.compareToIgnoreCase(ten2);
            });
        }

        tableModel.setRowCount(0); 
        int stt = 1;
        DecimalFormat df = new DecimalFormat("#,###"); 

        for (DichVuDTO dv : danhSach) {
            String hienThiSoLuong = (dv.getSoLuong() == null) ? "-" : String.valueOf(dv.getSoLuong());
            tableModel.addRow(new Object[]{
                stt++, dv.getMaDV(), dv.getTenDV(), dv.getTenLoaiDV(),
                df.format(dv.getDonGia()), hienThiSoLuong, dv.getTrangThaiDV()
            });
        }
    }

    public void hienThiThongBaoLoi(String thongBao) {
        JOptionPane.showMessageDialog(this, thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        // Thiết lập giao diện (Look and Feel) cho giống với hệ điều hành đang dùng
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chạy giao diện trên Event Dispatch Thread (EDT) chuẩn của Swing
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kiểm tra giao diện Quản Lý Kho");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600); // Kích thước cửa sổ test
            frame.setLocationRelativeTo(null); // Hiển thị ở giữa màn hình

            // Khởi tạo Form và thêm vào Frame
            QuanLyKhoForm form = new QuanLyKhoForm();
            frame.add(form);

            // Hiển thị Frame
            frame.setVisible(true);
        });
    }
}