package com.wms.view.TrangChuQuanLy.TongQuan;

import com.wms.service.TrangChuQuanLy.TongQuan.TongQuanService;

import javax.swing.*;
import java.awt.*;

public class TongQuanTabbedForm extends JPanel {

    public TongQuanTabbedForm() {
        setLayout(new BorderLayout());
        setBackground(new Color(254, 248, 250));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Doanh thu", new BaoCaoTongQuatPanel(
                TongQuanService.BAO_CAO_DOANH_THU,
                "Theo dõi doanh thu, chiết khấu và trạng thái thanh toán theo khoảng ngày.",
                true,
                false));
        tabbedPane.addTab("Nhập kho dịch vụ", new BaoCaoTongQuatPanel(
                TongQuanService.BAO_CAO_NHAP_KHO_DICH_VU,
                "Theo dõi số lượng dịch vụ nhập kho theo chi nhánh và thời gian.",
                false,
                false));
        tabbedPane.addTab("Chi phí nhập kho", new BaoCaoTongQuatPanel(
                TongQuanService.BAO_CAO_CHI_PHI_NHAP_KHO,
                "Tổng hợp chi phí nhập kho dịch vụ phục vụ vận hành.",
                false,
                false));
        tabbedPane.addTab("Dịch vụ bán chạy", new BaoCaoTongQuatPanel(
                TongQuanService.BAO_CAO_DICH_VU_BAN_CHAY,
                "Xếp hạng dịch vụ theo số lượng bán và doanh thu ghi nhận.",
                false,
                false));
        tabbedPane.addTab("Lợi nhuận gộp", new BaoCaoTongQuatPanel(
                TongQuanService.BAO_CAO_LOI_NHUAN_GOP_UOC_TINH,
                "Ước tính lợi nhuận gộp từ doanh thu và chi phí dịch vụ.",
                false,
                false));
        tabbedPane.addTab("Trả lương nhân viên", new BaoCaoTongQuatPanel(
                TongQuanService.BAO_CAO_TRA_LUONG_NHAN_VIEN,
                "Tính tiền lương theo ngày làm việc, phụ cấp và thưởng hiện có.",
                false,
                true));

        add(tabbedPane, BorderLayout.CENTER);
    }
}
