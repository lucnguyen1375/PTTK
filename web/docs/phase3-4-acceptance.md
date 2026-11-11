# Nghiệm thu Phase 3–4 — 11/11/2026

## P3 — Prototype giao diện

Prototype tự chứa trong web/prototype/index.html; mở trực tiếp bằng Chrome/Edge. Nó hiện nhãn dữ liệu minh họa và không kết nối CSDL. Các màn hình có luồng bấm:

- Đăng nhập và trang chính.
- Tìm NCC, thêm NCC, sửa NCC.
- Chọn NCC, lập phiếu tạm, tìm/chọn đầu truyện có sẵn, thêm đầu truyện mới, sửa/xóa dòng, xác nhận/hủy phiếu.
- Chọn ngày thống kê, xem doanh thu, hóa đơn theo NCC, chi tiết hóa đơn và quay lại.

Prototype hỗ trợ trạng thái tìm thấy/không tìm thấy, trường sai, trùng NCC/đầu truyện, giới hạn phiếu, lỗi báo cáo, đăng nhập sai/khóa tài khoản/hết phiên, xác nhận xóa/hủy/rời trang. Các hành động lưu NCC/phiếu chỉ đổi dữ liệu minh họa trong tab.

Thêm query ?view=login, ?view=supplier, ?view=supplier-add, ?view=supplier-edit, ?view=import-supplier, ?view=import-draft, ?view=title-add, ?view=title-new, ?view=title-existing, ?view=report hoặc ?view=invoice để mở màn hình tương ứng.

## P4 — Đăng nhập và phiên

