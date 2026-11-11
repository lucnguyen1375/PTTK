package vn.ptit.pttkm3.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReturnInvoiceRevenue(long invoiceId, String invoiceCode, LocalDate invoiceDate,
                                   List<ReturnRevenueLine> lines, BigDecimal revenue) {
    public ReturnInvoiceRevenue {
        lines = List.copyOf(lines);
    }
}
