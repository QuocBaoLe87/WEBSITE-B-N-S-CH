-- ========================================
-- V5: Remove discount-related features
-- ========================================

-- Drop discount-related columns from orders table
ALTER TABLE orders 
  DROP COLUMN IF EXISTS discount_code,
  DROP COLUMN IF EXISTS discount_percent,
  DROP COLUMN IF EXISTS discount_amount,
  DROP COLUMN IF EXISTS total_before_discount,
  DROP COLUMN IF EXISTS discount_id;

-- Drop discounts table if exists
DROP TABLE IF EXISTS discounts;
