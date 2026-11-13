package ru.rostislav.shop.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import ru.rostislav.shop.domain.OrderLine;
import ru.rostislav.shop.domain.PurchaseOrder;

/**
 * Заказ с рассчитанной суммой и зафиксированными реквизитами позиций.
 *
 * @param id идентификатор заказа
 * @param customerName имя покупателя
 * @param customerEmail контактная почта покупателя
 * @param createdAt момент оформления
 * @param items позиции заказа
 * @param total полная стоимость заказа в рублях
 */
public record OrderView(
        long id, String customerName, String customerEmail, Instant createdAt, List<Item> items, BigDecimal total
) {
    /**
     * Преобразует сохранённый заказ вместе с его позициями.
     *
     * @param order заказ, доступный в текущей транзакции
     * @return данные заказа для HTTP-ответа
     */
    public static OrderView from(PurchaseOrder order) {
        List<Item> items = order.getLines().stream().map(Item::from).toList();
        return new OrderView(order.getId(), order.getCustomerName(), order.getCustomerEmail(),
                order.getCreatedAt(), items, order.calculateTotal());
    }

    /**
     * Позиция заказа со стоимостью и названием на момент покупки.
     *
     * @param productId идентификатор товара
     * @param productName название на момент покупки
     * @param unitPrice цена единицы на момент покупки в рублях
     * @param quantity число купленных единиц
     * @param subtotal стоимость позиции в рублях
     */
    public record Item(long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {

        private static Item from(OrderLine line) {
            return new Item(line.getProductId(), line.getProductName(), line.getUnitPrice(),
                    line.getQuantity(), line.calculateSubtotal());
        }
    }
}
