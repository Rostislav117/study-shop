package ru.rostislav.shop.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Оформленный заказ, которому принадлежат его позиции. */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 254)
    private String customerEmail;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<OrderLine> lines = new ArrayList<>();

    /** Конструктор, который использует JPA при загрузке записи. */
    protected PurchaseOrder() {
    }

    /**
     * Начинает формирование заказа.
     *
     * @param customerName имя покупателя
     * @param customerEmail контактная электронная почта
     */
    public PurchaseOrder(String customerName, String customerEmail) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.createdAt = Instant.now();
    }

    /**
     * Создаёт позицию со снимком текущей цены и связывает её с заказом.
     *
     * @param product товар каталога
     * @param quantity количество единиц товара
     */
    public void addLine(Product product, int quantity) {
        lines.add(new OrderLine(this, product, quantity));
    }

    /**
     * Складывает суммы позиций по ценам, зафиксированным при покупке.
     *
     * @return полная сумма заказа в рублях
     */
    public BigDecimal calculateTotal() {
        return lines.stream()
                .map(OrderLine::calculateSubtotal)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    /** @return идентификатор заказа */
    public Long getId() {
        return id;
    }

    /** @return имя покупателя */
    public String getCustomerName() {
        return customerName;
    }

    /** @return контактная почта покупателя */
    public String getCustomerEmail() {
        return customerEmail;
    }

    /** @return момент оформления заказа */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** @return неизменяемая копия списка позиций в порядке добавления */
    public List<OrderLine> getLines() {
        return List.copyOf(lines);
    }
}
