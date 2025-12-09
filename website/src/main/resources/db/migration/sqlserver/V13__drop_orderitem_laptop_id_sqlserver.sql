-- V13: drop legacy laptop_id from order_items (SQL Server)
SET NOCOUNT ON;
IF OBJECT_ID('order_items', 'U') IS NOT NULL
BEGIN
    -- Drop foreign key if exists (name may differ)
    IF EXISTS (SELECT * FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('order_items') AND name = 'FKntwt12snw188o34x2m01na7jm')
    BEGIN
        ALTER TABLE order_items DROP CONSTRAINT [FKntwt12snw188o34x2m01na7jm];
    END

    -- Remove column if present
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('order_items') AND name = 'laptop_id')
    BEGIN
        ALTER TABLE order_items DROP COLUMN laptop_id;
    END
END
