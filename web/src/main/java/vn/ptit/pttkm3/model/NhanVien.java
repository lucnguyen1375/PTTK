package vn.ptit.pttkm3.model;

public final class NhanVien extends ThanhVien {
    private final String maNhanVien;
    private final String hoTen;
    private final String chucVu;

    public NhanVien(Long id, String tenDangNhap, String matKhauHash, String maNhanVien, String hoTen, String chucVu) {
        super(id, tenDangNhap, matKhauHash);
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
    }

    public String getMaNhanVien() { return maNhanVien; }
    public String getHoTen() { return hoTen; }
    public String getChucVu() { return chucVu; }
}
