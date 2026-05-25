package com.wms.controller.TrangChuQuanLy.QuanLyLoaiDichVu;

import com.wms.model.TrangChuQuanLy.QuanLyLoaiDichVu.LoaiDichVuDTO;
import com.wms.service.TrangChuQuanLy.QuanLyLoaiDichVu.LoaiDichVuService;
import com.wms.view.TrangChuQuanLy.QuanLyLoaiDichVu.QuanLyLoaiDichVuForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

public class LoaiDichVuController {
    private final QuanLyLoaiDichVuForm view;
    private final LoaiDichVuService service;
    private List<LoaiDichVuDTO> currentList;

    public LoaiDichVuController(QuanLyLoaiDichVuForm view) {
        this.view = view;
        this.service = new LoaiDichVuService();
        initController();
    }

    private void initController() {
        loadData();
        clearForm();
    }

    public void loadData() {
        try {
            currentList = service.getAllLoaiDichVu();
            fillTable(currentList);
        } catch (SQLException e) {
            showError("Lỗi tải dữ liệu.", e);
        }
    }

    public void search(String keyword) {
        try {
            currentList = service.searchLoaiDichVu(keyword);
            fillTable(currentList);
        } catch (SQLException e) {
            showError("Lỗi tìm kiếm.", e);
        }
    }

    private void fillTable(List<LoaiDichVuDTO> list) {
        DefaultTableModel model = (DefaultTableModel) view.getTblLoaiDichVu().getModel();
        model.setRowCount(0);
        for (LoaiDichVuDTO loai : list) {
            model.addRow(new Object[]{
                    loai.getMaLoaiDV(),
                    loai.getTenLoaiDV(),
                    loai.getTrangThaiLDV()
            });
        }
    }

    public void displaySelected() {
        int row = view.getTblLoaiDichVu().getSelectedRow();
        if (row < 0) return;

        LoaiDichVuDTO loai = currentList.get(row);
        view.getTxtMaLoai().setText(loai.getMaLoaiDV());
        view.getTxtTenLoai().setText(loai.getTenLoaiDV());
        
        String trangThai = loai.getTrangThaiLDV();
        if ("Đang hoạt động".equals(trangThai) || "Hoạt động".equals(trangThai)) {
            view.getCbxTrangThai().setSelectedItem("Hoạt động");
        } else {
            view.getCbxTrangThai().setSelectedItem("Ngừng kinh doanh");
        }
    }

    public void handleAdd() {
        try {
            LoaiDichVuDTO loai = collectFormData();
            if (loai == null) return;
            loai.setMaLoaiDV(view.getTxtMaLoai().getText()); // Lấy mã từ textfield đã hiển thị sẵn
            
            service.addLoaiDichVu(loai);
            com.wms.util.MessageUtil.showInfo(view, "Thêm loại dịch vụ mới thành công!");
            loadData();
            clearForm();
        } catch (Exception e) {
            showError(e.getMessage(), e);
        }
    }

    public void handleUpdate() {
        try {
            String ma = view.getTxtMaLoai().getText();
            if (ma == null || ma.isEmpty()) {
                showError("Vui lòng chọn loại dịch vụ để cập nhật!");
                return;
            }
            
            LoaiDichVuDTO loai = collectFormData();
            if (loai == null) return;
            loai.setMaLoaiDV(ma);
            
            service.updateLoaiDichVu(loai);
            com.wms.util.MessageUtil.showInfo(view, "Cập nhật loại dịch vụ thành công!");
            loadData();
        } catch (Exception e) {
            showError(e.getMessage(), e);
        }
    }

    public void clearForm() {
        view.getTxtMaLoai().setText("");
        view.getTxtTenLoai().setText("");
        view.getCbxTrangThai().setSelectedIndex(0);
        view.getTblLoaiDichVu().clearSelection();
    }

    private LoaiDichVuDTO collectFormData() {
        LoaiDichVuDTO loai = new LoaiDichVuDTO();
        loai.setTenLoaiDV(view.getTxtTenLoai().getText().trim());
        
        String uiTrangThai = view.getCbxTrangThai().getSelectedItem().toString();
        loai.setTrangThaiLDV("Hoạt động".equals(uiTrangThai) ? "Đang hoạt động" : "Ngừng kinh doanh");

        return loai;
    }

    private void showError(String msg) {
        com.wms.util.MessageUtil.showError(view, msg);
    }

    private void showError(String msg, Throwable t) {
        com.wms.util.MessageUtil.showError(view, msg, t);
    }
}
