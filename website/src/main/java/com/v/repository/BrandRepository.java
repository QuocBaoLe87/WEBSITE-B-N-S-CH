package com.v.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.v.model.Brand;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Tìm brand theo tên (case-insensitive)
     */
    Optional<Brand> findByNameIgnoreCase(String name);

    /**
     * Kiểm tra brand có tồn tại theo tên
     */
    boolean existsByNameIgnoreCase(String name);
}
