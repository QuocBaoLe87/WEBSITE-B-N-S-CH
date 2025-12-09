package com.v.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.v.model.Book;
import com.v.service.BookService;

import java.util.Base64;

@RestController
public class ProductImageController {

    private final BookService bookService;

    // PNG 1x1 trong base64 – dùng khi không có ảnh
    private static final byte[] PLACEHOLDER = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=");

    public ProductImageController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/product/{id}/image/{index}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id, @PathVariable int index) {
        try {
            Book book = bookService.findById(id);
            byte[] data = null;
            if (book != null) {
                data = switch (index) {
                    case 1 -> book.getImage1();
                    case 2 -> book.getImage2();
                    case 3 -> book.getImage3();
                    case 4 -> book.getImage4();
                    case 5 -> book.getImage5();
                    default -> null;
                };
            }
            if (data == null || data.length == 0) {
                // Luôn trả về ảnh hợp lệ để UI không bị 404
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(PLACEHOLDER);
            }
            // Mặc định coi là JPEG; nếu anh lưu PNG/GIF vẫn hiển thị OK trên đa số trình
            // duyệt
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(data);
        } catch (Exception ex) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(PLACEHOLDER);
        }
    }
}
