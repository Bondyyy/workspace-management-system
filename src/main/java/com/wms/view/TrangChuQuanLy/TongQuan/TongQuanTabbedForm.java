package com.wms.view.TrangChuQuanLy.TongQuan;

import javax.swing.*;
import java.awt.*;

public class TongQuanTabbedForm extends JPanel {

    public TongQuanTabbedForm() {
        setLayout(new BorderLayout());
        setBackground(new Color(254, 248, 250));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Tổng quan doanh thu", new TongQuanForm());
        tabbedPane.addTab("Báo cáo lương nhân viên", new BaoCaoLuongNhanVienForm());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
