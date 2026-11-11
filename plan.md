# Kế hoạch xây dựng website quản lí cửa hàng cho thuê truyện — Module M3

Ngày lập: 11/11/2026.

## 1. Căn cứ và cách sử dụng kế hoạch

Nguồn nghiệp vụ: `B23DCCN516 - Nguyen Van Minh Luc - Bao cao tong hop.pdf` (72 trang) và các điều chỉnh đã được người dùng xác nhận trong cuộc trao đổi.

Kế hoạch này chia công việc thành các phase có đầu ra và tiêu chí nghiệm thu. Đây là kế hoạch triển khai, không phải khẳng định mọi đề xuất kỹ thuật dưới đây đều đã có trong PDF.

Phân loại yêu cầu:

- **Đã chốt:** có trong tài liệu và không mâu thuẫn với điều chỉnh của người dùng, hoặc được người dùng xác nhận trực tiếp.
- **Cần chốt:** tài liệu thiếu hoặc chưa thống nhất; phải ghi quyết định trước khi triển khai phần phụ thuộc.
- **Đề xuất triển khai:** biện pháp để bảo đảm tính đúng đắn, khả năng sử dụng và vận hành của website.

Một phase chỉ được đánh dấu hoàn thành khi có bằng chứng đáp ứng tiêu chí nghiệm thu. Có thể làm sớm công việc độc lập của phase sau, nhưng không được coi giả định chưa chốt là yêu cầu chính thức.

## 2. Phạm vi đã xác nhận

### 2.1. Chức năng chính

1. Nhân viên đăng nhập, xem trang chính và đăng xuất.
2. Tìm kiếm và sửa thông tin nhà cung cấp (NCC).
3. Nhập truyện từ NCC, bao gồm thêm NCC và đầu truyện khi cần.
4. Thống kê NCC theo doanh thu; xem các hóa đơn trả liên quan trong khoảng thời gian được chọn.

### 2.2. Các điều chỉnh được ưu tiên áp dụng

- Tất cả hành động trên website do nhân viên thực hiện; chưa làm giao diện NCC tự đăng nhập hoặc tự sửa thông tin.
- Chi tiết thống kê dùng mô tả cập nhật về **hóa đơn trả**.
- Sau khi thêm NCC: thông báo thành công; khi chọn quay lại thì về màn hình tìm NCC theo phần activity trang 61.
- Sau khi sửa NCC: thông báo thành công; nhấn OK thì về trang chính nhân viên theo kịch bản tuần tự trang 66.
- Quay lại từ chi tiết thống kê phải giữ nguyên bộ lọc và kết quả thống kê trước đó.
- Thêm dòng nhập chỉ thay đổi phiếu tạm. Khi xác nhận lưu mới lưu phiếu, chi tiết và tạo các quyển truyện vật lý.
- Đầu truyện không gắn trực tiếp với một NCC. Nguồn cung của quyển truyện được truy theo: quyển truyện → chi tiết phiếu nhập → phiếu nhập → NCC.

### 2.3. Chưa đưa vào phạm vi mặc định

- Xây dựng đầy đủ giao diện mượn/trả, quản lí khách hàng, cọc và tiền phạt.
- Xóa NCC chỉ vì kịch bản ban đầu có nút Xóa.
- Quản trị tài khoản, phân quyền nhiều cấp, thanh toán trực tuyến, xuất Excel/PDF hoặc các báo cáo khác.

Các mục này chỉ được bổ sung khi có quyết định mở rộng phạm vi. Dữ liệu mượn/trả vẫn cần cho thống kê M3, thông qua tích hợp với module của nhóm hoặc dữ liệu mẫu hợp lệ.

## 3. Tổng quan các phase

| Phase | Mục tiêu | Phụ thuộc chính |
|---|---|---|
| P0 | Chốt nghiệp vụ và phạm vi | PDF và điều chỉnh của người dùng |
| P1 | Khảo sát project hiện có, chốt kiến trúc và môi trường | P0 cho các quyết định phạm vi |
| P2 | Hoàn thiện mô hình dữ liệu và dữ liệu mẫu | P0, P1 |
| P3 | Chốt giao diện, điều hướng và thông báo | P0; phối hợp P1–P2 |
| P4 | Xây dựng nền tảng đăng nhập và phiên làm việc | P1, P2, P3 |
| P5 | Hoàn thiện tìm kiếm, thêm và sửa NCC | P2–P4 |
| P6 | Hoàn thiện đầu truyện và nhập truyện | P2–P5 |
| P7 | Hoàn thiện thống kê doanh thu và hóa đơn trả | P2–P4, hợp đồng dữ liệu mượn/trả |
| P8 | Kiểm thử tích hợp, xử lý lỗi và đối chiếu dữ liệu | P5–P7 |
| P9 | Triển khai, nghiệm thu và bàn giao | P8 |

