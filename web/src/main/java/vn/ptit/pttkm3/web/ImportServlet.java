package vn.ptit.pttkm3.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.ptit.pttkm3.auth.SessionEmployee;
import vn.ptit.pttkm3.config.ApplicationContextListener;
import vn.ptit.pttkm3.dao.NhaCungCapDao;
import vn.ptit.pttkm3.model.DauTruyen;
import vn.ptit.pttkm3.model.NhaCungCap;
import vn.ptit.pttkm3.service.ImportDraft;
import vn.ptit.pttkm3.service.ImportService;

@WebServlet("/app/imports/*")
public final class ImportServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(ImportServlet.class.getName());
    private static final String DRAFT_VIEW = "/WEB-INF/views/imports/draft.jsp";
    private static final String TITLES_VIEW = "/WEB-INF/views/imports/titles.jsp";
    private static final String NEW_TITLE_VIEW = "/WEB-INF/views/imports/title-new.jsp";
    private ImportService imports;
    private NhaCungCapDao suppliers;

    @Override
    public void init() throws ServletException {
        Object value = getServletContext().getAttribute(ApplicationContextListener.DATA_SOURCE_ATTRIBUTE);
        if (value instanceof DataSource dataSource) {
            imports = new ImportService(dataSource);
            suppliers = new NhaCungCapDao(dataSource);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCsrf(request);
        response.setHeader("Cache-Control", "no-store");
        String path = path(request);
        try {
            switch (path) {
                case "/draft" -> showDraft(request, response, draft(request));
                case "/titles" -> showTitles(request, response, draft(request));
                case "/titles/new" -> showNewTitle(request, response, draft(request));
                case "/receipt" -> showReceipt(request, response);
                default -> showSuppliers(request, response);
            }
        } catch (IllegalArgumentException error) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thông tin yêu cầu không hợp lệ.");
        } catch (SQLException error) {
            systemError(request, response, error, request.getParameter("id"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCsrf(request);
        String path = path(request);
        SessionEmployee employee = employee(request);
        try {
            switch (path) {
                case "/create" -> createDraft(request, response, employee);
                case "/title/stage" -> stageTitle(request, response, employee);
                case "/line/add-existing" -> addExisting(request, response, employee);
                case "/line/add-staged" -> addStaged(request, response, employee);
                case "/line/edit" -> editLine(request, response, employee);
                case "/line/remove" -> removeLine(request, response, employee);
                case "/cancel" -> cancelDraft(request, response, employee);
                case "/change-supplier" -> changeSupplier(request, response, employee);
                case "/save" -> save(request, response, employee);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (ImportDraft.StaleDraftException error) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            request.setAttribute("error", "Phiếu đã thay đổi ở tab khác. Hãy kiểm tra nội dung mới nhất trước khi tiếp tục.");
            renderAfterPost(request, response, path);
        } catch (java.util.NoSuchElementException error) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            request.setAttribute("error", "Dòng phiếu không còn tồn tại. Nội dung hiện tại đã được giữ lại.");
            renderAfterPost(request, response, path);
        } catch (IllegalArgumentException error) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", error.getMessage());
            renderAfterPost(request, response, path);
        } catch (SQLException error) {
            systemError(request, response, error, value(request, "draftId"));
        }
    }

    private void createDraft(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws SQLException, IOException {
        long supplierId = parsePositiveLong(request.getParameter("supplierId"), "Nhà cung cấp");
        if (!imports.supplierExists(supplierId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nhà cung cấp không còn tồn tại.");
            return;
        }
        ImportDraft draft = new ImportDraft(UUID.randomUUID().toString(), employee.getId(), supplierId);
        DraftRegistry.get(request.getSession(false)).put(draft);
        response.sendRedirect(request.getContextPath() + "/app/imports/draft?id=" + draft.id());
    }

    private void stageTitle(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws SQLException, ServletException, IOException {
        ImportDraft draft = requireDraft(request, employee);
        String name = value(request, "name").strip();
        String author = value(request, "author").strip();
        String category = value(request, "category").strip();
        String publisher = value(request, "publisher").strip();
        Short year = parseYear(request.getParameter("year"));
        imports.ensureTitleNew(name, author, category, publisher, year);
        draft.stageTitle(name, author, category, publisher, year, version(request));
        response.sendRedirect(request.getContextPath() + "/app/imports/titles?id=" + draft.id()
                + "&q=" + encode(value(request, "q")) + "&page=" + page(request.getParameter("page")) + "&staged=1");
    }

    private void addExisting(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws SQLException, ServletException, IOException {
        ImportDraft draft = requireDraft(request, employee);
        long titleId = parsePositiveLong(request.getParameter("titleId"), "Đầu truyện");
        DauTruyen title = imports.findTitle(titleId);
        if (title == null) {
            throw new IllegalArgumentException("Đầu truyện không còn tồn tại. Hãy tìm lại trước khi thêm vào phiếu.");
        }
        ImportDraft.Title item = new ImportDraft.Title("existing:" + titleId, titleId, title.maDauTruyen(),
                title.tenDauTruyen(), title.tacGia(), title.theLoai(), title.nhaXuatBan(), title.namXuatBan());
        draft.add(item, quantity(request), money(request, "cost"), money(request, "rent"), version(request));
        redirectDraft(request, response, draft);
    }

    private void addStaged(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws IOException {
        ImportDraft draft = requireDraft(request, employee);
        String reference = value(request, "titleRef");
        ImportDraft.Title item = draft.stagedTitle(reference);
        if (item == null) {
            throw new IllegalArgumentException("Đầu truyện tạm không còn trong phiếu này. Hãy thêm lại đầu truyện.");
        }
        draft.add(item, quantity(request), money(request, "cost"), money(request, "rent"), version(request));
        redirectDraft(request, response, draft);
    }

    private void editLine(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws IOException {
        ImportDraft draft = requireDraft(request, employee);
        draft.edit(value(request, "lineKey"), quantity(request), money(request, "cost"),
                money(request, "rent"), version(request));
        redirectDraft(request, response, draft);
    }

    private void removeLine(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws IOException {
        ImportDraft draft = requireDraft(request, employee);
        draft.remove(value(request, "lineKey"), version(request));
        redirectDraft(request, response, draft);
    }

    private void cancelDraft(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws IOException {
        ImportDraft draft = requireDraft(request, employee);
        if (!"1".equals(request.getParameter("confirmed"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cần xác nhận hủy phiếu nhập.");
            return;
        }
        DraftRegistry.get(request.getSession(false)).remove(draft.id());
        String destination = switch (value(request, "returnTo")) {
            case "suppliers" -> "/app/suppliers";
            case "reports" -> "/app/reports/suppliers";
            default -> "/app/home";
        };
        response.sendRedirect(request.getContextPath() + destination);
    }

    private void changeSupplier(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws SQLException, IOException {
        ImportDraft old = requireDraft(request, employee);
        if (!"1".equals(request.getParameter("confirmed"))) {
            throw new IllegalArgumentException("Hãy xác nhận bỏ phiếu tạm trước khi đổi nhà cung cấp.");
        }
        long supplierId = parsePositiveLong(request.getParameter("supplierId"), "Nhà cung cấp");
        if (!imports.supplierExists(supplierId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nhà cung cấp không còn tồn tại.");
            return;
        }
        DraftRegistry registry = DraftRegistry.get(request.getSession(false));
        registry.remove(old.id());
        ImportDraft replacement = new ImportDraft(UUID.randomUUID().toString(), employee.getId(), supplierId);
        registry.put(replacement);
        response.sendRedirect(request.getContextPath() + "/app/imports/draft?id=" + replacement.id());
    }

    private void save(HttpServletRequest request, HttpServletResponse response, SessionEmployee employee)
            throws SQLException, ServletException, IOException {
        String id = value(request, "draftId");
        long expected = version(request);
        DraftRegistry registry = DraftRegistry.get(request.getSession(false));
        ImportService.Receipt previous = imports.findReceipt(id, employee.getId());
        if (previous != null) {
            registry.remove(id);
            response.sendRedirect(request.getContextPath() + "/app/imports/receipt?id=" + previous.id());
            return;
        }
        ImportDraft draft = registry.get(id, employee.getId());
        if (draft == null) {
            showExpired(request, response);
            return;
        }
        ImportDraft.Snapshot snapshot = draft.beginSave(expected);
        try {
            ImportService.Receipt receipt = imports.commit(snapshot);
            registry.remove(id);
            response.sendRedirect(request.getContextPath() + "/app/imports/receipt?id=" + receipt.id());
        } catch (IllegalArgumentException error) {
            draft.endSave();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", error.getMessage());
            showDraft(request, response, draft);
        } catch (SQLException | RuntimeException error) {
            draft.endSave();
            throw error;
        }
    }

    private void showSuppliers(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String draftId = value(request, "draftId");
        ImportDraft draft = draftId.isBlank() ? null : draft(request);
        if (!draftId.isBlank() && draft == null) {
            showExpired(request, response);
            return;
        }
        String query = value(request, "q").strip();
        int page = page(request.getParameter("page"));
        NhaCungCapDao.Page result = suppliers.searchSelection(query, page);
        request.setAttribute("suppliers", result);
        request.setAttribute("q", query);
        request.setAttribute("draft", draft);
        request.setAttribute("draftId", draft == null ? "" : draft.id());
        request.setAttribute("activeNav", "imports");
        request.setAttribute("activeDraftFlow", draft != null);
        request.getRequestDispatcher("/WEB-INF/views/imports/suppliers.jsp").forward(request, response);
    }

    private void showDraft(HttpServletRequest request, HttpServletResponse response, ImportDraft draft)
            throws SQLException, ServletException, IOException {
        if (draft == null) {
            showExpired(request, response);
            return;
        }
        NhaCungCap supplier = suppliers.find(draft.supplierId());
        if (supplier == null) {
            request.setAttribute("error", "Nhà cung cấp của phiếu không còn tồn tại. Hãy chọn nhà cung cấp khác.");
        }
        request.setAttribute("draft", draft);
        request.setAttribute("supplier", supplier);
        request.setAttribute("employee", employee(request));
        request.setAttribute("editKey", value(request, "editKey"));
        request.setAttribute("activeNav", "imports");
        request.setAttribute("activeDraftFlow", true);
        request.getRequestDispatcher(DRAFT_VIEW).forward(request, response);
    }

    private void showTitles(HttpServletRequest request, HttpServletResponse response, ImportDraft draft)
            throws SQLException, ServletException, IOException {
        if (draft == null) {
            showExpired(request, response);
            return;
        }
        String query = value(request, "q").strip();
        int pageNumber = page(request.getParameter("page"));
        vn.ptit.pttkm3.dao.DauTruyenDao.Page titlePage = imports.searchTitles(query, pageNumber);
        request.setAttribute("draft", draft);
        request.setAttribute("titlePage", titlePage);
        request.setAttribute("titleQuery", query);
        request.setAttribute("staged", "1".equals(request.getParameter("staged")));
        request.setAttribute("activeNav", "imports");
        request.setAttribute("activeDraftFlow", true);
        request.getRequestDispatcher(TITLES_VIEW).forward(request, response);
    }

    private void showNewTitle(HttpServletRequest request, HttpServletResponse response, ImportDraft draft)
            throws ServletException, IOException {
        if (draft == null) {
            showExpired(request, response);
            return;
        }
        request.setAttribute("draft", draft);
        request.setAttribute("titleQuery", value(request, "q"));
        request.setAttribute("titlePageNumber", page(request.getParameter("page")));
        request.setAttribute("activeNav", "imports");
        request.setAttribute("activeDraftFlow", true);
        request.getRequestDispatcher(NEW_TITLE_VIEW).forward(request, response);
    }

    private void showReceipt(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        long id = parsePositiveLong(request.getParameter("id"), "Phiếu nhập");
        SessionEmployee employee = employee(request);
        ImportService.Receipt receipt = imports.receipt(id, employee.getId());
        if (receipt == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy phiếu nhập.");
            return;
        }
        request.setAttribute("receipt", receipt);
        request.setAttribute("supplier", suppliers.find(receipt.supplierId()));
        request.setAttribute("activeNav", "imports");
        request.getRequestDispatcher("/WEB-INF/views/imports/receipt.jsp").forward(request, response);
    }

    private void renderAfterPost(HttpServletRequest request, HttpServletResponse response, String path)
            throws ServletException, IOException {
        try {
            if ("/title/stage".equals(path)) {
                showNewTitle(request, response, draft(request));
            } else if ("/line/add-existing".equals(path) || "/line/add-staged".equals(path)) {
                request.setAttribute("quantity", value(request, "quantity"));
                request.setAttribute("cost", value(request, "cost"));
                request.setAttribute("rent", value(request, "rent"));
                request.setAttribute("selectedTitleId", value(request, "titleId"));
                request.setAttribute("selectedTitleRef", value(request, "titleRef"));
                showTitles(request, response, draft(request));
            } else if ("/line/edit".equals(path)) {
                request.setAttribute("editKey", value(request, "lineKey"));
                request.setAttribute("editQuantity", value(request, "quantity"));
                request.setAttribute("editCost", value(request, "cost"));
                request.setAttribute("editRent", value(request, "rent"));
                showDraft(request, response, draft(request));
            } else {
                showDraft(request, response, draft(request));
            }
        } catch (SQLException error) {
            systemError(request, response, error, value(request, "draftId"));
        }
    }

    private ImportDraft requireDraft(HttpServletRequest request, SessionEmployee currentEmployee) {
        ImportDraft draft = draft(request);
        if (draft == null || draft.employeeId() != currentEmployee.getId()) {
            throw new IllegalArgumentException("Phiếu tạm đã hết hạn hoặc không thuộc phiên làm việc này.");
        }
        return draft;
    }

    private ImportDraft draft(HttpServletRequest request) {
        String id = value(request, "draftId");
        if (id.isBlank()) id = value(request, "id");
        if (!id.matches("[0-9a-fA-F-]{36}")) return null;
        return DraftRegistry.get(request.getSession(false)).get(id, employee(request).getId());
    }

    private void showExpired(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        request.setAttribute("activeNav", "imports");
        request.setAttribute("error", "Phiếu tạm đã hết hạn hoặc không còn trong phiên làm việc này. Chọn lại nhà cung cấp để bắt đầu phiếu mới.");
        request.getRequestDispatcher("/WEB-INF/views/imports/expired.jsp").forward(request, response);
    }

    private void systemError(HttpServletRequest request, HttpServletResponse response, SQLException error, String draftId)
            throws ServletException, IOException {
        String requestId = String.valueOf(request.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE));
        LOG.log(Level.SEVERE, "Import operation failed; requestId={0}, errorType={1}, sqlState={2}, errorCode={3}",
                new Object[]{requestId, error.getClass().getSimpleName(), error.getSQLState(), error.getErrorCode()});
        request.setAttribute("requestId", requestId);
        request.setAttribute("draftId", draftId);
        request.setAttribute("activeNav", "imports");
        request.setAttribute("activeDraftFlow", draftId != null && !draftId.isBlank());
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        request.getRequestDispatcher("/WEB-INF/views/imports/error.jsp").forward(request, response);
    }

    private void setCsrf(HttpServletRequest request) {
        request.setAttribute("csrfToken", SecuritySupport.csrfToken(request.getSession(false)));
    }

    private static String path(HttpServletRequest request) {
        return request.getPathInfo() == null ? "/" : request.getPathInfo();
    }

    private static SessionEmployee employee(HttpServletRequest request) {
        return (SessionEmployee) request.getSession(false).getAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE);
    }

    private static String value(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        return value == null ? "" : value;
    }

    private static int page(String raw) {
        if (raw == null || raw.isBlank()) return 1;
        try { int value = Integer.parseInt(raw); return value < 1 ? 1 : value; }
        catch (NumberFormatException error) { return 1; }
    }

    private static long parsePositiveLong(String raw, String label) {
        try {
            long number = Long.parseLong(raw);
            if (number < 1) throw new NumberFormatException();
            return number;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException(label + " không hợp lệ.");
        }
    }

    private static long version(HttpServletRequest request) {
        try {
            long number = Long.parseLong(request.getParameter("version"));
            if (number < 0) throw new NumberFormatException();
            return number;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Phiên bản phiếu không hợp lệ. Hãy tải lại phiếu.");
        }
    }

    private static int quantity(HttpServletRequest request) {
        try {
            int number = Integer.parseInt(request.getParameter("quantity"));
            if (number < 1 || number > 10_000) throw new NumberFormatException();
            return number;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Số lượng phải từ 1 đến 10.000.");
        }
    }

    private static BigDecimal money(HttpServletRequest request, String key) {
        try {
            BigDecimal number = new BigDecimal(request.getParameter(key)).stripTrailingZeros();
            BigDecimal max = new BigDecimal("999999999999");
            if (number.scale() > 0 || number.compareTo(BigDecimal.ONE) < 0 || number.compareTo(max) > 0) {
                throw new NumberFormatException();
            }
            return number.setScale(0);
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Giá nhập và giá thuê phải là số VND nguyên dương, tối đa 999.999.999.999.");
        }
    }

    private static Short parseYear(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int year = Integer.parseInt(raw);
            if (year < 1 || year > Year.now().getValue()) throw new NumberFormatException();
            return (short) year;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Năm xuất bản phải từ 1 đến năm hiện tại.");
        }
    }

    private void redirectDraft(HttpServletRequest request, HttpServletResponse response, ImportDraft draft)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/app/imports/draft?id=" + draft.id());
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
