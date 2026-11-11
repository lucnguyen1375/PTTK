package vn.ptit.pttkm3.model;

public class ThanhVien {
    private final Long id;
    private final String tenDangNhap;
    private final String matKhauHash;

    public ThanhVien(Long id, String tenDangNhap, String matKhauHash) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.matKhauHash = matKhauHash;
    }

    public Long getId() { return id; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhauHash() { return matKhauHash; }

    @Override
    public String toString() {
        return "ThanhVien[id=" + id + ", tenDangNhap=" + tenDangNhap + ", matKhauHash=<redacted>]";
    }
}
