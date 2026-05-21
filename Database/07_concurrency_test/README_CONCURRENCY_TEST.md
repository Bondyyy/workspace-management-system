# Kiểm thử xử lý đồng thời

Thư mục này dùng để demo trước giảng viên rằng các luồng nghiệp vụ chính có transaction và row lock.

## Cách chạy

1. Mở 2 worksheet/session Oracle SQL Developer khác nhau.
2. Chọn cùng schema WMS.
3. Chạy script `session_1` trước và giữ nguyên worksheet đó, chưa `COMMIT` hoặc `ROLLBACK`.
4. Chạy script `session_2` tương ứng ở worksheet thứ hai.
5. Kết quả mong đợi: session 2 nhận thông báo dữ liệu đang được nhân viên khác thao tác, hoặc nghiệp vụ bị từ chối vì trạng thái đã thay đổi.

## Dữ liệu mẫu cần chuẩn bị

- Có ít nhất một không gian đang `Trống`, ví dụ `KG000001`.
- Có ít nhất một dịch vụ đang tồn tại, ví dụ `DV000001`.
- Có ít nhất một phiên đang `Đang hoạt động` và có hóa đơn `Đang chờ thanh toán`, ví dụ `PLV000001`.
- Nếu kiểm thử phiếu giảm giá, dùng mã còn hiệu lực và có `SLDaDung < SLToiDa`, ví dụ `PGG000001`.

Nếu dữ liệu thật đang dùng mã cũ như `KG001`, script vẫn chạy được sau khi thay biến đầu file.

## Kết quả mong đợi

- Mở phiên cùng không gian: chỉ một session được mở phiên; session còn lại bị chặn hoặc nhận trạng thái không khả dụng.
- Nhập kho cùng dịch vụ: không mất cập nhật số lượng; session tranh chấp nhận lỗi lock thân thiện.
- Thanh toán cùng phiên: không thanh toán hai lần một phiên; phiếu giảm giá không vượt `SLToiDa`.
