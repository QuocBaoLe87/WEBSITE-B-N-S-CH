-- V11: update categories (SQL Server) - safely replace content
SET NOCOUNT ON;
IF OBJECT_ID('categories', 'U') IS NOT NULL
BEGIN
    DELETE FROM categories;
    IF EXISTS (SELECT 1 FROM sys.identity_columns WHERE object_id = OBJECT_ID('categories'))
    BEGIN
        DBCC CHECKIDENT('categories', RESEED, 0);
    END

    INSERT INTO categories (id, name) VALUES
    (1, N'Sách bán chạy'),
    (2, N'Sách thiếu nhi'),
    (3, N'Sách kỹ năng');
END
