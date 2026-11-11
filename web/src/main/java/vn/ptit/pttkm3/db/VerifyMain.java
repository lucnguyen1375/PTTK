package vn.ptit.pttkm3.db;

import vn.ptit.pttkm3.config.AppConfig;
import vn.ptit.pttkm3.config.DatabaseDataSource;
import vn.ptit.pttkm3.model.BusinessCode;
import vn.ptit.pttkm3.model.DataNormalization;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class VerifyMain {
    private static final String SEED_KEY = "m3-demo-v1";

    private VerifyMain() { }

    public static void main(String[] args) throws Exception {
        verifyNormalizationRules();
        AppConfig config = AppConfig.fromEnvironment(System.getenv());
        DataSource dataSource = DatabaseDataSource.create(config);
        try (Connection connection = dataSource.getConnection()) {
            verifyMigrations(connection);
            verifySeedMarker(connection);
            verifyTableCount(connection);
            verifyCodes(connection);
            verifyCanonicalValues(connection);
            verifyRentalSource(connection);
            verifyDatesAndForeignKeys(connection);
            verifyHistoricalPrice(connection);
            verifyConstraintRejections(connection);
            require(scalarLong(connection, "SELECT COUNT(*) FROM tblChiTietPhieuNhap d WHERE d.soLuong <> "
                    + "(SELECT COUNT(*) FROM tblQuyenTruyen q WHERE q.chiTietPhieuNhapId = d.id)") == 0,
                    "Physical copy count does not match import quantity");
            verifyRevenue(connection, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                    Map.of(101L, BigDecimal.valueOf(37_000), 102L, BigDecimal.valueOf(30_000)));
            verifyRevenue(connection, LocalDate.of(2025, 1, 3), LocalDate.of(2025, 1, 3),
                    Map.of(101L, BigDecimal.valueOf(20_000), 102L, BigDecimal.valueOf(30_000)));
            verifyRevenue(connection, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 1),
                    Map.of(102L, BigDecimal.valueOf(15_000)));
            verifyRevenue(connection, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), Map.of());
        } finally {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        }
        System.out.println("P2 schema and demo-data verification passed.");
        System.out.println("Verified 18 business tables, import-source lineage, return dates, and expected revenue totals.");
    }

    private static void verifyNormalizationRules() {
        require(DataNormalization.text("\u00a0ĐẦU\u2003 TRUYỆN\u00a0").equals("đầu truyện"),
                "Unicode whitespace must be trimmed and collapsed");
        require(DataNormalization.text("a\u0301").equals(DataNormalization.text("á")),
                "Equivalent Unicode accents must normalize equally");
        require(!DataNormalization.text("a").equals(DataNormalization.text("á")),
                "Normalization must preserve Vietnamese accents");
        require(DataNormalization.titleKey(" TÊN ", "Tác giả", "Loại", "NXB", null)
                .equals(DataNormalization.titleKey("tên", "tác giả", "loại", "nxb", null)),
                "Title identity must ignore case/spacing and accept absent year");
        require(!DataNormalization.titleKey("Tên", "Tác giả", "Loại", "NXB", null)
                .equals(DataNormalization.titleKey("Tên", "Tác giả", "Loại", "NXB", (short) 2024)),
                "Absent year must differ from a specified year");
    }

    private static void verifyConstraintRejections(Connection c) throws SQLException {
        boolean autoCommit = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            expectRejected(c, "UPDATE tblPhieuNhap SET maPhieuNhap = 'PN000001' WHERE id = 2", 1062);
            expectRejected(c, "UPDATE tblNhaCungCap SET soDienThoaiChuan = "
                    + "(SELECT v.phone FROM (SELECT soDienThoaiChuan phone FROM tblNhaCungCap WHERE id=101) v) WHERE id=102", 1062);
            expectRejected(c, "UPDATE tblNhaCungCap SET emailChuan = 'TUOITRE@EXAMPLE.TEST' WHERE id=102", 1062);
            expectRejected(c, "UPDATE tblQuyenTruyen SET dauTruyenId = 2 WHERE id = 1", 1452);
            expectRejected(c, "UPDATE tblChiTietMuon SET soLuong = 2 WHERE id = 1", 3819);
            expectRejected(c, "UPDATE tblChiTietPhieuNhap SET soLuong = 0 WHERE id = 1", 3819);
            expectRejected(c, "UPDATE tblChiTietPhieuNhap SET soLuong = 10001 WHERE id = 1", 3819);
            expectRejected(c, "UPDATE tblQuyenTruyen SET giaThue = 0 WHERE id = 1", 3819);
            expectRejected(c, "UPDATE tblNhaCungCap SET soDienThoai = '123' WHERE id = 101", 3819);
            expectRejected(c, "UPDATE tblNhaCungCap SET email = '' WHERE id = 101", 3819);
            expectRejected(c, "UPDATE tblNhaCungCap SET emailChuan = '' WHERE id = 101", 3819);
            expectRejected(c, "UPDATE tblChiTietPhieuNhap SET soLuong = 2, donGiaNhap = 999999999999 WHERE id = 1", 3819);
            expectRejected(c, "UPDATE tblDauTruyen SET naturalKey = "
                    + "(SELECT v.k FROM (SELECT naturalKey k FROM tblDauTruyen WHERE id=1) v) WHERE id=2", 1062);
        } finally {
            try { c.rollback(); } finally { c.setAutoCommit(autoCommit); }
        }
        System.out.println("Verified 13 invalid writes were rejected; verification transaction rolled back.");
    }

    private static void expectRejected(Connection c, String sql, int mysqlError) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (exception.getErrorCode() == mysqlError) return;
            throw exception;
        }
        throw new IllegalStateException("Database accepted an invalid write: " + sql);
    }

    private static void verifyMigrations(Connection c) throws SQLException {
        long successful = scalarLong(c, "SELECT COUNT(*) FROM flyway_schema_history "
                + "WHERE success = TRUE AND version IN ('1','2','3','4','5')");
        require(successful == 5, "Expected five successful Flyway migrations");
    }

    private static void verifySeedMarker(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("SELECT 1 FROM app_seed_history WHERE seedKey = ?")) {
            statement.setString(1, SEED_KEY);
            try (ResultSet result = statement.executeQuery()) {
                require(result.next(), "Demo seed marker is missing");
            }
        }
    }

    private static void verifyTableCount(Connection c) throws SQLException {
        long count = scalarLong(c, "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE' AND table_name LIKE 'tbl%' ");
        require(count == 18, "Expected 18 business tables; found " + count);
    }

    private static void verifyCodes(Connection c) throws SQLException {
        verifyCodes(c, "tblNhanVien", "id", "maNhanVien", "NV");
        verifyCodes(c, "tblNhaCungCap", "id", "maNCC", "NCC");
        verifyCodes(c, "tblKhachHang", "id", "maKhachHang", "KH");
        verifyCodes(c, "tblDauTruyen", "id", "maDauTruyen", "DT");
        verifyCodes(c, "tblQuyenTruyen", "id", "maQuyenTruyen", "QT");
        verifyCodes(c, "tblPhieuNhap", "id", "maPhieuNhap", "PN");
        verifyCodes(c, "tblPhieuMuon", "id", "maPhieuMuon", "PM");
        verifyCodes(c, "tblHoaDonTra", "id", "maHoaDonTra", "HDT");
    }

    private static void verifyCodes(Connection c, String table, String idColumn, String codeColumn, String prefix)
            throws SQLException {
        String sql = "SELECT " + idColumn + ", " + codeColumn + " FROM " + table;
        try (PreparedStatement statement = c.prepareStatement(sql); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                long id = rows.getLong(1);
                require(BusinessCode.fromId(prefix, id).equals(rows.getString(2)),
                        "Business code does not match its id in " + table);
            }
        }
    }

    private static void verifyCanonicalValues(Connection c) throws SQLException {
        String vendorSql = "SELECT tenNCC, soDienThoai, soDienThoaiChuan, email, emailChuan "
                + "FROM tblNhaCungCap";
        try (PreparedStatement statement = c.prepareStatement(vendorSql); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                require(DataNormalization.phone(rows.getString("soDienThoai"))
                        .equals(rows.getString("soDienThoaiChuan")), "Supplier phone canonical value is inconsistent");
                require(DataNormalization.email(rows.getString("email"))
                        .equals(rows.getString("emailChuan")), "Supplier email canonical value is inconsistent");
                require(!rows.getString("tenNCC").isBlank(), "Supplier name is empty");
            }
        }

        String titleSql = "SELECT tenDauTruyen, tacGia, theLoai, nhaXuatBan, namXuatBan, "
                + "tenChuan, tacGiaChuan, theLoaiChuan, nhaXuatBanChuan, namXuatBanChuan, naturalKey "
                + "FROM tblDauTruyen";
        try (PreparedStatement statement = c.prepareStatement(titleSql); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                Short year = rows.getObject("namXuatBan", Short.class);
                require(DataNormalization.text(rows.getString("tenDauTruyen")).equals(rows.getString("tenChuan")),
                        "Title canonical name is inconsistent");
                require(DataNormalization.text(rows.getString("tacGia")).equals(rows.getString("tacGiaChuan")),
                        "Title canonical author is inconsistent");
                require(DataNormalization.text(rows.getString("theLoai")).equals(rows.getString("theLoaiChuan")),
                        "Title canonical category is inconsistent");
                require(DataNormalization.text(rows.getString("nhaXuatBan")).equals(rows.getString("nhaXuatBanChuan")),
                        "Title canonical publisher is inconsistent");
                int expectedYear = year == null ? 0 : year;
                require(rows.getInt("namXuatBanChuan") == expectedYear, "Title canonical year is inconsistent");
                require(DataNormalization.titleKey(rows.getString("tenDauTruyen"), rows.getString("tacGia"),
                        rows.getString("theLoai"), rows.getString("nhaXuatBan"), year)
                        .equals(rows.getString("naturalKey")), "Title natural key is inconsistent");
                require(year == null || year <= LocalDate.now().getYear(), "Publication year is in the future");
            }
        }
    }

    private static void verifyRentalSource(Connection c) throws SQLException {
        long copiesWithWrongSource = scalarLong(c, "SELECT COUNT(*) FROM tblQuyenTruyen q "
                + "JOIN tblChiTietPhieuNhap d ON d.id = q.chiTietPhieuNhapId "
                + "WHERE q.dauTruyenId <> d.dauTruyenId");
        require(copiesWithWrongSource == 0, "A physical copy has a different title from its import line");

        long suppliersOnH1 = scalarLong(c, "SELECT COUNT(DISTINCT pn.nhaCungCapId) "
                + "FROM tblChiTietTra ct JOIN tblChiTietMuon cm ON cm.id = ct.chiTietMuonId "
                + "JOIN tblQuyenTruyen q ON q.id = cm.quyenTruyenId "
                + "JOIN tblChiTietPhieuNhap cpn ON cpn.id = q.chiTietPhieuNhapId "
                + "JOIN tblPhieuNhap pn ON pn.id = cpn.phieuNhapId WHERE ct.hoaDonTraId = 1");
        require(suppliersOnH1 == 2, "Return invoice H1 must contain books from two suppliers");
    }

    private static void verifyDatesAndForeignKeys(Connection c) throws SQLException {
        long invalidDates = scalarLong(c, "SELECT COUNT(*) FROM tblChiTietTra ct "
                + "JOIN tblChiTietMuon cm ON cm.id = ct.chiTietMuonId "
                + "JOIN tblPhieuMuon pm ON pm.id = cm.phieuMuonId "
                + "JOIN tblHoaDonTra h ON h.id = ct.hoaDonTraId "
                + "WHERE ct.ngayTra < pm.ngayMuon OR h.ngayLap < ct.ngayTra");
        require(invalidDates == 0, "A seeded return violates the rental or invoice date order");

        long physicalCopiesPerRental = scalarLong(c, "SELECT COUNT(*) FROM tblChiTietMuon WHERE soLuong <> 1");
        require(physicalCopiesPerRental == 0, "A rental detail must represent exactly one physical copy");
        long errorsOnH1Return = scalarLong(c, "SELECT COUNT(*) FROM tblLoiTra WHERE chiTietTraId = 1");
        require(errorsOnH1Return == 2, "Seed must include multiple return faults on one detail");
        long assetsReturnedOnH1 = scalarLong(c, "SELECT COUNT(*) FROM tblTaiSanTra WHERE hoaDonTraId = 1");
        require(assetsReturnedOnH1 == 1, "Seed must include a returned deposit asset on H1");
    }

    private static void verifyHistoricalPrice(Connection c) throws SQLException {
        BigDecimal current = scalarMoney(c, "SELECT giaThue FROM tblQuyenTruyen WHERE id = 1");
        BigDecimal firstRentalSnapshot = scalarMoney(c, "SELECT donGia FROM tblChiTietMuon WHERE id = 1");
        BigDecimal secondRentalSnapshot = scalarMoney(c, "SELECT donGia FROM tblChiTietMuon WHERE id = 3");
        require(current.compareTo(BigDecimal.valueOf(12_000)) == 0, "Demo current rental price was not updated");
        require(firstRentalSnapshot.compareTo(BigDecimal.valueOf(10_000)) == 0,
                "First rental must keep its original price snapshot");
        require(secondRentalSnapshot.compareTo(BigDecimal.valueOf(12_000)) == 0,
                "Second rental must keep its own price snapshot");
    }

    private static void verifyRevenue(Connection c, LocalDate from, LocalDate to,
                                     Map<Long, BigDecimal> expected) throws SQLException {
        String sql = "SELECT pn.nhaCungCapId, "
                + "SUM(GREATEST(1, DATEDIFF(ct.ngayTra, pm.ngayMuon)) * cm.donGia) AS doanhThu "
                + "FROM tblChiTietTra ct "
                + "JOIN tblHoaDonTra h ON h.id = ct.hoaDonTraId "
                + "JOIN tblChiTietMuon cm ON cm.id = ct.chiTietMuonId "
                + "JOIN tblPhieuMuon pm ON pm.id = cm.phieuMuonId "
                + "JOIN tblQuyenTruyen q ON q.id = cm.quyenTruyenId "
                + "JOIN tblChiTietPhieuNhap cpn ON cpn.id = q.chiTietPhieuNhapId "
                + "JOIN tblPhieuNhap pn ON pn.id = cpn.phieuNhapId "
                + "WHERE h.ngayLap BETWEEN ? AND ? GROUP BY pn.nhaCungCapId";
        Map<Long, BigDecimal> actual = new HashMap<>();
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(to));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) actual.put(rows.getLong("nhaCungCapId"), rows.getBigDecimal("doanhThu"));
            }
        }
        require(actual.keySet().equals(expected.keySet()), "Revenue suppliers mismatch for " + from + " through " + to
                + "; expected=" + expected.keySet() + ", actual=" + actual.keySet());
        expected.forEach((supplier, amount) -> require(actual.get(supplier).compareTo(amount) == 0,
                "Revenue amount mismatch for supplier id " + supplier));
        require(!actual.containsKey(103L), "A supplier with no revenue must be omitted from results");
    }

    private static long scalarLong(Connection c, String sql) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            if (!result.next()) throw new SQLException("Verification query returned no row");
            return result.getLong(1);
        }
    }

    private static BigDecimal scalarMoney(Connection c, String sql) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            if (!result.next()) throw new SQLException("Verification query returned no row");
            return result.getBigDecimal(1);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
