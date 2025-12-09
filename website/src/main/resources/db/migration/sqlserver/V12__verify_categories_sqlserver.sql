-- V12: verify categories (SQL Server)
SET NOCOUNT ON;
IF OBJECT_ID('categories', 'U') IS NOT NULL
BEGIN
    DELETE FROM categories WHERE id NOT IN (1,2,3);
    UPDATE categories SET name = N'Sách bán chạy' WHERE id = 1;
    UPDATE categories SET name = N'Sách thiếu nhi' WHERE id = 2;
    UPDATE categories SET name = N'Sách kỹ năng' WHERE id = 3;

    IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 1)
        INSERT INTO categories (id, name) VALUES (1, N'Sách bán chạy');
    IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 2)
        INSERT INTO categories (id, name) VALUES (2, N'Sách thiếu nhi');
    IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 3)
        INSERT INTO categories (id, name) VALUES (3, N'Sách kỹ năng');
END
