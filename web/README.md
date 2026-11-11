# PTTK M3 — Môi trường phát triển

Khung ứng dụng dùng Java 21, Maven, Servlet/JSP trên Tomcat 10.1 và MySQL 8.4. Các thao tác dưới đây được chạy trong thư mục `web`.

## Cấu trúc mã nguồn

```text
src/main/java/vn/ptit/pttkm3/
  config/   đọc cấu hình, tạo DataSource, khởi tạo ứng dụng
  web/      Servlet điều khiển request và Filter dùng chung
  model/    mô hình dữ liệu và quy tắc chuẩn hóa cơ bản
  service/  nghiệp vụ đăng nhập, NCC, nhập truyện và báo cáo
  dao/      truy cập JDBC có tham số
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
2. Đặt `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD` và `DEMO_PASSWORD` thành mật khẩu cục bộ riêng; không commit `.env`.
3. Chạy `docker compose up --build -d`.
4. Tạo schema và dữ liệu demo theo mục **Database P2** bên dưới.
5. Mở `http://localhost:8080/`. Đăng nhập demo bằng username `a` và giá trị `DEMO_PASSWORD` trong file `.env`.
6. Prototype giao diện P3 chạy riêng, không dùng CSDL: mở `prototype/index.html` bằng Chrome hoặc Edge; thêm `?view=report`, `?view=import-draft`, v.v. để mở nhanh màn hình minh họa.

Compose khởi tạo database `pttk_m3`, tài khoản ứng dụng `pttk_app` và named volume `mysql-data`. Mật khẩu `DEMO_PASSWORD` dùng cho tài khoản demo `a`; trong CSDL chỉ lưu PBKDF2 hash. Thay đổi mật khẩu database sau lần MySQL đầu tiên cần được xử lý cùng dữ liệu trong volume; biến khởi tạo không tự đổi mật khẩu của database đã có.

## Database P2

Các migration version hóa nằm tại `src/main/resources/db/migration`. Tạo/nâng cấp schema bằng Flyway, nạp seed demo và chạy verifier từ thư mục `web`:

```powershell
docker compose --profile tools run --rm db-tools -Pdb-tools flyway:migrate
docker compose --profile tools run --rm db-tools -Pdb-tools compile exec:java@seed-demo -q
docker compose --profile tools run --rm db-tools -Pdb-tools compile exec:java@verify-schema -q
```

Seed chỉ chạy lần đầu; chạy lại sẽ báo đã có và không sửa dữ liệu. Verifier kiểm tra 18 bảng nghiệp vụ và đối chiếu kết quả doanh thu demo. `db-tools` là service chỉ bật theo profile `tools`; MySQL không publish port ra máy chủ.

Mô hình, quy tắc dữ liệu và doanh thu mẫu được ghi tại [docs/data-model.md](docs/data-model.md). P5–P7 đã bổ sung tìm/thêm/sửa NCC, phiếu nhập và báo cáo doanh thu hóa đơn trả. Biên bản nghiệm thu: [P3–P4](docs/phase3-4-acceptance.md) và [P5–P7](docs/phase5-7-acceptance.md).

Kết quả và giới hạn nghiệm thu P3–P4: [docs/phase3-4-acceptance.md](docs/phase3-4-acceptance.md).

## Kiểm tra và log

Kiểm tra hồi quy các phase đã triển khai từ thư mục `web` (cần Node.js):

```powershell
node scripts/check-prototype.cjs
node scripts/check-auth.cjs
node scripts/check-phase5-6.cjs
node scripts/check-business-commits.cjs
node scripts/check-import-rollback.cjs
node scripts/check-phase7.cjs
```

Hai lệnh đầu kiểm tra prototype và đăng nhập. `check-phase5-6.cjs` kiểm tra tìm/sửa NCC, kiểm tra lỗi và vòng đời phiếu nháp. `check-business-commits.cjs` tạo NCC/phiếu nhập kiểm tra rồi dọn theo email và request key duy nhất. `check-import-rollback.cjs` tạo trigger lỗi tạm thời để kiểm chứng rollback rồi gỡ trigger. Hai lệnh này chỉ chạy trên CSDL phát triển/kiểm thử. `check-phase7.cjs` đối chiếu báo cáo bằng dữ liệu mẫu. Các lệnh HTTP dùng tài khoản `a` và mật khẩu cục bộ trong `.env`. Phạm vi hoãn kiểm tra được ghi trong biên bản từng phase.

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
