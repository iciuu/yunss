CREATE TABLE tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_name VARCHAR(100) NOT NULL,
    admin_user_id BIGINT,
    expire_time DATETIME,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    carrier_id BIGINT,
    name VARCHAR(50),
    phone VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    avatar VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_carrier_id (carrier_id),
    INDEX idx_phone (phone)
);

CREATE TABLE carrier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(100) NOT NULL,
    license_no VARCHAR(100),
    contact VARCHAR(50),
    phone VARCHAR(20),
    score DECIMAL(3,2),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE freight_source (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    publish_no VARCHAR(50) NOT NULL,
    user_id BIGINT,
    cargo_name VARCHAR(100) NOT NULL,
    cargo_type VARCHAR(30),
    weight DECIMAL(10,2),
    volume DECIMAL(10,2),
    origin_address VARCHAR(255),
    dest_address VARCHAR(255),
    loading_time DATETIME,
    deadline DATETIME,
    vehicle_types VARCHAR(255),
    price_model VARCHAR(30),
    floor_price DECIMAL(10,2),
    show_bid_count TINYINT DEFAULT 1,
    attachments TEXT,
    status VARCHAR(20) NOT NULL,
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status_deadline (status, deadline),
    INDEX idx_route (origin_address(50), dest_address(50))
);

CREATE TABLE bid (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    carrier_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    remark VARCHAR(255),
    attachment VARCHAR(255),
    bid_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified DATETIME,
    status VARCHAR(20) NOT NULL,
    INDEX idx_source_id (source_id),
    INDEX idx_carrier_id (carrier_id),
    INDEX idx_source_amount (source_id, amount),
    INDEX idx_tenant_id (tenant_id)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    source_id BIGINT NOT NULL,
    bid_id BIGINT NOT NULL,
    carrier_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    confirm_time DATETIME,
    finished_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_carrier_id (carrier_id),
    INDEX idx_status (status)
);

CREATE TABLE waybill (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    driver_id BIGINT,
    vehicle_id BIGINT,
    status VARCHAR(20) NOT NULL,
    proof_url VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
);

CREATE TABLE message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receiver_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(100),
    content TEXT,
    related_id BIGINT,
    is_read TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_receiver (receiver_id),
    INDEX idx_read (is_read)
);
