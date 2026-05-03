package com.wms.view.TrangChuHoiVien.DatChoKhongGian.XemSoDoKhongGian;

import com.wms.dao.LoaiKhongGianDAO;

import com.wms.dao.KhongGianDAO;
import com.wms.model.KhongGianDTO;


import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.wms.model.LoaiKhongGianDTO;
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
    private KhongGianDTO khongGianDangChon = null;
    private String maChiNhanhHienTai = "";
    private String tenChiNhanhHienTai = "";
    private final DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private final LoaiKhongGianDAO lkgDao = new LoaiKhongGianDAO();

    public XemSoDoKhongGianForm(String maChiNhanh, String tenChiNhanh) {
        this.maChiNhanhHienTai = maChiNhanh;
        this.tenChiNhanhHienTai = tenChiNhanh;

        initComponents();
        initCustomUI();

        loadDataTuDatabase(maChiNhanh);
    }

    private void initCustomUI() {
        lblHeaderTitle.setText("Sơ đồ Không gian - " + tenChiNhanhHienTai);
        txtChiNhanh.setText(tenChiNhanhHienTai);
        txtNgayDat.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        setupTimeComboBoxes();
        
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

    private void setupTimeComboBoxes() {
        cbxGioBatDau.removeAllItems();
        cbxGioKetThuc.removeAllItems();
        for (int i = 7; i <= 21; i++) {
            String time = String.format("%02d:00", i);
            cbxGioBatDau.addItem(time);
        }
        for (int i = 8; i <= 22; i++) {
            String time = String.format("%02d:00", i);
            cbxGioKetThuc.addItem(time);
        }
        cbxGioBatDau.setSelectedItem("08:00");
        cbxGioKetThuc.setSelectedItem("10:00");
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
            List<com.wms.model.KhongGianDTO> dsTuDB = kgDao.layTheoChiNhanh(maChiNhanh);
            veSoDo(dsTuDB);
        } catch (Exception e) {
            System.err.println("[XemSoDoKhongGianForm] Lỗi tải sơ đồ từ DB: " + e.getMessage());
        }
    }

    private String mapTrangThai(String raw) {
        if (raw == null) return "Trống";
        return raw.trim();
    }

    public void veSoDo(List<com.wms.model.KhongGianDTO> danhSachKG) {
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

        for (com.wms.model.KhongGianDTO kg : danhSachKG) {
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

    private JButton taoNutKhongGian(com.wms.model.KhongGianDTO kg) {
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
        nutKG.putClientProperty("dto", kg);

        return nutKG;
    }

    private void xuLyChon(JButton nutBam, KhongGianDTO kg) {
        if (nutDangChon == nutBam) {
            huyChon();
        } else {
            if (nutDangChon != null) {
                nutDangChon.setBackground((Color) nutDangChon.getClientProperty("mauNenGoc"));
                nutDangChon.putClientProperty("mauVien", (Color) nutDangChon.getClientProperty("mauNenGoc") == mauTrong_Nen ? mauTrong_Vien : (Color) nutDangChon.getClientProperty("mauVien"));
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
            khongGianDangChon = kg;
            txtKhongGian.setText(kg.getTenKG());
            btnDatCho.setEnabled(true);
            tinhTienTuDong(null);
        }
    }

    private void huyChon() {
        if (nutDangChon != null) {
            nutDangChon.setBackground((Color) nutDangChon.getClientProperty("mauNenGoc"));
            nutDangChon.putClientProperty("mauVien", nutDangChon.getClientProperty("mauNenGoc") == mauTrong_Nen ? mauTrong_Vien : (Color) nutDangChon.getClientProperty("mauVien"));
            nutDangChon.setText((String) nutDangChon.getClientProperty("htmlGoc"));
            nutDangChon.repaint();
        }
        nutDangChon = null;
        khongGianDangChon = null;
        txtKhongGian.setText("Chưa chọn từ bản đồ");
        btnDatCho.setEnabled(false);
        txtTongTien.setText("0 VNĐ");
    }

    private void tinhTienTuDong(java.awt.event.ActionEvent evt) {
        if (khongGianDangChon == null) return;

        try {
            String gioBD = (String) cbxGioBatDau.getSelectedItem();
            String gioKT = (String) cbxGioKetThuc.getSelectedItem();

            int hBD = Integer.parseInt(gioBD.split(":")[0]);
            int hKT = Integer.parseInt(gioKT.split(":")[0]);

            if (hKT <= hBD) {
                txtTongTien.setText("Giờ kết thúc > bắt đầu");
                txtTongTien.setForeground(Color.RED);
                btnDatCho.setEnabled(false);
                return;
            }

            txtTongTien.setForeground(new Color(0, 153, 51));
            btnDatCho.setEnabled(true);

            LoaiKhongGianDTO lkg = lkgDao.layTheoMa(khongGianDangChon.getMaLoaiKG());
            double donGia = (lkg != null && lkg.getDonGiaTheoGio() != null) ? lkg.getDonGiaTheoGio() : 0;
            double tongTien = (hKT - hBD) * donGia;

            txtTongTien.setText(df.format(tongTien));
        } catch (Exception e) {
            txtTongTien.setText("Lỗi tính tiền");
        }
    }

    private void btnDatChoActionPerformed(java.awt.event.ActionEvent evt) {
        if (khongGianDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn không gian trên sơ đồ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ngayDat = txtNgayDat.getText().trim();
        if (ngayDat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ngày sử dụng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String gioBD = (String) cbxGioBatDau.getSelectedItem();
        String gioKT = (String) cbxGioKetThuc.getSelectedItem();
        
        try {
            String tongTienStr = txtTongTien.getText().replaceAll("[^0-9]", "");
            double tongTien = Double.parseDouble(tongTienStr);

            Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof com.wms.view.TrangChuHoiVien.TrangChuHoiVienForm) {
                ((com.wms.view.TrangChuHoiVien.TrangChuHoiVienForm) win).showPanel(new com.wms.view.TrangChuHoiVien.DatChoKhongGian.XacNhan.ChuyenKhoanDatTruoc(
                        maChiNhanhHienTai, tenChiNhanhHienTai, 
                        khongGianDangChon.getMaKG(), khongGianDangChon.getTenKG(),
                        ngayDat, gioBD, gioKT, tongTien
                    )
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý dữ liệu đặt chỗ!");
        }
    }

    private void btnQuayLaiActionPerformed(java.awt.event.ActionEvent evt) {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof com.wms.view.TrangChuHoiVien.TrangChuHoiVienForm) {
            ((com.wms.view.TrangChuHoiVien.TrangChuHoiVienForm) win).showPanel(new com.wms.view.TrangChuHoiVien.DatChoKhongGian.XemChiNhanh.XemChiNhanhForm()
            );
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

        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        pnLeftMap = new javax.swing.JPanel();
        lblMapTitle = new javax.swing.JLabel();
        scrollSoDo = new javax.swing.JScrollPane();
        pnBaoNgoai = new javax.swing.JPanel();
        pnChuThich = new javax.swing.JPanel();
        pnRightForm = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblChiNhanh = new javax.swing.JLabel();
        txtChiNhanh = new javax.swing.JTextField();
        lblKhongGian = new javax.swing.JLabel();
        txtKhongGian = new javax.swing.JTextField();
        lblNgayDat = new javax.swing.JLabel();
        txtNgayDat = new javax.swing.JTextField();
        lblGioBatDau = new javax.swing.JLabel();
        cbxGioBatDau = new javax.swing.JComboBox<>();
        lblGioKetThuc = new javax.swing.JLabel();
        cbxGioKetThuc = new javax.swing.JComboBox<>();
        lblTongTien = new javax.swing.JLabel();
        txtTongTien = new javax.swing.JTextField();
        btnDatCho = new javax.swing.JButton();
        btnQuayLai = new javax.swing.JButton();

        setBackground(new java.awt.Color(254, 248, 250));
        setPreferredSize(new java.awt.Dimension(1050, 640));
        setLayout(null);

        pnHeader.setBackground(new java.awt.Color(235, 94, 141));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("CHỌN CHỖ & THỜI GIAN SỬ DỤNG");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 60);

        add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 60);

        pnLeftMap.setBackground(new java.awt.Color(255, 255, 255));
        pnLeftMap.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeftMap.setLayout(null);

        lblMapTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblMapTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblMapTitle.setText("BẢN ĐỒ KHÔNG GIAN (CLICK ĐỂ CHỌN)");
        pnLeftMap.add(lblMapTitle);
        lblMapTitle.setBounds(20, 15, 400, 30);

        scrollSoDo.setBorder(null);

        pnBaoNgoai.setBackground(new java.awt.Color(255, 255, 255));
        scrollSoDo.setViewportView(pnBaoNgoai);

        pnLeftMap.add(scrollSoDo);
        scrollSoDo.setBounds(20, 55, 610, 410);

        pnChuThich.setBackground(new java.awt.Color(255, 255, 255));
        pnChuThich.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        pnLeftMap.add(pnChuThich);
        pnChuThich.setBounds(20, 475, 610, 50);

        add(pnLeftMap);
        pnLeftMap.setBounds(20, 80, 650, 530);

        pnRightForm.setBackground(new java.awt.Color(255, 255, 255));
        pnRightForm.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRightForm.setLayout(null);

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblFormTitle.setText("THÔNG TIN ĐẶT CHỖ");
        pnRightForm.add(lblFormTitle);
        lblFormTitle.setBounds(20, 15, 300, 30);

        lblChiNhanh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChiNhanh.setForeground(new java.awt.Color(35, 30, 48));
        lblChiNhanh.setText("Tại Chi nhánh (Cố định)");
        pnRightForm.add(lblChiNhanh);
        lblChiNhanh.setBounds(20, 60, 300, 18);

        txtChiNhanh.setEditable(false);
        txtChiNhanh.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtChiNhanh.setBackground(new java.awt.Color(240, 240, 240));
        pnRightForm.add(txtChiNhanh);
        txtChiNhanh.setBounds(20, 80, 300, 35);

        lblKhongGian.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKhongGian.setForeground(new java.awt.Color(35, 30, 48));
        lblKhongGian.setText("Không gian đã chọn");
        pnRightForm.add(lblKhongGian);
        lblKhongGian.setBounds(20, 130, 300, 18);

        txtKhongGian.setEditable(false);
        txtKhongGian.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtKhongGian.setForeground(new java.awt.Color(235, 94, 141));
        txtKhongGian.setText("Chưa chọn từ bản đỒ");
        pnRightForm.add(txtKhongGian);
        txtKhongGian.setBounds(20, 150, 300, 35);

        lblNgayDat.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNgayDat.setForeground(new java.awt.Color(35, 30, 48));
        lblNgayDat.setText("Ngày sử dụng (dd/MM/yyyy) (*)");
        pnRightForm.add(lblNgayDat);
        lblNgayDat.setBounds(20, 200, 300, 18);

        txtNgayDat.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pnRightForm.add(txtNgayDat);
        txtNgayDat.setBounds(20, 220, 300, 35);

        lblGioBatDau.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGioBatDau.setForeground(new java.awt.Color(35, 30, 48));
        lblGioBatDau.setText("Từ giờ (*)");
        pnRightForm.add(lblGioBatDau);
        lblGioBatDau.setBounds(20, 270, 140, 18);

        cbxGioBatDau.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbxGioBatDau.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "07:00", "08:00", "09:00", "10:00", "11:00" }));
        cbxGioBatDau.addActionListener(this::tinhTienTuDong);
        pnRightForm.add(cbxGioBatDau);
        cbxGioBatDau.setBounds(20, 290, 140, 35);

        lblGioKetThuc.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblGioKetThuc.setForeground(new java.awt.Color(35, 30, 48));
        lblGioKetThuc.setText("Đến giờ (*)");
        pnRightForm.add(lblGioKetThuc);
        lblGioKetThuc.setBounds(180, 270, 140, 18);

        cbxGioKetThuc.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbxGioKetThuc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "08:00", "09:00", "10:00", "11:00", "12:00" }));
        cbxGioKetThuc.addActionListener(this::tinhTienTuDong);
        pnRightForm.add(cbxGioKetThuc);
        cbxGioKetThuc.setBounds(180, 290, 140, 35);

        lblTongTien.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTongTien.setForeground(new java.awt.Color(35, 30, 48));
        lblTongTien.setText("Tổng tiền dự kiến");
        pnRightForm.add(lblTongTien);
        lblTongTien.setBounds(20, 350, 300, 18);

        txtTongTien.setEditable(false);
        txtTongTien.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtTongTien.setForeground(new java.awt.Color(0, 153, 51));
        txtTongTien.setText("0 VNĐ");
        txtTongTien.setBorder(null);
        pnRightForm.add(txtTongTien);
        txtTongTien.setBounds(20, 370, 300, 40);

        btnDatCho.setBackground(new java.awt.Color(235, 94, 141));
        btnDatCho.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnDatCho.setForeground(new java.awt.Color(255, 255, 255));
        btnDatCho.setText("Đặt chỗ và Thanh toán");
        btnDatCho.setEnabled(false);
        btnDatCho.addActionListener(this::btnDatChoActionPerformed);
        pnRightForm.add(btnDatCho);
        btnDatCho.setBounds(20, 425, 300, 45);

        btnQuayLai.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnQuayLai.setForeground(new java.awt.Color(235, 94, 141));
        btnQuayLai.setText("Quay lại Chọn Chi nhánh");
        btnQuayLai.addActionListener(this::btnQuayLaiActionPerformed);
        pnRightForm.add(btnQuayLai);
        btnQuayLai.setBounds(20, 480, 300, 35);

        add(pnRightForm);
        pnRightForm.setBounds(690, 80, 340, 530);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDatCho;
    private javax.swing.JButton btnQuayLai;
    private javax.swing.JComboBox<String> cbxGioBatDau;
    private javax.swing.JComboBox<String> cbxGioKetThuc;
    private javax.swing.JLabel lblChiNhanh;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblGioBatDau;
    private javax.swing.JLabel lblGioKetThuc;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblKhongGian;
    private javax.swing.JLabel lblMapTitle;
    private javax.swing.JLabel lblNgayDat;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JPanel pnBaoNgoai;
    private javax.swing.JPanel pnChuThich;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeftMap;
    private javax.swing.JPanel pnRightForm;
    private javax.swing.JScrollPane scrollSoDo;
    private javax.swing.JTextField txtChiNhanh;
    private javax.swing.JTextField txtKhongGian;
    private javax.swing.JTextField txtNgayDat;
    private javax.swing.JTextField txtTongTien;
    // End of variables declaration//GEN-END:variables
}






