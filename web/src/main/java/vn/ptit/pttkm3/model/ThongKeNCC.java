package vn.ptit.pttkm3.model;

import java.math.BigDecimal;

/** Giá trị tổng hợp tính từ giao dịch; không tương ứng với bảng lưu thống kê. */
public record ThongKeNCC(Long nhaCungCapId, String maNCC, String tenNCC, BigDecimal doanhThu) { }
