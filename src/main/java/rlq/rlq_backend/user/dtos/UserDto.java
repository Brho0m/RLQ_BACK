package rlq.rlq_backend.user.dtos;

import lombok.Data;
import rlq.rlq_backend.user.enums.Roles;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Roles role;
}
