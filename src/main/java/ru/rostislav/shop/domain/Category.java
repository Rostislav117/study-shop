package ru.rostislav.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Категория каталога, которую могут использовать несколько товаров. */
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    /** Конструктор, который использует JPA при загрузке записи. */
    protected Category() {
    }

    /**
     * Создаёт категорию каталога.
     *
     * @param name уникальное название категории
     */
    public Category(String name) {
        this.name = name;
    }

    /** @return идентификатор категории, присвоенный БД */
    public Long getId() {
        return id;
    }

    /** @return название категории */
    public String getName() {
        return name;
    }
}
