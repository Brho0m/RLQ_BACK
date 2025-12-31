package rlq.rlq_backend.match.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerScoreDTO {
    private Long userId;
    private String username;
    private String firstName;
    private Long score;
}

