package vn.ptit.pttkm3.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReturnRevenueLine(long invoiceId, String invoiceCode, LocalDate invoiceDate,
                                String loanCode, LocalDate loanDate, LocalDate returnDate,
                                String copyCode, String titleCode, String titleName,
                                BigDecimal dailyPrice, long calendarDays, BigDecimal revenue) { }
