package com.v.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.v.model.Book;
import com.v.model.Review;
import com.v.model.User;
import com.v.service.CartService;
import com.v.service.BookService;
import com.v.service.ProductService;
import com.v.service.ReviewService;
import com.v.service.UserService;

import java.util.Locale;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final BookService bookService;
    private final CartService cartService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final ProductService productService;

    public ProductController(BookService bookService,
            CartService cartService,
            ReviewService reviewService,
            UserService userService,
            ProductService productService) {
        this.bookService = bookService;
        this.cartService = cartService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model m) {
        Book book;
        try {
            book = bookService.findById(id);
        } catch (NoSuchElementException | IllegalArgumentException ex) {
            return "redirect:/?notfound=1";
        }

        // Build carousel từ 5 slot ảnh chi tiết -> /product/{id}/image/{i}
        try {
            var imgs = new java.util.ArrayList<com.v.model.BookImage>();
            for (int i = 1; i <= 5; i++) {
                if (book.hasImage(i)) {
                    imgs.add(new com.v.model.BookImage(book, "/product/" + id + "/image/" + i));
                }
            }
            book.setImages(imgs);
            if ((book.getImageUrl() == null || book.getImageUrl().isBlank()) && !imgs.isEmpty()) {
                book.setImageUrl(imgs.get(0).getUrl());
            }
        } catch (Exception ignore) {
        }

        m.addAttribute("book", book);
        m.addAttribute("reviews", reviewService.findByBook(book));
        m.addAttribute("avgRating", reviewService.averageRating(book));
        return "product";
    }

    @PostMapping("/{id}/add-to-cart")
    public String addToCart(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication auth) {
        try {
            if (quantity < 1)
                quantity = 1;
            cartService.addToCart(id, quantity, auth);
            return "redirect:/cart?added";
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book không tồn tại");
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thêm được vào giỏ");
        }
    }

    // === GỬI REVIEW: AJAX -> trả fragment; bình thường -> redirect ===
    @PostMapping(value = "/{id}/review", produces = MediaType.TEXT_HTML_VALUE)
    public String addReview(@PathVariable Long id,
            @RequestParam(name = "rating", defaultValue = "5") int rating,
            @RequestParam(name = "comment", required = false) String comment,
            Authentication auth,
            HttpServletRequest request,
            RedirectAttributes ra,
            Model model) {
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        if (auth == null || !auth.isAuthenticated()) {
            if (isAjax) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để đánh giá");
            }
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để đánh giá.");
            return "redirect:/login";
        }

        try {
            Book book = bookService.findById(id);
            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                if (isAjax)
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy tài khoản");
                ra.addFlashAttribute("error", "Không tìm thấy tài khoản.");
                return "redirect:/login";
            }

            Review saved = reviewService.addReview(book, user, rating, comment);

            if (isAjax) {
                // Trả HTML của 1 review item để JS prepend vào danh sách
                model.addAttribute("r", saved);
                return "fragments/review-item :: item";
            } else {
                ra.addFlashAttribute("message", "Cảm ơn bạn đã đánh giá sản phẩm!");
                return "redirect:/product/" + id;
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            if (isAjax)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gửi đánh giá thất bại");
            ra.addFlashAttribute("error", "Gửi đánh giá thất bại.");
            return "redirect:/product/" + id;
        }
    }

    // === Trả trung bình rating dạng text/plain để JS cập nhật ===
    @GetMapping(value = "/{id}/avg", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String avgRating(@PathVariable Long id) {
        Book book = bookService.findById(id);
        double avg = reviewService.averageRating(book);
        return String.format(Locale.US, "%.1f", avg);
    }
}
