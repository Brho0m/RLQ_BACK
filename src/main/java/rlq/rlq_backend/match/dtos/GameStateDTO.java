package rlq.rlq_backend.match.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rlq.rlq_backend.match.enums.GameStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDTO {
    private GameStatus phase;
    private MatchQuestionDTO currentQuestion;
    private SelectionPhaseDTO selectionPhaseDTO;
    private QuestionResultDTO results;
    private List<PlayerScoreDTO> leaderboard;
    private Boolean hasSubmitted;
    private MatchDTO match;
    private QuestionResultDTO questionResultDTO;
}
