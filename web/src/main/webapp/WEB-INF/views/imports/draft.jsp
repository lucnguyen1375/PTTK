<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.text.NumberFormat,java.util.Locale,vn.ptit.pttkm3.service.ImportDraft,vn.ptit.pttkm3.model.NhaCungCap,vn.ptit.pttkm3.auth.SessionEmployee,vn.ptit.pttkm3.web.SecuritySupport" %>
<%
  ImportDraft draft=(ImportDraft)request.getAttribute("draft");
  NhaCungCap supplier=(NhaCungCap)request.getAttribute("supplier");
  SessionEmployee employee=(SessionEmployee)request.getAttribute("employee");
  NumberFormat money=NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
  java.math.BigDecimal total=draft.lines().stream().map(line->line.cost().multiply(java.math.BigDecimal.valueOf(line.quantity()))).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add);
  String error=(String)request.getAttribute("error"), editKey=(String)request.getAttribute("editKey");
  int row=0;
%>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Phiếu nhập tạm · Truyện Nhà Mình</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css"></head>
<body><jsp:include page="/WEB-INF/views/partials/top.jsp" />
<main class="wrap">
  <div class="page-intro"><div class="eyebrow">Nhập truyện · Phiếu tạm</div><h1>Phiếu nhập tạm</h1><p class="lead">Kiểm tra nhà cung cấp và các đầu truyện trước khi lưu phiếu.</p></div>
  <% if(error!=null){ %><div id="error-summary" class="alert error" role="alert" tabindex="-1"><strong>Có thao tác chưa hoàn tất.</strong><br><%=SecuritySupport.escape(error)%></div><script>document.getElementById('error-summary').focus();</script><% } %>
  <section class="card card-body stack" aria-label="Thông tin phiếu nhập">
    <div class="card-head"><h2>Thông tin phiếu</h2><span class="small">Mã tạm <code><%=SecuritySupport.escape(draft.id())%></code> · Phiên bản <%=draft.version()%></span></div>
    <div class="summary-grid">
      <div class="summary-item"><span>Nhà cung cấp</span><strong><%=supplier==null?"Không còn tồn tại":SecuritySupport.escape(supplier.getTenNCC())%></strong><span><%=supplier==null?"":SecuritySupport.escape(supplier.getMaNCC())%></span></div>
      <div class="summary-item"><span>Nhân viên lập</span><strong><%=SecuritySupport.escape(employee.getFullName())%></strong><span><%=SecuritySupport.escape(employee.getCode())%></span></div>
      <div class="summary-item"><span>Thời điểm tạo phiếu tạm</span><strong><%=draft.createdAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))%></strong></div>
    </div>
    <div class="footer-actions spread">
      <a class="btn" href="${pageContext.request.contextPath}/app/imports?draftId=<%=SecuritySupport.escape(draft.id())%>">Đổi nhà cung cấp</a>
      <form method="post" action="${pageContext.request.contextPath}/app/imports/cancel" onsubmit="return confirm('Hủy phiếu tạm và xóa toàn bộ dòng chưa lưu?')">
        <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draft.id())%>"><input type="hidden" name="confirmed" value="1">
        <button class="btn danger" type="submit">Hủy phiếu</button>
      </form>
    </div>
  </section>

  <section class="card" aria-labelledby="lines-title" style="margin-top:18px">
    <div class="card-head"><h2 id="lines-title">Đầu truyện trong phiếu</h2><span class="small"><%=draft.lines().size()%> dòng</span></div>
    <% if(draft.lines().isEmpty()){ %><div class="empty"><strong>Phiếu chưa có đầu truyện</strong>Dùng nút “Tìm đầu truyện” bên dưới để bắt đầu thêm dòng.</div>
    <% } else { %>
      <div class="table-wrap"><table class="table"><thead><tr><th>Đầu truyện</th><th class="numeric">Số lượng</th><th class="numeric">Giá nhập</th><th class="numeric">Giá thuê/ngày</th><th class="numeric">Thành tiền</th><th>Thao tác</th></tr></thead><tbody>
      <% for(ImportDraft.Line line:draft.lines()){ row++; boolean editing=line.key().equals(editKey); %>
        <tr><td><strong><%=SecuritySupport.escape(line.title().name())%></strong><div class="small"><%=line.title().code()==null?"Đầu truyện mới trong phiếu":SecuritySupport.escape(line.title().code())%> · <%=SecuritySupport.escape(line.title().author())%></div></td>
          <td class="numeric"><%=line.quantity()%></td><td class="numeric"><%=money.format(line.cost())%> ₫</td><td class="numeric"><%=money.format(line.rent())%> ₫</td><td class="numeric"><strong><%=money.format(line.cost().multiply(java.math.BigDecimal.valueOf(line.quantity())))%> ₫</strong></td>
          <td><div class="table-actions"><a class="btn sm" href="${pageContext.request.contextPath}/app/imports/draft?id=<%=SecuritySupport.escape(draft.id())%>&editKey=<%=java.net.URLEncoder.encode(line.key(),java.nio.charset.StandardCharsets.UTF_8)%>">Chỉnh sửa</a>
            <form method="post" action="${pageContext.request.contextPath}/app/imports/line/remove" onsubmit="return confirm('Xóa đầu truyện này khỏi phiếu?')"><input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draft.id())%>"><input type="hidden" name="version" value="<%=draft.version()%>"><input type="hidden" name="lineKey" value="<%=SecuritySupport.escape(line.key())%>"><button class="btn sm danger" type="submit">Xóa</button></form></div></td>
        </tr>
        <% if(editing){ String qty=(String)request.getAttribute("editQuantity"),cost=(String)request.getAttribute("editCost"),rent=(String)request.getAttribute("editRent"); %>
        <tr><td colspan="6"><form class="form-grid" method="post" action="${pageContext.request.contextPath}/app/imports/line/edit">
          <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draft.id())%>"><input type="hidden" name="version" value="<%=draft.version()%>"><input type="hidden" name="lineKey" value="<%=SecuritySupport.escape(line.key())%>">
          <div class="field"><label for="edit-quantity-<%=row%>">Số lượng</label><input id="edit-quantity-<%=row%>" name="quantity" type="number" min="1" max="10000" value="<%=SecuritySupport.escape(qty==null?String.valueOf(line.quantity()):qty)%>" required <%=error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>></div>
          <div class="field"><label for="edit-cost-<%=row%>">Giá nhập (VND)</label><input id="edit-cost-<%=row%>" name="cost" type="number" min="1" max="999999999999" step="1" value="<%=SecuritySupport.escape(cost==null?line.cost().toPlainString():cost)%>" required <%=error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>></div>
          <div class="field"><label for="edit-rent-<%=row%>">Giá thuê/ngày (VND)</label><input id="edit-rent-<%=row%>" name="rent" type="number" min="1" max="999999999999" step="1" value="<%=SecuritySupport.escape(rent==null?line.rent().toPlainString():rent)%>" required <%=error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>></div>
          <div class="footer-actions"><a class="btn" href="${pageContext.request.contextPath}/app/imports/draft?id=<%=SecuritySupport.escape(draft.id())%>">Đóng</a><button class="btn primary" type="submit">Lưu dòng</button></div>
        </form></td></tr><% } %>
      <% } %>
      </tbody></table></div>
      <div class="total-row"><span>Tổng tiền nhập</span><strong><%=money.format(total)%> ₫</strong></div>
    <% } %>
    <div class="card-body footer-actions spread">
      <a class="btn" href="${pageContext.request.contextPath}/app/imports/titles?id=<%=SecuritySupport.escape(draft.id())%>">Tìm đầu truyện</a>
      <form method="post" action="${pageContext.request.contextPath}/app/imports/save" onsubmit="this.querySelector('button').disabled=true;this.querySelector('button').textContent='Đang lưu…';">
        <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draft.id())%>"><input type="hidden" name="version" value="<%=draft.version()%>">
        <button class="btn primary" type="submit" <%=draft.lines().isEmpty()?"disabled":""%>>Xác nhận lưu phiếu</button>
      </form>
    </div>
  </section>
</main></body></html>
