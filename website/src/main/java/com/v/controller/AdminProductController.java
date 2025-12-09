package com.v.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.v.model.Product;
import com.v.service.ProductService;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model) {
        try {
            List<Product> products = productService.findAll();
            model.addAttribute("list", products);

            // Calculate statistics
            long totalBooks = products.size();
            long totalQuantity = products.stream()
                    .mapToLong(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                    .sum();
            double totalValue = products.stream()
                    .mapToDouble(p -> (p.getPrice() != null ? p.getPrice() : 0) *
                            (p.getQuantity() != null ? p.getQuantity() : 0))
                    .sum();
            long outOfStock = products.stream()
                    .filter(p -> p.getQuantity() == null || p.getQuantity() == 0)
                    .count();

            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("totalQuantity", totalQuantity);
            model.addAttribute("totalValue", totalValue);
            model.addAttribute("outOfStock", outOfStock);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load products: " + e.getMessage());
        }
        return "admin/products";
    }
}
