package ru.rostislav.shop.web;

import java.util.List;

/**
 * Формат ожидаемой ошибки, возвращаемой контроллерами.
 *
 * @param code стабильный код для клиентской программы
 * @param message пояснение для пользователя
 * @param details список полей и нарушений валидации
 */
public record ApiError(String code, String message, List<String> details) {
}
