package vn.ptit.pttkm3.dao;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import vn.ptit.pttkm3.auth.PasswordHasher;
import vn.ptit.pttkm3.model.BusinessCode;
import vn.ptit.pttkm3.model.NhaCungCap;

public final class NhaCungCapDao {
    private final DataSource dataSource;
    public NhaCungCapDao(DataSource dataSource) { this.dataSource = dataSource; }

    public record Page(List<NhaCungCap> items, int page, int pageCount, long total) { }
    public enum DeleteResult { DELETED, IN_USE, NOT_FOUND }

    public Page search(String keyword, int requestedPage) throws SQLException {
        String q = keyword == null ? "" : keyword.strip();
        int page = Math.max(1, requestedPage);
        try (Connection c = dataSource.getConnection()) {
            long total;
        String where = " WHERE (? = '' OR LOCATE(LOWER(?),LOWER(n.maNCC))>0 OR LOCATE(LOWER(?),LOWER(n.tenNCC))>0 OR LOCATE(LOWER(?),LOWER(n.diaChi))>0)";
            try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM tblNhaCungCap n" + where)) {
                bindSearch(p, q); try (ResultSet r = p.executeQuery()) { r.next(); total = r.getLong(1); }
            }
            int pageCount = Math.max(1, (int)Math.min(Integer.MAX_VALUE, (total + 19) / 20));
            page = Math.min(page, pageCount);
            String sql = "SELECT n.id,tv.tenDangNhap,n.maNCC,n.tenNCC,n.diaChi,n.soDienThoai,n.email,n.version "
                    + "FROM tblNhaCungCap n JOIN tblThanhVien tv ON tv.id=n.id" + where + " ORDER BY n.id LIMIT 20 OFFSET ?";
            List<NhaCungCap> items = new ArrayList<>();
            try (PreparedStatement p = c.prepareStatement(sql)) {
                bindSearch(p, q); p.setInt(5, (page - 1) * 20);
                try (ResultSet r = p.executeQuery()) { while (r.next()) items.add(map(r)); }
            }
            return new Page(List.copyOf(items), page, pageCount, total);
        }
    }

    public Page searchSelection(String keyword, int requestedPage) throws SQLException {
        String q = keyword == null ? "" : keyword.strip();
        int page = Math.max(1, requestedPage);
        String where = " WHERE (?='' OR LOCATE(LOWER(?),LOWER(n.maNCC))>0 OR LOCATE(LOWER(?),LOWER(n.tenNCC))>0)";
        try (Connection c = dataSource.getConnection()) {
            long total;
            try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM tblNhaCungCap n" + where)) {
                p.setString(1, q); p.setString(2, q); p.setString(3, q);
                try (ResultSet r = p.executeQuery()) { r.next(); total = r.getLong(1); }
            }
            int pageCount = Math.max(1, (int)Math.min(Integer.MAX_VALUE, (total + 19) / 20));
            page = Math.min(page, pageCount);
            String sql = "SELECT n.id,tv.tenDangNhap,n.maNCC,n.tenNCC,n.diaChi,n.soDienThoai,n.email,n.version "
                    + "FROM tblNhaCungCap n JOIN tblThanhVien tv ON tv.id=n.id" + where
                    + " ORDER BY n.id LIMIT 20 OFFSET ?";
            List<NhaCungCap> items = new ArrayList<>();
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, q); p.setString(2, q); p.setString(3, q); p.setInt(4, (page - 1) * 20);
                try (ResultSet r = p.executeQuery()) { while (r.next()) items.add(map(r)); }
            }
            return new Page(List.copyOf(items), page, pageCount, total);
        }
    }

    private static void bindSearch(PreparedStatement p, String q) throws SQLException {
        p.setString(1, q); p.setString(2, q); p.setString(3, q); p.setString(4, q);
    }

    public NhaCungCap find(long id) throws SQLException {
        try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT n.id,tv.tenDangNhap,n.maNCC,n.tenNCC,n.diaChi,n.soDienThoai,n.email,n.version FROM tblNhaCungCap n JOIN tblThanhVien tv ON tv.id=n.id WHERE n.id=?")) {
            p.setLong(1, id); try (ResultSet r = p.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }

    public List<NhaCungCap> listForSelection(String keyword) throws SQLException {
        String q = keyword == null ? "" : keyword.strip();
        String pattern = q;
        String sql = "SELECT n.id,tv.tenDangNhap,n.maNCC,n.tenNCC,n.diaChi,n.soDienThoai,n.email,n.version FROM tblNhaCungCap n "
                + "JOIN tblThanhVien tv ON tv.id=n.id WHERE (?='' OR LOCATE(LOWER(?),LOWER(n.maNCC))>0 OR LOCATE(LOWER(?),LOWER(n.tenNCC))>0) ORDER BY n.id LIMIT 20";
        try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1,q); p.setString(2,pattern); p.setString(3,pattern);
            List<NhaCungCap> result = new ArrayList<>(); try (ResultSet r=p.executeQuery()) { while(r.next()) result.add(map(r)); }
            return List.copyOf(result);
        }
    }

    public NhaCungCap create(String name, String address, String phone, String phoneCanonical,
                             String email, String emailCanonical) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                String username = "ncc_" + UUID.randomUUID().toString().replace("-", "");
                char[] random = PasswordHasher.randomPassword();
                String hash;
                try { hash = PasswordHasher.hash(random); } finally { java.util.Arrays.fill(random, '\0'); }
                long id;
                try (PreparedStatement p = c.prepareStatement("INSERT INTO tblThanhVien(tenDangNhap,matKhauHash) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1,username); p.setString(2,hash); p.executeUpdate();
                    try (ResultSet keys=p.getGeneratedKeys()) { if(!keys.next()) throw new SQLException("Member id missing"); id=keys.getLong(1); }
                }
                try (PreparedStatement p = c.prepareStatement("INSERT INTO tblNhaCungCap(id,tenNCC,diaChi,soDienThoai,soDienThoaiChuan,email,emailChuan) VALUES (?,?,?,?,?,?,?)")) {
                    p.setLong(1,id); p.setString(2,name); p.setString(3,address); p.setString(4,phone); p.setString(5,phoneCanonical); p.setString(6,email); p.setString(7,emailCanonical); p.executeUpdate();
                }
                String code=BusinessCode.fromId("NCC",id);
                try (PreparedStatement p=c.prepareStatement("UPDATE tblNhaCungCap SET maNCC=? WHERE id=?")) { p.setString(1,code); p.setLong(2,id); p.executeUpdate(); }
                c.commit(); return find(id);
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    public boolean update(long id, long expectedVersion, String name, String address, String phone,
                          String phoneCanonical, String email, String emailCanonical) throws SQLException {
        String sql="UPDATE tblNhaCungCap SET tenNCC=?,diaChi=?,soDienThoai=?,soDienThoaiChuan=?,email=?,emailChuan=?,version=version+1 WHERE id=? AND version=?";
        try(Connection c=dataSource.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,name);p.setString(2,address);p.setString(3,phone);p.setString(4,phoneCanonical);p.setString(5,email);p.setString(6,emailCanonical);p.setLong(7,id);p.setLong(8,expectedVersion);
            return p.executeUpdate()==1;
        }
    }

    public DeleteResult deleteIfUnused(long id) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT id FROM tblNhaCungCap WHERE id=? FOR UPDATE")) {
                    p.setLong(1, id);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            return DeleteResult.NOT_FOUND;
                        }
                    }
                }

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT 1 FROM tblPhieuNhap WHERE nhaCungCapId=? LIMIT 1")) {
                    p.setLong(1, id);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) {
                            c.rollback();
                            return DeleteResult.IN_USE;
                        }
                    }
                }

                try (PreparedStatement p = c.prepareStatement("DELETE FROM tblNhaCungCap WHERE id=?")) {
                    p.setLong(1, id);
                    if (p.executeUpdate() != 1) {
                        c.rollback();
                        return DeleteResult.NOT_FOUND;
                    }
                }
                try (PreparedStatement p = c.prepareStatement("DELETE FROM tblThanhVien WHERE id=?")) {
                    p.setLong(1, id);
                    if (p.executeUpdate() != 1) throw new SQLException("Supplier member row missing");
                }
                c.commit();
                return DeleteResult.DELETED;
            } catch (SQLException | RuntimeException e) {
                boolean rollbackSucceeded = true;
                try {
                    c.rollback();
                } catch (SQLException rollbackError) {
                    rollbackSucceeded = false;
                    e.addSuppressed(rollbackError);
                }
                if (rollbackSucceeded && e instanceof SQLException sqlError && "23000".equals(sqlError.getSQLState())) {
                    return DeleteResult.IN_USE;
                }
                throw e;
            }
        }
    }

    private static NhaCungCap map(ResultSet r) throws SQLException {
        return new NhaCungCap(r.getLong("id"),r.getString("tenDangNhap"),null,r.getString("maNCC"),r.getString("tenNCC"),r.getString("diaChi"),r.getString("soDienThoai"),r.getString("email"),r.getLong("version"));
    }
}
