package vn.ptit.pttkm3.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class SecuritySupport {
    static final String CSRF_ATTRIBUTE = SecuritySupport.class.getName() + ".csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecuritySupport() { }

    public static String csrfToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_ATTRIBUTE);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = HexFormat.of().formatHex(bytes);
            session.setAttribute(CSRF_ATTRIBUTE, token);
        }
        return token;
    }

    public static void rotateCsrf(HttpSession session) {
        session.removeAttribute(CSRF_ATTRIBUTE);
        csrfToken(session);
    }

    public static boolean validCsrf(HttpServletRequest request, HttpSession session) {
        if (session == null) return false;
        Object stored = session.getAttribute(CSRF_ATTRIBUTE);
        String submitted = request.getParameter("_csrf");
        if (!(stored instanceof String expected) || submitted == null) return false;
        return MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                submitted.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
