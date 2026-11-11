CREATE TABLE tblThanhVien (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenDangNhap VARCHAR(120) NOT NULL,
    matKhauHash VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_ThanhVien_tenDangNhap UNIQUE (tenDangNhap)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblNhanVien (
    id BIGINT NOT NULL,
    maNhanVien VARCHAR(16) NULL,
    hoTen VARCHAR(255) NOT NULL,
    chucVu VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_NhanVien_ma UNIQUE (maNhanVien),
    CONSTRAINT fk_NhanVien_ThanhVien FOREIGN KEY (id) REFERENCES tblThanhVien (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_NhanVien_hoTen CHECK (CHAR_LENGTH(TRIM(hoTen)) BETWEEN 1 AND 255),
    CONSTRAINT ck_NhanVien_chucVu CHECK (CHAR_LENGTH(TRIM(chucVu)) BETWEEN 1 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblNhaCungCap (
    id BIGINT NOT NULL,
    maNCC VARCHAR(16) NULL,
    tenNCC VARCHAR(255) NOT NULL,
    diaChi VARCHAR(500) NOT NULL,
    soDienThoai VARCHAR(16) NOT NULL,
    soDienThoaiChuan VARCHAR(15) NOT NULL,
    email VARCHAR(254) NOT NULL,
    emailChuan VARCHAR(254) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uq_NCC_ma UNIQUE (maNCC),
    CONSTRAINT uq_NCC_dienThoai UNIQUE (soDienThoaiChuan),
    CONSTRAINT uq_NCC_email UNIQUE (emailChuan),
    CONSTRAINT fk_NCC_ThanhVien FOREIGN KEY (id) REFERENCES tblThanhVien (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_NCC_ten CHECK (CHAR_LENGTH(TRIM(tenNCC)) BETWEEN 1 AND 255),
    CONSTRAINT ck_NCC_diaChi CHECK (CHAR_LENGTH(TRIM(diaChi)) BETWEEN 1 AND 500),
    CONSTRAINT ck_NCC_dienThoai CHECK (REGEXP_LIKE(soDienThoai, '^\\+?[0-9]{10,15}$')),
    CONSTRAINT ck_NCC_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblKhachHang (
    id BIGINT NOT NULL,
    maKhachHang VARCHAR(16) NULL,
    ten VARCHAR(255) NOT NULL,
    diaChi VARCHAR(500) NOT NULL,
    soDienThoai VARCHAR(16) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_KhachHang_ma UNIQUE (maKhachHang),
    CONSTRAINT fk_KhachHang_ThanhVien FOREIGN KEY (id) REFERENCES tblThanhVien (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_KhachHang_ten CHECK (CHAR_LENGTH(TRIM(ten)) BETWEEN 1 AND 255),
    CONSTRAINT ck_KhachHang_diaChi CHECK (CHAR_LENGTH(TRIM(diaChi)) BETWEEN 1 AND 500),
    CONSTRAINT ck_KhachHang_dienThoai CHECK (REGEXP_LIKE(soDienThoai, '^\\+?[0-9]{10,15}$'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tblDauTruyen (
    id BIGINT NOT NULL AUTO_INCREMENT,
    maDauTruyen VARCHAR(16) NULL,
    tenDauTruyen VARCHAR(255) NOT NULL,
    tacGia VARCHAR(255) NOT NULL,
    theLoai VARCHAR(255) NOT NULL,
    nhaXuatBan VARCHAR(255) NOT NULL,
    namXuatBan SMALLINT NULL,
    tenChuan VARCHAR(255) NOT NULL,
    tacGiaChuan VARCHAR(255) NOT NULL,
    theLoaiChuan VARCHAR(255) NOT NULL,
    nhaXuatBanChuan VARCHAR(255) NOT NULL,
    namXuatBanChuan SMALLINT NOT NULL,
    naturalKey CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_DauTruyen_ma UNIQUE (maDauTruyen),
    CONSTRAINT uq_DauTruyen_naturalKey UNIQUE (naturalKey),
    CONSTRAINT ck_DauTruyen_ten CHECK (CHAR_LENGTH(TRIM(tenDauTruyen)) BETWEEN 1 AND 255),
    CONSTRAINT ck_DauTruyen_tacGia CHECK (CHAR_LENGTH(TRIM(tacGia)) BETWEEN 1 AND 255),
    CONSTRAINT ck_DauTruyen_theLoai CHECK (CHAR_LENGTH(TRIM(theLoai)) BETWEEN 1 AND 255),
    CONSTRAINT ck_DauTruyen_nxb CHECK (CHAR_LENGTH(TRIM(nhaXuatBan)) BETWEEN 1 AND 255),
    CONSTRAINT ck_DauTruyen_nam CHECK (namXuatBan IS NULL OR namXuatBan BETWEEN 1 AND 9999),
    CONSTRAINT ck_DauTruyen_namChuan CHECK
        (namXuatBanChuan = COALESCE(namXuatBan, 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE app_seed_history (
    seedKey VARCHAR(100) NOT NULL,
    appliedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (seedKey)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;
