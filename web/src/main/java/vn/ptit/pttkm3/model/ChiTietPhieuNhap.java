package vn.ptit.pttkm3.model;

import java.math.BigDecimal;

public record ChiTietPhieuNhap(Long id, Long phieuNhapId, Long dauTruyenId,
                               int soLuong, BigDecimal donGiaNhap, BigDecimal giaThueNgay) { }
