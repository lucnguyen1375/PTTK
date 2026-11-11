<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="vn.ptit.pttkm3.dao.NhaCungCapDao,vn.ptit.pttkm3.model.NhaCungCap,vn.ptit.pttkm3.service.ImportDraft,vn.ptit.pttkm3.web.SecuritySupport" %>
<% NhaCungCapDao.Page page=(NhaCungCapDao.Page)request.getAttribute("suppliers"); ImportDraft draft=(ImportDraft)request.getAttribute("draft"); String q=(String)request.getAttribute("q"); %>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Chọn nhà cung cấp · Truyện Nhà Mình</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css"></head>
<body><jsp:include page="/WEB-INF/views/partials/top.jsp" /><main class="wrap">
  <div class="page-intro"><div class="eyebrow">Nhập truyện · Bước 1</div><h1><%=draft==null?"Chọn nhà cung cấp":"Đổi nhà cung cấp"%></h1><p class="lead"><%=draft==null?"Chọn nhà cung cấp để bắt đầu một phiếu nhập tạm.":"Đổi nhà cung cấp sẽ xóa phiếu tạm hiện tại và các dòng chưa lưu."%></p></div>
  <% if(draft!=null){ %><div class="alert info" role="note">Phiếu hiện tại có <strong><%=draft.lines().size()%></strong> dòng chưa lưu. Việc chọn nhà cung cấp khác sẽ hủy toàn bộ phiếu tạm.</div><% } %>
  <section class="card card-body" aria-label="Tìm nhà cung cấp">
    <form method="get" action="${pageContext.request.contextPath}/app/imports">
      <%if(draft!=null){%><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draft.id())%>"><%}%>
      <div class="field"><label for="supplier-query">Mã hoặc tên nhà cung cấp</label><div class="inline"><input id="supplier-query" name="q" maxlength="255" value="<%=SecuritySupport.escape(q)%>" placeholder="Nhập từ khóa"><button class="btn" type="submit">Tìm</button></div></div>
    </form>
  </section>
  <section class="card" style="margin-top:18px" aria-labelledby="supplier-results-title">
    <div class="card-head"><h2 id="supplier-results-title">Danh sách nhà cung cấp</h2><span class="small"><%=page.total()%> kết quả</span></div>
    <%if(page.items().isEmpty()){%><div class="empty"><strong>Không tìm thấy nhà cung cấp</strong>Thử từ khóa khác hoặc thêm nhà cung cấp mới.</div><%}else{%>
    <div class="table-wrap"><table class="table"><thead><tr><th>Mã</th><th>Tên nhà cung cấp</th><th>Địa chỉ</th><th>Điện thoại</th><th>Email</th><th>Chọn</th></tr></thead><tbody>
      <%for(NhaCungCap supplier:page.items()){ %><tr><td class="code"><%=SecuritySupport.escape(supplier.getMaNCC())%></td><td><strong><%=SecuritySupport.escape(supplier.getTenNCC())%></strong></td><td><%=SecuritySupport.escape(supplier.getDiaChi())%></td><td><%=SecuritySupport.escape(supplier.getSoDienThoai())%></td><td><%=SecuritySupport.escape(supplier.getEmail())%></td><td>
        <form class="stack" method="post" action="${pageContext.request.contextPath}/app/imports/<%=draft==null?"create":"change-supplier"%>">
          <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="supplierId" value="<%=supplier.getId()%>">
          <%if(draft!=null){%><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draft.id())%>"><label class="check-row"><input type="checkbox" name="confirmed" value="1" required><span>Xác nhận hủy phiếu tạm và chọn nhà cung cấp này</span></label><%}%>
          <button class="btn sm primary" type="submit"><%=draft==null?"Chọn":"Đổi nhà cung cấp"%></button>
        </form>
      </td></tr><% } %>
    </tbody></table></div>
    <nav class="pagination" aria-label="Phân trang nhà cung cấp"><span class="page-count">Trang <%=page.page()%> / <%=page.pageCount()%></span>
      <%if(page.page()>1){%><a class="btn sm" href="?q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=page.page()-1%><%=draft==null?"":"&amp;draftId="+draft.id()%>">Trước</a><%}%>
      <%if(page.page()<page.pageCount()){%><a class="btn sm" href="?q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=page.page()+1%><%=draft==null?"":"&amp;draftId="+draft.id()%>">Sau</a><%}%>
    </nav><%}%>
  </section>
  <div class="footer-actions spread" style="margin-top:16px"><a class="btn primary" href="${pageContext.request.contextPath}/app/suppliers/add?returnTo=import&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=page.page()%><%=draft==null?"":"&amp;draftId="+java.net.URLEncoder.encode(draft.id(),java.nio.charset.StandardCharsets.UTF_8)%>">Thêm nhà cung cấp</a><%if(draft!=null){%><a class="btn" href="${pageContext.request.contextPath}/app/imports/draft?id=<%=SecuritySupport.escape(draft.id())%>">Quay lại phiếu</a><%}%></div>
</main></body></html>
