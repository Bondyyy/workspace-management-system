package com.wms.view.components;

import com.wms.model.KhongGianDTO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public class SoDoKhongGianPanel extends JPanel {
    private final int MAX_COLS = 12;
    private final int MAX_ROWS = 8;
    private final int DON_VI = 50;

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

    private Consumer<KhongGianDTO> onTableClick;
    private String selectedMaKG = null;

    public SoDoKhongGianPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(MAX_COLS * DON_VI, MAX_ROWS * DON_VI));
    }

    public void setOnTableClick(Consumer<KhongGianDTO> onTableClick) {
        this.onTableClick = onTableClick;
    }

    public void veSoDo(List<KhongGianDTO> danhSachKG, String selectedMaKG) {
        this.selectedMaKG = selectedMaKG;
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;

        // Quầy Lễ Tân
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        JLabel lblLeTan = new JLabel("RECEPTION", SwingConstants.CENTER);
        lblLeTan.setOpaque(true);
        lblLeTan.setBackground(mauLeTan);
        lblLeTan.setForeground(Color.WHITE);
        lblLeTan.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLeTan.setPreferredSize(new Dimension(DON_VI * 2, DON_VI));
        add(lblLeTan, gbc);

        // Các bàn
        if (danhSachKG != null) {
            for (KhongGianDTO kg : danhSachKG) {
                gbc.gridx = kg.getToaDoX();
                gbc.gridy = kg.getToaDoY();
                gbc.gridwidth = kg.getChieuDai() > 0 ? kg.getChieuDai() : 1;
                gbc.gridheight = kg.getChieuRong() > 0 ? kg.getChieuRong() : 1;

                JButton nutKG = taoNutKhongGian(kg);
                add(nutKG, gbc);
            }
        }

        // Đẩy về góc
        gbc.gridx = MAX_COLS;
        gbc.gridy = MAX_ROWS;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(Box.createRigidArea(new Dimension(0, 0)), gbc);

        revalidate();
        repaint();
    }

    private JButton taoNutKhongGian(KhongGianDTO kg) {
        JButton nutKG = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                Color vien = (Color) getClientProperty("mauVien");
                if (vien != null) {
                    g2.setColor(vien);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        String displayText = kg.getTenKG();
        if (displayText != null && displayText.length() > 10) {
            displayText = kg.getMaKG();
        }
        nutKG.setText("<html><center>" + displayText + "</center></html>");
        nutKG.setFont(new Font("Segoe UI", Font.BOLD, 10));
        
        nutKG.setFocusPainted(false);
        nutKG.setContentAreaFilled(false);
        nutKG.setOpaque(false);
        nutKG.setBorderPainted(false);
        nutKG.setPreferredSize(new Dimension(DON_VI * (kg.getChieuDai() > 0 ? kg.getChieuDai() : 1), 
                                           DON_VI * (kg.getChieuRong() > 0 ? kg.getChieuRong() : 1)));

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
        nutKG.putClientProperty("mauVien", mauVienBtn);
        nutKG.setForeground(mauChu);

        nutKG.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onTableClick != null) {
                    onTableClick.accept(kg);
                }
            }
        });

        return nutKG;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(254, 252, 253));
        g2.fillRect(0, 0, MAX_COLS * DON_VI, MAX_ROWS * DON_VI);

        g2.setColor(new Color(235, 235, 235, 100));
        g2.setStroke(new BasicStroke(1.0f));

        int gap = 15;
        for (int i = -MAX_ROWS * DON_VI; i < MAX_COLS * DON_VI; i += gap) {
            g2.drawLine(i, 0, i + MAX_ROWS * DON_VI, MAX_ROWS * DON_VI);
        }

        g2.setColor(new Color(235, 94, 141, 50));
        g2.drawRect(0, 0, MAX_COLS * DON_VI, MAX_ROWS * DON_VI);

        g2.dispose();
    }
}
