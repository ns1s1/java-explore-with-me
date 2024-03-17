package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final EventRepository eventRepository;


    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.convertToCategory(newCategoryDto);

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Категория с таким именем уже существует");
        }

        return categoryMapper.convertToCategoryDto(category);
    }

    @Override
    public CategoryDto update(Long categoryId, NewCategoryDto newCategoryDto) {
        Category category = getCategoryId(categoryId);

        category.setName(newCategoryDto.getName());

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Категория с таким именем уже существует");
        }

        return categoryMapper.convertToCategoryDto(category);
    }

    @Override
    public CategoryDto findById(Long categoryId) {
        Category category = getCategoryId(categoryId);

        return categoryMapper.convertToCategoryDto(category);
    }

    @Override
    public void delete(Long categoryId) {
        Category category = getCategoryId(categoryId);
        List<Event> events = eventRepository.findByCategory(category);

        if (!events.isEmpty()) {
            throw new ValidationException("Нельзя удалить Category, т.к она используется для Event");
        }

        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryDto> findAll(Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        return categoryRepository.findAll(page).stream()
                .map(categoryMapper::convertToCategoryDto)
                .collect(Collectors.toList());
    }

    private Category getCategoryId(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }

}
