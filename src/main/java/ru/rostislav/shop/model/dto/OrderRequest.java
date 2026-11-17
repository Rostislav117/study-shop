package ru.rostislav.shop.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Входные данные оформления заказа.
 *
 * @param customerName имя покупателя
 * @param customerEmail контактная почта покупателя
 * @param items от одной до пятидесяти строк заказа
 */
public record OrderRequest(
        @NotBlank @Size(max = 120) String customerName,
        @NotBlank @Email @Size(max = 254) String customerEmail,
        @NotNull @Size(min = 1, max = 50) List<@NotNull @Valid Item> items
) {
    /**
     * Строка запроса, которая ссылается на товар в каталоге.
     *
     * @param productId идентификатор товара
     * @param quantity количество в пределах от 1 до 10 000
     */
    public record Item(@NotNull @Positive Long productId, @NotNull @Positive @Max(10_000) Integer quantity) {
    }
}
