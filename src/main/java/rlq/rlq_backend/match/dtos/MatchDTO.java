package rlq.rlq_backend.match.dtos;

import lombok.Data;
import rlq.rlq_backend.match.enums.GameStatus;
import rlq.rlq_backend.match.enums.MatchStatus;
import rlq.rlq_backend.user.dtos.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MatchDTO {
    private Long id;
    private MatchStatus status;
    private GameStatus gameStatus;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private List<UserDto> players;

}
