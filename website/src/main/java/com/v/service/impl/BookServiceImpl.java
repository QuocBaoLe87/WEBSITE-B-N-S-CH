package com.v.service.impl;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.v.model.Book;
import com.v.repository.BookRepository;
import com.v.service.BrandService;
import com.v.service.BookService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepo;
    private final BrandService brandService;

    public BookServiceImpl(BookRepository bookRepo, BrandService brandService) {
        this.bookRepo = bookRepo;
        this.brandService = brandService;
    }

    // ===== Public APIs =====

    @Override
    public Page<Book> search(String brand, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        String kw = sanitize(brand);
        return kw.isEmpty()
                ? bookRepo.findAll(pageable)
                : bookRepo.findByBrandContainingIgnoreCase(kw, pageable);
    }

    @Override
    public Page<Book> searchByCategoryKey(String categoryKey, String brand, int page, int size, String sort) {
        Long cid = mapKeyToCategoryId(categoryKey);
        return searchByCategoryId(cid, brand, page, size, sort);
    }

    @Override
    public Page<Book> searchByCategoryId(Long categoryId, String brand, int page, int size, String sort) {
        // Nếu không có category → fallback về search all
        if (categoryId == null)
            return search(brand, page, size, sort);

        Pageable pageable = buildPageable(page, size, sort);
        String kw = sanitize(brand);

        return kw.isEmpty()
                ? bookRepo.findByCategory_Id(categoryId, pageable)
                : bookRepo.findByCategory_IdAndBrandContainingIgnoreCase(categoryId, kw, pageable);
    }

    @Override
    public Book findById(Long id) {
        return bookRepo.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Book save(Book book) {
        return bookRepo.save(book);
    }

    // ===== Convenience (không phân trang) =====

    @Override
    public List<Book> getOfficeBooks() {
        Long cid = mapKeyToCategoryId("office");
        return bookRepo.findByCategory_Id(cid);
    }

    @Override
    public List<Book> getGamingBooks() {
        Long cid = mapKeyToCategoryId("gaming");
        return bookRepo.findByCategory_Id(cid);
    }

    @Override
    public List<Book> getStudyBooks() {
        Long cid = mapKeyToCategoryId("study");
        return bookRepo.findByCategory_Id(cid);
    }

    @Override
    public List<String> getAllBrands() {
        // Lấy tất cả brands từ bảng Brand, không chỉ từ products
        // Như vậy brand mới thêm sẽ hiển thị ngay cả khi không có sản phẩm
        return brandService.getAllBrands()
                .stream()
                .map(b -> b.getName())
                .collect(Collectors.toList());
    }

    // ===== Helpers =====

    private Pageable buildPageable(int page, int size, String sort) {
        int p = Math.max(page, 0);
        int s = Math.max(size, 1);

        Sort order = switch (sort == null ? "" : sort) {
            case "new" -> Sort.by(Sort.Direction.DESC, "id");
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };
        return PageRequest.of(p, s, order);
        // NOTE: nếu muốn secondary sort ổn định:
        // return PageRequest.of(p, s, order.and(Sort.by("id").ascending()));
    }

    private String sanitize(String text) {
        return text == null ? "" : text.trim();
    }

    /**
     * Map "office"/"study"/"gaming" → ID trong bảng categories.
     * Đổi lại các con số này theo seed của DB anh đang dùng.
     */
    private Long mapKeyToCategoryId(String key) {
        String k = (key == null ? "all" : key.trim().toLowerCase());
        return switch (k) {
            case "office" -> 1L; // Book văn phòng
            case "study" -> 2L; // Book sinh viên
            case "gaming" -> 3L; // Book gaming
            default -> null; // all
        };
    }
}
