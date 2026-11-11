package vn.ptit.pttkm3.model;

import java.util.Locale;
import java.util.Set;

public final class BusinessCode {
    private static final Set<String> PREFIXES = Set.of("NV", "NCC", "KH", "DT", "QT", "PN", "PM", "HDT");

    private BusinessCode() { }

    public static String fromId(String prefix, long id) {
        if (!PREFIXES.contains(prefix) || id < 1) {
            throw new IllegalArgumentException("Prefix hoặc id mã nghiệp vụ không hợp lệ");
        }
        String digits = String.format(Locale.ROOT, "%06d", id);
        String code = prefix + digits;
        if (code.length() > 16) {
            throw new IllegalArgumentException("Id vượt giới hạn độ dài mã nghiệp vụ");
        }
        return code;
    }
}
