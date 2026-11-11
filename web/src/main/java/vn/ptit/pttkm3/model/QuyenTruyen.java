package vn.ptit.pttkm3.model;

import java.math.BigDecimal;

public record QuyenTruyen(Long id, String maQuyenTruyen, BigDecimal giaThue,
                          Long chiTietPhieuNhapId, Long dauTruyenId) { }
