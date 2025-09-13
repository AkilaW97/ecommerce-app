package com.ewis.ecommerce.service;

import com.ewis.ecommerce.model.Category;

import java.util.List;

public interface CategoryService {

    List <Category> getAllCategories();
    void createCategory(Category category);
    String deleteCategory(Long categoryId);
}
