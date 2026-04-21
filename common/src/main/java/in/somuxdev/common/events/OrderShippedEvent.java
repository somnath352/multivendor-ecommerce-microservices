package in.somuxdev.common.events;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class OrderShippedEvent {
    private String orderId;
    private String userId;
    private String userEmail;
    private String trackingNumber;
}
