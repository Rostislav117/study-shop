package ru.rostislav.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Частичное обновление товара после инвентаризации.
 *
 * @param stock новый абсолютный остаток
 */
public record StockUpdate(@NotNull @Min(0) Integer stock) {
}
