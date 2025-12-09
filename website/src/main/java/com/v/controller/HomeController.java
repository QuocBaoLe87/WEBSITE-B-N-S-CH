package com.v.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.v.dto.RatingAgg;
import com.v.model.Book;
import com.v.service.AnnouncementService;
import com.v.service.CartService;
import com.v.service.BookService;
import com.v.service.ReviewService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private static final int MAX_FETCH = 10_000; // đủ lớn cho data hiện tại

    private final BookService bookService;
    private final CartService cartService;
    private final AnnouncementService announcementService;
    private final ReviewService reviewService;

    public HomeController(BookService bookService,
            CartService cartService,
            AnnouncementService announcementService,
            ReviewService reviewService) {
        this.bookService = bookService;
        this.cartService = cartService;
        this.announcementService = announcementService;
        this.reviewService = reviewService;
    }

    @GetMapping({ "/", "/books" })
    public String list(
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "all") String category, // all | office | gaming | study
            @RequestParam(required = false) Long categoryId, // ưu tiên nếu có
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String cpu,
            @RequestParam(required = false) String brandFilter,
            Model model) {

        String key = (category == null ? "all" : category.toLowerCase()).trim();

        // Map chuỗi tab cũ -> id danh mục trong DB
        if (categoryId == null && !"all".equals(key)) {
            Map<String, Long> legacy = Map.of(
                    "office", 1L,
                    "study", 2L,
                    "gaming", 3L);
            categoryId = legacy.get(key);
        }

        // 1) Lấy TẤT CẢ (đủ lớn) theo brand + sort để bảo toàn thứ tự hiển thị
        Page<Book> allSorted = bookService.search(brand, 0, MAX_FETCH, sort);
        List<Book> base = new ArrayList<>(allSorted.getContent());

        // 2) Lọc theo categoryId nếu có
        if (categoryId != null) {
            final Long cid = categoryId;
            base = base.stream()
                    .filter(l -> l.getCategory() != null
                            && l.getCategory().getId() != null
                            && l.getCategory().getId().equals(cid))
                    .collect(Collectors.toList());
        }

        // 3) Lọc các bộ lọc phụ trong bộ nhớ
        List<Book> filtered = base.stream()
                .filter(l -> {
                    // Giá
                    if (priceRange != null && !priceRange.isBlank()) {
                        String[] p = priceRange.split("-");
                        double min = p[0].isEmpty() ? 0 : Double.parseDouble(p[0]);
                        double max = (p.length < 2 || p[1].isEmpty()) ? Double.MAX_VALUE : Double.parseDouble(p[1]);
                        if (l.getPrice() < min || l.getPrice() > max)
                            return false;
                    }
                    // RAM
                    if (ram != null && !ram.isBlank()) {
                        int r = Integer.parseInt(ram.replace(" GB", "").trim());
                        if (l.getRam() != r)
                            return false;
                    }
                    // CPU
                    if (cpu != null && !cpu.isBlank()) {
                        if (l.getCpu() == null || !l.getCpu().contains(cpu))
                            return false;
                    }
                    // Hãng
                    if (brandFilter != null && !brandFilter.isBlank()) {
                        if (l.getBrand() == null || !l.getBrand().equalsIgnoreCase(brandFilter))
                            return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 4) TỰ PHÂN TRANG sau khi đã lọc
        int total = filtered.size();
        int lastPage = (total == 0) ? 0 : Math.max(0, (int) Math.ceil((double) total / size) - 1);
        int pageClamped = Math.min(Math.max(page, 0), lastPage);

        int from = Math.min(pageClamped * size, total);
        int to = Math.min(from + size, total);
        List<Book> pageContent = (from < to) ? filtered.subList(from, to) : List.of();

        Page<Book> pageBooks = new PageImpl<>(pageContent, PageRequest.of(pageClamped, size), total);

        // 4.5) TÍNH RATING CHO CÁC SẢN PHẨM TRONG TRANG HIỆN TẠI
        var ids = pageContent.stream().map(Book::getId).toList();
        Map<Long, RatingAgg> aggMap = reviewService.ratingAgg(ids);

        // % = avg/5*100 (làm tròn) để fill sao
        Map<Long, Integer> ratingPct = aggMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (int) Math.round(((e.getValue().avg() == null ? 0.0 : e.getValue().avg()) / 5.0) * 100)));

        // 5) Model attributes cho view
        model.addAttribute("products", pageContent);
        model.addAttribute("category", key);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("sort", sort);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("ram", ram);
        model.addAttribute("cpu", cpu);
        model.addAttribute("brandFilter", brandFilter);

        model.addAttribute("pageBooks", pageBooks);
        model.addAttribute("currentPage", pageClamped);
        model.addAttribute("totalPages", pageBooks.getTotalPages());
        model.addAttribute("brand", brand);

        // Đưa rating ra view
        model.addAttribute("ratingAgg", aggMap);
        model.addAttribute("ratingPct", ratingPct);

        // Lấy danh sách brand từ DB để hiển thị filter động
        List<String> allBrands = bookService.getAllBrands();
        model.addAttribute("allBrands", allBrands);

        // Badge giỏ hàng + Thông báo
        model.addAttribute("cartItemCount", cartService.getItemCount());
        announcementService.getActive().ifPresent(a -> model.addAttribute("activeAnn", a));

        return "home";
    }
}
