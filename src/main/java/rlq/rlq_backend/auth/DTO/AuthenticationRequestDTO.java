package rlq.rlq_backend.auth.DTO;
import lombok.Data;

@Data
public class AuthenticationRequestDTO {
    private String username;
    private String password;
}
