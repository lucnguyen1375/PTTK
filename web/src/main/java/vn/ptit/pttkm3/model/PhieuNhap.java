package vn.ptit.pttkm3.model;

import java.time.LocalDate;

public record PhieuNhap(Long id, String maPhieuNhap, LocalDate ngayLap, Long nhanVienId, Long nhaCungCapId) { }
