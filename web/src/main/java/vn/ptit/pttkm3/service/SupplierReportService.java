package vn.ptit.pttkm3.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import vn.ptit.pttkm3.dao.SupplierReportDao;
import vn.ptit.pttkm3.model.ReturnInvoiceRevenue;
import vn.ptit.pttkm3.model.ReturnRevenueLine;
import vn.ptit.pttkm3.model.SupplierRevenue;

public final class SupplierReportService {
    private final SupplierReportDao dao;

    public SupplierReportService(SupplierReportDao dao) {
        this.dao = dao;
    }

    public List<SupplierRevenue> summarize(LocalDate from, LocalDate to) throws SQLException {
        return dao.summarize(from, to);
    }

    public List<ReturnInvoiceRevenue> details(long supplierId, LocalDate from, LocalDate to) throws SQLException {
        Map<Long, List<ReturnRevenueLine>> grouped = new LinkedHashMap<>();
        for (ReturnRevenueLine line : dao.returnsForSupplier(supplierId, from, to)) {
            grouped.computeIfAbsent(line.invoiceId(), ignored -> new ArrayList<>()).add(line);
        }
        List<ReturnInvoiceRevenue> result = new ArrayList<>();
        for (List<ReturnRevenueLine> lines : grouped.values()) {
            ReturnRevenueLine first = lines.get(0);
            BigDecimal amount = lines.stream().map(ReturnRevenueLine::revenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(new ReturnInvoiceRevenue(first.invoiceId(), first.invoiceCode(), first.invoiceDate(), lines, amount));
        }
        return List.copyOf(result);
    }
}
