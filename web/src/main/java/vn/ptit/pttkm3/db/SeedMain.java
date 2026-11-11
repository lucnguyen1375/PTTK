package vn.ptit.pttkm3.db;

import vn.ptit.pttkm3.config.AppConfig;
import vn.ptit.pttkm3.config.DatabaseDataSource;
import vn.ptit.pttkm3.auth.PasswordHasher;
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
import java.util.ArrayList;
import java.util.List;

public final class SeedMain {
    private static final String SEED_KEY = "m3-demo-v1";

    private SeedMain() { }

    public static void main(String[] args) throws Exception {
        String demoPassword = System.getenv("DEMO_PASSWORD");
        if (demoPassword == null || demoPassword.isBlank()) {
            throw new IllegalStateException("DEMO_PASSWORD must be set in web/.env before seeding");
        }

        AppConfig config = AppConfig.fromEnvironment(System.getenv());
        DataSource dataSource = DatabaseDataSource.create(config);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (seedAlreadyApplied(connection)) {
                    connection.rollback();
                    System.out.println("Demo seed m3-demo-v1 is already present; no rows were changed.");
                    return;
                }
                insertMembers(connection, demoPassword);
                insertSuppliers(connection);
                insertCustomers(connection);
                insertTitles(connection);
                insertImportDocuments(connection);
                insertImportLines(connection);
                insertCopies(connection);
                insertRentalDocuments(connection);
                insertRentalLines(connection);
                insertReturnInvoices(connection);
                insertReturns(connection);
                insertDepositAndDamageRecords(connection);
                markSeedApplied(connection);
                connection.commit();
                System.out.println("Demo seed m3-demo-v1 inserted successfully.");
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } finally {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }

