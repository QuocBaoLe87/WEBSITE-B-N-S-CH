package com.v.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.v.model.Brand;
import com.v.repository.BrandRepository;
import com.v.repository.BookRepository;
import com.v.service.BrandService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BookRepository bookRepository;

    public BrandServiceImpl(BrandRepository brandRepository, BookRepository bookRepository) {
        this.brandRepository = brandRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return brandRepository.findById(id);
    }

    @Transactional
    @Override
    public Brand save(Brand brand) {
        return brandRepository.save(brand);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        brandRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return brandRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameAndNotId(String name, Long id) {
        Optional<Brand> existing = brandRepository.findByNameIgnoreCase(name);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }

    @Override
    public long countProductsByBrand(String brandName) {
        return bookRepository.countByBrandIgnoreCase(brandName);
    }
}
