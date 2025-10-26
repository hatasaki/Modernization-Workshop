package com.example.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class HomeController {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @GetMapping("/")
    public String home(Model model) {
        try {
            // Backend API を呼び出し (環境変数で切り替え可能)
            String backendHost = System.getenv().getOrDefault("BACKEND_HOST", "backend-api");
            String apiUrl = "http://" + backendHost + "/api/products";
            Object products = restTemplate.getForObject(apiUrl, Object.class);
            model.addAttribute("products", products);
            model.addAttribute("message", "Backend API から商品データを取得しました!");
        } catch (Exception e) {
            model.addAttribute("message", "Backend API に接続できません: " + e.getMessage());
            model.addAttribute("products", null);
        }
        return "index";
    }
}