package com.wms.controller.TrangChuQuanLy.QuanLyDatChoTruoc;

import com.wms.model.TrangChuQuanLy.QuanLyDatChoTruoc.DatChoTruocDTO;
import com.wms.service.TrangChuQuanLy.QuanLyDatChoTruoc.QuanLyDatChoTruocService;

import java.util.List;

public class QuanLyDatChoTruocController {
    private final QuanLyDatChoTruocService service = new QuanLyDatChoTruocService();

    public List<DatChoTruocDTO> layDanhSach(String keyword) {
        return service.layDanhSach(keyword);
    }

    public boolean capNhat(DatChoTruocDTO dto) {
        return service.capNhat(dto);
    }
}
