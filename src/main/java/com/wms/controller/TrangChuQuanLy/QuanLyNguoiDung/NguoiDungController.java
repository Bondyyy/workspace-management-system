package com.wms.controller.TrangChuQuanLy.QuanLyNguoiDung;

import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.service.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungService;
import com.wms.view.TrangChuQuanLy.QuanLyNguoiDung.QuanLyNguoiDungForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.ByteArrayInputStream;

public class NguoiDungController {
    private final QuanLyNguoiDungForm view;
    private final NguoiDungService service;
    private List<NguoiDungDTO> currentList;
    private byte[] selectedImageData;

    public NguoiDungController(QuanLyNguoiDungForm view) {
        this.view = view;
        this.service = new NguoiDungService();
        initController();
    }

    private void initController() {
        loadData();
        clearForm();
    }

    public void loadData() {
        try {
            currentList = service.getAllUsers();
            fillTable(currentList);
        } catch (SQLException e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    public void search(String keyword) {
        try {
            currentList = service.searchUsers(keyword);
            fillTable(currentList);
        } catch (SQLException e) {
            showError("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void fillTable(List<NguoiDungDTO> list) {
        DefaultTableModel model = (DefaultTableModel) view.getTblNguoiDung().getModel();
        model.setRowCount(0);
        for (NguoiDungDTO user : list) {
            model.addRow(new Object[]{
                    user.getMaND(),
                    user.getHoTen(),
                    user.getTenTaiKhoan(),
                    user.getGioiTinh(),
                    user.getSdt(),
                    user.getEmail(),
                    user.getTrangThaiND()
            });
        }
    }

    public void displaySelectedUser() {
        int row = view.getTblNguoiDung().getSelectedRow();
        if (row < 0) return;

        NguoiDungDTO user = currentList.get(row);
        view.getTxtMaND().setText(user.getMaND());
        view.getTxtTaiKhoan().setText(user.getTenTaiKhoan());
        view.getTxtHoTen().setText(user.getHoTen());
        view.getCbxGioiTinh().setSelectedItem(user.getGioiTinh());
        view.getTxtNgaySinh().setText(user.getNgaySinh() != null ? 
                new java.text.SimpleDateFormat("dd/MM/yyyy").format(user.getNgaySinh()) : "");
        view.getTxtSDT().setText(user.getSdt());
        view.getTxtEmail().setText(user.getEmail());
        
        String trangThai = user.getTrangThaiND();
        if ("Đang hoạt động".equals(trangThai)) {
            view.getCbxTrangThai().setSelectedItem("Hoạt động");
        } else {
            view.getCbxTrangThai().setSelectedItem("Đã khóa");
        }
        
        // Handle Password
        view.getTxtMatKhau().setText(user.getMatKhauMaHoa());

        // Handle Image
        selectedImageData = user.getAnhDaiDien();
        if (selectedImageData != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(user.getAnhDaiDien());
                BufferedImage bImage = ImageIO.read(bis);
                if (bImage != null) {
                    Image scaledImage = bImage.getScaledInstance(view.getLblAnhDaiDien().getWidth(), 
                                                               view.getLblAnhDaiDien().getHeight(), Image.SCALE_SMOOTH);
                    view.getLblAnhDaiDien().setIcon(new ImageIcon(scaledImage));
                    view.getLblAnhDaiDien().setText("");
                } else {
                    view.getLblAnhDaiDien().setIcon(null);
                    view.getLblAnhDaiDien().setText("[Ảnh lỗi]");
                }
            } catch (IOException e) {
                view.getLblAnhDaiDien().setIcon(null);
                view.getLblAnhDaiDien().setText("[Lỗi ảnh]");
            }
        } else {
            view.getLblAnhDaiDien().setIcon(null);
            view.getLblAnhDaiDien().setText("[Chưa có ảnh]");
        }
    }

    public void handleAdd() {
        try {
            NguoiDungDTO user = collectFormData();
            if (user == null) return;
            user.setMaND(view.getTxtMaND().getText()); // Lấy mã hiển thị sẵn
            
            service.addUser(user);
            com.wms.util.MessageUtil.showInfo(view, "Thêm người dùng mới thành công!");
            loadData();
            clearForm();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public void handleUpdate() {
        try {
            String maND = view.getTxtMaND().getText();
            if (maND == null || maND.isEmpty()) {
                showError("Vui lòng chọn người dùng để cập nhật!");
                return;
            }
            
            NguoiDungDTO user = collectFormData();
            if (user == null) return;
            user.setMaND(maND);
            
            service.updateUser(user);
            com.wms.util.MessageUtil.showInfo(view, "Cập nhật thông tin người dùng thành công!");
            loadData();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public void clearForm() {
        try {
            view.getTxtMaND().setText(service.generateNextMaND());
        } catch (SQLException e) {
            view.getTxtMaND().setText("");
        }
        view.getTxtTaiKhoan().setText("");
        view.getTxtHoTen().setText("");
        view.getCbxGioiTinh().setSelectedIndex(0);
        view.getTxtNgaySinh().setText("");
        view.getTxtSDT().setText("");
        view.getTxtEmail().setText("");
        view.getCbxTrangThai().setSelectedIndex(0);
        view.getTxtMatKhau().setText("");
        view.getLblAnhDaiDien().setIcon(null);
        view.getLblAnhDaiDien().setText("[Ảnh 3x4]");
        selectedImageData = null;
        view.getTblNguoiDung().clearSelection();
    }

    private NguoiDungDTO collectFormData() {
        NguoiDungDTO user = new NguoiDungDTO();
        user.setTenTaiKhoan(view.getTxtTaiKhoan().getText().trim());
        user.setHoTen(view.getTxtHoTen().getText().trim());
        user.setGioiTinh(view.getCbxGioiTinh().getSelectedItem().toString());
        user.setSdt(view.getTxtSDT().getText().trim());
        user.setEmail(view.getTxtEmail().getText().trim());
        
        String uiTrangThai = view.getCbxTrangThai().getSelectedItem().toString();
        user.setTrangThaiND("Hoạt động".equals(uiTrangThai) ? "Đang hoạt động" : "Không hoạt động");
        
        user.setAnhDaiDien(selectedImageData);

        try {
            String birthStr = view.getTxtNgaySinh().getText().trim();
            if (!birthStr.isEmpty()) {
                java.util.Date date = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(birthStr);
                user.setNgaySinh(new java.sql.Date(date.getTime()));
            }
        } catch (Exception e) {
            showError("Ngày sinh không hợp lệ (định dạng dd/MM/yyyy)!");
            return null;
        }

        return user;
    }

    public void handleChangeImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg", "gif"));
        int result = fileChooser.showOpenDialog(view);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                
                // Hiển thị ảnh preview
                ImageIcon icon = new ImageIcon(imageData);
                Image img = icon.getImage().getScaledInstance(view.getLblAnhDaiDien().getWidth(), 
                                                            view.getLblAnhDaiDien().getHeight(), Image.SCALE_SMOOTH);
                view.getLblAnhDaiDien().setIcon(new ImageIcon(img));
                view.getLblAnhDaiDien().setText("");
                
                selectedImageData = imageData;
            } catch (IOException e) {
                showError("Lỗi đọc file ảnh: " + e.getMessage());
            }
        }
    }

    private void showError(String msg) {
        com.wms.util.MessageUtil.showError(view, msg);
    }
}
