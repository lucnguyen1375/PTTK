package vn.ptit.pttkm3.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import jakarta.servlet.http.HttpSession;
import vn.ptit.pttkm3.auth.SessionEmployee;

@WebServlet(urlPatterns = "")
public final class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthenticationFilter.EMPLOYEE_ATTRIBUTE) instanceof SessionEmployee) {
            response.sendRedirect(request.getContextPath() + "/app/home");
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
