package com.ewis.ecommerce.service;

import com.ewis.ecommerce.exceptions.MyGlobalExceptionHandler;
import com.ewis.ecommerce.exceptions.ResourceNotFoundException;
import com.ewis.ecommerce.model.Category;
import com.ewis.ecommerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{

    //private List <Category> categories = new ArrayList<>();
    private Long nextId = 1L;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MyGlobalExceptionHandler myGlobalExceptionHandler;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        //category.setCategoryId(nextId++);
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {

        Category deleteCategory = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId" ,categoryId));

       categoryRepository.delete(deleteCategory);
       return "Deleted category id " + categoryId + " Successfully";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {

        Category savedCategory = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId" ,categoryId));

        // Copy only the fields that are allowed to change
        savedCategory.setCategoryName(category.getCategoryName());

        // Because savedCategory is managed by Hibernate, save() will issue an UPDATE
        return categoryRepository.save(savedCategory);
    }


}
