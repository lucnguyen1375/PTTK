package vn.ptit.pttkm3.model;

import java.time.LocalDate;

public record PhieuMuon(Long id, String maPhieuMuon, LocalDate ngayMuon,
                        Long nhanVienId, Long khachHangId) { }
