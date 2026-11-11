package vn.ptit.pttkm3.model;

import java.math.BigDecimal;

public record ChiTietMuon(Long id, Long phieuMuonId, Long quyenTruyenId,
                          BigDecimal donGia, int soLuong, String tinhTrangMuon) { }
