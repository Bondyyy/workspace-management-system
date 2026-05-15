package com.wms.util;

import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public class SoDoKhongGianPanel extends JPanel {
    private final int MAX_COLS = 12;
    private final int MAX_ROWS = 8;
    private final int DON_VI = 85;

    private final Color mauHongChinh = Color.decode("#EB5E8D");
    private final Color mauTrong_Nen = Color.decode("#FFF0F5");
    private final Color mauTrong_Vien = Color.decode("#E0E0E0");
    private final Color mauDaDat_Nen = Color.decode("#FFF3E0");
    private final Color mauDaDat_Vien = Color.decode("#FFB74D");
    private final Color mauDangDung_Nen = Color.decode("#E8F5E9");
    private final Color mauDangDung_Vien = Color.decode("#66BB6A");
    private final Color mauBaoTri_Nen = Color.decode("#F5F5F5");
    private final Color mauBaoTri_Vien = Color.decode("#9E9E9E");
    private final Color mauLeTan = Color.decode("#34495E");
    private final Color mauChuDam = Color.decode("#231E30");

    private boolean managementMode = false;
    private Consumer<KhongGianDTO> onTableClick;
    private String selectedMaKG = null;

    public SoDoKhongGianPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
    }

    public void setManagementMode(boolean mode) {
        this.managementMode = mode;
        setPreferredSize(new Dimension(MAX_COLS * DON_VI + 50, MAX_ROWS * DON_VI + 50));
    }

    public void setOnTableClick(Consumer<KhongGianDTO> onTableClick) {
        this.onTableClick = onTableClick;
    }

    public void veSoDo(List<KhongGianDTO> danhSachKG, String selectedMaKG) {
        this.selectedMaKG = selectedMaKG;
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0); // Không dùng insets bên ngoài để tránh lệch lưới
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Quầy Lễ Tân (Mặc định ở 0,0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        JLabel lblLeTan = new JLabel("RECEPTION", SwingConstants.CENTER);
        lblLeTan.setOpaque(true);
        lblLeTan.setBackground(mauLeTan);
        lblLeTan.setForeground(Color.WHITE);
        lblLeTan.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblLeTan.setPreferredSize(new Dimension(DON_VI * 2, DON_VI));
        if (managementMode) lblLeTan.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(lblLeTan, gbc);

        // Lối vào (ENTRANCE)
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel lblEntrance = new JLabel("ENTRANCE", SwingConstants.CENTER);
        lblEntrance.setOpaque(true);
        lblEntrance.setBackground(new Color(240, 240, 240));
        lblEntrance.setForeground(new Color(150, 150, 150));
        lblEntrance.setFont(new Font("Segoe UI", Font.BOLD, 8));
        lblEntrance.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        lblEntrance.setPreferredSize(new Dimension(DON_VI, DON_VI));
        add(lblEntrance, gbc);

        // Vẽ các ô không gian
        if (danhSachKG != null) {
            for (KhongGianDTO kg : danhSachKG) {
                gbc.gridx = kg.getToaDoX();
                gbc.gridy = kg.getToaDoY();
                gbc.gridwidth = kg.getChieuDai() > 0 ? kg.getChieuDai() : 1;
                gbc.gridheight = kg.getChieuRong() > 0 ? kg.getChieuRong() : 1;

                boolean isSelected = selectedMaKG != null && selectedMaKG.equals(kg.getMaKG());
            JButton nutKG = taoNutKhongGian(kg, isSelected);
                add(nutKG, gbc);
            }
        }

        // Ép kích thước các cột
        for (int i = 0; i < MAX_COLS; i++) {
            GridBagConstraints gbcCol = new GridBagConstraints();
            gbcCol.gridx = i; gbcCol.gridy = MAX_ROWS;
            add(Box.createRigidArea(new Dimension(DON_VI, 0)), gbcCol);
        }
        // Ép kích thước các hàng
        for (int j = 0; j < MAX_ROWS; j++) {
            GridBagConstraints gbcRow = new GridBagConstraints();
            gbcRow.gridx = MAX_COLS; gbcRow.gridy = j;
            add(Box.createRigidArea(new Dimension(0, DON_VI)), gbcRow);
        }
        // Filler cuối cùng để đẩy tất cả về Top-Left (NorthWest)
        GridBagConstraints gbcFiller = new GridBagConstraints();
        gbcFiller.gridx = MAX_COLS;
        gbcFiller.gridy = MAX_ROWS;
        gbcFiller.weightx = 1.0;
        gbcFiller.weighty = 1.0;
        add(Box.createRigidArea(new Dimension(0, 0)), gbcFiller);

        revalidate();
        repaint();
    }

    private JButton taoNutKhongGian(KhongGianDTO kg, boolean isSelected) {
        JButton nutKG = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int margin = 5; // Tạo khoảng trống nhỏ giữa các ô (lối đi)
                int w = getWidth() - 2 * margin;
                int h = getHeight() - 2 * margin;

                // Vẽ nền bo góc
                g2.setColor(getBackground());
                g2.fillRoundRect(margin, margin, w, h, 15, 15);
                
                // Vẽ viền bo góc
                Color vien = (Color) getClientProperty("mauVien");
                if (vien != null) {
                    g2.setColor(vien);
                    g2.setStroke(new BasicStroke(isSelected ? 3 : 1));
                    g2.drawRoundRect(margin, margin, w - 1, h - 1, 15, 15);
                }
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        String line1 = kg.getMaKG();
        String line2 = kg.getTenKG() != null ? kg.getTenKG() : "";
        String line3 = kg.getTenLoaiKG() != null ? kg.getTenLoaiKG() : "";
        
        String htmlText = String.format("<html><center>" + 
                "<div style='font-family: Segoe UI;'>" +
                "<span style='font-size: 8pt;'>%s</span><br/>" +
                "<b><span style='font-size: 8pt;'>%s</span></b><br/>" +
                "<i style='font-size: 7pt;'>%s</i>" +
                "</div></center></html>", 
                line1, line2, line3);
        
        nutKG.setText(htmlText);
        nutKG.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        nutKG.setFocusPainted(false);
        nutKG.setPreferredSize(new Dimension(DON_VI * (kg.getChieuDai() > 0 ? kg.getChieuDai() : 1), 
                                           DON_VI * (kg.getChieuRong() > 0 ? kg.getChieuRong() : 1)));

        nutKG.setContentAreaFilled(false);
        nutKG.setOpaque(false);
        nutKG.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        Color mauNenBtn = mauTrong_Nen, mauVienBtn = mauTrong_Vien, mauChu = mauChuDam;

        if (kg.getMaKG().equals(selectedMaKG)) {
            mauNenBtn = mauHongChinh;
            mauVienBtn = mauHongChinh;
            mauChu = Color.WHITE;
        } else {
            String status = kg.getTrangThaiKG() != null ? kg.getTrangThaiKG().trim() : "Trống";
            switch (status) {
                case "Trống":
                    mauNenBtn = mauTrong_Nen;
                    mauVienBtn = mauTrong_Vien;
                    mauChu = Color.decode("#c73d6e");
                    break;
                case "Đã đặt trước":
                    mauNenBtn = mauDaDat_Nen;
                    mauVienBtn = mauDaDat_Vien;
                    mauChu = Color.decode("#E65100");
                    break;
                case "Đang hoạt động":
                    mauNenBtn = mauDangDung_Nen;
                    mauVienBtn = mauDangDung_Vien;
                    mauChu = Color.decode("#2E7D32");
                    break;
                case "Bảo trì":
                    mauNenBtn = mauBaoTri_Nen;
                    mauVienBtn = mauBaoTri_Vien;
                    mauChu = Color.GRAY;
                    break;
            }
        }

        nutKG.setBackground(mauNenBtn);
        nutKG.putClientProperty("mauVien", isSelected ? mauHongChinh : mauVienBtn);
        nutKG.setForeground(mauChu);
        
        if (!managementMode) {
            nutKG.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }

        nutKG.addActionListener(e -> {
            if (onTableClick != null) {
                onTableClick.accept(kg);
            }
        });

        return nutKG;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (managementMode) {
            // Vẽ lưới ô vuông cho chế độ Quản lý
            g2.setColor(new Color(245, 245, 245));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            g2.setColor(new Color(220, 220, 220));
            for (int i = 0; i <= MAX_COLS; i++) {
                int x = i * DON_VI;
                g2.drawLine(x, 0, x, MAX_ROWS * DON_VI);
                if (i < MAX_COLS) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g2.drawString("X:" + i, x + 5, 12);
                }
            }
            for (int j = 0; j <= MAX_ROWS; j++) {
                int y = j * DON_VI;
                g2.drawLine(0, y, MAX_COLS * DON_VI, y);
                if (j < MAX_ROWS) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g2.drawString("Y:" + j, 2, y + 12);
                }
            }
        } else {
            // Vẽ đường kẻ chéo cho chế độ Xem
            g2.setColor(new Color(254, 252, 253));
            g2.fillRect(0, 0, MAX_COLS * DON_VI, MAX_ROWS * DON_VI);

            g2.setColor(new Color(235, 235, 235, 100));
            g2.setStroke(new BasicStroke(1.0f));

            int gap = 15;
            for (int i = -MAX_ROWS * DON_VI; i < MAX_COLS * DON_VI; i += gap) {
                g2.drawLine(i, 0, i + MAX_ROWS * DON_VI, MAX_ROWS * DON_VI);
            }
        }

        g2.setColor(new Color(235, 94, 141, 50));
        g2.drawRect(0, 0, MAX_COLS * DON_VI, MAX_ROWS * DON_VI);

        g2.dispose();
    }
}
