package vn.ptit.pttkm3.web;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import vn.ptit.pttkm3.model.ReturnInvoiceRevenue;
import vn.ptit.pttkm3.model.SupplierRevenue;
import vn.ptit.pttkm3.service.SupplierReportSnapshot;

final class ReportSnapshotRegistry {
    private static final String KEY = ReportSnapshotRegistry.class.getName();

    static ReportSnapshotRegistry get(HttpSession session) {
        synchronized (session) {
            ReportSnapshotRegistry registry = (ReportSnapshotRegistry) session.getAttribute(KEY);
            if (registry == null) {
                registry = new ReportSnapshotRegistry();
                session.setAttribute(KEY, registry);
            }
            return registry;
        }
    }

    private final Map<String, SupplierReportSnapshot> snapshots = new LinkedHashMap<>();
    private final Map<String, List<ReturnInvoiceRevenue>> invoiceLists = new LinkedHashMap<>();

    synchronized SupplierReportSnapshot put(LocalDate from, LocalDate to, List<SupplierRevenue> rows) {
        String id = UUID.randomUUID().toString();
        SupplierReportSnapshot snapshot = new SupplierReportSnapshot(id, from, to, rows);
        snapshots.put(id, snapshot);
        while (snapshots.size() > 32) {
            String expired = snapshots.keySet().iterator().next();
            snapshots.remove(expired);
            invoiceLists.keySet().removeIf(key -> key.startsWith(expired + ":"));
        }
        return snapshot;
    }

    synchronized SupplierReportSnapshot get(String id) {
        return snapshots.get(id);
    }

    synchronized List<ReturnInvoiceRevenue> invoiceList(String snapshotId, long supplierId) {
        return invoiceLists.get(snapshotId + ":" + supplierId);
    }

    synchronized void putInvoiceList(String snapshotId, long supplierId, List<ReturnInvoiceRevenue> invoices) {
        if (!snapshots.containsKey(snapshotId)) return;
        invoiceLists.put(snapshotId + ":" + supplierId, List.copyOf(invoices));
    }
}
