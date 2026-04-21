package in.somuxdev.common.events;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class OrderItemEvent {
    private String productId;
    private String productName;
    private Integer quantity;
}
