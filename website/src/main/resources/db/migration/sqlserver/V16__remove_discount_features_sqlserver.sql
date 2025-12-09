-- ========================================
-- V16: Remove discount-related features
-- ========================================

-- Drop discount-related columns from orders table
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'discount_code')
BEGIN
    ALTER TABLE orders DROP COLUMN discount_code;
END;

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'discount_percent')
BEGIN
    ALTER TABLE orders DROP COLUMN discount_percent;
END;

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'discount_amount')
BEGIN
    ALTER TABLE orders DROP COLUMN discount_amount;
END;

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'total_before_discount')
BEGIN
    ALTER TABLE orders DROP COLUMN total_before_discount;
END;

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'discount_id')
BEGIN
    ALTER TABLE orders DROP COLUMN discount_id;
END;

-- Drop discounts table if exists
IF OBJECT_ID('discounts', 'U') IS NOT NULL
BEGIN
    DROP TABLE discounts;
END;
