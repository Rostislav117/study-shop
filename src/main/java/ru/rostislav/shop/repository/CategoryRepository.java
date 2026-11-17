package ru.rostislav.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.rostislav.shop.model.entity.Category;

/** Доступ к справочнику категорий через операции Spring Data JPA. */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
