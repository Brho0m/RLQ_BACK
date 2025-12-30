package rlq.rlq_backend.match.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import rlq.rlq_backend.match.enums.MatchStatus;
import rlq.rlq_backend.user.dtos.UserDto;

@Data
public class MatchList {
    private Long id;
    private MatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private List<UserDto> players;
}
