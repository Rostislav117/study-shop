package ru.rostislav.shop.repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.rostislav.shop.model.entity.Product;

/** Запросы каталога и блокировка товара для операций записи. */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Читает активные товары вместе с категориями по возрастанию идентификатора.
     *
     * @return список для каталога
     */
    @EntityGraph(attributePaths = "category")
    List<Product> findByArchivedFalseOrderByIdAsc();

    /**
     * Читает активный товар и его категорию.
     *
     * @param id идентификатор товара
     * @return товар или пустой результат
     */
    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndArchivedFalse(long id);

    /**
     * Читает строку товара с блокировкой записи до конца транзакции.
     *
     * @param id идентификатор товара, включая архивные
     * @return товар или пустой результат
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> lockById(@Param("id") long id);
}
