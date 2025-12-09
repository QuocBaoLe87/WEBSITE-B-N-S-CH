package com.v.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.v.dto.RatingAgg;
import com.v.model.Book;
import com.v.model.Review;
import com.v.model.User;
import com.v.repository.ReviewRepository;
import com.v.service.ReviewService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findByBook(Book book) {
        return reviewRepository.findByBook(book);
    }

    @Override
    @Transactional(readOnly = true)
    public double averageRating(Book book) {
        Double avg = reviewRepository.averageRatingByBookId(book.getId());
        return avg == null ? 0.0 : avg;
    }

    @Override
    @Transactional
    public Review addReview(Book book, User user, int rating, String comment) {
        Review r = new Review();
        r.setBook(book);
        r.setUser(user);
        r.setRating(Math.max(1, Math.min(5, rating)));
        r.setComment(comment);
        r.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, RatingAgg> ratingAgg(Collection<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty())
            return Collections.emptyMap();
        return reviewRepository.findAggByBookIds(new ArrayList<>(bookIds))
                .stream()
                .collect(Collectors.toMap(RatingAgg::bookId, Function.identity()));
    }
}