## 4. P0 — Chốt nghiệp vụ và phạm vi

### Công việc

- Lập danh sách yêu cầu theo từng chức năng, đối chiếu các phần khác nhau của PDF.
- Ghi rõ những điều chỉnh đã xác nhận ở mục 2; dùng chúng làm căn cứ khi tài liệu cũ khác với thiết kế sau.
- Chốt các quyết định sau:

| Mã | Quyết định cần chốt | Ảnh hưởng |
|---|---|---|
| Q01 | M3 chạy độc lập với dữ liệu mẫu hay tích hợp module mượn/trả? | CSDL, dữ liệu đầu vào và nghiệm thu thống kê |
| Q02 | Tiền thuê lấy từ số tiền giao dịch đã lưu hay tính lại? Nếu tính lại: ngày thuê, làm tròn, thời điểm áp dụng đơn giá ra sao? | Công thức doanh thu |
| Q03 | Doanh thu có gồm tiền phạt hoặc khoản khác không? | Tổng hợp hóa đơn |
| Q04 | Lọc theo ngày lập hóa đơn trả hay ngày trả của chi tiết? Có tính cả hai ngày biên không? | Điều kiện truy vấn |
| Q05 | NCC doanh thu bằng 0 có xuất hiện trong bảng không? | Kết quả báo cáo |
| Q06 | Thêm trùng đầu truyện trong một phiếu: gộp hay tách dòng? Nếu đơn giá/giá thuê khác nhau thì xử lý thế nào? | Phiếu tạm và chi tiết nhập |
| Q07 | Những trường nào bắt buộc, độ dài tối đa, định dạng điện thoại/email, giới hạn số lượng và giá? Giá bằng 0 có hợp lệ không? | Kiểm tra dữ liệu |
| Q08 | Thế nào là trùng NCC/đầu truyện; cảnh báo hay từ chối? | Thêm dữ liệu |
| Q09 | Có cho sửa/xóa dòng khỏi phiếu tạm không? Hủy/rời phiếu và nhiều tab cùng lập phiếu xử lý thế nào? | Vòng đời phiếu tạm |
| Q10 | Phiếu tạm có cần phục hồi sau hết phiên, đóng trình duyệt hoặc khởi động lại ứng dụng không? | Lưu trữ phiếu tạm |
| Q11 | Cách sinh mã NCC, đầu truyện, phiếu nhập và quyển truyện; quan hệ giữa mã nghiệp vụ với id? | Định danh dữ liệu |
| Q12 | Java/JSP/DAO có bắt buộc theo yêu cầu môn học không; CSDL và môi trường chạy nào được sử dụng? | Kiến trúc và triển khai |
| Q13 | Trường thể loại, nhà xuất bản, năm xuất bản xuất hiện chưa đồng nhất: bộ trường cuối cùng là gì? | Form, lớp thực thể, CSDL |
| Q14 | Nếu dữ liệu vừa bị nhân viên khác thay đổi: cảnh báo xung đột hay cho ghi đè? | Sửa NCC và tính nhất quán |

- Xác định cách xử lý dữ liệu lịch sử khi giá thuê hoặc thông tin NCC thay đổi; không để thay đổi giá hiện tại tự làm sai doanh thu đã phát sinh.
- Lập ví dụ tính tay cho doanh thu một hóa đơn chứa truyện của hai NCC.
- Ghi người xác nhận, ngày và nội dung quyết định cho từng mục.

### Đầu ra

- Đặc tả nghiệp vụ M3 đã thống nhất.
- Bảng quyết định Q01–Q14 và các quy tắc kiểm tra dữ liệu.
- Danh sách màn hình, luồng chuẩn, ngoại lệ và các mục ngoài phạm vi.

### Nghiệm thu

- [ ] Không còn quyết định chưa chốt đối với chức năng chuẩn bị triển khai.
- [ ] Công thức doanh thu và ngày dùng để lọc được mô tả bằng ví dụ có kết quả cụ thể.
- [ ] Phạm vi M3 và trách nhiệm cung cấp dữ liệu mượn/trả được xác định rõ.
- [ ] Không tự bổ sung quyền NCC, chức năng xóa hoặc module ngoài phạm vi.

## 5. P1 — Khảo sát project, kiến trúc và môi trường

**Trạng thái: Hoàn thành ngày 11/11/2026.** Khung Maven WAR đã build và chạy bằng Docker Compose; kết quả nghiệm thu chi tiết nằm trong phần này.

### Công việc

