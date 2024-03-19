package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto findById(Long categoryId);

    void delete(Long categoryId);

    List<CategoryDto> findAll(int from, int size);

    CategoryDto update(Long categoryId, NewCategoryDto newCategoryDto);
}
