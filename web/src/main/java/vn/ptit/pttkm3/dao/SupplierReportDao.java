package vn.ptit.pttkm3.dao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import vn.ptit.pttkm3.model.ReturnRevenueLine;
import vn.ptit.pttkm3.model.SupplierRevenue;

public final class SupplierReportDao {
    private static final String SOURCE_JOINS = " FROM tblChiTietTra ct "
            + "JOIN tblHoaDonTra hd ON hd.id=ct.hoaDonTraId "
            + "JOIN tblChiTietMuon cm ON cm.id=ct.chiTietMuonId "
            + "JOIN tblPhieuMuon pm ON pm.id=cm.phieuMuonId "
            + "JOIN tblQuyenTruyen qt ON qt.id=cm.quyenTruyenId "
            + "JOIN tblChiTietPhieuNhap cpn ON cpn.id=qt.chiTietPhieuNhapId AND cpn.dauTruyenId=qt.dauTruyenId "
            + "JOIN tblPhieuNhap pn ON pn.id=cpn.phieuNhapId "
            + "JOIN tblNhaCungCap n ON n.id=pn.nhaCungCapId ";

    private final DataSource dataSource;

    public SupplierReportDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<SupplierRevenue> summarize(LocalDate from, LocalDate to) throws SQLException {
        String amount = "GREATEST(1,DATEDIFF(ct.ngayTra,pm.ngayMuon))*cm.donGia";
        String sql = "SELECT n.id,n.maNCC,n.tenNCC,SUM(" + amount + ") revenue,COUNT(DISTINCT hd.id) invoiceCount"
                + SOURCE_JOINS + "WHERE hd.ngayLap BETWEEN ? AND ? "
                + "GROUP BY n.id,n.maNCC,n.tenNCC HAVING SUM(" + amount + ")>0 "
                + "ORDER BY revenue DESC,n.maNCC ASC";
        try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, Date.valueOf(from));
            p.setDate(2, Date.valueOf(to));
            try (ResultSet r = p.executeQuery()) {
                List<SupplierRevenue> rows = new ArrayList<>();
                while (r.next()) rows.add(new SupplierRevenue(r.getLong("id"), r.getString("maNCC"),
                        r.getString("tenNCC"), r.getBigDecimal("revenue"), r.getInt("invoiceCount")));
                return List.copyOf(rows);
            }
        }
    }

    public List<ReturnRevenueLine> returnsForSupplier(long supplierId, LocalDate from, LocalDate to)
            throws SQLException {
        String sql = "SELECT hd.id invoiceId,hd.maHoaDonTra,hd.ngayLap,pm.maPhieuMuon,pm.ngayMuon,ct.ngayTra,"
                + "qt.maQuyenTruyen,dt.maDauTruyen,dt.tenDauTruyen,cm.donGia "
                + SOURCE_JOINS
                + "JOIN tblDauTruyen dt ON dt.id=qt.dauTruyenId "
                + "WHERE n.id=? AND hd.ngayLap BETWEEN ? AND ? "
                + "ORDER BY hd.ngayLap,hd.id,ct.chiTietMuonId";
        try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, supplierId);
            p.setDate(2, Date.valueOf(from));
            p.setDate(3, Date.valueOf(to));
            try (ResultSet r = p.executeQuery()) {
                List<ReturnRevenueLine> rows = new ArrayList<>();
                while (r.next()) {
                    LocalDate loanDate = r.getDate("ngayMuon").toLocalDate();
                    LocalDate returnDate = r.getDate("ngayTra").toLocalDate();
                    long days = Math.max(1L, ChronoUnit.DAYS.between(loanDate, returnDate));
                    BigDecimal dailyPrice = r.getBigDecimal("donGia");
                    rows.add(new ReturnRevenueLine(r.getLong("invoiceId"), r.getString("maHoaDonTra"),
                            r.getDate("ngayLap").toLocalDate(), r.getString("maPhieuMuon"), loanDate,
                            returnDate, r.getString("maQuyenTruyen"), r.getString("maDauTruyen"),
                            r.getString("tenDauTruyen"), dailyPrice, days,
                            dailyPrice.multiply(BigDecimal.valueOf(days))));
                }
                return List.copyOf(rows);
            }
        }
    }
}