- Khảo sát thư mục `web` và cấu hình hiện có trước khi tạo hoặc thay đổi cấu trúc; xác định phần có thể sử dụng lại.
- Kiểm tra hướng dẫn repository nếu có; ghi nhận phiên bản runtime, công cụ build, máy chủ ứng dụng và CSDL.
- Chốt cấu trúc Servlet (controller) → service → DAO/JDBC → model; JSP chỉ hiển thị, không chứa SQL. Các package nghiệp vụ sẽ được bổ sung ở phase sau.
- Chuyển vai trò action JSP trong báo cáo sang Servlet; ghi rõ đây là lựa chọn kiến trúc của dự án.
- Thiết lập cấu hình phát triển bằng Docker Compose, kết nối CSDL và tài khoản ứng dụng riêng. P1 tạo CSDL rỗng; chưa tạo bảng nghiệp vụ hoặc tài khoản nhân viên demo.
- Thiết lập cách xử lý lỗi chung, ghi log và quản lí cấu hình bí mật.
- Cố định phiên bản và digest các image nền; chỉ mở cổng web trên localhost, không publish cổng MySQL.
- Chỉ bật health endpoint trong cấu hình `APP_ENV=development`; cấu hình thiếu hoặc sai vẫn cho liveness hoạt động nhưng readiness trả 503.

### Đầu ra

- Sơ đồ cấu trúc project, danh sách công nghệ và hướng dẫn chạy môi trường phát triển.
- Project khởi động được với cấu hình `.env`; có trang gốc, liveness và readiness phù hợp.

### Nghiệm thu

- [x] Maven WAR build thành công; Docker build và Compose khởi động Tomcat/MySQL thành công.
- [x] `/` trả 200 và render tiếng Việt; `/health/live` trả 200; `/health/ready` trả 200 khi MySQL hoạt động, 503 khi cấu hình thiếu hoặc CSDL ngừng; readiness phục hồi sau khi CSDL khởi động lại.
- [x] Trang 404 và 500 tùy chỉnh đã được gọi thực tế; lỗi 500 không làm lộ nội dung exception và trả mã request.
- [x] `.env` được Git bỏ qua; cấu hình sai chỉ nêu tên khóa thiếu trong log; không publish cổng MySQL; không ghi đè hoặc loại bỏ project có sẵn.

### Kết quả kỹ thuật

- Java 21, Maven 3.9.16, Servlet/JSP Jakarta trên Tomcat 10.1.61, MySQL 8.4.13 và MySQL Connector/J 9.5.0.
- Dockerfile build WAR trong image Maven; Docker Compose chạy ứng dụng ở `http://localhost:8080` và CSDL trong named volume `mysql-data`.
- Kiểm tra cơ sở dữ liệu dùng `SELECT 1`, thời gian chờ kết nối/truy vấn hữu hạn; endpoint health chỉ bật trong development.
- Đã tạo `.env` cục bộ với hai mật khẩu ngẫu nhiên riêng cho tài khoản ứng dụng và root MySQL; file này không nằm trong Git hoặc Docker build context.
- Kiểm tra build Maven và Docker đều thành công. Đã kiểm tra liveness/readiness, trạng thái khi thiếu cấu hình, 404/500 tùy chỉnh, MySQL outage/recovery và Compose configuration.
- Hai container đang chạy để tiếp tục phát triển; MySQL mới có database rỗng, chưa có schema nghiệp vụ.

## 6. P2 — Mô hình dữ liệu và dữ liệu mẫu

**Trạng thái: Hoàn thành ngày 11/11/2026.** Lược đồ, migration, model Java, seed demo, verifier và kết quả doanh thu được ghi tại [`web/docs/data-model.md`](web/docs/data-model.md).

### Công việc

- Hoàn thiện mô hình thực thể và lược đồ CSDL theo quyết định P0; thống nhất tên trường và kiểu dữ liệu.
- Thiết kế nhóm dữ liệu trực tiếp của M3: thành viên/nhân viên, NCC, đầu truyện, phiếu nhập, chi tiết phiếu nhập và quyển truyện.
- Xác định các bảng/quan hệ mượn, trả tối thiểu cần sử dụng; thống nhất hợp đồng dữ liệu với module liên quan.
- Bảo đảm mỗi quyển truyện truy được đúng chi tiết nhập và NCC cung cấp.
- Xác định dữ liệu giá tại thời điểm giao dịch để tính đúng doanh thu lịch sử.
- Dùng kiểu số thập phân chính xác cho tiền; thống nhất độ chính xác và cách làm tròn theo P0.
- Bổ sung khóa chính, khóa ngoại, ràng buộc hợp lệ và chỉ mục cần thiết cho tìm kiếm/thống kê.
- Chuẩn bị cách tạo/nâng cấp CSDL có thể chạy lại trên môi trường mới.
- Tạo dữ liệu mẫu: nhiều NCC; cùng đầu truyện nhập từ hai NCC; phiếu nhiều dòng; quyển truyện được thuê nhiều lần; hóa đơn trả có nhiều NCC; dữ liệu ở biên ngày; NCC không phát sinh doanh thu.
- Không tạo bảng thống kê lưu dư kết quả dẫn xuất theo thiết kế báo cáo.

