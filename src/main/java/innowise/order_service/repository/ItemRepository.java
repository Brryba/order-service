package innowise.order_service.repository;

import innowise.order_service.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findItemById(Long itemId);
    boolean existsByName(String name);
    void deleteItemById(Long itemId);
}
