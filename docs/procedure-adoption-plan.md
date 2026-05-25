# Procedure Adoption Plan

Ngày lập: 2026-05-25

Nguyên tắc: chỉ chuyển Java sang stored procedure khi input/output khớp, message map được, transaction boundary không đổi và có test chứng minh behavior giống 100%. Lượt này không chuyển candidate nào vì chưa đạt điều kiện transaction an toàn.

| Procedure | Java hiện tại đang làm gì | Procedure làm gì | Input có khớp không | Output có khớp không | Transaction có rủi ro không | Có nên chuyển không |
|----------|----------------------------|------------------|---------------------|----------------------|-----------------------------|--------------------|
| `SP_ThemLoaiDichVu` | `LoaiDichVuDAO.them` insert trực tiếp `LOAIDICHVU(TenLoaiDV, TrangThaiLDV)` và để DB sinh mã nếu có trigger/sequence | Validate trùng mã/tên, validate trạng thái, insert `LOAIDICHVU`, trả `p_outMessage` | Gần khớp, nhưng procedure có thêm `p_MaLoaiDV` và validation chặt hơn Java | Không khớp hoàn toàn vì Java hiện chỉ trả boolean/SQLException, UI đang dựa message riêng | Có rủi ro: procedure có `COMMIT/ROLLBACK` nội bộ | Không chuyển trong lượt này |
| `SP_CapNhatKhongGian` | `KhongGianDAO.capNhat` update trực tiếp nhiều field không gian theo DTO hiện tại | Validate tồn tại, validate trùng tên theo chi nhánh, validate loại, update không gian, trả `p_outMessage` | Gần khớp các field chính | Chưa chứng minh message/output giống 100% với UI hiện tại | Có rủi ro: procedure có `COMMIT/ROLLBACK` nội bộ | Không chuyển trong lượt này |

