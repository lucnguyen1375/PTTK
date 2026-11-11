<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="vn.ptit.pttkm3.dao.NhaCungCapDao,vn.ptit.pttkm3.model.NhaCungCap,vn.ptit.pttkm3.web.SecuritySupport,vn.ptit.pttkm3.service.ImportDraft" %>
<% NhaCungCapDao.Page result=(NhaCungCapDao.Page)request.getAttribute("result"); String q=(String)request.getAttribute("q"),draftId=(String)request.getAttribute("draftId"); ImportDraft draft=(ImportDraft)request.getAttribute("draft"); String returnTo=(String)request.getAttribute("returnTo"); boolean importing="import".equals(returnTo); String flash=(String)session.getAttribute("supplierFlash"); if(flash!=null)session.removeAttribute("supplierFlash"); %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Nhà cung cấp · Truyện Nhà Mình</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css"></head><body>
<jsp:include page="/WEB-INF/views/partials/top.jsp" /><main class="wrap">
  <div class="page-intro"><div class="eyebrow">Quản lý</div><h1><%=importing?"Chọn nhà cung cấp":"Nhà cung cấp"%></h1><p class="lead">Tìm kiếm, xem và cập nhật thông tin nhà cung cấp.</p></div>
  <%if(flash!=null){%><div class="alert success" role="status"><%=SecuritySupport.escape(flash)%></div><%}%>
  <section class="card card-body" aria-label="Tìm nhà cung cấp"><form method="get" action="${pageContext.request.contextPath}/app/suppliers">
    <input type="hidden" name="returnTo" value="<%=importing?"import":"find"%>"><%if(draftId!=null&&!draftId.isBlank()){%><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draftId)%>"><%}%>
    <div class="field"><label for="supplier-search">Mã, tên hoặc địa chỉ</label><div class="inline"><input id="supplier-search" name="q" maxlength="500" value="<%=SecuritySupport.escape(q)%>" placeholder="Nhập từ khóa"><button class="btn" type="submit">Tìm</button></div></div>
  </form></section>
  <section class="card" style="margin-top:18px" aria-labelledby="supplier-list-heading">
    <div class="card-head"><h2 id="supplier-list-heading">Danh sách nhà cung cấp</h2><span class="small"><%=result.total()%> kết quả</span></div>
    <%if(result.items().isEmpty()){%><div class="empty"><strong>Không tìm thấy nhà cung cấp</strong>Thử từ khóa khác hoặc thêm nhà cung cấp mới.</div><%}else{%>
    <div class="table-wrap"><table class="table"><thead><tr><th>Mã</th><th>Tên</th><th>Địa chỉ</th><th>Điện thoại</th><th>Email</th><th>Thao tác</th></tr></thead><tbody>
      <%for(NhaCungCap supplier:result.items()){%><tr><td class="code"><%=SecuritySupport.escape(supplier.getMaNCC())%></td><td><strong><%=SecuritySupport.escape(supplier.getTenNCC())%></strong></td><td><%=SecuritySupport.escape(supplier.getDiaChi())%></td><td><%=SecuritySupport.escape(supplier.getSoDienThoai())%></td><td><%=SecuritySupport.escape(supplier.getEmail())%></td><td><a class="btn sm" href="${pageContext.request.contextPath}/app/suppliers/edit?id=<%=supplier.getId()%>&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=result.page()%>">Sửa</a></td></tr><%}%>
    </tbody></table></div>
    <nav class="pagination" aria-label="Phân trang nhà cung cấp"><span class="page-count">Trang <%=result.page()%> / <%=result.pageCount()%></span>
      <%if(result.page()>1){%><a class="btn sm" href="?q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;returnTo=<%=importing?"import":"find"%>&amp;page=<%=result.page()-1%><%=draftId==null||draftId.isBlank()?"":"&amp;draftId="+java.net.URLEncoder.encode(draftId,java.nio.charset.StandardCharsets.UTF_8)%>">Trước</a><%}%>
      <%if(result.page()<result.pageCount()){%><a class="btn sm" href="?q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;returnTo=<%=importing?"import":"find"%>&amp;page=<%=result.page()+1%><%=draftId==null||draftId.isBlank()?"":"&amp;draftId="+java.net.URLEncoder.encode(draftId,java.nio.charset.StandardCharsets.UTF_8)%>">Sau</a><%}%>
    </nav><%}%>
  </section>
  <div class="footer-actions" style="margin-top:16px"><a class="btn primary" href="${pageContext.request.contextPath}/app/suppliers/add?returnTo=<%=importing?"import":"find"%>&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=result.page()%><%=draftId==null||draftId.isBlank()?"":"&amp;draftId="+java.net.URLEncoder.encode(draftId,java.nio.charset.StandardCharsets.UTF_8)%>">Thêm nhà cung cấp</a></div>
</main></body></html>
