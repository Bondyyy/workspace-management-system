package com.wms.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

public final class TienIchFormQuanLy {

    private static final Color MAU_XANH_SAN_SANG = new Color(40, 167, 69);
    private static final String KHOA_TRANG_THAI = "wms.tienIchFormQuanLy.trangThai";
    private static final String KHOA_MAU_NEN_GOC = "wms.tienIchFormQuanLy.mauNenGoc";
    private static final String KHOA_MAU_CHU_GOC = "wms.tienIchFormQuanLy.mauChuGoc";
    private static final String KHOA_ENTER = "wms.tienIchFormQuanLy.enter";
    private static final String KHOA_VIET_HOA = "wms.tienIchFormQuanLy.vietHoaChuCaiDau";

    private static final Map<String, CauHinh> CAU_HINH = taoCauHinh();

    private TienIchFormQuanLy() {
    }

    public static void apDung(Object form) {
        if (form == null) {
            return;
        }

        CauHinh cauHinh = CAU_HINH.get(form.getClass().getSimpleName());
        if (cauHinh == null) {
            return;
        }

        JComponent goc = timGoc(form);
        if (goc == null || goc.getClientProperty(KHOA_TRANG_THAI) != null) {
            return;
        }

        JButton nutThem = timThanhPhan(form, cauHinh.tenNutThem, JButton.class);
        JButton nutCapNhat = timThanhPhan(form, cauHinh.tenNutCapNhat, JButton.class);
        JTable bang = timThanhPhan(form, cauHinh.tenBang, JTable.class);
        List<JComponent> truongBatBuoc = timCacThanhPhan(form, cauHinh.tenTruongBatBuoc);
        List<JComponent> truongTheoDoi = timCacThanhPhan(form, cauHinh.tenTruongTheoDoi);

        if (truongTheoDoi.isEmpty()) {
            truongTheoDoi = truongBatBuoc;
        }

        TrangThai trangThai = new TrangThai(goc, nutThem, nutCapNhat, bang, truongBatBuoc, truongTheoDoi);
        goc.putClientProperty(KHOA_TRANG_THAI, trangThai);

        chuanBiNut(nutThem, trangThai);
        chuanBiNut(nutCapNhat, trangThai);

        Runnable khiThayDoi = () -> henCapNhat(trangThai);
        for (JComponent thanhPhan : khongTrung(truongBatBuoc, truongTheoDoi)) {
            ganLangNgheThayDoi(thanhPhan, khiThayDoi, trangThai);
        }

        if (bang != null) {
            bang.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    henChupTrangThaiDong(trangThai);
                }
            });
            bang.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    henChupTrangThaiDong(trangThai);
                }
            });
        }

        apDungVietHoaChuCaiDau(form);
        ganEnter(trangThai);
        ganThuTuTab(form, goc, cauHinh.tenThuTuTab, nutThem, nutCapNhat);
        henChupTrangThaiDong(trangThai);
    }

    public static void lamMoiTrangThai(Object form) {
        JComponent goc = timGoc(form);
        if (goc == null) {
            return;
        }
        Object trangThai = goc.getClientProperty(KHOA_TRANG_THAI);
        if (trangThai instanceof TrangThai trangThaiForm) {
            henChupTrangThaiDong(trangThaiForm);
        }
    }

    private static Map<String, CauHinh> taoCauHinh() {
        Map<String, CauHinh> cauHinh = new HashMap<>();
        cauHinh.put("QuanLyChiNhanhForm", cauHinh(
                "btnThemMoi", "btnCapNhat", "tblChiNhanh",
                ten("txtTenChiNhanh", "txtDiaChi", "txtHotline1", "cbxTrangThai"),
                ten("txtTenChiNhanh", "txtDiaChi", "txtHotline1", "cbxTrangThai", "txtGioMoCua", "txtGioDongCua"),
                ten("txtTenChiNhanh", "txtDiaChi", "txtGioMoCua", "txtGioDongCua", "txtHotline1", "cbxTrangThai")));
        cauHinh.put("QuanLyKhongGianForm", cauHinh(
                "btnThemMoi", "btnCapNhat", "tblKhongGian",
                ten("txtTenKhongGian", "cbxChiNhanh", "cbxLoaiKhongGian", "cbxTrangThai"),
                ten("txtTenKhongGian", "cbxChiNhanh", "cbxLoaiKhongGian", "cbxTrangThai"),
                ten("cbxTrangThai", "txtTenKhongGian", "cbxChiNhanh", "cbxLoaiKhongGian")));
        cauHinh.put("QuanLyLoaiKhongGianForm", cauHinh(
                "btnThemMoi", "btnCapNhat", "tblLoaiKhongGian",
                ten("txtTenLoaiKG", "txtDonGia", "cbxTrangThai"),
                ten("txtTenLoaiKG", "txtSucChua", "txtDonGia", "cbxTrangThai"),
                ten("txtTenLoaiKG", "txtSucChua", "txtDonGia", "cbxTrangThai")));
        cauHinh.put("QuanLyLoaiDichVuForm", cauHinh(
                "btnThem", "btnCapNhat", "tblLoaiDichVu",
                ten("txtTenLoai"),
                ten("txtTenLoai", "cbxTrangThai"),
                ten("txtTenLoai", "cbxTrangThai")));
        cauHinh.put("QuanLyHoiVienForm", cauHinh(
                "btnThemMoi", "btnCapNhat", "tblHoiVien",
                ten("txtHoTen", "txtSDT", "txtEmail", "cbxHangTV", "cbxNhomQuyen"),
                ten("txtHoTen", "txtSDT", "txtEmail", "txtNgaySinh", "cbxGioiTinh", "cbxHangTV", "cbxTrangThai", "cbxNhomQuyen"),
                ten("txtHoTen", "txtSDT", "txtEmail", "txtNgaySinh", "cbxGioiTinh", "cbxHangTV", "cbxTrangThai", "cbxNhomQuyen")));
        cauHinh.put("QuanLyThongTinDichVuForm", cauHinh(
                "btnThemMoi", "btnCapNhat", "tblDichVu",
                ten("txtTenDV", "txtDonGia", "cbxLoaiDV", "cbxTrangThai"),
                ten("txtTenDV", "txtDonGia", "cbxLoaiDV", "cbxTrangThai"),
                ten("cbxTrangThai", "cbxLoaiDV", "txtDonGia", "txtTenDV")));
        cauHinh.put("QuanLyPhieuGiamGiaForm", cauHinh(
                "btnThemMoi", "btnCapNhat", "tblPhieuGiamGia",
                ten("txtMaChuSoPGG", "txtGiaTriGiamGia", "txtGiaTriApDungToiThieu", "txtNgayBatDauApDung", "txtNgayKetThucApDung", "txtSLToiDa"),
                ten("txtMaChuSoPGG", "txtGiaTriGiamGia", "txtGiaTriApDungToiThieu", "txtNgayBatDauApDung", "txtNgayKetThucApDung", "txtSLToiDa"),
                ten("txtMaChuSoPGG", "txtGiaTriGiamGia", "txtGiaTriApDungToiThieu", "txtNgayBatDauApDung", "txtNgayKetThucApDung", "txtSLToiDa")));
        cauHinh.put("QuanLyNguoiDungForm", cauHinh(
                "btnThem", "btnCapNhat", "tblNguoiDung",
                ten("txtHoTen", "txtSDT", "txtEmail", "txtTaiKhoan", "txtMatKhau", "cbxNhomQuyen"),
                ten("txtHoTen", "txtSDT", "txtEmail", "txtTaiKhoan", "txtMatKhau", "txtNgaySinh", "cbxGioiTinh", "cbxTrangThai", "cbxNhomQuyen"),
                ten("cbxGioiTinh", "txtHoTen", "txtSDT", "txtEmail", "txtTaiKhoan", "txtMatKhau", "cbxTrangThai", "txtNgaySinh", "cbxNhomQuyen")));
        cauHinh.put("QuanLyNhanVienForm", cauHinh(
                "btnThemMoi", "btnCapNhat", "tblNhanVien",
                ten("txtHoTen", "txtSDT", "txtEmail", "txtTenTaiKhoan", "txtMatKhau", "cbxChiNhanh", "cbxTrangThai", "cbxNhomQuyen"),
                ten("txtHoTen", "txtSDT", "txtEmail", "txtTenTaiKhoan", "txtMatKhau", "txtNgaySinh", "txtLuong", "cbxGioiTinh", "cbxCaLam", "cbxChiNhanh", "cbxTrangThai", "cbxNhomQuyen"),
                ten("txtHoTen", "txtSDT", "txtTenTaiKhoan", "txtMatKhau", "txtEmail", "txtNgaySinh", "cbxGioiTinh", "cbxCaLam", "cbxChiNhanh", "cbxNhomQuyen", "txtLuong", "cbxTrangThai")));
        cauHinh.put("QuanLyHangTVForm", cauHinh(
                null, "btnSua", "tblHangTV",
                ten(),
                ten("txtMaHang", "txtTenHang", "spnPhanTram", "txtChiTieu"),
                ten("txtTenHang", "spnPhanTram", "txtChiTieu")));
        cauHinh.put("QuanLyHangThanhVienForm", cauHinh(
                null, "btnCapNhat", "tblHangTV",
                ten(),
                ten("txtMaHang", "txtTenHang", "spnPhanTram", "txtChiTieu"),
                ten("txtTenHang", "spnPhanTram", "txtChiTieu")));
        cauHinh.put("QuanLyVaiTroForm", cauHinh(
                "btnThemMoi", "btnCapNhat", "tblNhomQuyen",
                ten("txtTenNhom"),
                ten("txtTenNhom", "txtMoTa"),
                ten("txtTenNhom", "txtMoTa", "cbxChucNang")));
        cauHinh.put("MoPhienMoiForm", cauHinh(
                "btnMoPhien", null, null,
                ten("txtMaKGian", "txtTenKhachHang", "txtSoDienThoai", "txtThoiGianSuDung"),
                ten("txtMaKGian", "txtTenKhachHang", "txtSoDienThoai", "txtThoiGianSuDung", "txtThoiGianBatDau", "txtThoiGianKetThuc"),
                ten("txtTenKhachHang", "txtSoDienThoai", "txtThoiGianSuDung", "txtThoiGianBatDau", "txtThoiGianKetThuc")));
        cauHinh.put("QuanLyPhienForm", cauHinh(
                null, "btnCapNhat", "tblPhienLamViec",
                ten(),
                ten("txtBatDau", "txtDuKien", "txtKetThuc", "txtHinhThuc", "cbxTrangThai"),
                ten("txtBatDau", "txtDuKien", "txtKetThuc", "txtHinhThuc", "cbxTrangThai")));
        cauHinh.put("QuanLyDichVuDatForm", cauHinh(
                "btnLuu", "btnSua", "tableDichVu",
                ten("txtMaPhien", "cboLoaiDichVu", "cboTenDichVu", "spinSoLuong"),
                ten("txtMaPhien", "cboLoaiDichVu", "cboTenDichVu", "spinSoLuong", "txtGhiChu"),
                ten("txtMaPhien", "cboLoaiDichVu", "cboTenDichVu", "txtGhiChu", "spinSoLuong")));
        cauHinh.put("QuanLyKhoForm", cauHinh(
                "btnLuu", null, null,
                ten("cbNhanVien", "cbLoaiDichVu", "txtTenDichVu", "txtGiaNhap", "spnSoLuong"),
                ten("cbNhanVien", "cbLoaiDichVu", "txtTenDichVu", "txtGiaNhap", "spnSoLuong"),
                ten("cbNhanVien", "cbLoaiDichVu", "txtTenDichVu", "spnSoLuong", "txtGiaNhap")));
        cauHinh.put("QuanLyDatChoTruocForm", cauHinh(
                null, null, "tblDatCho",
                ten(),
                ten("txtTimKiem"),
                ten("txtTimKiem", "btnTimKiem", "btnLamMoi")));
        cauHinh.put("QuanLyHoaDonForm", cauHinh(
                null, null, "tblHoaDon",
                ten(),
                ten("txtTimKiem", "cbxLocTrangThai"),
                ten("txtTimKiem", "cbxLocTrangThai", "btnTimKiem", "btnLamMoi")));
        return Collections.unmodifiableMap(cauHinh);
    }

    private static CauHinh cauHinh(String tenNutThem, String tenNutCapNhat, String tenBang, List<String> tenTruongBatBuoc, List<String> tenTruongTheoDoi, List<String> tenThuTuTab) {
        return new CauHinh(tenNutThem, tenNutCapNhat, tenBang, tenTruongBatBuoc, tenTruongTheoDoi, tenThuTuTab);
    }

    private static List<String> ten(String... ten) {
        return Arrays.asList(ten);
    }

    private static JComponent timGoc(Object form) {
        if (form instanceof JComponent component) {
            return component;
        }
        if (form instanceof RootPaneContainer container) {
            JRootPane rootPane = container.getRootPane();
            return rootPane != null ? rootPane : null;
        }
        return null;
    }

    private static <T> T timThanhPhan(Object form, String tenTruong, Class<T> loai) {
        if (tenTruong == null || tenTruong.isBlank()) {
            return null;
        }

        Object giaTri = docTruong(form, tenTruong);
        if (loai.isInstance(giaTri)) {
            return loai.cast(giaTri);
        }
        return null;
    }

    private static List<JComponent> timCacThanhPhan(Object form, List<String> tenTruong) {
        List<JComponent> thanhPhan = new ArrayList<>();
        for (String ten : tenTruong) {
            Object giaTri = docTruong(form, ten);
            if (giaTri instanceof JComponent component) {
                thanhPhan.add(component);
            }
        }
        return thanhPhan;
    }

    private static Object docTruong(Object doiTuong, String tenTruong) {
        Class<?> loai = doiTuong.getClass();
        while (loai != null) {
            try {
                Field truong = loai.getDeclaredField(tenTruong);
                truong.setAccessible(true);
                return truong.get(doiTuong);
            } catch (NoSuchFieldException ignored) {
                loai = loai.getSuperclass();
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }
        return null;
    }

    private static List<JComponent> khongTrung(List<JComponent> truongBatBuoc, List<JComponent> truongTheoDoi) {
        List<JComponent> thanhPhan = new ArrayList<>();
        for (JComponent component : truongBatBuoc) {
            if (!thanhPhan.contains(component)) {
                thanhPhan.add(component);
            }
        }
        for (JComponent component : truongTheoDoi) {
            if (!thanhPhan.contains(component)) {
                thanhPhan.add(component);
            }
        }
        return thanhPhan;
    }

    private static void ganLangNgheThayDoi(JComponent thanhPhan, Runnable khiThayDoi, TrangThai trangThai) {
        if (thanhPhan instanceof JTextComponent textComponent) {
            textComponent.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    khiThayDoi.run();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    khiThayDoi.run();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    khiThayDoi.run();
                }
            });
            if (thanhPhan instanceof javax.swing.JTextField textField) {
                textField.addActionListener(event -> kichHoatNutSanSang(trangThai));
            }
        } else if (thanhPhan instanceof JComboBox<?> comboBox) {
            comboBox.addActionListener(event -> khiThayDoi.run());
        } else if (thanhPhan instanceof JSpinner spinner) {
            spinner.addChangeListener(event -> khiThayDoi.run());
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
                defaultEditor.getTextField().addActionListener(event -> kichHoatNutSanSang(trangThai));
            }
        } else if (thanhPhan instanceof AbstractButton button && !(thanhPhan instanceof JButton)) {
            button.addActionListener(event -> khiThayDoi.run());
        }
    }

    private static void chuanBiNut(JButton nut, TrangThai trangThai) {
        if (nut == null) {
            return;
        }
        nut.putClientProperty(KHOA_MAU_NEN_GOC, nut.getBackground());
        nut.putClientProperty(KHOA_MAU_CHU_GOC, nut.getForeground());
        nut.addPropertyChangeListener("enabled", event -> henCapNhat(trangThai));
    }

    private static void henChupTrangThaiDong(TrangThai trangThai) {
        SwingUtilities.invokeLater(() -> {
            trangThai.dangSua = trangThai.bang != null && trangThai.bang.getSelectedRow() >= 0;
            trangThai.trangThaiBanDau = trangThai.dangSua ? chupTrangThai(trangThai.truongTheoDoi) : Collections.emptyList();
            capNhatTrangThai(trangThai);
        });
    }

    private static void henCapNhat(TrangThai trangThai) {
        if (trangThai.dangChoCapNhat) {
            return;
        }
        trangThai.dangChoCapNhat = true;
        SwingUtilities.invokeLater(() -> {
            trangThai.dangChoCapNhat = false;
            capNhatTrangThai(trangThai);
        });
    }

    private static void capNhatTrangThai(TrangThai trangThai) {
        trangThai.nutThemSanSang = trangThai.nutThem != null
                && !trangThai.dangSua
                && tatCaDaNhap(trangThai.truongBatBuoc)
                && trangThai.nutThem.isEnabled();
        trangThai.nutCapNhatSanSang = trangThai.nutCapNhat != null
                && trangThai.dangSua
                && !trangThai.trangThaiBanDau.isEmpty()
                && !chupTrangThai(trangThai.truongTheoDoi).equals(trangThai.trangThaiBanDau)
                && trangThai.nutCapNhat.isEnabled();

        apDungTrangThaiNut(trangThai.nutThem, trangThai.nutThemSanSang);
        apDungTrangThaiNut(trangThai.nutCapNhat, trangThai.nutCapNhatSanSang);
    }

    private static boolean tatCaDaNhap(List<JComponent> thanhPhan) {
        for (JComponent component : thanhPhan) {
            if (!daNhap(component)) {
                return false;
            }
        }
        return true;
    }

    private static boolean daNhap(JComponent component) {
        if (component instanceof JTextComponent textComponent) {
            return !textComponent.getText().trim().isEmpty();
        }
        if (component instanceof JComboBox<?> comboBox) {
            Object selected = comboBox.getSelectedItem();
            return selected != null && coGiaTri(selected.toString());
        }
        if (component instanceof JSpinner spinner) {
            Object value = spinner.getValue();
            if (value instanceof Number number) {
                return number.doubleValue() > 0;
            }
            return value != null && coGiaTri(value.toString());
        }
        if (component instanceof AbstractButton button) {
            return button.isSelected();
        }
        return true;
    }

    private static boolean coGiaTri(String giaTri) {
        String chuanHoa = giaTri == null ? "" : giaTri.trim().toLowerCase();
        return !chuanHoa.isEmpty()
                && !chuanHoa.startsWith("--")
                && !chuanHoa.equals("chọn")
                && !chuanHoa.contains("chon")
                && !chuanHoa.contains("chọn");
    }

    private static List<String> chupTrangThai(List<JComponent> thanhPhan) {
        List<String> giaTri = new ArrayList<>();
        for (JComponent component : thanhPhan) {
            giaTri.add(giaTriCua(component));
        }
        return giaTri;
    }

    private static String giaTriCua(JComponent component) {
        if (component instanceof JTextComponent textComponent) {
            return textComponent.getText();
        }
        if (component instanceof JComboBox<?> comboBox) {
            Object selected = comboBox.getSelectedItem();
            return selected == null ? "" : selected.toString();
        }
        if (component instanceof JSpinner spinner) {
            Object value = spinner.getValue();
            return value == null ? "" : value.toString();
        }
        if (component instanceof AbstractButton button) {
            return Boolean.toString(button.isSelected());
        }
        return "";
    }

    private static void apDungTrangThaiNut(JButton nut, boolean sanSang) {
        if (nut == null) {
            return;
        }
        if (sanSang) {
            nut.setBackground(MAU_XANH_SAN_SANG);
            nut.setForeground(Color.WHITE);
            return;
        }

        Object mauNen = nut.getClientProperty(KHOA_MAU_NEN_GOC);
        Object mauChu = nut.getClientProperty(KHOA_MAU_CHU_GOC);
        if (mauNen instanceof Color color) {
            nut.setBackground(color);
        }
        if (mauChu instanceof Color color) {
            nut.setForeground(color);
        }
    }

    private static void ganEnter(TrangThai trangThai) {
        AbstractAction hanhDong = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                kichHoatNutSanSang(trangThai);
            }
        };

        trangThai.goc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KHOA_ENTER);
        trangThai.goc.getActionMap().put(KHOA_ENTER, hanhDong);
        ganEnterChoNut(trangThai.nutThem, hanhDong);
        ganEnterChoNut(trangThai.nutCapNhat, hanhDong);
    }

    private static void ganEnterChoNut(JButton nut, AbstractAction hanhDong) {
        if (nut == null) {
            return;
        }
        nut.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KHOA_ENTER);
        nut.getActionMap().put(KHOA_ENTER, hanhDong);
    }

    private static void ganThuTuTab(Object form, JComponent goc, List<String> tenThuTuTab, JButton nutThem, JButton nutCapNhat) {
        List<Component> thuTu = new ArrayList<>();
        for (String ten : tenThuTuTab) {
            Object giaTri = docTruong(form, ten);
            if (giaTri instanceof Component component && !thuTu.contains(component)) {
                Component focusTarget = timThanhPhanNhanFocus(component);
                if (!thuTu.contains(focusTarget)) {
                    thuTu.add(focusTarget);
                }
            }
        }
        if (nutThem != null && !thuTu.contains(nutThem)) {
            thuTu.add(nutThem);
        }
        if (nutCapNhat != null && !thuTu.contains(nutCapNhat)) {
            thuTu.add(nutCapNhat);
        }
        if (thuTu.size() < 2) {
            return;
        }

        Container vungFocus = form instanceof Container container ? container : goc;
        vungFocus.setFocusTraversalPolicy(new ThuTuTab(thuTu));
        vungFocus.setFocusTraversalPolicyProvider(true);
        vungFocus.setFocusCycleRoot(true);
    }

    private static Component timThanhPhanNhanFocus(Component component) {
        if (component instanceof JSpinner spinner && spinner.getEditor() instanceof JSpinner.DefaultEditor defaultEditor) {
            return defaultEditor.getTextField();
        }
        return component;
    }

    public static void apDungVietHoaChuCaiDau(JTextComponent textComponent) {
        if (textComponent == null || Boolean.TRUE.equals(textComponent.getClientProperty(KHOA_VIET_HOA))) {
            return;
        }
        if (textComponent.getDocument() instanceof AbstractDocument document
                && !(document.getDocumentFilter() instanceof VietHoaChuCaiDauFilter)
                && document.getDocumentFilter() == null) {
            document.setDocumentFilter(new VietHoaChuCaiDauFilter());
            textComponent.putClientProperty(KHOA_VIET_HOA, Boolean.TRUE);
            if (!textComponent.getText().isBlank()) {
                textComponent.setText(vietHoaChuCaiDau(textComponent.getText()));
            }
        }
    }

    private static void apDungVietHoaChuCaiDau(Object form) {
        Class<?> loai = form.getClass();
        while (loai != null && loai.getName().startsWith("com.wms.")) {
            for (Field field : loai.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object giaTri = field.get(form);
                    if (giaTri instanceof JTextComponent textComponent
                            && textComponent.isEditable()
                            && nenVietHoaChuCaiDau(field.getName())) {
                        apDungVietHoaChuCaiDau(textComponent);
                    }
                } catch (IllegalAccessException | SecurityException ignored) {
                    // Bỏ qua field không đọc được; các field khác vẫn được xử lý bình thường.
                }
            }
            loai = loai.getSuperclass();
        }
    }

    private static boolean nenVietHoaChuCaiDau(String tenTruong) {
        if (tenTruong == null) {
            return false;
        }
        String ten = tenTruong.toLowerCase();
        if (ten.contains("timkiem")
                || ten.contains("ma")
                || ten.contains("taikhoan")
                || ten.contains("matkhau")
                || ten.contains("email")
                || ten.contains("sdt")
                || ten.contains("dienthoai")
                || ten.contains("hotline")
                || ten.contains("ngay")
                || ten.contains("gio")
                || ten.contains("thoigian")
                || ten.contains("tien")
                || ten.contains("gia")
                || ten.contains("don")
                || ten.contains("luong")
                || ten.contains("qr")
                || ten.contains("giam")
                || ten.contains("so")
                || ten.contains("sl")
                || ten.contains("succhua")
                || ten.contains("toado")) {
            return false;
        }
        return ten.contains("hoten")
                || ten.contains("ten")
                || ten.contains("diachi")
                || ten.contains("ghichu")
                || ten.contains("mota");
    }

    private static String vietHoaChuCaiDau(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isLetter(ch)) {
                char upper = Character.toUpperCase(ch);
                if (upper == ch) {
                    return value;
                }
                return value.substring(0, i) + upper + value.substring(i + 1);
            }
            if (!Character.isWhitespace(ch)) {
                return value;
            }
        }
        return value;
    }

    private static void kichHoatNutSanSang(TrangThai trangThai) {
        if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof javax.swing.JTextArea) {
            return;
        }

        capNhatTrangThai(trangThai);
        if (trangThai.nutCapNhatSanSang && trangThai.nutCapNhat != null) {
            trangThai.nutCapNhat.doClick();
        } else if (trangThai.nutThemSanSang && trangThai.nutThem != null) {
            trangThai.nutThem.doClick();
        }
    }

    private static final class CauHinh {
        private final String tenNutThem;
        private final String tenNutCapNhat;
        private final String tenBang;
        private final List<String> tenTruongBatBuoc;
        private final List<String> tenTruongTheoDoi;
        private final List<String> tenThuTuTab;

        private CauHinh(String tenNutThem, String tenNutCapNhat, String tenBang, List<String> tenTruongBatBuoc, List<String> tenTruongTheoDoi, List<String> tenThuTuTab) {
            this.tenNutThem = tenNutThem;
            this.tenNutCapNhat = tenNutCapNhat;
            this.tenBang = tenBang;
            this.tenTruongBatBuoc = tenTruongBatBuoc;
            this.tenTruongTheoDoi = tenTruongTheoDoi;
            this.tenThuTuTab = tenThuTuTab;
        }
    }

    private static final class ThuTuTab extends FocusTraversalPolicy {
        private final List<Component> thuTu;

        private ThuTuTab(List<Component> thuTu) {
            this.thuTu = new ArrayList<>(thuTu);
        }

        @Override
        public Component getComponentAfter(Container root, Component component) {
            return timThanhPhanKeTiep(component, 1);
        }

        @Override
        public Component getComponentBefore(Container root, Component component) {
            return timThanhPhanKeTiep(component, -1);
        }

        @Override
        public Component getFirstComponent(Container root) {
            List<Component> hopLe = thanhPhanHopLe();
            return hopLe.isEmpty() ? null : hopLe.get(0);
        }

        @Override
        public Component getLastComponent(Container root) {
            List<Component> hopLe = thanhPhanHopLe();
            return hopLe.isEmpty() ? null : hopLe.get(hopLe.size() - 1);
        }

        @Override
        public Component getDefaultComponent(Container root) {
            return getFirstComponent(root);
        }

        private Component timThanhPhanKeTiep(Component hienTai, int huong) {
            List<Component> hopLe = thanhPhanHopLe();
            if (hopLe.isEmpty()) {
                return null;
            }

            int viTri = timViTri(hienTai, hopLe);
            if (viTri < 0) {
                return huong > 0 ? hopLe.get(0) : hopLe.get(hopLe.size() - 1);
            }

            int viTriMoi = (viTri + huong + hopLe.size()) % hopLe.size();
            return hopLe.get(viTriMoi);
        }

        private int timViTri(Component hienTai, List<Component> hopLe) {
            for (int i = 0; i < hopLe.size(); i++) {
                Component component = hopLe.get(i);
                if (component == hienTai) {
                    return i;
                }
                if (component instanceof Container container && SwingUtilities.isDescendingFrom(hienTai, container)) {
                    return i;
                }
            }
            return -1;
        }

        private List<Component> thanhPhanHopLe() {
            List<Component> hopLe = new ArrayList<>();
            for (Component component : thuTu) {
                if (component != null && component.isVisible() && component.isEnabled() && component.isFocusable()) {
                    hopLe.add(component);
                }
            }
            return hopLe;
        }
    }

    private static final class VietHoaChuCaiDauFilter extends DocumentFilter {
        private boolean dangXuLy;

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            replace(fb, offset, length, "", null);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (dangXuLy) {
                fb.replace(offset, length, text, attrs);
                return;
            }

            javax.swing.text.Document document = fb.getDocument();
            String current = document.getText(0, document.getLength());
            String replacement = text == null ? "" : text;
            String candidate = current.substring(0, offset)
                    + replacement
                    + current.substring(offset + length);
            String formatted = vietHoaChuCaiDau(candidate);
            if (formatted.equals(candidate)) {
                fb.replace(offset, length, text, attrs);
                return;
            }

            dangXuLy = true;
            try {
                fb.replace(0, document.getLength(), formatted, attrs);
            } finally {
                dangXuLy = false;
            }
        }
    }

    private static final class TrangThai {
        private final JComponent goc;
        private final JButton nutThem;
        private final JButton nutCapNhat;
        private final JTable bang;
        private final List<JComponent> truongBatBuoc;
        private final List<JComponent> truongTheoDoi;
        private List<String> trangThaiBanDau = Collections.emptyList();
        private boolean dangSua;
        private boolean nutThemSanSang;
        private boolean nutCapNhatSanSang;
        private boolean dangChoCapNhat;

        private TrangThai(JComponent goc, JButton nutThem, JButton nutCapNhat, JTable bang, List<JComponent> truongBatBuoc, List<JComponent> truongTheoDoi) {
            this.goc = goc;
            this.nutThem = nutThem;
            this.nutCapNhat = nutCapNhat;
            this.bang = bang;
            this.truongBatBuoc = truongBatBuoc;
            this.truongTheoDoi = truongTheoDoi;
        }
    }
}
