CREATE TABLE tblPhieuMuon (
    id BIGINT NOT NULL AUTO_INCREMENT,
    maPhieuMuon VARCHAR(16) NULL,
    ngayMuon DATE NOT NULL,
    nhanVienId BIGINT NOT NULL,
    khachHangId BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_PhieuMuon_ma UNIQUE (maPhieuMuon),
    KEY ix_PhieuMuon_ngay (ngayMuon),
    CONSTRAINT fk_PhieuMuon_NhanVien FOREIGN KEY (nhanVienId) REFERENCES tblNhanVien (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_PhieuMuon_KhachHang FOREIGN KEY (khachHangId) REFERENCES tblKhachHang (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblChiTietMuon (
    id BIGINT NOT NULL AUTO_INCREMENT,
    phieuMuonId BIGINT NOT NULL,
    quyenTruyenId BIGINT NOT NULL,
    donGia DECIMAL(12,0) NOT NULL,
    soLuong TINYINT NOT NULL DEFAULT 1,
    tinhTrangMuon VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_ChiTietMuon_quyenTrongPhieu UNIQUE (phieuMuonId, quyenTruyenId),
    CONSTRAINT fk_ChiTietMuon_PhieuMuon FOREIGN KEY (phieuMuonId) REFERENCES tblPhieuMuon (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_ChiTietMuon_QuyenTruyen FOREIGN KEY (quyenTruyenId) REFERENCES tblQuyenTruyen (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_ChiTietMuon_donGia CHECK (donGia BETWEEN 1 AND 999999999999),
    CONSTRAINT ck_ChiTietMuon_soLuong CHECK (soLuong = 1),
    CONSTRAINT ck_ChiTietMuon_tinhTrang CHECK (CHAR_LENGTH(TRIM(tinhTrangMuon)) BETWEEN 1 AND 255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblHoaDonTra (
    id BIGINT NOT NULL AUTO_INCREMENT,
    maHoaDonTra VARCHAR(16) NULL,
    ngayLap DATE NOT NULL,
    nhanVienId BIGINT NOT NULL,
    khachHangId BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_HoaDonTra_ma UNIQUE (maHoaDonTra),
    KEY ix_HoaDonTra_ngay (ngayLap),
    CONSTRAINT fk_HoaDonTra_NhanVien FOREIGN KEY (nhanVienId) REFERENCES tblNhanVien (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_HoaDonTra_KhachHang FOREIGN KEY (khachHangId) REFERENCES tblKhachHang (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblChiTietTra (
    chiTietMuonId BIGINT NOT NULL,
    hoaDonTraId BIGINT NOT NULL,
    ngayTra DATE NOT NULL,
    tinhTrangTra VARCHAR(255) NOT NULL,
    PRIMARY KEY (chiTietMuonId),
    KEY ix_ChiTietTra_hoaDon (hoaDonTraId),
    KEY ix_ChiTietTra_ngay (ngayTra),
    CONSTRAINT fk_ChiTietTra_ChiTietMuon FOREIGN KEY (chiTietMuonId) REFERENCES tblChiTietMuon (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_ChiTietTra_HoaDon FOREIGN KEY (hoaDonTraId) REFERENCES tblHoaDonTra (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_ChiTietTra_tinhTrang CHECK (CHAR_LENGTH(TRIM(tinhTrangTra)) BETWEEN 1 AND 255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;
