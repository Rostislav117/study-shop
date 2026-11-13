package ru.rostislav.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.rostislav.shop.domain.PurchaseOrder;

/** Чтение и сохранение заказов. JPA сохраняет новые позиции каскадно из заказа. */
public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {
}
