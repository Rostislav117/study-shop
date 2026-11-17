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
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/** Позиция заказа с количеством и неизменяемыми реквизитами товара на момент покупки. */
@Entity
@Table(name = "order_lines", uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "product_id"}))
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 120)
    private String productName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    /** Конструктор, который использует JPA при загрузке записи. */
    protected OrderLine() {
    }

    /**
     * Создаёт позицию из текущих данных товара.
     *
     * @param order заказ, которому принадлежит позиция
     * @param product связанный товар каталога
     * @param quantity число купленных единиц
     */
    OrderLine(PurchaseOrder order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.productName = product.getName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
    }

    /** @return цена одной единицы, умноженная на количество */
    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** @return идентификатор связанного товара */
    public Long getProductId() {
        return product.getId();
    }

    /** @return название товара в момент покупки */
    public String getProductName() {
        return productName;
    }

    /** @return цена одной единицы в момент покупки */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /** @return число купленных единиц */
    public int getQuantity() {
        return quantity;
    }
}
