package ru.rostislav.shop.service;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.rostislav.shop.model.entity.Product;
import ru.rostislav.shop.model.entity.PurchaseOrder;
import ru.rostislav.shop.model.dto.OrderRequest;
import ru.rostislav.shop.model.dto.OrderView;
import ru.rostislav.shop.exception.ResourceNotFoundException;
import ru.rostislav.shop.exception.StockConflictException;
import ru.rostislav.shop.repository.OrderRepository;

/** Оформление и чтение заказов с атомарным списанием остатков. */
@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductService products;

    /**
     * Подключает хранилище заказов и общий доступ к активным товарам.
     *
     * @param orders репозиторий заказов
     * @param products сервис каталога
     */
    public OrderService(OrderRepository orders, ProductService products) {
        this.orders = orders;
        this.products = products;
    }

    /**
     * Объединяет повторяющиеся товары, блокирует их по возрастанию ID и списывает остатки.
     * Позиции сохраняют название и цену, актуальные в момент оформления.
     * Заказ, его позиции и остатки изменяются в одной транзакции.
     *
     * @param request проверенные данные покупателя и товары с количеством
     * @return сохранённый заказ с идентификатором, позициями и полной суммой в рублях
     * @throws ResourceNotFoundException если какой-либо товар отсутствует или архивирован
     * @throws StockConflictException если остатка хотя бы по одному товару недостаточно
     */
    @Transactional
    public OrderView placeOrder(OrderRequest request) {
        Map<Long, Integer> quantitiesByProduct = new TreeMap<>();
        for (OrderRequest.Item item : request.items()) {
            quantitiesByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        }

        PurchaseOrder order = new PurchaseOrder(request.customerName().strip(), request.customerEmail());
        for (Map.Entry<Long, Integer> item : quantitiesByProduct.entrySet()) {
            Product product = products.lockActive(item.getKey());
            int quantity = item.getValue();
            product.deductStock(quantity);
            order.addLine(product, quantity);
        }
        return OrderView.from(orders.save(order));
    }

    /**
     * Читает оформленный заказ и его позиции внутри транзакции.
     *
     * @param id идентификатор заказа
     * @return сохранённый заказ с полной суммой
     * @throws ResourceNotFoundException если заказ отсутствует
     */
    @Transactional(readOnly = true)
    public OrderView get(long id) {
        return orders.findById(id)
                .map(OrderView::from)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ", id));
    }
}
