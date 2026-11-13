package ru.rostislav.shop.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.rostislav.shop.domain.Category;
import ru.rostislav.shop.domain.Product;
import ru.rostislav.shop.repository.CategoryRepository;
import ru.rostislav.shop.repository.ProductRepository;

/** Заполняет новую БД справочником и товарами для локальной демонстрации. */
@Component
@ConditionalOnProperty(name = "shop.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class DemoData implements ApplicationRunner {
    private final CategoryRepository categories;
    private final ProductRepository products;

    /**
     * Получает репозитории для первоначального заполнения.
     *
     * @param categories репозиторий справочника
     * @param products репозиторий каталога
     */
    public DemoData(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    /**
     * Добавляет справочник и начальные товары, если база ещё не заполнена.
     *
     * @param args аргументы запуска Spring Boot
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categories.count() > 0) {
            return;
        }
        Category stationery = categories.save(new Category("Канцелярия"));
        Category electronics = categories.save(new Category("Электроника"));
        products.saveAll(List.of(
                new Product("Блокнот A5", new BigDecimal("180.00"), 12, stationery),
                new Product("Механический карандаш", new BigDecimal("95.50"), 20, stationery),
                new Product("Кабель USB-C", new BigDecimal("490.00"), 8, electronics)
        ));
    }
}
