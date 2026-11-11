package vn.ptit.pttkm3.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.ptit.pttkm3.auth.SessionEmployee;

import java.io.IOException;

@WebFilter("/app/*")
public final class AuthenticationFilter implements Filter {
    public static final String EMPLOYEE_ATTRIBUTE = SessionEmployee.class.getName();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Cache-Control", "no-store");
        HttpSession session = httpRequest.getSession(false);
        if (session == null || !(session.getAttribute(EMPLOYEE_ATTRIBUTE) instanceof SessionEmployee)) {
            if ("GET".equals(httpRequest.getMethod()) || "HEAD".equals(httpRequest.getMethod())) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?expired=1");
            } else {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpRequest.getRequestDispatcher("/WEB-INF/views/session-expired.jsp").forward(httpRequest, httpResponse);
            }
            return;
        }
        if (isWriteMethod(httpRequest.getMethod()) && !SecuritySupport.validCsrf(httpRequest, session)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpRequest.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(httpRequest, httpResponse);
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isWriteMethod(String method) {
        return !"GET".equals(method) && !"HEAD".equals(method) && !"OPTIONS".equals(method);
    }
}
