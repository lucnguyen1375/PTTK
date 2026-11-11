ALTER TABLE tblNhaCungCap
    ADD CONSTRAINT ck_NCC_emailBatBuoc CHECK (CHAR_LENGTH(TRIM(email)) > 0),
    ADD CONSTRAINT ck_NCC_emailChuanBatBuoc CHECK (CHAR_LENGTH(TRIM(emailChuan)) > 0);

ALTER TABLE tblChiTietPhieuNhap
    ADD CONSTRAINT ck_CTPN_thanhTien CHECK (soLuong * donGiaNhap <= 999999999999);
