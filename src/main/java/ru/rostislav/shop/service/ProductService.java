package ru.rostislav.shop.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.rostislav.shop.domain.Category;
import ru.rostislav.shop.domain.Product;
import ru.rostislav.shop.dto.ProductData;
import ru.rostislav.shop.dto.ProductView;
import ru.rostislav.shop.error.ResourceNotFoundException;
import ru.rostislav.shop.repository.CategoryRepository;
import ru.rostislav.shop.repository.ProductRepository;

/** Чтение и изменение каталога с общей политикой доступа к активным товарам. */
@Service
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;

    /**
     * Подключает репозитории каталога.
     *
     * @param products репозиторий товаров
     * @param categories репозиторий категорий
     */
    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    /**
     * Возвращает активные товары, название которых содержит строку поиска.
     *
     * @param query подстрока без учёта регистра, пустое значение отключает фильтр
     * @return подходящие товары по возрастанию идентификатора
     */
    @Transactional(readOnly = true)
    public List<ProductView> list(String query) {
        String search = query.strip().toLowerCase(Locale.ROOT);
        return products.findByArchivedFalseOrderByIdAsc().stream()
                .filter(product -> product.getName().toLowerCase(Locale.ROOT).contains(search))
                .map(ProductView::from)
                .toList();
    }

    /**
     * Читает карточку активного товара.
     *
     * @param id идентификатор товара
     * @return карточка товара
     * @throws ResourceNotFoundException если товар отсутствует или архивирован
     */
    @Transactional(readOnly = true)
    public ProductView get(long id) {
        return products.findByIdAndArchivedFalse(id)
                .map(ProductView::from)
                .orElseThrow(() -> new ResourceNotFoundException("Товар", id));
    }

    /**
     * Сохраняет новый товар с существующей категорией.
     *
     * @param data проверенные поля товара
     * @return сохранённая карточка с новым идентификатором
     * @throws ResourceNotFoundException если категория отсутствует
     */
    @Transactional
    public ProductView create(ProductData data) {
        Product product = new Product(data.name().strip(), data.price(), data.stock(), requireCategory(data.categoryId()));
        return ProductView.from(products.save(product));
    }

    /**
     * Полностью заменяет редактируемые поля активного товара.
     *
     * @param id идентификатор товара
     * @param data проверенные новые поля
     * @return обновлённая карточка
     * @throws ResourceNotFoundException если товар недоступен или категория отсутствует
     */
    @Transactional
    public ProductView replace(long id, ProductData data) {
        Product product = lockActive(id);
        product.replaceDetails(data.name().strip(), data.price(), data.stock(), requireCategory(data.categoryId()));
        return ProductView.from(product);
    }

    /**
     * Устанавливает абсолютный остаток активного товара.
     *
     * @param id идентификатор товара
     * @param stock новый остаток
     * @return обновлённая карточка товара
     * @throws ResourceNotFoundException если товар отсутствует или архивирован
     */
    @Transactional
    public ProductView updateStock(long id, int stock) {
        Product product = lockActive(id);
        product.setStock(stock);
        return ProductView.from(product);
    }

    /**
     * Скрывает товар из каталога. Повторная архивация даёт тот же результат.
     *
     * @param id идентификатор товара
     * @throws ResourceNotFoundException если запись с таким идентификатором отсутствует
     */
    @Transactional
    public void archive(long id) {
        lockExisting(id).archive();
    }

    /**
     * Читает активный товар под блокировкой в текущей транзакции.
     *
     * @param id идентификатор товара
     * @return управляемая JPA сущность
     * @throws ResourceNotFoundException если товар отсутствует или архивирован
     */
    Product lockActive(long id) {
        Product product = lockExisting(id);
        if (product.isArchived()) {
            throw new ResourceNotFoundException("Товар", id);
        }
        return product;
    }

    private Product lockExisting(long id) {
        return products.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Товар", id));
    }

    private Category requireCategory(long id) {
        return categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Категория", id));
    }
}
