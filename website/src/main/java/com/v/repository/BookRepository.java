package com.v.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v.model.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Tìm kiếm theo brand (phân trang)
    Page<Book> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    // Phân trang theo category thực trong DB
    Page<Book> findByCategory_Id(Long categoryId, Pageable pageable);

    // Lấy List theo category (dùng cho block "văn phòng / gaming / sinh viên" cũ)
    List<Book> findByCategory_Id(Long categoryId);

    // Phân trang theo category + keyword brand
    Page<Book> findByCategory_IdAndBrandContainingIgnoreCase(Long categoryId, String brand, Pageable pageable);

    // Phân loại theo giá (nếu nơi khác dùng)
    List<Book> findByPriceBetween(double min, double max);

    List<Book> findByPriceGreaterThan(double price);

    List<Book> findByPriceLessThan(double price);

    // Lấy danh sách brand duy nhất (distinct) từ DB, sắp xếp theo tên
    @Query("SELECT DISTINCT b.brand FROM Book b WHERE b.brand IS NOT NULL ORDER BY b.brand")
    List<String> findDistinctBrands();

    // Đếm số sản phẩm có brand cụ thể (case-insensitive)
    long countByBrandIgnoreCase(String brand);
}
