package vn.ptit.pttkm3.auth;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class EmployeeAccountDao {
    private final DataSource dataSource;

    public EmployeeAccountDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public AccountRecord findByUsername(String username) throws SQLException {
        String sql = "SELECT tv.id, tv.matKhauHash, nv.maNhanVien, nv.hoTen, nv.chucVu "
                + "FROM tblThanhVien tv LEFT JOIN tblNhanVien nv ON nv.id = tv.id "
                + "WHERE tv.tenDangNhap = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(5);
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                String code = result.getString("maNhanVien");
                SessionEmployee employee = code == null ? null : new SessionEmployee(
                        result.getLong("id"), code, result.getString("hoTen"), result.getString("chucVu"));
                return new AccountRecord(result.getString("matKhauHash"), employee);
            }
        }
    }

    public record AccountRecord(String passwordHash, SessionEmployee employee) { }
}
