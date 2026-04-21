package in.somuxdev.common.events;

import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class PaymentSuccessEvent {
    private String paymentId;
    private String orderId;
    private String userId;
    private String userEmail;
    private BigDecimal amount;
}
