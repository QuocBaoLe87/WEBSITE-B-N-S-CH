package com.v.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.v.dto.RatingAgg;
import com.v.model.Book;
import com.v.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

        // Trung bình rating (ép kiểu Double cho chắc)
        @Query("select coalesce(avg(r.rating), 0.0) from Review r where r.book.id = :bookId")
        Double averageRatingByBookId(@Param("bookId") Long bookId);

        // Tổng số review
        @Query("select count(r) from Review r where r.book.id = :bookId")
        long countByBookId(@Param("bookId") Long bookId);

        // Phân trang tất cả reviews, load kèm user & book để tránh N+1
        @EntityGraph(attributePaths = { "user", "book" })
        Page<Review> findAll(Pageable pageable);

        // Dùng ở service hiện tại
        @EntityGraph(attributePaths = { "user", "book" })
        List<Review> findByBook(Book book);

        // Tuỳ chọn: lấy mới nhất trước
        @EntityGraph(attributePaths = { "user", "book" })
        List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);

        // Tuỳ chọn: chặn user review trùng
        boolean existsByBookIdAndUserId(Long bookId, Long userId);

        // =========================
        // TÌM KIẾM (PHÂN TRANG)
        // =========================
        // Tìm theo từ khóa trên: tên book, tên/username/email user, hoặc nội dung
        // bình luận (không phân biệt hoa/thường)
        @EntityGraph(attributePaths = { "user", "book" })
        @Query("""
                        select r from Review r
                          join r.user u
                          join r.book b
                        where (:kw is null or :kw = ''
                           or lower(u.fullName) like lower(concat('%', :kw, '%'))
                           or lower(u.username) like lower(concat('%', :kw, '%'))
                           or lower(u.email)    like lower(concat('%', :kw, '%'))
                           or lower(b.name)     like lower(concat('%', :kw, '%'))
                           or lower(r.comment)  like lower(concat('%', :kw, '%'))
                        )
                        """)
        Page<Review> search(@Param("kw") String kw, Pageable pageable);

        // =========================
        // Gộp trung bình + số lượng theo danh sách book (dùng cho trang chủ)
        // =========================
        @Query("""
                        select new com.v.dto.RatingAgg(r.book.id, avg(r.rating), count(r))
                        from Review r
                        where r.book.id in :ids
                        group by r.book.id
                        """)
        List<RatingAgg> findAggByBookIds(@Param("ids") List<Long> ids);
}
