# Nghiệm thu P2 — 11/11/2026

Kết luận: đạt phạm vi P2 sau khi sửa các điểm dưới đây. P2 cung cấp schema/model/dữ liệu demo; các quy tắc cần transaction nghiệp vụ tiếp tục thuộc các phase service.

## Các điểm đã sửa

| Vấn đề | Sửa đổi |
|---|---|
| Hướng dẫn seed/verifier phụ thuộc thư mục class đã biên dịch sẵn | Thêm goal `compile` trước `exec:java` trong README và tài liệu dữ liệu |
| Khoảng trắng Unicode có thể làm lọt đầu truyện trùng | Chuẩn hóa toàn bộ nhóm whitespace Unicode trước khi trim; thêm kiểm tra NBSP, em-space, dấu Unicode, phân biệt có dấu/không dấu và năm trống |
| Schema cho phép email NCC là chuỗi rỗng | V5 thêm CHECK cho email hiển thị và email chuẩn |
| Đơn giá và số lượng hợp lệ riêng lẻ nhưng thành tiền dòng có thể vượt giới hạn | V5 thêm CHECK `soLuong * donGiaNhap <= 999999999999` |
| Verifier chưa chứng minh CSDL từ chối các thao tác sai | Thêm 13 thao tác sai và kiểm tra đúng mã lỗi MySQL; rollback toàn bộ transaction kiểm tra |
| Tiến trình CLI để lại luồng dọn kết nối MySQL khi thoát | Seed/verifier gọi shutdown trong finally sau khi đóng connection |

## Bằng chứng nghiệm thu

- Flyway áp dụng V5 trên schema V4 hiện có và xác thực checksum các migration trước đó; giữ nguyên dữ liệu demo.
- Java 21 biên dịch 30 file nguồn thành công.
- Verifier xác nhận 18 bảng nghiệp vụ, mã, giá trị chuẩn hóa, số quyển đúng số lượng nhập, nguồn NCC, giá lịch sử, ngày và dữ liệu lỗi/cọc.
- 13 thao tác sai bị từ chối: mã trùng, điện thoại/email chuẩn trùng, sai đầu truyện so với nguồn nhập, số lượng mượn khác 1, số lượng nhập 0 hoặc 10001, giá thuê 0, điện thoại sai, email/email chuẩn rỗng, thành tiền dòng quá giới hạn và khóa tự nhiên đầu truyện trùng.
- Doanh thu mẫu tháng 01/2025: NCC 101 = 37.000 VND; NCC 102 = 30.000 VND. Ngày 03/01: 20.000 và 30.000 VND; ngày 01/02: NCC 102 = 15.000 VND. Khoảng ngày không có giao dịch trả kết quả rỗng.
- Chạy lại seed báo đã có marker và không sửa dữ liệu; verifier chạy sau các kiểm tra ghi sai vẫn khớp toàn bộ tổng demo.

## Những điều phải thực hiện ở phase nghiệp vụ

- Mọi bản ghi mới phải có mã đúng ID trước khi commit; hiện schema cho phép mã NULL để sinh ID trong transaction.
- Sinh giá trị chuẩn hóa và khóa tự nhiên qua cùng quy tắc Java; unique key chỉ bảo vệ đúng khi giá trị này được sinh đúng.
- Kiểm tra năm xuất bản theo năm hiện tại, định dạng email, thứ tự ngày và khách hàng của hóa đơn phải khớp giao dịch mượn.
- Lưu phiếu nhập, chi tiết, đủ số quyển và mã trong một transaction; giới hạn tổng phiếu/tổng báo cáo cần kiểm tra ở service.
- Chỉ hồ sơ nhân viên được đăng nhập; xử lý version khi sửa NCC và chuyển lỗi ràng buộc thành thông báo phù hợp ở giao diện.

Đây là các yêu cầu bàn giao cho phase sau, chưa được chứng minh bằng luồng website trong P2. Các kiểm tra lần này thực hiện trên schema cục bộ đang có; chưa chạy lại toàn bộ trên một volume MySQL mới độc lập.
