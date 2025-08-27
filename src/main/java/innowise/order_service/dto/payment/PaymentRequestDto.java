package innowise.order_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    private String eventType;
    private Long orderId;
    private Long userId;
    private BigDecimal paymentAmount;
}