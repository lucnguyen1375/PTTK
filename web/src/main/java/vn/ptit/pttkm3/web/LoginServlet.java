package vn.ptit.pttkm3.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.ptit.pttkm3.auth.AuthenticationService;
import vn.ptit.pttkm3.auth.EmployeeAccountDao;
import vn.ptit.pttkm3.auth.LoginAttemptLimiter;
import vn.ptit.pttkm3.auth.SessionEmployee;
import vn.ptit.pttkm3.config.ApplicationContextListener;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/login")
public final class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private AuthenticationService authentication;

    @Override
    public void init() throws ServletException {
        Object source = getServletContext().getAttribute(ApplicationContextListener.DATA_SOURCE_ATTRIBUTE);
        if (source instanceof DataSource dataSource) {
            authentication = new AuthenticationService(new EmployeeAccountDao(dataSource), new LoginAttemptLimiter());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        HttpSession existing = request.getSession(false);
        if (existing != null && existing.getAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE) instanceof SessionEmployee) {
            response.sendRedirect(request.getContextPath() + "/app/home");
            return;
        }
        HttpSession session = request.getSession(true);
        request.setAttribute("csrfToken", SecuritySupport.csrfToken(session));
        if ("1".equals(request.getParameter("expired"))) {
            request.setAttribute("loginMessage", "Phiên làm việc đã hết hạn. Hãy đăng nhập lại.");
            request.setAttribute("messageType", "info");
        } else if ("1".equals(request.getParameter("loggedOut"))) {
            request.setAttribute("loginMessage", "Bạn đã đăng xuất thành công.");
            request.setAttribute("messageType", "info");
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        if (!SecuritySupport.validCsrf(request, session)) {
            request.setAttribute("loginMessage", "Biểu mẫu đã hết hạn. Hãy tải lại trang rồi thử lại.");
            request.setAttribute("messageType", "error");
            forwardLogin(request, response, HttpServletResponse.SC_FORBIDDEN, session);
            return;
        }

        String username = request.getParameter("username");
        String passwordValue = request.getParameter("password");
        if (username == null || username.strip().isEmpty() || username.strip().length() > 120
                || passwordValue == null || passwordValue.isEmpty() || passwordValue.length() > 1024) {
            request.setAttribute("loginMessage", "Nhập tên đăng nhập và mật khẩu hợp lệ để tiếp tục.");
            request.setAttribute("messageType", "error");
            request.setAttribute("username", username == null ? "" : username.strip());
            forwardLogin(request, response, HttpServletResponse.SC_BAD_REQUEST, session);
            return;
        }

        char[] password = passwordValue.toCharArray();
        try {
            if (authentication == null) throw new SQLException("Data source is unavailable");
            AuthenticationService.Result result = authentication.authenticate(username, password, request.getRemoteAddr());
            switch (result.status()) {
                case INVALID_INPUT -> {
                    request.setAttribute("loginMessage", "Nhập tên đăng nhập và mật khẩu hợp lệ để tiếp tục.");
                    request.setAttribute("messageType", "error");
                    request.setAttribute("username", username.strip());
                    forwardLogin(request, response, HttpServletResponse.SC_BAD_REQUEST, session);
                }
                case INVALID_CREDENTIALS -> {
                    request.setAttribute("loginMessage", "Tên đăng nhập hoặc mật khẩu không đúng.");
                    request.setAttribute("messageType", "error");
                    request.setAttribute("username", username.strip());
                    forwardLogin(request, response, HttpServletResponse.SC_UNAUTHORIZED, session);
                }
                case RATE_LIMITED -> {
                    response.setHeader("Retry-After", Long.toString(result.retryAfterSeconds()));
                    request.setAttribute("loginMessage", "Bạn đã thử đăng nhập quá số lần. Vui lòng thử lại sau "
                            + result.retryAfterSeconds() + " giây.");
                    request.setAttribute("messageType", "error");
                    request.setAttribute("username", username.strip());
                    forwardLogin(request, response, 429, session);
                }
                case AUTHENTICATED -> establishSession(request, response, session, result.employee());
            }
        } catch (SQLException | IllegalStateException exception) {
            String requestId = String.valueOf(request.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE));
            LOGGER.log(Level.SEVERE, "Login failed due to a service error; requestId={0}, errorType={1}",
                    new Object[]{requestId, exception.getClass().getSimpleName()});
            request.setAttribute("loginMessage", "Hệ thống đang gặp sự cố. Hãy thử lại sau. Mã yêu cầu: " + requestId);
            request.setAttribute("messageType", "error");
            request.setAttribute("username", username.strip());
            forwardLogin(request, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, session);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void establishSession(HttpServletRequest request, HttpServletResponse response,
                                  HttpSession session, SessionEmployee employee) throws IOException {
        request.changeSessionId();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) session.removeAttribute(names.nextElement());
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE, employee);
        SecuritySupport.rotateCsrf(session);
        response.sendRedirect(request.getContextPath() + "/app/home");
    }

    private void forwardLogin(HttpServletRequest request, HttpServletResponse response, int status, HttpSession session)
            throws ServletException, IOException {
        response.setStatus(status);
        if (session != null) request.setAttribute("csrfToken", SecuritySupport.csrfToken(session));
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
}
