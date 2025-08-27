package innowise.order_service.service.kafka;

import innowise.order_service.dto.payment.PaymentRequestDto;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, PaymentRequestDto> kafkaTemplate;

    private static final String TOPIC = "orders";

    public void sendCreatePaymentEvent(Order order) {
        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
                .eventType("CREATE_ORDER")
                .orderId(order.getId())
                .userId(order.getUserId())
                .paymentAmount(countTotalOrderAmount(order))
                .build();

        log.info("Sending create CREATE_ORDER event to {} for order {}", TOPIC, order.getId());

        try {
            kafkaTemplate.send(TOPIC, paymentRequestDto);
            log.info("Successfully sent CREATE_ORDER event to {} for order {}", TOPIC, order.getId());
        } catch (Exception ex) {
            log.error("Failed to send CREATE_ORDER event to {} for order {}", TOPIC, order.getId(), ex);
        }
    }

    private BigDecimal countTotalOrderAmount(Order order) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem orderItem : order.getOrderItems()) {
            totalAmount = totalAmount.add(orderItem.getItem().getPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }
        return totalAmount;
    }
}
