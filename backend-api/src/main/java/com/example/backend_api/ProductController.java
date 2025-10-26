package com.example.backend_api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductRepository repository;
    
    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }
}