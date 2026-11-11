<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.text.NumberFormat,java.util.Locale,vn.ptit.pttkm3.service.SupplierReportSnapshot,vn.ptit.pttkm3.model.SupplierRevenue,vn.ptit.pttkm3.model.ReturnInvoiceRevenue,vn.ptit.pttkm3.model.NhaCungCap,vn.ptit.pttkm3.web.SecuritySupport" %>
<%SupplierReportSnapshot snapshot=(SupplierReportSnapshot)request.getAttribute("snapshot"); SupplierRevenue summary=(SupplierRevenue)request.getAttribute("summary"); NhaCungCap supplier=(NhaCungCap)request.getAttribute("supplier"); List<ReturnInvoiceRevenue> invoices=(List<ReturnInvoiceRevenue>)request.getAttribute("invoices"); NumberFormat money=NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));%>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Hóa đơn trả · <%=SecuritySupport.escape(supplier.getTenNCC())%></title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css"></head><body>
<jsp:include page="/WEB-INF/views/partials/top.jsp" /><main class="wrap">
  <div class="page-intro"><div class="eyebrow">Thống kê · Hóa đơn trả</div><h1><%=SecuritySupport.escape(supplier.getTenNCC())%></h1><p class="lead"><%=SecuritySupport.escape(supplier.getMaNCC())%> · Kỳ thống kê <%=snapshot.from().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))%> – <%=snapshot.to().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))%></p></div>
  <section class="summary-grid" aria-label="Tổng quan nhà cung cấp">
    <div class="summary-item"><span>Số hóa đơn trả</span><strong><%=summary.returnInvoiceCount()%></strong></div>
    <div class="summary-item"><span>Tổng doanh thu</span><strong><%=money.format(summary.revenue())%> ₫</strong></div>
    <div class="summary-item"><span>Hóa đơn trong danh sách</span><strong><%=invoices.size()%></strong></div>
  </section>
  <section class="card" style="margin-top:18px" aria-labelledby="invoice-list-title"><div class="card-head"><h2 id="invoice-list-title">Danh sách hóa đơn trả</h2><span class="small">Chọn một hóa đơn để xem các quyển truyện được tính</span></div>
    <%if(invoices.isEmpty()){%><div class="empty"><strong>Không có hóa đơn trả của nhà cung cấp này</strong>Quay lại thống kê để chọn kỳ khác.</div><%}else{%>
    <div class="table-wrap"><table class="table"><thead><tr><th>Mã hóa đơn trả</th><th>Ngày lập</th><th class="numeric">Số quyển trả</th><th class="numeric">Doanh thu</th><th>Chi tiết</th></tr></thead><tbody>
      <%for(ReturnInvoiceRevenue invoice:invoices){%><tr><td class="code"><%=SecuritySupport.escape(invoice.invoiceCode())%></td><td><%=invoice.invoiceDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))%></td><td class="numeric"><%=invoice.lines().size()%></td><td class="numeric"><strong><%=money.format(invoice.revenue())%> ₫</strong></td><td><a class="btn sm" href="${pageContext.request.contextPath}/app/reports/suppliers/invoice?snapshot=<%=snapshot.id()%>&amp;supplierId=<%=supplier.getId()%>&amp;invoiceId=<%=invoice.invoiceId()%>">Xem hóa đơn</a></td></tr><%}%>
    </tbody></table></div><%}%>
  </section>
  <div class="footer-actions" style="margin-top:16px"><a class="btn" href="${pageContext.request.contextPath}/app/reports/suppliers?snapshot=<%=snapshot.id()%>">Quay lại thống kê</a></div>
</main></body></html>
