CREATE TABLE tblPhieuNhap (
    id BIGINT NOT NULL AUTO_INCREMENT,
    maPhieuNhap VARCHAR(16) NULL,
    ngayLap DATE NOT NULL,
    nhanVienId BIGINT NOT NULL,
    nhaCungCapId BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_PhieuNhap_ma UNIQUE (maPhieuNhap),
    KEY ix_PhieuNhap_ngayLap (ngayLap),
    KEY ix_PhieuNhap_ncc_ngay (nhaCungCapId, ngayLap),
    CONSTRAINT fk_PhieuNhap_NhanVien FOREIGN KEY (nhanVienId) REFERENCES tblNhanVien (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_PhieuNhap_NCC FOREIGN KEY (nhaCungCapId) REFERENCES tblNhaCungCap (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblChiTietPhieuNhap (
    id BIGINT NOT NULL AUTO_INCREMENT,
    phieuNhapId BIGINT NOT NULL,
    dauTruyenId BIGINT NOT NULL,
    soLuong INT NOT NULL,
    donGiaNhap DECIMAL(12,0) NOT NULL,
    giaThueNgay DECIMAL(12,0) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_CTPN_gopDong UNIQUE (phieuNhapId, dauTruyenId, donGiaNhap, giaThueNgay),
    CONSTRAINT uq_CTPN_id_dauTruyen UNIQUE (id, dauTruyenId),
    CONSTRAINT fk_CTPN_PhieuNhap FOREIGN KEY (phieuNhapId) REFERENCES tblPhieuNhap (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_CTPN_DauTruyen FOREIGN KEY (dauTruyenId) REFERENCES tblDauTruyen (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_CTPN_soLuong CHECK (soLuong BETWEEN 1 AND 10000),
    CONSTRAINT ck_CTPN_donGia CHECK (donGiaNhap BETWEEN 1 AND 999999999999),
    CONSTRAINT ck_CTPN_giaThue CHECK (giaThueNgay BETWEEN 1 AND 999999999999)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblQuyenTruyen (
    id BIGINT NOT NULL AUTO_INCREMENT,
    maQuyenTruyen VARCHAR(16) NULL,
    giaThue DECIMAL(12,0) NOT NULL,
    chiTietPhieuNhapId BIGINT NOT NULL,
    dauTruyenId BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_QuyenTruyen_ma UNIQUE (maQuyenTruyen),
    KEY ix_QuyenTruyen_dauTruyen (dauTruyenId),
    CONSTRAINT fk_QuyenTruyen_ChiTietDau FOREIGN KEY (chiTietPhieuNhapId, dauTruyenId)
        REFERENCES tblChiTietPhieuNhap (id, dauTruyenId)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_QuyenTruyen_gia CHECK (giaThue BETWEEN 1 AND 999999999999)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;
