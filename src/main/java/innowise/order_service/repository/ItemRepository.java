package innowise.order_service.repository;

import innowise.order_service.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findItemsByIdIn(Collection<Long> orderId);
    boolean existsByName(String name);
}
