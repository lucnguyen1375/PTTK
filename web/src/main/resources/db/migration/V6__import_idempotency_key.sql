ALTER TABLE tblPhieuNhap
    ADD COLUMN requestKey CHAR(36) NULL AFTER nhaCungCapId,
    ADD CONSTRAINT uq_PhieuNhap_requestKey UNIQUE (requestKey);
