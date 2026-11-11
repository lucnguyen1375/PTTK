package vn.ptit.pttkm3.service;

import java.time.LocalDate;
import java.util.List;
import vn.ptit.pttkm3.model.SupplierRevenue;

public record SupplierReportSnapshot(String id, LocalDate from, LocalDate to, List<SupplierRevenue> rows) {
    public SupplierReportSnapshot {
        rows = List.copyOf(rows);
    }
}