### Đầu ra

- Lược đồ dữ liệu, từ điển dữ liệu, bộ khởi tạo/nâng cấp CSDL và dữ liệu mẫu.
- Bảng kết quả doanh thu kỳ vọng tính tay cho dữ liệu mẫu.

### Nghiệm thu

- [x] Sáu migration Flyway hiện chạy thành công trên CSDL MySQL 8.4; seed demo nạp thành công, chạy lại không ghi thêm dữ liệu.
- [x] 18 bảng nghiệp vụ có khóa ngoại/khóa duy nhất theo mô hình; verifier xác nhận mã demo khớp ID và các đường tham chiếu chính không mồ côi.
- [x] Cùng đầu truyện được nhập từ NCC 101 và 102; mỗi quyển vật lý truy đúng dòng nhập và NCC nguồn.
- [x] Có giá chụp lúc mượn, ngày mượn/ngày trả và hóa đơn trả để phân bổ tiền thuê cho từng NCC.
- [x] Ghi dữ liệu demo và kết quả tính tay cho tháng 01/2025, riêng ngày 03/01/2025 và 01/02/2025; verifier đối chiếu các tổng này.

### Kết quả thực hiện

- Nghiệm thu lại và sửa lỗi: xem [`web/docs/phase2-review.md`](web/docs/phase2-review.md). V5 bổ sung email bắt buộc và giới hạn thành tiền dòng nhập; verifier kiểm tra 13 thao tác sai bị từ chối và rollback.
- Sáu migration hiện có trong `web/src/main/resources/db/migration` tạo 18 bảng nghiệp vụ; không tạo bảng lưu tổng thống kê. `app_seed_history` dùng riêng để chống nạp seed lặp.
- Có 18 model ứng với bảng nghiệp vụ, cùng DTO `ThongKeNCC`, chuẩn hóa dữ liệu, mã nghiệp vụ và PBKDF2-HMAC-SHA256 cho mật khẩu seed.
- `SeedMain` tạo ba NCC, hai khách hàng, hai đầu truyện, bốn quyển vật lý, các lần mượn/trả, một hóa đơn có nhiều NCC, nhiều lỗi trên một lượt trả và một tài sản cọc được trả.
- `VerifyMain` xác nhận schema, mã, chuẩn hóa, nguồn quyển, ngày, giá lịch sử và doanh thu theo các khoảng ngày kỳ vọng.
- Build/compile Java thành công; migration, seed, verifier đều chạy thành công. Chạy seed lần hai báo đã có và không thay đổi dữ liệu.
- Quy tắc cần kiểm tra giữa nhiều bảng (thứ tự ngày trả và ngày lập hóa đơn) cùng giới hạn tổng tiền cần được áp đặt ở service trước khi ghi; chưa thuộc P2 vì DAO/service chưa triển khai.

## 7. P3 — Giao diện, điều hướng và thông báo

### Công việc

- Thiết kế các màn hình: đăng nhập, trang chính, tìm/sửa NCC, tìm NCC để nhập, thêm NCC, lập phiếu, tìm/thêm đầu truyện, chọn báo cáo, thống kê và hóa đơn trả của NCC.
- Xác định các trường nhập, trường chỉ đọc, nút hành động và thông tin ngữ cảnh trên từng màn hình.
- Thiết kế trạng thái ban đầu, chưa tìm kiếm, không có kết quả, đang xử lý, dữ liệu không hợp lệ, lỗi hệ thống và thành công.
- Thống nhất định dạng tiền, ngày tháng, nhãn trường và tên thao tác.
- Lập bảng điều hướng, đặc biệt các điểm quay lại sau thêm/sửa NCC và khi đang lập phiếu.
- Thiết kế thông báo tại trường sai; thông báo chung phải có hành động tiếp theo phù hợp.
- Chốt các kích thước màn hình mục tiêu; bảo đảm form và bảng sử dụng được trên các kích thước đó.

### Đầu ra

- Bản mô tả hoặc bản dựng giao diện, bảng điều hướng và danh mục thông báo.

### Nghiệm thu

