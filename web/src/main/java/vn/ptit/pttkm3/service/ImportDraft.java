package vn.ptit.pttkm3.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class ImportDraft {
    public record Title(String reference, Long existingId, String code, String name,
                        String author, String category, String publisher, Short year) { }
    public record Line(String key, Title title, int quantity, BigDecimal cost, BigDecimal rent) { }
    public record Snapshot(String id, long employeeId, long supplierId, long version, List<Line> lines) { }

    private final String id;
    private final long employeeId;
    private final long supplierId;
    private final LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    private final LinkedHashMap<String, Line> lines = new LinkedHashMap<>();
    private final LinkedHashMap<String, Title> stagedTitles = new LinkedHashMap<>();
    private long version;
    private boolean saving;

    public ImportDraft(String id, long employeeId, long supplierId) {
        this.id = id;
        this.employeeId = employeeId;
        this.supplierId = supplierId;
    }

    public String id() { return id; }
    public long employeeId() { return employeeId; }
    public long supplierId() { return supplierId; }
    public LocalDateTime createdAt() { return createdAt; }
    public synchronized long version() { return version; }
    public synchronized List<Line> lines() { return List.copyOf(lines.values()); }
    public synchronized List<Title> stagedTitles() { return List.copyOf(stagedTitles.values()); }
    public synchronized Title stagedTitle(String reference) { return stagedTitles.get(reference); }

    public synchronized Title stageTitle(String name, String author, String category,
                                         String publisher, Short year, long expected) {
        check(expected);
        for (Title title : stagedTitles.values()) {
            if (title.name().equals(name) && title.author().equals(author)
                    && title.category().equals(category) && title.publisher().equals(publisher)
                    && java.util.Objects.equals(title.year(), year)) return title;
        }
        String reference = "new:" + java.util.UUID.randomUUID();
        Title title = new Title(reference, null, null, name, author, category, publisher, year);
        stagedTitles.put(reference, title);
        version++;
        return title;
    }

    public synchronized void add(Title title, int quantity, BigDecimal cost, BigDecimal rent, long expected) {
        check(expected);
        validateQuantity(quantity);
        if (title.existingId() == null && !stagedTitles.containsKey(title.reference())) {
            throw new IllegalArgumentException("Đầu truyện tạm không còn trong phiếu này. Hãy tìm hoặc thêm lại đầu truyện.");
        }
        String lineKey = key(title, cost, rent);
        Line old = lines.get(lineKey);
        int sum = quantity + (old == null ? 0 : old.quantity());
        validateQuantity(sum);
        lines.put(lineKey, new Line(lineKey, title, sum, cost, rent));
        version++;
    }

    public synchronized void edit(String lineKey, int quantity, BigDecimal cost, BigDecimal rent, long expected) {
        check(expected);
        validateQuantity(quantity);
        Line old = lines.get(lineKey);
        if (old == null) throw new NoSuchElementException();
        String newKey = key(old.title(), cost, rent);
        Line target = lines.get(newKey);
        int sum = quantity + (target == null || target.key().equals(lineKey) ? 0 : target.quantity());
        validateQuantity(sum);
        lines.remove(lineKey);
        lines.put(newKey, new Line(newKey, old.title(), sum, cost, rent));
        version++;
    }

    public synchronized void remove(String lineKey, long expected) {
        check(expected);
        if (lines.remove(lineKey) == null) throw new NoSuchElementException();
        version++;
    }

    public synchronized Snapshot beginSave(long expected) {
        check(expected);
        if (lines.isEmpty()) throw new IllegalArgumentException("Phiếu nhập chưa có đầu truyện.");
        saving = true;
        return new Snapshot(id, employeeId, supplierId, version, List.copyOf(lines.values()));
    }

    public synchronized void endSave() { saving = false; }

    private void check(long expected) {
        if (saving || expected != version) throw new StaleDraftException();
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1 || quantity > 10_000) {
            throw new IllegalArgumentException("Số lượng mỗi dòng phải từ 1 đến 10.000.");
        }
    }

    private static String key(Title title, BigDecimal cost, BigDecimal rent) {
        return title.reference() + "|" + cost.toPlainString() + "|" + rent.toPlainString();
    }

    public static final class StaleDraftException extends RuntimeException { }
}
