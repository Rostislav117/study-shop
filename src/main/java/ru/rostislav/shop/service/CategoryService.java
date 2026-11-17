package ru.rostislav.shop.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.rostislav.shop.model.dto.CategoryView;
import ru.rostislav.shop.repository.CategoryRepository;

/** Чтение справочника категорий каталога. */
@Service
public class CategoryService {
    private final CategoryRepository categories;

    /**
     * Подключает репозиторий через конструктор.
     *
     * @param categories репозиторий категорий
     */
    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    /**
     * Читает категории по возрастанию идентификатора.
     *
     * @return категории каталога
     */
    @Transactional(readOnly = true)
    public List<CategoryView> list() {
        return categories.findAll(Sort.by("id")).stream().map(CategoryView::from).toList();
    }
}
