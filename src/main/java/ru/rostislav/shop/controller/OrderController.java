package ru.rostislav.shop.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.rostislav.shop.model.dto.OrderRequest;
import ru.rostislav.shop.model.dto.OrderView;
import ru.rostislav.shop.service.OrderService;

/** HTTP-интерфейс оформления и просмотра заказов. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orders;

    /**
     * Получает сервис заказов от Spring.
     *
     * @param orders сервис заказов
     */
    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    /**
     * Оформляет заказ и возвращает адрес созданного ресурса.
     *
     * @param request JSON с покупателем и списком товаров
     * @return HTTP 201 с заказом и заголовком Location
     */
    @PostMapping
    public ResponseEntity<OrderView> create(@RequestBody @Valid OrderRequest request) {
        OrderView order = orders.placeOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    /**
     * Читает оформленный заказ.
     *
     * @param id идентификатор заказа
     * @return HTTP 200 с заказом и его позициями
     */
    @GetMapping("/{id}")
    public OrderView get(@PathVariable @Positive long id) {
        return orders.get(id);
    }
}
