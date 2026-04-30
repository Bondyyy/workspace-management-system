package com.wms.view.TrangChu.TrangChuHoiVien.DatChoKhongGian.XemSoDoKhongGian;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class XemSoDoKhongGianForm extends javax.swing.JPanel {

    private final Color mauHongChinh = Color.decode("#EB5E8D");
    private final Color mauTrong_Nen = Color.decode("#FFF0F5");
    private final Color mauTrong_Vien = Color.decode("#E0E0E0");
    private final Color mauDaDat_Nen = Color.decode("#FFF3E0");
    private final Color mauDaDat_Vien = Color.decode("#FFB74D");
    private final Color mauDangDung_Nen = Color.decode("#E8F5E9");
    private final Color mauDangDung_Vien = Color.decode("#66BB6A");
    private final Color mauBaoTri_Nen = Color.decode("#F5F5F5");
    private final Color mauBaoTri_Vien = Color.decode("#9E9E9E");
    private final Color mauViTri = Color.decode("#D32F2F");
    private final Color mauChuDam = Color.decode("#212529");
    private final Color mauChuNhat = Color.decode("#757575");

    private final Color mauLeTan = Color.decode("#34495E");
    private final int DON_VI = 50; // Đồng bộ tỉ lệ 50px/đơn vị với bên Quản lý
    private final int MAX_COLS = 12; // Chiều rộng cố định
    private final int MAX_ROWS = 8;  // Chiều dài cố định

    private JPanel panelSoDo;
    private JButton nutDangChon = null;
    private String maChiNhanhHienTai = "";
    private String tenChiNhanhHienTai = "";

    public XemSoDoKhongGianForm(String maChiNhanh, String tenChiNhanh) {
        this.maChiNhanhHienTai = maChiNhanh;
        this.tenChiNhanhHienTai = tenChiNhanh;

        initComponents();
        initCustomUI();

        loadDataTuDatabase(maChiNhanh);
    }

    private void initCustomUI() {
        lblTitle.setText("Sơ đồ Không gian - " + tenChiNhanhHienTai);
        scrollSoDo.getVerticalScrollBar().setUnitIncrement(20);
        scrollSoDo.getHorizontalScrollBar().setUnitIncrement(20);

        JPanel canvas = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        canvas.setOpaque(false);
        canvas.setBorder(new EmptyBorder(20, 20, 20, 20));

        panelSoDo = new JPanel(new GridBagLayout()) {
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
        };
        panelSoDo.setBackground(Color.WHITE);
        canvas.add(panelSoDo, BorderLayout.CENTER);
        pnBaoNgoai.add(canvas);

        pnChuThich.add(taoMucChuThich("Quầy lễ tân", mauLeTan, Color.BLACK, false));
        pnChuThich.add(taoMucChuThich("Trống", mauTrong_Nen, mauTrong_Vien, false));
        pnChuThich.add(taoMucChuThich("Đang chọn", mauHongChinh, mauHongChinh, false));
        pnChuThich.add(taoMucChuThich("Đã đặt", mauDaDat_Nen, mauDaDat_Vien, false));
        pnChuThich.add(taoMucChuThich("Đang dùng", mauDangDung_Nen, mauDangDung_Vien, false));
        pnChuThich.add(taoMucChuThich("Bảo trì", mauBaoTri_Nen, mauBaoTri_Vien, false));
    }

    private JPanel taoMucChuThich(String vanBan, Color mauNen, Color mauVien, boolean laChamDo) {
        JPanel muc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        muc.setOpaque(false);
        if (laChamDo) {
            JLabel hopMau = new JLabel(new IconHinhTron(16, mauViTri));
            hopMau.setPreferredSize(new Dimension(22, 22));
            hopMau.setHorizontalAlignment(SwingConstants.CENTER);
            muc.add(hopMau);
        } else {
            JPanel hopMau = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(mauNen);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(mauVien);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                    g2.dispose();
                }
            };
            hopMau.setOpaque(false);
            hopMau.setPreferredSize(new Dimension(22, 22));
            muc.add(hopMau);
        }
        JLabel nhan = new JLabel(vanBan);
        nhan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nhan.setForeground(mauChuDam);
        muc.add(nhan);
        return muc;
    }

    private void loadDataTuDatabase(String maChiNhanh) {
        try {
            com.wms.dao.KhongGianDAO kgDao = new com.wms.dao.KhongGianDAO();
            List<com.wms.model.CoSoVatChat.KhongGianDTO> dsTuDB = kgDao.layTheoChiNhanh(maChiNhanh);
            veSoDo(dsTuDB);
        } catch (Exception e) {
            System.err.println("[XemSoDoKhongGianForm] Lỗi tải sơ đồ từ DB: " + e.getMessage());
        }
    }

    private String mapTrangThai(String raw) {
        if (raw == null) return "Trống";
        return raw.trim();
    }

    public void veSoDo(List<com.wms.model.CoSoVatChat.KhongGianDTO> danhSachKG) {
        panelSoDo.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;

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
        panelSoDo.add(lblLeTan, gbc);

        for (com.wms.model.CoSoVatChat.KhongGianDTO kg : danhSachKG) {
            gbc.gridx = kg.getToaDoX();
            gbc.gridy = kg.getToaDoY();
            gbc.gridwidth = kg.getChieuDai();
            gbc.gridheight = kg.getChieuRong();

            JButton nutKG = taoNutKhongGian(kg);
            panelSoDo.add(nutKG, gbc);
        }

        gbc.gridx = MAX_COLS;
        gbc.gridy = MAX_ROWS;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panelSoDo.add(Box.createRigidArea(new Dimension(0, 0)), gbc);
        
        panelSoDo.setPreferredSize(new Dimension(MAX_COLS * DON_VI, MAX_ROWS * DON_VI));

        panelSoDo.revalidate();
        panelSoDo.repaint();
    }

    private JButton taoNutKhongGian(com.wms.model.CoSoVatChat.KhongGianDTO kg) {
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
        nutKG.setFocusPainted(false);
        nutKG.setContentAreaFilled(false);
        nutKG.setOpaque(false);
        nutKG.setBorderPainted(false);
        
        nutKG.setPreferredSize(new Dimension(DON_VI * kg.getChieuDai(), DON_VI * kg.getChieuRong()));

        Color mauNenBtn = mauTrong_Nen, mauVienBtn = mauTrong_Vien, mauChu = mauChuDam;

        switch (kg.getTrangThaiKG()) {
            case "Trống":
                nutKG.setCursor(new Cursor(Cursor.HAND_CURSOR));
                nutKG.addActionListener(e -> xuLyChon(nutKG, kg));
                break;
            case "Đã đặt trước":
                mauNenBtn = mauDaDat_Nen;
                mauVienBtn = mauDaDat_Vien;
                mauChu = new Color(230, 81, 0);
                nutKG.setEnabled(false);
                break;
            case "Đang hoạt động":
                mauNenBtn = mauDangDung_Nen;
                mauVienBtn = mauDangDung_Vien;
                mauChu = new Color(46, 125, 50);
                nutKG.setEnabled(false);
                break;
            case "Bảo trì":
                mauNenBtn = mauBaoTri_Nen;
                mauVienBtn = mauBaoTri_Vien;
                mauChu = mauChuNhat;
                nutKG.setEnabled(false);
                break;
        }

        nutKG.setBackground(mauNenBtn);
        nutKG.putClientProperty("mauVien", mauVienBtn);
        nutKG.putClientProperty("mauNenGoc", mauNenBtn);
        nutKG.putClientProperty("mauChuGoc", mauChu);

        String htmlText = String.format(
                "<html><div style='text-align:center; padding:3px;'>" +
                        "<div style='font-family:Segoe UI; font-size:10px; font-weight:bold; margin-bottom:2px; color:rgb(%d,%d,%d);'>%s</div>"
                        +
                        "</div></html>",
                mauChu.getRed(), mauChu.getGreen(), mauChu.getBlue(), kg.getMaKG());
        nutKG.setText(htmlText);
        nutKG.setToolTipText(kg.getTenKG());
        nutKG.putClientProperty("htmlGoc", htmlText);
        nutKG.putClientProperty("maKG", kg.getMaKG());
        nutKG.putClientProperty("tenKG", kg.getTenKG());

        return nutKG;
    }

    private void xuLyChon(JButton nutBam, com.wms.model.CoSoVatChat.KhongGianDTO kg) {
        if (nutDangChon == nutBam) {
            nutBam.setBackground((Color) nutBam.getClientProperty("mauNenGoc"));
            nutBam.putClientProperty("mauVien", mauTrong_Vien);
            nutBam.setText((String) nutBam.getClientProperty("htmlGoc"));
            nutBam.repaint();
            nutDangChon = null;
            btnXacNhan.setEnabled(false);
        } else {
            if (nutDangChon != null) {
                nutDangChon.setBackground((Color) nutDangChon.getClientProperty("mauNenGoc"));
                nutDangChon.putClientProperty("mauVien", mauTrong_Vien);
                nutDangChon.setText((String) nutDangChon.getClientProperty("htmlGoc"));
                nutDangChon.repaint();
            }
            nutBam.setBackground(mauHongChinh);
            nutBam.putClientProperty("mauVien", mauHongChinh);
            String htmlSelected = String.format(
                    "<html><div style='text-align:center; padding:3px;'>" +
                            "<div style='font-family:Segoe UI; font-size:11px; font-weight:bold; color:white; margin-bottom:2px;'>%s</div>"
                            +
                            "<div style='font-family:Segoe UI; font-size:9px; color:white;'>%s</div>" +
                            "</div></html>",
                    nutBam.getClientProperty("maKG"), nutBam.getClientProperty("tenKG"));
            nutBam.setText(htmlSelected);
            nutBam.repaint();

            nutDangChon = nutBam;
            btnXacNhan.setEnabled(true);
        }
    }

    private void btnXacNhanActionPerformed(java.awt.event.ActionEvent evt) {
        if (nutDangChon != null) {
            String maKGDangChon = (String) nutDangChon.getClientProperty("maKG");
            System.out.println("Tiến hành đặt không gian: " + maKGDangChon + " thuộc chi nhánh: " + maChiNhanhHienTai);

            // Xử lý chuyển form
        }
    }

    private static class IconHinhTron implements Icon {
        private final int kichThuoc;
        private final Color mauSac;

        public IconHinhTron(int kichThuoc, Color mauSac) {
            this.kichThuoc = kichThuoc;
            this.mauSac = mauSac;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(mauSac);
            g2.fillOval(x, y, kichThuoc, kichThuoc);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return kichThuoc;
        }

        @Override
        public int getIconHeight() {
            return kichThuoc;
        }
    }

    public static class ModelKhongGian {
        private String maKG, tenKG, trangThai;
        private int toaDoX, toaDoY, doRong, doCao;

        public ModelKhongGian(String ma, String ten, String tt, int x, int y, int r, int c) {
            this.maKG = ma;
            this.tenKG = ten;
            this.trangThai = tt;
            this.toaDoX = x;
            this.toaDoY = y;
            this.doRong = r;
            this.doCao = c;
        }

        public String getMaKG() {
            return maKG;
        }

        public String getTenKG() {
            return tenKG;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public int getToaDoX() {
            return toaDoX;
        }

        public int getToaDoY() {
            return toaDoY;
        }

        public int getDoRong() {
            return doRong;
        }

        public int getDoCao() {
            return doCao;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblSubTitle = new javax.swing.JLabel();
        scrollSoDo = new javax.swing.JScrollPane();
        pnBaoNgoai = new javax.swing.JPanel();
        pnChuThich = new javax.swing.JPanel();
        btnXacNhan = new javax.swing.JButton();

        setBackground(new java.awt.Color(254, 248, 250));
        setPreferredSize(new java.awt.Dimension(1050, 640));
        setLayout(null);

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblTitle.setText("Sơ đồ Không gian");
        add(lblTitle);
        lblTitle.setBounds(40, 20, 500, 32);

        lblSubTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSubTitle.setForeground(new java.awt.Color(136, 136, 136));
        lblSubTitle.setText("Vui lòng chọn một chỗ ngồi hoặc phòng họp phù hợp với nhu cầu của bạn");
        add(lblSubTitle);
        lblSubTitle.setBounds(40, 55, 500, 20);

        scrollSoDo.setBorder(null);

        pnBaoNgoai.setBackground(new java.awt.Color(254, 248, 250));
        scrollSoDo.setViewportView(pnBaoNgoai);

        add(scrollSoDo);
        scrollSoDo.setBounds(40, 100, 970, 430);

        pnChuThich.setBackground(new java.awt.Color(254, 248, 250));
        pnChuThich.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        add(pnChuThich);
        pnChuThich.setBounds(40, 550, 700, 50);

        btnXacNhan.setBackground(new java.awt.Color(235, 94, 141));
        btnXacNhan.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnXacNhan.setForeground(new java.awt.Color(255, 255, 255));
        btnXacNhan.setText("Tiến hành Đặt chỗ  ➔");
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(this::btnXacNhanActionPerformed);
        add(btnXacNhan);
        btnXacNhan.setBounds(760, 550, 250, 50);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnXacNhan;
    private javax.swing.JLabel lblSubTitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnBaoNgoai;
    private javax.swing.JPanel pnChuThich;
    private javax.swing.JScrollPane scrollSoDo;
    // End of variables declaration//GEN-END:variables
}
