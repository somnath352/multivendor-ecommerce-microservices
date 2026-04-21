package in.somuxdev.common.events;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class OrderPlacedEvent {
    private String orderId;
    private String userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
}
