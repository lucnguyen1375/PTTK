package vn.ptit.pttkm3.model;

import java.time.LocalDate;

public record ChiTietTra(Long chiTietMuonId, Long hoaDonTraId, LocalDate ngayTra, String tinhTrangTra) { }
