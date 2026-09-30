package com.platter.controller;

import com.platter.dto.CategoryResponse;
import com.platter.repository.CategoryRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(category -> new CategoryResponse(category.getId(), category.getName())).toList();
    }
}
