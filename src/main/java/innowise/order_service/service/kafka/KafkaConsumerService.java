package innowise.order_service.service.kafka;

import innowise.order_service.dto.payment.PaymentResponseDto;
import innowise.order_service.dto.payment.PaymentStatus;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final OrderService orderService;

    @KafkaListener(topics = "payments", groupId = "order-service-group")
    public void listenGroupFoo(PaymentResponseDto payment) {
        log.info("Received {} event from payments topic for {} order. The status is {}",
                payment.getEventType(), payment.getOrderId(), payment.getStatus());

        orderService.updateOrderStatus(payment.getOrderId(),
               payment.getStatus() == PaymentStatus.COMPLETED ?
                                OrderStatus.PAYMENT_RECEIVED :
                                OrderStatus.PAYMENT_FAILED);
    }
}