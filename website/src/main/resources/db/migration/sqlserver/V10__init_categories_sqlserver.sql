-- V10: initialize categories (SQL Server)
SET NOCOUNT ON;
IF OBJECT_ID('categories', 'U') IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 1)
        INSERT INTO categories (id, name) VALUES (1, N'Sách bán chạy');
    IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 2)
        INSERT INTO categories (id, name) VALUES (2, N'Sách thiếu nhi');
    IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 3)
        INSERT INTO categories (id, name) VALUES (3, N'Sách kỹ năng');
END