Website thật dùng /login, /app/home và POST /logout. / chuyển người dùng chưa xác thực tới đăng nhập và chuyển nhân viên đã đăng nhập tới trang chính. Filter bảo vệ /app/*; không có giao diện đăng nhập NCC hoặc khách hàng.

Tài khoản được xác thực bằng PBKDF2WithHmacSHA256 theo định dạng hash hiện có (600.000 vòng, salt ngẫu nhiên 16 byte, khóa 256 bit). Truy vấn tài khoản dùng PreparedStatement và yêu cầu có hồ sơ nhân viên. Lỗi sai tài khoản, mật khẩu hoặc không có hồ sơ nhân viên dùng cùng thông báo chung. Mật khẩu không được đưa vào session hay log.

Phiên timeout 30 phút. Đăng nhập đổi session ID, chỉ giữ ID/mã/tên/chức vụ nhân viên và xoay CSRF token. Session chỉ đi bằng cookie HttpOnly, SameSite=Lax; Secure bật ngoài APP_ENV=development. GET chưa đăng nhập chuyển tới /login?expired=1; POST nghiệp vụ chưa đăng nhập trả HTTP 401 cùng đường dẫn đăng nhập, không chạy lại thao tác. POST dùng CSRF token; đăng xuất là POST và hủy session.

Giới hạn sai đăng nhập là 5 lần trong 15 phút theo username chuẩn hóa và IP kết nối trực tiếp. Lần thứ 6 bị HTTP 429 và có Retry-After. Bộ đếm ở bộ nhớ nên mất khi ứng dụng khởi động lại và chỉ dùng được cho một instance.

Trang chính hiện ba nhóm chức năng M3 ở trạng thái chưa khả dụng; các chức năng thật sẽ được xây ở P5–P7. Phiếu tạm không có nơi lưu trong P4; hết phiên/đăng xuất thì không được phục hồi.

## Bằng chứng kiểm tra

- Java 21 biên dịch 39 file; Docker build WAR thành công; ứng dụng sẵn sàng sau khi deploy, /health/ready trả HTTP 200.
- HTTP kiểm tra được chuyển hướng root, lấy CSRF, đăng nhập thành công bằng tài khoản nv_demo và mật khẩu cục bộ, hiện đúng nhân viên, logout, chặn URL sau logout, trả 401 cho POST khi phiên hết hạn, từ chối CSRF sai (403), và khóa lần thử sai thứ 6 (429).
- Session ID đổi sau đăng nhập. Cookie kiểm tra được với HttpOnly và SameSite=Lax; trong development HTTP, cookie không có Secure.
- Chrome headless tải và thực thi prototype ở các màn trang chính, thống kê, phiếu nhập, tìm đầu truyện; bố cục 390×844 cũng đã được render và xem ảnh. Các request HTTP nêu trên đạt kết quả mong đợi. Đây chưa phải kiểm tra tương tác tự động đầy đủ cho mọi nút trong prototype.

## Nghiệm thu lại code ngày 11/11/2026

Kết luận: **P3–P4 đạt trong phạm vi hiện tại sau khi sửa các lỗi prototype dưới đây.** Người dùng đã cho phép chuyển các giới hạn kiểm tra sang phase sau khi đủ tài nguyên. Các mục hoãn không được coi là đã kiểm tra đạt.

Đã đọc lại các lớp auth, Servlet/Filter, JSP và logic prototype. P4 không phát hiện lỗi chặn nghiệm thu trong phạm vi code đã rà và HTTP đã chạy. P3 có các lỗi đã tái hiện và sửa:

- Dòng thêm từ đầu truyện mới/có sẵn thiếu đối tượng `title`, gây lỗi render phiếu. Đã thống nhất cấu trúc dòng và kiểm tra gộp số lượng.
- Chi tiết hóa đơn chỉ tìm theo mã, có thể hiện tiền của NCC khác cùng hóa đơn. Đã lọc đồng thời theo NCC đang chọn.
- Hộp thoại có callback không điều hướng không đóng khỏi màn hình; mở/hủy hộp thoại còn dựng lại form làm mất nội dung chưa nhập xong. Đã sửa cách thêm/gỡ hộp thoại và cập nhật sau xác nhận.
- Rời phần thêm/tìm đầu truyện qua menu chưa xác nhận phiếu đang có dữ liệu. Đã dùng cùng quy tắc xác nhận cho các màn con.
- Tên/địa chỉ NCC và thông tin đầu truyện chỉ chứa khoảng trắng vẫn được nhận. Đã từ chối với thông báo hướng sửa; năm xuất bản phải là số nguyên.
- Lỗi sửa số lượng không có vùng hiển thị trên phiếu; lỗi tải báo cáo lại báo sai khoảng ngày dù ngày hợp lệ. Đã sửa hai thông báo.
- Thông báo thêm/sửa NCC có nhánh ở lại form sau khi đã lưu, dễ hiểu nhầm để gửi lại. Đã dùng hành động quay lại/OK đúng luồng đã chốt.
- Xóa hết từ khóa chọn NCC chưa luôn trả lại danh sách đầy đủ. Đã đồng bộ từ khóa từ input.

Bằng chứng chạy lại:

| Kiểm tra | Kết quả | Phạm vi |
|---|---|---|
| `node scripts/check-prototype.cjs` | 16/16 đạt | Chạy script prototype gốc trong Node VM với DOM stub; thêm/gộp dòng, chặn trùng, xác nhận rời/đổi NCC, đóng hộp thoại, kiểm tra form, NCC, báo cáo và giữ bộ lọc. |
| `node scripts/check-auth.cjs` | 20/20 đạt | HTTP tới ứng dụng thật: readiness, URL được bảo vệ, CSRF, dữ liệu thiếu, 5 lần sai và lần 6 khóa, cookie, đăng nhập, đổi session ID/token, nhân viên từ session, logout và chặn session cũ. |
| Chrome headless | Đạt | Script hiện tại tải và render phiếu nhập có dòng mẫu. |
| `git diff --check` | Đạt | Không có lỗi khoảng trắng; Git chỉ cảnh báo chuyển LF/CRLF. |

Hai lệnh Node chạy từ thư mục `web`, cần Node.js. Kiểm tra HTTP cần ứng dụng/MySQL đang chạy, seed `a` và `DEMO_PASSWORD` cục bộ trong `.env`; script không in mật khẩu và không sửa dữ liệu nghiệp vụ. Kiểm tra Node VM không thay thế nghiệm thu tương tác đầy đủ trên trình duyệt.

## Các mục chuyển sang phase sau

| Mục chưa kiểm tra/giới hạn | Phase tiếp nhận | Điều kiện nghiệm thu |
|---|---|---|
| Toàn bộ thao tác UI trên trình duyệt, bàn phím và thiết bị mục tiêu | P5–P7 từng module; P8 tổng hợp | Kiểm tra trên giao diện thật khi module tương ứng hoàn thành và có công cụ thao tác trình duyệt. |
| Sửa mã nhân viên trong request lập phiếu | P6 | Gửi mã khác nhưng người lập phiếu lưu trong CSDL vẫn là nhân viên lấy từ session. P4 hiện đã kiểm tra tham số `employeeId=999` không thay danh tính trang chính. |
| Chờ session thật đủ 30 phút | P8 | Hết thời gian không hoạt động thì GET/POST được bảo vệ không thực hiện nghiệp vụ; thông báo đúng, không tự gửi lại thao tác. |
| CSDL ngắt trong lúc đăng nhập | P8, môi trường kiểm thử riêng | HTTP 503/thông báo thử lại, có mã yêu cầu, không lộ dữ liệu nhạy cảm và không tạo session xác thực. |
| NCC/khách hàng có mật khẩu đúng vẫn không được đăng nhập | P8 | Chuẩn bị tài khoản kiểm thử có hash hợp lệ nhưng không có hồ sơ nhân viên; nhận cùng thông báo sai thông tin. |
| HTTPS, cookie Secure và cấu hình proxy triển khai | P9 | Kiểm tra trên môi trường triển khai thực tế. |
| Bộ đếm đăng nhập chỉ ở bộ nhớ, mất khi restart | P9 đánh giá cấu hình vận hành | Phạm vi hiện tại chạy một instance. Nếu triển khai nhiều instance hoặc cần giữ bộ đếm qua restart, bổ sung cơ chế dùng chung rồi nghiệm thu; không mặc định mở rộng phạm vi ngay. |

## Cập nhật tài khoản theo yêu cầu người dùng

Ngày 11/11/2026: đổi tài khoản nhân viên Nguyễn Văn A (NV000001) từ `nv_demo` thành `a`, đặt mật khẩu theo yêu cầu trong `.env`. Giữ nguyên ID nhân viên và các liên kết dữ liệu. Seed cho môi trường mới và script kiểm tra đã chuyển sang username `a`; kết quả kiểm tra trước cập nhật vẫn giữ tên tài khoản tại thời điểm chạy.
