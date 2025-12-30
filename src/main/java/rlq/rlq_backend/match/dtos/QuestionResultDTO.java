package rlq.rlq_backend.match.dtos;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResultDTO {
    private Long correctAnswerId;
    private List<AnswerResultsDTO> userSelections;
    private List<PlayerScoreDTO> scores;
}
