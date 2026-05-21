package com.wms.service.TrangChuQuanLy.QuanLyDatChoTruoc;

import com.wms.dao.TrangChuQuanLy.QuanLyDatChoTruoc.QuanLyDatChoTruocDAO;
import com.wms.model.TrangChuQuanLy.QuanLyDatChoTruoc.DatChoTruocDTO;

import java.util.List;

public class QuanLyDatChoTruocService {
    private final QuanLyDatChoTruocDAO dao = new QuanLyDatChoTruocDAO();

    public List<DatChoTruocDTO> layDanhSach(String keyword) {
        return dao.layDanhSach(keyword);
    }

    public boolean capNhat(DatChoTruocDTO dto) {
        return dao.capNhat(dto);
    }
}
