<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="vn.ptit.pttkm3.web.SecuritySupport" %>
<%String message=(String)request.getAttribute("loginMessage"),messageType=(String)request.getAttribute("messageType");%>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="robots" content="noindex,nofollow"><title>Đăng nhập · Truyện Nhà Mình</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css"></head>
<body><main class="login-page">
  <section class="visual"><div class="brand"><span class="logo" aria-hidden="true">T</span><span><b>Truyện Nhà Mình</b><small>Quản lý cửa hàng</small></span></div><h1>Mỗi câu chuyện hay bắt đầu từ một trang sách.</h1><p>Không gian làm việc dành cho nhân viên quản lý cửa hàng cho thuê truyện.</p></section>
  <section class="form-side"><form class="form-card" method="post" action="${pageContext.request.contextPath}/login" autocomplete="on">
    <div class="eyebrow">Khu vực nhân viên</div><h2>Đăng nhập</h2><p>Nhập tài khoản nhân viên để tiếp tục.</p>
    <%if(message!=null&&!message.isBlank()){%><div id="login-message" class="alert <%="info".equals(messageType)?"info":"error"%>" role="<%="info".equals(messageType)?"status":"alert"%>"><%=SecuritySupport.escape(message)%></div><%}%>
    <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>">
    <div class="field"><label for="username">Tên đăng nhập <span class="req">*</span></label><input id="username" name="username" value="<%=SecuritySupport.escape((String)request.getAttribute("username"))%>" autocomplete="username" maxlength="120" required autofocus></div>
    <div class="field"><label for="password">Mật khẩu <span class="req">*</span></label><input id="password" name="password" type="password" autocomplete="current-password" maxlength="1024" required></div>
    <button class="submit" type="submit">Đăng nhập</button><div class="hint">Chỉ nhân viên được cấp tài khoản mới có thể đăng nhập.</div>
  </form></section>
</main></body></html>
