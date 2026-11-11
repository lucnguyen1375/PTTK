<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="vn.ptit.pttkm3.dao.DauTruyenDao,vn.ptit.pttkm3.service.ImportDraft,vn.ptit.pttkm3.model.DauTruyen,vn.ptit.pttkm3.web.SecuritySupport" %>
<% ImportDraft draft=(ImportDraft)request.getAttribute("draft"); DauTruyenDao.Page titlePage=(DauTruyenDao.Page)request.getAttribute("titlePage"); String q=(String)request.getAttribute("titleQuery"),error=(String)request.getAttribute("error"); String selected=(String)request.getAttribute("selectedTitleId"),selectedTitleRef=(String)request.getAttribute("selectedTitleRef"),quantity=(String)request.getAttribute("quantity"),cost=(String)request.getAttribute("cost"),rent=(String)request.getAttribute("rent"); %>
<!doctype html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Tìm đầu truyện · Truyện Nhà Mình</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css"></head>
<body><jsp:include page="/WEB-INF/views/partials/top.jsp" /><main class="wrap">
  <div class="page-intro"><div class="eyebrow">Nhập truyện · Bước 2</div><h1>Tìm đầu truyện</h1><p class="lead">Tìm đầu truyện trong danh mục hoặc thêm thông tin đầu truyện mới vào phiếu tạm.</p></div>
  <% if(error!=null){ %><div id="error-summary" class="alert error" role="alert" tabindex="-1"><%=SecuritySupport.escape(error)%></div><script>document.getElementById('error-summary').focus();</script><% } %>
  <% if(Boolean.TRUE.equals(request.getAttribute("staged"))){ %><div class="alert success" role="status">Đã thêm thông tin đầu truyện vào phiếu tạm. Hãy nhập số lượng và giá để thêm đầu truyện vào phiếu.</div><% } %>
  <section class="card card-body" aria-label="Tìm trong danh mục">
    <form method="get" action="${pageContext.request.contextPath}/app/imports/titles">
      <input type="hidden" name="id" value="<%=SecuritySupport.escape(draft.id())%>">
      <div class="field"><label for="title-query">Từ khóa</label><div class="inline"><input id="title-query" name="q" maxlength="255" value="<%=SecuritySupport.escape(q)%>" placeholder="Mã hoặc tên đầu truyện"><button class="btn" type="submit">Tìm</button></div></div>
    </form>
  </section>

  <% if(!draft.stagedTitles().isEmpty()){ %>
  <section class="card" style="margin-top:18px" aria-labelledby="staged-title-heading">
    <div class="card-head"><h2 id="staged-title-heading">Đầu truyện mới trong phiếu tạm</h2><span class="small">Chưa ghi vào danh mục</span></div>
    <div class="table-wrap"><table class="table"><thead><tr><th>Tên đầu truyện</th><th>Tác giả</th><th>Thể loại</th><th>Nhà xuất bản</th><th>Thêm vào phiếu</th></tr></thead><tbody>
      <% for(ImportDraft.Title title:draft.stagedTitles()){ boolean isStagedSelected=title.reference().equals(selectedTitleRef); String stagedSuffix=title.reference().substring(4,12); %><tr><td><strong><%=SecuritySupport.escape(title.name())%></strong><div class="small">Mới · phiếu <code><%=SecuritySupport.escape(draft.id().substring(0,8))%></code></div></td><td><%=SecuritySupport.escape(title.author())%></td><td><%=SecuritySupport.escape(title.category())%></td><td><%=SecuritySupport.escape(title.publisher())%></td><td>
        <form class="table-actions" data-line-calculator method="post" action="${pageContext.request.contextPath}/app/imports/line/add-staged">
          <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draft.id())%>"><input type="hidden" name="version" value="<%=draft.version()%>"><input type="hidden" name="titleRef" value="<%=SecuritySupport.escape(title.reference())%>"><input type="hidden" name="q" value="<%=SecuritySupport.escape(q)%>"><input type="hidden" name="page" value="<%=titlePage.page()%>">
          <label class="sr-only" for="staged-qty-<%=stagedSuffix%>">Số lượng <%=SecuritySupport.escape(title.name())%></label><input id="staged-qty-<%=stagedSuffix%>" name="quantity" type="number" min="1" max="10000" value="<%=isStagedSelected&&quantity!=null?SecuritySupport.escape(quantity):"1"%>" required <%=isStagedSelected&&error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>>
          <label class="sr-only" for="staged-cost-<%=stagedSuffix%>">Giá nhập <%=SecuritySupport.escape(title.name())%> (VND)</label><input id="staged-cost-<%=stagedSuffix%>" name="cost" type="number" min="1" max="999999999999" step="1" value="<%=isStagedSelected&&cost!=null?SecuritySupport.escape(cost):""%>" placeholder="Giá nhập" required <%=isStagedSelected&&error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>>
          <label class="sr-only" for="staged-rent-<%=stagedSuffix%>">Giá thuê/ngày <%=SecuritySupport.escape(title.name())%> (VND)</label><input id="staged-rent-<%=stagedSuffix%>" name="rent" type="number" min="1" max="999999999999" step="1" value="<%=isStagedSelected&&rent!=null?SecuritySupport.escape(rent):""%>" placeholder="Giá thuê/ngày" required <%=isStagedSelected&&error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>>
          <span class="line-total-preview">Thành tiền: <output data-line-total>—</output></span>
          <button class="btn sm primary" type="submit">Thêm</button>
        </form>
      </td></tr><% } %>
    </tbody></table></div>
  </section>
  <% } %>

  <section class="card" style="margin-top:18px" aria-labelledby="catalog-title-heading">
    <div class="card-head"><h2 id="catalog-title-heading">Đầu truyện trong danh mục</h2><span class="small"><%=titlePage.total()%> kết quả</span></div>
    <% if(titlePage.items().isEmpty()){ %><div class="empty"><strong>Không tìm thấy đầu truyện</strong>Thử từ khóa khác hoặc thêm đầu truyện mới vào phiếu.</div>
    <% } else { %><div class="table-wrap"><table class="table"><thead><tr><th>Mã</th><th>Tên đầu truyện</th><th>Tác giả</th><th>Thể loại · Nhà xuất bản</th><th>Thêm vào phiếu</th></tr></thead><tbody>
      <% for(DauTruyen title:titlePage.items()){ String id=String.valueOf(title.id()); boolean isSelected=id.equals(selected); String suffix=String.valueOf(title.id()); %>
      <tr><td class="code"><%=SecuritySupport.escape(title.maDauTruyen())%></td><td><strong><%=SecuritySupport.escape(title.tenDauTruyen())%></strong><%if(title.namXuatBan()!=null){%><div class="small">Năm xuất bản: <%=title.namXuatBan()%></div><%}%></td><td><%=SecuritySupport.escape(title.tacGia())%></td><td><%=SecuritySupport.escape(title.theLoai())%><div class="small"><%=SecuritySupport.escape(title.nhaXuatBan())%></div></td><td>
        <form class="table-actions" data-line-calculator method="post" action="${pageContext.request.contextPath}/app/imports/line/add-existing">
          <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draft.id())%>"><input type="hidden" name="version" value="<%=draft.version()%>"><input type="hidden" name="titleId" value="<%=title.id()%>"><input type="hidden" name="q" value="<%=SecuritySupport.escape(q)%>"><input type="hidden" name="page" value="<%=titlePage.page()%>">
          <label class="sr-only" for="catalog-qty-<%=suffix%>">Số lượng <%=SecuritySupport.escape(title.tenDauTruyen())%></label><input id="catalog-qty-<%=suffix%>" name="quantity" type="number" min="1" max="10000" value="<%=isSelected&&quantity!=null?SecuritySupport.escape(quantity):"1"%>" required <%=isSelected&&error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>>
          <label class="sr-only" for="catalog-cost-<%=suffix%>">Giá nhập <%=SecuritySupport.escape(title.tenDauTruyen())%> (VND)</label><input id="catalog-cost-<%=suffix%>" name="cost" type="number" min="1" max="999999999999" step="1" value="<%=isSelected&&cost!=null?SecuritySupport.escape(cost):""%>" placeholder="Giá nhập" required <%=isSelected&&error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>>
          <label class="sr-only" for="catalog-rent-<%=suffix%>">Giá thuê/ngày <%=SecuritySupport.escape(title.tenDauTruyen())%> (VND)</label><input id="catalog-rent-<%=suffix%>" name="rent" type="number" min="1" max="999999999999" step="1" value="<%=isSelected&&rent!=null?SecuritySupport.escape(rent):""%>" placeholder="Giá thuê/ngày" required <%=isSelected&&error!=null?"aria-invalid=\"true\" aria-describedby=\"error-summary\"":""%>>
          <span class="line-total-preview">Thành tiền: <output data-line-total>—</output></span>
          <button class="btn sm primary" type="submit">Thêm</button>
        </form>
      </td></tr><% } %>
    </tbody></table></div><% } %>
    <%if(titlePage.pageCount()>1){%><nav class="pagination" aria-label="Phân trang đầu truyện"><span class="page-count">Trang <%=titlePage.page()%> / <%=titlePage.pageCount()%></span><%if(titlePage.page()>1){%><a class="btn sm" href="?id=<%=draft.id()%>&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=titlePage.page()-1%>">Trước</a><%}%><%if(titlePage.page()<titlePage.pageCount()){%><a class="btn sm" href="?id=<%=draft.id()%>&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=titlePage.page()+1%>">Sau</a><%}%></nav><%}%>
    <div class="card-body footer-actions spread"><a class="btn" href="${pageContext.request.contextPath}/app/imports/draft?id=<%=SecuritySupport.escape(draft.id())%>">Quay lại phiếu</a><a class="btn primary" href="${pageContext.request.contextPath}/app/imports/titles/new?id=<%=SecuritySupport.escape(draft.id())%>&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=titlePage.page()%>">Thêm đầu truyện mới</a></div>
  </section>
</main>
<script>
  document.querySelectorAll('[data-line-calculator]').forEach(form => {
    const quantity = form.elements.namedItem('quantity');
    const cost = form.elements.namedItem('cost');
    const total = form.querySelector('[data-line-total]');
    const update = () => {
      if (!quantity.value || !cost.value || !quantity.validity.valid || !cost.validity.valid) { total.textContent = '—'; return; }
      try { total.textContent = new Intl.NumberFormat('vi-VN').format(BigInt(quantity.value) * BigInt(cost.value)) + ' ₫'; }
      catch (error) { total.textContent = '—'; }
    };
    quantity.addEventListener('input', update);
    cost.addEventListener('input', update);
    update();
  });
</script></body></html>
