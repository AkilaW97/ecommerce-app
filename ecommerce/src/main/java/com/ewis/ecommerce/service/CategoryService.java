package com.ewis.ecommerce.service;

import com.ewis.ecommerce.model.Category;
import com.ewis.ecommerce.payload.CategoryDTO;
import com.ewis.ecommerce.payload.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse getAllCategories();
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    String deleteCategory(Long categoryId);
    Category updateCategory(Category category, Long categoryId);
}
