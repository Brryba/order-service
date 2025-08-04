package innowise.order_service.repository;

import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrdersByIdIn(List<Long> orderIds);

    List<Order> findOrdersByStatusAndUserId(OrderStatus status, long userId);
}
