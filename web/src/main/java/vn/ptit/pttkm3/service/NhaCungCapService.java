package vn.ptit.pttkm3.service;

import java.sql.SQLException;
import java.util.regex.Pattern;
import vn.ptit.pttkm3.dao.NhaCungCapDao;
import vn.ptit.pttkm3.model.DataNormalization;
import vn.ptit.pttkm3.model.NhaCungCap;

public final class NhaCungCapService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final NhaCungCapDao dao;
    public NhaCungCapService(NhaCungCapDao dao) { this.dao=dao; }
    public NhaCungCapDao.Page search(String q,int page) throws SQLException { return dao.search(q,page); }
    public java.util.List<NhaCungCap> list(String q) throws SQLException { return dao.listForSelection(q); }
    public NhaCungCap find(long id) throws SQLException { return dao.find(id); }
    public NhaCungCap create(String name,String address,String phone,String email) throws SQLException {
        Values v=validate(name,address,phone,email);
        try { return dao.create(v.name,v.address,v.phone,v.phoneCanonical,v.email,v.emailCanonical); }
        catch(SQLException e) { if("23000".equals(e.getSQLState())) throw new DuplicateSupplierException(); throw e; }
    }
    public boolean update(long id,long version,String name,String address,String phone,String email) throws SQLException {
        Values v=validate(name,address,phone,email);
        try { return dao.update(id,version,v.name,v.address,v.phone,v.phoneCanonical,v.email,v.emailCanonical); }
        catch(SQLException e) { if("23000".equals(e.getSQLState())) throw new DuplicateSupplierException(); throw e; }
    }
    private static Values validate(String name,String address,String phone,String email) {
        if(name==null||name.isBlank())throw new IllegalArgumentException("Tên nhà cung cấp là bắt buộc.");
        if(address==null||address.isBlank())throw new IllegalArgumentException("Địa chỉ là bắt buộc.");
        if(phone==null||phone.isBlank())throw new IllegalArgumentException("Số điện thoại là bắt buộc.");
        if(email==null||email.isBlank())throw new IllegalArgumentException("Email là bắt buộc.");
        String n=DataNormalization.text(name),a=DataNormalization.text(address),p=phone==null?"":phone.strip();
        String canonicalPhone=DataNormalization.phone(p),e=email==null?"":email.strip();
        if(e.length()>254||!EMAIL.matcher(e).matches()) throw new IllegalArgumentException("Email không đúng định dạng hoặc vượt 254 ký tự.");
        if(n.length()>255||a.length()>500||p.length()>16) throw new IllegalArgumentException("Tên, địa chỉ hoặc số điện thoại vượt độ dài cho phép.");
        return new Values(n,a,p,canonicalPhone,e,DataNormalization.email(e));
    }
    private record Values(String name,String address,String phone,String phoneCanonical,String email,String emailCanonical) { }
    public static final class DuplicateSupplierException extends RuntimeException { }
}
