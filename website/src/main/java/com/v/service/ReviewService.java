package com.v.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.v.dto.RatingAgg;
import com.v.model.Book;
import com.v.model.Review;
import com.v.model.User;

public interface ReviewService {

    Review save(Review review);

    List<Review> findByBook(Book book);

    double averageRating(Book book);

    /**
     * Tạo review mới cho 1 book bởi 1 user, tự động clamp rating [1..5] và set
     * createdAt.
     */
    Review addReview(Book book, User user, int rating, String comment);

    /** Lấy trung bình + số lượng review theo lô bookIds, trả về Map theo id. */
    Map<Long, RatingAgg> ratingAgg(Collection<Long> bookIds);
}
