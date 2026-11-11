package vn.ptit.pttkm3.model;

import java.time.LocalDate;

public record TaiSanTra(Long ctTaiSanCocId, Long hoaDonTraId, LocalDate ngayTra, String tinhTrangTra) { }
