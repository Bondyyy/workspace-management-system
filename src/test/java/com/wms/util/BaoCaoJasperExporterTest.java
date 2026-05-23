package com.wms.util;

import com.wms.model.TrangChuQuanLy.TongQuan.DongBaoCaoTongQuatDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaoCaoJasperExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void loadCompileVaExportPdfTuDuLieuTiengVietKhongCanOracle() throws Exception {
        assertNotNull(
                BaoCaoJasperExporter.class.getResourceAsStream(BaoCaoJasperExporter.TEMPLATE_PATH),
                "Template JasperReports phải tồn tại trong classpath."
        );
        assertNotNull(BaoCaoJasperExporter.compileTemplate());

        Path output = tempDir.resolve("bao-cao-wms.pdf");
        DuLieuBaoCaoTongQuatDTO duLieu = new DuLieuBaoCaoTongQuatDTO();
        duLieu.setLoaiBaoCao("Báo cáo doanh thu");
        duLieu.setTieuDeBaoCao("BÁO CÁO DOANH THU");
        duLieu.setPhuDeBaoCao("Dữ liệu kiểm thử tiếng Việt có dấu.");
        duLieu.setTuNgay("01/05/2026");
        duLieu.setDenNgay("21/05/2026");
        duLieu.setTenChiNhanh("Chi nhánh Trung tâm");
        duLieu.setNguoiXuat("Quản lý");
        duLieu.setThoiGianXuat("21/05/2026 10:00");
        duLieu.setNhanTongGiaTri1("Tổng doanh thu");
        duLieu.setTongGiaTri1("900,000 VNĐ");
        duLieu.setNhanTongGiaTri2("Trước giảm giá");
        duLieu.setTongGiaTri2("1,000,000 VNĐ");
        duLieu.setNhanTongGiaTri3("Tổng chiết khấu");
        duLieu.setTongGiaTri3("100,000 VNĐ");
        duLieu.setGhiChuBaoCao("Đã thanh toán thành công, Chuyển khoản, Khách vãng lai.");
        duLieu.setDanhSachTieuDeCot(List.of(
                "Mã HĐ", "Ngày lập", "Khách hàng", "Chi nhánh",
                "Không gian", "Tổng tiền", "Thành tiền", "Thanh toán"
        ));
        duLieu.setDanhSachDongBaoCao(List.of(new DongBaoCaoTongQuatDTO(
                "HD001",
                "21/05/2026 10:00",
                "Khách vãng lai",
                "Chi nhánh Trung tâm",
                "Phòng họp A",
                "1,000,000 VNĐ",
                "900,000 VNĐ",
                "Chuyển khoản / Đã thanh toán thành công"
        )));

        BaoCaoJasperExporter.xuatBaoCaoPdf(output.toFile(), duLieu);

        assertTrue(Files.exists(output), "File PDF JasperReports phải được tạo.");
        assertTrue(Files.size(output) > 0, "File PDF JasperReports không được rỗng.");
    }
}
