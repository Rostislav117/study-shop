package ru.rostislav.shop.dto;

import java.math.BigDecimal;

import ru.rostislav.shop.domain.Product;

/**
 * Представление товара в каталоге.
 *
 * @param id идентификатор товара
 * @param name название товара
 * @param price цена за единицу в рублях
 * @param stock доступный остаток
 * @param category категория товара
 */
public record ProductView(long id, String name, BigDecimal price, int stock, CategoryView category) {

    /**
     * Копирует данные товара и категории в DTO внутри транзакции.
     *
     * @param product товар с присвоенным идентификатором
     * @return данные каталога для сериализации
     */
    public static ProductView from(Product product) {
        return new ProductView(product.getId(), product.getName(), product.getPrice(),
                product.getStock(), CategoryView.from(product.getCategory()));
    }
}
