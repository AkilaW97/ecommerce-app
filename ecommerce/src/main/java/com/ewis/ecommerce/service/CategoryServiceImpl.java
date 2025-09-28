package com.ewis.ecommerce.service;

import com.ewis.ecommerce.exceptions.APIException;
import com.ewis.ecommerce.exceptions.MyGlobalExceptionHandler;
import com.ewis.ecommerce.exceptions.ResourceNotFoundException;
import com.ewis.ecommerce.model.Category;
import com.ewis.ecommerce.payload.CategoryDTO;
import com.ewis.ecommerce.payload.CategoryResponse;
import com.ewis.ecommerce.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty())
            throw new APIException("Categories are not available");

        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category categoryFromDb = categoryRepository.findByCategoryName(category.getCategoryName());
        if (categoryFromDb != null) {
            throw new APIException("Category with the name " + categoryDTO.getCategoryName() + " already exists");
        }
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category deleteCategory = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        categoryRepository.delete(deleteCategory);
        return modelMapper.map(deleteCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        // find existing category
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // map incoming DTO -> entity (but here you only need the fields to update)
        Category category = modelMapper.map(categoryDTO, Category.class);

        // update only allowed fields
        savedCategory.setCategoryName(category.getCategoryName());

        // save back to DB
        Category updatedCategory = categoryRepository.save(savedCategory);

        // return updated entity as DTO
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }



}
