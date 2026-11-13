package ru.rostislav.shop.error;

/** Запрошенная запись отсутствует или недоступна в каталоге. */
public final class ResourceNotFoundException extends ShopException {
    /**
     * Создаёт ошибку поиска по идентификатору.
     *
     * @param resource название типа ресурса
     * @param id запрошенный идентификатор
     */
    public ResourceNotFoundException(String resource, long id) {
        super("%s с ID %d не найден.".formatted(resource, id));
    }

    /** {@inheritDoc} */
    @Override
    public String code() {
        return "NOT_FOUND";
    }
}