- [x] Các luồng chuẩn và ngoại lệ đã có màn hình/trạng thái trong prototype tương tác.
- [x] Các nút đã được nối tới hành vi mô phỏng theo phạm vi P3; không đưa nút Xóa NCC vào mặc định.
- [x] Mã chỉ đọc, dữ liệu đang chọn và dữ liệu đang lưu được thể hiện rõ.
- [x] Form có thông báo lỗi bằng chữ và hướng sửa; trạng thái tải báo cáo lỗi có thể thử lại.
- [x] Luồng quay lại/đổi NCC/hủy phiếu có xác nhận và áp dụng quy tắc đã chốt.

Kết quả nghiệm thu lại: P3 đạt ở mức prototype tự chứa, dữ liệu minh họa. Đã sửa lỗi cấu trúc dòng nhập, chi tiết hóa đơn theo NCC, hộp thoại, điều hướng và kiểm tra form; 16/16 kiểm tra hồi quy logic đạt, Chrome headless render đạt. Tương tác trình duyệt đầy đủ chuyển sang P5–P8 theo chấp thuận của người dùng. Chi tiết: [web/docs/phase3-4-acceptance.md](web/docs/phase3-4-acceptance.md).

## 8. P4 — Đăng nhập, phân quyền và phiên làm việc

### Công việc

- Xây dựng đăng nhập, lấy thông tin nhân viên, trang chính và đăng xuất.
- Lưu mật khẩu bằng hàm băm phù hợp; không lưu mật khẩu thuần trong CSDL hoặc phiên.
- Kiểm tra phiên và quyền nhân viên ở mọi yêu cầu nghiệp vụ, bao gồm yêu cầu ghi dữ liệu.
- Lấy nhân viên lập phiếu từ phiên; không tin mã nhân viên gửi từ trình duyệt.
- Xử lý thiếu thông tin, đăng nhập sai, tài khoản không có hồ sơ nhân viên, hết phiên và truy cập trực tiếp URL.
- Khi hết phiên phải báo rõ; việc phục hồi phiếu tạm tuân theo Q10, không hứa giữ dữ liệu nếu chưa có cơ chế.
- Thiết lập bảo vệ yêu cầu thay đổi dữ liệu và cấu hình cookie phiên phù hợp môi trường triển khai.

### Đầu ra

- Luồng đăng nhập/đăng xuất và cơ chế bảo vệ các trang nghiệp vụ.

### Nghiệm thu

- [x] Đúng tài khoản nhân viên thì vào được trang chính và hiện đúng thông tin.
- [x] Sai thông tin nhận thông báo chung, không tiết lộ tài khoản có tồn tại hay không; thử sai lần thứ 6 trong cửa sổ khóa trả HTTP 429.
- [x] Chưa đăng nhập hoặc đã đăng xuất thì không đọc/ghi được đường dẫn được bảo vệ; GET chuyển về đăng nhập, POST trả HTTP 401.
- [x] Danh tính trang chính lấy từ session/CSDL; tham số `employeeId=999` không thay đổi nhân viên hiển thị. Đối chiếu người lập phiếu trong CSDL chuyển sang nghiệm thu P6 khi có endpoint ghi phiếu.
- [x] Kiểm tra luồng đăng nhập không đưa mật khẩu vào URL, trang lỗi hoặc log ứng dụng.

Kết quả nghiệm thu lại: P4 đạt trong phạm vi nền tảng đăng nhập/phiên; 20/20 kiểm tra HTTP đạt. Các mục cần thêm dữ liệu/môi trường hoặc module ghi phiếu đã được người dùng cho phép hoãn và gán sang P6/P8/P9 trong [web/docs/phase3-4-acceptance.md](web/docs/phase3-4-acceptance.md); không cản trở bắt đầu P5.

## 9. P5 — Tìm kiếm, thêm và sửa NCC

**Trạng thái: Hoàn thành.** Biên bản và bằng chứng chạy nằm trong [nghiệm thu P5–P7](web/docs/phase5-7-acceptance.md).

### Công việc

- Tìm theo tên, mã hoặc địa chỉ theo đặc tả; quy định rõ hành vi từ khóa rỗng và khoảng trắng.
- Hiển thị bảng kết quả, trạng thái không tìm thấy và yêu cầu chọn NCC trước khi sửa.
- Xây dựng form thêm NCC phục vụ luồng nhập; không tạo chức năng NCC tự đăng nhập.
- Xây dựng form sửa; mã NCC chỉ đọc và được bảo vệ phía máy chủ.
- Dùng cùng bộ quy tắc kiểm tra cho thêm/sửa; thực hiện cả trên giao diện và máy chủ.
- Xử lý dữ liệu trùng, NCC không tồn tại, lỗi lưu và xung đột cập nhật theo P0.
- Giữ nội dung đã nhập khi lỗi; chỉ báo thành công sau khi lưu thật sự thành công.
- Áp dụng đúng điều hướng sau thêm và sửa theo mục 2.2.

### Đầu ra

