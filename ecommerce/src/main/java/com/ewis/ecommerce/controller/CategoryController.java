package com.ewis.ecommerce.controller;

import com.ewis.ecommerce.model.Category;
import com.ewis.ecommerce.payload.CategoryDTO;
import com.ewis.ecommerce.payload.CategoryResponse;
import com.ewis.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(){
        CategoryResponse categoryResponse = categoryService.getAllCategories();
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
            CategoryDTO deleteCategory = categoryService.deleteCategory(categoryId);
            return new ResponseEntity<>(deleteCategory, HttpStatus.OK);
        }

    @PutMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId){
        CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryDTO, categoryId);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);
    }
}



