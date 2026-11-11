package vn.ptit.pttkm3.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import vn.ptit.pttkm3.config.ApplicationContextListener;
import vn.ptit.pttkm3.dao.NhaCungCapDao;
import vn.ptit.pttkm3.dao.SupplierReportDao;
import vn.ptit.pttkm3.model.ReturnInvoiceRevenue;
import vn.ptit.pttkm3.model.SupplierRevenue;
import vn.ptit.pttkm3.model.NhaCungCap;
import vn.ptit.pttkm3.service.SupplierReportService;
import vn.ptit.pttkm3.service.SupplierReportSnapshot;

@WebServlet("/app/reports/suppliers/*")
public final class SupplierReportServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SupplierReportServlet.class.getName());
    private SupplierReportService reports;
    private NhaCungCapDao suppliers;

    @Override
    public void init() throws ServletException {
        Object value = getServletContext().getAttribute(ApplicationContextListener.DATA_SOURCE_ATTRIBUTE);
        if (value instanceof DataSource dataSource) {
            reports = new SupplierReportService(new SupplierReportDao(dataSource));
            suppliers = new NhaCungCapDao(dataSource);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("csrfToken", SecuritySupport.csrfToken(request.getSession(false)));
        request.setAttribute("activeNav", "reports");
        response.setHeader("Cache-Control", "no-store");
        String path = request.getPathInfo() == null ? "/" : request.getPathInfo();
        try {
            if ("/details".equals(path)) {
                showDetails(request, response);
                return;
            }
            if ("/invoice".equals(path)) {
                showInvoice(request, response);
                return;
            }
            if (request.getParameter("snapshot") != null) {
                SupplierReportSnapshot saved = snapshot(request, request.getParameter("snapshot"));
                if (saved == null) { showExpired(request, response); return; }
                showReport(request, response, saved, null, true);
                return;
            }
            if (!"1".equals(request.getParameter("run"))) {
                showReport(request, response, null, null, false);
                return;
            }
            LocalDate from = parseDate(request.getParameter("from"), "Ngày bắt đầu");
            LocalDate to = parseDate(request.getParameter("to"), "Ngày kết thúc");
            if (from.isAfter(to)) throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc.");
            List<SupplierRevenue> rows = reports.summarize(from, to);
            SupplierReportSnapshot saved = ReportSnapshotRegistry.get(request.getSession(false)).put(from, to, rows);
            response.sendRedirect(request.getContextPath() + "/app/reports/suppliers?snapshot=" + saved.id());
        } catch (IllegalArgumentException error) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            showReport(request, response, null, error.getMessage(), false);
        } catch (SQLException error) {
            systemError(request, response, error);
        }
    }

    private void showDetails(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        SupplierReportSnapshot saved = snapshot(request, request.getParameter("snapshot"));
        if (saved == null) { showExpired(request, response); return; }
        long supplierId = parseId(request.getParameter("supplierId"));
        SupplierRevenue summary = summary(saved, supplierId);
        if (summary == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        NhaCungCap supplier = suppliers.find(supplierId);
        if (supplier == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        List<ReturnInvoiceRevenue> invoices = invoiceList(request, saved, supplierId);
        request.setAttribute("snapshot", saved);
        request.setAttribute("supplier", supplier);
        request.setAttribute("summary", summary);
        request.setAttribute("invoices", invoices);
        request.getRequestDispatcher("/WEB-INF/views/reports/supplier-details.jsp").forward(request, response);
    }

    private void showInvoice(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        SupplierReportSnapshot saved = snapshot(request, request.getParameter("snapshot"));
        if (saved == null) { showExpired(request, response); return; }
        long supplierId = parseId(request.getParameter("supplierId"));
        SupplierRevenue summary = summary(saved, supplierId);
        if (summary == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        long invoiceId = parseId(request.getParameter("invoiceId"));
        ReturnInvoiceRevenue invoice = invoiceList(request, saved, supplierId).stream()
                .filter(row -> row.invoiceId() == invoiceId).findFirst().orElse(null);
        if (invoice == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND, "Hóa đơn trả không thuộc nhà cung cấp hoặc kỳ thống kê này."); return; }
        NhaCungCap supplier = suppliers.find(supplierId);
        if (supplier == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        request.setAttribute("snapshot", saved);
        request.setAttribute("supplier", supplier);
        request.setAttribute("summary", summary);
        request.setAttribute("invoice", invoice);
        request.getRequestDispatcher("/WEB-INF/views/reports/supplier-invoice.jsp").forward(request, response);
    }

    private List<ReturnInvoiceRevenue> invoiceList(HttpServletRequest request, SupplierReportSnapshot snapshot,
                                                   long supplierId) throws SQLException {
        ReportSnapshotRegistry registry = ReportSnapshotRegistry.get(request.getSession(false));
        List<ReturnInvoiceRevenue> cached = registry.invoiceList(snapshot.id(), supplierId);
        if (cached != null) return cached;
        List<ReturnInvoiceRevenue> loaded = reports.details(supplierId, snapshot.from(), snapshot.to());
        registry.putInvoiceList(snapshot.id(), supplierId, loaded);
        return loaded;
    }

    private static SupplierRevenue summary(SupplierReportSnapshot snapshot, long supplierId) {
        return snapshot.rows().stream().filter(row -> row.supplierId() == supplierId).findFirst().orElse(null);
    }

    private void showReport(HttpServletRequest request, HttpServletResponse response,
                            SupplierReportSnapshot snapshot, String error, boolean hasRun)
            throws ServletException, IOException {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        request.setAttribute("snapshot", snapshot);
        request.setAttribute("from", snapshot == null ? value(request, "from", today.minusDays(30).toString()) : snapshot.from().toString());
        request.setAttribute("to", snapshot == null ? value(request, "to", today.toString()) : snapshot.to().toString());
        request.setAttribute("error", error);
        request.setAttribute("hasRun", hasRun);
        request.getRequestDispatcher("/WEB-INF/views/reports/suppliers.jsp").forward(request, response);
    }

    private void showExpired(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        request.getRequestDispatcher("/WEB-INF/views/reports/expired.jsp").forward(request, response);
    }

    private SupplierReportSnapshot snapshot(HttpServletRequest request, String token) {
        if (token == null || !token.matches("[0-9a-fA-F-]{36}")) return null;
        return ReportSnapshotRegistry.get(request.getSession(false)).get(token);
    }

    private void systemError(HttpServletRequest request, HttpServletResponse response, SQLException error)
            throws ServletException, IOException {
        String requestId = String.valueOf(request.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE));
        LOG.log(Level.SEVERE, "Supplier report failed; requestId={0}, errorType={1}, sqlState={2}, errorCode={3}",
                new Object[]{requestId, error.getClass().getSimpleName(), error.getSQLState(), error.getErrorCode()});
        request.setAttribute("requestId", requestId);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        request.getRequestDispatcher("/WEB-INF/views/reports/error.jsp").forward(request, response);
    }

    private static LocalDate parseDate(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " là bắt buộc.");
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException error) { throw new IllegalArgumentException(label + " không đúng định dạng ngày."); }
    }

    private static long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id < 1) throw new NumberFormatException();
            return id;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Mã nhà cung cấp hoặc hóa đơn không hợp lệ.");
        }
    }

    private static String value(HttpServletRequest request, String name, String fallback) {
        String value = request.getParameter(name);
        return value == null ? fallback : value;
    }
}
