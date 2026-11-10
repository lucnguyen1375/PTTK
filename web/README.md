# PTTK M3 — Môi trường phát triển

Khung ứng dụng dùng Java 21, Maven, Servlet/JSP trên Tomcat 10.1 và MySQL 8.4. Các thao tác dưới đây được chạy trong thư mục `web`.

## Cấu trúc mã nguồn

```text
src/main/java/vn/ptit/pttkm3/
  config/   đọc cấu hình, tạo DataSource, khởi tạo ứng dụng
  web/      Servlet điều khiển request và Filter dùng chung
  model/    đối tượng dữ liệu nghiệp vụ (bổ sung ở phase sau)
  service/  điều phối nghiệp vụ (bổ sung ở phase sau)
  dao/      truy cập JDBC (bổ sung ở phase sau)
src/main/webapp/WEB-INF/views/  JSP chỉ dùng để hiển thị
```

Luồng xử lý thống nhất: Servlet → service → DAO/JDBC → model; Servlet chuyển dữ liệu sang JSP. JSP không truy cập SQL trực tiếp. Đây là cách triển khai Servlet/JSP cho vai trò action JSP được mô tả trong báo cáo.

## Yêu cầu

- Docker Desktop đang chạy ở chế độ Linux containers.
- Docker Compose v2 hoặc mới hơn.
- Cổng `8080` chưa được ứng dụng khác sử dụng.

Ứng dụng chạy bên trong Docker nên không cần cài JDK hoặc Maven trên máy. MySQL không mở cổng ra máy chủ; chỉ container ứng dụng truy cập được.

## Cấu hình lần đầu

1. Sao chép `.env.example` thành `.env`.
2. Đổi `DB_PASSWORD` và `MYSQL_ROOT_PASSWORD` thành hai mật khẩu cục bộ riêng biệt. Không commit `.env`.
3. Chạy `docker compose up --build -d`.
4. Mở `http://localhost:8080/`.

Compose khởi tạo database `pttk_m3`, tài khoản ứng dụng `pttk_app` và named volume `mysql-data`. P1 chưa tạo bảng nghiệp vụ. Thay đổi mật khẩu sau lần MySQL đầu tiên cần được xử lý cùng dữ liệu trong volume; biến khởi tạo không tự đổi mật khẩu của database đã có.

## Kiểm tra và log

- `/health/live`: tiến trình web đang phục vụ request.
- `/health/ready`: ứng dụng và kết nối MySQL sẵn sàng; khi chưa sẵn sàng trả HTTP 503.
- Hai endpoint sức khỏe chỉ bật trong cấu hình `APP_ENV=development`.
- `docker compose ps`: trạng thái dịch vụ.
- `docker compose logs -f app db`: log ứng dụng và CSDL.

## Dừng và khởi động lại

- Dừng container, giữ database: `docker compose down`.
- Khởi động lại: `docker compose up -d`.
- Build lại ứng dụng: `docker compose up --build -d`.

Không dùng `docker compose down -v` nếu cần giữ dữ liệu MySQL; tùy chọn `-v` xóa volume database.
