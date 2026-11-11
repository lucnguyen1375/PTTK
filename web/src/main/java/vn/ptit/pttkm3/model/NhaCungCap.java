package vn.ptit.pttkm3.model;

public final class NhaCungCap extends ThanhVien {
    private final String maNCC;
    private final String tenNCC;
    private final String diaChi;
    private final String soDienThoai;
    private final String email;
    private final long version;

    public NhaCungCap(Long id, String tenDangNhap, String matKhauHash, String maNCC,
                      String tenNCC, String diaChi, String soDienThoai, String email, long version) {
        super(id, tenDangNhap, matKhauHash);
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.version = version;
    }

    public String getMaNCC() { return maNCC; }
    public String getTenNCC() { return tenNCC; }
    public String getDiaChi() { return diaChi; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getEmail() { return email; }
    public long getVersion() { return version; }
}
