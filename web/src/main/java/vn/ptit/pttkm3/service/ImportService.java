package vn.ptit.pttkm3.service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import vn.ptit.pttkm3.model.BusinessCode;
import vn.ptit.pttkm3.model.DauTruyen;
import vn.ptit.pttkm3.model.DataNormalization;

public final class ImportService {
    private static final BigDecimal MAX= new BigDecimal("999999999999");
    private final DataSource ds;
    public ImportService(DataSource ds){this.ds=ds;}
    public boolean supplierExists(long id)throws SQLException{try(Connection c=ds.getConnection();PreparedStatement p=c.prepareStatement("SELECT 1 FROM tblNhaCungCap WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next();}}}
    public List<DauTruyen> searchTitles(String q)throws SQLException{return new vn.ptit.pttkm3.dao.DauTruyenDao(ds).search(q);}
    public vn.ptit.pttkm3.dao.DauTruyenDao.Page searchTitles(String q,int page)throws SQLException{return new vn.ptit.pttkm3.dao.DauTruyenDao(ds).searchPage(q,page);}
    public DauTruyen findTitle(long id)throws SQLException{return new vn.ptit.pttkm3.dao.DauTruyenDao(ds).find(id);}
    public void ensureTitleNew(String title,String author,String category,String publisher,Short year)throws SQLException{
        if(title==null||title.isBlank()||author==null||author.isBlank()||category==null||category.isBlank()||publisher==null||publisher.isBlank())throw new IllegalArgumentException("Tên đầu truyện, tác giả, thể loại và nhà xuất bản là bắt buộc.");
        String t=DataNormalization.text(title),a=DataNormalization.text(author),g=DataNormalization.text(category),p=DataNormalization.text(publisher);
        if(t.length()>255||a.length()>255||g.length()>255||p.length()>255)throw new IllegalArgumentException("Thông tin đầu truyện vượt 255 ký tự.");
        if(year!=null&&(year<1||year>java.time.Year.now().getValue()))throw new IllegalArgumentException("Năm xuất bản phải từ 1 đến năm hiện tại.");
        String key=DataNormalization.titleKey(t,a,g,p,year);
        try(Connection c=ds.getConnection();PreparedStatement q=c.prepareStatement("SELECT id FROM tblDauTruyen WHERE naturalKey=?")){q.setString(1,key);try(ResultSet r=q.executeQuery()){if(r.next())throw new IllegalArgumentException("Đầu truyện đã có trong danh mục. Hãy tìm và chọn đầu truyện đó.");}}
    }
    public record Receipt(long id,String code,LocalDate date,long supplierId,long employeeId,BigDecimal total,int lineCount,int copyCount) { }
    public Receipt findReceipt(String requestKey,long employeeId)throws SQLException{
        try(Connection c=ds.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,maPhieuNhap,ngayLap,nhaCungCapId,nhanVienId,requestKey FROM tblPhieuNhap WHERE requestKey=? AND nhanVienId=?")){p.setString(1,requestKey);p.setLong(2,employeeId);try(ResultSet r=p.executeQuery()){if(!r.next())return null;return loadReceipt(c,r.getLong("id"));}}
    }
    public Receipt commit(ImportDraft.Snapshot draft)throws SQLException {
        if(draft.lines().isEmpty())throw new IllegalArgumentException("Phiếu nhập chưa có dòng.");
        BigDecimal total=BigDecimal.ZERO;
        for(ImportDraft.Line l:draft.lines()){
            validateMoney(l.cost());validateMoney(l.rent());
            if(l.quantity()<1||l.quantity()>10000)throw new IllegalArgumentException("Số lượng mỗi dòng phải từ 1 đến 10.000.");
            total=total.add(l.cost().multiply(BigDecimal.valueOf(l.quantity())));
            if(total.compareTo(MAX)>0)throw new IllegalArgumentException("Tổng phiếu vượt 999.999.999.999 VND.");
        }
        try(Connection c=ds.getConnection()){
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM tblNhaCungCap WHERE id=? FOR UPDATE")){p.setLong(1,draft.supplierId());try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Nhà cung cấp không còn tồn tại.");}}
                LinkedHashMap<String,ResolvedLine> merged=new LinkedHashMap<>();
                for(ImportDraft.Line line:draft.lines()){
                    long titleId=resolveTitle(c,line.title());String key=titleId+"|"+line.cost().toPlainString()+"|"+line.rent().toPlainString();
                    ResolvedLine old=merged.get(key);int quantity=line.quantity()+(old==null?0:old.quantity());if(quantity>10000)throw new IllegalArgumentException("Số lượng gộp vượt 10.000 quyển.");
                    merged.put(key,new ResolvedLine(titleId,quantity,line.cost(),line.rent()));
                }
                LocalDate date=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));long invoiceId;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO tblPhieuNhap(ngayLap,nhanVienId,nhaCungCapId,requestKey) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    p.setDate(1,java.sql.Date.valueOf(date));p.setLong(2,draft.employeeId());p.setLong(3,draft.supplierId());p.setString(4,draft.id());p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Import id missing");invoiceId=r.getLong(1);}}
                String invoiceCode=BusinessCode.fromId("PN",invoiceId);
                try(PreparedStatement p=c.prepareStatement("UPDATE tblPhieuNhap SET maPhieuNhap=? WHERE id=?")){p.setString(1,invoiceCode);p.setLong(2,invoiceId);p.executeUpdate();}
                int copies=0;
                for(ResolvedLine line:merged.values()){
                    long detailId;
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO tblChiTietPhieuNhap(phieuNhapId,dauTruyenId,soLuong,donGiaNhap,giaThueNgay) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                        p.setLong(1,invoiceId);p.setLong(2,line.titleId());p.setInt(3,line.quantity());p.setBigDecimal(4,line.cost());p.setBigDecimal(5,line.rent());p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Import line id missing");detailId=r.getLong(1);}}
                    List<Long> copyIds=new ArrayList<>(line.quantity());
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO tblQuyenTruyen(giaThue,chiTietPhieuNhapId,dauTruyenId) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                        for(int i=0;i<line.quantity();i++){p.setBigDecimal(1,line.rent());p.setLong(2,detailId);p.setLong(3,line.titleId());p.addBatch();}
                        p.executeBatch();try(ResultSet keys=p.getGeneratedKeys()){while(keys.next())copyIds.add(keys.getLong(1));}
                    }
                    if(copyIds.size()!=line.quantity())throw new SQLException("Generated copy count mismatch");
                    try(PreparedStatement p=c.prepareStatement("UPDATE tblQuyenTruyen SET maQuyenTruyen=? WHERE id=?")){
                        for(long copyId:copyIds){p.setString(1,BusinessCode.fromId("QT",copyId));p.setLong(2,copyId);p.addBatch();}p.executeBatch();
                    }
                    copies+=line.quantity();
                }
                c.commit();return new Receipt(invoiceId,invoiceCode,date,draft.supplierId(),draft.employeeId(),total,merged.size(),copies);
            } catch(SQLException|RuntimeException e){c.rollback();throw e;}
        } catch(SQLException duplicate) {
            if("23000".equals(duplicate.getSQLState())){Receipt existing=findReceipt(draft.id(),draft.employeeId());if(existing!=null)return existing;}
            throw duplicate;
        }
    }
    public Receipt receipt(long id,long employeeId)throws SQLException{
        try(Connection c=ds.getConnection();PreparedStatement p=c.prepareStatement("SELECT 1 FROM tblPhieuNhap WHERE id=? AND nhanVienId=?")){p.setLong(1,id);p.setLong(2,employeeId);try(ResultSet r=p.executeQuery()){return r.next()?loadReceipt(c,id):null;}}
    }
    private Receipt loadReceipt(Connection c,long id)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT p.id,p.maPhieuNhap,p.ngayLap,p.nhaCungCapId,p.nhanVienId,COUNT(d.id) lineCount,COALESCE(SUM(d.soLuong),0) copyCount,COALESCE(SUM(d.soLuong*d.donGiaNhap),0) total FROM tblPhieuNhap p LEFT JOIN tblChiTietPhieuNhap d ON d.phieuNhapId=p.id WHERE p.id=? GROUP BY p.id,p.maPhieuNhap,p.ngayLap,p.nhaCungCapId,p.nhanVienId")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){r.next();return new Receipt(r.getLong("id"),r.getString("maPhieuNhap"),r.getDate("ngayLap").toLocalDate(),r.getLong("nhaCungCapId"),r.getLong("nhanVienId"),r.getBigDecimal("total"),r.getInt("lineCount"),r.getInt("copyCount"));}}
    }
    private long resolveTitle(Connection c,ImportDraft.Title title)throws SQLException{
        if(title.existingId()!=null){try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM tblDauTruyen WHERE id=?")){p.setLong(1,title.existingId());try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Đầu truyện đã bị xóa hoặc không còn tồn tại.");}}return title.existingId();}
        String name=DataNormalization.text(title.name()),author=DataNormalization.text(title.author()),category=DataNormalization.text(title.category()),publisher=DataNormalization.text(title.publisher());
        short normalizedYear=title.year()==null?0:title.year();String natural=DataNormalization.titleKey(name,author,category,publisher,title.year());
        Long existing=null;
        try(PreparedStatement p=c.prepareStatement("SELECT id,tenChuan,tacGiaChuan,theLoaiChuan,nhaXuatBanChuan,namXuatBanChuan FROM tblDauTruyen WHERE naturalKey=? FOR UPDATE")){p.setString(1,natural);try(ResultSet r=p.executeQuery()){if(r.next()){if(!name.equals(r.getString(2))||!author.equals(r.getString(3))||!category.equals(r.getString(4))||!publisher.equals(r.getString(5))||normalizedYear!=r.getShort(6))throw new IllegalStateException("Khóa tự nhiên đầu truyện bị xung đột.");existing=r.getLong(1);}}}
        if(existing!=null)return existing;
        long id;
        try(PreparedStatement p=c.prepareStatement("INSERT INTO tblDauTruyen(tenDauTruyen,tacGia,theLoai,nhaXuatBan,namXuatBan,tenChuan,tacGiaChuan,theLoaiChuan,nhaXuatBanChuan,namXuatBanChuan,naturalKey) VALUES (?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,title.name());p.setString(2,title.author());p.setString(3,title.category());p.setString(4,title.publisher());if(title.year()==null)p.setNull(5,Types.SMALLINT);else p.setShort(5,title.year());p.setString(6,name);p.setString(7,author);p.setString(8,category);p.setString(9,publisher);p.setShort(10,normalizedYear);p.setString(11,natural);
            try{p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Title id missing");id=r.getLong(1);}}
            catch(SQLException duplicate){if(!"23000".equals(duplicate.getSQLState()))throw duplicate;try(PreparedStatement q=c.prepareStatement("SELECT id FROM tblDauTruyen WHERE naturalKey=? FOR UPDATE")){q.setString(1,natural);try(ResultSet r=q.executeQuery()){if(!r.next())throw duplicate;return r.getLong(1);}}}
        }
        try(PreparedStatement p=c.prepareStatement("UPDATE tblDauTruyen SET maDauTruyen=? WHERE id=?")){p.setString(1,BusinessCode.fromId("DT",id));p.setLong(2,id);p.executeUpdate();}return id;
    }
    private static void validateMoney(BigDecimal value){if(value==null||value.scale()>0||value.compareTo(BigDecimal.ONE)<0||value.compareTo(MAX)>0)throw new IllegalArgumentException("Giá nhập và giá thuê phải là VND nguyên dương trong giới hạn cho phép.");}
    private record ResolvedLine(long titleId,int quantity,BigDecimal cost,BigDecimal rent) { }
}
