<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>PTTK M3 — Hệ thống quản lí cửa hàng cho thuê truyện</title>
    <style>
        :root { color-scheme: light; font-family: system-ui, sans-serif; color: #17212b; background: #f3f6fa; }
        body { margin: 0; min-height: 100vh; display: grid; place-items: center; }
        main { width: min(34rem, calc(100% - 3rem)); padding: 2rem; background: white; border: 1px solid #d9e1ea; border-radius: 1rem; box-shadow: 0 1rem 3rem #17212b12; }
        h1 { margin-top: 0; font-size: 1.6rem; }
        p { line-height: 1.6; }
        code { color: #075985; }
        .status { padding: .75rem 1rem; background: #e8f5ee; border-radius: .5rem; color: #14532d; }
    </style>
</head>
<body>
<main>
    <h1>Quản lí cửa hàng cho thuê truyện</h1>
    <p>Khung ứng dụng Module M3 đã khởi động. Các chức năng nghiệp vụ sẽ được bổ sung ở những phase tiếp theo.</p>
    <p class="status">Ứng dụng đang phục vụ yêu cầu. Trạng thái cơ sở dữ liệu: <a href="${pageContext.request.contextPath}/health/ready">kiểm tra kết nối</a>.</p>
    <p>Mã yêu cầu: <code><%= request.getAttribute("vn.ptit.pttkm3.web.RequestLoggingFilter.requestId") %></code></p>
</main>
</body>
</html>