    private static boolean seedAlreadyApplied(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM app_seed_history WHERE seedKey = ?")) {
            statement.setString(1, SEED_KEY);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static void insertMembers(Connection c, String demoPassword) throws SQLException {
        String sql = "INSERT INTO tblThanhVien (id, tenDangNhap, matKhauHash) VALUES (?, ?, ?)";
        List<MemberSeed> members = new ArrayList<>();
        members.add(new MemberSeed(1, "a", demoPassword.toCharArray()));
        members.add(new MemberSeed(2, "nv_000002", PasswordHasher.randomPassword()));
        members.add(new MemberSeed(101, "ncc_000101", PasswordHasher.randomPassword()));
        members.add(new MemberSeed(102, "ncc_000102", PasswordHasher.randomPassword()));
        members.add(new MemberSeed(103, "ncc_000103", PasswordHasher.randomPassword()));
        members.add(new MemberSeed(201, "kh_000201", PasswordHasher.randomPassword()));
        members.add(new MemberSeed(202, "kh_000202", PasswordHasher.randomPassword()));
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            for (MemberSeed member : members) {
                statement.setLong(1, member.id());
                statement.setString(2, member.username());
                statement.setString(3, PasswordHasher.hash(member.password()));
                statement.addBatch();
                java.util.Arrays.fill(member.password(), '\0');
            }
            statement.executeBatch();
        }

        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblNhanVien (id, maNhanVien, hoTen, chucVu) VALUES (?, ?, ?, ?)")) {
            addEmployee(statement, 1, "Nguyễn Văn A", "Quản lí");
            addEmployee(statement, 2, "Trần Thị B", "Nhân viên");
            statement.executeBatch();
        }
    }

    private static void addEmployee(PreparedStatement statement, long id, String name, String role)
            throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, BusinessCode.fromId("NV", id));
        statement.setString(3, name);
        statement.setString(4, role);
        statement.addBatch();
    }

    private static void insertSuppliers(Connection c) throws SQLException {
        String sql = "INSERT INTO tblNhaCungCap "
                + "(id, maNCC, tenNCC, diaChi, soDienThoai, soDienThoaiChuan, email, emailChuan, version) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            addSupplier(statement, 101, "Tuổi Trẻ", "Quận 1, Thành phố Hồ Chí Minh", "+84912345678", "tuoitre@example.test");
            addSupplier(statement, 102, "Kim Đồng", "Ba Đình, Hà Nội", "0912345679", "kimdong@example.test");
            addSupplier(statement, 103, "Nhà sách Thử Nghiệm", "Cầu Giấy, Hà Nội", "0912345680", "demo-ncc@example.test");
            statement.executeBatch();
        }
    }

    private static void addSupplier(PreparedStatement statement, long id, String name, String address,
                                   String phone, String email) throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, BusinessCode.fromId("NCC", id));
        statement.setString(3, name);
        statement.setString(4, address);
        statement.setString(5, phone);
        statement.setString(6, DataNormalization.phone(phone));
        statement.setString(7, email);
        statement.setString(8, DataNormalization.email(email));
        statement.addBatch();
    }

    private static void insertCustomers(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblKhachHang (id, maKhachHang, ten, diaChi, soDienThoai) VALUES (?, ?, ?, ?, ?)")) {
            addCustomer(statement, 201, "Lê Minh An", "Đống Đa, Hà Nội", "0987654321");
            addCustomer(statement, 202, "Phạm Thu Hà", "Thanh Xuân, Hà Nội", "0987654322");
            statement.executeBatch();
        }
    }

    private static void addCustomer(PreparedStatement statement, long id, String name, String address, String phone)
            throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, BusinessCode.fromId("KH", id));
        statement.setString(3, name);
        statement.setString(4, address);
        statement.setString(5, phone);
        statement.addBatch();
    }

    private static void insertTitles(Connection c) throws SQLException {
        String sql = "INSERT INTO tblDauTruyen "
                + "(id, maDauTruyen, tenDauTruyen, tacGia, theLoai, nhaXuatBan, namXuatBan, "
                + "tenChuan, tacGiaChuan, theLoaiChuan, nhaXuatBanChuan, namXuatBanChuan, naturalKey) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            addTitle(statement, 1, "Doraemon Tập 1", "Fujiko F. Fujio", "Truyện tranh", "Kim Đồng", (short) 2024);
            addTitle(statement, 2, "Sách thiếu nhi", "Nguyễn Nhật Ánh", "Thiếu nhi", "Nhà xuất bản Trẻ", (short) 2023);
            statement.executeBatch();
        }
    }

    private static void addTitle(PreparedStatement statement, long id, String title, String author,
                                 String category, String publisher, short year) throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, BusinessCode.fromId("DT", id));
        statement.setString(3, title);
        statement.setString(4, author);
        statement.setString(5, category);
        statement.setString(6, publisher);
        statement.setShort(7, year);
        statement.setString(8, DataNormalization.text(title));
        statement.setString(9, DataNormalization.text(author));
        statement.setString(10, DataNormalization.text(category));
        statement.setString(11, DataNormalization.text(publisher));
        statement.setShort(12, year);
        statement.setString(13, DataNormalization.titleKey(title, author, category, publisher, year));
        statement.addBatch();
    }

    private static void insertImportDocuments(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblPhieuNhap (id, maPhieuNhap, ngayLap, nhanVienId, nhaCungCapId) VALUES (?, ?, ?, ?, ?)")) {
            addImport(statement, 1, LocalDate.of(2024, 12, 20), 1, 101);
            addImport(statement, 2, LocalDate.of(2024, 12, 21), 2, 102);
            statement.executeBatch();
        }
    }

    private static void addImport(PreparedStatement statement, long id, LocalDate date, long employee, long supplier)
            throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, BusinessCode.fromId("PN", id));
        statement.setDate(3, Date.valueOf(date));
        statement.setLong(4, employee);
        statement.setLong(5, supplier);
        statement.addBatch();
    }

    private static void insertImportLines(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblChiTietPhieuNhap (id, phieuNhapId, dauTruyenId, soLuong, donGiaNhap, giaThueNgay) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
            addImportLine(statement, 1, 1, 1, 1, 40_000, 10_000);
            addImportLine(statement, 2, 1, 2, 1, 20_000, 5_000);
            addImportLine(statement, 3, 2, 1, 1, 45_000, 15_000);
            addImportLine(statement, 4, 2, 1, 1, 50_000, 15_000);
            statement.executeBatch();
        }
    }

    private static void addImportLine(PreparedStatement statement, long id, long document, long title,
                                      int quantity, long purchase, long rental) throws SQLException {
        statement.setLong(1, id);
        statement.setLong(2, document);
        statement.setLong(3, title);
        statement.setInt(4, quantity);
        statement.setBigDecimal(5, BigDecimal.valueOf(purchase));
        statement.setBigDecimal(6, BigDecimal.valueOf(rental));
        statement.addBatch();
    }

    private static void insertCopies(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblQuyenTruyen (id, maQuyenTruyen, giaThue, chiTietPhieuNhapId, dauTruyenId) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
            addCopy(statement, 1, 12_000, 1, 1);
            addCopy(statement, 2, 15_000, 3, 1);
            addCopy(statement, 3, 5_000, 2, 2);
            addCopy(statement, 4, 15_000, 4, 1);
            statement.executeBatch();
        }
    }

    private static void addCopy(PreparedStatement statement, long id, long rental, long importLine, long title)
            throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, BusinessCode.fromId("QT", id));
        statement.setBigDecimal(3, BigDecimal.valueOf(rental));
        statement.setLong(4, importLine);
        statement.setLong(5, title);
        statement.addBatch();
    }

    private static void insertRentalDocuments(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblPhieuMuon (id, maPhieuMuon, ngayMuon, nhanVienId, khachHangId) VALUES (?, ?, ?, ?, ?)")) {
            addRental(statement, 1, LocalDate.of(2025, 1, 1), 1, 201);
            addRental(statement, 2, LocalDate.of(2025, 1, 10), 2, 201);
            addRental(statement, 3, LocalDate.of(2025, 1, 30), 1, 202);
            addRental(statement, 4, LocalDate.of(2025, 1, 31), 2, 202);
            statement.executeBatch();
        }
    }

    private static void addRental(PreparedStatement statement, long id, LocalDate date, long employee, long customer)
            throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, BusinessCode.fromId("PM", id));
        statement.setDate(3, Date.valueOf(date));
        statement.setLong(4, employee);
        statement.setLong(5, customer);
        statement.addBatch();
    }

    private static void insertRentalLines(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblChiTietMuon (id, phieuMuonId, quyenTruyenId, donGia, soLuong, tinhTrangMuon) "
                        + "VALUES (?, ?, ?, ?, 1, ?)")) {
            addRentalLine(statement, 1, 1, 1, 10_000);
            addRentalLine(statement, 2, 1, 2, 15_000);
            addRentalLine(statement, 3, 2, 1, 12_000);
            addRentalLine(statement, 4, 3, 3, 5_000);
            addRentalLine(statement, 5, 4, 4, 15_000);
            statement.executeBatch();
        }
    }

    private static void addRentalLine(PreparedStatement statement, long id, long rental, long copy, long price)
            throws SQLException {
        statement.setLong(1, id);
        statement.setLong(2, rental);
        statement.setLong(3, copy);
        statement.setBigDecimal(4, BigDecimal.valueOf(price));
        statement.setString(5, "Bình thường");
        statement.addBatch();
    }

    private static void insertReturnInvoices(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblHoaDonTra (id, maHoaDonTra, ngayLap, nhanVienId, khachHangId) VALUES (?, ?, ?, ?, ?)")) {
            addReturnInvoice(statement, 1, LocalDate.of(2025, 1, 3), 1, 201);
            addReturnInvoice(statement, 2, LocalDate.of(2025, 1, 10), 2, 201);
            addReturnInvoice(statement, 3, LocalDate.of(2025, 1, 31), 1, 202);
            addReturnInvoice(statement, 4, LocalDate.of(2025, 2, 1), 2, 202);
            statement.executeBatch();
        }
    }

    private static void addReturnInvoice(PreparedStatement statement, long id, LocalDate date,
                                        long employee, long customer) throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, BusinessCode.fromId("HDT", id));
        statement.setDate(3, Date.valueOf(date));
        statement.setLong(4, employee);
        statement.setLong(5, customer);
        statement.addBatch();
    }

    private static void insertReturns(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblChiTietTra (chiTietMuonId, hoaDonTraId, ngayTra, tinhTrangTra) VALUES (?, ?, ?, ?)")) {
            addReturn(statement, 1, 1, LocalDate.of(2025, 1, 3));
            addReturn(statement, 2, 1, LocalDate.of(2025, 1, 3));
            addReturn(statement, 3, 2, LocalDate.of(2025, 1, 10));
            addReturn(statement, 4, 3, LocalDate.of(2025, 1, 31));
            addReturn(statement, 5, 4, LocalDate.of(2025, 2, 1));
            statement.executeBatch();
        }
    }

    private static void addReturn(PreparedStatement statement, long detail, long invoice, LocalDate date)
            throws SQLException {
        statement.setLong(1, detail);
        statement.setLong(2, invoice);
        statement.setDate(3, Date.valueOf(date));
        statement.setString(4, "Đã trả");
        statement.addBatch();
    }

    private static void insertDepositAndDamageRecords(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblTaiSanCoc (id, moTa, giaTriUocTinh, ten) VALUES (1, ?, ?, ?)")) {
            statement.setString(1, "Thẻ thư viện demo");
            statement.setBigDecimal(2, BigDecimal.valueOf(50_000));
            statement.setString(3, "Thẻ thư viện");
            statement.executeUpdate();
        }
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblCTTaiSanCoc (id, giaTri, tinhTrang, daTra, taiSanCocId, phieuMuonId) "
                        + "VALUES (1, ?, ?, TRUE, 1, 1)")) {
            statement.setBigDecimal(1, BigDecimal.valueOf(50_000));
            statement.setString(2, "Đã nhận");
            statement.executeUpdate();
        }
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblTaiSanTra (ctTaiSanCocId, hoaDonTraId, ngayTra, tinhTrangTra) VALUES (1, 1, ?, ?)")) {
            statement.setDate(1, Date.valueOf(LocalDate.of(2025, 1, 3)));
            statement.setString(2, "Nguyên vẹn");
            statement.executeUpdate();
        }
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblLoi (id, ten, moTa, tienPhat) VALUES (?, ?, ?, ?)")) {
            addFault(statement, 1, "Rách bìa", "Bìa sách bị rách nhẹ", 0);
            addFault(statement, 2, "Mất trang", "Thiếu một trang sách", 10_000);
            statement.executeBatch();
        }
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblLoiMuon (id, chiTietMuonId, loiId, ten, moTa) VALUES (1, 1, 1, ?, ?)")) {
            statement.setString(1, "Rách bìa");
            statement.setString(2, "Bìa sách bị rách nhẹ");
            statement.executeUpdate();
        }
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tblLoiTra (id, chiTietTraId, loiId, ten, moTa) VALUES (?, ?, ?, ?, ?)")) {
            statement.setLong(1, 1);
            statement.setLong(2, 1);
            statement.setLong(3, 1);
            statement.setString(4, "Rách bìa");
            statement.setString(5, "Bìa sách bị rách nhẹ");
            statement.addBatch();
            statement.setLong(1, 2);
            statement.setLong(2, 1);
            statement.setLong(3, 2);
            statement.setString(4, "Mất trang");
            statement.setString(5, "Thiếu một trang sách");
            statement.addBatch();
            statement.executeBatch();
        }
    }

    private static void addFault(PreparedStatement statement, long id, String name, String description, long fine)
            throws SQLException {
        statement.setLong(1, id);
        statement.setString(2, name);
        statement.setString(3, description);
        statement.setBigDecimal(4, BigDecimal.valueOf(fine));
        statement.addBatch();
    }

    private static void markSeedApplied(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("INSERT INTO app_seed_history (seedKey) VALUES (?)")) {
            statement.setString(1, SEED_KEY);
            statement.executeUpdate();
        }
    }

    private record MemberSeed(long id, String username, char[] password) { }
}
