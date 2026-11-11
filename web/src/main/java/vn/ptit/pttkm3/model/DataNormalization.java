package vn.ptit.pttkm3.model;

import java.text.Normalizer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class DataNormalization {
    private DataNormalization() { }

    public static String text(String value) {
        if (value == null) throw new IllegalArgumentException("Giá trị không được để trống");
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFC)
                .replaceAll("(?U)\\s+", " ").strip().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) throw new IllegalArgumentException("Giá trị không được để trống");
        return normalized;
    }

    public static String phone(String value) {
        if (value == null || !value.matches("\\+?[0-9]{10,15}")) {
            throw new IllegalArgumentException("Số điện thoại phải có 10–15 chữ số, có thể bắt đầu bằng dấu +");
        }
        return value.startsWith("+") ? value.substring(1) : value;
    }

    public static String email(String value) {
        return text(value);
    }

    public static String titleKey(String title, String author, String category, String publisher, Short year) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String value : new String[]{text(title), text(author), text(category), text(publisher),
                    year == null ? "<NO_YEAR>" : year.toString()}) {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
                digest.update(bytes);
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
