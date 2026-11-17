package ru.rostislav.shop.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.rostislav.shop.model.dto.ProductData;
import ru.rostislav.shop.model.dto.ProductView;
import ru.rostislav.shop.model.dto.StockUpdate;
import ru.rostislav.shop.service.ProductService;

/** HTTP-интерфейс каталога и операций управления товаром. */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    /**
     * Обрабатывает GET каталога.
     *
     * @param query необязательная подстрока названия
     * @return HTTP 200 со списком подходящих товаров
     */
    @GetMapping
    public List<ProductView> list(@RequestParam(name = "q", defaultValue = "") String query) {
        return products.list(query);
    }

    /**
     * Читает товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return HTTP 200 с карточкой товара
     */
    @GetMapping("/{id}")
    public ProductView get(@PathVariable @Positive long id) {
        return products.get(id);
    }

    /**
     * Принимает полный набор полей для нового товара.
     *
     * @param data JSON с полями товара
     * @return HTTP 201 с карточкой и заголовком Location
     */
    @PostMapping
    public ResponseEntity<ProductView> create(@RequestBody @Valid ProductData data) {
        ProductView product = products.create(data);
        return ResponseEntity.created(URI.create("/api/products/" + product.id())).body(product);
    }

    /**
     * Заменяет все редактируемые поля товара.
     *
     * @param id идентификатор товара
     * @param data JSON с полным набором полей
     * @return HTTP 200 с обновлённой карточкой
     */
    @PutMapping("/{id}")
    public ProductView replace(@PathVariable @Positive long id, @RequestBody @Valid ProductData data) {
        return products.replace(id, data);
    }

    /**
     * Обновляет только остаток товара.
     *
     * @param id идентификатор товара
     * @param data JSON с полем stock
     * @return HTTP 200 с обновлённой карточкой
     */
    @PatchMapping("/{id}")
    public ProductView updateStock(@PathVariable @Positive long id, @RequestBody @Valid StockUpdate data) {
        return products.updateStock(id, data.stock());
    }

    /**
     * Скрывает товар из каталога.
     *
     * @param id идентификатор товара
     * @return HTTP 204 с пустым телом
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable @Positive long id) {
        products.archive(id);
        return ResponseEntity.noContent().build();
    }
}
