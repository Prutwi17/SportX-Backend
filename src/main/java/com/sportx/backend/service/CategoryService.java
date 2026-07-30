package com.sportx.backend.service;

import com.sportx.backend.dto.CategoryDTO;
import com.sportx.backend.dto.CategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    CategoryDTO getCategoryById(Long id);
    CategoryDTO createCategory(CategoryRequest request);
    CategoryDTO updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
