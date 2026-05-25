package com.wms.service.TrangChuQuanLy.QuanLyDatChoTruoc;

import com.wms.dao.TrangChuQuanLy.QuanLyDatChoTruoc.QuanLyDatChoTruocDAO;
import com.wms.model.TrangChuQuanLy.QuanLyDatChoTruoc.DatChoTruocDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.KetQuaNhanChoDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.EmailUtil;
import com.wms.util.MaQRUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class QuanLyDatChoTruocService {
    private final QuanLyDatChoTruocDAO dao = new QuanLyDatChoTruocDAO();

    public List<DatChoTruocDTO> layDanhSach(String keyword) {
        return dao.layDanhSach(keyword);
    }

    public boolean capNhatThongTinKhach(DatChoTruocDTO dto) {
        return dao.capNhatThongTinKhach(dto);
    }

    public ThongTinXacNhanDatChoDTO xacNhanThanhToanThuCong(String maDatCho) {
        ThongTinXacNhanDatChoDTO thongTin = dao.xacNhanThanhToanThuCong(maDatCho);
        if (thongTin == null) {
            return null;
        }

        byte[] qrPng = MaQRUtil.taoAnhPng(thongTin.getMaQR());
        boolean daGui = EmailUtil.guiEmailXacNhanDatChoDaThanhToan(
                thongTin.getEmail(),
                thongTin.getHoTen(),
                null,
                thongTin.getMaDatCho(),
                thongTin.getTenKhongGian(),
                thongTin.getTenChiNhanh(),
                dinhDangKhoangThoiGian(thongTin),
                dinhDangTien(thongTin.getThanhTien()),
                thongTin.getMaQR(),
                qrPng
        );
        if (!daGui) {
            System.err.println("[QuanLyDatChoTruocService] Đã xác nhận thanh toán thủ công nhưng chưa gửi được email QR cho "
                    + thongTin.getEmail());
        }
        return thongTin;
    }

    public KetQuaNhanChoDTO moPhienTuDatChoThuCong(DatChoTruocDTO dto) {
        return dao.moPhienTuDatChoThuCong(dto);
    }

    private String dinhDangKhoangThoiGian(ThongTinXacNhanDatChoDTO thongTin) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (thongTin.getThoiGianBatDau() == null) {
            return "Chưa có";
        }
        String start = formatter.format(thongTin.getThoiGianBatDau());
        if (thongTin.getThoiGianDuKienKetThuc() == null) {
            return start;
        }
        return start + " - " + formatter.format(thongTin.getThoiGianDuKienKetThuc());
    }

    private String dinhDangTien(BigDecimal value) {
        if (value == null) {
            return "0 VNĐ";
        }
        return com.wms.util.InputFormatUtil.formatThousands(value) + " VNĐ";
    }
}
