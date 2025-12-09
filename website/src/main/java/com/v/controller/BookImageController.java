package com.v.controller;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.v.model.Book;
import com.v.service.BookService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/book")
public class BookImageController {

    private final BookService bookService;
    private final ResourceLoader resourceLoader;

    public BookImageController(BookService bookService, ResourceLoader resourceLoader) {
        this.bookService = bookService;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Dùng: /book/{id}/image/{index}?w=...&h=...
     * Quy ước:
     * - index=1 -> Book.imageUrl (ảnh chính)
     * - index>=2 -> cố thử gọi getImage{index-1}() nếu entity có (ví dụ
     * image1..image5 dạng String URL)
     * - nếu không có URL -> 404
     */
    @GetMapping("/{id}/image/{index}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable("id") Long id,
            @PathVariable("index") int index,
            @RequestParam(value = "w", required = false) Integer w,
            @RequestParam(value = "h", required = false) Integer h) {
        Book b = bookService.findById(id);
        if (b == null)
            return ResponseEntity.notFound().build();

        String url = pickUrl(b, index);
        if (url == null || url.isBlank())
            return ResponseEntity.notFound().build();

        byte[] data = loadBytes(url.trim());
        if (data == null || data.length == 0)
            return ResponseEntity.notFound().build();

        String contentType = detectImageContentType(data);

        // Resize nếu có w/h
        if (w != null || h != null) {
            try {
                int width = (w != null ? Math.max(1, w) : 0);
                int height = (h != null ? Math.max(1, h) : 0);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                var thumb = Thumbnails.of(new ByteArrayInputStream(data))
                        .outputQuality(0.85f);

                if (width > 0 && height > 0) {
                    thumb.size(width, height)
                            .crop(net.coobird.thumbnailator.geometry.Positions.CENTER);
                } else if (width > 0) {
                    thumb.width(width);
                } else {
                    thumb.height(height);
                }

                // Cố gắng giữ định dạng gần gốc
                if (MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
                    thumb.outputFormat("png");
                } else if ("image/webp".equals(contentType)) {
                    thumb.outputFormat("webp");
                } else if (MediaType.IMAGE_GIF_VALUE.equals(contentType)) {
                    thumb.outputFormat("gif");
                } else {
                    thumb.outputFormat("jpeg");
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                }

                thumb.toOutputStream(out);
                data = out.toByteArray();
            } catch (Exception ignore) {
                // nếu resize lỗi -> trả ảnh gốc
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).cachePublic())
                .body(data);
    }

    /**
     * Chọn URL theo index (1 = ảnh chính imageUrl; >=2 thử getImage{index-1}() nếu
     * có).
     */
    private static String pickUrl(Book b, int idx) {
        if (idx <= 1) {
            return b.getImageUrl();
        }
        // Nếu Book có thêm getter getImage1()..getImage5() (String), sẽ dùng được luôn
        try {
            var m = b.getClass().getMethod("getImage" + (idx - 1));
            Object val = m.invoke(b);
            return val == null ? null : String.valueOf(val);
        } catch (NoSuchMethodException e) {
            return null; // entity không có ảnh phụ -> 404
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tải bytes từ URL/đường dẫn: hỗ trợ http(s), file:, classpath:, và đường dẫn
     * thường (coi như file:).
     */
    private byte[] loadBytes(String location) {
        try {
            String loc = normalizeLocation(location);
            Resource res = resourceLoader.getResource(loc);
            try (InputStream is = res.getInputStream()) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeLocation(String location) {
        String lower = location.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("file:") || lower.startsWith("classpath:")) {
            return location;
        }
        // Nếu là đường dẫn hệ thống/relative -> prepends file:
        return (location.startsWith("/") ? "file:" : "file:") + location;
    }

    /** Nhận diện content-type từ magic bytes (thêm GIF). */
    private static String detectImageContentType(byte[] bytes) {
        if (bytes != null && bytes.length >= 12) {
            // PNG
            if (bytes[0] == (byte) 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
                return MediaType.IMAGE_PNG_VALUE;
            }
            // JPEG
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
                return MediaType.IMAGE_JPEG_VALUE;
            }
            // WEBP: "RIFF....WEBP"
            if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                    && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
                return "image/webp";
            }
            // AVIF: "....ftypavif"
            if (bytes[4] == 'f' && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p'
                    && bytes[8] == 'a' && bytes[9] == 'v' && bytes[10] == 'i' && bytes[11] == 'f') {
                return "image/avif";
            }
            // GIF: "GIF8"
            if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8') {
                return MediaType.IMAGE_GIF_VALUE;
            }
        }
        return MediaType.IMAGE_JPEG_VALUE;
    }
}
