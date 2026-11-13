package ru.rostislav.shop.error;

/** Базовое непроверяемое исключение для ожидаемых ошибок предметной области. */
public abstract class ShopException extends RuntimeException {
    /**
     * Создаёт ошибку с сообщением, которое можно вернуть клиенту.
     *
     * @param message описание причины ошибки
     */
    protected ShopException(String message) {
        super(message);
    }

    /**
     * Возвращает стабильный код для обработки ошибки клиентом.
     *
     * @return код ошибки предметной области
     */
    public abstract String code();
}
