package vn.ptit.pttkm3.model;

import java.math.BigDecimal;

public record CTTaiSanCoc(Long id, BigDecimal giaTri, String tinhTrang,
                          boolean daTra, Long taiSanCocId, Long phieuMuonId) { }
