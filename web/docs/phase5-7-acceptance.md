# Biên bản triển khai và nghiệm thu P5–P7

Ngày: 11/11/2026  
Phạm vi: tìm/thêm/sửa NCC, đầu truyện và phiếu nhập, thống kê NCC theo hóa đơn trả.

## Kết quả

| Hạng mục | Kết quả |
|---|---|
| Build Maven WAR trong Docker | Thành công; biên dịch 55 tệp Java |
| P5–P6 HTTP | 26/26 kiểm tra đạt |
| Giao dịch NCC và phiếu nhập | 12/12 kiểm tra đạt; dữ liệu kiểm tra đã dọn |
| Rollback phiếu nhập khi lỗi giữa transaction | 7/7 kiểm tra đạt; trigger tạm đã gỡ |
| P7 HTTP | 10/10 kiểm tra đạt |
| Xác minh lại schema và dữ liệu P2 | Đạt; 18 bảng nghiệp vụ và các tổng doanh thu mẫu đúng |

Các lệnh kiểm tra:

```powershell
docker compose up --build -d app
node scripts/check-phase5-6.cjs
node scripts/check-business-commits.cjs
node scripts/check-import-rollback.cjs
node scripts/check-phase7.cjs
docker compose --profile tools run --rm db-tools -Pdb-tools compile exec:java@verify-schema -q
```

## P5 — Nhà cung cấp

- Đã có tìm kiếm, phân trang, trạng thái rỗng, thêm và sửa NCC; các truy vấn dùng tham số.
- Điện thoại/email trùng hoặc dữ liệu không hợp lệ bị từ chối ở máy chủ. Lỗi form giữ lại nội dung và CSRF.
- Sửa NCC dùng mã chỉ đọc, token đối tượng và phiên bản để chặn thay ID/xung đột ghi.
- Thêm thành công quay về tìm NCC; sửa thành công quay về trang chính và hiện thông báo.
- Kiểm tra ghi thật tạo một NCC và tài khoản thành viên liên kết; sửa được lưu và hiện thông báo. Mẫu kiểm tra đã được xóa sau lượt chạy.
- Thêm/sửa CSDL thất bại giữ form và mã yêu cầu; tình huống CSDL ngừng hoạt động chưa được tiêm lỗi, chuyển sang P8.

## P6 — Đầu truyện và nhập truyện

- Draft gắn với nhân viên/NCC và nằm riêng theo UUID; chỉnh sửa dùng phiên bản để từ chối request cũ. Draft không trộn giữa các tab.
- Thêm đầu truyện mới chỉ ghi trong draft; đầu truyện và quyển chỉ được tạo khi xác nhận lưu.
- Giao dịch kiểm tra đã lưu một phiếu có hai dòng, số lượng 2 và 3, năm quyển vật lý và tổng `8.000 VND`. CSDL xác nhận NCC/nhân viên đúng session dù request gửi giá trị giả.
- Gửi lại cùng request key trả đúng phiếu đã tồn tại. NCC, đầu truyện, phiếu, chi tiết và quyển kiểm tra được dọn sau khi chạy; kiểm tra schema P2 sau đó vẫn đạt.
- Đã kiểm tra phiếu rỗng, số lượng không dương, giá VND lẻ, phiên bản cũ, hủy draft và đổi NCC có xác nhận.
- Trigger lỗi chỉ dùng trong CSDL phát triển đã làm lỗi ở bước tạo quyển. Sau request, không còn phiếu, chi tiết, quyển hoặc đầu truyện mới; draft vẫn còn để thử lại. Trigger đã được gỡ và verifier P2 chạy đạt.
- **Còn ở P8:** kiểm tra timeout và rời trang qua trình duyệt thật.

## P7 — Báo cáo và chi tiết hóa đơn trả

- Bộ lọc dùng ngày lập `tblHoaDonTra.ngayLap`, hai biên được tính. Ngày trống, sai định dạng và khoảng ngày đảo ngược có lỗi rõ ràng.
- Doanh thu đi theo nguồn của từng quyển: chi tiết trả → chi tiết mượn → quyển → dòng nhập → phiếu nhập → NCC. Truy vấn không nối bảng lỗi/cọc.
- Công thức đang dùng theo quy tắc trong [mô hình dữ liệu P2](data-model.md): `đơn giá thuê chụp lúc mượn × max(1, số ngày lịch giữa ngày mượn và ngày trả)`; không cộng tiền phạt hoặc tài sản cọc.
- Dữ liệu mẫu tháng 01/2025 cho NCC 101 là `37.000 VND`, NCC 102 là `30.000 VND`; ngày 03/01 lần lượt `20.000` và `30.000`; ngày 01/02 NCC 102 là `15.000`.
- Chi tiết chỉ tính phần quyển của NCC đang xem. Hóa đơn `HDT000001` chứa nhiều NCC nhưng không bị cộng toàn bộ cho mỗi NCC.
- Báo cáo loại NCC doanh thu 0, sắp xếp giảm dần, có trạng thái rỗng riêng. Token snapshot trong session giữ nguyên khoảng lọc/kết quả khi quay lại.
- Trang lỗi tải báo cáo đã có riêng; chưa ngắt CSDL để kiểm tra trực tiếp trạng thái này, chuyển sang P8.

## Việc chuyển sang P8

- Mô phỏng lỗi CSDL giữa transaction nhập và xác minh không có dữ liệu dở dang.
- Kiểm tra timeout 30 phút, đăng xuất và hành vi rời trang trên trình duyệt thực.
- Ngắt CSDL để kiểm tra lỗi lưu NCC và lỗi tải báo cáo; xác nhận không báo thành công giả, form nhập còn dữ liệu và báo cáo lỗi khác trạng thái rỗng.
- Kiểm tra giao diện/kích thước màn hình, bàn phím và các luồng trên trình duyệt thực.
- Chạy ma trận kiểm thử toàn luồng, kiểm tra quyền và đo thời gian phản hồi theo quy mô dữ liệu đã thống nhất.
