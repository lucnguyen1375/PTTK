package vn.ptit.pttkm3.model;

import java.time.LocalDate;

public record HoaDonTra(Long id, String maHoaDonTra, LocalDate ngayLap,
                        Long nhanVienId, Long khachHangId) { }
