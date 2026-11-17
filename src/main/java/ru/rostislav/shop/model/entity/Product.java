package ru.rostislav.shop.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

import ru.rostislav.shop.exception.StockConflictException;

/** Товар каталога с ценой в рублях, остатком и признаком архивации. */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean archived;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Конструктор, который использует JPA при загрузке записи. */
    protected Product() {
    }

    /**
     * Создаёт активный товар из проверенных полей каталога.
     *
     * @param name название
     * @param price цена за единицу в рублях
     * @param stock число единиц на складе
     * @param category существующая категория
     */
    public Product(String name, BigDecimal price, int stock, Category category) {
        replaceDetails(name, price, stock, category);
    }

    /**
     * Заменяет редактируемые поля товара.
     *
     * @param name новое название
     * @param price новая цена за единицу в рублях
     * @param stock новый абсолютный остаток
     * @param category категория, к которой будет отнесён товар
     */
    public void replaceDetails(String name, BigDecimal price, int stock, Category category) {
        this.name = name;
        this.category = category;
        changePrice(price);
        setStock(stock);
    }

    /**
     * Устанавливает цену товара в рублях.
     *
     * @param price положительная цена, представленная с точностью до копейки
     * @throws IllegalArgumentException если цена отсутствует, неположительна или требует округления
     */
    public void changePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0 || price.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Цена должна быть положительной и точной до копейки.");
        }
        this.price = price.setScale(2);
    }

    /**
     * Устанавливает остаток после инвентаризации.
     *
     * @param stock новый абсолютный остаток
     * @throws IllegalArgumentException если остаток отрицательный
     */
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Остаток не может быть отрицательным.");
        }
        this.stock = stock;
    }

    /**
     * Списывает со склада положительное количество единиц под блокировкой записи товара.
     *
     * @param quantity количество единиц для заказа
     * @throws IllegalArgumentException если количество неположительное
     * @throws StockConflictException если доступного остатка недостаточно
     */
    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть положительным.");
        }
        if (quantity > stock) {
            throw new StockConflictException(id, quantity, stock);
        }
        stock -= quantity;
    }

    /** Скрывает товар из каталога, сохраняя строку для истории заказов. */
    public void archive() {
        archived = true;
    }

    /** @return идентификатор товара */
    public Long getId() {
        return id;
    }

    /** @return текущее название товара */
    public String getName() {
        return name;
    }

    /** @return текущая цена единицы в рублях */
    public BigDecimal getPrice() {
        return price;
    }

    /** @return текущий остаток в штуках */
    public int getStock() {
        return stock;
    }

    /** @return категория товара */
    public Category getCategory() {
        return category;
    }

    /** @return true, если товар скрыт из каталога */
    public boolean isArchived() {
        return archived;
    }
}
