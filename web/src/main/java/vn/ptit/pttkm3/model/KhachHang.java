package vn.ptit.pttkm3.model;

public final class KhachHang extends ThanhVien {
    private final String maKhachHang;
    private final String ten;
    private final String diaChi;
    private final String soDienThoai;

    public KhachHang(Long id, String tenDangNhap, String matKhauHash, String maKhachHang,
                     String ten, String diaChi, String soDienThoai) {
        super(id, tenDangNhap, matKhauHash);
        this.maKhachHang = maKhachHang;
        this.ten = ten;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
    }

    public String getMaKhachHang() { return maKhachHang; }
    public String getTen() { return ten; }
    public String getDiaChi() { return diaChi; }
    public String getSoDienThoai() { return soDienThoai; }
}
