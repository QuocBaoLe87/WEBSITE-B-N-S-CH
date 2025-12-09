-- V1: Initial schema for SQL Server converted from MySQL dump
SET NOCOUNT ON;

-- Create categories table
IF OBJECT_ID('categories','U') IS NULL
BEGIN
    CREATE TABLE categories (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(120) NOT NULL UNIQUE
    );
END

-- Create users table
IF OBJECT_ID('users','U') IS NULL
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NULL,
        password NVARCHAR(100) NOT NULL,
        role NVARCHAR(30) NOT NULL,
        email NVARCHAR(100) NOT NULL,
        full_name NVARCHAR(255) NULL,
        phone NVARCHAR(30) NULL,
        avatar_url NVARCHAR(512) NULL,
        created_at DATETIME2 NULL,
        email_verified BIT NOT NULL DEFAULT 0,
        enabled BIT NOT NULL DEFAULT 0,
        failed_login_attempts INT NOT NULL DEFAULT 0,
        locked BIT NOT NULL DEFAULT 0,
        updated_at DATETIME2 NULL,
        CONSTRAINT uq_users_email UNIQUE (email),
        CONSTRAINT uq_users_username UNIQUE (username)
    );
END

-- Create brand table
IF OBJECT_ID('brand','U') IS NULL
BEGIN
    CREATE TABLE brand (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        created_at DATETIME2 NOT NULL,
        description NVARCHAR(500) NULL,
        name NVARCHAR(100) NOT NULL,
        updated_at DATETIME2 NULL,
        CONSTRAINT uq_brand_name UNIQUE (name)
    );
END

-- Create books table
IF OBJECT_ID('books','U') IS NULL
BEGIN
    CREATE TABLE books (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(255) NOT NULL,
        brand NVARCHAR(255) NULL,
        configuration NVARCHAR(MAX) NULL,
        price DECIMAL(19,2) NOT NULL,
        image_url NVARCHAR(255) NULL,
        quantity INT NOT NULL DEFAULT 0,
        image1 VARBINARY(MAX) NULL,
        image2 VARBINARY(MAX) NULL,
        image3 VARBINARY(MAX) NULL,
        image4 VARBINARY(MAX) NULL,
        image5 VARBINARY(MAX) NULL,
        featured_index INT NULL,
        category_id BIGINT NOT NULL,
        CONSTRAINT fk_laptops_category FOREIGN KEY (category_id) REFERENCES categories(id),
        CONSTRAINT books_chk_1 CHECK (featured_index IS NULL OR (featured_index <= 5 AND featured_index >= 1))
    );
    CREATE INDEX idx_laptops_brand ON books(brand);
    CREATE INDEX idx_laptops_name ON books(name);
    CREATE INDEX idx_laptops_featured ON books(featured_index);
    CREATE INDEX idx_laptops_category_id ON books(category_id);
END

-- Create discounts table
IF OBJECT_ID('discounts','U') IS NULL
BEGIN
    CREATE TABLE discounts (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        code NVARCHAR(50) NOT NULL,
        percent INT NOT NULL,
        start_date DATE NULL,
        end_date DATE NULL,
        active BIT NOT NULL DEFAULT 1,
        CONSTRAINT uq_discounts_code UNIQUE (code),
        CONSTRAINT discounts_chk_1 CHECK (percent BETWEEN 1 AND 100)
    );
    CREATE INDEX idx_discounts_active ON discounts(active);
    CREATE INDEX idx_discounts_dates ON discounts(start_date, end_date);
END

-- Create orders table
IF OBJECT_ID('orders','U') IS NULL
BEGIN
    CREATE TABLE orders (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        customer_id BIGINT NOT NULL,
        total DECIMAL(19,2) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        status NVARCHAR(255) NOT NULL DEFAULT 'PENDING',
        discount_id BIGINT NULL,
        recipient_name NVARCHAR(255) NOT NULL,
        recipient_email NVARCHAR(255) NOT NULL,
        recipient_address NVARCHAR(255) NULL,
        recipient_phone NVARCHAR(255) NULL,
        discount_code NVARCHAR(255) NULL,
        discount_amount DECIMAL(19,2) NULL,
        discount_percent INT NULL,
        total_before_discount DECIMAL(19,2) NULL,
        payment_method NVARCHAR(32) NULL,
        delivered_at DATETIME2 NULL,
        cancel_reason NVARCHAR(255) NULL,
        canceled_at DATETIME2 NULL,
        canceled_by NVARCHAR(32) NULL,
        CONSTRAINT FK_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id)
    );
    CREATE INDEX idx_orders_customer ON orders(customer_id);
    CREATE INDEX idx_orders_created ON orders(created_at);
    CREATE INDEX idx_orders_status ON orders(status);
    CREATE INDEX idx_orders_discount_code ON orders(discount_code);
