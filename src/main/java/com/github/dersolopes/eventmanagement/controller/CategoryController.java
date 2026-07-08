package com.github.dersolopes.eventmanagement.controller;

import com.github.dersolopes.eventmanagement.entity.Category;
import com.github.dersolopes.eventmanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository repository;

    @PostMapping
    public Category criar(@RequestBody Category category) {
        return repository.save(category);
    }
}
