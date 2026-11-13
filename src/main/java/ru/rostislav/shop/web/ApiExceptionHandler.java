package ru.rostislav.shop.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import ru.rostislav.shop.error.ResourceNotFoundException;
import ru.rostislav.shop.error.ShopException;
import ru.rostislav.shop.error.StockConflictException;

/** Преобразует известные ошибки API в статус HTTP и понятный JSON. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Обрабатывает отсутствие запрошенного ресурса.
     *
     * @param exception ошибка поиска
     * @return HTTP 404 и описание ресурса
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException exception) {
        return domainError(HttpStatus.NOT_FOUND, exception);
    }

    /**
     * Обрабатывает недостаток товара на складе.
     *
     * @param exception ошибка списания
     * @return HTTP 409 с запрошенным и доступным количеством
     */
    @ExceptionHandler(StockConflictException.class)
    public ResponseEntity<ApiError> stockConflict(StockConflictException exception) {
        return domainError(HttpStatus.CONFLICT, exception);
    }

    /**
     * Собирает ошибки проверок полей JSON.
     *
     * @param exception результат Bean Validation для тела запроса
     * @return HTTP 400 со списком ошибок по полям
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        return validationError(exception.getBindingResult().getFieldErrors());
    }

    /**
     * Обрабатывает ограничения параметров контроллера и тела запроса.
     *
     * @param exception результат валидации параметров метода
     * @return HTTP 400 с ошибками полей или идентификатора
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> invalidArguments(HandlerMethodValidationException exception) {
        List<FieldError> fieldErrors = exception.getParameterValidationResults().stream()
                .filter(ParameterErrors.class::isInstance)
                .map(ParameterErrors.class::cast)
                .flatMap(errors -> errors.getFieldErrors().stream())
                .toList();
        if (fieldErrors.isEmpty()) {
            return invalidPath();
        }
        return validationError(fieldErrors);
    }

    /**
     * Обрабатывает JSON, который нельзя прочитать как ожидаемый DTO.
     *
     * @return HTTP 400 без внутренних сведений о десериализации
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadableBody() {
        return ResponseEntity.badRequest().body(new ApiError(
                "INVALID_JSON", "Проверьте синтаксис, имена полей и типы значений JSON.", List.of()));
    }

    /**
     * Обрабатывает некорректное значение идентификатора в пути.
     *
     * @return HTTP 400 с сообщением о формате идентификатора
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> invalidPath() {
        return ResponseEntity.badRequest().body(new ApiError(
                "INVALID_PARAMETER", "Идентификатор должен быть положительным целым числом.", List.of()));
    }

    private ResponseEntity<ApiError> domainError(HttpStatus status, ShopException exception) {
        return ResponseEntity.status(status).body(new ApiError(exception.code(), exception.getMessage(), List.of()));
    }

    private ResponseEntity<ApiError> validationError(List<FieldError> errors) {
        List<String> details = errors.stream()
                .map(error -> error.getField() + " — " + error.getDefaultMessage())
                .sorted()
                .toList();
        return ResponseEntity.badRequest().body(new ApiError(
                "VALIDATION_ERROR", "Проверьте поля запроса.", details));
    }
}
