package com.wms.util;

import com.wms.model.TrangChuQuanLy.TongQuan.DongBaoCaoTongQuatDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class BaoCaoCsvExporter {

    public static final String[] CSV_HEADERS_DOANH_THU = {
            "Mã hóa đơn",
            "Khách hàng",
            "Ngày lập",
            "Tổng tiền",
            "Thành tiền",
            "Phương thức",
            "Trạng thái"
    };

    private BaoCaoCsvExporter() {
    }

    public static void xuatCsv(File file, List<Object[]> rows) throws java.io.IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.write(toCsvLine((Object[]) CSV_HEADERS_DOANH_THU));
            writer.newLine();
            if (rows != null) {
                for (Object[] row : rows) {
                    writer.write(toCsvLine(row));
                    writer.newLine();
                }
            }
        }
    }

    public static void xuatCsv(File file, DuLieuBaoCaoTongQuatDTO duLieu) throws java.io.IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            if (duLieu == null) {
                return;
            }

            writer.write(toCsvLine(new Object[]{duLieu.getTieuDeBaoCao()}));
            writer.newLine();
            writer.write(toCsvLine(new Object[]{"Khoảng ngày", duLieu.getTuNgay() + " - " + duLieu.getDenNgay()}));
            writer.newLine();
            writer.write(toCsvLine(new Object[]{"Chi nhánh", duLieu.getTenChiNhanh()}));
            writer.newLine();
            writer.newLine();
            writer.write(toCsvLine(duLieu.getDanhSachTieuDeCot().toArray()));
            writer.newLine();

            for (DongBaoCaoTongQuatDTO row : duLieu.getDanhSachDongBaoCao()) {
                writer.write(toCsvLine(new Object[]{
                        row.getCot1(), row.getCot2(), row.getCot3(), row.getCot4(),
                        row.getCot5(), row.getCot6(), row.getCot7(), row.getCot8()
                }));
                writer.newLine();
            }
        }
    }

    static String toCsvLine(Object[] values) {
        StringBuilder line = new StringBuilder();
        if (values == null) {
            return "";
        }
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escapeCsv(values[i]));
        }
        return line.toString();
    }

    static String escapeCsv(Object value) {
        String text = value == null ? "" : value.toString();
        boolean mustQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        String escaped = text.replace("\"", "\"\"");
        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }
}
