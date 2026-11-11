package vn.ptit.pttkm3.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ptit.pttkm3.auth.SessionEmployee;
import vn.ptit.pttkm3.config.ApplicationContextListener;
import vn.ptit.pttkm3.dao.NhaCungCapDao;
import vn.ptit.pttkm3.model.NhaCungCap;
import vn.ptit.pttkm3.service.ImportDraft;
import vn.ptit.pttkm3.service.NhaCungCapService;

@WebServlet("/app/suppliers/*")
public final class SupplierServlet extends HttpServlet {
    private static final Logger LOG=Logger.getLogger(SupplierServlet.class.getName());
    private static final String EDITS=SupplierServlet.class.getName()+".edits";
    private NhaCungCapService service;
    @Override public void init() throws ServletException {
        Object d=getServletContext().getAttribute(ApplicationContextListener.DATA_SOURCE_ATTRIBUTE);
        if(d instanceof DataSource ds) service=new NhaCungCapService(new NhaCungCapDao(ds));
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        req.setAttribute("csrfToken",SecuritySupport.csrfToken(req.getSession(false)));
        String path=req.getPathInfo()==null?"/":req.getPathInfo();
        try {
            if("/success".equals(path)) { showSuccess(req,res); return; }
            if("/add".equals(path)) {
                String returnTo=safeReturn(req.getParameter("returnTo")),draftId=safeDraftId(req.getParameter("draftId"));
                ImportDraft activeDraft=null;
                if("import".equals(returnTo)&&!draftId.isEmpty()){
                    SessionEmployee employee=(SessionEmployee)req.getSession(false).getAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE);
                    activeDraft=DraftRegistry.get(req.getSession(false)).get(draftId,employee.getId());
                    if(activeDraft==null)draftId="";
                }
                req.setAttribute("returnTo",returnTo);req.setAttribute("draftId",draftId);req.setAttribute("draft",activeDraft);
                req.setAttribute("searchQuery",safeQuery(req.getParameter("q")));req.setAttribute("searchPage",safePage(req.getParameter("page")));
                req.setAttribute("activeNav","suppliers");req.setAttribute("activeDraftFlow",activeDraft!=null);
                req.getRequestDispatcher("/WEB-INF/views/suppliers/add.jsp").forward(req,res); return;
            }
            if("/edit".equals(path)) { showEdit(req,res); return; }
            int page=parsePage(req.getParameter("page")); String q=req.getParameter("q")==null?"":req.getParameter("q").strip();
            String returnTo=safeReturn(req.getParameter("returnTo")),draftId=safeDraftId(req.getParameter("draftId"));
            ImportDraft draft=null;
            if("import".equals(returnTo)&&!draftId.isEmpty()){
                SessionEmployee employee=(SessionEmployee)req.getSession(false).getAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE);
                draft=DraftRegistry.get(req.getSession(false)).get(draftId,employee.getId());
                if(draft==null)draftId="";
            }
            NhaCungCapDao.Page result=service.search(q,page);
            req.setAttribute("result",result);req.setAttribute("q",q);req.setAttribute("returnTo",returnTo);req.setAttribute("draftId",draftId);req.setAttribute("draft",draft);
            req.setAttribute("activeNav","suppliers");req.setAttribute("activeDraftFlow",draft!=null);
            req.getRequestDispatcher("/WEB-INF/views/suppliers/list.jsp").forward(req,res);
        } catch (IllegalArgumentException e) { res.sendError(400); }
        catch(SQLException e) { systemError(req,res,e); }
    }
    private void showEdit(HttpServletRequest req,HttpServletResponse res) throws SQLException,ServletException,IOException {
        long id=parseId(req.getParameter("id")); NhaCungCap n=service.find(id);
        if(n==null){res.sendError(404);return;}
        HttpSession session=req.getSession(false); @SuppressWarnings("unchecked") Map<String,EditContext> edits=(Map<String,EditContext>)session.getAttribute(EDITS);
        if(edits==null){edits=new LinkedHashMap<>();session.setAttribute(EDITS,edits);}
        String token=UUID.randomUUID().toString(); synchronized(edits){if(edits.size()>=64)edits.remove(edits.keySet().iterator().next());edits.put(token,new EditContext(id,n.getVersion(),n));}
        req.setAttribute("supplier",n);req.setAttribute("editToken",token);req.setAttribute("activeNav","suppliers");
        req.setAttribute("returnQuery",safeQuery(req.getParameter("q")));req.setAttribute("returnPage",safePage(req.getParameter("page")));
        req.getRequestDispatcher("/WEB-INF/views/suppliers/edit.jsp").forward(req,res);
    }
    private void showSuccess(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        String type="edit".equals(req.getParameter("type"))?"edit":"add";
        String returnTo=safeReturn(req.getParameter("returnTo")),draftId=safeDraftId(req.getParameter("draftId"));
        ImportDraft activeDraft=null;
        if("add".equals(type)&&"import".equals(returnTo)&&!draftId.isEmpty()){
            SessionEmployee employee=(SessionEmployee)req.getSession(false).getAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE);
            activeDraft=DraftRegistry.get(req.getSession(false)).get(draftId,employee.getId());
            if(activeDraft==null)draftId="";
        }
        req.setAttribute("successType",type);req.setAttribute("returnTo",returnTo);req.setAttribute("draftId",draftId);req.setAttribute("draft",activeDraft);
        req.setAttribute("searchQuery",safeQuery(req.getParameter("q")));req.setAttribute("searchPage",safePage(req.getParameter("page")));
        req.setAttribute("activeNav","edit".equals(type)?"suppliers":"import".equals(returnTo)?"imports":"suppliers");
        req.setAttribute("activeDraftFlow",activeDraft!=null);
        req.getRequestDispatcher("/WEB-INF/views/suppliers/success.jsp").forward(req,res);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        req.setAttribute("csrfToken",SecuritySupport.csrfToken(req.getSession(false)));
        String path=req.getPathInfo()==null?"/":req.getPathInfo();
        if("/add".equals(path)){create(req,res);return;}
        if("/edit".equals(path)){update(req,res);return;}
        if("/action".equals(path)){manageAction(req,res);return;}
        res.sendError(404);
    }
    private void manageAction(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        String action=value(req,"action"),query=safeQuery(req.getParameter("q"));
        int page=safePage(req.getParameter("page"));
        long id;
        try { id=parseId(req.getParameter("supplierId")); }
        catch(RuntimeException e) {
            setSupplierFlash(req,"error","Yêu cầu không hợp lệ. Hãy chọn một nhà cung cấp rồi thử lại.");
            redirectToList(req,res,query,page);return;
        }
        if(!"edit".equals(action)&&!"delete".equals(action)) {
            setSupplierFlash(req,"error","Thao tác không hợp lệ. Hãy tải lại danh sách và thử lại.");
            redirectToList(req,res,query,page);return;
        }
        try {
            if("edit".equals(action)) {
                if(service.find(id)==null) {
                    setSupplierFlash(req,"error","Nhà cung cấp không còn tồn tại. Danh sách đã được cập nhật.");
                    redirectToList(req,res,query,page);return;
                }
                String editUrl=req.getContextPath()+"/app/suppliers/edit?id="+id
                        +"&q="+java.net.URLEncoder.encode(query,java.nio.charset.StandardCharsets.UTF_8)
                        +"&page="+page;
                res.sendRedirect(editUrl);return;
            }
            switch(service.delete(id)) {
                case DELETED -> setSupplierFlash(req,"success","Đã xóa nhà cung cấp thành công.");
                case IN_USE -> setSupplierFlash(req,"error","Không thể xóa nhà cung cấp vì đã có phiếu nhập liên quan.");
                case NOT_FOUND -> setSupplierFlash(req,"error","Nhà cung cấp không còn tồn tại. Danh sách đã được cập nhật.");
            }
            redirectToList(req,res,query,page);
        } catch(SQLException e) {
            logSqlFailure(req,e,action);systemError(req,res,e);
        }
    }
    private static void setSupplierFlash(HttpServletRequest req,String type,String message) {
        HttpSession session=req.getSession(false);
        if(session!=null) {
            session.setAttribute("supplierFlash",message);
            session.setAttribute("supplierFlashType",type);
        }
    }
    private static void redirectToList(HttpServletRequest req,HttpServletResponse res,String query,int page)throws IOException {
        String url=req.getContextPath()+"/app/suppliers?q="
                +java.net.URLEncoder.encode(query,java.nio.charset.StandardCharsets.UTF_8)+"&page="+page;
        res.sendRedirect(url);
    }
    private void create(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        String name=value(req,"name"),address=value(req,"address"),phone=value(req,"phone"),email=value(req,"email"),returnTo=safeReturn(req.getParameter("returnTo")),draftId=safeDraftId(req.getParameter("draftId")),query=safeQuery(req.getParameter("q")); int page=safePage(req.getParameter("page"));
        try { service.create(name,address,phone,email); res.sendRedirect(req.getContextPath()+"/app/suppliers/success?type=add&returnTo="+returnTo+"&q="+java.net.URLEncoder.encode(query,java.nio.charset.StandardCharsets.UTF_8)+"&page="+page+(draftId.isEmpty()?"":"&draftId="+draftId)); }
        catch(IllegalArgumentException e){res.setStatus(400);req.setAttribute("error",e.getMessage());addValues(req,name,address,phone,email,returnTo,draftId);req.getRequestDispatcher("/WEB-INF/views/suppliers/add.jsp").forward(req,res);}
        catch(NhaCungCapService.DuplicateSupplierException e){res.setStatus(409);req.setAttribute("error","Số điện thoại hoặc email đã được dùng cho nhà cung cấp khác.");addValues(req,name,address,phone,email,returnTo,draftId);req.getRequestDispatcher("/WEB-INF/views/suppliers/add.jsp").forward(req,res);}
        catch(SQLException e){logSqlFailure(req,e,"create");addValues(req,name,address,phone,email,returnTo,draftId);req.setAttribute("error","Không lưu được nhà cung cấp do lỗi CSDL. Nội dung đã nhập được giữ lại; mã yêu cầu "+req.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE)+".");res.setStatus(503);req.getRequestDispatcher("/WEB-INF/views/suppliers/add.jsp").forward(req,res);}
    }
    private void update(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        String token=req.getParameter("editToken"); HttpSession session=req.getSession(false);
        @SuppressWarnings("unchecked") Map<String,EditContext> edits=session==null?null:(Map<String,EditContext>)session.getAttribute(EDITS);
        EditContext context=edits==null?null:edits.get(token);
        long postedId,postedVersion;
        try { postedId=parseId(req.getParameter("id"));postedVersion=Long.parseLong(req.getParameter("version")); }
        catch(RuntimeException e){res.sendError(400);return;}
        if(context==null||context.id()!=postedId||context.version()!=postedVersion){res.sendError(400,"Biểu mẫu sửa NCC không còn hợp lệ.");return;}
        String name=value(req,"name"),address=value(req,"address"),phone=value(req,"phone"),email=value(req,"email");
        try {
            if(!service.update(context.id(),context.version(),name,address,phone,email)){
                NhaCungCap latest=service.find(context.id());
                if(latest==null){res.sendError(HttpServletResponse.SC_NOT_FOUND,"Nhà cung cấp không còn tồn tại.");return;}
                req.setAttribute("error","NCC đã được người khác cập nhật. Tải thông tin mới trước khi sửa tiếp.");
                req.setAttribute("supplier",latest);req.setAttribute("editToken",token);req.setAttribute("oldName",name);req.setAttribute("oldAddress",address);req.setAttribute("oldPhone",phone);req.setAttribute("oldEmail",email);req.setAttribute("stale",true);
                req.setAttribute("activeNav","suppliers");
                req.setAttribute("returnQuery",safeQuery(req.getParameter("returnQuery")));req.setAttribute("returnPage",safePage(req.getParameter("returnPage")));
                res.setStatus(409);req.getRequestDispatcher("/WEB-INF/views/suppliers/edit.jsp").forward(req,res);return;
            }
            synchronized(edits){edits.remove(token);}res.sendRedirect(req.getContextPath()+"/app/suppliers/success?type=edit");
        } catch(IllegalArgumentException e){renderEditError(req,res,context,token,name,address,phone,email,e.getMessage(),400);}
        catch(NhaCungCapService.DuplicateSupplierException e){renderEditError(req,res,context,token,name,address,phone,email,"Số điện thoại hoặc email đã được dùng cho nhà cung cấp khác.",409);}
        catch(SQLException e){logSqlFailure(req,e,"update");req.setAttribute("supplier",context.snapshot());req.setAttribute("editToken",token);req.setAttribute("oldName",name);req.setAttribute("oldAddress",address);req.setAttribute("oldPhone",phone);req.setAttribute("oldEmail",email);req.setAttribute("error","Không lưu được nhà cung cấp do lỗi CSDL. Nội dung đã nhập được giữ lại; mã yêu cầu "+req.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE)+".");res.setStatus(503);req.getRequestDispatcher("/WEB-INF/views/suppliers/edit.jsp").forward(req,res);}
    }
    private void renderEditError(HttpServletRequest req,HttpServletResponse res,EditContext c,String token,String n,String a,String p,String e,String message,int status)throws ServletException,IOException{
        NhaCungCap latest=null;try{latest=service.find(c.id());}catch(SQLException ignored){}req.setAttribute("supplier",latest==null?c.snapshot():latest);
        req.setAttribute("editToken",token);req.setAttribute("oldName",n);req.setAttribute("oldAddress",a);req.setAttribute("oldPhone",p);req.setAttribute("oldEmail",e);req.setAttribute("error",message);req.setAttribute("activeNav","suppliers");req.setAttribute("returnQuery",safeQuery(req.getParameter("returnQuery")));req.setAttribute("returnPage",safePage(req.getParameter("returnPage")));res.setStatus(status);req.getRequestDispatcher("/WEB-INF/views/suppliers/edit.jsp").forward(req,res);
    }
    private void logSqlFailure(HttpServletRequest req,SQLException error,String action){String id=String.valueOf(req.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE));LOG.log(Level.SEVERE,"Supplier {0} failed; requestId={1}, errorType={2}, sqlState={3}, errorCode={4}",new Object[]{action,id,error.getClass().getSimpleName(),error.getSQLState(),error.getErrorCode()});}
    private void systemError(HttpServletRequest req,HttpServletResponse res,SQLException e)throws ServletException,IOException{
        String id=String.valueOf(req.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE));LOG.log(Level.SEVERE,"Supplier operation failed; requestId={0}, errorType={1}, sqlState={2}, errorCode={3}",new Object[]{id,e.getClass().getSimpleName(),e.getSQLState(),e.getErrorCode()});
        req.setAttribute("requestId",id);req.setAttribute("activeNav","suppliers");res.setStatus(503);req.getRequestDispatcher("/WEB-INF/views/suppliers/error.jsp").forward(req,res);
    }
    private static void addValues(HttpServletRequest r,String n,String a,String p,String e,String back,String draft){
        r.setAttribute("name",n);r.setAttribute("address",a);r.setAttribute("phone",p);r.setAttribute("email",e);r.setAttribute("returnTo",back);r.setAttribute("draftId",draft);
        ImportDraft activeDraft=null;
        if("import".equals(back)&&draft!=null&&!draft.isBlank()){
            SessionEmployee employee=(SessionEmployee)r.getSession(false).getAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE);
            activeDraft=DraftRegistry.get(r.getSession(false)).get(draft,employee.getId());
        }
        r.setAttribute("draft",activeDraft);r.setAttribute("activeNav","suppliers");r.setAttribute("activeDraftFlow",activeDraft!=null);
    }
    private static String value(HttpServletRequest r,String k){String s=r.getParameter(k);return s==null?"":s;}
    private static String safeReturn(String r){return "import".equals(r)?"import":"find";}
    private static String safeDraftId(String id){return id!=null&&id.matches("[0-9a-fA-F-]{36}")?id:"";}
    private static String safeQuery(String query){return query==null?"":query.strip().substring(0,Math.min(query.strip().length(),500));}
    private static int safePage(String raw){try{int page=raw==null?1:Integer.parseInt(raw);return Math.max(1,Math.min(page,1_000_000));}catch(RuntimeException error){return 1;}}
    private static int parsePage(String x){int p=x==null?1:Integer.parseInt(x);if(p<1)throw new IllegalArgumentException();return p;}
    private static long parseId(String x){long id=Long.parseLong(x);if(id<1)throw new IllegalArgumentException();return id;}
    private record EditContext(long id,long version,NhaCungCap snapshot) { }
}
