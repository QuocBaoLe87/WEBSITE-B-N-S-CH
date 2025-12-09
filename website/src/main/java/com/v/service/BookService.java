package com.v.service;

import org.springframework.data.domain.Page;

import com.v.model.Book;

import java.util.List;

public interface BookService {

    /**
     * Tìm & phân trang, có thể sort theo:
     * - "new" → id desc
     * - "priceAsc" → price asc
     * - "priceDesc" → price desc
     */
    Page<Book> search(String brand, int page, int size, String sort);

    /**
     * Tìm theo key của tab cũ: all | office | study | gaming
     * (dùng khi controller truyền "category" dạng chuỗi).
     */
    Page<Book> searchByCategoryKey(String categoryKey, String brand, int page, int size, String sort);

    /**
     * Tìm theo ID danh mục trong DB (dùng khi controller truyền thẳng categoryId).
     */
    Page<Book> searchByCategoryId(Long categoryId, String brand, int page, int size, String sort);

    Book findById(Long id);

    Book save(Book book);

    // Tiện ích cho các block cũ (không phân trang)
    List<Book> getOfficeBooks();

    List<Book> getGamingBooks();

    List<Book> getStudyBooks();

    /**
     * Lấy danh sách tất cả brand có trong DB (distinct, sorted)
     */
    List<String> getAllBrands();
}
