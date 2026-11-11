<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="vn.ptit.pttkm3.dao.NhaCungCapDao,vn.ptit.pttkm3.model.NhaCungCap,vn.ptit.pttkm3.web.SecuritySupport,vn.ptit.pttkm3.service.ImportDraft" %>
<% NhaCungCapDao.Page result=(NhaCungCapDao.Page)request.getAttribute("result"); String q=(String)request.getAttribute("q"),draftId=(String)request.getAttribute("draftId"); ImportDraft draft=(ImportDraft)request.getAttribute("draft"); String returnTo=(String)request.getAttribute("returnTo"); boolean importing="import".equals(returnTo); String flash=(String)session.getAttribute("supplierFlash"),flashType=(String)session.getAttribute("supplierFlashType"); if(flash!=null){session.removeAttribute("supplierFlash");session.removeAttribute("supplierFlashType");} if(flashType==null)flashType="success"; %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Nhà cung cấp · Truyện Nhà Mình</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css"></head><body>
<jsp:include page="/WEB-INF/views/partials/top.jsp" /><main class="wrap">
  <div class="page-intro"><div class="eyebrow">Quản lý</div><h1><%=importing?"Chọn nhà cung cấp":"Nhà cung cấp"%></h1><p class="lead"><%=importing?"Chọn nhà cung cấp cho phiếu nhập tạm.":"Tìm kiếm và quản lý thông tin nhà cung cấp."%></p></div>
  <%if(flash!=null){%><div class="alert <%="error".equals(flashType)?"error":"success"%>" role="<%="error".equals(flashType)?"alert":"status"%>" tabindex="-1"><%=SecuritySupport.escape(flash)%></div><%}%>
  <section class="card card-body" aria-label="Tìm nhà cung cấp"><form method="get" action="${pageContext.request.contextPath}/app/suppliers">
    <input type="hidden" name="returnTo" value="<%=importing?"import":"find"%>"><%if(draftId!=null&&!draftId.isBlank()){%><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(draftId)%>"><%}%>
    <div class="field"><label for="supplier-search">Mã, tên hoặc địa chỉ</label><div class="inline"><input id="supplier-search" name="q" maxlength="500" value="<%=SecuritySupport.escape(q)%>" placeholder="Nhập từ khóa"><button class="btn" type="submit">Tìm</button></div></div>
  </form></section>
  <section class="card" style="margin-top:18px" aria-labelledby="supplier-list-heading">
    <div class="card-head"><h2 id="supplier-list-heading">Danh sách nhà cung cấp</h2><span class="small"><%=result.total()%> kết quả</span></div>
    <%if(result.items().isEmpty()){%><div class="empty"><strong>Không tìm thấy nhà cung cấp</strong>Thử từ khóa khác hoặc thêm nhà cung cấp mới.</div><%}else{%>
    <div class="table-wrap"><table class="table"><thead><tr><%if(!importing){%><th scope="col">Chọn</th><%}%><th scope="col">Mã</th><th scope="col">Tên</th><th scope="col">Địa chỉ</th><th scope="col">Điện thoại</th><th scope="col">Email</th><%if(importing){%><th scope="col">Thao tác</th><%}%></tr></thead><tbody>
      <%for(NhaCungCap supplier:result.items()){%><tr class="<%=importing?"":"supplier-select-row"%>" <%=importing?"":"data-supplier-row"%>>
        <%if(!importing){String accessibleName="Chọn nhà cung cấp "+supplier.getMaNCC()+" - "+supplier.getTenNCC();%><td class="supplier-radio-cell"><input type="radio" form="supplier-action-form" name="supplierId" value="<%=supplier.getId()%>" aria-label="<%=SecuritySupport.escape(accessibleName)%>" data-code="<%=SecuritySupport.escape(supplier.getMaNCC())%>" data-name="<%=SecuritySupport.escape(supplier.getTenNCC())%>"></td><%}%>
        <td class="code"><%=SecuritySupport.escape(supplier.getMaNCC())%></td><td><strong><%=SecuritySupport.escape(supplier.getTenNCC())%></strong></td><td><%=SecuritySupport.escape(supplier.getDiaChi())%></td><td><%=SecuritySupport.escape(supplier.getSoDienThoai())%></td><td><%=SecuritySupport.escape(supplier.getEmail())%></td>
        <%if(importing){%><td><a class="btn sm" href="${pageContext.request.contextPath}/app/suppliers/edit?id=<%=supplier.getId()%>&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=result.page()%>">Sửa</a></td><%}%>
      </tr><%}%>
    </tbody></table></div>
    <nav class="pagination" aria-label="Phân trang nhà cung cấp"><span class="page-count">Trang <%=result.page()%> / <%=result.pageCount()%></span>
      <%if(result.page()>1){%><a class="btn sm" href="?q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;returnTo=<%=importing?"import":"find"%>&amp;page=<%=result.page()-1%><%=draftId==null||draftId.isBlank()?"":"&amp;draftId="+java.net.URLEncoder.encode(draftId,java.nio.charset.StandardCharsets.UTF_8)%>">Trước</a><%}%>
      <%if(result.page()<result.pageCount()){%><a class="btn sm" href="?q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;returnTo=<%=importing?"import":"find"%>&amp;page=<%=result.page()+1%><%=draftId==null||draftId.isBlank()?"":"&amp;draftId="+java.net.URLEncoder.encode(draftId,java.nio.charset.StandardCharsets.UTF_8)%>">Sau</a><%}%>
    </nav><%}%>
  </section>
  <%if(importing){%>
    <div class="footer-actions" style="margin-top:16px"><a class="btn primary" href="${pageContext.request.contextPath}/app/suppliers/add?returnTo=import&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=result.page()%><%=draftId==null||draftId.isBlank()?"":"&amp;draftId="+java.net.URLEncoder.encode(draftId,java.nio.charset.StandardCharsets.UTF_8)%>">Thêm nhà cung cấp</a></div>
  <%}else{%>
    <form id="supplier-action-form" method="post" action="${pageContext.request.contextPath}/app/suppliers/action" class="card card-body supplier-actions-panel">
      <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="q" value="<%=SecuritySupport.escape(q)%>"><input type="hidden" name="page" value="<%=result.page()%>">
      <p id="supplier-selection-status" class="supplier-selection-status" role="status" aria-live="polite">Chọn một nhà cung cấp trong bảng để sửa hoặc xóa.</p>
      <div class="supplier-action-buttons"><a class="btn primary" href="${pageContext.request.contextPath}/app/suppliers/add?returnTo=find&amp;q=<%=java.net.URLEncoder.encode(q==null?"":q,java.nio.charset.StandardCharsets.UTF_8)%>&amp;page=<%=result.page()%>">Thêm NCC</a><button id="edit-selected-supplier" class="btn" type="submit" name="action" value="edit" disabled>Sửa NCC</button><button id="delete-selected-supplier" class="btn danger" type="submit" name="action" value="delete" disabled>Xóa NCC</button></div>
    </form>
    <script>
      (() => {
        const form = document.getElementById('supplier-action-form');
        const choices = Array.from(document.querySelectorAll('input[type="radio"][form="supplier-action-form"][name="supplierId"]'));
        const editButton = document.getElementById('edit-selected-supplier');
        const deleteButton = document.getElementById('delete-selected-supplier');
        const status = document.getElementById('supplier-selection-status');
        const selectedChoice = () => choices.find((choice) => choice.checked);
        const updateSelection = () => {
          const selected = selectedChoice();
          editButton.disabled = !selected;
          deleteButton.disabled = !selected;
          choices.forEach((choice) => choice.closest('tr')?.classList.toggle('is-selected', choice.checked));
          status.textContent = selected
            ? 'Đã chọn ' + selected.dataset.code + ' · ' + selected.dataset.name
            : 'Chọn một nhà cung cấp trong bảng để sửa hoặc xóa.';
        };
        choices.forEach((choice) => {
          choice.addEventListener('change', updateSelection);
          const row = choice.closest('[data-supplier-row]');
          row?.addEventListener('click', (event) => {
            if (event.target.closest('a, button, input, label')) return;
            choice.checked = true;
            updateSelection();
            choice.focus();
          });
        });
        form.addEventListener('submit', (event) => {
          if (event.submitter?.value !== 'delete') return;
          const selected = selectedChoice();
          if (!selected || !window.confirm('Xóa nhà cung cấp ' + selected.dataset.code + ' · ' + selected.dataset.name + '? Nhà cung cấp đã có phiếu nhập sẽ không thể xóa.')) {
            event.preventDefault();
          }
        });
        updateSelection();
      })();
    </script>
  <%}%>
</main></body></html>
