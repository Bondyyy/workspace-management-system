package com.wms.util;

import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BaoCaoJasperExporter {

    public static final String TEMPLATE_PATH = "/reports/bao_cao_wms.jrxml";

    private BaoCaoJasperExporter() {
    }

    public static void xuatBaoCaoPdf(File file, DuLieuBaoCaoTongQuatDTO duLieu)
            throws IOException, JRException {
        if (file == null) {
            throw new IllegalArgumentException("File xuất báo cáo không được rỗng.");
        }
        if (duLieu == null) {
            throw new IllegalArgumentException("Dữ liệu báo cáo không được rỗng.");
        }

        JasperReport report = compileTemplate();
        JasperPrint print = JasperFillManager.fillReport(
                report,
                taoThamSoJasper(duLieu),
                new JRBeanCollectionDataSource(duLieu.getDanhSachDongBaoCao())
        );
        JasperExportManager.exportReportToPdfFile(print, file.getAbsolutePath());
    }

    static JasperReport compileTemplate() throws IOException, JRException {
        try (InputStream input = BaoCaoJasperExporter.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (input == null) {
                throw new IOException("Không tìm thấy template JasperReports: " + TEMPLATE_PATH);
            }
            return JasperCompileManager.compileReport(input);
        }
    }

    private static Map<String, Object> taoThamSoJasper(DuLieuBaoCaoTongQuatDTO duLieu) {
        Map<String, Object> map = new HashMap<>();
        map.put("TIEU_DE_BAO_CAO", giaTri(duLieu.getTieuDeBaoCao()));
        map.put("PHU_DE_BAO_CAO", giaTri(duLieu.getPhuDeBaoCao()));
        map.put("TU_NGAY", giaTri(duLieu.getTuNgay()));
        map.put("DEN_NGAY", giaTri(duLieu.getDenNgay()));
        map.put("CHI_NHANH", giaTri(duLieu.getTenChiNhanh()));
        map.put("NGUOI_XUAT", giaTri(duLieu.getNguoiXuat()));
        map.put("THOI_GIAN_XUAT", giaTri(duLieu.getThoiGianXuat()));
        map.put("NHAN_TONG_1", giaTri(duLieu.getNhanTongGiaTri1()));
        map.put("GIA_TRI_TONG_1", giaTri(duLieu.getTongGiaTri1()));
        map.put("NHAN_TONG_2", giaTri(duLieu.getNhanTongGiaTri2()));
        map.put("GIA_TRI_TONG_2", giaTri(duLieu.getTongGiaTri2()));
        map.put("NHAN_TONG_3", giaTri(duLieu.getNhanTongGiaTri3()));
        map.put("GIA_TRI_TONG_3", giaTri(duLieu.getTongGiaTri3()));
        map.put("GHI_CHU_BAO_CAO", giaTri(duLieu.getGhiChuBaoCao()));
        map.put("LOAI_BAO_CAO", giaTri(duLieu.getLoaiBaoCao()));

        List<String> tieuDeCot = duLieu.getDanhSachTieuDeCot();
        for (int i = 0; i < 8; i++) {
            String tenCot = i < tieuDeCot.size() ? tieuDeCot.get(i) : "";
            map.put("TEN_COT_" + (i + 1), giaTri(tenCot));
        }
        return map;
    }

    private static String giaTri(String value) {
        return value == null ? "" : value;
    }
}
