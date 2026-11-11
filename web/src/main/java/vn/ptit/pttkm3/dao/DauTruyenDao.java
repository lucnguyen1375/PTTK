package vn.ptit.pttkm3.dao;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import vn.ptit.pttkm3.model.DauTruyen;
public final class DauTruyenDao {
    private final DataSource ds; public DauTruyenDao(DataSource ds){this.ds=ds;}
    public record Page(List<DauTruyen> items,int page,int pageCount,long total) { }
    public List<DauTruyen> search(String query)throws SQLException{
        return searchPage(query,1).items();
    }
    public Page searchPage(String query,int requestedPage)throws SQLException{
        String q=query==null?"":query.strip();int page=Math.max(1,requestedPage);
        String where=" WHERE (?='' OR LOCATE(LOWER(?),LOWER(maDauTruyen))>0 OR LOCATE(LOWER(?),LOWER(tenDauTruyen))>0)";
        try(Connection c=ds.getConnection()){
            long total;
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM tblDauTruyen"+where)){p.setString(1,q);p.setString(2,q);p.setString(3,q);try(ResultSet r=p.executeQuery()){r.next();total=r.getLong(1);}}
            int pageCount=Math.max(1,(int)Math.min(Integer.MAX_VALUE,(total+19)/20));page=Math.min(page,pageCount);
            String sql="SELECT id,maDauTruyen,tenDauTruyen,tacGia,theLoai,nhaXuatBan,namXuatBan FROM tblDauTruyen"+where+" ORDER BY tenChuan,id LIMIT 20 OFFSET ?";
            List<DauTruyen> items=new ArrayList<>();
            try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,q);p.setString(2,q);p.setString(3,q);p.setInt(4,(page-1)*20);try(ResultSet r=p.executeQuery()){while(r.next())items.add(map(r));}}
            return new Page(List.copyOf(items),page,pageCount,total);
        }
    }
    private static DauTruyen map(ResultSet r)throws SQLException{int y=r.getInt("namXuatBan");Short year=r.wasNull()?null:(short)y;return new DauTruyen(r.getLong("id"),r.getString("maDauTruyen"),r.getString("tenDauTruyen"),r.getString("tacGia"),r.getString("theLoai"),r.getString("nhaXuatBan"),year);}
    public DauTruyen find(long id)throws SQLException{try(Connection c=ds.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,maDauTruyen,tenDauTruyen,tacGia,theLoai,nhaXuatBan,namXuatBan FROM tblDauTruyen WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;int y=r.getInt("namXuatBan");Short year=r.wasNull()?null:(short)y;return new DauTruyen(r.getLong("id"),r.getString("maDauTruyen"),r.getString("tenDauTruyen"),r.getString("tacGia"),r.getString("theLoai"),r.getString("nhaXuatBan"),year);}}}
}
