package vn.ptit.pttkm3.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.ptit.pttkm3.config.ApplicationContextListener;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/health/live", "/health/ready"})
public final class HealthServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(HealthServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"development".equalsIgnoreCase(System.getenv("APP_ENV"))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");

        if ("/health/live".equals(request.getServletPath())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"UP\"}");
            return;
        }

        Object configError = getServletContext().getAttribute(ApplicationContextListener.CONFIG_ERROR_ATTRIBUTE);
        Object source = getServletContext().getAttribute(ApplicationContextListener.DATA_SOURCE_ATTRIBUTE);
        if (configError != null || !(source instanceof DataSource dataSource)) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"status\":\"DOWN\",\"database\":\"DOWN\"}");
            return;
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(5);
            try (ResultSet result = statement.executeQuery("SELECT 1")) {
                if (result.next() && result.getInt(1) == 1) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"status\":\"UP\",\"database\":\"UP\"}");
                    return;
                }
            }
            writeUnavailable(response);
        } catch (SQLException exception) {
            String requestId = String.valueOf(request.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE));
            LOGGER.log(Level.WARNING, "Database readiness check failed; requestId={0}, errorType={1}",
                    new Object[]{requestId, exception.getClass().getSimpleName()});
            writeUnavailable(response);
        }
    }

    private static void writeUnavailable(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.getWriter().write("{\"status\":\"DOWN\",\"database\":\"DOWN\"}");
    }
}
