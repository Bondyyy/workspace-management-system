package com.wms.model.TrangChuQuanLy.TongQuan;

import java.util.List;
import java.util.Map;

public class TongQuanDTO {

    private double doanhThuThuc;
    private double truocGiam;
    private double chietKhau;

    // Doanh thu 7 ngày gần nhất theo thứ tự ngày cũ → mới
    private List<Double> doanhThu7Ngay;

    // Tỷ lệ thanh toán: key "CK" (chuyển khoản/Momo), "TM" (tiền mặt), giá trị là phần trăm
    private Map<String, Integer> coCauThanhToan;

    // Giao dịch gần nhất: mỗi phần tử gồm [MaHoaDon, TenKhach, SoTien, TrangThai]
    private List<Object[]> giaoDichGanNhat;

    public double getDoanhThuThuc() { return doanhThuThuc; }
    public void setDoanhThuThuc(double doanhThuThuc) { this.doanhThuThuc = doanhThuThuc; }

    public double getTruocGiam() { return truocGiam; }
    public void setTruocGiam(double truocGiam) { this.truocGiam = truocGiam; }

    public double getChietKhau() { return chietKhau; }
    public void setChietKhau(double chietKhau) { this.chietKhau = chietKhau; }

    public List<Double> getDoanhThu7Ngay() { return doanhThu7Ngay; }
    public void setDoanhThu7Ngay(List<Double> doanhThu7Ngay) { this.doanhThu7Ngay = doanhThu7Ngay; }

    public Map<String, Integer> getCoCauThanhToan() { return coCauThanhToan; }
    public void setCoCauThanhToan(Map<String, Integer> coCauThanhToan) { this.coCauThanhToan = coCauThanhToan; }

    public List<Object[]> getGiaoDichGanNhat() { return giaoDichGanNhat; }
    public void setGiaoDichGanNhat(List<Object[]> giaoDichGanNhat) { this.giaoDichGanNhat = giaoDichGanNhat; }
}
