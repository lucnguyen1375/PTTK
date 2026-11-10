<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Lỗi xử lý</title></head>
<body>
<main>
    <h1>Không thể xử lý yêu cầu</h1>
    <p>Đã xảy ra lỗi trong lúc xử lý. Vui lòng thử lại sau.</p>
    <p>Mã yêu cầu: <code><%= request.getAttribute("vn.ptit.pttkm3.web.RequestLoggingFilter.requestId") %></code></p>
    <p><a href="${pageContext.request.contextPath}/">Về trang chính</a></p>
</main>
</body></html>
