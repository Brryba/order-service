package innowise.order_service.repository;

import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrdersByUserId(Long userId);
    List<Order> findOrdersByIdIn(List<Long> orderIds);
    List<Order> findOrdersByStatus(OrderStatus orderStatus);
    void deleteOrderById(Long orderId);
}
