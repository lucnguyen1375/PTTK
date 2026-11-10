package vn.ptit.pttkm3.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebFilter("/*")
public final class RequestLoggingFilter implements Filter {
    public static final String REQUEST_ID_ATTRIBUTE = RequestLoggingFilter.class.getName() + ".requestId";
    private static final Logger LOGGER = Logger.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String requestId = UUID.randomUUID().toString();
            request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
            httpResponse.setHeader("X-Request-Id", requestId);
            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException | RuntimeException exception) {
                LOGGER.log(Level.SEVERE, "Request failed; requestId={0}, method={1}, path={2}, errorType={3}",
                        new Object[]{requestId, httpRequest.getMethod(), httpRequest.getRequestURI(), exception.getClass().getSimpleName()});
                throw exception;
            }
            return;
        }

        chain.doFilter(request, response);
    }
}
