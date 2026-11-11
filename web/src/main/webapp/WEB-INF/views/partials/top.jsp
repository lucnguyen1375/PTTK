<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="vn.ptit.pttkm3.auth.SessionEmployee,vn.ptit.pttkm3.service.ImportDraft,vn.ptit.pttkm3.web.AuthenticationFilter,vn.ptit.pttkm3.web.SecuritySupport" %>
<%
  SessionEmployee currentEmployee=(SessionEmployee)session.getAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE);
  String activeNav=(String)request.getAttribute("activeNav");
  boolean activeDraftFlow=Boolean.TRUE.equals(request.getAttribute("activeDraftFlow"));
  ImportDraft currentDraft=(ImportDraft)request.getAttribute("draft");
  String activeDraftId=currentDraft==null?(String)request.getAttribute("draftId"):currentDraft.id();
  if(activeDraftId==null||activeDraftId.isBlank()) activeDraftId=request.getParameter("draftId");
  if(activeDraftId==null||activeDraftId.isBlank()) activeDraftId=request.getParameter("id");
  if(activeDraftId==null) activeDraftId="";
%>
<header class="top">
  <% if(activeDraftFlow){ %>
    <span class="brand"><span class="logo" aria-hidden="true">T</span><span><b>Truyện Nhà Mình</b><small>Đang lập phiếu nhập</small></span></span>
  <% } else { %>
    <a class="brand" href="${pageContext.request.contextPath}/app/home" aria-label="Truyện Nhà Mình, trang chính"><span class="logo" aria-hidden="true">T</span><span><b>Truyện Nhà Mình</b><small>Quản lý cửa hàng</small></span></a>
  <% } %>
  <nav class="main-nav" aria-label="Điều hướng chính">
    <% if(activeDraftFlow){ %>
      <a href="${pageContext.request.contextPath}/app/imports/draft?id=<%=SecuritySupport.escape(activeDraftId)%>" <%= "imports".equals(activeNav)?"aria-current=\"page\"":"" %>>Phiếu nhập</a>
      <% String[][] exits={{"suppliers","Nhà cung cấp"},{"reports","Thống kê"},{"home","Trang chính"}}; for(String[] exit:exits){ %>
      <form method="post" action="${pageContext.request.contextPath}/app/imports/cancel" onsubmit="return confirm('Rời phiếu nhập và xóa toàn bộ nội dung chưa lưu?')">
        <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><input type="hidden" name="draftId" value="<%=SecuritySupport.escape(activeDraftId)%>"><input type="hidden" name="confirmed" value="1"><input type="hidden" name="returnTo" value="<%=exit[0]%>">
        <button class="nav-link" type="submit"><%=exit[1]%></button>
      </form>
      <% } %>
    <% } else { %>
      <a href="${pageContext.request.contextPath}/app/suppliers" <%= "suppliers".equals(activeNav)?"aria-current=\"page\"":"" %>>Nhà cung cấp</a>
      <a href="${pageContext.request.contextPath}/app/imports" <%= "imports".equals(activeNav)?"aria-current=\"page\"":"" %>>Nhập truyện</a>
      <a href="${pageContext.request.contextPath}/app/reports/suppliers" <%= "reports".equals(activeNav)?"aria-current=\"page\"":"" %>>Thống kê</a>
    <% } %>
  </nav>
  <% if(currentEmployee!=null) { %>
  <div class="person">
    <span class="avatar" aria-hidden="true"><%=SecuritySupport.escape(currentEmployee.getFullName().substring(0,1))%></span>
    <span><b><%=SecuritySupport.escape(currentEmployee.getFullName())%></b><br><span class="role muted"><%=SecuritySupport.escape(currentEmployee.getCode())%> · <%=SecuritySupport.escape(currentEmployee.getRole())%></span></span>
    <form method="post" action="${pageContext.request.contextPath}/logout" onsubmit="<%=activeDraftFlow?"return confirm('Đăng xuất và xóa phiếu nhập chưa lưu?')":""%>">
      <input type="hidden" name="_csrf" value="<%=SecuritySupport.escape((String)request.getAttribute("csrfToken"))%>"><button class="signout" type="submit">Đăng xuất</button>
    </form>
  </div>
  <% } %>
</header>
