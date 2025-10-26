package com.example.backend_api;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    
    private final ProductRepository repository;
    
    @Override
    public void run(String... args) {
        Product p1 = new Product();
        p1.setName("ノートPC");
        p1.setPrice(120000);
        p1.setStock(10);
        repository.save(p1);
        
        Product p2 = new Product();
        p2.setName("マウス");
        p2.setPrice(3000);
        p2.setStock(50);
        repository.save(p2);
    }
}