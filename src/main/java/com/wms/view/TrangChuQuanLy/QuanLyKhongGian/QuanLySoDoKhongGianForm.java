package com.wms.view.TrangChuQuanLy.QuanLyKhongGian;

import com.wms.controller.TrangChuQuanLy.QuanLyKhongGian.KhongGianController;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import com.wms.util.SoDoKhongGianPanel;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class QuanLySoDoKhongGianForm extends javax.swing.JDialog {

    private static final Logger logger = Logger.getLogger(QuanLySoDoKhongGianForm.class.getName());
    private final KhongGianController controller = new KhongGianController();
    private String maCN;
    private List<KhongGianDTO> dsKG;
    private Map<String, KhongGianDTO> mapKG = new HashMap<>(); // lookup nhanh theo MaKG
    
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

    private String selectedMaKG = null;

    public QuanLySoDoKhongGianForm(java.awt.Frame parent, boolean modal, String maCN, String tenCN) {
        super(parent, modal);
        this.maCN = maCN;
        initComponents();
        this.setContentPane(pnMain);
        if (tenCN != null) lblHeaderTitle.setText("SƠ ĐỒ KHÔNG GIAN - " + tenCN.toUpperCase());
        initCustomUI();
        taiDanhSach();
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    public QuanLySoDoKhongGianForm(java.awt.Frame parent, boolean modal) {
        this(parent, modal, "CN001", "Chi nhánh mặc định");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMain = new javax.swing.JPanel();
        pnHeader = new javax.swing.JPanel();
        lblHeaderTitle = new javax.swing.JLabel();
        pnLeft = new javax.swing.JPanel();
        lblListTitle = new javax.swing.JLabel();
        scrollKhongGian = new javax.swing.JScrollPane();
        tblKhongGian = new javax.swing.JTable();
        lblToaDoX = new javax.swing.JLabel();
        txtToaDoX = new javax.swing.JTextField();
        lblToaDoY = new javax.swing.JLabel();
        txtToaDoY = new javax.swing.JTextField();
        lblChieuRong = new javax.swing.JLabel();
        txtChieuRong = new javax.swing.JTextField();
        lblChieuDai = new javax.swing.JLabel();
        txtChieuDai = new javax.swing.JTextField();
        btnGo = new javax.swing.JButton();
        btnCapNhatToaDo = new javax.swing.JButton();
        btnLuuCSDL1 = new javax.swing.JButton();
        pnRight = new javax.swing.JPanel();
        lblDetailTitle = new javax.swing.JLabel();
        scrollSoDo = new javax.swing.JScrollPane();
        pnBaoNgoai = new com.wms.util.SoDoKhongGianPanel();
        pnChuThich = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Quản lý Sơ đồ Không Gian");
        setModal(true);
        setResizable(false);

        pnMain.setBackground(new java.awt.Color(254, 248, 250));
        pnMain.setPreferredSize(new java.awt.Dimension(1050, 650));
        pnMain.setLayout(null);

        pnHeader.setBackground(new java.awt.Color(35, 30, 48));
        pnHeader.setLayout(null);

        lblHeaderTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblHeaderTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeaderTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeaderTitle.setText("CẬP NHẬT TỌA ĐỘ SƠ ĐỒ KHÔNG GIAN");
        pnHeader.add(lblHeaderTitle);
        lblHeaderTitle.setBounds(0, 0, 1050, 50);

        pnMain.add(pnHeader);
        pnHeader.setBounds(0, 0, 1050, 50);

        pnLeft.setBackground(new java.awt.Color(255, 255, 255));
        pnLeft.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnLeft.setLayout(null);

        lblListTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblListTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblListTitle.setText("DANH SÁCH KHÔNG GIAN");
        pnLeft.add(lblListTitle);
        lblListTitle.setBounds(15, 15, 250, 30);

        tblKhongGian.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã KG", "Tên KG", "X", "Y", "Rộng", "Dài"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblKhongGian.setRowHeight(25);
        tblKhongGian.setSelectionBackground(new java.awt.Color(235, 94, 141));
        tblKhongGian.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKhongGianMouseClicked(evt);
            }
        });
        scrollKhongGian.setViewportView(tblKhongGian);

        pnLeft.add(scrollKhongGian);
        scrollKhongGian.setBounds(15, 50, 320, 230);

        lblToaDoX.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblToaDoX.setForeground(new java.awt.Color(35, 30, 48));
        lblToaDoX.setText("Toạ độ X:");
        pnLeft.add(lblToaDoX);
        lblToaDoX.setBounds(15, 290, 150, 18);

        txtToaDoX.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnLeft.add(txtToaDoX);
        txtToaDoX.setBounds(15, 310, 150, 30);

        lblToaDoY.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblToaDoY.setForeground(new java.awt.Color(35, 30, 48));
        lblToaDoY.setText("Toạ độ Y:");
        pnLeft.add(lblToaDoY);
        lblToaDoY.setBounds(185, 290, 150, 18);

        txtToaDoY.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnLeft.add(txtToaDoY);
        txtToaDoY.setBounds(185, 310, 150, 30);

        lblChieuRong.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChieuRong.setForeground(new java.awt.Color(35, 30, 48));
        lblChieuRong.setText("Rộng (Ngang):");
        pnLeft.add(lblChieuRong);
        lblChieuRong.setBounds(15, 350, 150, 18);

        txtChieuRong.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnLeft.add(txtChieuRong);
        txtChieuRong.setBounds(15, 370, 150, 30);

        lblChieuDai.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblChieuDai.setForeground(new java.awt.Color(35, 30, 48));
        lblChieuDai.setText("Dài (Dọc):");
        pnLeft.add(lblChieuDai);
        lblChieuDai.setBounds(185, 350, 150, 18);

        txtChieuDai.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        pnLeft.add(txtChieuDai);
        txtChieuDai.setBounds(185, 370, 150, 30);

        btnGo.setBackground(new java.awt.Color(220, 53, 69));
        btnGo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnGo.setForeground(new java.awt.Color(255, 255, 255));
        btnGo.setText("Gỡ không gian khỏi bản đồ");
        btnGo.addActionListener(this::btnGoActionPerformed);
        pnLeft.add(btnGo);
        btnGo.setBounds(15, 450, 320, 35);

        btnCapNhatToaDo.setBackground(new java.awt.Color(235, 94, 141));
        btnCapNhatToaDo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCapNhatToaDo.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhatToaDo.setText("Cập nhật trên Sơ đồ");
        btnCapNhatToaDo.addActionListener(this::btnCapNhatToaDoActionPerformed);
        pnLeft.add(btnCapNhatToaDo);
        btnCapNhatToaDo.setBounds(15, 410, 320, 35);

        btnLuuCSDL1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLuuCSDL1.setForeground(new java.awt.Color(235, 94, 141));
        btnLuuCSDL1.setText("Lưu Sơ đồ");
        btnLuuCSDL1.addActionListener(this::btnLuuCSDL1ActionPerformed);
        pnLeft.add(btnLuuCSDL1);
        btnLuuCSDL1.setBounds(15, 490, 320, 35);

        pnMain.add(pnLeft);
        pnLeft.setBounds(10, 70, 350, 530);

        pnRight.setBackground(new java.awt.Color(255, 255, 255));
        pnRight.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 0, 0, 0, new java.awt.Color(235, 94, 141)));
        pnRight.setLayout(null);

        lblDetailTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblDetailTitle.setForeground(new java.awt.Color(48, 30, 35));
        lblDetailTitle.setText("SƠ ĐỒ CHI NHÁNH TRỰC QUAN");
        pnRight.add(lblDetailTitle);
        lblDetailTitle.setBounds(20, 15, 300, 30);

        scrollSoDo.setBorder(null);

        pnBaoNgoai.setBackground(new java.awt.Color(254, 248, 250));
        scrollSoDo.setViewportView(pnBaoNgoai);

        pnRight.add(scrollSoDo);
        scrollSoDo.setBounds(10, 50, 650, 460);

        pnChuThich.setBackground(new java.awt.Color(255, 255, 255));
        pnChuThich.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        pnRight.add(pnChuThich);
        pnChuThich.setBounds(20, 470, 610, 50);

        pnMain.add(pnRight);
        pnRight.setBounds(370, 70, 670, 530);

        getContentPane().add(pnMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    private void initCustomUI() {
        pnBaoNgoai.setManagementMode(true);
        pnBaoNgoai.setOnTableClick(this::handleTableClick);

        pnChuThich.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        pnChuThich.add(taoMucChuThich("Quầy lễ tân", mauLeTan, Color.BLACK));
        pnChuThich.add(taoMucChuThich("Trống", mauTrong_Nen, mauTrong_Vien));
        pnChuThich.add(taoMucChuThich("Đã đặt", mauDaDat_Nen, mauDaDat_Vien));
        pnChuThich.add(taoMucChuThich("Đang dùng", mauDangDung_Nen, mauDangDung_Vien));
        pnChuThich.add(taoMucChuThich("Bảo trì", mauBaoTri_Nen, mauBaoTri_Vien));
    }

    private void handleTableClick(com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO kg) {
        for(int i=0; i<tblKhongGian.getRowCount(); i++) {
            if(tblKhongGian.getValueAt(i, 0).equals(kg.getMaKG())) {
                tblKhongGian.setRowSelectionInterval(i, i);
                tblKhongGianMouseClicked(null);
                break;
            }
        }
    }

    private javax.swing.JPanel taoMucChuThich(String ten, Color nen, Color vien) {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        javax.swing.JLabel box = new javax.swing.JLabel();
        box.setPreferredSize(new Dimension(15, 15));
        box.setOpaque(true);
        box.setBackground(nen);
        box.setBorder(javax.swing.BorderFactory.createLineBorder(vien, 1));
        p.add(box);
        p.add(new javax.swing.JLabel(ten));
        return p;
    }

    private void taiDanhSach() {
        dsKG = controller.layTheoChiNhanh(maCN);
        mapKG.clear();
        for (KhongGianDTO dto : dsKG) mapKG.put(dto.getMaKG(), dto);

        DefaultTableModel model = (DefaultTableModel) tblKhongGian.getModel();
        model.setRowCount(0);
        for (KhongGianDTO dto : dsKG) {
            model.addRow(new Object[]{
                dto.getMaKG(), dto.getTenKG(), 
                dto.getToaDoX() != null ? dto.getToaDoX() : "", 
                dto.getToaDoY() != null ? dto.getToaDoY() : "",
                dto.getChieuDai(), dto.getChieuRong()
            });
        }
        veSoDo();
    }

    private void veSoDo() {
        if (pnBaoNgoai != null) {
            pnBaoNgoai.veSoDo(dsKG, selectedMaKG);
        }
    }

    private void tblKhongGianMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tblKhongGian.getSelectedRow();
        if (row < 0) return;
        String maKG = tblKhongGian.getValueAt(row, 0).toString();
        KhongGianDTO dto = mapKG.get(maKG);
        if (dto == null) return;
        txtToaDoX.setText(dto.getToaDoX() != null ? String.valueOf(dto.getToaDoX()) : "");
        txtToaDoY.setText(dto.getToaDoY() != null ? String.valueOf(dto.getToaDoY()) : "");
        txtChieuRong.setText(String.valueOf(dto.getChieuDai()));
        txtChieuDai.setText(String.valueOf(dto.getChieuRong()));
    }

    private void btnCapNhatToaDoActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblKhongGian.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một không gian trong bảng!");
            return;
        }
        try {
            String maKG = tblKhongGian.getValueAt(row, 0).toString();
            KhongGianDTO dto = mapKG.get(maKG);
            if (dto != null && "Ngừng hoạt động".equals(dto.getTrangThaiLoaiKG())) {
                JOptionPane.showMessageDialog(this, "Loại không gian này đã ngừng hoạt động, không thể đặt lên sơ đồ!");
                return;
            }
            int x = Integer.parseInt(txtToaDoX.getText().trim());
            int y = Integer.parseInt(txtToaDoY.getText().trim());
            int w = Integer.parseInt(txtChieuRong.getText().trim());
            int h = Integer.parseInt(txtChieuDai.getText().trim());

            if (x < 0 || y < 0 || w <= 0 || h <= 0) {
                JOptionPane.showMessageDialog(this, "Toạ độ và kích thước không hợp lệ (X, Y >= 0; W, H > 0)!");
                return;
            }
            if (x + w > 12 || y + h > 8) {
                JOptionPane.showMessageDialog(this, "Không gian vượt quá diện tích sơ đồ (12x8)!");
                return;
            }
            if (kiemTraChongLanVoiVungCoDinh(x, y, w, h)) {
                JOptionPane.showMessageDialog(this, "Không gian bị chồng lấn với khu vực RECEPTION hoặc ENTRANCE!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String loiChongLan = kiemTraChongLan(maKG, x, y, w, h);
            if (loiChongLan != null) {
                JOptionPane.showMessageDialog(this, loiChongLan, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dto != null) {
                dto.setToaDoX(x); dto.setToaDoY(y);
                dto.setChieuDai(w); dto.setChieuRong(h);
                tblKhongGian.setValueAt(x, row, 2);
                tblKhongGian.setValueAt(y, row, 3);
                tblKhongGian.setValueAt(w, row, 4);
                tblKhongGian.setValueAt(h, row, 5);
                veSoDo();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Toạ độ và kích thước phải là số nguyên!");
        }
    }

    private void btnGoActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblKhongGian.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một không gian trong bảng để gỡ!");
            return;
        }
        String maKG = tblKhongGian.getValueAt(row, 0).toString();
        KhongGianDTO dto = mapKG.get(maKG);
        if (dto != null) {
            dto.setToaDoX(null); dto.setToaDoY(null);
            tblKhongGian.setValueAt("", row, 2);
            tblKhongGian.setValueAt("", row, 3);
            txtToaDoX.setText("");
            txtToaDoY.setText("");
            veSoDo();
        }
    }

    private void btnLuuCSDL1ActionPerformed(java.awt.event.ActionEvent evt) {
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn lưu toàn bộ thay đổi toạ độ vào CSDL không?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (xacNhan == JOptionPane.YES_OPTION) {
            String loi = controller.luuToaDo(dsKG);
            if (loi == null) {
                JOptionPane.showMessageDialog(this, "Lưu thành công!");
                taiDanhSach();
            } else {
                JOptionPane.showMessageDialog(this, loi, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean kiemTraChongLanVoiVungCoDinh(int x, int y, int w, int h) {
        boolean chongReception = (x < 2) && (x + w > 0) && (y < 1) && (y + h > 0);
        boolean chongEntrance  = (x < 3) && (x + w > 2) && (y < 1) && (y + h > 0);
        return chongReception || chongEntrance;
    }

    private String kiemTraChongLan(String maKGHienTai, int x, int y, int w, int h) {
        for (KhongGianDTO other : dsKG) {
            if (other.getMaKG().equals(maKGHienTai)) continue;
            if (other.getToaDoX() == null || other.getToaDoY() == null) continue;
            
            int ox = other.getToaDoX(), oy = other.getToaDoY();
            int ow = other.getChieuDai() > 0 ? other.getChieuDai() : 1;
            int oh = other.getChieuRong() > 0 ? other.getChieuRong() : 1;
            
            if ((x < ox + ow) && (x + w > ox) && (y < oy + oh) && (y + h > oy)) {
                return String.format("Không gian bị chồng lấn với %s (%s)!", other.getMaKG(), other.getTenKG());
            }
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                QuanLySoDoKhongGianForm dialog = new QuanLySoDoKhongGianForm(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhatToaDo;
    private javax.swing.JButton btnGo;
    private javax.swing.JButton btnLuuCSDL1;
    private javax.swing.JLabel lblChieuDai;
    private javax.swing.JLabel lblChieuRong;
    private javax.swing.JLabel lblDetailTitle;
    private javax.swing.JLabel lblHeaderTitle;
    private javax.swing.JLabel lblListTitle;
    private javax.swing.JLabel lblToaDoX;
    private javax.swing.JLabel lblToaDoY;
    private com.wms.util.SoDoKhongGianPanel pnBaoNgoai;
    private javax.swing.JPanel pnChuThich;
    private javax.swing.JPanel pnHeader;
    private javax.swing.JPanel pnLeft;
    private javax.swing.JPanel pnMain;
    private javax.swing.JPanel pnRight;
    private javax.swing.JScrollPane scrollKhongGian;
    private javax.swing.JScrollPane scrollSoDo;
    private javax.swing.JTable tblKhongGian;
    private javax.swing.JTextField txtChieuDai;
    private javax.swing.JTextField txtChieuRong;
    private javax.swing.JTextField txtToaDoX;
    private javax.swing.JTextField txtToaDoY;
    // End of variables declaration//GEN-END:variables
}


