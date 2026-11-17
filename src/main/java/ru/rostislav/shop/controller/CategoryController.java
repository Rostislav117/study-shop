package ru.rostislav.shop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.rostislav.shop.model.dto.CategoryView;
import ru.rostislav.shop.service.CategoryService;

/** HTTP-интерфейс справочника категорий. */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categories;

    /**
     * Получает сервис справочника от Spring.
     *
     * @param categories сервис категорий
     */
    public CategoryController(CategoryService categories) {
        this.categories = categories;
    }

    /** @return HTTP 200 со списком категорий */
    @GetMapping
    public List<CategoryView> list() {
        return categories.list();
    }
}