- Chức năng NCC hoàn chỉnh và dữ liệu đã lưu có thể tìm lại được.

### Nghiệm thu

- [x] Tìm theo mã/tên/địa chỉ, từ khóa ký tự đặc biệt và trạng thái không kết quả được kiểm tra.
- [x] Màn hình sửa chỉ mở theo NCC được chọn; token phiên bản chặn thay ID trong yêu cầu.
- [x] Dữ liệu sai và thông tin trùng bị từ chối ở máy chủ, nội dung form/CSRF được giữ khi hiển thị lỗi.
- [x] Mã NCC chỉ đọc; yêu cầu đổi ID không thể sửa đối tượng khác.
- [x] Thêm và sửa thành công đã chạy qua CSDL; điều hướng và thông báo theo nghiệp vụ đã chốt.
- [x] Câu lệnh sửa chỉ cập nhật thuộc tính NCC và version; khóa/liên kết phiếu nhập không bị thay đổi.

## 10. P6 — Đầu truyện và nhập truyện

**Trạng thái: Luồng chức năng, lưu thành công và rollback khi lỗi giữa giao dịch đã kiểm tra.** Timeout thật và rời trang bằng trình duyệt được ghi là việc P8.

### Công việc

- Xây dựng tìm kiếm đầu truyện theo tên/mã, thêm đầu truyện và kiểm tra trùng theo P0.
- Tạo phiếu tạm gắn với nhân viên và NCC; giữ phiếu khi đi qua các màn hình tìm/thêm dữ liệu.
- Nhận đầu truyện, số lượng, đơn giá nhập và giá thuê/ngày; kiểm tra kiểu, giới hạn và trường bắt buộc.
- Tính thành tiền và tổng tiền trên máy chủ; giao diện có thể hiển thị trước để hỗ trợ nhập.
- Xử lý dòng trùng, sửa/xóa dòng, nhiều tab và hủy phiếu đúng quyết định Q06/Q09.
- Khi xác nhận, kiểm tra lại phiếu không rỗng, đối tượng liên quan còn hợp lệ và dữ liệu không bị sửa trái phép.
- Lưu phiếu nhập, chi tiết và tạo đúng số quyển trong một giao dịch CSDL; lỗi bất kỳ bước nào phải hoàn tác toàn bộ.
- Ngăn tạo phiếu trùng do nhấn Lưu nhiều lần, tải lại trang hoặc gửi lại yêu cầu.
- Sau lưu thành công, kết thúc phiếu tạm; nếu lưu thất bại thì giữ dữ liệu hợp lệ để thử lại.

### Đầu ra

- Luồng nhập truyện đầy đủ từ chọn NCC đến phiếu và quyển truyện đã lưu.

### Nghiệm thu

- [x] Phiếu nhiều đầu truyện có tổng đúng theo số lượng × đơn giá.
- [x] Đầu truyện mới ở dạng tạm chưa có trong CSDL trước khi xác nhận lưu.
- [x] Hai dòng số lượng 2 và 3 tạo đúng 5 quyển; CSDL xác nhận một phiếu, hai dòng và đúng tổng.
- [x] Phiếu rỗng, số lượng không dương và giá lẻ bị từ chối; draft vẫn hiển thị sau lỗi.
- [x] Gửi thêm tổng/NCC/nhân viên giả trong yêu cầu không thay đổi dữ liệu lấy từ draft và session.
- [x] Trigger lỗi tạm tại bước tạo quyển xác nhận transaction rollback phiếu, dòng, quyển và đầu truyện; draft còn dùng thử lại được.
- [x] Gửi lại cùng draft sau lưu trả về đúng phiếu đã tạo, không phát sinh dữ liệu trùng.
- [x] Draft được giữ khi tìm/thêm NCC; đổi NCC yêu cầu xác nhận và tạo draft độc lập.
- [x] Hủy có xác nhận, phiên bản cũ và nhiều tab đã kiểm tra; draft không trộn giữa các tab.
- [ ] Kiểm tra rời trang bằng trình duyệt và hết phiên thực tế chuyển sang P8.

## 11. P7 — Thống kê NCC và chi tiết hóa đơn trả

**Trạng thái: Chức năng và số liệu mẫu đã nghiệm thu.** Công thức sử dụng theo mô tả trong [mô hình dữ liệu P2](web/docs/data-model.md); kiểm thử khi CSDL ngừng hoạt động được chuyển sang P8.

### Công việc

