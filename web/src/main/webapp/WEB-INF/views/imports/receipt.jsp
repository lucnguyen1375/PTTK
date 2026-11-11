<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.text.NumberFormat,java.util.Locale,vn.ptit.pttkm3.service.ImportService,vn.ptit.pttkm3.model.NhaCungCap,vn.ptit.pttkm3.web.SecuritySupport" %>
<% ImportService.Receipt receipt=(ImportService.Receipt)request.getAttribute("receipt"); NhaCungCap supplier=(NhaCungCap)request.getAttribute("supplier"); NumberFormat money=NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")); %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Đã lưu phiếu nhập · Truyện Nhà Mình</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css"></head>
<body><jsp:include page="/WEB-INF/views/partials/top.jsp" /><main class="wrap"><section class="card card-body" style="max-width:820px;margin:24px auto">
  <div class="alert success" role="status"><strong>Đã lưu phiếu nhập thành công.</strong> Dữ liệu đầu truyện, hóa đơn và các quyển truyện đã được ghi nhận.</div>
  <div class="eyebrow">Biên nhận nhập truyện</div><h1>Phiếu <%=SecuritySupport.escape(receipt.code())%></h1>
  <div class="summary-grid" style="margin-top:20px">
    <div class="summary-item"><span>Ngày lập</span><strong><%=receipt.date().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))%></strong></div>
    <div class="summary-item"><span>Nhà cung cấp</span><strong><%=supplier==null?"Không xác định":SecuritySupport.escape(supplier.getTenNCC())%></strong></div>
    <div class="summary-item"><span>Tổng tiền nhập</span><strong><%=money.format(receipt.total())%> ₫</strong></div>
    <div class="summary-item"><span>Số dòng đầu truyện</span><strong><%=receipt.lineCount()%></strong></div>
    <div class="summary-item"><span>Số quyển đã nhập</span><strong><%=receipt.copyCount()%></strong></div>
  </div>
  <div class="footer-actions" style="margin-top:22px"><a class="btn" href="${pageContext.request.contextPath}/app/home">Trang chính</a><a class="btn primary" href="${pageContext.request.contextPath}/app/imports">Lập phiếu nhập mới</a></div>
</section></main></body></html>
