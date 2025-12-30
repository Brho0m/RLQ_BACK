package rlq.rlq_backend.match.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rlq.rlq_backend.match.enums.GameStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchEvent<T> {
    private GameStatus type;
    private T data;
    private Long matchId;
}
