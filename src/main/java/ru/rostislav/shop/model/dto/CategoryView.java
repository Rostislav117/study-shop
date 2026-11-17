package ru.rostislav.shop.model.dto;

import ru.rostislav.shop.model.entity.Category;

/**
 * Представление категории в HTTP-ответе.
 *
 * @param id идентификатор категории
 * @param name название категории
 */
public record CategoryView(long id, String name) {

    /**
     * Копирует данные сущности в независимый объект ответа.
     *
     * @param category категория с присвоенным идентификатором
     * @return данные категории для сериализации
     */
    public static CategoryView from(Category category) {
        return new CategoryView(category.getId(), category.getName());
    }
}
