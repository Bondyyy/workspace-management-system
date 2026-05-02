package com.wms.view.XemSoDoKhongGian;

import com.wms.view.TrangChu.TrangChuHoiVien.DatChoKhongGian.DatCho.ChonKhongGian;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class XemSoDoKhongGianForm extends JPanel {

    // === BẢNG MÀU CHÍNH - Tone hồng sang trọng ===
    private final Color mauNenChinh = Color.decode("#FAFAFA");        // Xám trắng
    private final Color mauKhungSoDo = Color.WHITE;                   // Trắng
    private final Color mauHongChinh = Color.decode("#D81B60");       // Hồng đậm chủ đạo
    private final Color mauHongNhat = Color.decode("#FCE4EC");        // Hồng rất nhạt
    private final Color mauHongPhu = Color.decode("#F06292");         // Hồng vừa
    
    // Màu trạng thái
    private final Color mauTrong_Nen = Color.decode("#FFF0F5");       // Hồng nhạt thay vì trắng
    private final Color mauTrong_Vien = Color.decode("#E0E0E0");
    private final Color mauDaDat_Nen = Color.decode("#FFF3E0");       // Cam nhạt
    private final Color mauDaDat_Vien = Color.decode("#FFB74D");      // Cam
    private final Color mauDangDung_Nen = Color.decode("#E8F5E9");    // Xanh lá nhạt
    private final Color mauDangDung_Vien = Color.decode("#66BB6A");   // Xanh lá
    private final Color mauBaoTri_Nen = Color.decode("#F5F5F5");      // Xám nhạt
    private final Color mauBaoTri_Vien = Color.decode("#9E9E9E");     // Xám
    
    // Màu chữ
    private final Color mauChuDam = Color.decode("#212529");
    private final Color mauChuNhat = Color.decode("#757575");
    private final Color mauChuTrang = Color.WHITE;
    private final Color mauViTri = Color.decode("#D32F2F");           // Đỏ đậm

    private JPanel panelSoDo;
    private JLabel nhanTieuDe;
    private JButton nutXacNhan;
    
    private JButton nutDangChon = null;

    public XemSoDoKhongGianForm() {
        khoiTaoThanhPhan();
    }

    public XemSoDoKhongGianForm (String loaiKhongGian) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public XemSoDoKhongGianForm(Frame parentFrame, boolean b, ChonKhongGian aThis) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void khoiTaoThanhPhan() {
        this.setLayout(new BorderLayout(0, 25));
        this.setBackground(mauNenChinh);
        this.setBorder(new EmptyBorder(30, 40, 30, 40));

        // === 1. TIÊU ĐỀ ===
        JPanel panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setBackground(mauNenChinh);
        
        nhanTieuDe = new JLabel("SƠ ĐỒ KHÔNG GIAN - CHI NHÁNH QUẬN 1", SwingConstants.CENTER);
        nhanTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 30));
        nhanTieuDe.setForeground(mauHongChinh);
        panelTieuDe.add(nhanTieuDe, BorderLayout.CENTER);
        
        JLabel lblMoTa = new JLabel("Chọn không gian phù hợp cho nhu cầu của bạn", SwingConstants.CENTER);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblMoTa.setForeground(mauChuNhat);
        lblMoTa.setBorder(new EmptyBorder(8, 0, 0, 0));
        panelTieuDe.add(lblMoTa, BorderLayout.SOUTH);
        
        this.add(panelTieuDe, BorderLayout.NORTH);

        // === 2. SƠ ĐỒ ===
        panelSoDo = new JPanel(new GridBagLayout());
        panelSoDo.setBackground(mauKhungSoDo);
        panelSoDo.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JPanel panelBaoNgoai = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBaoNgoai.setBackground(mauNenChinh);
        
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
        canvas.add(panelSoDo, BorderLayout.CENTER);
        panelBaoNgoai.add(canvas);

        JScrollPane thanhCuon = new JScrollPane(panelBaoNgoai);
        thanhCuon.setBorder(null);
        thanhCuon.getViewport().setBackground(mauNenChinh);
        thanhCuon.getVerticalScrollBar().setUnitIncrement(20);
        this.add(thanhCuon, BorderLayout.CENTER);

        // === 3. THANH ĐIỀU KHIỂN DƯỚI ===
        JPanel panelDuoi = new JPanel(new BorderLayout());
        panelDuoi.setBackground(mauNenChinh);
        panelDuoi.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Chú thích
        JPanel panelChuThich = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 5));
        panelChuThich.setBackground(mauNenChinh);
        panelChuThich.add(taoMucChuThich("Trống", mauTrong_Nen, mauTrong_Vien, false));
        panelChuThich.add(taoMucChuThich("Đang chọn", mauHongChinh, mauHongChinh, false));
        panelChuThich.add(taoMucChuThich("Đã đặt", mauDaDat_Nen, mauDaDat_Vien, false));
        panelChuThich.add(taoMucChuThich("Đang dùng", mauDangDung_Nen, mauDangDung_Vien, false));
        panelChuThich.add(taoMucChuThich("Bảo trì", mauBaoTri_Nen, mauBaoTri_Vien, false));
        panelChuThich.add(taoMucChuThich("Vị trí của bạn", mauViTri, mauViTri, true));
        panelDuoi.add(panelChuThich, BorderLayout.WEST);

        // Nút Xác nhận
        nutXacNhan = taoNutXacNhan();
        nutXacNhan.setEnabled(false);
        panelDuoi.add(nutXacNhan, BorderLayout.EAST);

        this.add(panelDuoi, BorderLayout.SOUTH);
    }

    private JButton taoNutXacNhan() {
        JButton nut = new JButton("XÁC NHẬN ĐẶT CHỖ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isEnabled()) {
                    g2.setColor(mauHongChinh);
                } else {
                    g2.setColor(new Color(189, 189, 189));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        nut.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nut.setForeground(Color.WHITE);
        nut.setPreferredSize(new Dimension(220, 50));
        nut.setContentAreaFilled(false);
        nut.setBorderPainted(false);
        nut.setFocusPainted(false);
        nut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        nut.addActionListener(e -> {
            if (nutDangChon != null) {
                // TODO: Xử lý đặt chỗ
                String maKG = (String) nutDangChon.getClientProperty("maKG");
                System.out.println("Đã xác nhận đặt: " + maKG);
            }
        });
        
        return nut;
    }

    private JPanel taoMucChuThich(String vanBan, Color mauNen, Color mauVien, boolean laChamDo) {
        JPanel muc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        muc.setBackground(mauNenChinh);
        
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

    public void veSoDo(List<ModelKhongGian> danhSachKG) {
        panelSoDo.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;

        // Kích thước cơ bản - đơn vị pixel (tăng lên để rộng hơn)
        int donViRong = 75;
        int donViCao = 75;

        for (ModelKhongGian kg : danhSachKG) {
            gbc.gridx = kg.getToaDoX();
            gbc.gridy = kg.getToaDoY();
            gbc.gridwidth = kg.getDoRong();
            gbc.gridheight = kg.getDoCao();
            gbc.weightx = 0;
            gbc.weighty = 0;

            // === XỬ LÝ LỐI ĐI ===
            if ("Lối đi".equals(kg.getTrangThai())) {
                JPanel loiDi = new JPanel();
                loiDi.setOpaque(false);
                loiDi.setPreferredSize(new Dimension(
                    donViRong * kg.getDoRong(),
                    donViCao * kg.getDoCao()
                ));
                panelSoDo.add(loiDi, gbc);
                continue;
            }
            
            // === XỬ LÝ VỊ TRÍ HIỆN TẠI ===
            if ("Vị trí".equals(kg.getTrangThai())) {
                JLabel lblViTri = new JLabel(new IconHinhTron(20, mauViTri));
                lblViTri.setHorizontalAlignment(SwingConstants.CENTER);
                lblViTri.setToolTipText("Bạn đang ở đây");
                gbc.fill = GridBagConstraints.NONE;
                panelSoDo.add(lblViTri, gbc);
                gbc.fill = GridBagConstraints.BOTH;
                continue;
            }

            // === XỬ LÝ TRANG TRÍ (Quầy, vật dụng) ===
            if ("Trang trí".equals(kg.getTrangThai())) {
                JLabel lblTrangTri = new JLabel();
                lblTrangTri.setHorizontalAlignment(SwingConstants.CENTER);
                lblTrangTri.setVerticalAlignment(SwingConstants.CENTER);
                lblTrangTri.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblTrangTri.setForeground(mauChuNhat);
                lblTrangTri.setText(String.format(
                    "<html><center>%s<br><span style='font-size:11px'>%s</span></center></html>",
                    "🏢", kg.getTenKG()
                ));
                lblTrangTri.setOpaque(true);
                lblTrangTri.setBackground(new Color(245, 245, 245));
                lblTrangTri.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
                lblTrangTri.setPreferredSize(new Dimension(
                    donViRong * kg.getDoRong(),
                    donViCao * kg.getDoCao()
                ));
                panelSoDo.add(lblTrangTri, gbc);
                continue;
            }

            // === XỬ LÝ CÁC KHÔNG GIAN CÓ THỂ ĐẶT ===
            JButton nutKG = taoNutKhongGian(kg, donViRong, donViCao);
            panelSoDo.add(nutKG, gbc);
        }

        panelSoDo.revalidate();
        panelSoDo.repaint();
    }

    private JButton taoNutKhongGian(ModelKhongGian kg, int donViRong, int donViCao) {
        JButton nutKG = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ nền với border radius
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Vẽ viền
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
        nutKG.setPreferredSize(new Dimension(
            donViRong * kg.getDoRong(),
            donViCao * kg.getDoCao()
        ));

        // Xác định màu sắc theo trạng thái
        Color mauNenBtn = mauTrong_Nen;
        Color mauVienBtn = mauTrong_Vien;
        Color mauChu = mauChuDam;

        switch (kg.getTrangThai()) {
            case "Trống":
                mauNenBtn = mauTrong_Nen;
                mauVienBtn = mauTrong_Vien;
                mauChu = mauChuDam;
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

        // Format chữ bên trong button
        String htmlText = String.format(
            "<html><div style='text-align:center; padding:3px;'>" +
            "<div style='font-family:Segoe UI; font-size:11px; font-weight:bold; margin-bottom:2px;'>%s</div>" +
            "<div style='font-family:Segoe UI; font-size:9px; color:#757575;'>%s</div>" +
            "</div></html>",
            kg.getMaKG(), kg.getTenKG()
        );
        nutKG.setText(htmlText);
        
        nutKG.putClientProperty("htmlGoc", htmlText);
        nutKG.putClientProperty("maKG", kg.getMaKG());
        nutKG.putClientProperty("tenKG", kg.getTenKG());

        return nutKG;
    }

    private void xuLyChon(JButton nutBam, ModelKhongGian kg) {
        if (nutDangChon == nutBam) {
            // Bỏ chọn
            nutBam.setBackground((Color) nutBam.getClientProperty("mauNenGoc"));
            nutBam.putClientProperty("mauVien", mauTrong_Vien);
            nutBam.setText((String) nutBam.getClientProperty("htmlGoc"));
            nutBam.repaint();
            nutDangChon = null;
            nutXacNhan.setEnabled(false);
        } else {
            // Bỏ chọn nút cũ
            if (nutDangChon != null) {
                nutDangChon.setBackground((Color) nutDangChon.getClientProperty("mauNenGoc"));
                nutDangChon.putClientProperty("mauVien", mauTrong_Vien);
                nutDangChon.setText((String) nutDangChon.getClientProperty("htmlGoc"));
                nutDangChon.repaint();
            }
            
            // Chọn nút mới
            nutBam.setBackground(mauHongChinh);
            nutBam.putClientProperty("mauVien", mauHongChinh);
            
            String htmlSelected = String.format(
                "<html><div style='text-align:center; padding:3px;'>" +
                "<div style='font-family:Segoe UI; font-size:11px; font-weight:bold; color:white; margin-bottom:2px;'>%s</div>" +
                "<div style='font-family:Segoe UI; font-size:9px; color:white;'>%s</div>" +
                "</div></html>",
                nutBam.getClientProperty("maKG"), nutBam.getClientProperty("tenKG")
            );
            nutBam.setText(htmlSelected);
            nutBam.repaint();
            
            nutDangChon = nutBam;
            nutXacNhan.setEnabled(true);
        }
    }

    // === ICON HÌNH TRÒN ===
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
        public int getIconWidth() { return kichThuoc; }
        
        @Override
        public int getIconHeight() { return kichThuoc; }
    }

    // === MODEL DTO ===
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

        public String getMaKG() { return maKG; }
        public String getTenKG() { return tenKG; }
        public String getTrangThai() { return trangThai; }
        public int getToaDoX() { return toaDoX; }
        public int getToaDoY() { return toaDoY; }
        public int getDoRong() { return doRong; }
        public int getDoCao() { return doCao; }
    }

    // === MAIN TEST ===
    public static void main(String[] args) {
        JFrame f = new JFrame("Sơ Đồ Không Gian - UIT Coworking Space");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1600, 900);
        f.setLocationRelativeTo(null);

        XemSoDoKhongGianForm view = new XemSoDoKhongGianForm();
        List<ModelKhongGian> ds = new ArrayList<>();
        
        // === LỐI ĐI CHÍNH ===
        ds.add(new ModelKhongGian("L1", "Lối đi", "Lối đi", 2, 0, 1, 12));
        ds.add(new ModelKhongGian("L2", "Lối đi", "Lối đi", 5, 0, 1, 12));
        ds.add(new ModelKhongGian("L3", "Lối đi", "Lối đi", 10, 0, 1, 12));
        ds.add(new ModelKhongGian("L4", "Lối đi", "Lối đi", 0, 4, 16, 1));
        
        // === VỊ TRÍ HIỆN TẠI ===
        ds.add(new ModelKhongGian("ViTri", "Vị trí", "Vị trí", 10, 7, 1, 1));

        // === DÃY BÀN BÊN TRÁI (Bàn đơn & đôi) ===
        ds.add(new ModelKhongGian("B1-01", "Bàn đơn", "Trống", 0, 0, 1, 1));
        ds.add(new ModelKhongGian("B1-02", "Bàn đơn", "Trống", 1, 0, 1, 1));
        ds.add(new ModelKhongGian("B2-01", "Bàn đôi", "Đang hoạt động", 0, 1, 2, 1));
        
        ds.add(new ModelKhongGian("B1-03", "Bàn đơn", "Đã đặt trước", 0, 2, 1, 1));
        ds.add(new ModelKhongGian("B1-04", "Bàn đơn", "Trống", 1, 2, 1, 1));
        ds.add(new ModelKhongGian("B2-02", "Bàn đôi", "Trống", 0, 3, 2, 1));

        // Bàn 4 người (2x2)
        ds.add(new ModelKhongGian("B4-01", "Bàn 4 ng", "Trống", 0, 5, 2, 2));
        ds.add(new ModelKhongGian("B4-02", "Bàn 4 ng", "Bảo trì", 0, 8, 2, 2));

        // === KHU VỰC GIỮA - Bàn 8 người (2x3) ===
        ds.add(new ModelKhongGian("B8-01", "Bàn 8 ng", "Trống", 3, 0, 2, 3));
        ds.add(new ModelKhongGian("B8-02", "Bàn 8 ng", "Đang hoạt động", 3, 5, 2, 3));
        ds.add(new ModelKhongGian("B8-03", "Bàn 8 ng", "Trống", 3, 9, 2, 3));

        // === KHU VỰC PHÒNG HỌP ===
        // Phòng 16 người (3x4) - To hơn bàn 8
        ds.add(new ModelKhongGian("P16-01", "Phòng 16", "Trống", 6, 0, 3, 4));
        ds.add(new ModelKhongGian("P16-02", "Phòng 16", "Đã đặt trước", 6, 5, 3, 4));
        
        // Phòng 32 người (4x5) - To nhất
        ds.add(new ModelKhongGian("P32-01", "Phòng 32", "Trống", 11, 0, 4, 5));
        ds.add(new ModelKhongGian("P32-02", "Phòng 32", "Đang hoạt động", 11, 6, 4, 5));

        // Bàn 4 người thêm
        ds.add(new ModelKhongGian("B4-03", "Bàn 4 ng", "Trống", 6, 9, 2, 2));

        // === QUẦY LỄ TÂN ===
        ds.add(new ModelKhongGian("Quầy", "Quầy lễ tân", "Trang trí", 9, 5, 2, 2));

        view.veSoDo(ds);
        f.add(view);
        f.setVisible(true);
    }
}