package innowise.order_service.repository;

import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findOrdersByUserId(Long userId, Pageable pageable);

    List<Order> findOrdersByIdIn(List<Long> orderIds);

    Page<Order> findOrdersByStatusAndUserId(OrderStatus status, long userId, Pageable pageable);

    boolean existsOrderByStatusAndUserId(OrderStatus status, long userId);
}
