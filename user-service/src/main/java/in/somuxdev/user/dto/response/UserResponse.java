package in.somuxdev.user.dto.response;

import in.somuxdev.user.entity.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}
