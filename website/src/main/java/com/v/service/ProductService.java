package com.v.service;

import java.util.List;

import org.springframework.ui.Model;

import com.v.model.Product;

public interface ProductService {
    List<Product> findAll();
    Product    findById(Long id);
    Product    save(Product product);
    void       deleteById(Long id);

}
