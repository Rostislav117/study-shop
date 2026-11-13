package ru.rostislav.shop.error;

/** Оформление заказа конфликтует с текущим остатком товара. */
public final class StockConflictException extends ShopException {
    /**
     * Создаёт ошибку с данными для исправления запроса.
     *
     * @param productId идентификатор товара
     * @param requested запрошенное количество
     * @param available доступный остаток
     */
    public StockConflictException(Long productId, int requested, int available) {
        super("Товар %s: запрошено %d шт., в наличии %d шт.".formatted(productId, requested, available));
    }

    /** {@inheritDoc} */
    @Override
    public String code() {
        return "INSUFFICIENT_STOCK";
    }
}