- Xây dựng nhập khoảng thời gian, kiểm tra ngày và hiển thị thống kê giảm dần theo doanh thu.
- Áp dụng chính xác ngày lọc, hai biên thời gian và cách xử lý NCC doanh thu 0 theo P0.
- Truy nguồn từng quyển qua chi tiết nhập và phiếu nhập; không gán NCC trực tiếp cho đầu truyện để tính báo cáo.
- Tổng hợp tiền thuê theo công thức đã xác nhận; ngăn cộng trùng do nối các bảng chi tiết/lỗi/tài sản cọc.
- Xây dựng trang hóa đơn trả của NCC: thông tin NCC, khoảng thời gian, mã hóa đơn, ngày liên quan, phần doanh thu thuộc NCC và tổng doanh thu.
- Một hóa đơn chứa nhiều NCC chỉ đóng góp phần doanh thu thuộc NCC đang xem.
- Giữ bộ lọc và kết quả khi quay lại; phân biệt không có dữ liệu với không tải được báo cáo.

### Đầu ra

- Báo cáo tổng hợp và trang chi tiết hóa đơn trả với số liệu đối chiếu được.

### Nghiệm thu

- [x] Ngày trống, sai định dạng hoặc khoảng ngày đảo ngược được báo lỗi rõ.
- [x] Kết quả kiểm tra các khoảng tháng 01, ngày 03/01 và ngày 01/02; cả hai biên được tính.
- [x] Cùng đầu truyện nhập từ hai NCC được phân theo nguồn của từng quyển vật lý.
- [x] Hóa đơn có hai NCC hiển thị đúng phần tiền của NCC đang xem.
- [x] Nhiều lần thuê dùng giá chụp tại giao dịch; nối bảng báo cáo không nhân tiền theo lỗi/cọc.
- [x] Tổng trang chi tiết khớp tổng NCC trong dữ liệu mẫu.
- [x] Kết quả giữ đúng doanh thu lịch sử dù giá quyển hiện tại khác.
- [x] Quay lại dùng snapshot giữ nguyên bộ lọc/kết quả; bảng giảm dần; dữ liệu rỗng hiển thị riêng.
- [ ] Tiêm lỗi CSDL khi tải báo cáo để xác nhận trang lỗi khác trạng thái rỗng; chuyển sang P8.

## 12. P8 — Kiểm thử tích hợp và hoàn thiện chất lượng

Tiếp nhận các mục kiểm tra P3–P4 đã hoãn: tương tác trình duyệt tổng hợp, timeout thực tế, lỗi CSDL khi đăng nhập và tài khoản không phải nhân viên có mật khẩu hợp lệ. Checklist chi tiết ở [biên bản P3–P4](web/docs/phase3-4-acceptance.md).

### Công việc

- Lập ma trận yêu cầu → tình huống kiểm thử → kết quả; ưu tiên nghiệp vụ tiền, giao dịch lưu và quyền truy cập.
- Chạy xuyên suốt các luồng M3 trên cùng bộ dữ liệu; kiểm tra trực tiếp CSDL khi cần chứng minh số quyển và nguồn nhập.
- Kiểm tra thao tác gửi lại, hết phiên, đối tượng không tồn tại, nhiều tab và xung đột cập nhật theo chính sách đã chọn.
- Kiểm tra lỗi CSDL tại đăng nhập, lưu NCC, lưu phiếu và tải thống kê; không hiển thị thành công giả.
- Kiểm tra truy vấn có tham số, nội dung hiển thị được xử lý an toàn, kiểm tra quyền và bảo vệ yêu cầu ghi dữ liệu.
- Kiểm tra giao diện trên trình duyệt/kích thước đã chốt; thao tác bàn phím cơ bản và bảng dài.
- Chốt quy mô dữ liệu thử và ngưỡng thời gian phản hồi cần đạt; chỉ tối ưu khi có kết quả đo cho thấy cần thiết.
- Ghi lỗi, sửa và kiểm tra lại đúng vùng bị ảnh hưởng; không bổ sung kiểm thử chỉ lặp lại chi tiết cài đặt.

### Đầu ra

- Báo cáo kiểm thử, danh sách lỗi đã xử lý và các hạn chế còn lại.

### Nghiệm thu

- [ ] Tất cả tình huống bắt buộc của P4–P7 đạt.
- [ ] Không còn lỗi làm sai tiền, mất dữ liệu, lưu dở dang, lưu trùng hoặc truy cập trái phép.
- [ ] Không còn lỗi chặn luồng chính; hạn chế còn lại được mô tả và được chấp nhận rõ ràng.
- [ ] Không có thông báo lỗi kỹ thuật nội bộ hoặc bí mật trong giao diện/log bàn giao.
- [ ] Các màn hình đạt mục tiêu tương thích và ngưỡng phản hồi đã thống nhất.

## 13. P9 — Triển khai, nghiệm thu và bàn giao

Tiếp nhận kiểm tra HTTPS/cookie/proxy và đánh giá giới hạn bộ đếm đăng nhập theo cấu hình triển khai thực tế từ [biên bản P3–P4](web/docs/phase3-4-acceptance.md).

