package vn.ptit.pttkm3.model;

import java.math.BigDecimal;

public record SupplierRevenue(long supplierId, String supplierCode, String supplierName,
                              BigDecimal revenue, int returnInvoiceCount) { }