END

-- Create order_items table
IF OBJECT_ID('order_items','U') IS NULL
BEGIN
    CREATE TABLE order_items (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        quantity INT NOT NULL,
        unit_price DECIMAL(38,2) NULL,
        book_id BIGINT NOT NULL,
        CONSTRAINT FK_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
        CONSTRAINT FK_order_items_book FOREIGN KEY (book_id) REFERENCES books(id)
    );
    CREATE INDEX idx_items_order ON order_items(order_id);
END

-- Create reviews table
IF OBJECT_ID('reviews','U') IS NULL
BEGIN
    CREATE TABLE reviews (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        comment NVARCHAR(1000) NULL,
        created_at DATETIME2 NULL,
        rating INT NOT NULL,
        user_id BIGINT NULL,
        order_id BIGINT NULL,
        book_id BIGINT NOT NULL,
        CONSTRAINT FK_reviews_book FOREIGN KEY (book_id) REFERENCES books(id),
        CONSTRAINT FK_reviews_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
    CREATE INDEX idx_reviews_user ON reviews(user_id);
END

-- Create password_reset_tokens table
IF OBJECT_ID('password_reset_tokens','U') IS NULL
BEGIN
    CREATE TABLE password_reset_tokens (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        expiry_date DATETIME2 NULL,
        token NVARCHAR(255) NULL,
        user_id BIGINT NOT NULL,
        CONSTRAINT uq_reset_user UNIQUE (user_id),
        CONSTRAINT FK_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
    CREATE INDEX idx_reset_user ON password_reset_tokens(user_id);
END

-- Create return_requests table
IF OBJECT_ID('return_requests','U') IS NULL
BEGIN
    CREATE TABLE return_requests (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        admin_note NVARCHAR(MAX) NULL,
        created_at DATETIME2 NOT NULL,
        photos NVARCHAR(MAX) NULL,
        processed_at DATETIME2 NULL,
        reason NVARCHAR(MAX) NULL,
        received_at DATETIME2 NULL,
        refund_amount DECIMAL(19,2) NULL,
        refunded_at DATETIME2 NULL,
        return_items NVARCHAR(MAX) NULL,
        status NVARCHAR(255) NOT NULL,
        customer_id BIGINT NOT NULL,
        order_id BIGINT NOT NULL,
        CONSTRAINT FK_return_customer FOREIGN KEY (customer_id) REFERENCES users(id),
        CONSTRAINT FK_return_order FOREIGN KEY (order_id) REFERENCES orders(id)
    );
END

-- Create announcements table
IF OBJECT_ID('announcements','U') IS NULL
BEGIN
    CREATE TABLE announcements (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title NVARCHAR(255) NULL,
        image_url NVARCHAR(255) NULL,
        link_url NVARCHAR(255) NULL,
        type NVARCHAR(20) NOT NULL DEFAULT 'MODAL',
        enabled BIT NOT NULL DEFAULT 0,
        dismissible BIT NOT NULL DEFAULT 1,
        start_at DATETIME2 NULL,
        end_at DATETIME2 NULL,
        priority INT NOT NULL DEFAULT 0,
        updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        message NVARCHAR(500) NOT NULL,
        position NVARCHAR(20) NULL,
        variant NVARCHAR(20) NULL
    );
    CREATE INDEX idx_ann_active ON announcements(enabled, start_at, end_at, priority);
END

-- Create chat_messages table
IF OBJECT_ID('chat_messages','U') IS NULL
BEGIN
    CREATE TABLE chat_messages (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(100) NULL,
        content NVARCHAR(MAX) NOT NULL,
        sent_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        role NVARCHAR(50) NOT NULL DEFAULT 'USER',
        recipient NVARCHAR(100) NULL,
        recipient_id BIGINT NULL,
        is_read BIT NOT NULL DEFAULT 0,
        sender NVARCHAR(100) NULL,
        sender_id BIGINT NULL,
        read_at DATETIME2 NULL,
        sender_role NVARCHAR(20) NULL
    );
    CREATE INDEX idx_chat_sent_at ON chat_messages(sent_at);
    CREATE INDEX idx_chat_sender ON chat_messages(sender);
    CREATE INDEX idx_chat_recipient ON chat_messages(recipient);
    CREATE INDEX idx_chat_sender_id ON chat_messages(sender_id);
    CREATE INDEX idx_chat_recipient_id ON chat_messages(recipient_id);
END