### Công việc

- Chuẩn bị bản build, cấu hình mẫu, bộ tạo/nâng cấp CSDL và dữ liệu demo có kết quả kỳ vọng.
- Triển khai vào môi trường đích đã chốt; dùng HTTPS nếu công khai trên mạng.
- Ghi hướng dẫn cài đặt, chạy, cấu hình, tạo tài khoản demo, sao lưu và phục hồi dữ liệu.
- Kiểm tra lại sau triển khai: đăng nhập, tìm/sửa NCC, nhập phiếu và xem báo cáo.
- Chuẩn bị kịch bản trình diễn, bao gồm một ngoại lệ nhập liệu, một lần lưu phiếu và báo cáo hóa đơn có nhiều NCC.
- Cập nhật tài liệu để phản ánh phiên bản cuối, đặc biệt quyền nhân viên, điều hướng và hóa đơn trả.
- Không dùng thao tác đặt lại dữ liệu demo để xóa dữ liệu thật của môi trường đang sử dụng.

### Đầu ra

- Website chạy tại môi trường đích, mã nguồn, bộ thiết lập CSDL và hướng dẫn sử dụng/triển khai.
- Biên bản nghiệm thu hoặc checklist cuối có kết quả và bằng chứng.

### Nghiệm thu

- [ ] Có thể cài đặt và chạy lại từ hướng dẫn trên môi trường sạch phù hợp.
- [ ] Ba chức năng M3 chạy xuyên suốt trên bản đã triển khai.
- [ ] Báo cáo demo khớp kết quả tính tay, kể cả trường hợp một hóa đơn liên quan nhiều NCC.
- [ ] Có quy trình sao lưu/phục hồi và đã thử trên dữ liệu thử nghiệm.
- [ ] Tài khoản demo và cấu hình được bàn giao an toàn; không có bí mật ghi cứng trong mã nguồn.
- [ ] Tài liệu cuối ghi đúng phạm vi đã làm và các hạn chế đã được chấp nhận.

## 14. Quy chuẩn thông báo và bảo toàn dữ liệu áp dụng mọi phase

| Loại tình huống | Hành vi bắt buộc |
|---|---|
| Thiếu/sai dữ liệu | Chỉ rõ trường và cách sửa; giữ nội dung hợp lệ đã nhập. |
| Không có kết quả | Hiển thị trạng thái rỗng, cho tìm/chọn lại; không gọi là lỗi hệ thống. |
| Đối tượng không còn hợp lệ | Không tiếp tục lưu; hướng dẫn tải lại hoặc chọn lại đối tượng. |
| Hết phiên | Báo hết phiên, chuyển đăng nhập; xử lý phiếu tạm đúng chính sách đã chốt. |
| Xử lý/lưu thất bại | Không báo thành công; giữ dữ liệu có thể thử lại; ghi chi tiết vào log. |
| Thành công | Chỉ báo sau khi thao tác hoàn tất; điều hướng đúng đặc tả. |
| Thao tác có thể mất phiếu tạm | Xác nhận trước theo chính sách đã chốt; không làm mất âm thầm. |
| Gửi lại yêu cầu | Không tạo thêm dữ liệu ngoài ý muốn; kiểm soát ở máy chủ, không chỉ vô hiệu hóa nút. |

## 15. Điều kiện hoàn thành toàn dự án M3

- [ ] Các quyết định nghiệp vụ đã được chốt và ghi lại; không còn giả định ngầm ảnh hưởng tiền hoặc dữ liệu.
- [ ] Hoàn thành P0–P9 với bằng chứng nghiệm thu phù hợp.
- [ ] Nhân viên thực hiện được trọn vẹn các chức năng M3, không phụ thuộc thao tác thủ công sửa CSDL để hoàn tất nghiệp vụ.
- [ ] Ngoại lệ được xử lý rõ ràng; dữ liệu không bị mất, lưu trùng hoặc lưu dở dang.
- [ ] Truy đúng nguồn NCC theo từng quyển và thống kê đúng phần tiền từ hóa đơn trả.
- [ ] Mã nguồn, CSDL, cấu hình mẫu và tài liệu đủ để chạy lại dự án.

Trạng thái cập nhật ngày 11/11/2026: P1–P5 và P7 đã hoàn thành trong phạm vi nghiệm thu hiện tại; P6 đã kiểm tra cả giao dịch thành công và rollback. Kiểm thử timeout/rời trang thật, UI tổng hợp và P9 nằm ở các phase tiếp theo. P8 và P9 chưa thực hiện. Xem [biên bản P5–P7](web/docs/phase5-7-acceptance.md).
